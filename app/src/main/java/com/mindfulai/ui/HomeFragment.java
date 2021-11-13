package com.mindfulai.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import com.baoyz.widget.PullRefreshLayout;
import com.bumptech.glide.Glide;
import com.mindfulai.Activites.*;
import com.mindfulai.Adapter.BrandsAdapter;
import com.mindfulai.Adapter.CategoriesAdapter;
import com.mindfulai.Adapter.FeaturedCategoriesAdapter;
import com.mindfulai.Adapter.MainSliderAdapter;
import com.mindfulai.Adapter.ProductsAdapter;
import com.mindfulai.Adapter.SecondBannerAdapter;
import com.mindfulai.Adapter.VendorsAdapter;
import com.mindfulai.Models.BannerInfoData.BannerData;
import com.mindfulai.Models.BannerInfoData.BannerInfo;
import com.mindfulai.Models.Brand;
import com.mindfulai.Models.BrandModel;
import com.mindfulai.Models.DeliveryMethod.DeliveryMethodBase;
import com.mindfulai.Models.DeliveryMethod.DeliveryMethodData;
import com.mindfulai.Models.VendorBase;
import com.mindfulai.Models.VendorChild;
import com.mindfulai.Models.categoryData.CategoryInfo;
import com.mindfulai.Models.categoryData.Datum;
import com.mindfulai.Models.varientsByCategory.PageVarientsByCategory;
import com.mindfulai.Models.varientsByCategory.VarientsByCategory;
import com.mindfulai.NetworkRetrofit.ApiService;
import com.mindfulai.NetworkRetrofit.ApiUtils;
import com.mindfulai.Utils.CommonUtils;
import com.mindfulai.Utils.SPData;
import com.mindfulai.customclass.PicassoImageLoadingService;
import com.mindfulai.kwikapi.AllKwikServicesActivity;
import com.mindfulai.kwikapi.MobileRechargeActivity;
import com.mindfulai.kwikapi.PostpaidRechargeActivity;
import com.mindfulai.ministore.R;
import com.mindfulai.ministore.databinding.FragmentHomeBinding;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ss.com.bannerslider.Slider;

import static android.app.Activity.RESULT_OK;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.mindfulai.Utils.CommonUtils.*;

public class HomeFragment extends Fragment {

    private final List<com.mindfulai.Models.varientsByCategory.Datum> topFeaturedProductsList = new ArrayList<>();
    private final List<com.mindfulai.Models.varientsByCategory.Datum> featuredProductsList = new ArrayList<>();
    private final List<com.mindfulai.Models.varientsByCategory.Datum> bestSellingList = new ArrayList<>();
    private final List<com.mindfulai.Models.varientsByCategory.Datum> preOrderProductsList = new ArrayList<>();
    private ArrayList<Brand> brandArrayList = new ArrayList<>();
    private final List<BannerInfo> bannerImages_List = new ArrayList<>();
    private final List<BannerInfo> bannerImages_List2 = new ArrayList<>();
    private final List<BannerInfo> bannerImages_List3 = new ArrayList<>();

    private final List<BannerInfo> bannerImages_List4 = new ArrayList<>();
    private final List<BannerInfo> bannerImages_List5 = new ArrayList<>();
    private final List<BannerInfo> bannerImages_List6 = new ArrayList<>();
    private List<Datum> categoryList = new ArrayList<>();
    private List<Datum> featuredCategoryList = new ArrayList<>();

    private static final String TAG = "HomeFragment";
    private CategoriesAdapter categoriesAdapter;
    private boolean fabExpanded = false;
    private ProductsAdapter featuredProductAdapter;
    private ProductsAdapter bestSellingProductAdapter;
    private ProductsAdapter preOrderProductAdapter;
    public FragmentHomeBinding fragmentHomeBinding;
    private SecondBannerAdapter secondBannerAdapter;
    private Handler handler;
    private Runnable runnable;
    private SecondBannerAdapter carouselAdapter1;
    private SecondBannerAdapter carouselAdapter2;
    private SecondBannerAdapter carouselAdapter3;
    private ProductsAdapter topfeaturedProductAdapter;


    @SuppressLint({"SetTextI18n", "InflateParams"})
    public View onCreateView(@NonNull LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        inflater.inflate(R.layout.fragment_home, container, false);
        fragmentHomeBinding = FragmentHomeBinding.inflate(inflater);

        initViews();

        checkSPData();

        hideFabSubmenu();

        setView();

        handleReferesh();

        handleOnClick();

        return fragmentHomeBinding.getRoot();
    }

    private void handleReferesh() {
        fragmentHomeBinding.swipeRefreshLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                setView();
                final Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        fragmentHomeBinding.swipeRefreshLayout.setRefreshing(false);
                    }
                }, 1000);
            }
        });
    }

    private void handleOnClick() {

        fragmentHomeBinding.mobileRecharge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(requireActivity(), MobileRechargeActivity.class).putExtra("type", "Prepaid"));
            }
        });
        fragmentHomeBinding.postpaidRecharge.setOnClickListener(v -> startActivity(new Intent(requireActivity(), PostpaidRechargeActivity.class).putExtra("type", "Postpaid")));

        fragmentHomeBinding.dthRecharge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(requireActivity(), MobileRechargeActivity.class).putExtra("type", "DTH"));
            }
        });
        fragmentHomeBinding.sellAllRecharge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(requireActivity(), AllKwikServicesActivity.class));
            }
        });

        fragmentHomeBinding.seeAllPreorderProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(requireActivity(), AllPreOrderProductsActivity.class));
            }
        });
        fragmentHomeBinding.pickupOrderLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(requireActivity(), PickupOrderActivity.class));
            }
        });

        fragmentHomeBinding.wellnessCustomOrderItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
           //     startActivity(new Intent(requireActivity(), PlaceCustomOrderActivity.class));
            }
        });
        fragmentHomeBinding.wellnessItemWhatsapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommonUtils.sendToWhatsApp(requireActivity());
            }
        });
        fragmentHomeBinding.wellnessItemCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommonUtils.sendToPhone(requireActivity(), SPData.supportCallNumber());
            }
        });
//        fragmentHomeBinding.customOrderItem.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                startActivity(new Intent(requireActivity(), PlaceCustomOrderActivity.class));
//            }
//        });
        fragmentHomeBinding.edittextSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(requireActivity(), SearchPrdouctActivity.class));
            }
        });

        fragmentHomeBinding.edittextSearch2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(requireActivity(), SearchPrdouctActivity.class));
            }
        });
        fragmentHomeBinding.floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SPData.showBothCallAndWhatsAppOnHome())
                    if (fabExpanded) {
                        hideFabSubmenu();
                    } else {
                        showFabSubmenu();
                    }
                else if (SPData.showOnlyWhatsAppOnHome()) {
                    CommonUtils.sendToWhatsApp(requireActivity());
                } else {
                    CommonUtils.sendToPhone(requireActivity(), SPData.supportCallNumber());
                }
            }
        });
        fragmentHomeBinding.fabCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommonUtils.sendToPhone(requireActivity(), SPData.supportCallNumber());
            }
        });
        fragmentHomeBinding.fabWhatsapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommonUtils.sendToWhatsApp(requireActivity());
            }
        });
        fragmentHomeBinding.seeAllCategories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(getActivity(), SeeAllCategoryActivity.class), 10);
            }
        });
    }

    private void setView() {


        getProductCategories();

        fragmentHomeBinding.seeAllCategories.setText(SPData.seeAllCategoryText());

        if (SPData.showPreOrderProducts()) {
            fragmentHomeBinding.preorderProductLayout.setVisibility(VISIBLE);
            getAllPreOrderProducts();
        } else {
            fragmentHomeBinding.preorderProductLayout.setVisibility(GONE);
        }


        if (SPData.showBestSellingProducts()) {
            fragmentHomeBinding.bestSellingLayout.setVisibility(VISIBLE);
            getAllBestSelling();
        } else {
            fragmentHomeBinding.bestSellingLayout.setVisibility(GONE);
        }

        if (SPData.showFeaturedCategory()) {
            fragmentHomeBinding.featuredCategoryLayout.setVisibility(VISIBLE);
            getFeaturedCategories();
        } else
            fragmentHomeBinding.featuredCategoryLayout.setVisibility(GONE);


        if (SPData.showFeaturedProducts()) {
            fragmentHomeBinding.featuredProductLayout.setVisibility(VISIBLE);
            getAllFeaturedProducts();
        } else {
            fragmentHomeBinding.featuredProductLayout.setVisibility(GONE);
        }

        if (SPData.showHomePageBrand() && !SPData.showCategorySameAsBrand()) {
            getBrands();
        } else if (!SPData.showCategorySameAsBrand()) {
            hideBrands();
        }

        if (SPData.showTopVendors()) {
            getVendors();
        } else {
            hideVendors();
        }

    }

    private void getAllPreOrderProducts() {
        preOrderProductsList.clear();
        ApiService apiService = ApiUtils.getAPIService();
        apiService.getPreOrderProducts(1).enqueue(new Callback<PageVarientsByCategory>() {
            @Override
            public void onResponse(@NotNull Call<PageVarientsByCategory> call, @NotNull Response<PageVarientsByCategory> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData().getRecords().size() > 0) {
                    preOrderProductsList.addAll(response.body().getData().getRecords());
                    preOrderProductAdapter.notifyDataSetChanged();
                } else {
                    hidePreOrderProduct();
                }
            }

            @Override
            public void onFailure(@NotNull Call<PageVarientsByCategory> call, @NotNull Throwable t) {
                hidePreOrderProduct();
            }
        });
    }

    private void hidePreOrderProduct() {
        fragmentHomeBinding.preorderProductLayout.setVisibility(GONE);
    }

    private void checkSPData() {

        fragmentHomeBinding.topVendorTxt.setText(SPData.topVendorTitle());
        fragmentHomeBinding.topVendorTxt2.setText(SPData.topVendorTitle());
        if (SPData.showVendorBelowCategory()) {
            fragmentHomeBinding.topVendorLl2.setVisibility(VISIBLE);
            fragmentHomeBinding.topVendorLl1.setVisibility(GONE);
        } else {
            fragmentHomeBinding.topVendorLl2.setVisibility(GONE);
            fragmentHomeBinding.topVendorLl1.setVisibility(VISIBLE);
        }

        if (SPData.showRechargesAndBillPayOnHomePage()) {
            fragmentHomeBinding.rechargeBillLayout.setVisibility(VISIBLE);
        } else {
            fragmentHomeBinding.rechargeBillLayout.setVisibility(GONE);
        }

        if (SPData.useBannersAndCarousel()) {
            getFixedBanner();
            getSlidingBanner();
            getTopSellingCategories();
            fragmentHomeBinding.topProductGrid.setLayoutManager(new CommonUtils(getActivity()).getProductGridLayoutManager());
            topfeaturedProductAdapter = new ProductsAdapter(getContext(), topFeaturedProductsList, "grid", TOP_FEATURED_PRODUCTS_REQUEST_CODE);
            fragmentHomeBinding.topProductGrid.setAdapter(topfeaturedProductAdapter);
        } else {
            fragmentHomeBinding.banner1.setVisibility(GONE);
            fragmentHomeBinding.banner2.setVisibility(GONE);
            fragmentHomeBinding.banner3.setVisibility(GONE);
            fragmentHomeBinding.banner4.setVisibility(GONE);
            fragmentHomeBinding.banner5.setVisibility(GONE);
            fragmentHomeBinding.banner6.setVisibility(GONE);
            fragmentHomeBinding.banner7.setVisibility(GONE);
            fragmentHomeBinding.banner8.setVisibility(GONE);
            fragmentHomeBinding.carousel1.setVisibility(GONE);
            fragmentHomeBinding.carousel2.setVisibility(GONE);
            fragmentHomeBinding.carousel3.setVisibility(GONE);
            fragmentHomeBinding.topSellingCategoryLayout.setVisibility(GONE);
            fragmentHomeBinding.topProductLayout.setVisibility(GONE);
        }

        if (SPData.useCustomHeight()) {
            fragmentHomeBinding.cardViewBanner1.getLayoutParams().height = SPData.topBannerHeight();
            fragmentHomeBinding.cardViewBanner2.getLayoutParams().height = SPData.middleBannerHeight();
            fragmentHomeBinding.cardViewBanner3.getLayoutParams().height = SPData.bottomBannerHeight();
            fragmentHomeBinding.banner1.getLayoutParams().height = SPData.bannerHeight();
            fragmentHomeBinding.banner2.getLayoutParams().height = SPData.bannerHeight();
            fragmentHomeBinding.banner3.getLayoutParams().height = SPData.bannerHeight();
            fragmentHomeBinding.banner4.getLayoutParams().height = SPData.bannerHeight();
            fragmentHomeBinding.banner5.getLayoutParams().height = SPData.bannerHeight();
            fragmentHomeBinding.banner6.getLayoutParams().height = SPData.bannerHeight();
            fragmentHomeBinding.banner7.getLayoutParams().height = SPData.bannerHeight();
            fragmentHomeBinding.banner8.getLayoutParams().height = SPData.bannerHeight();
        }

        if (SPData.showHomePageMsg())
            fragmentHomeBinding.homePageMsg.setText(SPData.homePageMsgText());
        else
            fragmentHomeBinding.homePageMsg.setVisibility(GONE);
        fragmentHomeBinding.fabWhatsapp.setBackgroundTintList(getResources().getColorStateList(R.color.whastappColor));
        fragmentHomeBinding.fabCall.setBackgroundTintList(getResources().getColorStateList(R.color.callColor));
        fragmentHomeBinding.floatingActionButton.setBackgroundTintList(getResources().getColorStateList(R.color.fabHomeColor));


        fragmentHomeBinding.edittextSearch2.setHint(SPData.searchHint());
        fragmentHomeBinding.edittextSearch.setHint(SPData.searchHint());
        if (SPData.showTopCategoryTitle()) {
            fragmentHomeBinding.productText.setVisibility(VISIBLE);
        } else {
            fragmentHomeBinding.productText.setVisibility(GONE);
        }
        fragmentHomeBinding.productText.setText(SPData.topCategoryText());
        fragmentHomeBinding.topBrandsBelow.setText(SPData.topBrandsTitle());
        fragmentHomeBinding.topBrandsAbove.setText(SPData.topBrandsTitle());
        fragmentHomeBinding.featuredProductsText.setText(SPData.featuredProductsTitle());
        fragmentHomeBinding.bestSellingText.setText(SPData.bestSellingProductsTitle());

        if (SPData.useWellnessHomeLayout()) {
            fragmentHomeBinding.wellnessButtonLayout.setVisibility(VISIBLE);
        } else {
            fragmentHomeBinding.wellnessButtonLayout.setVisibility(GONE);
        }

        if (SPData.showDunzoTypeCategory()) {
            fragmentHomeBinding.productText.setText("Instant delivery to your doorstep");
        }

        if (SPData.showSearchInHomeScreen()) {
            showSearchBar();
        } else {
            fragmentHomeBinding.edittextSearchLayout.setVisibility(GONE);
            fragmentHomeBinding.edittextSearch.setVisibility(GONE);
        }

        if (SPData.showDeliveryDaysOnHomePage() && !SPData.getAppPreferences().getPincode().isEmpty()) {
            fragmentHomeBinding.cardShippingDays.setVisibility(VISIBLE);
            getDeliveryMethods(SPData.getAppPreferences().getPincode());
        } else {
            fragmentHomeBinding.cardShippingDays.setVisibility(GONE);
        }

        if (SPData.allowCustomOrder()) {
            fragmentHomeBinding.customOrderLayout.setVisibility(View.VISIBLE);
        } else {
            fragmentHomeBinding.customOrderLayout.setVisibility(View.GONE);
        }

        if (SPData.allowCustomPickupOrder()) {
            fragmentHomeBinding.pickupOrderLayout.setVisibility(VISIBLE);
        } else {
            fragmentHomeBinding.pickupOrderLayout.setVisibility(GONE);
        }

        if (SPData.showSeeAllCategoryButton()) {
            fragmentHomeBinding.seeAllCategories.setVisibility(View.GONE);
        } else
            fragmentHomeBinding.seeAllCategories.setVisibility(View.VISIBLE);

        if (SPData.hideContactBtn()) {
            fragmentHomeBinding.floatingActionButton.setVisibility(View.GONE);
        } else {
            fragmentHomeBinding.floatingActionButton.setVisibility(View.VISIBLE);
        }
        if (SPData.showAboveBrand()) {
            fragmentHomeBinding.topbrandsRecyclerViewAbove.setVisibility(VISIBLE);
            fragmentHomeBinding.topBrandsAbove.setVisibility(VISIBLE);
            fragmentHomeBinding.topBrandsBelow.setVisibility(GONE);
            fragmentHomeBinding.topbrandsRecyclerViewBelow.setVisibility(GONE);
        } else {
            fragmentHomeBinding.topbrandsRecyclerViewBelow.setVisibility(VISIBLE);
            fragmentHomeBinding.topBrandsBelow.setVisibility(VISIBLE);
            fragmentHomeBinding.topBrandsAbove.setVisibility(GONE);
            fragmentHomeBinding.topbrandsRecyclerViewAbove.setVisibility(GONE);
        }
        if (SPData.showCategorySameAsBrand()) {
            if (SPData.showTopCategoryTitle()) {
                fragmentHomeBinding.topBrandsAbove.setVisibility(VISIBLE);
                fragmentHomeBinding.topBrandsBelow.setVisibility(VISIBLE);
            } else {
                fragmentHomeBinding.topBrandsAbove.setVisibility(GONE);
                fragmentHomeBinding.topBrandsBelow.setVisibility(GONE);
            }
            fragmentHomeBinding.topBrandsAbove.setText(SPData.topCategoryText());
            fragmentHomeBinding.topBrandsBelow.setText(SPData.topCategoryText());
        } else {
            fragmentHomeBinding.topBrandsAbove.setText(SPData.topBrandsTitle());
            fragmentHomeBinding.topBrandsBelow.setText(SPData.topBrandsTitle());
        }
        if (SPData.showFirstBanners()) {
            fragmentHomeBinding.cardViewBanner1.setVisibility(VISIBLE);
            getBanners();
        } else {
            fragmentHomeBinding.cardViewBanner1.setVisibility(GONE);
        }

        if (SPData.showMiddleBanner())
            getBanners2();
        else {
            fragmentHomeBinding.bannerSlider2.setVisibility(GONE);
            fragmentHomeBinding.secondaryBannerImages.setVisibility(GONE);
        }

        if (SPData.showBannerAtBottom()) {
            getBannersAtBottom();
        } else {
            fragmentHomeBinding.cardViewBanner3.setVisibility(GONE);
        }
    }

    private void getSlidingBanner() {
        setCarousel1();
        setCarousel2();
        setCarousel3();
    }

    private void setCarousel3() {
        LinearLayoutManager layoutManager3 = new LinearLayoutManager(requireActivity());
        layoutManager3.setOrientation(RecyclerView.HORIZONTAL);

        fragmentHomeBinding.carousel3.setLayoutManager(layoutManager3);
        carouselAdapter3 = new SecondBannerAdapter(requireActivity(), bannerImages_List6, 1);
        fragmentHomeBinding.carousel3.setAdapter(carouselAdapter3);
        ApiService apiService = ApiUtils.getAPIService();
        apiService.getCarouselBanner("6").enqueue(new Callback<BannerData>() {
            @Override
            public void onResponse(@NotNull Call<BannerData> call, @NotNull Response<BannerData> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null && response.body().getData().size() > 0) {
                    bannerImages_List6.clear();
                    bannerImages_List6.addAll(response.body().getData());
                    carouselAdapter3.notifyDataSetChanged();
                } else {
                    fragmentHomeBinding.carousel3.setVisibility(GONE);
                }
            }

            @Override
            public void onFailure(@NotNull Call<BannerData> call, @NotNull Throwable t) {
                fragmentHomeBinding.carousel3.setVisibility(GONE);
            }
        });
    }

    private void setCarousel2() {
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(requireActivity());
        layoutManager2.setOrientation(RecyclerView.HORIZONTAL);

        fragmentHomeBinding.carousel2.setLayoutManager(layoutManager2);
        carouselAdapter2 = new SecondBannerAdapter(requireActivity(), bannerImages_List5, 1);
        fragmentHomeBinding.carousel2.setAdapter(carouselAdapter2);
        ApiService apiService = ApiUtils.getAPIService();
        apiService.getCarouselBanner("5").enqueue(new Callback<BannerData>() {
            @Override
            public void onResponse(@NotNull Call<BannerData> call, @NotNull Response<BannerData> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null && response.body().getData().size() > 0) {
                    bannerImages_List5.clear();
                    bannerImages_List5.addAll(response.body().getData());
                    carouselAdapter2.notifyDataSetChanged();
                } else {
                    fragmentHomeBinding.carousel2.setVisibility(GONE);
                }
            }

            @Override
            public void onFailure(@NotNull Call<BannerData> call, @NotNull Throwable t) {
                fragmentHomeBinding.carousel2.setVisibility(GONE);
            }
        });
    }

    private void setCarousel1() {
        LinearLayoutManager layoutManager1 = new LinearLayoutManager(requireActivity());
        layoutManager1.setOrientation(RecyclerView.HORIZONTAL);

        fragmentHomeBinding.carousel1.setLayoutManager(layoutManager1);
        carouselAdapter1 = new SecondBannerAdapter(requireActivity(), bannerImages_List4, 1);
        fragmentHomeBinding.carousel1.setAdapter(carouselAdapter1);
        ApiService apiService = ApiUtils.getAPIService();
        apiService.getCarouselBanner("4").enqueue(new Callback<BannerData>() {
            @Override
            public void onResponse(@NotNull Call<BannerData> call, @NotNull Response<BannerData> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null && response.body().getData().size() > 0) {
                    bannerImages_List4.clear();
                    bannerImages_List4.addAll(response.body().getData());
                    carouselAdapter1.notifyDataSetChanged();
                } else {
                    fragmentHomeBinding.carousel1.setVisibility(GONE);
                }
            }

            @Override
            public void onFailure(@NotNull Call<BannerData> call, @NotNull Throwable t) {
                fragmentHomeBinding.carousel1.setVisibility(GONE);
            }
        });
    }

    private void hideBanner(int finalI) {
        if (finalI == 1) {
            fragmentHomeBinding.banner1.setVisibility(GONE);
        } else if (finalI == 2) {
            fragmentHomeBinding.banner2.setVisibility(GONE);
        } else if (finalI == 3) {
            fragmentHomeBinding.banner3.setVisibility(GONE);
        } else if (finalI == 4) {
            fragmentHomeBinding.banner4.setVisibility(GONE);
        } else if (finalI == 5) {
            fragmentHomeBinding.banner5.setVisibility(GONE);
        } else if (finalI == 6) {
            fragmentHomeBinding.banner6.setVisibility(GONE);
        } else if (finalI == 7) {
            fragmentHomeBinding.banner7.setVisibility(GONE);
        } else if (finalI == 8) {
            fragmentHomeBinding.banner8.setVisibility(GONE);
        }
    }

    private void getFixedBanner() {
        ApiService apiService = ApiUtils.getAPIService();
        apiService.getFixedBanner("1").enqueue(new Callback<BannerData>() {
            @Override
            public void onResponse(@NotNull Call<BannerData> call, @NotNull Response<BannerData> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null && response.body().getData().size() > 0) {
                    String image = response.body().getData().get(0).getImage();
                    if (getActivity() != null)
                        Glide.with(requireActivity()).load(SPData.getBucketUrl() + image).into(fragmentHomeBinding.banner1);
                } else {
                    hideBanner(1);
                }
            }

            @Override
            public void onFailure(@NotNull Call<BannerData> call, @NotNull Throwable t) {
                hideBanner(1);
            }
        });
        apiService.getFixedBanner("2").enqueue(new Callback<BannerData>() {
            @Override
            public void onResponse(@NotNull Call<BannerData> call, @NotNull Response<BannerData> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null && response.body().getData().size() > 0) {
                    String image = response.body().getData().get(0).getImage();
                    if (getActivity() != null)
                        Glide.with(requireActivity()).load(SPData.getBucketUrl() + image).into(fragmentHomeBinding.banner2);
                } else {
                    hideBanner(2);
                }
            }

            @Override
            public void onFailure(@NotNull Call<BannerData> call, @NotNull Throwable t) {
                hideBanner(2);
            }
        });
        apiService.getFixedBanner("3").enqueue(new Callback<BannerData>() {
            @Override
            public void onResponse(@NotNull Call<BannerData> call, @NotNull Response<BannerData> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null && response.body().getData().size() > 0) {
                    String image = response.body().getData().get(0).getImage();
                    if (getActivity() != null)
                        Glide.with(requireActivity()).load(SPData.getBucketUrl() + image).into(fragmentHomeBinding.banner3);
                } else {
                    hideBanner(3);
                }
            }

            @Override
            public void onFailure(@NotNull Call<BannerData> call, @NotNull Throwable t) {
                hideBanner(3);
            }
        });
        apiService.getFixedBanner("4").enqueue(new Callback<BannerData>() {
            @Override
            public void onResponse(@NotNull Call<BannerData> call, @NotNull Response<BannerData> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null && response.body().getData().size() > 0) {
                    String image = response.body().getData().get(0).getImage();
                    if (getActivity() != null)
                        Glide.with(requireActivity()).load(SPData.getBucketUrl() + image).into(fragmentHomeBinding.banner4);
                } else {
                    hideBanner(4);
                }
            }

            @Override
            public void onFailure(@NotNull Call<BannerData> call, @NotNull Throwable t) {
                hideBanner(4);
            }
        });
        apiService.getFixedBanner("5").enqueue(new Callback<BannerData>() {
            @Override
            public void onResponse(@NotNull Call<BannerData> call, @NotNull Response<BannerData> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null && response.body().getData().size() > 0) {
                    String image = response.body().getData().get(0).getImage();
                    if (getActivity() != null)
                        Glide.with(requireActivity()).load(SPData.getBucketUrl() + image).into(fragmentHomeBinding.banner5);
                } else {
                    hideBanner(5);
                }
            }

            @Override
            public void onFailure(@NotNull Call<BannerData> call, @NotNull Throwable t) {
                hideBanner(5);
            }
        });
        apiService.getFixedBanner("6").enqueue(new Callback<BannerData>() {
            @Override
            public void onResponse(@NotNull Call<BannerData> call, @NotNull Response<BannerData> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null && response.body().getData().size() > 0) {
                    String image = response.body().getData().get(0).getImage();
                    if (getActivity() != null)
                        Glide.with(requireActivity()).load(SPData.getBucketUrl() + image).into(fragmentHomeBinding.banner6);
                } else {
                    hideBanner(6);
                }
            }

            @Override
            public void onFailure(@NotNull Call<BannerData> call, @NotNull Throwable t) {
                hideBanner(6);
            }
        });
        apiService.getFixedBanner("7").enqueue(new Callback<BannerData>() {
            @Override
            public void onResponse(@NotNull Call<BannerData> call, @NotNull Response<BannerData> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null && response.body().getData().size() > 0) {
                    String image = response.body().getData().get(0).getImage();
                    if (getActivity() != null)
                        Glide.with(requireActivity()).load(SPData.getBucketUrl() + image).into(fragmentHomeBinding.banner7);
                } else {
                    hideBanner(7);
                }
            }

            @Override
            public void onFailure(@NotNull Call<BannerData> call, @NotNull Throwable t) {
                hideBanner(7);
            }
        });
        apiService.getFixedBanner("8").enqueue(new Callback<BannerData>() {
            @Override
            public void onResponse(@NotNull Call<BannerData> call, @NotNull Response<BannerData> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null && response.body().getData().size() > 0) {
                    String image = response.body().getData().get(0).getImage();
                    if (getActivity() != null)
                        Glide.with(requireActivity()).load(SPData.getBucketUrl() + image).into(fragmentHomeBinding.banner8);
                } else {
                    hideBanner(8);
                }
            }

            @Override
            public void onFailure(@NotNull Call<BannerData> call, @NotNull Throwable t) {
                hideBanner(8);
            }
        });


    }

    private void showSearchBar() {
        if (SPData.useSecondEdittextSearchBox()) {
            fragmentHomeBinding.edittextSearchLayout.setVisibility(VISIBLE);
            fragmentHomeBinding.edittextSearch.setVisibility(GONE);
        } else {
            fragmentHomeBinding.edittextSearchLayout.setVisibility(GONE);
            fragmentHomeBinding.edittextSearch.setVisibility(VISIBLE);
        }
    }

    private void initViews() {
        LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(getActivity());
        linearLayoutManager1.setOrientation(RecyclerView.HORIZONTAL);
        fragmentHomeBinding.topbrandsRecyclerViewAbove.setLayoutManager(linearLayoutManager1);

        LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(getActivity());
        linearLayoutManager2.setOrientation(RecyclerView.HORIZONTAL);
        fragmentHomeBinding.topbrandsRecyclerViewBelow.setLayoutManager(linearLayoutManager2);

        LinearLayoutManager linearLayoutManager3 = new LinearLayoutManager(requireActivity());
        linearLayoutManager3.setOrientation(RecyclerView.VERTICAL);
        fragmentHomeBinding.featuredCategoryRv.setLayoutManager(linearLayoutManager3);

        LinearLayoutManager linearLayoutManager4 = new LinearLayoutManager(getActivity());
        linearLayoutManager4.setOrientation(RecyclerView.HORIZONTAL);
        if (SPData.showVendorBelowCategory()) {
            fragmentHomeBinding.topvendorRecyclerView2.setLayoutManager(linearLayoutManager4);
        } else
            fragmentHomeBinding.topvendorRecyclerView.setLayoutManager(linearLayoutManager4);

        if (!SPData.showSecondaryBannerSameAsTop()) {
            if (SPData.showBannerInSlider()) {
                try {
                    LinearLayoutManager layoutManager5 = new LinearLayoutManager(requireActivity()) {
                        @Override
                        public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
                            if (getActivity() != null && isAdded()) {
                                LinearSmoothScroller smoothScroller = new LinearSmoothScroller(requireActivity()) {
                                    private static final float SPEED = 25f;

                                    @Override
                                    protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                                        return SPEED / displayMetrics.densityDpi;
                                    }
                                };
                                smoothScroller.setTargetPosition(position);
                                startSmoothScroll(smoothScroller);
                            }
                        }
                    };
                    layoutManager5.setOrientation(RecyclerView.HORIZONTAL);
                    fragmentHomeBinding.secondaryBannerImages.setLayoutManager(layoutManager5);
                    secondBannerAdapter = new SecondBannerAdapter(requireActivity(), bannerImages_List2, 1);
                    fragmentHomeBinding.secondaryBannerImages.setAdapter(secondBannerAdapter);
                } catch (Exception e) {
                    Log.e("TAG", "initViews: " + e);
                    fragmentHomeBinding.secondaryBannerImages.setVisibility(GONE);
                }
            } else {
                secondBannerAdapter = new SecondBannerAdapter(requireActivity(), bannerImages_List2, 1);
                fragmentHomeBinding.secondaryBannerImages.setAdapter(secondBannerAdapter);
                fragmentHomeBinding.secondaryBannerImages.setLayoutManager(new LinearLayoutManager(requireActivity()));
            }
        }

        fragmentHomeBinding.rvCategoriesMain.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        if (SPData.showCategoryInHorizontalScrolling()) {
            fragmentHomeBinding.rvCategories.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        } else
            fragmentHomeBinding.rvCategories.setLayoutManager(new GridLayoutManager(getActivity(), SPData.categorySpanCount()));

        fragmentHomeBinding.topSellingCategoryRv.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));

        if (SPData.showBestSellingProducts()) {
            fragmentHomeBinding.bestSellingProductGrid.setLayoutManager(new CommonUtils(getActivity()).getProductGridLayoutManager());
            bestSellingProductAdapter = new ProductsAdapter(getContext(), bestSellingList, "grid", BEST_SELLING_PRODUCT_REQUEST_CODE);
            fragmentHomeBinding.bestSellingProductGrid.setAdapter(bestSellingProductAdapter);
        }

        if (SPData.showFeaturedProducts()) {
            if (SPData.showFeaturedProductInSlider()) {
                fragmentHomeBinding.featuredProductGrid.setLayoutManager(new LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false));
            } else
                fragmentHomeBinding.featuredProductGrid.setLayoutManager(new CommonUtils(getActivity()).getProductGridLayoutManager());
            featuredProductAdapter = new ProductsAdapter(getContext(), featuredProductsList, "grid", FEATURED_PRODUCTS_REQUEST_CODE);
            fragmentHomeBinding.featuredProductGrid.setAdapter(featuredProductAdapter);
        }


        if (SPData.showPreOrderProducts()) {
            fragmentHomeBinding.rvCategoriesPreorderProducts.setLayoutManager(new CommonUtils(getActivity()).getProductGridLayoutManager());
            preOrderProductAdapter = new ProductsAdapter(requireActivity(), preOrderProductsList, "grid", PRE_ORDER_PRODUCTS_REQUEST_CODE);
            fragmentHomeBinding.rvCategoriesPreorderProducts.setAdapter(preOrderProductAdapter);
        }

        Slider.init(new PicassoImageLoadingService(requireActivity().getApplicationContext()));
    }

    public void autoScroll() {
        final int speedScroll = 1000 * 3;
        handler = new Handler();
        runnable = new Runnable() {
            int count = 0;

            @Override
            public void run() {
                if (count == secondBannerAdapter.getItemCount())
                    count = 0;
                if (count < secondBannerAdapter.getItemCount()) {
                    fragmentHomeBinding.secondaryBannerImages.smoothScrollToPosition(count++);
                    handler.postDelayed(this, speedScroll);
                }
            }
        };
        handler.postDelayed(runnable, speedScroll);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (handler != null) {
            handler.removeCallbacks(runnable);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacks(runnable);
        }
    }

    private void getDeliveryMethods(String pincode) {
        ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());
        apiService.getDeliveryMethods(pincode).enqueue(new Callback<DeliveryMethodBase>() {
            @Override
            public void onResponse(@NotNull Call<DeliveryMethodBase> call, @NotNull retrofit2.Response<DeliveryMethodBase> response) {
                if (response.isSuccessful() && response.body() != null) {
                    DeliveryMethodBase deliveryMethodBase = response.body();
                    ArrayList<DeliveryMethodData> deliveryMethodDataArrayList = deliveryMethodBase.getData();
                    ArrayList<String> availableDays = new ArrayList<>();
                    for (DeliveryMethodData deliveryMethodData : deliveryMethodDataArrayList) {
                        if (deliveryMethodData.getZoneData() != null && deliveryMethodData.getZoneData().size() > 0 && deliveryMethodData.getZoneData().get(0).getZone() != null && deliveryMethodData.getZoneData().get(0).getZone().getDays() != null) {
                            if (!availableDays.contains("Mon") && deliveryMethodData.getZoneData().get(0).getZone().getDays().getMon()) {
                                availableDays.add("Monday");
                            }
                            if (!availableDays.contains("Tue") && deliveryMethodData.getZoneData().get(0).getZone().getDays().getTue()) {
                                availableDays.add("Tuesday");
                            }
                            if (!availableDays.contains("Wed") && deliveryMethodData.getZoneData().get(0).getZone().getDays().getWed()) {
                                availableDays.add("Wednesday");
                            }
                            if (!availableDays.contains("Thu") && deliveryMethodData.getZoneData().get(0).getZone().getDays().getThu()) {
                                availableDays.add("Thursday");
                            }
                            if (!availableDays.contains("Fri") && deliveryMethodData.getZoneData().get(0).getZone().getDays().getFri()) {
                                availableDays.add("Friday");
                            }
                            if (!availableDays.contains("Sat") && deliveryMethodData.getZoneData().get(0).getZone().getDays().getSat()) {
                                availableDays.add("Saturday");
                            }
                            if (!availableDays.contains("Sun") && deliveryMethodData.getZoneData().get(0).getZone().getDays().getSun()) {
                                availableDays.add("Sunday");
                            }
                        }
                    }
                    if (availableDays.size() > 0) {
                        StringBuilder days = new StringBuilder();
                        for (String day : availableDays) {
                            days.append(day).append(",");
                        }
                        fragmentHomeBinding.cardShippingDays.setVisibility(VISIBLE);
                        fragmentHomeBinding.shippingDays.setText("Delivery available on " + CommonUtils.removeLastCharacter(days.toString()));
                    } else {
                        fragmentHomeBinding.cardShippingDays.setVisibility(GONE);
                    }
                } else {
                    fragmentHomeBinding.cardShippingDays.setVisibility(GONE);
                }
            }

            @Override
            public void onFailure(@NotNull Call<DeliveryMethodBase> call, @NotNull Throwable t) {
            }
        });
    }

    private void hideVendors() {
        fragmentHomeBinding.topVendorLl1.setVisibility(GONE);
        fragmentHomeBinding.topVendorLl2.setVisibility(GONE);
    }

    private void hideBrands() {
        fragmentHomeBinding.topBrandsBelow.setVisibility(GONE);
        fragmentHomeBinding.topbrandsRecyclerViewBelow.setVisibility(GONE);
        fragmentHomeBinding.topbrandsRecyclerViewAbove.setVisibility(GONE);
        fragmentHomeBinding.topBrandsAbove.setVisibility(GONE);
    }

    private void getVendors() {
//        ApiService apiService = ApiUtils.getHeaderAPIService();
//        apiService.getAllVendors().enqueue(new Callback<VendorBase>() {
//            @Override
//            public void onResponse(@NotNull Call<VendorBase> call, @NotNull Response<VendorBase> response) {
//                if (response.isSuccessful() && response.body() != null && response.body().getData().size() > 0) {
//                    if (getActivity() != null) {
//                        ArrayList<VendorChild> vendorChildArrayList = response.body().getData();
//                        if (vendorChildArrayList.size() > 10&&SPData.showVendorIn2Rows()) {
//                            if (SPData.showVendorBelowCategory())
//                                fragmentHomeBinding.topvendorRecyclerView2.setLayoutManager(new GridLayoutManager(requireActivity(), 2, RecyclerView.HORIZONTAL, false));
//                            else
//                                fragmentHomeBinding.topvendorRecyclerView.setLayoutManager(new GridLayoutManager(requireActivity(), 2, RecyclerView.HORIZONTAL, false));
//                        }
//                        VendorsAdapter vendorsAdapter = new VendorsAdapter(requireActivity(), vendorChildArrayList);
//                        if (SPData.showVendorBelowCategory()) {
//                            fragmentHomeBinding.topvendorRecyclerView2.setAdapter(vendorsAdapter);
//                        } else
//                            fragmentHomeBinding.topvendorRecyclerView.setAdapter(vendorsAdapter);
//                        vendorsAdapter.notifyDataSetChanged();
//                    }
//                } else {
//                    hideVendors();
//                }
//            }
//
//            @Override
//            public void onFailure(@NotNull Call<VendorBase> call, @NotNull Throwable t) {
//                hideVendors();
//            }
//        });
    }

    private void getBrands() {
        ApiService apiService = ApiUtils.getHeaderAPIService();
        apiService.getAllBrand().enqueue(new Callback<BrandModel>() {
            @Override
            public void onResponse(@NonNull Call<BrandModel> call, @NonNull Response<BrandModel> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData().size() > 0) {
                    brandArrayList.clear();
                    BrandModel brandModel = response.body();
                    brandArrayList = brandModel.getData();
                    BrandsAdapter brandsAdapter = new BrandsAdapter(getActivity(), brandArrayList);
                    brandsAdapter.notifyDataSetChanged();
                    if (SPData.showAboveBrand()) {
                        fragmentHomeBinding.topbrandsRecyclerViewAbove.setAdapter(brandsAdapter);
                    } else {
                        fragmentHomeBinding.topbrandsRecyclerViewBelow.setAdapter(brandsAdapter);
                    }
                } else {
                    hideBrands();
                }
            }

            @Override
            public void onFailure(@NonNull Call<BrandModel> call, @NonNull Throwable t) {
                hideBrands();
                Toast.makeText(getActivity(), "Failed to connect", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void hideFabSubmenu() {
        fragmentHomeBinding.fabSubmenu.setVisibility(View.GONE);
        if (SPData.useCompanyLogoInCall() && CommonUtils.stringIsNotNullAndEmpty(SPData.getLogo()))
            Glide.with(requireActivity()).load(SPData.getBucketUrl() + SPData.getLogo()).into(fragmentHomeBinding.floatingActionButton);
        else if (SPData.showOnlyWhatsAppOnHome()) {
            fragmentHomeBinding.floatingActionButton.setImageDrawable(requireActivity().getResources().getDrawable(R.drawable.ic_whtsapp));
        } else
            fragmentHomeBinding.floatingActionButton.setImageDrawable(requireActivity().getResources().getDrawable(R.drawable.ic_baseline_call_24));
        fabExpanded = false;
    }

    private void showFabSubmenu() {
        fragmentHomeBinding.fabSubmenu.setVisibility(View.VISIBLE);
        fragmentHomeBinding.floatingActionButton.setImageResource(R.drawable.ic_baseline_close_24);
        fabExpanded = true;
    }


    private void getBanners() {
        ApiService apiService = ApiUtils.getHeaderAPIService();
        apiService.getBannerData().enqueue(new Callback<BannerData>() {
            @Override
            public void onResponse(@NonNull Call<BannerData> call, @NonNull Response<BannerData> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData().size() > 0) {
                    BannerData bannerData = response.body();
                    assert bannerData != null;
                    bannerImages_List.clear();
                    bannerImages_List.addAll(bannerData.getData());
                    setupViews(fragmentHomeBinding.bannerSlider1, bannerImages_List);
                } else {
                    fragmentHomeBinding.cardViewBanner1.setVisibility(GONE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<BannerData> call, @NonNull Throwable t) {
                Log.e("TAG", call.toString());
                fragmentHomeBinding.cardViewBanner1.setVisibility(GONE);
                Toast.makeText(SPData.getAppContext(), "Failed to connect", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getBannersAtBottom() {
        ApiService apiService = ApiUtils.getHeaderAPIService();
        apiService.getBannerData3().enqueue(new Callback<BannerData>() {
            @Override
            public void onResponse(@NonNull Call<BannerData> call, @NonNull Response<BannerData> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData().size() > 0) {
                    fragmentHomeBinding.cardViewBanner3.setVisibility(VISIBLE);
                    BannerData bannerData = response.body();
                    assert bannerData != null;
                    bannerImages_List3.clear();
                    bannerImages_List3.addAll(bannerData.getData());
                    setupViews(fragmentHomeBinding.bannerSlider3, bannerImages_List3);
                } else {
                    fragmentHomeBinding.cardViewBanner3.setVisibility(GONE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<BannerData> call, @NonNull Throwable t) {
                Log.e("TAG getBannersAtBottom", call.toString());
                fragmentHomeBinding.cardViewBanner3.setVisibility(GONE);
                Toast.makeText(SPData.getAppContext(), "Failed to connect", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getBanners2() {
        ApiService apiService = ApiUtils.getHeaderAPIService();
        apiService.getBannerData2().enqueue(new Callback<BannerData>() {
            @Override
            public void onResponse(@NonNull Call<BannerData> call, @NonNull Response<BannerData> response) {
                try {
                    if (response.isSuccessful() && response.body() != null && response.body().getData().size() > 0) {
                        BannerData bannerData = response.body();
                        bannerImages_List2.clear();
                        bannerImages_List2.addAll(bannerData.getData());
                        if (SPData.showSecondaryBannerSameAsTop()) {
                            fragmentHomeBinding.cardViewBanner2.setVisibility(VISIBLE);
                            setupViews(fragmentHomeBinding.bannerSlider2, bannerImages_List2);
                        } else {
                            secondBannerAdapter.notifyDataSetChanged();
                            fragmentHomeBinding.cardViewBanner2.setVisibility(GONE);
                            if (SPData.showBannerInSlider() && bannerImages_List2.size() > 0) {
                                autoScroll();
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e("TAG", "onResponse: " + e);
                    fragmentHomeBinding.secondaryBannerImages.setVisibility(GONE);
                }

            }

            @Override
            public void onFailure(@NonNull Call<BannerData> call, @NonNull Throwable t) {
                Log.e("TAG", call.toString());
                Toast.makeText(SPData.getAppContext(), "Failed to connect", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getProductCategories() {
        fragmentHomeBinding.shimmerViewContainer.startShimmerAnimation();
        ApiService apiService = ApiUtils.getHeaderAPIService();
        apiService.getAllProductCategory(SPData.getAppPreferences().getPincode()).enqueue(new Callback<CategoryInfo>() {
            @Override
            public void onResponse(@NonNull Call<CategoryInfo> call, @NonNull Response<CategoryInfo> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData().size() > 0&&getActivity() != null) {
                    fragmentHomeBinding.topCategoryLayout.setVisibility(VISIBLE);
                    fragmentHomeBinding.shimmerViewContainer.setVisibility(GONE);
                    CategoryInfo categoryInfo = response.body();
                    categoryList = categoryInfo.getData();
                    String type = "grid";

                    if (SPData.showCategorySameAsBrand()) {
                        type = "";
                        fragmentHomeBinding.topCategoryLayout.setVisibility(GONE);
                    }

                    if (SPData.showDunzoTypeCategory()) {
                        CategoriesAdapter categoriesAdapterMain;
                        if (categoryList.size() > 4) {
                            categoriesAdapterMain = new CategoriesAdapter(requireActivity(), categoryList.subList(0, 4), getString(R.string.dunzo));
                        } else {
                            categoriesAdapterMain = new CategoriesAdapter(requireActivity(), categoryList, getString(R.string.dunzo));
                        }
                        fragmentHomeBinding.rvCategoriesMain.setAdapter(categoriesAdapterMain);
                        categoriesAdapterMain.notifyDataSetChanged();
                        if (categoryList.size() > 4) {
                            categoryList = categoryList.subList(4, categoryList.size());
                        } else {
                            categoryList.clear();
                        }
                    }
                    if (SPData.showCategoryInHorizontalScrolling()) {
                        type = "grid-horizontal";
                    }
                    if(SPData.showCategoryInCircular()){
                        type = "";
                    }

                    if (categoryList.size() <= SPData.noOfCategories())
                        categoriesAdapter = new CategoriesAdapter(getContext(), categoryList, type);
                    else
                        categoriesAdapter = new CategoriesAdapter(getContext(), categoryList.subList(0, (int) SPData.noOfCategories()), type);

                    if (SPData.showCategorySameAsBrand() && SPData.showAboveBrand()) {
                        fragmentHomeBinding.topbrandsRecyclerViewAbove.setAdapter(categoriesAdapter);
                    } else if (SPData.showCategorySameAsBrand()) {
                        fragmentHomeBinding.topbrandsRecyclerViewBelow.setAdapter(categoriesAdapter);
                    } else
                        fragmentHomeBinding.rvCategories.setAdapter(categoriesAdapter);

                    categoriesAdapter.notifyDataSetChanged();

                } else {
                    fragmentHomeBinding.topCategoryLayout.setVisibility(GONE);
                }

            }

            @Override
            public void onFailure(@NonNull Call<CategoryInfo> call, @NonNull Throwable t) {
                fragmentHomeBinding.topCategoryLayout.setVisibility(GONE);
            }
        });
    }

    private void getTopSellingCategories() {
        ApiService apiService = ApiUtils.getHeaderAPIService();
        apiService.getTopSellingCategory().enqueue(new Callback<CategoryInfo>() {
            @Override
            public void onResponse(@NonNull Call<CategoryInfo> call, @NonNull Response<CategoryInfo> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData().size() > 0) {
                    CategoryInfo categoryInfo = response.body();
                    List<Datum> topSellingCategoryList = categoryInfo.getData();
                    CategoriesAdapter categoriesAdapter = new CategoriesAdapter(getContext(), topSellingCategoryList, "");
                    fragmentHomeBinding.topSellingCategoryRv.setAdapter(categoriesAdapter);
                    categoriesAdapter.notifyDataSetChanged();
                } else {
                    fragmentHomeBinding.topSellingCategoryLayout.setVisibility(GONE);
                }

            }

            @Override
            public void onFailure(@NonNull Call<CategoryInfo> call, @NonNull Throwable t) {
                fragmentHomeBinding.topSellingCategoryLayout.setVisibility(GONE);
            }
        });
    }

    private void getFeaturedCategories() {
        ApiService apiService = ApiUtils.getHeaderAPIService();
        apiService.getAllFeaturedCategory().enqueue(new Callback<CategoryInfo>() {
            @Override
            public void onResponse(@NonNull Call<CategoryInfo> call, @NonNull Response<CategoryInfo> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData().size() > 0) {
                    fragmentHomeBinding.featuredCategoryLayout.setVisibility(VISIBLE);
                    featuredCategoryList.clear();
                    CategoryInfo categoryInfo = response.body();
                    assert categoryInfo != null;
                    featuredCategoryList = categoryInfo.getData();
                    FeaturedCategoriesAdapter featuredCategoriesAdapter = new FeaturedCategoriesAdapter(getContext(), featuredCategoryList);
                    fragmentHomeBinding.featuredCategoryRv.setAdapter(featuredCategoriesAdapter);
                    featuredCategoriesAdapter.notifyDataSetChanged();
                } else {
                    fragmentHomeBinding.featuredCategoryLayout.setVisibility(GONE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<CategoryInfo> call, @NonNull Throwable t) {
                fragmentHomeBinding.featuredCategoryLayout.setVisibility(GONE);
            }
        });
    }

    private void getAllFeaturedProducts() {
        featuredProductsList.clear();
        fragmentHomeBinding.featuredProductShimmerView.startShimmerAnimation();
        ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());
        apiService.getAllFeaturedProducts(SPData.getAppPreferences().getPincode(),"").enqueue(new Callback<VarientsByCategory>() {
            @Override
            public void onResponse(@NonNull Call<VarientsByCategory> call, @NonNull Response<VarientsByCategory> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData().size() > 0) {
                    fragmentHomeBinding.featuredProductLayout.setVisibility(VISIBLE);
                    fragmentHomeBinding.featuredProductShimmerView.setVisibility(GONE);
                    VarientsByCategory productVarients = response.body();
                    List<com.mindfulai.Models.varientsByCategory.Datum> productList = productVarients.getData();
                    if (productList.size() > SPData.noOfFeaturedProducts()) {
                        featuredProductsList.addAll(productVarients.getData().subList(0, SPData.noOfFeaturedProducts()));
                    } else {
                        featuredProductsList.addAll(productList);
                    }
                    if (SPData.useBannersAndCarousel() && productList.size() > featuredProductsList.size()) {
                        topFeaturedProductsList.addAll(productList.subList(featuredProductsList.size(), productList.size()));
                        if (topFeaturedProductsList.size() > 0) {
                            fragmentHomeBinding.topProductLayout.setVisibility(VISIBLE);
                        } else {
                            fragmentHomeBinding.topProductLayout.setVisibility(GONE);
                        }
                        topfeaturedProductAdapter.notifyDataSetChanged();
                    } else {
                        fragmentHomeBinding.topProductLayout.setVisibility(GONE);
                    }
                    featuredProductAdapter.notifyDataSetChanged();
                } else {
                    fragmentHomeBinding.topProductLayout.setVisibility(GONE);
                    fragmentHomeBinding.featuredProductLayout.setVisibility(GONE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<VarientsByCategory> call, @NonNull Throwable t) {
                fragmentHomeBinding.topProductLayout.setVisibility(GONE);
                fragmentHomeBinding.featuredProductLayout.setVisibility(GONE);
            }
        });
    }

    private void getAllBestSelling() {
        bestSellingList.clear();
        fragmentHomeBinding.bestSellingShimmerView2.startShimmerAnimation();
        ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());
        apiService.getAllBestSelling(SPData.getAppPreferences().getPincode()).enqueue(new Callback<VarientsByCategory>() {
            @Override
            public void onResponse(@NonNull Call<VarientsByCategory> call, @NonNull Response<VarientsByCategory> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData().size() > 0) {
                    fragmentHomeBinding.bestSellingLayout.setVisibility(VISIBLE);
                    fragmentHomeBinding.bestSellingShimmerView2.setVisibility(GONE);
                    VarientsByCategory productVarients = response.body();
                    if (productVarients.getData().size() > SPData.noOfFeaturedProducts()) {
                        bestSellingList.addAll(productVarients.getData().subList(0, SPData.noOfFeaturedProducts()));
                    } else {
                        bestSellingList.addAll(productVarients.getData());
                    }
                    bestSellingProductAdapter.notifyDataSetChanged();
                } else {
                    fragmentHomeBinding.bestSellingLayout.setVisibility(GONE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<VarientsByCategory> call, @NonNull Throwable t) {
                Log.e("TAG", "" + t);
                fragmentHomeBinding.bestSellingLayout.setVisibility(GONE);
            }
        });
    }

    private void setupViews(Slider slider, List<BannerInfo> bannerDataArrayList) {
        try {
            MainSliderAdapter mainSliderAdapter = new MainSliderAdapter(getActivity(), bannerDataArrayList);
            slider.setAdapter(mainSliderAdapter);
            slider.setInterval(1000 * 3);
            if (SPData.showIndicatorInBanner()) {
                slider.showIndicators();
            } else {
                slider.hideIndicators();
            }
            slider.setOnSlideClickListener(position -> {
                if (bannerDataArrayList.size() > slider.selectedSlidePosition - 1){
                    BannerInfo bannerInfo = bannerDataArrayList.get(slider.selectedSlidePosition - 1);
                    if (bannerInfo.getTarget() != null) {
                        if (bannerInfo.getType().equals("product_category")) {
                            Intent i = new Intent(getActivity(), AllProductsActivity.class);
                            i.putExtra("category_id", bannerInfo.getTarget().getId());
                            i.putExtra("title", bannerInfo.getTarget().getFullName());
                            startActivityForResult(i, 2);
                        }
                    }else if(bannerInfo.getLink()!=null){
                       CommonUtils.openLink(requireActivity(),bannerInfo.getLink());
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("TAG", "setupViews: " + e);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            int position = data.getIntExtra("position", -1);
            int incart = data.getIntExtra("incart", 0);
            String id = data.getStringExtra("id");
            if (position != -1 && requestCode == FEATURED_PRODUCTS_REQUEST_CODE)
                featuredProductAdapter.updateInCartItem(position, incart, id);
            else if (position != -1 && requestCode == BEST_SELLING_PRODUCT_REQUEST_CODE)
                bestSellingProductAdapter.updateInCartItem(position, incart, id);
            else if (position != -1 && requestCode == TOP_FEATURED_PRODUCTS_REQUEST_CODE)
                topfeaturedProductAdapter.updateInCartItem(position, incart, id);

        }
    }
}