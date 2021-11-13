package com.mindfulai.AppPrefrences;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.mindfulai.Utils.SPData;

/**
 * Created by ankur on 12/23/2016.
 */

public class AppPreferences {

    private static SharedPreferences.Editor mEditor;
    private SharedPreferences mPreferences;


    @SuppressLint("CommitPrefEdits")
    public AppPreferences(Context context) {
        mPreferences = context.getSharedPreferences(PreferenceID.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        mEditor = mPreferences.edit();
    }

    public void setMembershipPurchased(boolean purchased) {
        mEditor.putBoolean(SharedPrefrencesKey.MEMBERSHIP_PURCHASED.toString(), purchased).apply();
    }

    public boolean isMembershipPurchased() {
        return mPreferences.getBoolean(SharedPrefrencesKey.MEMBERSHIP_PURCHASED.toString(), false);
    }

    public void setNotificationList(String notificationList) {
        mEditor.putString(SharedPrefrencesKey.NOTIFICATION_LIST.toString(), notificationList).apply();
    }

    public String getNotificationList() {
        return mPreferences.getString(SharedPrefrencesKey.NOTIFICATION_LIST.toString(), "");
    }

    public String getReferralCode() {
        return mPreferences.getString(SharedPrefrencesKey.UserReferralId.toString(), "");
    }

    public void setReferralCode(String referralId) {
        mEditor.putString(SharedPrefrencesKey.UserReferralId.toString(), referralId).apply();
    }

    public int getNotificationCount() {
        return mPreferences.getInt(SharedPrefrencesKey.NOTIFICATION_COUNT.toString(), 0);
    }

    public void setNotificationCount(int count) {
        mEditor.putInt(SharedPrefrencesKey.NOTIFICATION_COUNT.toString(), count).apply();
    }

    public String getEmail() {
        return mPreferences.getString(SharedPrefrencesKey.UserEmail.toString(), "");
    }

    public void setEmail(String email) {
        mEditor.putString(SharedPrefrencesKey.UserEmail.toString(), email).apply();
    }

    public boolean getPaymentSuccess() {
        return mPreferences.getBoolean(SharedPrefrencesKey.paymentSuccess.toString(), false);
    }

    public void setPaymentSuccess(boolean payment) {
        mEditor.putBoolean(SharedPrefrencesKey.paymentSuccess.toString(), payment);
        mEditor.apply();
    }

    public String getLatitude() {
        return mPreferences.getString(SharedPrefrencesKey.Latitude.toString(), "");
    }

    public void setLatitude(String latitude) {
        mEditor.putString(SharedPrefrencesKey.Latitude.toString(), latitude);
        mEditor.commit();
    }

    public String getLongitude() {
        return mPreferences.getString(SharedPrefrencesKey.Longitude.toString(), "");
    }

    public void setLongitude(String longitude) {
        mEditor.putString(SharedPrefrencesKey.Longitude.toString(), longitude);
        mEditor.commit();
    }

    public void setUser_mobile_no(String user_mobile_no) {
        mEditor.putString(SharedPrefrencesKey.User_mobile_no.toString(), user_mobile_no);
        mEditor.commit();
    }

    public String getMobileNumber() {
        return mPreferences.getString(SharedPrefrencesKey.User_mobile_no.toString(), "");
    }

    public void signInByGoogle(boolean googleSignin) {
        mEditor.putBoolean(SharedPrefrencesKey.GoogleSignIn.toString(), googleSignin);
        mEditor.apply();
    }

    public boolean signInByGoogle() {
        return mPreferences.getBoolean(SharedPrefrencesKey.GoogleSignIn.toString(), false);
    }

    public void signInByFacebook(boolean googleSignin) {
        mEditor.putBoolean(SharedPrefrencesKey.FacebookSignIn.toString(), googleSignin);
        mEditor.apply();
    }

    public boolean signInByFacebook() {
        return mPreferences.getBoolean(SharedPrefrencesKey.FacebookSignIn.toString(), false);
    }

    public void signInByPhone(boolean googleSignin) {
        mEditor.putBoolean(SharedPrefrencesKey.PhoneNoSignIn.toString(), googleSignin);
        mEditor.apply();
    }

    public boolean signInByPhone() {
        return mPreferences.getBoolean(SharedPrefrencesKey.PhoneNoSignIn.toString(), false);
    }

    public String getAddress() {
        return mPreferences.getString(SharedPrefrencesKey.UserAddress.toString(), "");
    }

    public void setAddress(String add) {
        mEditor.putString(SharedPrefrencesKey.UserAddress.toString(), add);
        mEditor.apply();
    }

    public String getUserId() {
        return mPreferences.getString(SharedPrefrencesKey.UserId.toString(), "");
    }

    public void setUserId(String userId) {
        mEditor.putString(SharedPrefrencesKey.UserId.toString(), userId);
        mEditor.apply();
    }

    public String getUsertoken() {
        return mPreferences.getString(SharedPrefrencesKey.Usertoken.toString(), "");
    }

    public void setUsertoken(String usertoken) {
        mEditor.putString(SharedPrefrencesKey.Usertoken.toString(), usertoken);
        mEditor.apply();
    }

    public String getUserUniqueId() {
        return mPreferences.getString(SharedPrefrencesKey.UserUniqueId.toString(), "");
    }

    public void setUserUniqueId(String usertoken) {
        mEditor.putString(SharedPrefrencesKey.UserUniqueId.toString(), usertoken);
        mEditor.apply();
    }

    public int getTotalCartCount() {
        return mPreferences.getInt(SharedPrefrencesKey.UserCart.toString(), 0);
    }

    public void setTotalCartCount(int count) {
        mEditor.putInt(SharedPrefrencesKey.UserCart.toString(), count);
        mEditor.apply();
    }

    public String getUserName() {
        return mPreferences.getString(SharedPrefrencesKey.UserName.toString(), "");
    }

    public void setUserName(String name) {
        mEditor.putString(SharedPrefrencesKey.UserName.toString(), name);
        mEditor.apply();
    }

    public String getUserType() {
        return mPreferences.getString(SharedPrefrencesKey.UserType.toString(), "");
    }

    public void setUserType(String userType) {
        mEditor.putString(SharedPrefrencesKey.UserType.toString(), userType);
        mEditor.apply();
    }

    public String getUserProfilePic() {
        return mPreferences.getString(SharedPrefrencesKey.UserProfilePic.toString(), "");
    }

    public void setUserProfilePic(String userProfilePic) {
        mEditor.putString(SharedPrefrencesKey.UserProfilePic.toString(), userProfilePic);
        mEditor.apply();
    }

    public void setShopInside(String shopInside) {
        mEditor.putString(SharedPrefrencesKey.User_SHOP_INSIDE.toString(), shopInside);
        mEditor.apply();
    }

    public void setShopOutside(String shopOutside) {
        mEditor.putString(SharedPrefrencesKey.User_SHOP_OUTSIDE.toString(), shopOutside);
        mEditor.apply();
    }

    public void setMerchantCard(String merchantCard) {
        mEditor.putString(SharedPrefrencesKey.USER_MERCHANT.toString(), merchantCard);
        mEditor.apply();
    }

    public void setBusinessCard(String businessCard) {
        mEditor.putString(SharedPrefrencesKey.USER_BUSINESS_CARD.toString(), businessCard);
        mEditor.apply();
    }

    public void setOtherImage(String otherImage) {
        mEditor.putString(SharedPrefrencesKey.OTHER.toString(), otherImage);
        mEditor.apply();
    }

    public String getUserAadhar() {
        return mPreferences.getString(SharedPrefrencesKey.User_aadhar.toString(), "");
    }

    public void setUserAadhar(String aadhar) {
        mEditor.putString(SharedPrefrencesKey.User_aadhar.toString(), aadhar);
        mEditor.apply();
    }

    public String getUserPan() {
        return mPreferences.getString(SharedPrefrencesKey.User_pan.toString(), "");
    }

    public void setUserPan(String pan) {
        mEditor.putString(SharedPrefrencesKey.User_pan.toString(), pan);
        mEditor.apply();
    }

    public String getUserCheque() {
        return mPreferences.getString(SharedPrefrencesKey.User_cheque.toString(), "");
    }

    public void setUserCheque(String cheque) {
        mEditor.putString(SharedPrefrencesKey.User_cheque.toString(), cheque);
        mEditor.apply();
    }

    public String getUserShopInside() {
        return mPreferences.getString(SharedPrefrencesKey.User_SHOP_INSIDE.toString(), "");
    }

    public String getUserShopOutside() {
        return mPreferences.getString(SharedPrefrencesKey.User_SHOP_OUTSIDE.toString(), "");
    }

    public String getUserBusinessCard() {
        return mPreferences.getString(SharedPrefrencesKey.USER_BUSINESS_CARD.toString(), "");
    }

    public String getUserMerchant() {
        return mPreferences.getString(SharedPrefrencesKey.USER_MERCHANT.toString(), "");
    }

    public String getUserOther() {
        return mPreferences.getString(SharedPrefrencesKey.OTHER.toString(), "");
    }

    public void setServerUrl(String serverurl) {
        mEditor.putString(SharedPrefrencesKey.ServerURL.toString(), serverurl);
        mEditor.apply();
    }

    public String getServerUrl() {
        return mPreferences.getString(SharedPrefrencesKey.ServerURL.toString(), "");
    }

    public String getBucketUrl() {
        return mPreferences.getString(SharedPrefrencesKey.BUCKETURL.toString(), SPData.getBucketUrl());
    }

    public void setBucketUrl(String serverurl) {
        mEditor.putString(SharedPrefrencesKey.BUCKETURL.toString(), serverurl);
        mEditor.apply();
    }

    public String getPincode() {
        return mPreferences.getString(SharedPrefrencesKey.PINCODE.toString(), "");
    }

    public void setPincode(String pincode) {
        mEditor.putString(SharedPrefrencesKey.PINCODE.toString(), pincode);
        mEditor.apply();
    }

    public double getFirstKgPrice() {
        return mPreferences.getFloat(SharedPrefrencesKey.FIRSTKGPRICE.toString(), 0);
    }

    public void setFirstKgPrice(float price) {
        mEditor.putFloat(SharedPrefrencesKey.FIRSTKGPRICE.toString(), price);
        mEditor.apply();
    }

    public double getAfterFirstKgPrice() {
        return mPreferences.getFloat(SharedPrefrencesKey.AFTERFIRSTKG.toString(), 0);
    }

    public void setAfterFirstKgPrice(float price) {
        mEditor.putFloat(SharedPrefrencesKey.AFTERFIRSTKG.toString(), price);
        mEditor.apply();
    }

    public String getAddressId() {
        return mPreferences.getString(SharedPrefrencesKey.AddressId.toString(), "");
    }

    public void setAddressId(String aid) {
        mEditor.putString(SharedPrefrencesKey.AddressId.toString(), aid);
        mEditor.apply();
    }

    public String getUserShippingName() {
        return mPreferences.getString(SharedPrefrencesKey.USER_SHIPPING_NAME.toString(), "");
    }

    public void setUserShippingName(String shippingUserName) {
        mEditor.putString(SharedPrefrencesKey.USER_SHIPPING_NAME.toString(), shippingUserName);
        mEditor.apply();
    }

    public String getUserShippingAddress() {
        return mPreferences.getString(SharedPrefrencesKey.USER_SHIPPING_ADDRESS.toString(), "");
    }

    public void setUserShippingAddress(String shippingUserAddress) {
        mEditor.putString(SharedPrefrencesKey.USER_SHIPPING_ADDRESS.toString(), shippingUserAddress);
        mEditor.apply();
    }

    public String getUserShippingMobile() {
        return mPreferences.getString(SharedPrefrencesKey.USER_SHIPPING_MOBILE.toString(), "");
    }

    public void setUserShippingMobile(String userShippingMobile) {
        mEditor.putString(SharedPrefrencesKey.USER_SHIPPING_MOBILE.toString(), userShippingMobile);
        mEditor.apply();
    }

    public String getUserShippingCoordinated() {
        return mPreferences.getString(SharedPrefrencesKey.USER_SHIPPING_COORDINATES.toString(), "");
    }

    public void setUserShippingCoordinates(String userShippingCoordinates) {
        mEditor.putString(SharedPrefrencesKey.USER_SHIPPING_COORDINATES.toString(), userShippingCoordinates);
        mEditor.apply();
    }

    public String getCartVendorId() {
        return mPreferences.getString(SharedPrefrencesKey.CART_VENDOR_ID.toString(), "");
    }

    public void setCartVendorId(String vendorId) {
        mEditor.putString(SharedPrefrencesKey.CART_VENDOR_ID.toString(), vendorId);
        mEditor.apply();
    }
    public String getSharedByReferralCode() {
        return mPreferences.getString(SharedPrefrencesKey.SHARED_BY_RC.toString(), "");
    }

    public void setSharedByReferralCode(String vendorId) {
        mEditor.putString(SharedPrefrencesKey.SHARED_BY_RC.toString(), vendorId);
        mEditor.apply();
    }

    public void clearAppPreference() {
        if (mPreferences != null) {
            mEditor.clear().commit();
        }
    }

    public enum SharedPrefrencesKey {
        UserId,
        Usertoken,
        UserType,
        UserAddress,
        UserCart,
        UserName,
        UserEmail,
        UserReferralId,
        User_mobile_no,
        User_aadhar,
        User_pan,
        User_SHOP_INSIDE,
        User_SHOP_OUTSIDE,
        USER_MERCHANT,
        USER_BUSINESS_CARD,
        OTHER,
        User_cheque,
        Latitude,
        Longitude,
        UserProfilePic,
        varient_available,
        varient_stock,
        varient_id,
        varient_minQty,
        paymentSuccess,
        UserUniqueId,
        GoogleSignIn,
        FacebookSignIn,
        PhoneNoSignIn,
        ServerURL,
        BUCKETURL,
        PINCODE,
        FIRSTKGPRICE,
        AFTERFIRSTKG,
        AddressId,
        USER_SHIPPING_NAME,
        USER_SHIPPING_MOBILE,
        USER_SHIPPING_ADDRESS,
        CART_VENDOR_ID,
        USER_SHIPPING_COORDINATES,
        NOTIFICATION_COUNT,
        MEMBERSHIP_PURCHASED,
        NOTIFICATION_LIST,
        SHARED_BY_RC,
    }

    public enum APPCONFIG {
        SERVER_URL,
        AMAZON_URL,
        showAboveBrand,
        whatsAppNumber,
        supportCallNumber,
        emailAddress,
        getTollFreeNumber,
        getShareDomain,
        getInstagram,
        getFacebook,
        getLogo,
        getYoutube,
        getShowVendor,
        showCod,
        showOnlinePay,
        recommendedText,
        certifiedText,
        showCertifiedText,
        showProductsAndCart,
        noOfCategories,
        showBrandOrVendorOnProductList,
        getShowBrandOrVendor,
        showBrand,
        showTimeSlotPicker,
        showReturnReplace,
        getAppShareText,
        getRazorPayKey,
        getRazorPaySecret,
        getRazorPayScreenTitle,
        getRazorPayScreenSubtitle,
        showStaffLogin,
        showVarientDropdown,
        showCaryBag,
        showAllProductsBanner,
        showBottomNavMenu,
        showGridView,
        hideContactBtn,
        showGenuineProductLogo,
        allTimeSlot,
        individualTimeSlot,
        showHomePageBrand,
        showSubscription,
        showFeaturedCategory,
        showViewCart,
        getViewCartTxt,
        showVarientsInDropDownInProductDetail,
        showWholeSalePrice,
        showcartIconAddToCart,
        showGoogleSignInBtn,
        showFacebookSignInBtn,
        askForConfirmationAddToCart,
        cartPageMsgTxt,
        orderHistoryPageMsgTxt,
        showOrderNote,
        showLoginTxt,
        showAppLogoInPlaceofLocation,
        forceToLogin,
        showChangeServerUrlDialog,
        showUploadDocsBtn,
        showOnlineByDefault,
        noOfFeaturedProducts,
        showSocialMediaIcons,
        showProductDetail,
        showFeaturedProducts,
        showBestSellingProducts,
        showShippingMethods,
        showProfileScreenAfterLogin,
        topCategoryText,
        showRatingInProductDetail,
        showReturnableInProductDetail,
        showCategorySliderInMiddle,
        showBothCallAndWhatsAppOnHome,
        showOnlyWhatsAppOnHome,
        showOrderCancelInOrderHistoryDetail,
        showSubcategoryWithTitleOnly,
        showSubcategoryByBrand,
        amountToDeductFromWallet
    }
}
