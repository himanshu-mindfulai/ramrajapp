package com.mindfulai.Adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.flatdialoglibrary.dialog.FlatDialog;
import com.google.gson.JsonObject;
import com.mindfulai.Activites.*;
import com.mindfulai.Models.CartInformation.CartDetailsInformation;
import com.mindfulai.Models.varientsByCategory.*;
import com.mindfulai.NetworkRetrofit.ApiService;
import com.mindfulai.NetworkRetrofit.ApiUtils;
import com.mindfulai.Utils.CommonUtils;
import com.mindfulai.Utils.CustomProgressDialog;
import com.mindfulai.Utils.SPData;
import com.mindfulai.ministore.R;
import com.valdesekamdem.library.mdtoast.MDToast;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.mindfulai.Utils.CommonUtils.capitalizeWord;

public class ProductsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "ProductsAdapter";
    private int REQUEST_CODE = 10;
    private Context context;
    private List<Datum> responseProductList;
    public ArrayList<Datum> allProductList;
    private String type;
    private Varient varient;


    public ProductsAdapter(final Context context, List<Datum> varientList, String type, int requestCode) {
        try {
            this.context = context;
            this.responseProductList = varientList;
            this.type = type;
            REQUEST_CODE = requestCode;
            allProductList = new ArrayList<>();
            if (SPData.showDiamondPageLayout() && (context instanceof AllProductsActivity))
                allProductList.addAll(responseProductList);
        } catch (Exception e) {
            Log.e(TAG, "ProductsAdapter: " + e);
        }
    }

    public void filterList(List<Datum> filteredList) {
        this.responseProductList = filteredList;
        notifyDataSetChanged();
    }

    public void updateInCartItem(int position, int incart, String id) {
        if (position != -1) {
            for (Varient varient : responseProductList.get(position).getVarients()) {
                if (varient.getId().equals(id)) {
                    varient.setInCart(incart);
                    break;
                }
            }
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView;
        if ((context instanceof AllProductsActivity) && SPData.showDiamondPageLayout()) {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.diamond_product_layout, parent, false);
        } else if ((context instanceof AllProductsActivity) && SPData.showSecondProductView()) {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.custom_product_layout2, parent, false);
        } else if (type.equals("grid") || type.equals("list2"))
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.custom_product_layout, parent, false);
        else
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.custom_product_layout_list, parent, false);

        return new MyViewHolder(itemView);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        showProductItem((MyViewHolder) holder, position);
    }

    @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables"})
    private void showProductItem(final MyViewHolder holder, final int position) {
        //   try {

        if (isDiamondPageRequest()) {

            ArrayList<String> stringArrayList = new ArrayList<>();
            if (position == 0) {
                Varient varient = responseProductList.get(0).getVarients().get(0);
                stringArrayList.add("image".toUpperCase());
                for (Attribute attribute : varient.getAttributes()) {
                    String value = attribute.getAttribute().getName();
                    stringArrayList.add(CommonUtils.capitalizeWord(value));
                }
                stringArrayList.add("Price".toUpperCase());
                stringArrayList.add("Add".toUpperCase());
            } else {
                Varient currentVarient = responseProductList.get(position - 1).getVarients().get(0);
                if (currentVarient.getImages() != null && CommonUtils.stringIsNotNullAndEmpty(currentVarient.getImages().getPrimary()))
                    stringArrayList.add(currentVarient.getImages().getPrimary());

                else if (responseProductList.get(position - 1).getProduct().getImages() != null && CommonUtils.stringIsNotNullAndEmpty(responseProductList.get(position - 1).getProduct().getImages().getPrimary())) {
                    stringArrayList.add(responseProductList.get(position - 1).getProduct().getImages().getPrimary());
                } else
                    stringArrayList.add("");

                for (Attribute attribute : currentVarient.getAttributes()) {
                    String value = attribute.getOption().getValue();
                    stringArrayList.add(CommonUtils.capitalizeWord(value));
                }
                float actualPrice;
                float price = currentVarient.getPrice();
                float sellingPrice = currentVarient.getSellingPrice();
                if (sellingPrice > 0) {
                    actualPrice = sellingPrice;
                } else {
                    actualPrice = price;
                }
                stringArrayList.add(context.getString(R.string.rs) + actualPrice);
                stringArrayList.add("" + context.getResources().getDrawable(R.drawable.ic_shopping_cart));
            }
            LinearLayoutManager manager = new LinearLayoutManager(context);
            manager.setOrientation(RecyclerView.HORIZONTAL);
            holder.diamondProductRv.setLayoutManager(manager);
            DiamondProductItemAdapter diamondProductItemAdapter = new DiamondProductItemAdapter(context, stringArrayList, position, responseProductList, holder, ProductsAdapter.this);
            holder.diamondProductRv.setAdapter(diamondProductItemAdapter);
        } else {
            if (context instanceof MainActivity && type.equals("list2")) {
                holder.productCardView.requestLayout();
                holder.productCardView.getLayoutParams().width = SPData.featuredCategoryProductWidth();
            }

            if (SPData.getAppPreferences().isMembershipPurchased()) {
                holder.membershipLayout.setVisibility(GONE);
                holder.purchasedMembershipPrice.setVisibility(VISIBLE);
            } else {
                holder.purchasedMembershipPrice.setVisibility(GONE);
                holder.membershipLayout.setVisibility(VISIBLE);
            }

            handleProduct(holder, responseProductList.get(position).getProduct());

            setAllVarient(holder, position);

            holder.membershipLayout.setOnClickListener(v -> openMembership());

            holder.spinnerVarients.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    try {
                        holder.varients_attributes.setText(adapterView.getItemAtPosition(i).toString());
                        handleVarient(holder, responseProductList.get(position).getVarients().get(i), position);
                    } catch (Exception e) {
                        Log.e("TAG", "onItemSelected: " + e);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            holder.add_to_cart.setOnClickListener(v -> {
                onAddToCart(holder, position);
            });
            if (holder.imageViewEditQuantity != null) {
                if (SPData.enterQuantityManuallyInProducts()) {
                    holder.imageViewEditQuantity.setVisibility(VISIBLE);
                } else
                    holder.imageViewEditQuantity.setVisibility(GONE);

                holder.imageViewEditQuantity.setOnClickListener(v -> {
                    FlatDialog flatDialog = new FlatDialog(context);
                    flatDialog.setTitle("Enter quantity")
                            .setSubtitle("Update cart item quantity")
                            .setFirstTextFieldHint("quantity")
                            .setFirstButtonText("Done")
                            .isCancelable(true)
                            .setBackgroundColor(context.getResources().getColor(R.color.colorPrimaryDark))
                            .setFirstTextFieldInputType(InputType.TYPE_CLASS_NUMBER)
                            .setFirstButtonTextColor(context.getResources().getColor(R.color.colorPrimaryDark))
                            .setFirstButtonColor(context.getResources().getColor(R.color.colorWhite))
                            .withFirstButtonListner(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    try {
                                        if (CommonUtils.stringIsNotNullAndEmpty(flatDialog.getFirstTextField())) {
                                            Varient varient = getSelectedVarient(holder, position);
                                            int minQty = varient.getMinOrderQuantity();
                                            int value = Integer.parseInt(flatDialog.getFirstTextField());
                                            int stock = varient.getStock();
                                            int maxQty = varient.getMaxOrderQuantity();
                                            if (stock >= value) {
                                                if (value == 0 || value < minQty) {
                                                    Toast.makeText(context, "Minimum order quantity is " + minQty, Toast.LENGTH_SHORT).show();
                                                } else if (maxQty > 0 && value > maxQty) {
                                                    Toast.makeText(context, "Maximum order quantity is " + maxQty, Toast.LENGTH_SHORT).show();
                                                } else {
                                                    updateItem(value, holder, varient);
                                                }
                                            } else {
                                                Toast.makeText(context, "There is only " + stock + " stock left", Toast.LENGTH_SHORT).show();
                                            }
                                            flatDialog.dismiss();
                                        } else {
                                            Toast.makeText(context, "Enter a valid quantity", Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (Exception e) {
                                        Toast.makeText(context, "Try again after some time", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            })
                            .show();
                });
            }
            if (holder.decrease != null)
                holder.decrease.setOnClickListener(v -> {
                    Varient varient = getSelectedVarient(holder, position);
                    int minQty = varient.getMinOrderQuantity();
                    int currentQty = Integer.parseInt(holder.no_ofQuantity.getText().toString());
                    boolean varientRemove = false;
                    if ((minQty > 1 && minQty == currentQty) || (currentQty == 1)) {
                        if (currentQty != 1) {
                            MDToast.makeText(context, "Minimum order quantity is " + minQty, Toast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
                        }
                        varientRemove = true;
                    }
                    if (varientRemove) {
                        removeProductFromList(varient, holder);
                    } else {
                        updateItem(currentQty - 1, holder, varient);
                    }
                });

            if (holder.increase != null)
                holder.increase.setOnClickListener(v -> {
                    Varient varient = getSelectedVarient(holder, position);
                    checkItemToAdded(holder.no_ofQuantity, holder, varient);
                });

            holder.layoutItems.setOnClickListener(v -> {
                if (SPData.showProductDetail())
                    openProductDetail(position, holder);
            });

            if (holder.removeFromWishlist != null)
                if (!(context instanceof CommonActivity)) {
                    holder.removeFromWishlist.setVisibility(GONE);
                } else {
                    holder.removeFromWishlist.setVisibility(VISIBLE);
                    holder.removeFromWishlist.setOnClickListener(v -> removeFromWishlist(holder));
                }
        }

//        } catch (Exception e) {
//            e.printStackTrace();
//            Log.e("TAG", "showProductItem: " + e);
//        }
    }

    private void onAddToCart(MyViewHolder holder, int position) {
        if (SPData.openProductDetailOnAdd()) {
            openProductDetail(position, holder);
        } else {
            if (holder.add_to_cart.getText().toString().equals(context.getString(R.string.buy_now))) {
                Intent intent = new Intent(context, PreOrderCheckoutActivity.class);
                intent.putExtra("name", responseProductList.get(position).getProduct().getName());
                Varient varient = getSelectedVarient(holder, position);
                intent.putExtra("image", getImage(varient, position));
                intent.putExtra("sp", varient.getSellingPrice());
                intent.putExtra("mrp", varient.getPrice());
                intent.putExtra("id", varient.getId());
                intent.putExtra("from", responseProductList.get(position).getProduct().getAvailableFrom());
                intent.putExtra("to", responseProductList.get(position).getProduct().getExpireOn());
                intent.putExtra("attribute", holder.varients_attributes.getText().toString());
                context.startActivity(intent);
            } else {
                Vendor vendor = responseProductList.get(position).getProduct().getCreatedBy();
                String cartVendorId = SPData.getAppPreferences().getCartVendorId();
                if (SPData.allowOnlyOneVendorInCart() && vendor != null && !cartVendorId.isEmpty() && !cartVendorId.equals(vendor.getId())) {
                    showOnlyOneVendorDialog(responseProductList.get(position).getProduct().getName(), holder, position);
                } else
                    addItemToCart(holder, position);
            }
        }
    }

    private void openMembership() {
        context.startActivity(new Intent(context, CheckMembershipActivity.class));
    }

    private boolean isDiamondPageRequest() {
        return SPData.showDiamondPageLayout() && context instanceof AllProductsActivity;
    }

    private void addItemToCart(MyViewHolder holder, int position) {
        Varient varient = getSelectedVarient(holder, position);
        addVairent(varient, holder, position);
    }

    public void addVairent(Varient varient, MyViewHolder holder, int position) {
        int total_stock = varient.getStock();
        if (total_stock > 0) {
            int minQty = 1;
            if (varient.getMinOrderQuantity() > 1) {
                minQty = varient.getMinOrderQuantity();
                Log.e("TAG", "showProductItem: " + minQty);
                MDToast.makeText(context, "Minimum order quantity is " + minQty, Toast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
            }
            addItem(minQty, holder, varient, position);
        } else
            MDToast.makeText(context, "There is only " + total_stock + " left", Toast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
    }

    private void showOnlyOneVendorDialog(String name, MyViewHolder holder, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Replace cart item?");
        builder.setMessage("Your cart contains item from another vendor.Do you want to clear the cart and add " + name);
        builder.setPositiveButton("Add", (dialog, which) -> clearCart(holder, position));
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.create().show();
    }

    private void clearCart(MyViewHolder holder, int position) {
        String token = SPData.getAppPreferences().getUsertoken();
        if (!token.isEmpty()) {
            ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());
            apiService.clearCart().enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(@NotNull Call<JsonObject> call, @NotNull Response<JsonObject> response) {
                    handleClearCartResponse(holder, position, response);
                }

                @Override
                public void onFailure(@NotNull Call<JsonObject> call, @NotNull Throwable t) {
                    CommonUtils.showErrorMessage(context, "" + t.getMessage());
                }
            });
        } else {
            ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUserUniqueId());
            apiService.clearTempCart().enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(@NotNull Call<JsonObject> call, @NotNull Response<JsonObject> response) {
                    handleClearCartResponse(holder, position, response);
                }

                @Override
                public void onFailure(@NotNull Call<JsonObject> call, @NotNull Throwable t) {
                    CommonUtils.showErrorMessage(context, "" + t.getMessage());
                }
            });
        }

    }

    private void handleClearCartResponse(MyViewHolder holder, int position, Response<JsonObject> response) {
        if (response.isSuccessful() && response.body() != null) {
            SPData.getAppPreferences().setTotalCartCount(0);
            addItemToCart(holder, position);
        } else {
            CommonUtils.showErrorMessage(context, "" + response.message());
        }
    }

    private Varient getSelectedVarient(MyViewHolder holder, int position) {
        int selectedItemPosition = 0;
        if (holder.spinnerVarients.getAdapter().getCount() > 0)
            selectedItemPosition = holder.spinnerVarients.getSelectedItemPosition();
        return responseProductList.get(position).getVarients().get(selectedItemPosition);
    }

    @SuppressLint("SetTextI18n")
    private void handleProduct(MyViewHolder holder, Product product) {
        if (SPData.showGridView()) {
            if (product.getIsRecommended() != null
                    && product.getIsRecommended()) {
                holder.product_recommended.setVisibility(VISIBLE);
                holder.product_recommended_text.setText(SPData.recommendedText());
            } else if (SPData.showCertifiedText()) {
                holder.product_recommended.setCardBackgroundColor(context.getResources().getColor(R.color.colorInfo));
                holder.product_recommended.setVisibility(VISIBLE);
                holder.product_recommended_text.setText(SPData.certifiedText());
            } else {
                holder.product_recommended_text.setText("");
                holder.product_recommended.setVisibility(GONE);
            }
        } else {
            holder.product_recommended.setVisibility(GONE);
        }
        holder.product_name.setText(capitalizeWord(product.getName()));
        Brand brand = product.getBrand();
        String by = "by ";
        if (context instanceof AllProductsActivity && ((AllProductsActivity) context).brand_id != null) {
            by = "";
        }
        if (brand != null)
            holder.vendorName.setText(by + CommonUtils.capitalizeWord(brand.getName()));
        else
            holder.vendorName.setText("");
    }

    private void setAllVarient(MyViewHolder holder, int position) {
        ArrayList<String> varientsNameList = new ArrayList<>();
        ArrayList<Varient> varientArrayList = responseProductList.get(position).getVarients();
        Collections.sort(varientArrayList, new Comparator<Varient>() {
            @Override
            public int compare(Varient o1, Varient o2) {
                return o2.getStock() - o1.getStock();
            }
        });
        for (Varient varient : varientArrayList) {
            StringBuilder allAttribute = new StringBuilder();
            for (Attribute attribute : varient.getAttributes()) {
                allAttribute.append(attribute.getOption().getValue()).append(",");
            }
            varientsNameList.add(CommonUtils.removeLastCharacter(String.valueOf(allAttribute)));
        }
        if (varientArrayList.size() > 0)
            varient = varientArrayList.get(0);
        if (SPData.showVarientDropdown()) {
            if ((context instanceof AllProductsActivity) && SPData.showSecondProductView()) {
                holder.spinnerVarientsLL.setVisibility(GONE);
            } else if (varient.getAttributes().size() > 0) {
                holder.spinnerVarientsLL.setVisibility(VISIBLE);
                holder.varients_attributes.setText(varientsNameList.get(0));
            } else {
                holder.spinnerVarientsLL.setVisibility(INVISIBLE);
            }
        } else
            holder.spinnerVarientsLL.setVisibility(GONE);

        ArrayAdapter<String> spinnerAdapter;
        if (varientsNameList.size() == 1) {
            spinnerAdapter = new ArrayAdapter<>(context, R.layout.spinner_item_view_for_one_item, R.id.textSpinner1);
            holder.spinnerVarients.setEnabled(false);
        } else {
            spinnerAdapter = new ArrayAdapter<>(context, R.layout.spinner_item_view, R.id.textSpinner1);
            holder.spinnerVarients.setEnabled(true);
        }
        spinnerAdapter.addAll(varientsNameList);
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_drop_down_resource);
        holder.spinnerVarients.setAdapter(spinnerAdapter);
        holder.spinnerVarients.setPadding(20, 20, 20, 20);
        holder.spinnerVarients.setSelection(0, false);

        handleVarient(holder, varient, position);
    }

    public void filterByOption(ArrayList<String> stringArrayList) {
        responseProductList.clear();
        ArrayList<String> options = new ArrayList<>(stringArrayList);
        while (options.remove("None")) {
        }
        if (options.size() > 0) {
            for (Datum datum : allProductList) {
                ArrayList<String> optionslist = new ArrayList<>();
                for (Attribute attribute : datum.getVarients().get(0).getAttributes()) {
                    optionslist.add(attribute.getOption().getValue());
                }
                if (optionslist.containsAll(options)) {
                    responseProductList.add(datum);
                }
            }
        } else {
            responseProductList.addAll(allProductList);
        }
        notifyDataSetChanged();
    }

    @SuppressLint("SetTextI18n")
    private void handleVarient(MyViewHolder holder, Varient varient, int position) {
        this.varient = varient;
        if (CommonUtils.stringIsNotNullAndEmpty(varient.getTag()) && SPData.showTag()) {
            holder.tag.setVisibility(VISIBLE);
            holder.tag.setText(varient.getTag());
        } else {
            holder.tag.setVisibility(GONE);
        }

        if (SPData.showMembership() && varient.getMemberPrice() > 0) {
            if (SPData.getAppPreferences().isMembershipPurchased()) {
                holder.purchasedMembershipPrice.setVisibility(VISIBLE);
                holder.membershipLayout.setVisibility(GONE);
                holder.purchasedMembershipPrice.setText(context.getString(R.string.rs) + CommonUtils.getTwoDigitsRoundOffValue(varient.getMemberPrice()));
            } else {
                holder.membershipLayout.setVisibility(VISIBLE);
                holder.purchasedMembershipPrice.setVisibility(GONE);
            }
            holder.memberPrice.setText(context.getString(R.string.rs) + CommonUtils.getTwoDigitsRoundOffValue(varient.getMemberPrice()));
        } else {
            holder.purchasedMembershipPrice.setVisibility(GONE);
            holder.membershipLayout.setVisibility(GONE);
        }

        if (CommonUtils.stringIsNotNullAndEmpty(varient.getTag()) && varient.getTag().equals(context.getString(R.string.veg))) {
            holder.veg.setVisibility(VISIBLE);
            holder.veg.setImageDrawable(context.getResources().getDrawable(R.drawable.veg));
        } else if (CommonUtils.stringIsNotNullAndEmpty(varient.getTag()) && varient.getTag().equals(context.getString(R.string.nonveg))) {
            holder.veg.setVisibility(VISIBLE);
            holder.veg.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_non_veg));
        } else
            holder.veg.setVisibility(GONE);


        double sellingPrice;
        if (SPData.useWholesalePricingInProducts() && getSellingPriceByQuantity(varient) > 0) {
            sellingPrice = getSellingPriceByQuantity(varient);
        } else {
            sellingPrice = varient.getSellingPrice();
        }
        float productPrice = varient.getPrice();

        if (sellingPrice != 0.0 && sellingPrice != productPrice) {
            double save = 0.0;
            if (SPData.getAppPreferences().isMembershipPurchased() && varient.getMemberPrice() > 0 && !(SPData.useWholesalePricingInProducts() && getSellingPriceByQuantity(varient) > 0)) {
                save = productPrice - varient.getMemberPrice();
            } else
                save = productPrice - sellingPrice;
            long discount = Math.round((((save) / productPrice) * 100));
            holder.product_mrp.setText(context.getString(R.string.rs) + CommonUtils.getTwoDigitsRoundOffValue(productPrice));
            holder.product_mrp.setVisibility(VISIBLE);
            holder.product_discount.setText(discount + "% off");
            holder.product_discount_layout.setVisibility(VISIBLE);
            holder.product_discount.setVisibility(VISIBLE);
            if (SPData.showYouSaveAmount()) {
                holder.youSaveAmt.setVisibility(VISIBLE);
                if (SPData.useGreenYouSaveColor())
                    holder.youSaveAmt.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
                holder.youSaveAmt.setText("You save " + context.getString(R.string.rs) + CommonUtils.getTwoDigitsRoundOffValue(save));
            } else {
                holder.youSaveAmt.setVisibility(GONE);
            }
            holder.product_selling_price.setText(context.getString(R.string.rs) + CommonUtils.getTwoDigitsRoundOffValue(sellingPrice));
        } else {
            holder.product_discount_layout.setVisibility(INVISIBLE);
            holder.product_discount.setVisibility(INVISIBLE);
            holder.youSaveAmt.setVisibility(GONE);
            holder.product_selling_price.setText(context.getString(R.string.rs) + CommonUtils.getTwoDigitsRoundOffValue(productPrice));
            holder.product_mrp.setVisibility(GONE);
        }

        if (SPData.showSubscription() && varient.isSubscribable() && (!varient.isSubscribed())) {
            holder.product_repeat.setVisibility(VISIBLE);
            ((TextView) holder.product_repeat.getChildAt(0)).setText("Subscribe");
            holder.product_repeat.setOnClickListener(view -> {
                if (!SPData.getAppPreferences().getUsertoken().isEmpty()) {

                    String name = holder.product_name.getText().toString();
                    Images images = varient.getImages();
                    String image = null;

                    if (images != null)
                        image = images.getPrimary();

                    double mrp = 0.0;
                    if (holder.product_mrp.getVisibility() == VISIBLE) {
                        mrp = Double.parseDouble(holder.product_mrp.getText().toString().replace(context.getString(R.string.rs), ""));
                    }
                    double sellingPrice1 = 0.0;
                    if (holder.product_selling_price.getVisibility() == VISIBLE) {
                        sellingPrice1 = Double.parseDouble(holder.product_selling_price.getText().toString().replace(context.getString(R.string.rs), ""));
                    }
                    Log.e("TAG", "onClick: " + holder.varients_attributes.getText().toString());
                    CommonUtils.promptSelectAddressDialog(context, varient.getId(), name, image, holder.varients_attributes.getText().toString(), mrp, sellingPrice1);

                } else {
                    context.startActivity(new Intent(context, LoginActivity.class));
                }

            });
        } else if (SPData.showSubscription() && varient.isSubscribable() && (varient.isSubscribed())) {
            holder.product_repeat.setVisibility(VISIBLE);
            ((TextView) holder.product_repeat.getChildAt(0)).setText("Subscribed");
            holder.product_repeat.setOnClickListener(null);
        } else {
            holder.product_repeat.setVisibility(INVISIBLE);
            holder.product_repeat.setOnClickListener(null);
        }

        if (varient.getInCart() > 0 && SPData.showcartIconAddToCart()) {
            holder.add_to_cart.setVisibility(GONE);
            holder.linearLayoutPlusminus.setVisibility(VISIBLE);
            holder.no_ofQuantity.setText("" + varient.getInCart());
        } else if (SPData.showcartIconAddToCart()) {
            holder.add_to_cart.setVisibility(VISIBLE);
            holder.linearLayoutPlusminus.setVisibility(GONE);
            holder.no_ofQuantity.setText("0");
        } else {
            holder.add_to_cart.setVisibility(GONE);
            holder.linearLayoutPlusminus.setVisibility(GONE);
        }

        if (holder.product_rating != null) {
            if (SPData.showRatingInProductDetail() && varient.getReviews() != null) {
                DecimalFormat df = new DecimalFormat("#.##");
                holder.product_rating.setText("(" + df.format(varient.getReviews().getRating()) + ")");
                if ((context instanceof AllProductsActivity) && SPData.showSecondProductView()) {
                    holder.product_rating.setVisibility(GONE);
                    double rate = Math.round(varient.getReviews().getRating());
                    if (rate > 0 && rate <= 1) {
                        holder.imageViewStar1.setSelected(true);
                    } else if (rate <= 2) {
                        holder.imageViewStar1.setSelected(true);
                        holder.imageViewStar2.setSelected(true);
                    } else if (rate <= 3) {
                        holder.imageViewStar1.setSelected(true);
                        holder.imageViewStar2.setSelected(true);
                        holder.imageViewStar3.setSelected(true);
                    } else if (rate <= 4) {
                        holder.imageViewStar1.setSelected(true);
                        holder.imageViewStar2.setSelected(true);
                        holder.imageViewStar3.setSelected(true);
                        holder.imageViewStar4.setSelected(true);
                    } else if (rate <= 5) {
                        holder.imageViewStar1.setSelected(true);
                        holder.imageViewStar2.setSelected(true);
                        holder.imageViewStar3.setSelected(true);
                        holder.imageViewStar4.setSelected(true);
                        holder.imageViewStar5.setSelected(true);
                    }
                } else {
                    holder.product_rating.setVisibility(VISIBLE);
                }
            } else {
                holder.product_rating.setVisibility(GONE);
            }
        }

        if (varient.getStock() == 0) {
            holder.add_to_cart.setText(R.string.out_of_stock);
            holder.add_to_cart.setTextColor(context.getResources().getColor(R.color.colorError));
            holder.add_to_cart.setEnabled(false);
            if (holder.linearLayoutStarLayout != null) {
                holder.linearLayoutStarLayout.setVisibility(GONE);
            }
            holder.product_rating.setVisibility(GONE);
        } else {
            if (SPData.showRatingInProductDetail())
                holder.product_rating.setVisibility(VISIBLE);
            else
                holder.product_rating.setVisibility(GONE);

            if (responseProductList.get(position).getProduct().getKind() != null && responseProductList.get(position).getProduct().getKind().equals("pre-order")) {
                holder.product_rating.setVisibility(GONE);
                holder.add_to_cart.setText(R.string.buy_now);
            } else
                holder.add_to_cart.setText(SPData.addText());
            if (holder.add_to_cart2 != null && SPData.useSecondAddToCartDesign()) {
                holder.add_to_cart.setTextColor(context.getResources().getColor(R.color.colorWhite));
            } else
                holder.add_to_cart.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
            holder.add_to_cart.setEnabled(true);
            if (holder.linearLayoutStarLayout != null) {
                holder.linearLayoutStarLayout.setVisibility(GONE);
            }

        }
        setImage(holder, varient, position);
    }

    private double getSellingPriceByQuantity(Varient varient) {
        if (varient.getWholeSalePrices() != null && varient.getWholeSalePrices().size() > 0) {
            ArrayList<WholeSalePriceModel> wholeSalePriceModels = varient.getWholeSalePrices();
            Collections.sort(wholeSalePriceModels, (o1, o2) -> (int) (o1.getPricePerUnit() - o2.getPricePerUnit()));
            Log.e("TAG", "getSellingPriceByQuantity: " + wholeSalePriceModels.get(0).getPricePerUnit());
            return wholeSalePriceModels.get(0).getPricePerUnit();
        } else {
            return 0;
        }
    }


    @SuppressLint("UseCompatLoadingForDrawables")
    private void setImage(MyViewHolder holder, Varient varient, int position) {
        String image = getImage(varient, position);
        if (CommonUtils.stringIsNotNullAndEmpty(image))
            Glide.with(context).load(SPData.getBucketUrl() + image).into(holder.image);
        else
            Glide.with(context).load(context.getResources().getDrawable(R.drawable.noimage)).into(holder.image);
    }

    private String getImage(Varient varient, int position) {
        if (varient.getImages() != null && CommonUtils.stringIsNotNullAndEmpty(varient.getImages().getPrimary())) {
            return varient.getImages().getPrimary();
        } else if (responseProductList.get(position).getProduct().getImages() != null) {
            String pri = responseProductList.get(position).getProduct().getImages().getPrimary();
            if (CommonUtils.stringIsNotNullAndEmpty(pri))
                return pri;
            else
                return "";
        } else {
            return "";
        }
    }

    private void removeFromWishlist(MyViewHolder holder) {
        try {
            final CustomProgressDialog customProgressDialog = CommonUtils.showProgressDialog(context,
                    "Removing from wishlist ... ");
            ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());
            Varient varient = getSelectedVarient(holder, holder.getAdapterPosition());
            apiService.removeItemFromWishlist(varient.getId()).enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                    CommonUtils.hideProgressDialog(customProgressDialog);
                    Log.e("TAG", "onResponse: " + response);
                    if (response.isSuccessful() && response.body() != null) {
                        responseProductList.remove(holder.getAdapterPosition());
                        notifyItemRemoved(holder.getAdapterPosition());
                        MDToast.makeText(context, "Item removed from wishlist!!", MDToast.LENGTH_SHORT, MDToast.TYPE_SUCCESS).show();
                    } else {
                        CommonUtils.hideProgressDialog(customProgressDialog);
                        try {
                            Log.e("TAG", "onResponse: " + response.errorBody().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        MDToast.makeText(context, response.code() + " " + response.message(), MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                    Log.e("fail", call.toString());
                    CommonUtils.hideProgressDialog(customProgressDialog);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "removeItemFromWishList: " + e);
        }
    }

    private void openProductDetail(int position, MyViewHolder holder) {
        Intent i = new Intent(context, ProductDetailsActivity.class);
        i.putExtra("position", position);
        i.putExtra("preorder", holder.add_to_cart.getText().equals(context.getString(R.string.buy_now)));
        i.putExtra("product_id", responseProductList.get(position).getProduct().getId());
        if (context instanceof ProductDetailsActivity) {
            i.putExtra("page", ((ProductDetailsActivity) context).page);
            i.putExtra("total_page", ((ProductDetailsActivity) context).totalPages);
        }
        ((Activity) context).startActivityForResult(i, REQUEST_CODE);
        if (context instanceof ProductDetailsActivity) {
            ((ProductDetailsActivity) context).finish();
        }
    }

    private void removeProductFromList(Varient varient, final MyViewHolder holder) {
        if (!SPData.getAppPreferences().getUsertoken().isEmpty()) {
            ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());
            apiService.removeItemFromCart(varient.getId()).enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(@NotNull Call<JsonObject> call, @NotNull Response<JsonObject> response) {
                    handleRemoveItemResponse(response, varient, holder);
                }

                @Override
                public void onFailure(@NotNull Call<JsonObject> call, @NotNull Throwable t) {
                    MDToast.makeText(context, "Failed to connect", Toast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
                }
            });
        } else {

            ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUserUniqueId());
            apiService.removeItemFromCartWithoutLogin(varient.getId()).enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(@NotNull Call<JsonObject> call, @NotNull Response<JsonObject> response) {
                    handleRemoveItemResponse(response, varient, holder);
                }

                @Override
                public void onFailure(@NotNull Call<JsonObject> call, @NotNull Throwable t) {
                    MDToast.makeText(context, "Failed to connect", Toast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
                }
            });
        }
    }

    private void handleRemoveItemResponse(Response<JsonObject> response, Varient varient, MyViewHolder holder) {
        try {
            if (response.isSuccessful() && response.body() != null) {
                holder.add_to_cart.setVisibility(VISIBLE);
                holder.linearLayoutPlusminus.setVisibility(GONE);
                varient.setInCart(0);
                if (context instanceof AllProductsActivity) {
                    ((AllProductsActivity) context).getAllCart();
                }
                if (context instanceof ProductsByVendorActivity) {
                    ((ProductsByVendorActivity) context).getAllCart();
                }
                if (SPData.getAppPreferences().getTotalCartCount() != -1) {
                    int total_cart = SPData.getAppPreferences().getTotalCartCount() - 1;
                    if (total_cart > 0) {
                        addBadge(total_cart);
                    } else {
                        removeBadge();
                    }
                }
                MDToast.makeText(context, "Removed from cart !!", Toast.LENGTH_SHORT, MDToast.TYPE_SUCCESS).show();
            } else {
                MDToast.makeText(context, "" + response.message(), MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "onResponse: " + e);
        }
    }

    private void removeBadge() {
        SPData.getAppPreferences().setTotalCartCount(0);
        if (context instanceof MainActivity)
            ((MainActivity) context).removeBadge();
        if (context instanceof AllProductsActivity)
            ((AllProductsActivity) context).removeBadge();
        if (context instanceof ProductDetailsActivity)
            ((ProductDetailsActivity) context).removeBadge();
        if (context instanceof SearchPrdouctActivity)
            ((SearchPrdouctActivity) context).removeBadge();
    }

    private void addBadge(int total_cart) {
        SPData.getAppPreferences().setTotalCartCount(total_cart);
        if (context instanceof MainActivity)
            ((MainActivity) context).addBadge("" + total_cart);
        if (context instanceof AllProductsActivity)
            ((AllProductsActivity) context).addBadge("" + total_cart);
        if (context instanceof ProductDetailsActivity)
            ((ProductDetailsActivity) context).addBadge("" + total_cart);
        if (context instanceof SearchPrdouctActivity)
            ((SearchPrdouctActivity) context).addBadge("" + total_cart);
    }

    private void checkItemToAdded(TextView no_ofQuantity, MyViewHolder holder, Varient varient) {
        int total_stock = varient.getStock();
        int maxQty = varient.getMaxOrderQuantity();
        int currentQty = Integer.parseInt(no_ofQuantity.getText().toString());
        if (currentQty < total_stock) {
            if (maxQty == 0 || currentQty < maxQty)
                updateItem(currentQty + 1, holder, varient);
            else
                MDToast.makeText(context, "Maximum order quantity is " + maxQty, MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
        } else {
            MDToast.makeText(context, "There is only " + total_stock + " stock left", Toast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
        }
    }

    private void addItem(final int quantity, final MyViewHolder holder, Varient varient, int position) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("product", varient.getId());
        jsonObject.addProperty("quantity", quantity);
        Log.e("TAG", "addItem: " + jsonObject);
        if (!SPData.getAppPreferences().getUsertoken().isEmpty()) {
            ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());
            apiService.addItemToCart(jsonObject).enqueue(new Callback<JsonObject>() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                    handleAddToCartResponse(response, quantity, holder, varient, position);
                }

                @Override
                public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                    Log.e("fail", call.toString());
                }
            });
        } else {
            ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUserUniqueId());
            apiService.addItemToCartWithoutLogin(jsonObject).enqueue(new Callback<JsonObject>() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                    handleAddToCartResponse(response, quantity, holder, varient, position);
                }

                @Override
                public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                    Log.e("fail", call.toString());
                }
            });
        }
    }

    @SuppressLint("SetTextI18n")
    private void handleAddToCartResponse(Response<JsonObject> response, int quantity, MyViewHolder holder, Varient varient, int position) {
        Log.e("TAG", "handleAddToCartResponse: " + response.body());
        if (response.isSuccessful() && response.body() != null) {

            holder.add_to_cart.setVisibility(GONE);
            holder.linearLayoutPlusminus.setVisibility(VISIBLE);

            if (SPData.getAppPreferences().getTotalCartCount() != -1) {
                int cartItem = SPData.getAppPreferences().getTotalCartCount() + 1;
                SPData.getAppPreferences().setTotalCartCount(cartItem);
                addBadge(cartItem);
            }
            if (context instanceof AllProductsActivity) {
                ((AllProductsActivity) context).getAllCart();
            }
            if (context instanceof ProductsByVendorActivity) {
                ((ProductsByVendorActivity) context).getAllCart();
            }
            if (holder.no_ofQuantity != null)
                holder.no_ofQuantity.setText("" + quantity);
            varient.setInCart(quantity);
            if (responseProductList != null && responseProductList.size() > position && responseProductList.get(position).getProduct() != null) {
                Vendor vendor = responseProductList.get(position).getProduct().getCreatedBy();
                if (vendor != null)
                    SPData.getAppPreferences().setCartVendorId(vendor.getId());
            }
            MDToast.makeText(context, "Item added to cart !!", MDToast.LENGTH_SHORT, MDToast.TYPE_SUCCESS).show();
        } else {
            Log.e("TAG", "onResponse: " + response);
            MDToast.makeText(context, "" + response.message(), MDToast.TYPE_INFO, MDToast.LENGTH_SHORT).show();
        }
    }

    private void updateItem(final int quantity, final MyViewHolder holder, Varient varient) {
        try {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("quantity", quantity);
            if (!SPData.getAppPreferences().getUsertoken().isEmpty()) {
                ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());
                apiService.updateCartItem(varient.getId(), jsonObject).enqueue(new Callback<CartDetailsInformation>() {
                    @Override
                    public void onResponse(@NotNull Call<CartDetailsInformation> call, @NotNull Response<CartDetailsInformation> response) {
                        Log.e("TAG", "onResponse: " + response);
                        handleUpdateItemResponse(response, quantity, holder, varient);
                    }

                    @Override
                    public void onFailure(@NotNull Call<CartDetailsInformation> call, @NotNull Throwable t) {
                        Log.e("TAG", "onFailure: " + t.getMessage());
                        Toast.makeText(context, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUserUniqueId());
                apiService.updateCartItemWithoutLogin(varient.getId(), jsonObject).enqueue(new Callback<CartDetailsInformation>() {
                    @Override
                    public void onResponse(@NotNull Call<CartDetailsInformation> call, @NotNull Response<CartDetailsInformation> response) {
                        Log.e("TAG", "onResponse: " + response);
                        handleUpdateItemResponse(response, quantity, holder, varient);
                    }

                    @Override
                    public void onFailure(@NotNull Call<CartDetailsInformation> call, @NotNull Throwable t) {
                        Log.e("TAG", "onFailure: " + t.getMessage());
                        Toast.makeText(context, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

        } catch (Exception e) {
            Log.e(TAG, "updateItem: " + e);
        }
    }

    @SuppressLint("SetTextI18n")
    private void handleUpdateItemResponse(Response<CartDetailsInformation> response, int quantity, MyViewHolder holder, Varient varient) {
        Log.e("TAG", "handleUpdateItemResponse: " + response);
        if (response.isSuccessful() && response.body() != null) {
            varient.setInCart(quantity);
            holder.no_ofQuantity.setText("" + quantity);
            if (context instanceof AllProductsActivity) {
                ((AllProductsActivity) context).getAllCart();
            }
            if (context instanceof ProductsByVendorActivity) {
                ((ProductsByVendorActivity) context).getAllCart();
            }
            MDToast.makeText(context, "Quantity Updated!!", Toast.LENGTH_SHORT, MDToast.TYPE_SUCCESS).show();
        } else {
            MDToast.makeText(context, "" + response.message(), Toast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
        }
    }

    @Override
    public int getItemCount() {
        if (isDiamondPageRequest() && responseProductList.size() > 0) {
            return responseProductList.size() + 1;
        } else if (responseProductList != null)
            return responseProductList.size();
        else
            return 0;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView image, veg;
        TextView product_discount, product_recommended_text, product_name, product_minQty, add_to_cart, add_to_cart2, product_rating;
        TextView varients_attributes, tag, memberPrice, purchasedMembershipPrice;
        RelativeLayout cardView;
        RecyclerView recycler_options;
        Button increase, decrease;
        CardView product_recommended, product_discount_layout, product_repeat;
        Button removeFromWishlist;
        private final LinearLayout linearLayoutPlusminus;
        private final TextView no_ofQuantity, product_mrp, product_selling_price, vendorName;
        private final LinearLayout layoutItems, spinnerVarientsLL;
        private final Spinner spinnerVarients;
        LinearLayout linearLayoutStarLayout;
        ImageView imageViewStar1, imageViewStar2, imageViewStar3, imageViewStar4, imageViewStar5, lock, arrow;
        RecyclerView diamondProductRv;
        TextView youSaveAmt;
        CardView productCardView;
        ImageView imageViewEditQuantity;
        private final RelativeLayout membershipLayout;

        MyViewHolder(View view) {
            super(view);
            removeFromWishlist = view.findViewById(R.id.remove_from_wishlist);
            product_name = view.findViewById(R.id.product_name);
            image = view.findViewById(R.id.image);
            varients_attributes = view.findViewById(R.id.attributes);
            no_ofQuantity = view.findViewById(R.id.no_of_quantity);
            linearLayoutPlusminus = view.findViewById(R.id.plus_minus_linearLayout);
            cardView = view.findViewById(R.id.cardview);
            add_to_cart = view.findViewById(R.id.add_to_cart);
            add_to_cart2 = view.findViewById(R.id.add_to_cart2);
            if (add_to_cart2 != null && SPData.useSecondAddToCartDesign()) {
                add_to_cart = add_to_cart2;
            }
            product_rating = view.findViewById(R.id.no_of_rating);
            recycler_options = view.findViewById(R.id.recyclerview_option_values);
            increase = view.findViewById(R.id.increase);
            decrease = view.findViewById(R.id.decrease);
            layoutItems = view.findViewById(R.id.layout_item);
            product_minQty = view.findViewById(R.id.product_qty);
            this.product_mrp = view.findViewById(R.id.product_mrp);
            this.product_selling_price = view.findViewById(R.id.product_price);
            product_recommended = view.findViewById(R.id.product_recommended);
            product_recommended_text = view.findViewById(R.id.product_recommended_text);
            product_discount = view.findViewById(R.id.product_discount);
            vendorName = view.findViewById(R.id.vendor_name);
            product_discount_layout = view.findViewById(R.id.product_discount_layout);
            product_repeat = view.findViewById(R.id.product_repeat);
            tag = view.findViewById(R.id.tag);
            spinnerVarients = view.findViewById(R.id.spinner_varients);
            spinnerVarientsLL = view.findViewById(R.id.spinner_varients_ll);
            imageViewStar1 = view.findViewById(R.id.star1);
            imageViewStar2 = view.findViewById(R.id.star2);
            imageViewStar3 = view.findViewById(R.id.star3);
            imageViewStar4 = view.findViewById(R.id.star4);
            imageViewStar5 = view.findViewById(R.id.star5);
            linearLayoutStarLayout = view.findViewById(R.id.star_layout);
            diamondProductRv = view.findViewById(R.id.diamond_product_rv);
            youSaveAmt = view.findViewById(R.id.you_save);
            productCardView = view.findViewById(R.id.productcard);
            veg = view.findViewById(R.id.veg);
            membershipLayout = view.findViewById(R.id.membership_layout);
            memberPrice = view.findViewById(R.id.member_price);
            lock = view.findViewById(R.id.lock);
            arrow = view.findViewById(R.id.arrow);
            purchasedMembershipPrice = view.findViewById(R.id.purchased_member_price);
            imageViewEditQuantity = view.findViewById(R.id.edit_quantity);
        }
    }
}