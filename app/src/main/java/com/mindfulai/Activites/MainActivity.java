package com.mindfulai.Activites;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.play.core.tasks.Task;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mindfulai.Adapter.CategoriesAdapter;
import com.mindfulai.Models.AppInfoModel;
import com.mindfulai.Models.DeliveryMethod.DeliveryMethodBase;
import com.mindfulai.Models.categoryData.CategoryInfo;
import com.mindfulai.Models.categoryData.Datum;
import com.mindfulai.Models.flashbanner.FlashBanner;
import com.mindfulai.Models.membership.MembershipBaseModel;
import com.mindfulai.NetworkRetrofit.ApiService;
import com.mindfulai.NetworkRetrofit.ApiUtils;
import com.mindfulai.Utils.CommonUtils;
import com.mindfulai.Utils.SPData;
import com.mindfulai.customclass.FadeAnimator;
import com.mindfulai.ministore.BuildConfig;
import com.mindfulai.ministore.R;
import com.mindfulai.ministore.databinding.ActivityDrawerBinding;
import com.mindfulai.ui.*;
import com.razorpay.PaymentData;
import com.razorpay.PaymentResultWithDataListener;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.hdodenhof.circleimageview.CircleImageView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, PaymentResultWithDataListener {

    private RamRajHomeFragment homeFrgmant = new RamRajHomeFragment();
    private Home2Fragment home2Fragment;
    private AllCategoriesFragment allCategoriesFragment;
    private OrderHistoryFragment orderHistoryFragment;
    private CartPageFragment cartPageFragment;
    private WishListFragment wishListFragment;
    private MoreFragment moreFragment;
    public WalletFragment walletFragment;
    private SearchFragment searchFragment;
    private TopDiscountProductFragment topDiscountProductFragment;
    private final int[] tabIcons = {
            R.drawable.ic_home_black_24dp,
            R.drawable.ic_view_module_black_24dp,
            R.drawable.ic_cart,
            R.drawable.ic_history,
            R.drawable.ic_baseline_more_horiz_24
    };
    private AppInfoModel appInfoModel;
    private ActivityDrawerBinding binding;
    private ReviewManager reviewManager;
    public String[] msgs = new String[]{"Priority Delivery", "Extra Lower Prices", "Get More Savings", "Free Delivery"};
    private int selectedTab;
    Stack<Integer> pageHistory;
    int currentPage = 0;
    private List<Datum> allCategory;

    @SuppressLint("InflateParams")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // try {
        LayoutInflater layoutInflater = getLayoutInflater();
        layoutInflater.inflate(R.layout.activity_drawer, null);
        binding = ActivityDrawerBinding.inflate(layoutInflater);
        setContentView(binding.getRoot());

        Log.e("TAG", "onCreate: " + SPData.getAppPreferences().getPincode());
        Log.e("TAG", "onCreate: " + SPData.getAppPreferences().getUsertoken());

        reviewManager = ReviewManagerFactory.create(this);

        FadeAnimator animator = new FadeAnimator(binding.mainLayout.buyMembershipTitle, msgs);
        animator.startAnimation();

        pageHistory = new Stack<>();
        pageHistory.push(currentPage);
        setFragment(homeFrgmant);

//        if (SPData.showMembership())
//            getMembership();

        checkSPData();


        handleOnClick();


        selectedTab = getIntent().getIntExtra("tabposition", 0);

//        checkShippingZone();

        getFlashBanner();

        getAppinfo();

        setSocialMediaLinks();

        showRateApp();

        handleReferralsIfAny();

//        } catch (Exception e) {
//            Log.e("TAG", "onCreate: " + e);
//            FirebaseCrashlytics.getInstance().recordException(new Throwable(e));
//        }
    }

    private void handleReferralsIfAny() {
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, pendingDynamicLinkData -> {
                    if (pendingDynamicLinkData != null && pendingDynamicLinkData.getLink() != null && pendingDynamicLinkData.getLink().getQueryParameter("product") != null) {
                        startActivity(new Intent(MainActivity.this, ProductDetailsActivity.class).putExtra("product_id", pendingDynamicLinkData.getLink().getQueryParameter("product")));
                    }
                }).addOnFailureListener(e -> {
            Log.e("TAG", "onFailure: " + e.getMessage());
        });
    }

    private boolean useSecondHomeScreen() {
        return SPData.noOfZones == 0 && SPData.useZoneEnabledDisabledFeature() && !SPData.getAppPreferences().getPincode().isEmpty();
    }

    public void getMembership() {
        ApiService apiService = ApiUtils.getAPIService();
        apiService.getMemberships().enqueue(new Callback<MembershipBaseModel>() {
            @Override
            public void onResponse(@NotNull Call<MembershipBaseModel> call, @NotNull Response<MembershipBaseModel> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData().size() > 0) {
                    double price = response.body().getData().get(0).getPrice();
                    int expiry = response.body().getData().get(0).getExpiry();
                    binding.mainLayout.buyMembershipMsg.setText("Try " + expiry + " days membership at " + getString(R.string.rs) + CommonUtils.getTwoDigitsRoundOffValue(price));
                }
            }

            @Override
            public void onFailure(@NotNull Call<MembershipBaseModel> call, @NotNull Throwable t) {

            }
        });
    }

    public void showRateApp() {
        Task<ReviewInfo> request = reviewManager.requestReviewFlow();
        request.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ReviewInfo reviewInfo = task.getResult();
                Task<Void> flow = reviewManager.launchReviewFlow(this, reviewInfo);
                flow.addOnCompleteListener(task1 -> {
                });
            }
        });
    }

    private void handleOnClick() {
        binding.mainLayout.referEarn.setOnClickListener(v -> generateShareLink());
        binding.mainLayout.profileBadge.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ProfileActivity.class)));
        binding.mainLayout.homeUserLocation.setOnClickListener(v -> locationPicker());
        binding.mainLayout.notificationBadge.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, CommonActivity.class).putExtra("show", "notification")));
        binding.mainLayout.cartBadge.setOnClickListener(v -> startActivityForResult(new Intent(MainActivity.this, CartPageActivity.class), 10));
        binding.mainLayout.mainBottomLayout.tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                chooseTab(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        binding.mainLayout.buyMembership.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, CheckMembershipActivity.class));
        });
    }


    private void checkSPData() {

         checkMembership();

//        if (SPData.showWalletInPlaceOfCategory()) {
//            tabIcons[1] = R.drawable.ic_account_balance_wallet_black_24dp;
//        } else if (SPData.showSearchInBottomNav()) {
//            tabIcons[1] = R.drawable.ic_search_white;
//        } else {
//            tabIcons[1] = R.drawable.ic_view_module_black_24dp;
//        }
//
//        if (SPData.showFavouriteInBottomNav()) {
//            tabIcons[2] = R.drawable.ic_baseline_favorite_border_24;
//        } else
//            tabIcons[2] = R.drawable.ic_cart;
//
//        if (SPData.showTopDiscountProductInBottomNav()) {
//            tabIcons[3] = R.drawable.ic_discount;
//        } else {
//            tabIcons[3] = R.drawable.ic_history;
//        }

//        if (SPData.showSearchInMainToolbar()) {
//            binding.mainLayout.search.setVisibility(VISIBLE);
//        } else {
//            binding.mainLayout.search.setVisibility(GONE);
//        }

        if (SPData.showProieOptionAtTop()) {
            binding.mainLayout.profileBadge.setVisibility(VISIBLE);
        } else
            binding.mainLayout.profileBadge.setVisibility(GONE);

//        if (SPData.showAppLogoInPlaceofLocation()) {
//            binding.mainLayout.locationLayout.setVisibility(GONE);
//            binding.mainLayout.companyLogo.setVisibility(VISIBLE);
//        } else {
//            binding.mainLayout.locationLayout.setVisibility(VISIBLE);
//            binding.mainLayout.companyLogo.setVisibility(GONE);
//        }

        if (SPData.showBothMenu()) {
            configureSideMenu();
            getProductCategories();
        }
//        else if (!SPData.showBottomNavMenu()) {
//            binding.mainLayout.mainBottomLayout.appbar.setVisibility(GONE);
//            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
//                    RelativeLayout.LayoutParams.MATCH_PARENT,
//                    RelativeLayout.LayoutParams.WRAP_CONTENT
//            );
//            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
//            int px = (int) TypedValue.applyDimension(
//                    TypedValue.COMPLEX_UNIT_DIP,
//                    0,
//                    getResources().getDisplayMetrics()
//            );
//            params.setMargins(0, 0, 0, px);
//            binding.mainLayout.membershipLayout.setLayoutParams(params);
//            configureSideMenu();
//        } else {
//            binding.mainLayout.sideMenu.setVisibility(GONE);
//            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
//            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
//            int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 70, getResources().getDisplayMetrics());
//            params.setMargins(0, 0, 0, px);
//            binding.mainLayout.membershipLayout.setLayoutParams(params);
//            configureBottomNavMenu();
//        }


        binding.mainLayout.notificationBadge.setVisibility(VISIBLE);
        if ((SPData.showcartIconAddToCart() && SPData.showProductsAndCart()) && (!SPData.showBottomNavMenu() || SPData.showFavouriteInBottomNav())) {
            binding.mainLayout.cartBadge.setVisibility(VISIBLE);
        } else {
            binding.mainLayout.cartBadge.setVisibility(GONE);
        }

    }

    private void configureSideMenu() {
        binding.mainLayout.sideMenu.setOnClickListener(view -> binding.drawerLayout.openDrawer(Gravity.LEFT));
        binding.navigationView.setNavigationItemSelectedListener(this);
        binding.mainLayout.sideMenu.setVisibility(VISIBLE);
        binding.navigationView.getMenu().findItem(R.id.join_us).setVisible(SPData.useVendorPackageName());
        binding.navigationView.getMenu().findItem(R.id.nav_membership).setVisible(SPData.showMembership());
        binding.navigationView.getMenu().findItem(R.id.nav_website).setVisible(SPData.useWebsiteLink());
    }

    private void checkMembership() {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.BELOW, R.id.app_bar_layout);
        params.addRule(RelativeLayout.ABOVE, R.id.main_bottom_layout);
        binding.mainLayout.framelayoutContainer.setLayoutParams(params);
        binding.mainLayout.membershipLayout.setVisibility(GONE);
    }

    private void chooseTab(int position) {
        selectedTab = position;
        binding.mainLayout.mainBottomLayout.tabs.selectTab(binding.mainLayout.mainBottomLayout.tabs.getTabAt(position));
        currentPage = position;
        if (currentPage != pageHistory.lastElement())
            pageHistory.push(currentPage);
        homeFrgmant.loadHomePage(allCategory.get(position).getId());
    }

    @Override
    public void onBackPressed() {
        if (pageHistory.size() <= 1) {
            finish();
        } else {
            pageHistory.pop();
            chooseTab(pageHistory.lastElement());
        }
    }

    private void getProductCategories() {
        ApiService apiService = ApiUtils.getHeaderAPIService();
        apiService.getAllProductCategory(SPData.getAppPreferences().getPincode()).enqueue(new Callback<CategoryInfo>() {
            @Override
            public void onResponse(@NonNull Call<CategoryInfo> call, @NonNull Response<CategoryInfo> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData().size() > 0) {
                    binding.mainLayout.mainBottomLayout.tabs.setVisibility(VISIBLE);
                    binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                    int index = 0;
                    allCategory = response.body().getData();
                    for (Datum category : allCategory) {
                        Drawable drawable;
                        if (index == 0) {
                            drawable = getDrawable(R.drawable.nav_bottom_veg);
                        } else if (index == 1) {
                            drawable = getDrawable(R.drawable.bottom_nav_nonveg);
                        } else if (index == 2) {
                            drawable = getDrawable(R.drawable.bottom_nev_fastfood);
                        } else {
                            drawable = getDrawable(R.drawable.ic_baseline_more_horiz_24);
                        }
                        binding.mainLayout.mainBottomLayout.tabs.addTab(binding.mainLayout.mainBottomLayout.tabs.newTab().setIcon(drawable).setText(category.getName()));
                        index++;
                    }
                    chooseTab(0);
                } else {
                    binding.mainLayout.mainBottomLayout.tabs.setVisibility(GONE);
                }

            }

            @Override
            public void onFailure(@NonNull Call<CategoryInfo> call, @NonNull Throwable t) {
                binding.mainLayout.mainBottomLayout.tabs.setVisibility(GONE);
            }
        });
    }

    private void setSocialMediaLinks() {
        if (!SPData.showBottomNavMenu() || SPData.showBothMenu()) {
            Menu menu = binding.navigationView.getMenu();
            ImageView whatsApp = menu.findItem(R.id.item_social_media).getActionView().findViewById(R.id.nav_whatsapp);
            ImageView facebook = menu.findItem(R.id.item_social_media).getActionView().findViewById(R.id.nav_facebook);
            ImageView youtube = menu.findItem(R.id.item_social_media).getActionView().findViewById(R.id.nav_youtube);
            ImageView instagram = menu.findItem(R.id.item_social_media).getActionView().findViewById(R.id.nav_instagram);
            ImageView navGmail = menu.findItem(R.id.item_social_media).getActionView().findViewById(R.id.nav_gmail);
            whatsApp.setOnClickListener(view -> {
                CommonUtils.sendToWhatsApp(MainActivity.this);
            });
            facebook.setOnClickListener(view -> {
                CommonUtils.newFacebookIntent(MainActivity.this);
            });
            youtube.setOnClickListener(view -> {
                CommonUtils.watchYoutubeVideo(MainActivity.this);
            });
            instagram.setOnClickListener(view -> {
                CommonUtils.openInstagram(MainActivity.this);
            });
            navGmail.setOnClickListener(view -> {
                CommonUtils.sendToGmail(MainActivity.this, "Bug/Issue in " + getString(R.string.app_name), SPData.emailAddress());
            });
        }
    }

    private void loadImage(String url, ImageView destination) {
        Glide.with(this).setDefaultRequestOptions(CommonUtils.getRequestOption())
                .asBitmap().load(SPData.getBucketUrl() + url).into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap bitmap,
                                        Transition<? super Bitmap> transition) {
                destination.setImageBitmap(bitmap);
            }
        });
    }

    private void setFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.framelayout_container, fragment).commit();
    }

    private void showFlashDeal(String id, String name, String type, String flashBannerId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_flash_deal, null);
        builder.setView(view);
        ImageView close = view.findViewById(R.id.close);
        ProgressBar progressBar = view.findViewById(R.id.progressBar);
        ImageView imgDiscount = view.findViewById(R.id.img_discount);
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        close.setOnClickListener(view1 -> dialog.dismiss());
        imgDiscount.setOnClickListener(v -> {
            Log.e("TAG", "onClick: " + name);
            if (type != null && type.equals("product_category")) {
                startActivity(new Intent(MainActivity.this, AllProductsActivity.class)
                        .putExtra("category_id", id)
                        .putExtra("title", name)
                );
            }
        });
        Glide.with(this)
                .load(SPData.getBucketUrl() + flashBannerId).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                dialog.dismiss();
                Log.e("TAG", "onLoadFailed: " + e);
                return true;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                progressBar.setVisibility(GONE);
                return false;
            }
        }).into(imgDiscount);
        close.setVisibility(VISIBLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();
        if (SPData.allowAutoCanceFlashBanner()) {
            final Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(dialog::dismiss, 5000);
        }
    }

    private void getFlashBanner() {
        ApiService apiService = ApiUtils.getAPIService();
        apiService.getFlashBanner().enqueue(new Callback<FlashBanner>() {
            @Override
            public void onResponse(@NotNull Call<FlashBanner> call, @NotNull Response<FlashBanner> response) {
                try {
                    if (response.isSuccessful() && response.body() != null && response.body().getData() != null && response.body().getData().size() > 0) {
                        String flashBannerId = response.body().getData().get(0).getImage();
                        String id = "";
                        String name = "";
                        if (response.body().getData().get(0).getTarget() != null) {
                            id = response.body().getData().get(0).getTarget().get_id();
                            name = response.body().getData().get(0).getTarget().getName();
                        }
                        String type = response.body().getData().get(0).getType();
                        showFlashDeal(id, name, type, flashBannerId);
                    }
                } catch (Exception e) {
                    Log.e("TAG", "onResponse: " + e);
                }
            }

            @Override
            public void onFailure(@NotNull Call<FlashBanner> call, @NotNull Throwable t) {

            }
        });
    }

    private void locationPicker() {
        int SELECT_ADDRESS_CODE = 435;
        startActivityForResult(
                new Intent(MainActivity.this, AddressSelectorActivity.class),
                SELECT_ADDRESS_CODE
        );
    }

    private void getDeliveryMethods(String pincode) {
        ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());
        apiService.getDeliveryMethods(pincode).enqueue(new Callback<DeliveryMethodBase>() {
            @Override
            public void onResponse(@NotNull Call<DeliveryMethodBase> call, @NotNull retrofit2.Response<DeliveryMethodBase> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    SPData.noOfZones = response.body().getData().size();
                } else
                    SPData.noOfZones = 1;
                //setHomeScreen();
            }

            @Override
            public void onFailure(@NotNull Call<DeliveryMethodBase> call, @NotNull Throwable t) {
                SPData.noOfZones = 1;
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (searchFragment != null)
            searchFragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onResume() {
        try {
            super.onResume();

            if (!SPData.getAppPreferences().getAddress().isEmpty()) {
                binding.mainLayout.homeUserLocation.setText(SPData.getAppPreferences().getAddress());
            } else {
                binding.mainLayout.homeUserLocation.setText("Select location");
            }

            if (!SPData.getAppPreferences().getPincode().equals(SPData.selectedPincode) || SPData.loginlogout) {
                SPData.selectedPincode = SPData.getAppPreferences().getPincode();
                if (SPData.loginlogout) {
                    SPData.loginlogout = false;
                    checkMembership();
                }
                checkShippingZone();
            }
            setCartBadge();
            binding.mainLayout.notificationBadge.setBadgeValue(SPData.getAppPreferences().getNotificationCount());
            if (SPData.showReferAndEarnOnTop() && !SPData.getAppPreferences().getUsertoken().isEmpty())
                binding.mainLayout.referEarn.setVisibility(VISIBLE);
            else
                binding.mainLayout.referEarn.setVisibility(GONE);

            if (!SPData.showBottomNavMenu() || SPData.showBothMenu()) {
                Menu menu = binding.navigationView.getMenu();
                TextView name = binding.navigationView.getHeaderView(0).findViewById(R.id.uname);
                TextView phone = binding.navigationView.getHeaderView(0).findViewById(R.id.uphone);
                CircleImageView profile = binding.navigationView.getHeaderView(0).findViewById(R.id.userthumbimage);

                phone.setOnClickListener(v -> {
                    if (SPData.getAppPreferences().getUsertoken().isEmpty() && SPData.showLoginTxt())
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                });


                boolean login = !SPData.getAppPreferences().getUsertoken().isEmpty();
                menu.findItem(R.id.nav_logout).setVisible(login);
                menu.findItem(R.id.nav_wallet).setVisible(login);
                menu.findItem(R.id.nav_home).setVisible(login);
                menu.findItem(R.id.nav_membership).setVisible(SPData.showMembership() && login);
                menu.findItem(R.id.nav_wishlist).setVisible(login);
                menu.findItem(R.id.nav_subscriptions).setVisible(SPData.showSubscription() && login);
                menu.findItem(R.id.item_social_media).setVisible(SPData.showSocialMediaIcons());
                menu.findItem(R.id.nav_share).setVisible(login);
                menu.findItem(R.id.nav_order_history).setVisible(SPData.showcartIconAddToCart() && login);
                if (!SPData.getAppPreferences().getUsertoken().isEmpty()) {


                    if (!SPData.getAppPreferences().getMobileNumber().isEmpty())
                        phone.setText(SPData.getAppPreferences().getMobileNumber());
                    else
                        phone.setText(SPData.getAppPreferences().getEmail());

                    if (SPData.getAppPreferences().getUserName().isEmpty())
                        name.setText("N/A");
                    else
                        name.setText(CommonUtils.capitalizeWord(SPData.getAppPreferences().getUserName()));

                    if (!SPData.getAppPreferences().getUserProfilePic().equals("")) {
                        Glide.with(MainActivity.this).setDefaultRequestOptions(CommonUtils.getRequestOption()).load(SPData.getBucketUrl() + SPData.getAppPreferences().getUserProfilePic()).into(profile);
                    } else
                        profile.setImageDrawable(getResources().getDrawable(R.drawable.user));
                } else {
                    if (SPData.showLoginTxt())
                        phone.setText("Login");
                    else
                        phone.setVisibility(GONE);

                    name.setText(getString(R.string.app_name));

                    Glide.with(MainActivity.this).setDefaultRequestOptions(CommonUtils.getRequestOption()).load(SPData.getBucketUrl() + SPData.getLogo()).into(profile);
                }
                int total = SPData.getAppPreferences().getTotalCartCount();
                binding.mainLayout.cartBadge.setBadgeValue(Math.max(total, 0));
            } else {
                int total = SPData.getAppPreferences().getTotalCartCount();
                if (total != -1) {
                    binding.mainLayout.cartBadge.setBadgeValue(Math.max(total, 0));
                }
            }
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(new Throwable(e));
            Log.e("TAG", "onResume: " + e);
        }
    }

    private void checkShippingZone() {
        if (SPData.useZoneEnabledDisabledFeature() && !SPData.getAppPreferences().getPincode().isEmpty()) {
            getDeliveryMethods(SPData.getAppPreferences().getPincode());
        } else {
            SPData.noOfZones = 1;
            //  setHomeScreen();
        }
    }

    public void setCartBadge() {
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == 3) {
                orderHistoryFragment.onActivityResult(requestCode, resultCode, data);
            } else if (homeFrgmant != null) {
                homeFrgmant.onActivityResult(requestCode, resultCode, data);
            }
        } catch (Exception e) {
            e.printStackTrace();
            FirebaseCrashlytics.getInstance().recordException(new Throwable(e));
        }
    }

    @Override
    public void onPaymentError(int i, String s, PaymentData paymentData) {

    }

    @Override
    public void onPaymentSuccess(String s, PaymentData paymentData) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("paymentId", paymentData.getPaymentId());
        jsonObject.addProperty("orderId", paymentData.getOrderId());
        jsonObject.addProperty("signature", paymentData.getSignature());
        if (walletFragment != null) {
            walletFragment.walletRechargeFragment.doPayment.verifyPayment(jsonObject);
        }
    }

    public void addBadge(String count) {
        binding.mainLayout.cartBadge.setBadgeValue(Integer.parseInt(count));
        setCartBadge();
    }

    public void removeBadge() {
        binding.mainLayout.cartBadge.setBadgeValue(0);
        setCartBadge();
    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();
        switch (id) {
            case R.id.nav_home:
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                break;
            case R.id.nav_order_history:
                startActivity(new Intent(MainActivity.this, OrderHistoryActivity.class));
                break;
            case R.id.nav_wallet:
                startActivity(new Intent(MainActivity.this, WalletActivity.class));
                break;
            case R.id.nav_membership:
                startActivity(new Intent(MainActivity.this, PurchasedMembershipActivity.class));
                break;
            case R.id.nav_wishlist:
                startActivity(new Intent(MainActivity.this, CommonActivity.class).putExtra("show", "wish"));
                break;
            case R.id.nav_notification:
                startActivity(new Intent(MainActivity.this, CommonActivity.class).putExtra("show", "notification"));
                break;
            case R.id.nav_about:
                startActivity(new Intent(MainActivity.this, PrivacyPolicy.class).putExtra("type", ApiService.ABOUTUS));
                break;
            case R.id.nav_share:
                generateShareLink();
                break;
            case R.id.nav_website:
                CommonUtils.openWebsite(MainActivity.this);
                break;
            case R.id.join_us:
                CommonUtils.rateApp(SPData.vendorPackageName(), MainActivity.this);
                break;
            case R.id.nav_rate:
                CommonUtils.rateApp(getPackageName(), MainActivity.this);
                break;
            case R.id.nav_gmail:
                CommonUtils.sendToGmail(MainActivity.this, "Info/Help regarding " + getString(R.string.app_name), SPData.emailAddress());
            case R.id.nav_contact:
                CommonUtils.sendToGmail(MainActivity.this, "Info/Help regarding " + getString(R.string.app_name), SPData.emailAddress());
                break;
            case R.id.nav_faq:
                startActivity(new Intent(MainActivity.this, FAQActivity.class));
                break;
            case R.id.report_problem:
                CommonUtils.sendToGmail(MainActivity.this, "Bug/Issue in " + getString(R.string.app_name), SPData.emailAddress());
                break;
            case R.id.nav_tnc:
                startActivity(new Intent(MainActivity.this, PrivacyPolicy.class).putExtra("type", ApiService.TNC));
                break;
            case R.id.nav_privacy:
                startActivity(new Intent(MainActivity.this, PrivacyPolicy.class).putExtra("type", ApiService.PRIVACY));
                break;
            case R.id.nav_return_privacy:
                startActivity(new Intent(MainActivity.this, PrivacyPolicy.class).putExtra("type", ApiService.RETURN_POLICY));
                break;
            case R.id.nav_subscriptions:
                startActivity(new Intent(MainActivity.this, SubscriptionActivity.class));
                break;
            case R.id.nav_top_products:
                startActivity(new Intent(MainActivity.this, TopProductsActivity.class));
                break;
            case R.id.nav_payment_history:
                startActivity(new Intent(MainActivity.this, PaymentHistoryActivity.class));
                break;
            case R.id.nav_logout:
                showPopup();
                break;

        }
        return true;
    }

    private void generateShareLink() {
        binding.drawerLayout.closeDrawers();
        ShareAppBottomSheet bottomSheet = new ShareAppBottomSheet();
        bottomSheet.show(getSupportFragmentManager(), "ShareBS");
    }

    private void showPopup() {
        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
        alert.setMessage("Are you sure you want to Logout?")
                .setPositiveButton("Logout", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        new CommonUtils(MainActivity.this).logout();
                    }
                }).setNegativeButton("Cancel", null);

        AlertDialog alert1 = alert.create();
        alert1.show();
    }

    public void openSearchActivity(View view) {
        startActivityForResult(new Intent(MainActivity.this, SearchPrdouctActivity.class), 10);
    }

    private void getAppinfo() {
        ApiService apiService = ApiUtils.getAPIService();
        apiService.getAppInfo().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NotNull Call<JsonObject> call, @NotNull Response<JsonObject> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        Gson gson = new Gson();
                        JsonObject jsonObject = response.body();
                        if (jsonObject.getAsJsonObject("data") != null) {
                            appInfoModel = gson.fromJson(jsonObject.getAsJsonObject("data").toString(), AppInfoModel.class);
                            setAppInfo();
                        }
                    }
                } catch (Exception e) {
                    Log.e("TAG", "onResponse: " + e);
                }
            }

            @Override
            public void onFailure(@NotNull Call<JsonObject> call, @NotNull Throwable t) {
                binding.mainLayout.framelayoutContainer.setVisibility(GONE);
                binding.mainLayout.noProductLayout.setVisibility(VISIBLE);
            }
        });
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void setAppInfo() {
        if (appInfoModel != null) {
            SPData.email = appInfoModel.getEmail();
            SPData.mobileNumber = appInfoModel.getMobile();
            SPData.address = appInfoModel.getAddress();

            if (appInfoModel.getLogo() != null) {
                SPData.logo = appInfoModel.getLogo();
            }
            Log.e("TAG", "setAppInfo: " + appInfoModel.getLogo());

            if (appInfoModel.getTollFreeNumber() != null)
                SPData.tollFreeNumber = appInfoModel.getTollFreeNumber();

            if (appInfoModel.getLinkedIn() != null)
                SPData.linkedIn = appInfoModel.getLinkedIn();

            if (appInfoModel.getFacebook() != null && !appInfoModel.getFacebook().isEmpty()) {
                SPData.facebook = appInfoModel.getFacebook();
                binding.navigationView.getMenu().findItem(R.id.item_social_media).getActionView().findViewById(R.id.nav_facebook).setVisibility(VISIBLE);
                binding.navigationView.getMenu().findItem(R.id.item_social_media).getActionView().findViewById(R.id.nav_facebook).setEnabled(true);
            } else {
                binding.navigationView.getMenu().findItem(R.id.item_social_media).getActionView().findViewById(R.id.nav_facebook).setEnabled(false);
                binding.navigationView.getMenu().findItem(R.id.item_social_media).getActionView().findViewById(R.id.nav_facebook).setVisibility(View.INVISIBLE);
            }

            if (appInfoModel.getYoutube() != null && !appInfoModel.getYoutube().isEmpty()) {
                SPData.youtube = appInfoModel.getYoutube();
                binding.navigationView.getMenu().findItem(R.id.item_social_media).getActionView().findViewById(R.id.nav_youtube).setVisibility(VISIBLE);
                binding.navigationView.getMenu().findItem(R.id.item_social_media).getActionView().findViewById(R.id.nav_youtube).setEnabled(true);
            } else {
                binding.navigationView.getMenu().findItem(R.id.item_social_media).getActionView().findViewById(R.id.nav_youtube).setEnabled(false);
                binding.navigationView.getMenu().findItem(R.id.item_social_media).getActionView().findViewById(R.id.nav_youtube).setVisibility(View.INVISIBLE);
            }
            if (appInfoModel.getInstagram() != null && !appInfoModel.getInstagram().isEmpty()) {
                SPData.instagram = appInfoModel.getInstagram();
                binding.navigationView.getMenu().findItem(R.id.item_social_media).getActionView().findViewById(R.id.nav_instagram).setVisibility(VISIBLE);
                binding.navigationView.getMenu().findItem(R.id.item_social_media).getActionView().findViewById(R.id.nav_instagram).setEnabled(true);
            } else {
                binding.navigationView.getMenu().findItem(R.id.item_social_media).getActionView().findViewById(R.id.nav_instagram).setEnabled(false);
                binding.navigationView.getMenu().findItem(R.id.item_social_media).getActionView().findViewById(R.id.nav_instagram).setVisibility(View.INVISIBLE);
            }
            if (appInfoModel.getWhatsapp() != null && !appInfoModel.getWhatsapp().isEmpty()) {
                SPData.whatsapp = appInfoModel.getWhatsapp();
                binding.navigationView.getMenu().findItem(R.id.item_social_media).getActionView().findViewById(R.id.nav_whatsapp).setVisibility(VISIBLE);
                binding.navigationView.getMenu().findItem(R.id.item_social_media).getActionView().findViewById(R.id.nav_whatsapp).setEnabled(true);
            } else {
                binding.navigationView.getMenu().findItem(R.id.item_social_media).getActionView().findViewById(R.id.nav_whatsapp).setEnabled(false);
                binding.navigationView.getMenu().findItem(R.id.item_social_media).getActionView().findViewById(R.id.nav_whatsapp).setVisibility(View.INVISIBLE);
            }

            SPData.companyName = appInfoModel.getCompanyName();

            if (SPData.showAppLogoInPlaceofLocation()) {
                loadImage(SPData.getLogo(), binding.mainLayout.companyLogo);
            }
            if (!SPData.showBottomNavMenu() || SPData.showBothMenu()) {
                CircleImageView profile = binding.navigationView.getHeaderView(0).findViewById(R.id.userthumbimage);
                if (SPData.getAppPreferences().getUsertoken().isEmpty()) {
                    loadImage(SPData.getLogo(), profile);
                } else if (!SPData.getAppPreferences().getUserProfilePic().equals("")) {
                    loadImage(SPData.getAppPreferences().getUserProfilePic(), profile);
                } else
                    profile.setImageDrawable(getResources().getDrawable(R.drawable.user));
            }

            checkForAppUpdate();
        }
    }

    private void checkForAppUpdate() {
        try {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());
            executor.execute(() -> {
                String newVersion = "0";
                //Background work here
                try {
                    Document document = Jsoup.connect("https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID + "&hl=en")
                            .timeout(2000)
                            .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                            .referrer("http://www.google.com")
                            .get();
                    if (document != null) {
                        Elements element = document.getElementsContainingOwnText("Current Version");
                        for (Element ele : element) {
                            if (ele.siblingElements() != null) {
                                Elements sibElemets = ele.siblingElements();
                                for (Element sibElemet : sibElemets) {
                                    newVersion = sibElemet.text();
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                String finalNewVersion = newVersion;
                handler.post(() -> {
                    //UI Thread work here
                    try {
                        CommonUtils.checkForAppUpdate(MainActivity.this, Double.parseDouble(finalNewVersion));
                    } catch (Exception e) {
                        Log.e("TAG", "checkForAppUpdate: " + e);
                    }
                });
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("TAG", "setAppInfo: " + e);
        }
    }
}