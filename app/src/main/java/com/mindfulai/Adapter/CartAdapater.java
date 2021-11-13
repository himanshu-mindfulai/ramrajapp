package com.mindfulai.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.flatdialoglibrary.dialog.FlatDialog;
import com.google.gson.JsonObject;
import com.mindfulai.Activites.CheckoutActivity;
import com.mindfulai.Activites.MainActivity;
import com.mindfulai.AppPrefrences.AppPreferences;
import com.mindfulai.Models.CartInformation.Attribute;
import com.mindfulai.Models.CartInformation.CartDetailsInformation;
import com.mindfulai.Models.CartInformation.Product;
import com.mindfulai.Models.varientsByCategory.WholeSalePriceModel;
import com.mindfulai.NetworkRetrofit.ApiService;
import com.mindfulai.NetworkRetrofit.ApiUtils;
import com.mindfulai.Utils.CommonUtils;
import com.mindfulai.Utils.SPData;
import com.mindfulai.ministore.R;
import com.mindfulai.ui.CartPageFragment;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartAdapater extends RecyclerView.Adapter<CartAdapater.MyViewHolder> {


    private static final String TAG = "CartAdapter";
    private AppPreferences appPreferences;
    private final Context context;
    private final List<Product> cartDataArrayList;
    private List<Attribute> attributeList;
    private CartPageFragment cartPageFragment;
    private boolean quanityUpdated;
    private ArrayList<MyViewHolder> allInvalidZoneItem;
    private boolean visitedLastItem = true;
    private CheckoutActivity checkoutActivity;
    public String inActiveProductName = "";


    public CartAdapater(CheckoutActivity context) {
        this.context = context;
        this.checkoutActivity = context;
        this.cartDataArrayList = checkoutActivity.cartDataArrayList;
        allInvalidZoneItem = new ArrayList<>();
    }

    public CartAdapater(Context context, CartPageFragment cartPageFragment) {
        this.context = context;
        this.cartPageFragment = cartPageFragment;
        this.cartDataArrayList = cartPageFragment.cartDataArrayList;
    }

    @NotNull
    @Override
    public MyViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        View itemView;
        if (cartPageFragment != null) {
            if (SPData.useSecondCartUI()) {
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_cartlist_item2, parent, false);
            } else
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_cartlist_item, parent, false);
        } else {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_cartlist_item_checkout, parent, false);
        }
        return new MyViewHolder(itemView);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NotNull final MyViewHolder holder, final int position) {
        //    try {
        appPreferences = new AppPreferences(context);

        if (!cartDataArrayList.get(holder.getAdapterPosition()).getProduct().isActive()) {
            inActiveProductName = cartDataArrayList.get(holder.getAdapterPosition()).getProduct().getProduct().getName();
        }
        if (cartDataArrayList.get(holder.getAdapterPosition()).getProduct().getAttributes() != null) {
            attributeList = cartDataArrayList.get(holder.getAdapterPosition()).getProduct().getAttributes();
        }
        if (attributeList.size() >= 1) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < attributeList.size() - 1; i++) {
                sb.append(attributeList.get(i).getOption().getValue());
                sb.append(", ");
            }
            sb.append(attributeList.get(attributeList.size() - 1).getOption().getValue());
            holder.product_description.setText(CommonUtils.capitalizeWord("" + sb.toString()));
            holder.product_description.setVisibility(View.VISIBLE);
        } else
            holder.product_description.setVisibility(View.GONE);

        if ((SPData.showShippingMethods() || SPData.useShippingZonePincodeFeatureOnly()) && checkoutActivity != null) {
            if ((!cartDataArrayList.get(position).isZoneValid() && !quanityUpdated)) {
                holder.validZone.setText(SPData.noShippingMsg + " " + SPData.getAppPreferences().getPincode());
                holder.validZone.setVisibility(View.VISIBLE);
                allInvalidZoneItem.add(holder);
                holder.itemView.setEnabled(false);
                if (holder.plusMinusLayout != null)
                    holder.plusMinusLayout.setVisibility(View.GONE);
                holder.productItemLayout.setAlpha(0.15f);
            } else {
                holder.validZone.setVisibility(View.GONE);
                if (holder.plusMinusLayout != null)
                    holder.plusMinusLayout.setVisibility(View.VISIBLE);
                holder.itemView.setEnabled(true);
                holder.productItemLayout.setAlpha(1f);
            }

            if (position == cartDataArrayList.size() - 1)
                if (allInvalidZoneItem.size() > 0 && !quanityUpdated) {
                    checkoutActivity.binding.tvPayment.setBackgroundColor(context.getResources().getColor(R.color.colorSignInBackground));
                    checkoutActivity.binding.tvPayment.setText("Can't Place Order");
                    checkoutActivity.binding.tvPayment.setEnabled(false);
                    checkoutActivity.binding.applyCoupon.setVisibility(View.GONE);
                    checkoutActivity.binding.checkboxCaryBag.setVisibility(View.GONE);
                } else {
                    checkoutActivity.binding.tvPayment.setBackgroundColor(context.getResources().getColor(R.color.colorGreen));
                    checkoutActivity.binding.tvPayment.setText("Place Order");
                    checkoutActivity.binding.tvPayment.setEnabled(true);
                    if (SPData.allowApplyCoupon()) {
                        checkoutActivity.binding.applyCoupon.setVisibility(View.VISIBLE);
                    } else {
                        checkoutActivity.binding.applyCoupon.setVisibility(View.GONE);
                    }
                    if (SPData.showCaryBag())
                        checkoutActivity.binding.checkboxCaryBag.setVisibility(View.VISIBLE);
                    else
                        checkoutActivity.binding.checkboxCaryBag.setVisibility(View.GONE);
                }
        }


        holder.product_name.setText(CommonUtils.capitalizeWord(cartDataArrayList.get(position).getProduct().getProduct().getName()));

        if (cartPageFragment != null && !SPData.enterQuantityManuallyInCart())
            holder.no_ofQuantity.setText("" + cartDataArrayList.get(position).getQuantity());
        else
            holder.no_ofQuantity.setText("x" + cartDataArrayList.get(position).getQuantity());

        if (SPData.enterQuantityManuallyInCart()) {
            if (holder.imageViewEditQuantity != null)
                holder.imageViewEditQuantity.setVisibility(View.VISIBLE);
            if (holder.increase != null)
                holder.increase.setVisibility(View.GONE);
            if (holder.decrease != null)
                holder.decrease.setVisibility(View.GONE);
        } else {
            if (holder.imageViewEditQuantity != null)
                holder.imageViewEditQuantity.setVisibility(View.GONE);
            if (holder.increase != null)
                holder.increase.setVisibility(View.VISIBLE);
            if (holder.decrease != null)
                holder.decrease.setVisibility(View.VISIBLE);
        }

        if (holder.imageViewEditQuantity != null)
            holder.imageViewEditQuantity.setOnClickListener(view -> {
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
                                        int value = Integer.parseInt(flatDialog.getFirstTextField());
                                        int currentQuantity = cartDataArrayList.get(holder.getAdapterPosition()).getQuantity();
                                        int minQty = cartDataArrayList.get(holder.getAdapterPosition()).getProduct().getMinOrderQuantity();
                                        int stock = cartDataArrayList.get(holder.getAdapterPosition()).getProduct().getStock();
                                        int maxQty = cartDataArrayList.get(holder.getAdapterPosition()).getProduct().getMaxOrderQuantity();
                                        if (stock >= value) {
                                            if (value == 0 || value < minQty) {
                                                Toast.makeText(context, "Minimum order quantity is " + minQty, Toast.LENGTH_SHORT).show();
                                            } else if (maxQty > 0 && value > maxQty) {
                                                Toast.makeText(context, "Maximum order quantity is " + maxQty, Toast.LENGTH_SHORT).show();
                                            } else if (currentQuantity < value) {
                                                increaseQuantity(holder, value);
                                            } else if (currentQuantity > value) {
                                                decreaseQuantity(holder, value);
                                            }
                                        } else {
                                            Toast.makeText(context, "There is only " + stock + " stock left", Toast.LENGTH_SHORT).show();
                                        }
                                        flatDialog.dismiss();
                                    } else {
                                        if (cartPageFragment.getActivity() != null)
                                            Toast.makeText(cartPageFragment.requireActivity(), "Enter a valid quantity", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (Exception e) {
                                    Toast.makeText(context, "Try again after some time", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .show();

            });

        setPriceAndDiscount(holder);

        if (holder.image_cartlist != null) {
            if (cartDataArrayList.get(position).getProduct().getImages() != null) {
                Glide.with(context).load(SPData.getBucketUrl() +
                        cartDataArrayList.get(position).getProduct().getImages().getPrimary()).into(holder.image_cartlist);
            } else if (cartDataArrayList.get(position).getProduct().getProduct().getImages() != null) {
                Glide.with(context).load(SPData.getBucketUrl() +
                        cartDataArrayList.get(position).getProduct().getProduct().getImages().getPrimary()).into(holder.image_cartlist);
            } else {
                Glide.with(context).load(context.getResources().getDrawable(R.drawable.noimage)).into(holder.image_cartlist);
            }
        }
        if (holder.product_minQty != null)
            holder.product_minQty.setText("Min qty-" + cartDataArrayList.get(position).getProduct().getMinOrderQuantity());

        if (holder.product_minQty != null)
            if (cartDataArrayList.get(position).getProduct().getMinOrderQuantity() > 1) {
                holder.product_minQty.setVisibility(View.VISIBLE);
            } else {
                holder.product_minQty.setVisibility(View.GONE);
            }

        if (visitedLastItem && position == cartDataArrayList.size() - 1) {
            visitedLastItem = false;
            if (checkoutActivity != null && SPData.useShiprocketDeliveryCharge())
                checkoutActivity.getShiprocketDeliveryCharge();
            if (checkoutActivity != null && SPData.useDeliveryChargeByCoordinates())
                checkoutActivity.getDeliveryChargeByCoordinates();
            else if (checkoutActivity != null)
                checkoutActivity.setPaymentSummary();
        }

        if (holder.deleteItem != null)
            holder.deleteItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    removeProductFromList(cartDataArrayList.get(holder.getAdapterPosition()).getProduct().getId(), holder, true);
                }
            });
        if (holder.decrease != null)
            holder.decrease.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        decreaseQuantity(holder, -1);
                    } catch (Exception e) {
                        Log.e("TAG", "onClick: " + e);
                        Toast.makeText(context, "" + context.getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        if (holder.increase != null)
            holder.increase.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        int currentQuantity = cartDataArrayList.get(position).getQuantity();
                        int stock = cartDataArrayList.get(position).getProduct().getStock();
                        int maxQty = cartDataArrayList.get(position).getProduct().getMaxOrderQuantity();
                        if (currentQuantity < stock) {
                            if (maxQty == 0 || currentQuantity < maxQty)
                                increaseQuantity(holder, -1);
                            else
                                Toast.makeText(context, "Maximum order quantity is " + maxQty, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "There is only " + stock + " stock left", Toast.LENGTH_SHORT).show();
                        }

                    } catch (Exception e) {
                        Log.e("TAG", "onClick: " + e);
                        Toast.makeText(context, "" + context.getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
                    }
                }
            });
//        } catch (Exception e) {
//            Log.e("TAG", "onBindViewHolder: " + e);
//            Toast.makeText(context, "" + e, Toast.LENGTH_SHORT).show();
//        }
    }

    private void setPriceAndDiscount(MyViewHolder holder) {
        double mrp = cartDataArrayList.get(holder.getAdapterPosition()).getProduct().getPrice();
        double sellingPrice = cartDataArrayList.get(holder.getAdapterPosition()).getSellingPrice();
        int quantity = cartDataArrayList.get(holder.getAdapterPosition()).getQuantity();
        double discsaving = cartDataArrayList.get(holder.getAdapterPosition()).getDiscountAmount();
        double realPrice = sellingPrice - discsaving;
        double total = realPrice * quantity;

        if (holder.mrp != null && mrp > 0 && mrp != sellingPrice) {
            holder.mrp.setVisibility(View.VISIBLE);
            holder.mrp.setText(context.getString(R.string.rs) + CommonUtils.getTwoDigitsRoundOffValue(mrp));
        } else if (holder.mrp != null) {
            holder.mrp.setVisibility(View.GONE);
            mrp = realPrice;
        }

        holder.totalAmt.setText("Total: " + context.getString(R.string.rs) + CommonUtils.getTwoDigitsRoundOffValue(total));

        if (holder.product_price != null)
            holder.product_price.setText(context.getResources().getString(R.string.rs) + CommonUtils.getTwoDigitsRoundOffValue(realPrice));

        double saving = (mrp - realPrice) * quantity;

        if (holder.product_discount != null)
            if (saving > 0) {
                if (cartPageFragment != null && holder.product_discount != null) {
                    holder.product_discount.setVisibility(View.VISIBLE);
                    if (SPData.useGreenYouSaveColor())
                        holder.product_discount.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
                    holder.product_discount.setText("You save " + context.getResources().getString(R.string.rs) + CommonUtils.getTwoDigitsRoundOffValue(saving));
                } else {
                    holder.product_discount.setVisibility(View.GONE);
                }
            } else {
                holder.product_discount.setVisibility(View.INVISIBLE);
            }
    }

    public static HashMap<String, Integer> sortByValue(HashMap<String, Integer> hm) {
        List<Map.Entry<String, Integer>> list =
                new LinkedList<>(hm.entrySet());
        Collections.sort(list, (o1, o2) -> (o1.getValue()).compareTo(o2.getValue()));
        HashMap<String, Integer> temp = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }


    private void increaseQuantity(MyViewHolder holder, int quantityUpdateTo) {
        if (holder.getAdapterPosition() < cartDataArrayList.size()) {
            quanityUpdated = true;
            int currentQty = cartDataArrayList.get(holder.getAdapterPosition()).getQuantity();
            String varientID = cartDataArrayList.get(holder.getAdapterPosition()).getProduct().getId();
            int quantity;
            if (quantityUpdateTo == -1) {
                quantity = currentQty + 1;
            } else {
                quantity = quantityUpdateTo;
            }
            updateItem(varientID, quantity, holder.getAdapterPosition());
        } else {
            Toast.makeText(context, "Try again after some time", Toast.LENGTH_SHORT).show();
        }
    }

    private void decreaseQuantity(MyViewHolder holder, int quantityUpdateTo) {
        quanityUpdated = true;
        int minOrderQuantity = cartDataArrayList.get(holder.getAdapterPosition()).getProduct().getMinOrderQuantity();
        if (minOrderQuantity > 1) {
            if (cartDataArrayList.get(holder.getAdapterPosition()).getQuantity() > minOrderQuantity) {
                updateCartItem(holder, holder.getAdapterPosition(), quantityUpdateTo);
            } else {
                removeProductFromList(cartDataArrayList.get(holder.getAdapterPosition()).getProduct().getId(), holder, false);
            }
        } else if (cartDataArrayList.get(holder.getAdapterPosition()).getQuantity() > 1) {
            updateCartItem(holder, holder.getAdapterPosition(), quantityUpdateTo);
        } else {
            removeProductFromList(cartDataArrayList.get(holder.getAdapterPosition()).getProduct().getId(), holder, false);
        }
    }

    private void updateCartItem(MyViewHolder holder, int position, int quantityUpdateTo) {
        String varientID = cartDataArrayList.get(position).getProduct().getId();
        int quantity;
        if (quantityUpdateTo == -1) {
            quantity = cartDataArrayList.get(holder.getAdapterPosition()).getQuantity() - 1;
        } else {
            quantity = quantityUpdateTo;
        }
        updateItem(varientID, quantity, position);
    }

    private void deleteItem(MyViewHolder holder, Response<CartDetailsInformation> response) {
        if (holder.product_name.getText().toString().toLowerCase().contains(inActiveProductName.toLowerCase())) {
            inActiveProductName = "";
        }
        int minOrderQuantity = cartDataArrayList.get(holder.getAdapterPosition()).getProduct().getMinOrderQuantity();
        if (minOrderQuantity > 1)
            Toast.makeText(context, "Min order quantity is " + minOrderQuantity, Toast.LENGTH_SHORT).show();
        cartDataArrayList.clear();
        cartDataArrayList.addAll(Objects.requireNonNull(response.body()).getData().getProducts());
        notifyDataSetChanged();
        if (cartPageFragment != null && cartDataArrayList.size() == 0) {
            cartPageFragment.binding.proceedToCheckout.setVisibility(View.GONE);
            cartPageFragment.binding.noProducts.setVisibility(View.VISIBLE);
            SPData.getAppPreferences().setCartVendorId("");
        }
    }

    @Override
    public int getItemCount() {
        if (cartDataArrayList != null)
            return cartDataArrayList.size();
        else return 0;
    }

    private void removeProductFromList(String id, MyViewHolder holder, boolean b) {
        if (!appPreferences.getUsertoken().isEmpty()) {
            ApiService apiService = ApiUtils.getHeaderAPIService(appPreferences.getUsertoken());
            apiService.removeItemFromCart(id).enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(@NotNull Call<JsonObject> call, @NotNull Response<JsonObject> response) {
                    getCart(holder, b);
                }

                @Override
                public void onFailure(@NotNull Call<JsonObject> call, @NotNull Throwable t) {
                    Toast.makeText(context, "Failed to connect " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            ApiService apiService = ApiUtils.getHeaderAPIService(appPreferences.getUserUniqueId());
            apiService.removeItemFromCartWithoutLogin(id).enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(@NotNull Call<JsonObject> call, @NotNull Response<JsonObject> response) {
                    getCart(holder, b);
                }

                @Override
                public void onFailure(@NotNull Call<JsonObject> call, @NotNull Throwable t) {
                    Toast.makeText(context, "Failed to connect", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void getCart(MyViewHolder holder, boolean b) {
        if (SPData.getAppPreferences().getUsertoken().isEmpty()) {
            ApiService apiService = ApiUtils.getHeaderAPIService(appPreferences.getUserUniqueId());
            apiService.showCartItemsWithoutLogin(SPData.getAppPreferences().getPincode()).enqueue(new Callback<CartDetailsInformation>() {
                @Override
                public void onResponse(@NotNull Call<CartDetailsInformation> call, @NotNull Response<CartDetailsInformation> response) {
                    checkRemoveItemResponse(response, holder, b);
                }

                @Override
                public void onFailure(@NotNull Call<CartDetailsInformation> call, @NotNull Throwable t) {
                    Toast.makeText(context, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            ApiService apiService = ApiUtils.getHeaderAPIService(appPreferences.getUsertoken());
            apiService.showCartItems(SPData.getAppPreferences().getPincode()).enqueue(new Callback<CartDetailsInformation>() {
                @Override
                public void onResponse(@NotNull Call<CartDetailsInformation> call, @NotNull Response<CartDetailsInformation> response) {
                    checkRemoveItemResponse(response, holder, b);
                }

                @Override
                public void onFailure(@NotNull Call<CartDetailsInformation> call, @NotNull Throwable t) {
                    Toast.makeText(context, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void checkRemoveItemResponse(Response<CartDetailsInformation> response, MyViewHolder holder, boolean b) {
        try {
            if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                if (b && allInvalidZoneItem != null) {
                    allInvalidZoneItem.remove(holder);
                }
                deleteItem(holder, response);
                SPData.getAppPreferences().setTotalCartCount(cartDataArrayList.size());
                if (context instanceof MainActivity) {
                    ((MainActivity) context).setCartBadge();
                }
                CommonUtils.showSuccessMessage(context, "Removed from cart !!");
                if (cartPageFragment != null) {
                    cartPageFragment.setCartAmount(response.body());
                }
            } else {
                Toast.makeText(context, response.code() + " " + response.message(), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(context, "" + e, Toast.LENGTH_SHORT).show();
        }
    }

    private void updateItem(String id, final int quantity, final int position) {
        try {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("quantity", quantity);
            if (!SPData.getAppPreferences().getUsertoken().isEmpty()) {
                ApiService apiService = ApiUtils.getHeaderAPIService(appPreferences.getUsertoken());
                apiService.updateCartItemInCartPage(id, jsonObject).enqueue(new Callback<CartDetailsInformation>() {
                    @Override
                    public void onResponse(@NonNull Call<CartDetailsInformation> call, @NonNull Response<CartDetailsInformation> response) {
                        checkUpdateItemResponse(response, position);
                    }

                    @Override
                    public void onFailure(@NotNull Call<CartDetailsInformation> call, @NotNull Throwable t) {
                        Log.e("TAG", call.toString());
                        Toast.makeText(context, "Failed to connect", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                ApiService apiService = ApiUtils.getHeaderAPIService(appPreferences.getUserUniqueId());
                apiService.updateCartItemWithoutLoginInCartPage(id, jsonObject).enqueue(new Callback<CartDetailsInformation>() {
                    @Override
                    public void onResponse(@NonNull Call<CartDetailsInformation> call, @NonNull Response<CartDetailsInformation> response) {
                        checkUpdateItemResponse(response, position);
                    }

                    @Override
                    public void onFailure(@NotNull Call<CartDetailsInformation> call, @NotNull Throwable t) {
                        Toast.makeText(context, "Failed to connect " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } catch (Exception e) {
            Toast.makeText(context, "" + e, Toast.LENGTH_SHORT).show();
        }
    }

    private void checkUpdateItemResponse(Response<CartDetailsInformation> response, int position) {
        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
            notifyItemChanged(position);
            cartDataArrayList.clear();
            cartDataArrayList.addAll(response.body().getData().getProducts());
            Toast.makeText(context, "Quantity Updated!!", Toast.LENGTH_SHORT).show();
            if (cartPageFragment != null) {
                cartPageFragment.setCartAmount(response.body());
            }
        } else {
            Toast.makeText(context, response.code() + " " + response.message(), Toast.LENGTH_SHORT).show();
        }
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView image_cartlist;
        TextView product_name, product_description, product_price, product_minQty, product_discount;
        Button increase, decrease;
        LinearLayout plusMinusLayout;
        LinearLayout productItemLayout;
        TextView mrp;
        ImageView deleteItem;
        private final TextView no_ofQuantity;
        private final TextView validZone;
        TextView totalAmt;
        ImageView imageViewEditQuantity;

        public MyViewHolder(View view) {
            super(view);

            image_cartlist = view.findViewById(R.id.image_cartlist);
            product_name = view.findViewById(R.id.product_name);
            product_description = view.findViewById(R.id.product_description);
            product_price = view.findViewById(R.id.product_price);
            increase = view.findViewById(R.id.increase);
            decrease = view.findViewById(R.id.decrease);
            no_ofQuantity = view.findViewById(R.id.no_of_quantity);
            product_minQty = view.findViewById(R.id.product_qty);
            product_discount = view.findViewById(R.id.cart_discount);
            validZone = view.findViewById(R.id.invalid_zone);
            plusMinusLayout = view.findViewById(R.id.plus_minus_linearLayout);
            productItemLayout = view.findViewById(R.id.product_item_content);
            deleteItem = view.findViewById(R.id.delete_item);
            mrp = view.findViewById(R.id.product_mrp);
            totalAmt = view.findViewById(R.id.total_amt);
            imageViewEditQuantity = view.findViewById(R.id.edit_quantity);
        }
    }

}