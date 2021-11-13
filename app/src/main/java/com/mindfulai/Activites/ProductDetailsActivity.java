package com.mindfulai.Activites;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.TextViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flatdialoglibrary.dialog.FlatDialog;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.gson.JsonObject;
import com.mindfulai.Adapter.BigBasketAttributesAdapter;
import com.mindfulai.Adapter.OptionViewAdapter;
import com.mindfulai.Adapter.ProductsAdapter;
import com.mindfulai.Adapter.ReviewsAdapter;
import com.mindfulai.Adapter.SliderAdapterExample;
import com.mindfulai.Adapter.UserAddressesAdapter;
import com.mindfulai.Adapter.WholeSalesPriceAdapter;
import com.mindfulai.Models.CartInformation.CartDetailsInformation;
import com.mindfulai.Models.ReviewData.Datum;
import com.mindfulai.Models.ReviewData.ReviewData;
import com.mindfulai.Models.UserBaseAddress;
import com.mindfulai.Models.UserDataAddress;
import com.mindfulai.Models.varientsByCategory.Attribute__;
import com.mindfulai.Models.varientsByCategory.Images;
import com.mindfulai.Models.varientsByCategory.Option;
import com.mindfulai.Models.varientsByCategory.OptionsAttribute;
import com.mindfulai.Models.varientsByCategory.PageVarientsByCategory;
import com.mindfulai.Models.varientsByCategory.Varient;
import com.mindfulai.Models.varientsByCategory.VarientsByCategory;
import com.mindfulai.Models.varientsByCategory.Vendor;
import com.mindfulai.Models.varientsByCategory.WholeSalePriceModel;
import com.mindfulai.NetworkRetrofit.ApiService;
import com.mindfulai.NetworkRetrofit.ApiUtils;
import com.mindfulai.Utils.CommonUtils;
import com.mindfulai.Utils.CustomProgressDialog;
import com.mindfulai.Utils.SPData;
import com.mindfulai.ministore.R;
import com.mindfulai.ministore.databinding.ActivityProductDetails2Binding;
import com.mindfulai.ministore.databinding.ActivityProductDetailsBigBasketBinding;
import com.mindfulai.ministore.databinding.ActivityProductDetailsBinding;
import com.smarteist.autoimageslider.SliderAnimations;
import com.valdesekamdem.library.mdtoast.MDToast;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.nikartm.support.ImageBadgeView;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.mindfulai.Utils.CommonUtils.capitalizeWord;

public class ProductDetailsActivity extends AppCompatActivity {

    private static final String TAG = "ProductDetails";
    private static final int PROUDUCT_REQUEST_CODE = 104;
    public LinearLayoutManager linearLayoutManager;
    public SliderAdapterExample sliderAdapterExample;
    public String varient_id;
    private String category_id;
    public int stock;
    private ReviewsAdapter reviewsAdapter;
    private List<Datum> reviewList = new ArrayList<>();
    private final ArrayList<String> images = new ArrayList<>();
    private boolean isInWishlist;
    private ImageBadgeView cartBadge;
    private boolean isRecommended;
    private AlertDialog alertDialog1;
    private okhttp3.Response response;
    private String returnPolicytxt = "";
    private ArrayList<UserDataAddress> userDataAddressArrayList;
    public boolean available = true;
    public int minQty = 1, page = 0;
    public int totalPages = 1;
    public ActivityProductDetailsBinding productDetailsBinding;
    private ApiService apiService;
    public com.mindfulai.Models.varientsByCategory.Datum productData;
    private String productId;
    private Uri deepLink;
    public ArrayList<OptionsAttribute> optionsAttributeArrayList;
    public ArrayList<Varient> differentVarientsArrayList;
    public int maxQty;
    private final ArrayList<WholeSalePriceModel> wholeSalePriceModelArrayList = new ArrayList<>();
    private WholeSalesPriceAdapter wholeSalesPriceAdapter;
    private Varient selectedVarient;
    private boolean preorder;
    private Intent intent;

    @SuppressLint({"SetTextI18n", "InflateParams"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            LayoutInflater layoutInflater = getLayoutInflater();
            layoutInflater.inflate(R.layout.activity_product_details, null);
            productDetailsBinding = ActivityProductDetailsBinding.inflate(layoutInflater);
            setContentView(productDetailsBinding.getRoot());
            setTitle("Product Details");
            Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

            intent = getIntent();
            apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());
            preorder = intent.getBooleanExtra("preorder", false);
            productDetailsBinding.progressBar.setVisibility(GONE);

            getReturnPolicy();
            checkSPData();
            getProductDetailByProduct();
            setOnClick();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("TAG", "onCreate: " + e.getMessage());
        }
    }

    private void getProductDetailByProduct() {
        productId = getIntent().getStringExtra("product_id");
        Log.e("TAG", "getProductDetailByProduct: " + productId);
        if (productId != null)
            loadData();
        else
            handleReferralsIfAny();
    }

    private void loadData() {
        apiService.getVarientsByProductId(productId).enqueue(new Callback<VarientsByCategory>() {
            @Override
            public void onResponse(@NotNull Call<VarientsByCategory> call, @NotNull Response<VarientsByCategory> response) {
                Log.e("TAG", "onResponse: " + response);
                if (response.isSuccessful() && response.body() != null) {
                    List<com.mindfulai.Models.varientsByCategory.Datum> allVarientData = response.body().getData();
                    if (allVarientData.size() > 0) {
                        productData = allVarientData.get(0);
                        productDetailsBinding.rvAddToCart.setVisibility(VISIBLE);
                        productDetailsBinding.nestedScrollView.setVisibility(VISIBLE);
                        productDetailsBinding.noProductLayout.setVisibility(GONE);
                        productDetailsBinding.progressBar.setVisibility(GONE);
                        setProductDetails();
                    } else {
                        productDetailsBinding.progressBar.setVisibility(GONE);
                        handleError();
                        CommonUtils.showToastMessageAtCenter(ProductDetailsActivity.this,"No product found");
                    }
                } else {
                    handleError();
                }
            }

            @Override
            public void onFailure(@NotNull Call<VarientsByCategory> call, @NotNull Throwable t) {
                handleError();
            }
        });
    }

    private void handleError() {
        productDetailsBinding.rvAddToCart.setVisibility(GONE);
        productDetailsBinding.progressBar.setVisibility(GONE);
        productDetailsBinding.nestedScrollView.setVisibility(GONE);
        productDetailsBinding.noProductLayout.setVisibility(VISIBLE);
    }

    @SuppressLint("SetTextI18n")
    private void setProductDetails() {
        if (productData != null) {
            page = getIntent().getIntExtra("page", 0) + 1;
            totalPages = getIntent().getIntExtra("total_page", 1);
            if (page > totalPages) {
                page = 1;
            }
            if (SPData.showWholeSalePrice()) {
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
                productDetailsBinding.recyclerViewWholeSales.setLayoutManager(linearLayoutManager);
                wholeSalesPriceAdapter = new WholeSalesPriceAdapter(this, wholeSalePriceModelArrayList);
                productDetailsBinding.recyclerViewWholeSales.setAdapter(wholeSalesPriceAdapter);
            }

            handleProduct();

            setRecommendedText();

            handleVarient(productData.getVarients().get(0));

            if (SPData.showBigBasketProductDetail()) {
                setAllOptionVarientsInRadioGroup();
            } else {
                getAllOptionsVarientOfProduct();
            }

            getRealtedProducts();

        } else {
            handleNoProducts();
        }
    }

    private void handleProduct() {
        String description_ = productData.getProduct().getDetails();
        isRecommended = productData.getProduct().getIsRecommended();
        String productName = productData.getProduct().getName();
        category_id = productData.getProduct().getCategory().getId();
        boolean returnable = productData.getProduct().isReturnable();
        if (preorder) {
            productDetailsBinding.wishlistLayout.setVisibility(GONE);
        } else
            productDetailsBinding.wishlistLayout.setVisibility(VISIBLE);

        if (returnable) {
            productDetailsBinding.textReturnable.setText("Returnable");
            productDetailsBinding.textReturnable.setTextColor(getResources().getColor(R.color.colorGreen));
        } else {
            productDetailsBinding.textReturnable.setText("Non Returnable");
            productDetailsBinding.textReturnable.setTextColor(getResources().getColor(R.color.colorError));
        }

        productDetailsBinding.productName.setText(capitalizeWord(productName));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && description_ != null)
            productDetailsBinding.description.setText(Html.fromHtml(description_, Html.FROM_HTML_MODE_COMPACT));
        else
            productDetailsBinding.description.setText(Html.fromHtml(description_));

        if (SPData.showBrand()) {
            if (productData.getProduct().getBrand() != null)
                productDetailsBinding.vendorNameDetail.setText("by " + capitalizeWord(productData.getProduct().getBrand().getName()));
            else
                productDetailsBinding.vendorNameDetail.setVisibility(GONE);
        } else {
            productDetailsBinding.vendorNameDetail.setVisibility(GONE);
        }
        try {
            SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, dd MMM");
            String from = "", to = "";
            if (productData.getProduct().getAvailableFrom() != null) {
                Date date = iso.parse(productData.getProduct().getAvailableFrom());
                Date date1 = CommonUtils.gmttoLocalDate(Objects.requireNonNull(date));
                from = simpleDateFormat.format(date1);
            }
            if (productData.getProduct().getExpireOn() != null) {
                Date date = iso.parse(productData.getProduct().getExpireOn());
                Date date1 = CommonUtils.gmttoLocalDate(Objects.requireNonNull(date));
                to = simpleDateFormat.format(date1);
            }
            if (from.isEmpty() || to.isEmpty()) {
                productDetailsBinding.availableFrom.setVisibility(GONE);
            } else {
                productDetailsBinding.availableFrom.setVisibility(VISIBLE);
                productDetailsBinding.availableFrom.setText("Available from " + from + " to " + to);
            }
        } catch (Exception e) {
            Log.e("TAG", "handleProduct: " + e);
            productDetailsBinding.availableFrom.setVisibility(GONE);
        }

        getReview();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void handleVarient(Varient varient) {
        int incart = varient.getInCart();
        this.selectedVarient = varient;
        varient_id = varient.getId();
        minQty = varient.getMinOrderQuantity();
        maxQty = varient.getMaxOrderQuantity();
        isInWishlist = varient.getWishlist();
        stock = varient.getStock();
        productDetailsBinding.rvAddToCart.setVisibility(VISIBLE);
        productDetailsBinding.icWishlist.setVisibility(VISIBLE);

        if (incart > 0 && SPData.showcartIconAddToCart()) {
            hideAddToCart(incart);
        } else if (!SPData.showcartIconAddToCart()) {
            productDetailsBinding.rvAddToCart.setVisibility(GONE);
        } else {
            showAddtoCart();
        }
        if (SPData.showSubscription() && varient.isSubscribable()&&(!varient.isSubscribed())) {
            productDetailsBinding.productRepeat.setVisibility(VISIBLE);
            productDetailsBinding.productRepeat.setEnabled(true);
            ((TextView)productDetailsBinding.productRepeat.getChildAt(0)).setText("Subscribe");
        } else  if (SPData.showSubscription() && varient.isSubscribable()&&(varient.isSubscribed())){
            productDetailsBinding.productRepeat.setVisibility(View.VISIBLE);
            productDetailsBinding.productRepeat.setEnabled(false);
            ((TextView)productDetailsBinding.productRepeat.getChildAt(0)).setText("Subscribed");
        }else {
            productDetailsBinding.productRepeat.setVisibility(View.GONE);
        }
        if (SPData.showMembership() && varient.getMemberPrice() > 0) {
            if (SPData.getAppPreferences().isMembershipPurchased()) {
                productDetailsBinding.purchasedMemberPrice.setVisibility(VISIBLE);
                productDetailsBinding.ml.membershipLayout.setVisibility(GONE);
                productDetailsBinding.purchasedMemberPrice.setText(getString(R.string.rs) + CommonUtils.getTwoDigitsRoundOffValue(varient.getMemberPrice()));
            } else {
                productDetailsBinding.ml.membershipLayout.setVisibility(VISIBLE);
                productDetailsBinding.purchasedMemberPrice.setVisibility(GONE);
            }
            productDetailsBinding.ml.memberPrice.setText(getString(R.string.rs) + CommonUtils.getTwoDigitsRoundOffValue(varient.getMemberPrice()));
        } else {
            productDetailsBinding.purchasedMemberPrice.setVisibility(GONE);
            productDetailsBinding.ml.membershipLayout.setVisibility(GONE);
        }

        productDetailsBinding.icWishlist.setSelected(isInWishlist);
        if (isInWishlist) {
            productDetailsBinding.icWishlist.setText("Remove from save");
        } else {
            productDetailsBinding.icWishlist.setText("Save for later");
        }

        if (varient.getTag() != null && !varient.getTag().isEmpty() && SPData.showTag()) {
            productDetailsBinding.tag.setVisibility(View.VISIBLE);
            productDetailsBinding.tag.setText(varient.getTag());
        } else
            productDetailsBinding.tag.setVisibility(GONE);

        if (CommonUtils.stringIsNotNullAndEmpty(varient.getTag()) && varient.getTag().equals(getString(R.string.veg))) {
            productDetailsBinding.veg.setVisibility(VISIBLE);
            productDetailsBinding.veg.setImageDrawable(getResources().getDrawable(R.drawable.veg));
        } else if (CommonUtils.stringIsNotNullAndEmpty(varient.getTag()) && varient.getTag().equals(getString(R.string.nonveg))) {
            productDetailsBinding.veg.setVisibility(VISIBLE);
            productDetailsBinding.veg.setImageDrawable(getResources().getDrawable(R.drawable.ic_non_veg));
        } else
            productDetailsBinding.veg.setVisibility(GONE);


        if (varient.getReviews() != null) {
            int reviews_count = varient.getReviews().getTotal();
            double average_rating = varient.getReviews().getRating();
            DecimalFormat df = new DecimalFormat("#.##");
            String formatted_rating = df.format(average_rating);
            if (SPData.useMyntraProductDetail())
                productDetailsBinding.productReviews.setText(reviews_count + "");
            else
                productDetailsBinding.productReviews.setText(reviews_count + " Reviews");

            productDetailsBinding.textRatings.setText(formatted_rating + "");
        } else {
            productDetailsBinding.productReviews.setText("0 Reviews");
            productDetailsBinding.textRatings.setText("0");
        }

        if (SPData.showWholeSalePrice()) {
            ArrayList<WholeSalePriceModel> wholeSalePriceModels = varient.getWholeSalePrices();
            if (wholeSalePriceModelArrayList != null) {
                if (wholeSalePriceModels != null) {
                    wholeSalePriceModelArrayList.clear();
                    Collections.sort(wholeSalePriceModels, (o1, o2) -> (int) (o1.getUpto() - o2.getUpto()));
                    wholeSalePriceModelArrayList.addAll(wholeSalePriceModels);
                }
                if (wholeSalesPriceAdapter != null && wholeSalePriceModelArrayList.size() > 0) {
                    productDetailsBinding.recyclerViewWholeSales.setVisibility(VISIBLE);
                    wholeSalesPriceAdapter.notifyDataSetChanged();
                } else {
                    productDetailsBinding.recyclerViewWholeSales.setVisibility(GONE);
                }
            }
        }

        setPriceData(varient);

        setImagesData(varient);

    }

    private void setAllOptionVarientsInRadioGroup() {
        productDetailsBinding.recyclerviewOptionValues.setLayoutManager(new LinearLayoutManager(this));
        BigBasketAttributesAdapter attributesAdapter = new BigBasketAttributesAdapter(this, productData.getVarients());
        productDetailsBinding.recyclerviewOptionValues.setAdapter(attributesAdapter);
        attributesAdapter.notifyDataSetChanged();
    }


    private void handleReferralsIfAny() {
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, pendingDynamicLinkData -> {
                    if (pendingDynamicLinkData != null && pendingDynamicLinkData.getLink() != null) {
                        deepLink = pendingDynamicLinkData.getLink();
                        productId = deepLink.getQueryParameter("product");
                        loadData();
                    } else {
                        Log.e("TAG", "onSuccess: " + deepLink);
                        handleError();
                    }
                }).addOnFailureListener(e -> {
            Log.e("TAG", "onFailure: " + e.getMessage());
            handleError();
        });
    }


    private void setOnClick() {

        productDetailsBinding.ml.membershipLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMembership();
            }
        });

        productDetailsBinding.textReturnable.setOnClickListener(view -> showReturnPolicy());

        productDetailsBinding.share.setOnClickListener(v -> {
            try {
                DynamicLink dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
                        .setLink(Uri.parse(getString(R.string.linkUrl) + "product=" + productData.getProduct().getId()))
                        .setDomainUriPrefix("https://" + getString(R.string.productDetailUriPrefix))
                        .setAndroidParameters(new DynamicLink.AndroidParameters.Builder(getPackageName()).build())
                        .setIosParameters(new DynamicLink.IosParameters.Builder("com.example.ios").build())
                        .buildDynamicLink();
                Uri dynamicLinkUri = dynamicLink.getUri();
                //  String shareLink = URLDecoder.decode(dynamicLinkUri.toString(), "UTF-8");
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, "" + getString(R.string.app_name));
                intent.putExtra(Intent.EXTRA_TEXT, SPData.productShareMsg() + "\n\n" + productData.getProduct().getName() + "\n" + dynamicLinkUri.toString());
                startActivity(Intent.createChooser(intent, "Share"));
            } catch (Exception e) {
                Log.e("TAG", "setOnClick: " + e);
            }
        });

        productDetailsBinding.icWishlist.setOnClickListener(v -> {
            if (!SPData.getAppPreferences().getUsertoken().equals(""))
                if (isInWishlist) {
                    isInWishlist = false;
                    selectedVarient.setWishlist(false);
                    removeItemFromWishList(varient_id);
                    productDetailsBinding.icWishlist.setSelected(false);
                    productDetailsBinding.icWishlist.setText("Save for later");
                } else {
                    isInWishlist = true;
                    selectedVarient.setWishlist(true);
                    addItemToWishList(varient_id);
                    productDetailsBinding.icWishlist.setSelected(true);
                    productDetailsBinding.icWishlist.setText("Remove from save");
                }
            else {
                startActivity(new Intent(ProductDetailsActivity.this, LoginActivity.class));
            }
        });

        productDetailsBinding.increase.setOnClickListener(v -> checkItemToAdded());

        productDetailsBinding.decrease.setOnClickListener(v -> {
            String id = varient_id;
            int currentQty = Integer.parseInt(productDetailsBinding.noOfQuantity.getText().toString());
            boolean varientRemove = false;
            if ((minQty > 1 && minQty == currentQty) || (currentQty == 1)) {
                if (currentQty != 1) {
                    CommonUtils.showToastMessageAtCenter(ProductDetailsActivity.this,"Minimum order quantity is " + minQty);
                }
                varientRemove = true;
            }
            if (varientRemove) {
                removeProductFromList(id);
            } else {
                updateItem(id, currentQty - 1);
            }
        });
        productDetailsBinding.editQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FlatDialog flatDialog = new FlatDialog(ProductDetailsActivity.this);
                flatDialog.setTitle("Enter quantity")
                        .setSubtitle("Update cart item quantity")
                        .setFirstTextFieldHint("quantity")
                        .setFirstButtonText("Done")
                        .isCancelable(true)
                        .setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark))
                        .setFirstTextFieldInputType(InputType.TYPE_CLASS_NUMBER)
                        .setFirstButtonTextColor(getResources().getColor(R.color.colorPrimaryDark))
                        .setFirstButtonColor(getResources().getColor(R.color.colorWhite))
                        .withFirstButtonListner(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                try {
                                    if (CommonUtils.stringIsNotNullAndEmpty(flatDialog.getFirstTextField())) {
                                        int value = Integer.parseInt(flatDialog.getFirstTextField());
                                        if (stock >= value) {
                                            if (value == 0 || value < minQty) {
                                                CommonUtils.showToastMessageAtCenter(ProductDetailsActivity.this,""+"Minimum order quantity is " + minQty);
                                            } else if (maxQty > 0 && value > maxQty) {
                                                CommonUtils.showToastMessageAtCenter(ProductDetailsActivity.this,""+"Maximum order quantity is " + maxQty);
                                            } else {
                                                updateItem(varient_id, value);
                                            }
                                        } else {
                                            CommonUtils.showToastMessageAtCenter(ProductDetailsActivity.this,"There is only " + stock + " stock left");
                                        }
                                        flatDialog.dismiss();
                                    } else {
                                        CommonUtils.showToastMessageAtCenter(ProductDetailsActivity.this,"Enter a valid quantity");
                                    }
                                } catch (Exception e) {
                                    CommonUtils.showToastMessageAtCenter(ProductDetailsActivity.this,"Try again after some time");
                                }
                            }
                        })
                        .show();
            }
        });
        productDetailsBinding.buyNow.setOnClickListener(v -> {
            if (SPData.getAppPreferences().getUsertoken().isEmpty()) {
                startActivity(new Intent(ProductDetailsActivity.this, LoginActivity.class));
                CommonUtils.showToastMessageAtCenter(ProductDetailsActivity.this,"Login to buy this item");
            } else if (selectedVarient.getInCart() > 0) {
               moveToCheckout(true);
            } else {
                handleAddToCartClick();
                if(selectedVarient.getInCart()>0){
                   moveToCheckout(true);
                }
            }
        });
        productDetailsBinding.addToCart.setOnClickListener(v -> {
            handleAddToCartClick();
        });

        productDetailsBinding.productRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getAllAddress();

            }
        });
    }

    private void handleAddToCartClick() {
        if (preorder) {
            Intent intent = new Intent(ProductDetailsActivity.this, PreOrderCheckoutActivity.class);
            String image = null;
            if (images != null && images.size() > 0)
                image = images.get(0);
            float mrp = 0;
            if (productDetailsBinding.productMrp.getVisibility() == VISIBLE) {
                mrp = (float) Double.parseDouble(productDetailsBinding.productMrp.getText().toString().replace(getString(R.string.rs), ""));
            }
            float sellingPrice = 0;
            if (productDetailsBinding.productPrice.getVisibility() == VISIBLE) {
                sellingPrice = (float) Double.parseDouble(productDetailsBinding.productPrice.getText().toString().replace(getString(R.string.rs), ""));
            }
            intent.putExtra("name", productData.getProduct().getName());
            intent.putExtra("image", image);
            intent.putExtra("sp", sellingPrice);
            intent.putExtra("mrp", mrp);
            intent.putExtra("id", varient_id);
            intent.putExtra("from", productData.getProduct().getAvailableFrom());
            intent.putExtra("to", productData.getProduct().getExpireOn());
            String attributes = productDetailsBinding.attributes.getText().toString();
            String trimString = attributes.substring(0, attributes.length() - 2);
            intent.putExtra("attribute", trimString);
            startActivity(intent);
        } else if (!productDetailsBinding.addToCart.getText().equals(SPData.getViewCartTxt())) {
            handleAddToCart();
        } else {
            moveToCheckout(false);
        }
    }

    private void openMembership() {
        startActivity(new Intent(ProductDetailsActivity.this, CheckMembershipActivity.class));
    }

    private void moveToCheckout(boolean moveToCheckout) {
        startActivityForResult(new Intent(ProductDetailsActivity.this, CartPageActivity.class).putExtra("moveToCheckout",true), 10);
    }

    private void handleAddToCart() {
        Vendor vendor = productData.getProduct().getCreatedBy();
        String cartVendorId = SPData.getAppPreferences().getCartVendorId();

        if (SPData.allowOnlyOneVendorInCart() && vendor != null && !cartVendorId.isEmpty() && !cartVendorId.equals(vendor.getId())) {
            showOnlyOneVendorDialog(productData.getProduct().getName());
        } else
            addItemToCart();
    }

    private void showAddtoCart() {
        productDetailsBinding.addToCart.setVisibility(VISIBLE);
        productDetailsBinding.linearLayoutPlusMinus.setVisibility(GONE);
        productDetailsBinding.noOfQuantity.setText("0");
        if (stock > 0) {
            if (preorder) {
                productDetailsBinding.addToCart.setText(getString(R.string.buy_now));
            } else
                productDetailsBinding.addToCart.setText("Add to cart");
            productDetailsBinding.addToCart.setEnabled(true);
            productDetailsBinding.addToCart.setTextColor(getResources().getColor(R.color.colorWhite));
        } else {
            productDetailsBinding.addToCart.setText("Out of stock");
            productDetailsBinding.addToCart.setEnabled(false);
            productDetailsBinding.addToCart.setTextColor(getResources().getColor(R.color.outOfStock));
        }
    }

    private void showOnlyOneVendorDialog(String name) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ProductDetailsActivity.this);
        builder.setTitle("Replace cart item?");
        builder.setMessage("Your cart contains item from another vendor.Do you want to clear the cart and add " + name);
        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                clearCart();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    private void addItemToCart() {
        if (stock > 0 && available) {
            int minOrderQty = 1;
            if (minQty > 1) {
                minOrderQty = minQty;
                CommonUtils.showToastMessageAtCenter(ProductDetailsActivity.this,"Minimum order quantity is " + minOrderQty);
            }
            if (SPData.askForConfirmationAddToCart())
                askForConfirmation(varient_id, minOrderQty, true);
            else
                addItem(varient_id, minOrderQty, false);
        } else if (!available) {
            CommonUtils.showToastMessageAtCenter(ProductDetailsActivity.this,"This varient is not available");
        } else {
            CommonUtils.showToastMessageAtCenter(ProductDetailsActivity.this,""+stock + " left");
        }
    }

    private void clearCart() {
        String token = SPData.getAppPreferences().getUsertoken();
        if (!token.isEmpty()) {
            ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());
            apiService.clearCart().enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(@NotNull Call<JsonObject> call, @NotNull Response<JsonObject> response) {
                    handleClearCartResponse(response);
                }

                @Override
                public void onFailure(@NotNull Call<JsonObject> call, @NotNull Throwable t) {
                    CommonUtils.showErrorMessage(ProductDetailsActivity.this, "" + t.getMessage());
                }
            });
        } else {
            ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUserUniqueId());
            apiService.clearTempCart().enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(@NotNull Call<JsonObject> call, @NotNull Response<JsonObject> response) {
                    handleClearCartResponse(response);
                }

                @Override
                public void onFailure(@NotNull Call<JsonObject> call, @NotNull Throwable t) {
                    CommonUtils.showErrorMessage(ProductDetailsActivity.this, "" + t.getMessage());
                }
            });
        }

    }

    private void handleClearCartResponse(Response<JsonObject> response) {
        if (response.isSuccessful() && response.body() != null) {
            SPData.getAppPreferences().setTotalCartCount(0);
            addItemToCart();
        } else {
            CommonUtils.showErrorMessage(ProductDetailsActivity.this, "" + this.response.message());
        }
    }


    private void checkSPData() {

        if (SPData.showBuyNowBtn()) {
            productDetailsBinding.buyNow.setVisibility(VISIBLE);
        } else
            productDetailsBinding.buyNow.setVisibility(GONE);

        if (SPData.allowProductShare()) {
            productDetailsBinding.share.setVisibility(VISIBLE);
        } else
            productDetailsBinding.share.setVisibility(GONE);

        if (SPData.enterQuantityManuallyInProducts()) {
            productDetailsBinding.editQuantity.setVisibility(VISIBLE);
        } else
            productDetailsBinding.editQuantity.setVisibility(GONE);

        if (SPData.getAppPreferences().isMembershipPurchased()) {
            productDetailsBinding.ml.membershipLayout.setVisibility(GONE);
        } else {
            productDetailsBinding.ml.membershipLayout.setVisibility(VISIBLE);
        }


        if (SPData.showAvailableFromToDate()) {
            productDetailsBinding.availableFrom.setVisibility(VISIBLE);
        } else
            productDetailsBinding.availableFrom.setVisibility(GONE);


        if (SPData.useCustomSliderHeightProductDetail())
            productDetailsBinding.sliderHeight.getLayoutParams().height = SPData.sliderHeight();


        if (SPData.showInclusivePriceText()) {
            productDetailsBinding.inclusiveText.setVisibility(VISIBLE);
        } else
            productDetailsBinding.inclusiveText.setVisibility(GONE);

        if (SPData.showRatingInProductDetail()) {
            productDetailsBinding.textRatings.setVisibility(VISIBLE);
            productDetailsBinding.productReviews.setVisibility(VISIBLE);
        } else {
            productDetailsBinding.textRatings.setVisibility(GONE);
            productDetailsBinding.productReviews.setVisibility(GONE);
        }
        if (SPData.showReturnableInProductDetail()) {
            productDetailsBinding.textReturnable.setVisibility(VISIBLE);
        } else {
            productDetailsBinding.textReturnable.setVisibility(GONE);
        }

        if (SPData.showGenuineProductLogo()) {
            productDetailsBinding.genuineLogo.setVisibility(VISIBLE);
        } else {
            productDetailsBinding.genuineLogo.setVisibility(GONE);
        }
    }

    private void askForConfirmation(String id, int minqty, boolean moveToCheckout) {
        String attributes = productDetailsBinding.attributes.getText().toString();
        String trimString = attributes.substring(0, attributes.length() - 2);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Is it what you selected");

        builder.setMessage("" + trimString);
        builder.setPositiveButton("Continue", (dialogInterface, i) -> {
            dialogInterface.cancel();
            addItem(id, minqty, moveToCheckout);
        });
        builder.setNegativeButton("Change", (dialogInterface, i) -> dialogInterface.cancel());
        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    @SuppressLint("SetTextI18n")
    public void promptDialog(String varientId, String name, String image, String attributes, double price, double sellingPrice) {
        View view = LayoutInflater.from(ProductDetailsActivity.this).inflate(R.layout.dialog_pickk_address, null);
        RecyclerView recyclerViewAddress = view.findViewById(R.id.recycler_view_address);
        TextView timeSlottext = view.findViewById(R.id.select_time_text);
        TextView addAddress = view.findViewById(R.id.add_address);
        TextView timeSlot = view.findViewById(R.id.time_slot);
        ImageView close = view.findViewById(R.id.close);
        timeSlottext.setText("Select address");
        timeSlot.setVisibility(GONE);
        addAddress.setOnClickListener(v -> startActivityForResult(new Intent(ProductDetailsActivity.this, AddAddressActivity.class).putExtra("title", "Add Address"), 1));
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ProductDetailsActivity.this);
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ProductDetailsActivity.this);
        alertDialog.setView(view);
        alertDialog1 = alertDialog.create();
        alertDialog1.setCanceledOnTouchOutside(false);
        if (userDataAddressArrayList == null) {
            userDataAddressArrayList = new ArrayList<>();
        }
        UserAddressesAdapter addressesAdapter = new UserAddressesAdapter(
                ProductDetailsActivity.this,
                userDataAddressArrayList,
                alertDialog1,
                varientId,
                name,
                image,
                attributes,
                price,
                sellingPrice
        );
        recyclerViewAddress.setLayoutManager(linearLayoutManager);
        recyclerViewAddress.setAdapter(addressesAdapter);
        addressesAdapter.notifyDataSetChanged();

        alertDialog1.show();

        close.setOnClickListener(v -> alertDialog1.dismiss());
    }

    private void getAllAddress() {
        try {
            ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());
            apiService.getUserBaseAddress().enqueue(new Callback<UserBaseAddress>() {
                @Override
                public void onResponse(@NonNull Call<UserBaseAddress> call, @NonNull Response<UserBaseAddress> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        userDataAddressArrayList = response.body().getData();
                        if (!SPData.getAppPreferences().getUsertoken().isEmpty() && !productDetailsBinding.attributes.getText().toString().isEmpty()) {
                            Log.e("TAG", "onCreate: " + productDetailsBinding.attributes.getText().toString());
                            String name = productDetailsBinding.productName.getText().toString();
                            String image = null;
                            if (images != null && images.size() > 0)
                                image = images.get(0);
                            String id = varient_id;
                            double mrp = 0.0;
                            if (productDetailsBinding.productMrp.getVisibility() == VISIBLE) {
                                mrp = Double.parseDouble(productDetailsBinding.productMrp.getText().toString().replace(getString(R.string.rs), ""));
                            }
                            double sellingPrice = 0.0;
                            if (productDetailsBinding.productPrice.getVisibility() == VISIBLE) {
                                sellingPrice = Double.parseDouble(productDetailsBinding.productPrice.getText().toString().replace(getString(R.string.rs), ""));
                            }
                            promptDialog(id, name, image, productDetailsBinding.attributes.getText().toString(), mrp, sellingPrice);
                        } else if (SPData.getAppPreferences().getUsertoken().isEmpty()) {
                            startActivity(new Intent(ProductDetailsActivity.this, LoginActivity.class));
                        } else {
                            CommonUtils.showToastMessageAtCenter(ProductDetailsActivity.this,"Selected varient not available");
                        }
                    } else {
                        Log.e("TAG", "onResponse: " + response);
                    }
                }

                @Override
                public void onFailure(@NotNull Call<UserBaseAddress> call, @NotNull Throwable t) {
                    Log.e("TAG", "onFailure: " + t.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e("TAG", "getAllAddress: " + e);
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void handleIncartChange() {
        intent.putExtra("incart", Integer.parseInt(productDetailsBinding.noOfQuantity.getText().toString()));
        intent.putExtra("id", varient_id);
        setResult(RESULT_OK, intent);
    }

    private void showReturnPolicy() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.return_policy_view, null);
        builder.setTitle("Return Policy");
        builder.setView(view);
        TextView textView = view.findViewById(R.id.return_policy);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            textView.setText(Html.fromHtml(returnPolicytxt, Html.FROM_HTML_MODE_COMPACT));
        else
            textView.setText(Html.fromHtml(returnPolicytxt));
        builder.setPositiveButton("Ok", (dialogInterface, i) -> dialogInterface.dismiss());
        builder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel());
        builder.create().show();
    }

    private void setRecommendedText() {

        LinearLayout linearLayout = findViewById(R.id.product_recommended);
        if (SPData.showGridView()) {
            ImageView iv = findViewById(R.id.product_recommended_image);
            TextView tv = findViewById(R.id.product_recommended_text);
            TextViewCompat.setAutoSizeTextTypeWithDefaults(tv, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);
            if (isRecommended) {
                linearLayout.setVisibility(VISIBLE);
                iv.setImageResource(R.drawable.check_orange);
                tv.setText(SPData.recommendedText());
                tv.setTextColor(getResources().getColor(R.color.colorOrange));
            } else if (SPData.showCertifiedText()) {
                linearLayout.setVisibility(VISIBLE);
                iv.setImageResource(R.drawable.check_blue);
                tv.setText(SPData.certifiedText());
                tv.setTextColor(getResources().getColor(R.color.colorInfo));
            } else {
                linearLayout.setVisibility(GONE);
            }
        } else {
            linearLayout.setVisibility(GONE);
        }
    }

    private void getAllOptionsVarientOfProduct() {


        optionsAttributeArrayList = new ArrayList<>();

        for (Attribute__ attribute : productData.getAttributes()) {
            ArrayList<String> option_value = new ArrayList<>();
            for (Option option : attribute.getOption()) {
                option_value.add(option.getValue());
            }
            optionsAttributeArrayList.add(new OptionsAttribute(attribute.getAttribute().getName(), option_value));
        }

        differentVarientsArrayList = new ArrayList<>();


        differentVarientsArrayList.addAll(productData.getVarients());

        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        productDetailsBinding.recyclerviewOptionValues.setLayoutManager(linearLayoutManager);
        productDetailsBinding.recyclerviewOptionValues.setHasFixedSize(true);
        OptionViewAdapter optionViewAdapter = new OptionViewAdapter(ProductDetailsActivity.this);
        productDetailsBinding.recyclerviewOptionValues.setAdapter(optionViewAdapter);
        optionViewAdapter.notifyDataSetChanged();

    }

    private void setImagesData(Varient varient) {
        Images varientsImages = varient.getImages();
        images.clear();
        if (varientsImages != null) {
            if (CommonUtils.stringIsNotNullAndEmpty(varientsImages.getPrimary())) {
                images.add(varientsImages.getPrimary());
            }
            if (CommonUtils.stringIsNotNullAndEmpty(varientsImages.getSecondary())) {
                images.add(varientsImages.getSecondary());
            }
            if (CommonUtils.stringIsNotNullAndEmpty(varientsImages.getImage1())) {
                images.add(varientsImages.getImage1());
            }
            if (CommonUtils.stringIsNotNullAndEmpty(varientsImages.getImage2())) {
                images.add(varientsImages.getImage2());
            }
            if (CommonUtils.stringIsNotNullAndEmpty(varientsImages.getImage3())) {
                images.add(varientsImages.getImage3());
            }
        }

        Images imagesData = productData.getProduct().getImages();
        if (imagesData != null) {
            if (CommonUtils.stringIsNotNullAndEmpty(imagesData.getPrimary())) {
                images.add(imagesData.getPrimary());
            }
            if (CommonUtils.stringIsNotNullAndEmpty(imagesData.getSecondary())) {
                images.add(imagesData.getSecondary());
            }
            if (CommonUtils.stringIsNotNullAndEmpty(imagesData.getImage1())) {
                images.add(imagesData.getImage1());
            }
            if (CommonUtils.stringIsNotNullAndEmpty(imagesData.getImage2())) {
                images.add(imagesData.getImage2());
            }
            if (CommonUtils.stringIsNotNullAndEmpty(imagesData.getImage3())) {
                images.add(imagesData.getImage3());
            }
        }

        if (productData.getProduct().getVideo() != null) {
            images.add(productData.getProduct().getVideo());
        }
        sliderAdapterExample = new SliderAdapterExample(this, images);
        productDetailsBinding.showSalonImageSlider.setSliderAdapter(sliderAdapterExample);
        productDetailsBinding.showSalonImageSlider.setAutoCycle(false);
        productDetailsBinding.showSalonImageSlider.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
    }

    @SuppressLint("SetTextI18n")
    private void setPriceData(Varient varient) {
        double sellingPrice, mrp;

        if (SPData.useWholesalePricingInProducts() && getSellingPriceByQuantity(varient) > 0) {
            sellingPrice = getSellingPriceByQuantity(varient);
        } else {
            sellingPrice = varient.getSellingPrice();
        }
        mrp = varient.getPrice();


        if (sellingPrice > 0.0 && sellingPrice != mrp) {
            double save;
            if (SPData.getAppPreferences().isMembershipPurchased() && varient.getMemberPrice() > 0 && !(SPData.useWholesalePricingInProducts() && getSellingPriceByQuantity(varient) > 0)) {
                save = mrp - varient.getMemberPrice();
            } else
                save = mrp - sellingPrice;
            long discount = Math.round((((save) / mrp) * 100));
            productDetailsBinding.productMrp.setVisibility(VISIBLE);
            productDetailsBinding.productDiscount.setVisibility(VISIBLE);
            productDetailsBinding.productPrice.setText(getString(R.string.rs) + CommonUtils.getTwoDigitsRoundOffValue(sellingPrice));
            productDetailsBinding.productMrp.setText(getString(R.string.rs) + CommonUtils.getTwoDigitsRoundOffValue(mrp));
            productDetailsBinding.productDiscount.setText(discount + "% off");
            if (SPData.showYouSaveAmount()) {
                productDetailsBinding.youSave.setVisibility(VISIBLE);
                if (SPData.useGreenYouSaveColor())
                    productDetailsBinding.youSave.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                productDetailsBinding.youSave.setText("You save " + getString(R.string.rs) + CommonUtils.getTwoDigitsRoundOffValue(save));
            } else {
                productDetailsBinding.youSave.setVisibility(GONE);
            }
        } else {
            productDetailsBinding.productPrice.setText(getString(R.string.rs) + CommonUtils.getTwoDigitsRoundOffValue(mrp));
            productDetailsBinding.productMrp.setVisibility(GONE);
            productDetailsBinding.productDiscount.setVisibility(GONE);
            productDetailsBinding.youSave.setVisibility(GONE);
        }
    }

    private double getSellingPriceByQuantity(Varient varient) {
        if (varient.getWholeSalePrices() != null && varient.getWholeSalePrices().size() > 0) {
            ArrayList<WholeSalePriceModel> wholeSalePriceModels = varient.getWholeSalePrices();
            Collections.sort(wholeSalePriceModels, (o1, o2) -> (int) (o1.getPricePerUnit() - o2.getPricePerUnit()));
            return wholeSalePriceModels.get(0).getPricePerUnit();
        } else {
            return 0;
        }
    }

    private void getRealtedProducts() {
        try {
            productDetailsBinding.relatedProducts.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));
            productDetailsBinding.relatedProducts.setHasFixedSize(true);
            productDetailsBinding.shimmerRelatedProducts.setVisibility(VISIBLE);
            productDetailsBinding.shimmerRelatedProducts.startShimmerAnimation();
            apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());
            apiService.getAllProductsVarientsByPage(category_id, page, SPData.getAppPreferences().getPincode(), "", "").enqueue(new Callback<PageVarientsByCategory>() {
                @Override
                public void onResponse(@NotNull Call<PageVarientsByCategory> call, @NotNull Response<PageVarientsByCategory> response) {
                    Log.e("TAG", "onResponse: " + response);
                    showRelatedProductsFromResponse(response);
                }

                @Override
                public void onFailure(@NotNull Call<PageVarientsByCategory> call, @NotNull Throwable t) {
                    handleNoProducts();
                }
            });
        } catch (Exception e) {
            Log.e("TAG", "getProductsVarients: " + e);
        }
    }

    private void showRelatedProductsFromResponse(Response<PageVarientsByCategory> response) {
        try {
            PageVarientsByCategory productVarients = response.body();
            if (response.isSuccessful() && productVarients != null && productVarients.getData() != null && productVarients.getData().getRecords().size() > 0) {
                List<com.mindfulai.Models.varientsByCategory.Datum> varientList = productVarients.getData().getRecords();
                totalPages = (int) Math.ceil(productVarients.getData().getTotalCount() / productVarients.getData().getLimit());
                productDetailsBinding.shimmerRelatedProducts.stopShimmerAnimation();
                productDetailsBinding.shimmerRelatedProducts.setVisibility(GONE);
                productDetailsBinding.relatedProducts.setVisibility(VISIBLE);
                productDetailsBinding.relatedProductsTxt.setVisibility(VISIBLE);
                List<com.mindfulai.Models.varientsByCategory.Datum> filterList = new ArrayList<>();
                for (com.mindfulai.Models.varientsByCategory.Datum datum : varientList) {
                    if (!datum.getProduct().getId().equals(productId)) {
                        filterList.add(datum);
                    }
                }
                if (filterList.size() == 0) {
                    handleNoProducts();
                } else {
                    ProductsAdapter productAdapter = new ProductsAdapter(ProductDetailsActivity.this, filterList, "grid", PROUDUCT_REQUEST_CODE);
                    productDetailsBinding.relatedProducts.setAdapter(productAdapter);
                    productAdapter.notifyDataSetChanged();
                }
            } else {
                handleNoProducts();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("TAG", "showProductsFromResponse: " + e);
            handleNoProducts();
        }
    }

    private void handleNoProducts() {
        productDetailsBinding.shimmerRelatedProducts.stopShimmerAnimation();
        productDetailsBinding.shimmerRelatedProducts.setVisibility(GONE);
        productDetailsBinding.relatedProducts.setVisibility(GONE);
        productDetailsBinding.relatedProductsTxt.setVisibility(GONE);
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.cart_notification);
        cartBadge = item.getActionView().findViewById(R.id.notification_badge);
        item.setVisible(SPData.showcartIconAddToCart() && SPData.showProductsAndCart());
        MenuItem notificationItem = menu.findItem(R.id.notification_item);
        notificationItem.setVisible(false);

        int total = SPData.getAppPreferences().getTotalCartCount();
        cartBadge.setBadgeValue(total);
        cartBadge.setOnClickListener(v -> startActivityForResult(new Intent(ProductDetailsActivity.this, CartPageActivity.class), 10));
        MenuItem sort = menu.findItem(R.id.sort);
        MenuItem list = menu.findItem(R.id.navigation_list_view);
        MenuItem grid = menu.findItem(R.id.navigation_grid_view);
        MenuItem search = menu.findItem(R.id.search);
        sort.setVisible(false);
        list.setVisible(false);
        grid.setVisible(false);
        search.setVisible(false);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.browse_menu, menu);
        return true;
    }

    public void addBadge(String count) {
        cartBadge.setBadgeValue(Integer.parseInt(count));
    }

    public void removeBadge() {
        cartBadge.setBadgeValue(0);
    }

    private void removeProductFromList(String id) {
        if (!SPData.getAppPreferences().getUsertoken().isEmpty()) {
            ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());
            apiService.removeItemFromCart(id).enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(@NotNull Call<JsonObject> call, @NotNull Response<JsonObject> response) {
                    handleRemoveResponse(call, response);
                }

                @Override
                public void onFailure(@NotNull Call<JsonObject> call, @NotNull Throwable t) {
                    CommonUtils.showToastMessageAtCenter(ProductDetailsActivity.this,"Failed to connect");
                }
            });
        } else {
            ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUserUniqueId());
            apiService.removeItemFromCartWithoutLogin(id).enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(@NotNull Call<JsonObject> call, @NotNull Response<JsonObject> response) {
                    handleRemoveResponse(call, response);
                }

                @Override
                public void onFailure(@NotNull Call<JsonObject> call, @NotNull Throwable t) {
                    CommonUtils.showToastMessageAtCenter(ProductDetailsActivity.this,"Failed to connect");
                }
            });
        }
    }


    private void checkItemToAdded() {
        int total_stock = stock;
        int currentQty = Integer.parseInt(productDetailsBinding.noOfQuantity.getText().toString());
        Log.e("TAG", "checkItemToAdded:maxQty " + maxQty);
        if (currentQty < total_stock) {
            if (maxQty == 0 || currentQty < maxQty)
                updateItem(varient_id, currentQty + 1);
            else
                CommonUtils.showToastMessageAtCenter(ProductDetailsActivity.this,""+"Maximum order quantity is " + maxQty);
        } else {
            CommonUtils.showToastMessageAtCenter(ProductDetailsActivity.this,""+"There is only " + total_stock + " stock left");
        }
    }

    private void updateItem(String id, final int quantity) {
        try {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("quantity", quantity);
            if (!SPData.getAppPreferences().getUsertoken().isEmpty()) {
                ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());
                apiService.updateCartItem(id, jsonObject).enqueue(new Callback<CartDetailsInformation>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onResponse(@NotNull Call<CartDetailsInformation> call, @NotNull Response<CartDetailsInformation> response) {
                        handleUpdateResponse(call, response, quantity);
                    }

                    @Override
                    public void onFailure(@NotNull Call<CartDetailsInformation> call, @NotNull Throwable t) {
                        Log.e("fail", call.toString());
                        CommonUtils.showToastMessageAtCenter(ProductDetailsActivity.this,"Failed to connect");

                    }
                });
            } else {
                ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUserUniqueId());
                apiService.updateCartItemWithoutLogin(id, jsonObject).enqueue(new Callback<CartDetailsInformation>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onResponse(@NotNull Call<CartDetailsInformation> call, @NotNull Response<CartDetailsInformation> response) {
                        handleUpdateResponse(call, response, quantity);
                    }

                    @Override
                    public void onFailure(@NotNull Call<CartDetailsInformation> call, @NotNull Throwable t) {
                        Log.e("fail", call.toString());
                        CommonUtils.showToastMessageAtCenter(ProductDetailsActivity.this,"Failed to connect");

                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "updateItem: " + e);
        }
    }

    private void addItemToWishList(String id) {
        try {
            final CustomProgressDialog customProgressDialog = CommonUtils.showProgressDialog(ProductDetailsActivity.this,
                    "Adding to wishlist ... ");
            ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("product", id);

            apiService.addItemToWishlist(jsonObject).enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                    CommonUtils.hideProgressDialog(customProgressDialog);
                    if (response.isSuccessful()) {
                        String reponse_status = String.valueOf(response.body().get("status"));
                        if (reponse_status.matches("200")) {
                            CommonUtils.showToastMessageAtCenter(ProductDetailsActivity.this,"Item added to wishlist !!");
                        }
                    } else {
                        CommonUtils.showToastMessageAtCenter(ProductDetailsActivity.this,""+response.code() + " " + response.message());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                    Log.e("fail", call.toString());
                    CommonUtils.hideProgressDialog(customProgressDialog);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "addItemToWishList: " + e);
        }
    }

    private void removeItemFromWishList(String id) {
        try {
            final CustomProgressDialog customProgressDialog = CommonUtils.showProgressDialog(ProductDetailsActivity.this,
                    "Removing from wishlist ... ");
            ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());

            apiService.removeItemFromWishlist(id).enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                    CommonUtils.hideProgressDialog(customProgressDialog);
                    if (response.isSuccessful()) {
                        String reponse_status = String.valueOf(response.body().get("status"));
                        if (reponse_status.matches("200")) {
                            CommonUtils.showToastMessageAtCenter(ProductDetailsActivity.this,"Item removed from wishlist!!");
                        }
                    } else {
                        CommonUtils.hideProgressDialog(customProgressDialog);
                        CommonUtils.showToastMessageAtCenter(ProductDetailsActivity.this,""+response.code() + " " + response.message());
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    private void getReview() {
        reviewList.clear();
        ApiService apiService = ApiUtils.getHeaderAPIService();
        apiService.getReview(productId).enqueue(new Callback<ReviewData>() {
            @Override
            public void onResponse(@NonNull Call<ReviewData> call, @NonNull Response<ReviewData> response) {
                Log.e("TAG", "getReview onResponse: " + response);
                if (response.isSuccessful()) {
                    ReviewData reviewData = response.body();
                    assert reviewData != null;

                    reviewList = reviewData.getData();
                    Log.e("TAG", "onResponse: " + reviewList.size());
                    if (reviewList.size() > 0) {
                        LinearLayoutManager verticalLayoutManager = new LinearLayoutManager(ProductDetailsActivity.this, LinearLayoutManager.VERTICAL, true);
                        productDetailsBinding.rvReviews.setLayoutManager(verticalLayoutManager);
                        productDetailsBinding.linearLayoutReviewLabel.setVisibility(VISIBLE);
                        productDetailsBinding.rvReviews.setVisibility(VISIBLE);
                        reviewsAdapter = new ReviewsAdapter(ProductDetailsActivity.this, reviewList);
                        productDetailsBinding.rvReviews.setAdapter(reviewsAdapter);
                        reviewsAdapter.notifyDataSetChanged();
                    } else {
                        productDetailsBinding.linearLayoutReviewLabel.setVisibility(GONE);
                    }
                }

            }

            @Override
            public void onFailure(@NotNull Call<ReviewData> call, @NotNull Throwable t) {
                Log.e("fail", call.toString());
                CommonUtils.showToastMessageAtCenter(ProductDetailsActivity.this,"Failed to connect");
            }
        });
    }

    @SuppressLint("SetTextI18n")
    public void hideAddToCart(int incart) {
        if (!SPData.showViewCart()) {
            productDetailsBinding.addToCart.setVisibility(GONE);
            productDetailsBinding.linearLayoutPlusMinus.setVisibility(VISIBLE);
            productDetailsBinding.noOfQuantity.setText("" + incart);
        } else {
            productDetailsBinding.addToCart.setVisibility(VISIBLE);
            productDetailsBinding.addToCart.setText(SPData.getViewCartTxt());
        }
    }

    private void addItem(String id, final int qty, boolean moveToCheckout) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("product", id);
        jsonObject.addProperty("quantity", qty);
        if (!SPData.getAppPreferences().getUsertoken().isEmpty()) {
            ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());
            apiService.addItemToCart(jsonObject).enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                    handleAddToCartResponse(response, id, qty, moveToCheckout);
                }

                @Override
                public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                    Log.e("fail", call.toString());
                }
            });
        } else {
            ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUserUniqueId());
            apiService.addItemToCartWithoutLogin(jsonObject).enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                    handleAddToCartResponse(response, id, qty, moveToCheckout);
                }

                @Override
                public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                    Log.e("fail", call.toString());
                }
            });
        }
    }

    private void handleAddToCartResponse(Response<JsonObject> response, String id, int qty, boolean moveToCheckout) {
        if (response.isSuccessful() && response.body() != null) {
            selectedVarient.setInCart(qty);
            int cartItem = SPData.getAppPreferences().getTotalCartCount() + qty;
            SPData.getAppPreferences().setTotalCartCount(cartItem);
            Vendor createdBy = productData.getProduct().getCreatedBy();
            if (createdBy != null)
                SPData.getAppPreferences().setCartVendorId(createdBy.getId());
            addBadge("" + cartItem);
            hideAddToCart(qty);
            handleIncartChange();
            CommonUtils.showToastMessageAtCenter(ProductDetailsActivity.this,"Item added to cart !!");
        } else {
            CommonUtils.showToastMessageAtCenter(ProductDetailsActivity.this,"" + response.message());
        }
    }

    private void handleUpdateResponse(Call<CartDetailsInformation> call, Response<CartDetailsInformation> response, int quantity) {
        if (response.isSuccessful() && response.body() != null) {
            productDetailsBinding.noOfQuantity.setText("" + quantity);
            selectedVarient.setInCart(quantity);
            handleIncartChange();
            CommonUtils.showToastMessageAtCenter(ProductDetailsActivity.this,"Quantity Updated!!");
        } else {
            CommonUtils.showToast(ProductDetailsActivity.this,response.code() + " " + response.message());
        }
    }

    private void handleRemoveResponse(Call<JsonObject> call, Response<JsonObject> response) {
        try {
            if (response.isSuccessful() && response.body() != null) {
                selectedVarient.setInCart(0);
                showAddtoCart();
                handleIncartChange();
                if (SPData.getAppPreferences().getTotalCartCount() > 0) {
                    int total_cart = SPData.getAppPreferences().getTotalCartCount() - 1;
                    if (total_cart > 0) {
                        SPData.getAppPreferences().setTotalCartCount(total_cart);
                        addBadge("" + total_cart);
                    } else {
                        SPData.getAppPreferences().setTotalCartCount(0);
                        removeBadge();
                    }
                }
                CommonUtils.showToastMessageAtCenter(ProductDetailsActivity.this,"Removed from cart !!");
            } else {
                CommonUtils.showToastMessageAtCenter(ProductDetailsActivity.this,""+response.code() + " " + response.message());
            }
        } catch (Exception e) {
            Log.e(TAG, "onResponse: " + e);
        }
    }

    private void getReturnPolicy() {
        apiService.getReturnPolicy().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NotNull Call<JsonObject> call, @NotNull Response<JsonObject> response) {
                try {
                    if (response.isSuccessful() && response.body() != null && response.body().get("data") != null) {
                        returnPolicytxt = response.body().get("data").getAsJsonObject().get("paragraph").getAsString();
                    }
                } catch (Exception e) {
                    Log.e("TAG", "onResponse: " + e);
                }
            }

            @Override
            public void onFailure(@NotNull Call<JsonObject> call, @NotNull Throwable t) {

            }
        });
    }
}