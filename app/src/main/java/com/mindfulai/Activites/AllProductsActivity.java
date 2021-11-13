package com.mindfulai.Activites;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.headerfooter.songhang.library.SmartRecyclerAdapter;
import com.mindfulai.Adapter.AttributeAdapter;
import com.mindfulai.Adapter.CategoryBannerSliderAdapter;
import com.mindfulai.Adapter.CategoryDropDownAdapter;
import com.mindfulai.Adapter.ProductsAdapter;
import com.mindfulai.Adapter.SubCategoriesAdapter;
import com.mindfulai.Models.BannerInfoData.BannerCategoryData;
import com.mindfulai.Models.BannerInfoData.CategoryBannerData;
import com.mindfulai.Models.CartInformation.CartDetailsInformation;
import com.mindfulai.Models.SubcategoryModel.Datum;
import com.mindfulai.Models.SubcategoryModel.SubcategoryModel;
import com.mindfulai.Models.varientsByCategory.PageVarientsByCategory;
import com.mindfulai.Models.varientsByCategory.ProductAttribute;
import com.mindfulai.Models.varientsByCategory.ProductAttributeBase;
import com.mindfulai.NetworkRetrofit.ApiService;
import com.mindfulai.NetworkRetrofit.ApiUtils;
import com.mindfulai.Utils.CommonUtils;
import com.mindfulai.Utils.SPData;
import com.mindfulai.ministore.R;
import com.mindfulai.ministore.databinding.ActivityAllProductsBinding;
import com.mindfulai.ministore.databinding.AllProductsHeaderLayoutBinding;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.nikartm.support.ImageBadgeView;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class AllProductsActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private static final String TAG = "AllProductsActivity";
    private static final int PROUDUCT_REQUEST_CODE = 106;

    private List<Datum> subcategoryList = new ArrayList<>();
    public List<com.mindfulai.Models.varientsByCategory.Datum> productsList;
    private List<CategoryBannerData> bannerImages_List = new ArrayList<>();
    private ArrayList<com.mindfulai.Models.categoryData.Datum> categoryList;
    private ImageBadgeView cartBadge;
    public int totalPages = 1, currentPage = 1, level;
    private ApiService apiService;
    private String type = "grid";
    private ActivityAllProductsBinding activityAllProductsBinding;
    private AllProductsHeaderLayoutBinding topHeaderView;
    public SmartRecyclerAdapter smartRecyclerAdapter;
    private boolean mHasReachedBottomOnce;
    public String category_id;
    public String brand_id;
    private SubCategoriesAdapter subCategoriesAdapter;
    private AttributeAdapter attributeAdapter;
    public ArrayList<ProductAttribute> attributeList = new ArrayList<>();
    private float x1;
    static final int MIN_DISTANCE = 150;
    public int currentCategoryIndex = -1;

    @SuppressLint("InflateParams")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LayoutInflater layoutInflater = getLayoutInflater();
        layoutInflater.inflate(R.layout.activity_all_products, null);
        activityAllProductsBinding = ActivityAllProductsBinding.inflate(layoutInflater);
        setContentView(activityAllProductsBinding.getRoot());
        try {
            Intent intent = getIntent();
            level = intent.getIntExtra("level", 1);
            category_id = intent.getStringExtra("category_id");
            brand_id = intent.getStringExtra("brand_id");
            categoryList = intent.getParcelableArrayListExtra("list");

            LayoutInflater layoutInflaterHeader = getLayoutInflater();
            layoutInflaterHeader.inflate(R.layout.all_products_header_layout, null);
            topHeaderView = AllProductsHeaderLayoutBinding.inflate(layoutInflaterHeader);
            setSupportActionBar(activityAllProductsBinding.allProductToolbar);

            if (categoryList != null && !SPData.showDiamondPageLayout() && SPData.showCategoryInDropDown()) {
                activityAllProductsBinding.dropdownLayout.setVisibility(VISIBLE);
                showCategoryInTitle();
            } else {
                activityAllProductsBinding.dropdownLayout.setVisibility(GONE);
                Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_white);
                getSupportActionBar().setTitle(CommonUtils.capitalizeWord(intent.getStringExtra("title")));
            }

            checkSPData();

            configProductsList();

            getSubCategories();

            getProductsVarients();

            activityAllProductsBinding.allProductToolbar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (subCategoriesAdapter.selectedPosition != -1) {
                        subCategoriesAdapter.deselectItem();
                        category_id = intent.getStringExtra("category_id");
                        subCategoriesAdapter.refreshAllProductPage(category_id);
                        subCategoriesAdapter.selectedPosition = -1;
                    }
                }
            });

            activityAllProductsBinding.back.setOnClickListener(v -> finish());

            activityAllProductsBinding.bottomCart.cartView.setOnClickListener(view -> startActivityForResult(new Intent(AllProductsActivity.this, CartPageActivity.class), 10));

            activityAllProductsBinding.productsGrid.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NotNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    if (!recyclerView.canScrollVertically(1) && !mHasReachedBottomOnce) {
                        mHasReachedBottomOnce = true;
                        if (currentPage <= totalPages) {
                            activityAllProductsBinding.progressBar.setVisibility(VISIBLE);
                            if (brand_id == null) {
                                getProductByCategory();
                            } else {
                                getProductByBrand();
                            }
                        }
                    }
                }
            });

        } catch (Exception e) {
            Log.e("TAG", "onCreate: " + e);
            e.printStackTrace();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (SPData.useTabViewPagerSubcategory()) {
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
                            if (currentCategoryIndex < subcategoryList.size() - 1)
                                currentCategoryIndex++;
                        }
                        if (currentCategoryIndex >= 0 && currentCategoryIndex < subcategoryList.size()) {
                            topHeaderView.subcategoriesRecyclerViewAbove.smoothScrollToPosition(currentCategoryIndex);
                            subCategoriesAdapter.checkSubCategory(currentCategoryIndex);
                        }
                    } else {
                        // consider as something else - a screen tap for example
                    }
                    break;
            }
        }

        return super.dispatchTouchEvent(ev);

    }

    private void showCategoryInTitle() {
        CategoryDropDownAdapter categoryDropDownAdapter = new CategoryDropDownAdapter(this, categoryList);
        activityAllProductsBinding.categoriesSpinner.setAdapter(categoryDropDownAdapter);
        int position = getIntent().getIntExtra("position", 0);
        activityAllProductsBinding.categoriesSpinner.setSelection(position, false);
        activityAllProductsBinding.categoriesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                com.mindfulai.Models.categoryData.Datum clickedItem = (com.mindfulai.Models.categoryData.Datum) parent.getItemAtPosition(position);
                category_id = clickedItem.getId();
                productsList.clear();
                subcategoryList.clear();
                currentPage = 1;
                totalPages = 1;

                if (smartRecyclerAdapter != null)
                    smartRecyclerAdapter.getWrappedAdapter().notifyDataSetChanged();
                if (subCategoriesAdapter != null)
                    subCategoriesAdapter.notifyDataSetChanged();
                topHeaderView.cardView.setVisibility(GONE);
                activityAllProductsBinding.shimmerView2.setVisibility(VISIBLE);
                if (SPData.showDiamondPageLayout()) {
                    getAllProductAttribute();
                }
                checkForBanner();
                getSubCategories();
                getProductsVarients();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void configProductsList() {
        productsList = new ArrayList<>();
        ProductsAdapter productAdapter;
        productAdapter = new ProductsAdapter(AllProductsActivity.this, productsList, type, PROUDUCT_REQUEST_CODE);
        smartRecyclerAdapter = new SmartRecyclerAdapter(productAdapter);
        smartRecyclerAdapter.setHeaderView(topHeaderView.getRoot());
        activityAllProductsBinding.productsGrid.setAdapter(smartRecyclerAdapter);
    }

    private void getProductByBrand() {
        apiService.getAllProductsVariantsByBrand(brand_id, currentPage, SPData.getAppPreferences().getPincode()).enqueue(new Callback<PageVarientsByCategory>() {
            @Override
            public void onResponse(@NonNull Call<PageVarientsByCategory> call, @NonNull Response<PageVarientsByCategory> response) {
                Log.e("TAG", "onResponse: " + response);
                showProductsFromResponse(response);
            }

            @Override
            public void onFailure(@NonNull Call<PageVarientsByCategory> call, @NonNull Throwable t) {
                Log.e(TAG, "onFailure: " + t.getMessage());
                handleNoProducts();
            }
        });
    }

    private void getProductByCategory() {
        String vendorId = "";
        if (getIntent().getBooleanExtra("vendor", false)) {
            vendorId = getIntent().getStringExtra("vendor_id");
        }
        String brandId = "";
        if (brand_id != null) {
            brandId = brand_id;
        }
        apiService.getAllProductsVarientsByPage(category_id, currentPage, SPData.getAppPreferences().getPincode(), vendorId, brandId).enqueue(new Callback<PageVarientsByCategory>() {
            @Override
            public void onResponse(@NonNull Call<PageVarientsByCategory> call, @NonNull Response<PageVarientsByCategory> response) {
                Log.e("TAG", "onResponse: " + response);
                showProductsFromResponse(response);
            }

            @Override
            public void onFailure(@NonNull Call<PageVarientsByCategory> call, @NonNull Throwable t) {
                Log.e(TAG, "onFailure: " + t.getMessage());
                handleNoProducts();
            }
        });
    }


    public void getAllProductAttribute() {
        ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());
        apiService.getAllProductAttribute(category_id).enqueue(new Callback<ProductAttributeBase>() {
            @Override
            public void onResponse(@NotNull Call<ProductAttributeBase> call, @NotNull Response<ProductAttributeBase> response) {
                Log.e("TAG", "onResponse: " + response);
                if (response.isSuccessful() && response.body() != null) {
                    ProductAttributeBase productAttributeBase = response.body();
                    attributeList = productAttributeBase.getData();
                    topHeaderView.recyclerviewAttributes.setLayoutManager(new GridLayoutManager(AllProductsActivity.this, 2));
                    attributeAdapter = new AttributeAdapter(AllProductsActivity.this, attributeList);
                    topHeaderView.recyclerviewAttributes.setAdapter(attributeAdapter);
                    attributeAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(@NotNull Call<ProductAttributeBase> call, @NotNull Throwable t) {

            }
        });
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
        activityAllProductsBinding.bottomCart.cartItem.setVisibility(GONE);
    }

    @SuppressLint("SetTextI18n")
    public void setCartAmount(CartDetailsInformation cartDetailsInformation) {
        float totalAmt = cartDetailsInformation.getData().getTotal();
        if (totalAmt > 0) {
            int qty = cartDetailsInformation.getData().getProducts().size();
            activityAllProductsBinding.bottomCart.cartItem.setVisibility(VISIBLE);
            activityAllProductsBinding.bottomCart.cartTotal.setText(getString(R.string.rs) + totalAmt);
            activityAllProductsBinding.bottomCart.cartQty.setText(qty + " item");
        } else {
            handleNoCartItem();
        }
    }


    private void checkSPData() {

        if (SPData.showDiamondPageLayout()) {
            getAllProductAttribute();
            topHeaderView.editTextSearch.setVisibility(VISIBLE);
        } else {
            topHeaderView.editTextSearch.setVisibility(GONE);
        }

        if (!SPData.showGridView())
            type = "list";

        if (SPData.useCustomHeight())
            topHeaderView.cardView.getLayoutParams().height = SPData.allProductBannerHeight();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(AllProductsActivity.this);
        linearLayoutManager.setOrientation(RecyclerView.HORIZONTAL);

        if (SPData.showCategorySliderInMiddle()) {
            topHeaderView.subcategoriesRecyclerViewAbove.setLayoutManager(linearLayoutManager);
            topHeaderView.subcategoriesRecyclerViewAbove.setVisibility(VISIBLE);
            topHeaderView.subcategoriesRecyclerViewBelow.setVisibility(GONE);
        } else {
            topHeaderView.subcategoriesRecyclerViewAbove.setVisibility(GONE);
            topHeaderView.subcategoriesRecyclerViewBelow.setVisibility(VISIBLE);
            topHeaderView.subcategoriesRecyclerViewBelow.setLayoutManager(linearLayoutManager);
        }

        if (SPData.showGridView() && !SPData.showDiamondPageLayout())
            activityAllProductsBinding.productsGrid.setLayoutManager(new CommonUtils(this).getProductGridLayoutManager());
        else {
            activityAllProductsBinding.productsGrid.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        }
        checkForBanner();
    }

    public void checkForBanner() {
        if (SPData.showAllProductsBanner())
            getBanner();
        else
            topHeaderView.cardView.setVisibility(GONE);
    }


    public void getBanner() {
        if (category_id != null) {
            bannerImages_List.clear();
            topHeaderView.bannerSlider.recyclerView.getRecycledViewPool().clear();
            ApiService apiService = ApiUtils.getHeaderAPIService();
            apiService.getCategoryBannerData(category_id).enqueue(new Callback<BannerCategoryData>() {
                @Override
                public void onResponse(@NonNull Call<BannerCategoryData> call, @NonNull Response<BannerCategoryData> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().getData().size() > 0) {
                        BannerCategoryData bannerData = response.body();
                        assert bannerData != null;
                        bannerImages_List = bannerData.getData();
                        setupViews();
                    } else {
                        topHeaderView.cardView.setVisibility(GONE);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<BannerCategoryData> call, @NonNull Throwable t) {
                    topHeaderView.cardView.setVisibility(GONE);
                    Toast.makeText(SPData.getAppContext(), "Failed to connect", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            topHeaderView.cardView.setVisibility(GONE);
        }
    }

    private void setupViews() {
        try {
            CategoryBannerSliderAdapter categoryBannerSliderAdapter = new CategoryBannerSliderAdapter(AllProductsActivity.this, bannerImages_List);
            topHeaderView.bannerSlider.setAdapter(categoryBannerSliderAdapter);
            topHeaderView.bannerSlider.setInterval(3000);
            topHeaderView.cardView.setVisibility(VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("TAG", "setupViews: " + e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        int total = SPData.getAppPreferences().getTotalCartCount();
        if (cartBadge != null)
            cartBadge.setBadgeValue(total);
        if (resultCode == RESULT_OK && data != null) {
            int position = data.getIntExtra("position", -1);
            int incart = data.getIntExtra("incart", -1);
            String id = data.getStringExtra("id");
            ((ProductsAdapter) smartRecyclerAdapter.getWrappedAdapter()).updateInCartItem(position, incart, id);
        }
    }


    public void addBadge(String count) {
        cartBadge.setBadgeValue(Integer.parseInt(count));
    }

    public void removeBadge() {
        cartBadge.setBadgeValue(0);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        if (item.getItemId() == R.id.notification_item) {
            startActivity(new Intent(AllProductsActivity.this, CommonActivity.class).putExtra("show", "notification"));
        }
        if (item.getItemId() == R.id.sort) {
            View dialogView = getLayoutInflater().inflate(R.layout.bottom_sheet_sort, null);
            final BottomSheetDialog dialog = new BottomSheetDialog(AllProductsActivity.this);
            dialog.setContentView(dialogView);

            TextView sortPriceHtoL, sortPriceLtoH;
            sortPriceHtoL = dialogView.findViewById(R.id.tv_sort_price_h_to_l);
            sortPriceLtoH = dialogView.findViewById(R.id.tv_sort_price_l_to_h);
            sortPriceHtoL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Collections.sort(productsList, (o1, o2) -> (int) (o2.getVarients().get(0).getPrice() - o1.getVarients().get(0).getPrice()));
                    dialog.cancel();
                    if (smartRecyclerAdapter != null)
                        smartRecyclerAdapter.getWrappedAdapter().notifyDataSetChanged();
                }
            });

            sortPriceLtoH.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Collections.sort(productsList, (o1, o2) -> (int) (o1.getVarients().get(0).getPrice() - o2.getVarients().get(0).getPrice()));
                    dialog.cancel();
                    if (smartRecyclerAdapter != null)
                        smartRecyclerAdapter.getWrappedAdapter().notifyDataSetChanged();
                }
            });

            dialog.show();
        }
        return super.onOptionsItemSelected(item);
    }


    private void filter(String text) {
        List<com.mindfulai.Models.varientsByCategory.Datum> filteredList = new ArrayList<>();
        for (com.mindfulai.Models.varientsByCategory.Datum varientList : productsList) {
            if (varientList.getProduct().getName().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(varientList);
            }
        }
        if (smartRecyclerAdapter != null)
            ((ProductsAdapter) smartRecyclerAdapter.getWrappedAdapter()
            ).filterList(filteredList);
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.cart_notification);
        item.setVisible(false);
        cartBadge = item.getActionView().findViewById(R.id.notification_badge);
        cartBadge.setVisibility(GONE);
        int total = SPData.getAppPreferences().getTotalCartCount();
        cartBadge.setBadgeValue(total);
        cartBadge.setEnabled(false);
        MenuItem list = menu.findItem(R.id.navigation_list_view);
        MenuItem grid = menu.findItem(R.id.navigation_grid_view);
        MenuItem search = menu.findItem(R.id.search);
        search.setVisible(!SPData.showDiamondPageLayout());
        list.setVisible(false);
        grid.setVisible(false);
        return true;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.browse_menu, menu);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.search));
        searchView.setOnQueryTextListener(this);
        return true;
    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        filter(newText);
        return true;
    }

    private void getSubCategories() {
        try {
            ApiService apiService = ApiUtils.getHeaderAPIService();
            if (category_id != null)
                apiService.getAllSubCategory(category_id, SPData.getAppPreferences().getPincode()).enqueue(new Callback<SubcategoryModel>() {
                    @Override
                    public void onResponse(@NonNull Call<SubcategoryModel> call, @NonNull Response<SubcategoryModel> response) {
                        handleSubCategoryResponse(call, response);
                    }

                    @Override
                    public void onFailure(@NonNull Call<SubcategoryModel> call, @NonNull Throwable t) {
                        Toast.makeText(AllProductsActivity.this, "Failed to connect", Toast.LENGTH_SHORT).show();
                    }
                });
            else {
                if (SPData.showSubcategoryByBrand())
                    apiService.getAllSubCategoryByBrandID(brand_id).enqueue(new Callback<SubcategoryModel>() {
                        @Override
                        public void onResponse(@NonNull Call<SubcategoryModel> call, @NonNull Response<SubcategoryModel> response) {
                            handleSubCategoryResponse(call, response);
                        }

                        @Override
                        public void onFailure(@NonNull Call<SubcategoryModel> call, @NonNull Throwable t) {
                            Log.e("TAG", "" + t);
                            Toast.makeText(AllProductsActivity.this, "Failed to connect", Toast.LENGTH_SHORT).show();
                        }
                    });
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("TAG", "getSubCategories: " + e);
        }
    }

    private void handleSubCategoryResponse(Call<SubcategoryModel> call, Response<SubcategoryModel> response) {
        if (response.isSuccessful()) {
            subcategoryList.clear();
            SubcategoryModel subCategoryDetails = response.body();
            assert subCategoryDetails != null;
            if (subCategoryDetails.getData().size() > 0) {
                subcategoryList = subCategoryDetails.getData();
                subCategoriesAdapter = new SubCategoriesAdapter(AllProductsActivity.this, subcategoryList, level);
                if (SPData.showCategorySliderInMiddle()) {
                    topHeaderView.subcategoriesRecyclerViewAbove.setAdapter(subCategoriesAdapter);
                } else
                    topHeaderView.subcategoriesRecyclerViewBelow.setAdapter(subCategoriesAdapter);
                subCategoriesAdapter.notifyDataSetChanged();
            } else {
                topHeaderView.subcategoriesRecyclerViewBelow.setVisibility(GONE);
                topHeaderView.subcategoriesRecyclerViewAbove.setVisibility(GONE);
            }
        }
    }

    public void getProductsVarients() {
        try {
            activityAllProductsBinding.shimmerView2.startShimmerAnimation();
            apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());
            if (currentPage <= totalPages)
                if (category_id != null) {
                    getProductByCategory();
                } else {
                    getProductByBrand();
                }
        } catch (Exception e) {
            Log.e("TAG", "getProductsVarients: " + e);
        }
    }


    private void showProductsFromResponse(Response<PageVarientsByCategory> response) {
        try {
            activityAllProductsBinding.shimmerView2.setVisibility(GONE);
            activityAllProductsBinding.noProducts.setVisibility(GONE);
            if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                PageVarientsByCategory productVarients = response.body();
                int index = productsList.size();
                int totalProducts = (int) productVarients.getData().getTotalCount();
                if (productsList.size() < totalProducts) {
                    totalPages = (int) Math.ceil(productVarients.getData().getTotalCount() / productVarients.getData().getLimit());
                    currentPage++;
                    productsList.addAll(index, productVarients.getData().getRecords());
                    smartRecyclerAdapter.getWrappedAdapter().notifyDataSetChanged();

                    if (SPData.showDiamondPageLayout()) {
                        ((ProductsAdapter) smartRecyclerAdapter.getWrappedAdapter()).allProductList.addAll(productVarients.getData().getRecords());
                        if (attributeAdapter != null)
                            ((ProductsAdapter) smartRecyclerAdapter.getWrappedAdapter()).filterByOption(attributeAdapter.listOfSelectedOptions);
                    }
                    if (mHasReachedBottomOnce)
                        mHasReachedBottomOnce = false;
                    activityAllProductsBinding.progressBar.setVisibility(GONE);
                } else if (totalProducts == 0) {
                    handleNoProducts();
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
        activityAllProductsBinding.shimmerView2.stopShimmerAnimation();
        activityAllProductsBinding.shimmerView2.setVisibility(GONE);
        productsList.clear();
        if (smartRecyclerAdapter != null && (smartRecyclerAdapter.getWrappedAdapter() != null)) {
            smartRecyclerAdapter.getWrappedAdapter().notifyDataSetChanged();
        }
        activityAllProductsBinding.noProducts.setVisibility(VISIBLE);
    }
}