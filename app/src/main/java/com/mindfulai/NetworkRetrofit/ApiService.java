package com.mindfulai.NetworkRetrofit;

import com.google.gson.JsonObject;
import com.mindfulai.Models.AllOrderHistory.DatumModel;
import com.mindfulai.Models.AllOrderHistory.OrderHistory;
import com.mindfulai.Models.BannerInfoData.BannerCategoryData;
import com.mindfulai.Models.BannerInfoData.BannerData;
import com.mindfulai.Models.BrandModel;
import com.mindfulai.Models.CartInformation.CartDetailsInformation;
import com.mindfulai.Models.CustomerInfo.CustomerData;
import com.mindfulai.Models.DeliveryMethod.DeliveryMethodBase;
import com.mindfulai.Models.ProductVarients.ProductVarients;
import com.mindfulai.Models.ReviewData.ReviewData;
import com.mindfulai.Models.SlotModelBase;
import com.mindfulai.Models.SubcategoryModel.SubcategoryModel;
import com.mindfulai.Models.UserBaseAddress;
import com.mindfulai.Models.VarientById.VarientByIdResponse;
import com.mindfulai.Models.VendorBase;
import com.mindfulai.Models.WalletRechargeModel.WalletRechargeModel;
import com.mindfulai.Models.categoryData.CategoryInfo;
import com.mindfulai.Models.config.ConfigResponse;
import com.mindfulai.Models.coupon.CouponBaseModel;
import com.mindfulai.Models.faq.FaqBase;
import com.mindfulai.Models.flashbanner.FlashBanner;
import com.mindfulai.Models.kwikapimodels.*;
import com.mindfulai.Models.membership.GetMembershipBase;
import com.mindfulai.Models.membership.MembershipBaseModel;
import com.mindfulai.Models.orderDetailInfo.OrderDetailInfo;
import com.mindfulai.Models.payments.PaymentBase;
import com.mindfulai.Models.shiprocket.ShiprocketDC;
import com.mindfulai.Models.society.SocietyBase;
import com.mindfulai.Models.subscription.SubscriptionBaseModel;
import com.mindfulai.Models.varientsByCategory.PageVarientsByCategory;
import com.mindfulai.Models.varientsByCategory.ProductAttributeBase;
import com.mindfulai.Models.varientsByCategory.TopDiscountProductModel;
import com.mindfulai.Models.varientsByCategory.VarientsByCategory;
import com.mindfulai.Utils.SPData;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;


public interface ApiService {

    String ABOUTUS = SPData.getServerUrl() + "/api/aboutus";
    String PRIVACY = SPData.getServerUrl() + "/api/privacypolicy";
    String RETURN_POLICY = SPData.getServerUrl() + "/api/returnpolicy";
    String INSTRUCTION = SPData.getServerUrl() + "/api/instruction";
    String TNC = SPData.getServerUrl() + "/api/tnc";

    String SAVE_SUBSCRIPTION = SPData.getServerUrl() + "/api/subscription/update/";
    String CREATE_SUBSCRIPTION = SPData.getServerUrl() + "/api/subscription/create";
    String UPLOAD_DOCS = SPData.getServerUrl() + "/api/user/update/me2";

    @GET("api/returnpolicy")
    Call<JsonObject> getReturnPolicy();

    @POST("api/address")
    Call<JsonObject> saveAddress(@Body JsonObject jsonObject);

    @PUT("api/address/id/{id}")
    Call<JsonObject> updateAddress(@Path("id") String id,@Body JsonObject jsonObject);


    @POST("api/auth/login/mobile")
    Call<JsonObject> loginmobile(@Body JsonObject jsonObject);

    @POST("api/auth/resendotp")
    Call<JsonObject> resendOtp(@Body JsonObject jsonObject);

    @POST("api/auth/login")
    Call<JsonObject> loginemailPassword(@Body JsonObject jsonObject);

    @POST("api/oauth/authenticate/google/appcallback")
    Call<JsonObject> googleLogin(@Body JsonObject jsonObject);

    @POST("api/auth/verifyotp/customer")
    Call<JsonObject> verifyOtp(@Query("rc") String referralCode, @Body JsonObject jsonObject);

    @POST("api/product/review")
    Call<JsonObject> addReview(@Body JsonObject jsonObject);

    @GET("api/product/review/byproduct/{id}")
    Call<ReviewData> getReview(@Path("id") String id);

    @POST("api/cart")
    Call<JsonObject> addItemToCart(@Body JsonObject jsonObject);

    @POST("api/tempcart")
    Call<JsonObject> addItemToCartWithoutLogin(@Body JsonObject jsonObject);

    @DELETE("api/cart/{id}")
    Call<JsonObject> removeItemFromCart(@Path("id") String id);

    @DELETE("api/tempcart/{id}")
    Call<JsonObject> removeItemFromCartWithoutLogin(@Path("id") String id);

    @GET("api/cart")
    Call<CartDetailsInformation> showCartItems(@Query("pincode") String pincode);

    @GET("api/cart")
    Call<CartDetailsInformation> showCartItems();

    @GET("api/tempcart")
    Call<CartDetailsInformation> showCartItemsWithoutLogin();

    @GET("api/tempcart")
    Call<CartDetailsInformation> showCartItemsWithoutLogin(@Query("pincode") String pincode);

    @POST("api/cart/applycoupon")
    Call<CartDetailsInformation> applyCoupon(@Body JsonObject jsonObject);

    @POST("api/tempcart/applycoupon")
    Call<CartDetailsInformation> applyCouponWithoutLogin(@Body JsonObject jsonObject);

    @POST("api/orders/wishlist")
    Call<JsonObject> addItemToWishlist(@Body JsonObject jsonObject);

    @POST("api/orders/wishlist")
    Call<JsonObject> addItemToWishlistWithoutLogin(@Body JsonObject jsonObject);

    @DELETE("api/orders/wishlist/{id}")
    Call<JsonObject> removeItemFromWishlist(@Path("id") String id);

    @DELETE("api/orders/wishlist/{id}")
    Call<JsonObject> removeItemFromWishlistWithoutLogin(@Path("id") String id);

    @GET("api/auth/me")
    Call<CustomerData> getProfileDetails();

    @PUT("api/cart/{id}")
    Call<CartDetailsInformation> updateCartItem(@Path("id") String id, @Body JsonObject jsonObject);

    @PUT("api/cart/{id}")
    Call<CartDetailsInformation> updateCartItemInCartPage(@Path("id") String id, @Body JsonObject jsonObject);

    @PUT("api/tempcart/{id}")
    Call<CartDetailsInformation> updateCartItemWithoutLogin(@Path("id") String id, @Body JsonObject jsonObject);

    @PUT("api/tempcart/{id}")
    Call<CartDetailsInformation> updateCartItemWithoutLoginInCartPage(@Path("id") String id, @Body JsonObject jsonObject);

    @POST("api/cart/placeorder")
    Call<OrderDetailInfo> PlaceOrder(@Body JsonObject jsonObject);
    @POST("api/corder/payforcodorder/{id}")
    Call<OrderDetailInfo> PayForCodOrder(@Path("id") String id,@Body JsonObject jsonObject);

    //api/public/varients/trending/byvendor/{id}

    @POST("api/cart/placeorder/service")
    Call<OrderDetailInfo> PlaceOrderService(@Body JsonObject jsonObject);

    @GET("api/corder")
    Call<OrderHistory> getOrderHistory(@Query("page") int page);

    @GET("api/banner")
    Call<BannerData> getBannerData();

    @GET("api/banner/category/{id}")
    Call<BannerCategoryData> getCategoryBannerData(@Path("id") String id);

    @GET("api/banner/slider2")
    Call<BannerData> getBannerData2();

    @Multipart
    @POST("api/user/update/me")
    Call<JsonObject> uploadFile(@Part MultipartBody.Part photo,
                                @Part("full_name") RequestBody name,
                                @Part("email") RequestBody email,
                                @Part("mobile_number") RequestBody mobileNumber,
                                @Part("gstin") RequestBody gstin,
                                @Part("alternateNumber") RequestBody alternateNumber);

    @Multipart
    @POST("api/user/update/me")
    Call<JsonObject> updateName(@Part("full_name") RequestBody name,
                                @Part("mobile_number") RequestBody mobileNumber);

//    @GET("api/product/varients/btid/{id}")
//    Call<>

    @GET("api/product/category/all/product")
    Call<CategoryInfo> getAllProductCategory(@Query("pincode") String pincode);

    @GET("api/product/category/feature")
    Call<CategoryInfo> getAllFeaturedCategory();

    @GET("api/product/category/all/service")
    Call<CategoryInfo> getAllServiceCategory();

//    @GET("api/public/varients/trending")
//    Call<VarientsByCategory> getAllFeaturedProducts(@Query("id") String id);

    @GET("api/product/feature/app")
    Call<VarientsByCategory> getAllFeaturedProducts(@Query("pincode") String pincode,@Query("category") String category);

    @GET("api/product/varients/trending")
    Call<VarientsByCategory> getAllBestSelling(@Query("pincode") String pincode);

    @GET("api/orders/wishlist")
    Call<VarientsByCategory> getAllWishlist();

    @GET("api/orders/wishlist")
    Call<VarientsByCategory> getAllWishlistWithoutLogin();

    @GET("api/product/category/subcat/app/{id}")
    Call<SubcategoryModel> getAllSubCategory(@Path("id") String id, @Query("pincode") String pincode);

    @GET("api/product/category/forbrand/{id}/product")
    Call<SubcategoryModel> getAllSubCategoryByBrandID(@Path("id") String id);

    @GET("api/product/brand")
    Call<BrandModel> getAllBrand();

    @GET("api/discount")
    Call<CouponBaseModel> getAlCoupons();

    @GET("api/discount")
    Call<CouponBaseModel> getAlCouponsWithoutLogin();

    @GET("api/product/varients/byid/{id}")
    Call<VarientByIdResponse> getVarientById(@Path("id") String cid);

    @GET("api/product/varients/bycategory/{id}")
    Call<VarientsByCategory> getAllProductsVarients(@Path("id") String cid, @Query("pincode") String pincode);


    @GET("api/product/varients/bycategorypage/{id}")
    Call<PageVarientsByCategory> getAllProductsVarientsByPage(@Path("id") String cid, @Query("page") int page, @Query("pincode") String pincode, @Query("vendor") String vendor, @Query("brand") String brand);

    @GET("api/product/varients/byvendorwithoutpage/{id}")
    Call<VarientsByCategory> getAllProductsVariantsByVendorWithoutPage(@Path("id") String vid, @Query("pincode") String pincode);

    @GET("api/product/varients/byvendor/{id}")
    Call<PageVarientsByCategory> getAllProductsVarientsByVendorByPage(@Path("id") String vid, @Query("page") int page, @Query("pincode") String pincode);

    @GET("api/product/varients/bybrand/{id}")
    Call<PageVarientsByCategory> getAllProductsVariantsByBrand(@Path("id") String cid, @Query("page") int page, @Query("pincode") String pincode);

    @GET("api/product/varients/find")
    Call<VarientsByCategory> getSearchProducts(@Query("query") String query, @Query("pincode") String pincode);

    @GET("api/user/vendor/id/{id}")
    Call<VendorBase> getVendorProfile(@Path("id") String id);

    @POST("api/product/varients/related")
    Call<ProductVarients> getAllrelatedVarients(@Body JsonObject jsonObject);

    @GET("api/address/")
    Call<UserBaseAddress> getUserBaseAddress();

    @GET("api/deliveryslot")
    Call<SlotModelBase> getSlot();

    @POST("api/payment/verify")
    Call<OrderDetailInfo> verifyOnlinePayment(@Body JsonObject jsonObject);

    @POST("api/payment/walletrecharge")
    Call<WalletRechargeModel> walletRecharge(@Body JsonObject jsonObject);

    @POST("api/corder/action")
    Call<DatumModel> orderAction(@Body JsonObject jsonObject);

    @PUT("api/corder/status/{id}")
    Call<JsonObject> cancelOrder(@Path("id") String id, @Body JsonObject jsonObject);

    @POST("api/firebase/notification/add")
    Call<JsonObject> addFCMToken(@Body JsonObject jsonObject);

    @GET("api/config")
    Call<ConfigResponse> getConfig();

    @GET("api/subscription")
    Call<SubscriptionBaseModel> getSubscriptions();

    @GET("api/shippingmethod/pincode/{pincode}")
    Call<DeliveryMethodBase> getDeliveryMethods(@Path("pincode") String pincode);

    @POST("api/oauth/authenticate/facebook/callback")
    Call<JsonObject> facebookLogin(@Body JsonObject jsonObject);

    @GET("api/product/varients/topdiscounted")
    Call<TopDiscountProductModel> getTopDiscountedProducts();

    @GET("api/product/varients/bylink/{id}")
    Call<VarientsByCategory> getVarientsByProductId(@Path("id") String id);

    @GET("api/user/vendor/bycategory/{id}")
    Call<VendorBase> getAllVendors(@Path("id") String id,@Query("topVendor") boolean topVendor);

    @GET("api/user/vendor/bycategory/{id}")
    Call<VendorBase> getVendorByCategoryId(@Path("id") String id);

    @Multipart
    @POST("api/cart/placeordercustom")
    Call<JsonObject> placeCustomOrder(@Part MultipartBody.Part attachment,
                                      @Part("items") RequestBody items,
                                      @Part("address") RequestBody address,
                                      @Part("deliverySlot") RequestBody deliverySlot);

    @Multipart
    @POST("api/cart/placeordercustom")
    Call<JsonObject> placeCustomOrder(
            @Part("items") RequestBody items,
            @Part("address") RequestBody address,
            @Part("deliverySlot") RequestBody deliverySlot);

    @GET("api/aboutus")
    Call<JsonObject> getAppInfo();

    @GET("api/flashbanner")
    Call<FlashBanner> getFlashBanner();

    @GET("api/product/category/byvendor/{id}")
    Call<CategoryInfo> getAllCategoryByVendor(@Path("id") String id);

    @GET("api/product/attributes/category/{id}")
    Call<ProductAttributeBase> getAllProductAttribute(@Path("id") String id);

    @GET("api/faq")
    Call<FaqBase> getAllFaq();

    @PUT("api/subscription/status/{id}")
    Call<JsonObject> changeSubscriptionStatus(@Path("id") String id, @Body JsonObject jsonObject);

    @DELETE("api/address/{id}")
    Call<JsonObject> deleteAddress(@Path("id") String id);

    @GET("api/payment")
    Call<PaymentBase> getPayments(@Query("page") int page,@Query("purpose") String purpose,@Query("type") String type);

    @GET("api/payment/referralcredittotal")
    Call<JsonObject> getTotalReferral();

    @GET("api/pickupaddress")
    Call<UserBaseAddress> getAllShippingCenter();

    @GET("api/cart/dc")
    Call<ShiprocketDC> getShiprocketDeliveryCharge(@Query("pincode") String picode, @Query("paymentMethod") String paymentMethod);

    @GET("api/society")
    Call<SocietyBase> getSociety();

    @PATCH("api/tempcart/clear")
    Call<JsonObject> clearTempCart();

    @PATCH("api/cart/clear")
    Call<JsonObject> clearCart();

    @DELETE("api/subscription/{id}")
    Call<JsonObject> deleteSubscription(@Path("id") String id);

    @GET("api/banner/slider3")
    Call<BannerData> getBannerData3();

    @GET("api/cart/dcdistance")
    Call<ShiprocketDC> getDeliveryChargeByCoordinates(@Query("long") String lon, @Query("lat") String lat);

    @GET("api/product/varients/bykind/pre-order")
    Call<PageVarientsByCategory> getPreOrderProducts(@Query("page") int page);

    @POST("api/cart/placeorder/preorder")
    Call<OrderDetailInfo> PlacePreOrder(@Body JsonObject jsonObject);

    @GET("api/v2/operator_codes.php")
    Call<OperatorCodeBase> getOperatorCode(@Query("api_key") String apiKey);

    @GET("api/v2/recharge.php")
    Call<RechargeResponse> prepaidDthRecharge(@Query("api_key") String apiKey, @Query("number") String number, @Query("opid") String opid, @Query("amount") String amount, @Query("state_code") String stateCode, @Query("order_id") String orderID);

    @GET("api/v2/bills/recharge.php")
    Call<RechargeResponse> postpaidFastagRecharge(@Query("api_key") String apiKey, @Query("number") String number, @Query("opid") String opid, @Query("amount") String amount, @Query("state_code") String stateCode, @Query("order_id") String orderID,@Query("refrence_id") String refId);

    @GET("api/v2/bills/payments.php")
    Call<RechargeResponse> billsRecharge(@Query("api_key") String apiKey, @Query("number") String account, @Query("opid") String opid, @Query("amount") String amount,@Query("state_code") String stateCode, @Query("order_id") String orderID, @Query("mobile") String mobileno,@Query("opt2") String opt2,@Query("refrence_id") String refId);

    @POST("api/payment/walletdeduction")
    Call<JsonObject> deductAmtFromWallet(@Body JsonObject jsonObject);

    @GET("api/counters")
    Call<JsonObject> getKwikOrderID();

    @GET("api/v2/bills/validation.php")
    Call<BillFetchResponse> getBill(@Query("api_key") String apiKey, @Query("number") String number, @Query("opid") String opid, @Query("amount") String amount, @Query("state_code") String stateCode, @Query("order_id") String orderID, @Query("mobile") String mobileno,@Query("opt2") String opt2);

    @GET("api/v2/circle_codes.php")
    Call<CircleCodeBase> getCircleCode(@Query("api_key") String apiKey);

    @Multipart
    @POST("api/v2/recharge_plans.php")
    Call<GetPlanResponse> getPlans(@Part MultipartBody.Part api_key, @Part MultipartBody.Part state_code, @Part MultipartBody.Part opid);

    @GET("api/membership")
    Call<MembershipBaseModel> getMemberships();

    @POST("api/membership/purchase")
    Call<JsonObject> purchaseMembership(@Body JsonObject jsonObject);

    @GET("api/membership/purchase")
    Call<GetMembershipBase> getPurchasedMembership();

    @GET("api/banner/fixed/{type}")
    Call<BannerData> getFixedBanner(@Path("type") String type);

    @GET("api/banner/sliders/{type}")
    Call<BannerData> getCarouselBanner(@Path("type") String type);

    @GET("api/v2/balance.php")
    Call<JsonObject> getBalance(@Query("api_key") String apikey);

    @GET("api/product/category/top")
    Call<CategoryInfo> getTopSellingCategory();

    @GET("api/vendorgallery/vendor/{id}")
    Call<BannerData> getVendorGallery(@Path("id") String id);

}