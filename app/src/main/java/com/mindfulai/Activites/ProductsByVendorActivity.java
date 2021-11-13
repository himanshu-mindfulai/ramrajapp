package com.mindfulai.Activites;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.headerfooter.songhang.library.SmartRecyclerAdapter;
import com.mindfulai.Adapter.ProductsAdapter;
import com.mindfulai.Adapter.VendorCategoriesProductsAdapter;
import com.mindfulai.Adapter.VendorCategoryAdapter;
import com.mindfulai.Adapter.VendorGallerySliderAdapter;
import com.mindfulai.Models.BannerInfoData.BannerData;
import com.mindfulai.Models.BannerInfoData.BannerInfo;
import com.mindfulai.Models.CartInformation.CartDetailsInformation;
import com.mindfulai.Models.VendorBase;
import com.mindfulai.Models.VendorChild;
import com.mindfulai.Models.VendorProductsByCategory;
import com.mindfulai.Models.categoryData.CategoryInfo;
import com.mindfulai.Models.varientsByCategory.Datum;
import com.mindfulai.Models.varientsByCategory.PageVarientsByCategory;
import com.mindfulai.Models.varientsByCategory.VarientsByCategory;
import com.mindfulai.NetworkRetrofit.ApiService;
import com.mindfulai.NetworkRetrofit.ApiUtils;
import com.mindfulai.Utils.CommonUtils;
import com.mindfulai.Utils.SPData;
import com.mindfulai.ministore.R;
import com.mindfulai.ministore.databinding.ActivityProductsByVendorBinding;
import com.mindfulai.ministore.databinding.ProductsByvendorHeaderLayout2Binding;
import com.mindfulai.ministore.databinding.ProductsByvendorHeaderLayoutBinding;
import com.smarteist.autoimageslider.SliderAnimations;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class ProductsByVendorActivity extends AppCompatActivity {

    ActivityProductsByVendorBinding binding;
    public int totalPages = 1;
    public int currentPage = 1;
    public List<Datum> varientList = new ArrayList<>();
    public String vendorId;
    public String categoryId = "";
    private boolean mHasReachedBottomOnce;
    private float x1;
    static final int MIN_DISTANCE = 150;
    public int currentCategoryIndex = -1;
    private final List<com.mindfulai.Models.categoryData.Datum> categoryList = new ArrayList<>();
    private VendorCategoryAdapter vendorCategoryAdapter;
    private Uri deepLink;
    private ProductsByvendorHeaderLayout2Binding headerLayoutBinding;
    private SmartRecyclerAdapter smartRecyclerAdapter;
    private VendorChild vendor;
    private ApiService apiService;

    @SuppressLint("InflateParams")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater layoutInflater = getLayoutInflater();
        layoutInflater.inflate(R.layout.activity_products_by_vendor, null);
        binding = ActivityProductsByVendorBinding.inflate(layoutInflater);
        setContentView(binding.getRoot());
        Intent intent = getIntent();
        vendorId = intent.getStringExtra("vendor_id");
        apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());

        LayoutInflater layoutInflaterHeader = LayoutInflater.from(this);
        layoutInflaterHeader.inflate(R.layout.products_byvendor_header_layout, null);
        headerLayoutBinding = ProductsByvendorHeaderLayout2Binding.inflate(layoutInflaterHeader);

        setLayoutManager();

        handleOnClick();

        if (!SPData.showVendorProductsUnderCategory())
            handleProductScroll();

        if (vendorId != null) {
            getVendorById(vendorId);
        } else
            getVendorByReferral();
    }

    private void getVendorByReferral() {
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, pendingDynamicLinkData -> {
                    if (pendingDynamicLinkData != null && pendingDynamicLinkData.getLink() != null) {
                        deepLink = pendingDynamicLinkData.getLink();
                        vendorId = deepLink.getQueryParameter("vendor");
                        getVendorById(vendorId);
                    } else {
                        Log.e("TAG", "onSuccess: " + deepLink);
                        handleError();
                    }
                }).addOnFailureListener(e -> {
            Log.e("TAG", "onFailure: " + e.getMessage());
            handleError();
        });
    }

    private void getVendorById(String vendorId) {
        apiService.getVendorProfile(vendorId).enqueue(new Callback<VendorBase>() {
            @Override
            public void onResponse(@NotNull Call<VendorBase> call, @NotNull Response<VendorBase> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData().size() > 0) {
                    vendor = response.body().getData().get(0);
                    setVendorDetail();
                } else
                    handleError();
            }

            @Override
            public void onFailure(@NotNull Call<VendorBase> call, @NotNull Throwable t) {
                handleError();
            }
        });
    }

    private void handleError() {
        binding.parentlinearLayout.setVisibility(GONE);
        binding.noVendorLayout.setVisibility(VISIBLE);
    }

    private void setVendorDetail() {
        headerLayoutBinding.vendorName.setText(CommonUtils.capitalizeWord(vendor.getFull_name()));

        if (CommonUtils.stringIsNotNullAndEmpty(vendor.getAddress())&&SPData.showVendorAddress())
            headerLayoutBinding.vendorDetails.setText(vendor.getAddress());
        else
            headerLayoutBinding.vendorDetails.setVisibility(GONE);
        if (CommonUtils.stringIsNotNullAndEmpty(vendor.getMobile_number()) && SPData.showCallImagesAndShareOptionOfVendor()) {
            headerLayoutBinding.call.setVisibility(VISIBLE);
        } else
            headerLayoutBinding.call.setVisibility(GONE);
        if (SPData.showCallImagesAndShareOptionOfVendor()) {
            headerLayoutBinding.share.setVisibility(VISIBLE);
        } else
            headerLayoutBinding.share.setVisibility(GONE);

        if (SPData.showCallImagesAndShareOptionOfVendor())
            getVendorGallery();
        else
            headerLayoutBinding.gallerySlider.setVisibility(GONE);

         getCategoryByVendor();

        if (!SPData.showVendorProductsUnderCategory())
            getProductByVendor();

    }

    private void setLayoutManager() {
        LinearLayoutManager linearLayoutManager;
        if (SPData.showVendorProductsUnderCategory()) {
            linearLayoutManager = new LinearLayoutManager(this);
        } else {
            linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        }
        headerLayoutBinding.categoriesRecyclerview.setLayoutManager(linearLayoutManager);

        LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(this);
        binding.productsRecyclerView.setLayoutManager(linearLayoutManager1);
        ProductsAdapter productAdapter = new ProductsAdapter(ProductsByVendorActivity.this, varientList, "list", CommonUtils.PRODUCTS_BY_VENDOR_REQUEST_CODE);
        smartRecyclerAdapter = new SmartRecyclerAdapter(productAdapter);
        smartRecyclerAdapter.setHeaderView(headerLayoutBinding.getRoot());
        binding.productsRecyclerView.setAdapter(smartRecyclerAdapter);
    }

    private void handleProductScroll() {
        binding.productsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!recyclerView.canScrollVertically(1) && !mHasReachedBottomOnce) {
                    mHasReachedBottomOnce = true;
                    if (currentPage <= totalPages) {
                        ApiService apiService = ApiUtils.getAPIService();
                        binding.progressBar.setVisibility(VISIBLE);
                        if (!categoryId.isEmpty())
                            apiService.getAllProductsVarientsByPage(categoryId, currentPage, SPData.getAppPreferences().getPincode(), vendorId, "").enqueue(new Callback<PageVarientsByCategory>() {
                                @Override
                                public void onResponse(@NonNull Call<PageVarientsByCategory> call, @NonNull Response<PageVarientsByCategory> response) {
                                    showProductsFromResponse(response);
                                }

                                @Override
                                public void onFailure(@NonNull Call<PageVarientsByCategory> call, @NonNull Throwable t) {
                                    handleNoProducts();
                                }
                            });
                        else {
                            apiService.getAllProductsVarientsByVendorByPage(vendorId, currentPage, SPData.getAppPreferences().getPincode()).enqueue(new Callback<PageVarientsByCategory>() {
                                @Override
                                public void onResponse(@NonNull Call<PageVarientsByCategory> call, @NonNull Response<PageVarientsByCategory> response) {
                                    showProductsFromResponse(response);
                                }

                                @Override
                                public void onFailure(@NonNull Call<PageVarientsByCategory> call, @NonNull Throwable t) {
                                    handleNoProducts();
                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }

    private void handleOnClick() {
        binding.bottomCart.cartView.setOnClickListener(view -> startActivityForResult(new Intent(ProductsByVendorActivity.this, CartPageActivity.class), 10));
        headerLayoutBinding.call.setOnClickListener(v -> CommonUtils.sendToPhone(ProductsByVendorActivity.this, vendor.getMobile_number()));
        headerLayoutBinding.share.setOnClickListener(v -> {
            try {
                DynamicLink dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
                        .setLink(Uri.parse(getString(R.string.linkUrl) + "vendor=" + vendorId))
                        .setDomainUriPrefix("https://" + getString(R.string.vendor_profile))
                        .setAndroidParameters(new DynamicLink.AndroidParameters.Builder(getPackageName()).build())
                        .setIosParameters(new DynamicLink.IosParameters.Builder("com.example.ios").build())
                        .buildDynamicLink();
                Uri dynamicLinkUri = dynamicLink.getUri();
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, "" + getString(R.string.app_name));
                intent.putExtra(Intent.EXTRA_TEXT, SPData.vendorShareMsg() + "\n\n" + vendor.getFull_name() + "\n" + dynamicLinkUri.toString());
                startActivity(Intent.createChooser(intent, "Share"));
            } catch (Exception e) {
                Log.e("TAG", "setOnClick: " + e);
            }
        });
    }

    private void getVendorGallery() {
        ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());
        apiService.getVendorGallery(vendorId).enqueue(new Callback<BannerData>() {
            @Override
            public void onResponse(@NotNull Call<BannerData> call, @NotNull Response<BannerData> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData().size() > 0) {
                    headerLayoutBinding.gallerySlider.setVisibility(VISIBLE);
                    List<BannerInfo> images = response.body().getData();
                    headerLayoutBinding.gallerySlider.setSliderAdapter(new VendorGallerySliderAdapter(ProductsByVendorActivity.this, images));
                    headerLayoutBinding.gallerySlider.startAutoCycle();
                    headerLayoutBinding.gallerySlider.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
                } else
                    headerLayoutBinding.gallerySlider.setVisibility(GONE);
            }

            @Override
            public void onFailure(@NotNull Call<BannerData> call, @NotNull Throwable t) {
                headerLayoutBinding.gallerySlider.setVisibility(GONE);
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (SPData.useTabViewPagerSubcategory() && !SPData.showVendorProductsUnderCategory()) {
            switch (ev.getAction()) {

                case MotionEvent.ACTION_DOWN:
                    x1 = ev.getX();
                    break;
                case MotionEvent.ACTION_UP:
                    float x2 = ev.getX();
                    float deltaX = x2 - x1;
                    if (Math.abs(deltaX) > MIN_DISTANCE && ev.getY() > 1000) {
                        // Left to Right swipe action

                        if (x2 > x1) {
                            if (currentCategoryIndex > 0)
                                currentCategoryIndex--;
                        }
                        // Right to left swipe action
                        else {
                            if (currentCategoryIndex < categoryList.size() - 1)
                                currentCategoryIndex++;
                        }
                        if (currentCategoryIndex >= 0 && currentCategoryIndex < categoryList.size()) {
                            headerLayoutBinding.categoriesRecyclerview.smoothScrollToPosition(currentCategoryIndex);
                            vendorCategoryAdapter.checkCategory(currentCategoryIndex);
                        }
                    } else {
                        // consider as something else - a screen tap for example
                    }
                    break;
            }
        }

        return super.dispatchTouchEvent(ev);

    }

    @Override
    protected void onResume() {
        super.onResume();
        getAllCart();
    }

    public void getAllCart() {
        try {
            if (!SPData.getAppPreferences().getUsertoken().isEmpty()) {
                ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());
                if (SPData.showShippingMethods())
                    apiService.showCartItems(SPData.getAppPreferences().getPincode()).enqueue(new Callback<CartDetailsInformation>() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onResponse(@NotNull Call<CartDetailsInformation> call, @NotNull Response<CartDetailsInformation> response) {
                            checkResponseFromCart(response);
                        }

                        @Override
                        public void onFailure(@NonNull Call<CartDetailsInformation> call, @NonNull Throwable t) {
                            handleNoCartItem();
                        }
                    });
                else {
                    apiService.showCartItems().enqueue(new Callback<CartDetailsInformation>() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onResponse(@NotNull Call<CartDetailsInformation> call, @NotNull Response<CartDetailsInformation> response) {
                            checkResponseFromCart(response);
                        }

                        @Override
                        public void onFailure(@NonNull Call<CartDetailsInformation> call, @NonNull Throwable t) {
                            handleNoCartItem();
                        }
                    });
                }
            } else {
                ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUserUniqueId());
                if (SPData.showShippingMethods())
                    apiService.showCartItemsWithoutLogin(SPData.getAppPreferences().getPincode()).enqueue(new Callback<CartDetailsInformation>() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onResponse(@NotNull Call<CartDetailsInformation> call, @NotNull Response<CartDetailsInformation> response) {
                            checkResponseFromCart(response);
                        }

                        @Override
                        public void onFailure(@NonNull Call<CartDetailsInformation> call, @NonNull Throwable t) {
                            handleNoCartItem();
                        }
                    });
                else
                    apiService.showCartItemsWithoutLogin().enqueue(new Callback<CartDetailsInformation>() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onResponse(@NotNull Call<CartDetailsInformation> call, @NotNull Response<CartDetailsInformation> response) {
                            checkResponseFromCart(response);
                        }

                        @Override
                        public void onFailure(@NonNull Call<CartDetailsInformation> call, @NonNull Throwable t) {
                            handleNoCartItem();
                        }
                    });
            }
        } catch (Exception e) {
            Log.e("TAG", "getAllCart: " + e);
            Toast.makeText(this, "" + e, Toast.LENGTH_SHORT).show();
            handleNoCartItem();
        }
    }

    private void checkResponseFromCart(Response<CartDetailsInformation> response) {
        if (response.isSuccessful() && response.body() != null) {
            setCartAmount(response.body());
        } else {
            handleNoCartItem();
        }
    }

    private void handleNoCartItem() {
        binding.bottomCart.cartItem.setVisibility(GONE);
    }

    @SuppressLint("SetTextI18n")
    public void setCartAmount(CartDetailsInformation cartDetailsInformation) {
        float totalAmt = cartDetailsInformation.getData().getTotal();
        if (totalAmt > 0) {
            int qty = cartDetailsInformation.getData().getProducts().size();
            binding.bottomCart.cartItem.setVisibility(VISIBLE);
            binding.bottomCart.cartTotal.setText(getString(R.string.rs) + totalAmt);
            binding.bottomCart.cartQty.setText(qty + " item");
        } else {
            handleNoCartItem();
        }
    }

    private void getCategoryByVendor() {
        ApiService apiService = ApiUtils.getAPIService();
        apiService.getAllCategoryByVendor(vendorId).enqueue(new Callback<CategoryInfo>() {
            @Override
            public void onResponse(@NotNull Call<CategoryInfo> call, @NotNull Response<CategoryInfo> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categoryList.clear();
                    categoryList.addAll(response.body().getData());
                    if (categoryList.size() > 0) {
                        if (SPData.showVendorProductsUnderCategory()) {
                            getAllProducts();
                        } else {
                            headerLayoutBinding.categoriesRecyclerview.setVisibility(VISIBLE);
                            vendorCategoryAdapter = new VendorCategoryAdapter(ProductsByVendorActivity.this, categoryList, headerLayoutBinding.categoriesRecyclerview);
                            headerLayoutBinding.categoriesRecyclerview.setAdapter(vendorCategoryAdapter);
                            vendorCategoryAdapter.notifyDataSetChanged();
                        }
                    } else {
                        headerLayoutBinding.categoriesRecyclerview.setVisibility(GONE);
                    }
                }
            }

            @Override
            public void onFailure(@NotNull Call<CategoryInfo> call, @NotNull Throwable t) {
                handleNoProducts();
            }
        });
    }

    private void getAllProducts() {
        apiService.getAllProductsVariantsByVendorWithoutPage(vendorId, SPData.getAppPreferences().getPincode()).enqueue(new Callback<VarientsByCategory>() {
            @Override
            public void onResponse(@NotNull Call<VarientsByCategory> call, @NotNull Response<VarientsByCategory> response) {
                Log.e("TAG", "getAllProducts onResponse: " + response);
                binding.progressBar.setVisibility(GONE);
                HashMap<String, ArrayList<Datum>> hashMap = new HashMap<>();
                if (response.isSuccessful() && response.body() != null && response.body().getData().size() > 0) {
                    for (Datum product : response.body().getData()) {
                        if (!hashMap.containsKey(product.getProduct().getCategory().getName())) {
                            ArrayList<Datum> list = new ArrayList<>();
                            list.add(product);
                            hashMap.put(product.getProduct().getCategory().getName(), list);
                        } else {
                            ArrayList<Datum> productList = hashMap.get(product.getProduct().getCategory().getName());
                            if (productList != null) {
                                productList.add(product);
                            }
                        }
                    }
                    Iterator it = hashMap.entrySet().iterator();
                    ArrayList<VendorProductsByCategory> vendorProductsByCategoryArrayList = new ArrayList<>();
                    while (it.hasNext()) {
                        Map.Entry pair = (Map.Entry) it.next();
                        vendorProductsByCategoryArrayList.add(new VendorProductsByCategory((String) pair.getKey(), (ArrayList<Datum>) pair.getValue()));
                    }
                    VendorCategoriesProductsAdapter vendorProductsByCategory = new VendorCategoriesProductsAdapter(ProductsByVendorActivity.this, vendorProductsByCategoryArrayList);
                    headerLayoutBinding.categoriesRecyclerview.setAdapter(vendorProductsByCategory);
                }else{
                    handleNoProducts();
                }

            }

            @Override
            public void onFailure(@NotNull Call<VarientsByCategory> call, @NotNull Throwable t) {
                handleNoProducts();
            }
        });
    }

    public void getProductByCategoryAndVendor() {
        varientList.clear();
        totalPages = 1;
        currentPage = 1;
        smartRecyclerAdapter.getWrappedAdapter().notifyDataSetChanged();
        apiService.getAllProductsVarientsByPage(categoryId, currentPage, SPData.getAppPreferences().getPincode(), vendorId, "").enqueue(new Callback<PageVarientsByCategory>() {
            @Override
            public void onResponse(@NonNull Call<PageVarientsByCategory> call, @NonNull Response<PageVarientsByCategory> response) {
                showProductsFromResponse(response);
            }

            @Override
            public void onFailure(@NonNull Call<PageVarientsByCategory> call, @NonNull Throwable t) {
                handleNoProducts();
            }
        });
    }


    public void getProductByVendor() {
        varientList.clear();
        totalPages = 1;
        currentPage = 1;
        smartRecyclerAdapter.getWrappedAdapter().notifyDataSetChanged();
        apiService.getAllProductsVarientsByVendorByPage(vendorId, currentPage, SPData.getAppPreferences().getPincode()).enqueue(new Callback<PageVarientsByCategory>() {
            @Override
            public void onResponse(@NonNull Call<PageVarientsByCategory> call, @NonNull Response<PageVarientsByCategory> response) {
                showProductsFromResponse(response);
            }

            @Override
            public void onFailure(@NonNull Call<PageVarientsByCategory> call, @NonNull Throwable t) {
                handleNoProducts();
            }
        });
    }

    private void showProductsFromResponse(Response<PageVarientsByCategory> response) {
        try {
            if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                PageVarientsByCategory productVarients = response.body();
                int index = varientList.size();
                int totalProducts = (int) productVarients.getData().getTotalCount();
                if (varientList.size() < totalProducts) {
                    binding.noProductLayout.setVisibility(GONE);
                    binding.productsRecyclerView.setVisibility(VISIBLE);
                    varientList.addAll(index, productVarients.getData().getRecords());
                    totalPages = (int) Math.ceil(productVarients.getData().getTotalCount() / productVarients.getData().getLimit());
                    currentPage++;
                    smartRecyclerAdapter.getWrappedAdapter().notifyDataSetChanged();
                    if (mHasReachedBottomOnce)
                        mHasReachedBottomOnce = false;
                    binding.progressBar.setVisibility(GONE);
                } else if (totalProducts == 0) {
                    handleNoProducts();
                }
            } else {
                handleNoProducts();
            }
        } catch (Exception e) {
            Log.e("TAG", "showProductsFromResponse: " + e);
            handleNoProducts();
        }
    }

    private void handleNoProducts() {
        binding.progressBar.setVisibility(GONE);
        binding.noProductLayout.setVisibility(VISIBLE);
    }

}