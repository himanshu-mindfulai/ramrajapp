package com.mindfulai.Activites;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.flatdialoglibrary.dialog.FlatDialog;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginBehavior;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.CredentialPickerConfig;
import com.google.android.gms.auth.api.credentials.HintRequest;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mindfulai.AppPrefrences.AppPreferences;
import com.mindfulai.Models.CartInformation.CartDetailsInformation;
import com.mindfulai.Models.CartInformation.Product;
import com.mindfulai.Models.LoginData.Data;
import com.mindfulai.Models.LoginData.User_;
import com.mindfulai.Models.UserBaseAddress;
import com.mindfulai.Models.UserDataAddress;
import com.mindfulai.Models.membership.GetMembershipBase;
import com.mindfulai.NetworkRetrofit.ApiService;
import com.mindfulai.NetworkRetrofit.ApiUtils;
import com.mindfulai.Utils.CommonUtils;
import com.mindfulai.Utils.CustomProgressDialog;
import com.mindfulai.Utils.SPData;
import com.mindfulai.ministore.R;
import com.mindfulai.ministore.databinding.ActivityLoginBinding;
import com.mindfulai.otp.AutoDetectOTP;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private static final int RESOLVE_HINT = 1000;
    private static final int RC_SIGN_IN = 9001;
    private AppPreferences appPreferences;
    private AutoDetectOTP autoDetectOTP;
    private String firebasetoken = "";
    private GoogleSignInClient mGoogleSignInClient;
    private CustomProgressDialog customProgressDialog;
    private CallbackManager mCallbackManager;
    private ActivityLoginBinding binding;

    @SuppressLint("InflateParams")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LayoutInflater layoutInflater = getLayoutInflater();
        layoutInflater.inflate(R.layout.activity_login, null);
        binding = ActivityLoginBinding.inflate(layoutInflater);
        setContentView(binding.getRoot());

        autoDetectOTP = new AutoDetectOTP(this);
        appPreferences = new AppPreferences(this);

        if (!SPData.hideOtpLogin())
            requestHint();

        binding.resend.setVisibility(View.GONE);
        binding.signupTxt.setVisibility(View.GONE);
        binding.ccp.setAutoDetectedCountry(true);

        checkSPData();

        setFacebookSignInListener();

        setGoogleSignInClient();

        setListener();

    }

    private void setGoogleSignInClient() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.google_webclient_id))
                .requestServerAuthCode(getString(R.string.google_webclient_id), false)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(LoginActivity.this, gso);
    }

    private void checkSPData() {
        if (SPData.hideOtpLogin()) {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) binding.socialMediaLayout.getLayoutParams();
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, binding.getRoot().getId());
            binding.socialMediaLayout.setLayoutParams(layoutParams);
            binding.card.setVisibility(View.INVISIBLE);
            binding.card.setEnabled(false);
        } else {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) binding.socialMediaLayout.getLayoutParams();
            layoutParams.addRule(RelativeLayout.BELOW, binding.card.getId());
            binding.socialMediaLayout.setLayoutParams(layoutParams);
            binding.card.setVisibility(View.VISIBLE);
            binding.card.setEnabled(true);
        }

        if (SPData.usePincodeInLogin()) {
            binding.tilPincode.setVisibility(View.VISIBLE);
        } else
            binding.tilPincode.setVisibility(View.GONE);

        if (SPData.forceToLogin())
            binding.skip.setVisibility(View.GONE);
        else
            binding.skip.setVisibility(View.VISIBLE);

        if (SPData.showCountryCode())
            binding.ccp.setVisibility(View.VISIBLE);
        else
            binding.ccp.setVisibility(View.GONE);

        if (SPData.showGoogleSignInBtn())
            binding.googleSignin.setVisibility(View.VISIBLE);
        else
            binding.googleSignin.setVisibility(View.GONE);

        if (SPData.showFacebookSignInBtn())
            binding.facebookSignin.setVisibility(View.VISIBLE);
        else
            binding.facebookSignin.setVisibility(View.GONE);
    }

    private void setListener() {
        binding.googleSignin.setOnClickListener(view -> signIn());

        binding.signupTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.msg.setText("Login to continue");
                binding.signupTxt.setPaintFlags(binding.signupTxt.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                binding.signupTxt.setText(Html.fromHtml(getString(R.string.not_registered_yet_click_here_to_register)));
            }
        });

        binding.checkbox.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                binding.sendOtp.setEnabled(true);
                binding.sendOtp.setBackground(getDrawable(R.drawable.save_profile));
            } else {
                binding.sendOtp.setEnabled(false);
                binding.sendOtp.setBackground(getDrawable(R.drawable.square));
            }
        });

        binding.skip.setOnClickListener(v -> finish());


        binding.etMobileNo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                binding.sendOtp.setText("SEND OTP");
            }
        });

        binding.etOtp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (binding.sendOtp.getText().toString().length() == 10)
                    binding.sendOtp.setText("VERIFY OTP");
            }
        });

        binding.sendOtp.setOnClickListener(v -> {

            if (binding.sendOtp.getText().toString().matches("SEND OTP")) {

                if (binding.etMobileNo.getText().toString().isEmpty() || binding.etMobileNo.getText().length() < 10) {
                    binding.etMobileNo.setError("Please enter valid mobile number");
                    binding.etMobileNo.setFocusable(true);
                    binding.etMobileNo.requestFocus();
                } else {
                    sendOtp();

                }
            } else if (binding.sendOtp.getText().toString().matches("VERIFY OTP")) {
                if (TextUtils.isEmpty(binding.etOtp.getText().toString())) {
                    binding.etOtp.setError("Please enter OTP");
                    binding.etOtp.setFocusable(true);
                    binding.etOtp.requestFocus();
                } else if (binding.etPincode.getText().toString().replaceAll(" ", "").isEmpty() && SPData.usePincodeInLogin()) {
                    Toast.makeText(LoginActivity.this, "Please enter your pincode", Toast.LENGTH_SHORT).show();
                } else if (binding.etPincode.getText().toString().length() != 6 && SPData.usePincodeInLogin()) {
                    Toast.makeText(LoginActivity.this, "Please a valid pincode", Toast.LENGTH_SHORT).show();
                } else {
                    verifyOtp();
                }
            }
        });

        binding.resend.setOnClickListener(v -> {
            binding.etOtp.setText("");
            if (TextUtils.isEmpty(binding.etMobileNo.getText().toString()) || binding.etMobileNo.getText().length() < 10) {
                binding.etMobileNo.setError("Please enter valid mobile number");
                binding.etMobileNo.setFocusable(true);
                binding.etMobileNo.requestFocus();
            } else {
                sendOtp();

            }

        });
        if (SPData.showChangeServerUrlDialog()) {
            showdialogToChangeServer();
        }
    }

    private void showdialogToChangeServer() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(LoginActivity.this).inflate(R.layout.dialog_change_server_url, null);
        builder.setView(view);
        EditText editTextServerUrl = view.findViewById(R.id.edit_server_url);
        EditText editTextBucketUrl = view.findViewById(R.id.edit_bucket_url);
        Button done = view.findViewById(R.id.edit_button_done);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String serverUrl = editTextServerUrl.getText().toString();
                String bucketUrl = editTextBucketUrl.getText().toString();
                if (!serverUrl.isEmpty()) {
                    SPData.getAppPreferences().setServerUrl(serverUrl);
                    SPData.getAppPreferences().setUsertoken(bucketUrl);
                    alertDialog.dismiss();
                    Toast.makeText(LoginActivity.this, "Server Changed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setFacebookSignInListener() {
        mCallbackManager = CallbackManager.Factory.create();
        binding.facebookSignin.setReadPermissions("email", "public_profile");
        LoginManager loginManager = LoginManager.getInstance();
        loginManager.setLoginBehavior(LoginBehavior.WEB_VIEW_ONLY);
        loginManager.registerCallback(mCallbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.e("TAG", "onSuccess: " + loginResult.getAccessToken().getToken());
                        Log.e("TAG", "onSuccess: " + AccessToken.getCurrentAccessToken().getToken());
                        Toast.makeText(LoginActivity.this, "Facebook SignIn Successfully", Toast.LENGTH_SHORT).show();
                        verifyFacebookSignIn(loginResult.getAccessToken().getToken());
                    }

                    @Override
                    public void onCancel() {
                        Log.e("TAG", "facebook:onCancel");
                        Toast.makeText(LoginActivity.this, "Facebook Signin cancel", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(FacebookException error) {
                        Log.e("TAG", "facebook:onError" + error);
                        Toast.makeText(LoginActivity.this, "" + error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void verifyFacebookSignIn(String idtoken) {
        customProgressDialog = CommonUtils.showProgressDialog(LoginActivity.this,
                getString(R.string.please_wait));
        final ApiService apiService = ApiUtils.getAPIService();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("token", idtoken);
        jsonObject.addProperty("tempu", SPData.getAppPreferences().getUserUniqueId());
        apiService.facebookLogin(jsonObject).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NotNull Call<JsonObject> call, @NotNull Response<JsonObject> response) {
                Log.e("TAG", "onResponse: " + response);
                setDataFromResponse(response, AppPreferences.SharedPrefrencesKey.FacebookSignIn.toString());
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                Log.e("TAG", t.getMessage());
                CommonUtils.hideProgressDialog(customProgressDialog);
                Toast.makeText(LoginActivity.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        this.startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    public void getAllCart() {

        ApiService apiService;
        if (!SPData.getAppPreferences().getUsertoken().isEmpty()) {
            apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());
            apiService.showCartItems("").enqueue(new Callback<CartDetailsInformation>() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onResponse(@NonNull Call<CartDetailsInformation> call, @NonNull Response<CartDetailsInformation> response) {
                    try {
                        if (response.isSuccessful()) {
                            CartDetailsInformation cartDetailsInfo = response.body();
                            assert cartDetailsInfo != null;
                            List<Product> cartDataArrayList = cartDetailsInfo.getData().getProducts();
                            SPData.getAppPreferences().setTotalCartCount(cartDataArrayList.size());
                            if (cartDataArrayList.size() > 0) {
                                SPData.getAppPreferences().setCartVendorId(cartDataArrayList.get(0).getVendor());
                            }
                        } else {
                            Log.e("TAG", "onResponse: " + "Problem in getting orders");
                        }
                        getAddresses();
                    } catch (Exception e) {
                        Log.e("TAG", "onResponse: " + e);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<CartDetailsInformation> call, @NonNull Throwable t) {
                    Toast.makeText(LoginActivity.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    SPData.getAppPreferences().setUsertoken("");
                    CommonUtils.hideProgressDialog(customProgressDialog);
                }
            });
        } else {
            apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUserUniqueId());
            apiService.showCartItemsWithoutLogin().enqueue(new Callback<CartDetailsInformation>() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onResponse(@NonNull Call<CartDetailsInformation> call, @NonNull Response<CartDetailsInformation> response) {
                    try {
                        if (response.isSuccessful()) {
                            CartDetailsInformation cartDetailsInfo = response.body();
                            assert cartDetailsInfo != null;
                            List<Product> cartDataArrayList = cartDetailsInfo.getData().getProducts();
                            SPData.getAppPreferences().setTotalCartCount(cartDataArrayList.size());
                            getAddresses();
                        } else {
                            Toast.makeText(LoginActivity.this, "Try again", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e("TAG", "onResponse: " + e);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<CartDetailsInformation> call, @NonNull Throwable t) {
                    Toast.makeText(LoginActivity.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    SPData.getAppPreferences().setUsertoken("");
                    CommonUtils.hideProgressDialog(customProgressDialog);
                }
            });
        }
    }

    public void getAddresses() {

        ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());
        apiService.getUserBaseAddress().enqueue(new Callback<UserBaseAddress>() {
            @Override
            public void onResponse(@NotNull Call<UserBaseAddress> call, @NotNull Response<UserBaseAddress> response) {
                if (response.isSuccessful()) {
                    ArrayList<UserDataAddress> dataList = response.body().getData();
                    CommonUtils.hideProgressDialog(customProgressDialog);
                    if ((dataList == null || dataList.size() == 0) && (SPData.showShippingMethods() || SPData.forceToAddAddress())) {
                        startActivity(new Intent(LoginActivity.this, AddAddressActivity.class).putExtra("fromlogin", true).putExtra("title", "Add Address"));
                        finish();
                    } else {
                        if (dataList != null && dataList.size() > 0) {
                            if (dataList.get(0).getLocationModel() != null)
                                SPData.getAppPreferences().setUserShippingCoordinates(dataList.get(0).getLocationModel().getCoordinates()[0] + "," + dataList.get(0).getLocationModel().getCoordinates()[1]);
                            SPData.getAppPreferences().setPincode("" + dataList.get(0).getPincode());
                            SPData.getAppPreferences().setAddressId(dataList.get(0).get_id());
                            SPData.getAppPreferences().setAddress(dataList.get(0).getAddressLine2() + ", " + dataList.get(0).getPincode());
                            if (dataList.get(0).getName() != null)
                                SPData.getAppPreferences().setUserShippingName(dataList.get(0).getName());
                            if (dataList.get(0).getMobile_number() != null)
                                SPData.getAppPreferences().setUserShippingMobile(dataList.get(0).getMobile_number());
                            SPData.getAppPreferences().setUserShippingAddress(dataList.get(0).getAddressLine1() + "\n" + dataList.get(0).getAddressLine2() + ", " + dataList.get(0).getCity() + "\n" + dataList.get(0).getState() + ", " + dataList.get(0).getPincode());
                        }
                        if (getIntent().getStringExtra("from") == null) {
                            finish();
                        } else {
                            if (SPData.showProfileScreenAfterLogin() && (appPreferences.getUserName().isEmpty() || appPreferences.getEmail().isEmpty())) {
                                Toast.makeText(LoginActivity.this, "Save your profile", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(LoginActivity.this, ProfileActivity.class).putExtra("from", true));
                            } else
                                startActivity(new Intent(LoginActivity.this, MainActivity.class).putExtra("from", true));
                            finish();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<UserBaseAddress> call, Throwable t) {
                CommonUtils.hideProgressDialog(customProgressDialog);
                SPData.getAppPreferences().setUsertoken("");
            }
        });
    }

    public void autoDetectOTP() {
        final CustomProgressDialog customProgressDialog = CommonUtils.showProgressDialog(LoginActivity.this,
                "Verifying OTP");
        autoDetectOTP.startSmsRetriver(new AutoDetectOTP.SmsCallback() {
            @Override
            public void connectionfailed(String e) {
                CommonUtils.hideProgressDialog(customProgressDialog);
                Toast.makeText(LoginActivity.this, "Something went wrong in auto detection", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void connectionSuccess(Void aVoid) {
            }

            @Override
            public void smsCallback(String sms) {
                if (sms.contains(":") && sms.contains(".")) {
                    CommonUtils.hideProgressDialog(customProgressDialog);
                    String otp = sms.substring(sms.indexOf(":") + 1, sms.indexOf(".")).trim();
                    binding.etOtp.setText(otp);
                    verifyOtp();
                }
            }
        });
    }

    private void sendOtp() {

        Log.e("TAG", "sendOtp: " + binding.ccp.getSelectedCountryCode());
        final CustomProgressDialog customProgressDialog = CommonUtils.showProgressDialog(LoginActivity.this,
                getString(R.string.please_wait));
        ApiService apiService = ApiUtils.getAPIService();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("countryCode", binding.ccp.getSelectedCountryCode());
        jsonObject.addProperty("mobile_number", binding.etMobileNo.getText().toString());
        //   jsonObject.addProperty("hash",AutoDetectOTP.getHashCode(this));
        apiService.loginmobile(jsonObject).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                CommonUtils.hideProgressDialog(customProgressDialog);
                if (response.isSuccessful()) {
                    String reponse_status = String.valueOf(response.body().get("status"));
                    if (reponse_status.matches("200")) {
                        Toast.makeText(LoginActivity.this, "OTP Sent to : +" + binding.ccp.getSelectedCountryCode() + binding.etMobileNo.getText().toString(), Toast.LENGTH_SHORT).show();
                        appPreferences.setUser_mobile_no(binding.etMobileNo.getText().toString());
                        binding.etOtp.setEnabled(true);
                        binding.sendOtp.setText("VERIFY OTP");
                        otpSent();
                        //    autoDetectOTP();
                    }
                } else {
                    try {
                        Log.e("TAG", "onResponse: " + response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                Log.e("fail", call.toString());
                Toast.makeText(LoginActivity.this, "Failed to connect", Toast.LENGTH_SHORT).show();
                CommonUtils.hideProgressDialog(customProgressDialog);
            }
        });
    }

    private void otpSent() {
        binding.resend.setVisibility(View.VISIBLE);
        CountDownTimer timer = new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                binding.resend.setOnClickListener(null);
                binding.resend.setText("Resend OTP in " + millisUntilFinished / 1000 + "sec(s)");
            }

            @Override
            public void onFinish() {
                binding.resend.setText("Resend OTP");
                binding.resend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        binding.etOtp.setText("");
                        if (TextUtils.isEmpty(binding.etMobileNo.getText().toString()) || binding.etMobileNo.getText().length() < 10) {
                            binding.etMobileNo.setError("Please enter valid mobile number");
                            binding.etMobileNo.setFocusable(true);
                            binding.etMobileNo.requestFocus();
                        } else {
                            sendOtp();
                        }
                    }
                });
            }
        };
        timer.start();
    }

    private void verifyOtp() {

        customProgressDialog = CommonUtils.showProgressDialog(LoginActivity.this,
                getString(R.string.please_wait));
        final ApiService apiService = ApiUtils.getAPIService();
        Log.e("TAG", "verifyOtp: " + SPData.getAppPreferences().getSharedByReferralCode());
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("countryCode", binding.ccp.getSelectedCountryCode());
        jsonObject.addProperty("mobile_number", binding.etMobileNo.getText().toString());
        jsonObject.addProperty("otp", binding.etOtp.getText().toString());
        jsonObject.addProperty("tempu", SPData.getAppPreferences().getUserUniqueId());
        apiService.verifyOtp(SPData.getAppPreferences().getSharedByReferralCode(), jsonObject).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NotNull Call<JsonObject> call, @NotNull Response<JsonObject> response) {
                setDataFromResponse(response, AppPreferences.SharedPrefrencesKey.PhoneNoSignIn.toString());
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                Log.e("fail", call.toString());
                CommonUtils.hideProgressDialog(customProgressDialog);
                Toast.makeText(LoginActivity.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void setDataFromResponse(Response<JsonObject> response, String method) {
        if (response.isSuccessful() && response.body() != null && response.body().get("status").getAsInt() == 200) {
            Data data = new Gson().fromJson(response.body().get("data").getAsJsonObject().toString(), Data.class);
            if (!CommonUtils.stringIsNotNullAndEmpty(data.getUser().getFullName()) && SPData.allowFullNameInLogin()) {
                askForName(data, method);
            } else {
                setProfile(data, method);
            }
        } else {
            CommonUtils.hideProgressDialog(customProgressDialog);
            try {
                JSONObject jsonObject = new JSONObject(response.errorBody().string());
                Log.e("TAG", "setDataFromResponse: " + jsonObject);
                Toast.makeText(this, "" + jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setProfile(Data data, String method) {
        String token = data.getToken();
        User_ user_ = data.getUser();
        SPData.getAppPreferences().setUsertoken(token);
        SPData.getAppPreferences().setSharedByReferralCode("");
        if (SPData.usePincodeInLogin()) {
            SPData.getAppPreferences().setPincode(binding.etPincode.getText().toString());
            SPData.getAppPreferences().setAddress(binding.etPincode.getText().toString());
        } else {
            appPreferences.setPincode("");
            appPreferences.setAddress("");
        }

        SPData.loginlogout = true;
        appPreferences.setUser_mobile_no(user_.getMobileNumber());
        appPreferences.setUserId(user_.getId());
        appPreferences.setUserProfilePic(user_.getProfile_picture());
        appPreferences.setUserName(user_.getFullName());
        appPreferences.setEmail(user_.getEmail());
        appPreferences.setReferralCode(user_.getReferralCode());
        appPreferences.setUserType("customer");


        if (user_.getDocs() != null) {
            appPreferences.setUserAadhar(user_.getDocs().getAadhar());
            appPreferences.setUserCheque(user_.getDocs().getCheque());
            appPreferences.setUserPan(user_.getDocs().getPan());
        }
        if (user_.getGoogle() != null) {
            appPreferences.setEmail(user_.getGoogle().getEmail());
        }
        if (method.equals(AppPreferences.SharedPrefrencesKey.GoogleSignIn.toString())) {
            appPreferences.signInByGoogle(true);
            appPreferences.signInByPhone(false);
            appPreferences.signInByFacebook(false);
        } else if (method.equals(AppPreferences.SharedPrefrencesKey.PhoneNoSignIn.toString())) {
            appPreferences.signInByPhone(true);
            appPreferences.signInByGoogle(false);
            appPreferences.signInByFacebook(false);
        } else {
            appPreferences.signInByFacebook(true);
            appPreferences.signInByGoogle(false);
            appPreferences.signInByPhone(false);
        }
        if (SPData.showMembership())
            getPurchasedMemberShip();
        registerFCMToken();
    }

    private void getPurchasedMemberShip() {
        ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());
        apiService.getPurchasedMembership().enqueue(new Callback<GetMembershipBase>() {
            @Override
            public void onResponse(@NotNull Call<GetMembershipBase> call, @NotNull Response<GetMembershipBase> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SPData.getAppPreferences().setMembershipPurchased(response.body().getData().size() > 0);
                }
            }

            @Override
            public void onFailure(@NotNull Call<GetMembershipBase> call, @NotNull Throwable t) {

            }
        });
    }

    private void askForName(Data data, String method) {
        FlatDialog flatDialog = new FlatDialog(LoginActivity.this);
        flatDialog.setTitle("Enter name to continue")
                .setSubtitle("please enter your full name")
                .setFirstTextFieldHint("name")
                .setFirstButtonText("Done")
                .setSecondButtonText("Cancel")
                .isCancelable(false)
                .setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark))
                .setFirstTextFieldInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME)
                .setFirstButtonTextColor(getResources().getColor(R.color.colorPrimaryDark))
                .setFirstButtonColor(getResources().getColor(R.color.colorWhite))
                .setSecondButtonColor(getResources().getColor(R.color.colorPrimaryDark))
                .withFirstButtonListner(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String name = flatDialog.getFirstTextField();
                        updateName(name, data, method);
                        flatDialog.dismiss();
                    }
                }).withSecondButtonListner(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flatDialog.dismiss();
                setProfile(data, method);
            }
        })
                .show();
    }

    private void updateName(String name, Data data, String method) {
        ApiService apiService = ApiUtils.getHeaderAPIService(data.getToken());
        RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"), name);
        RequestBody mobileNumber = RequestBody.create(MediaType.parse("text/plain"), appPreferences.getMobileNumber());
        apiService.updateName(requestBody, mobileNumber).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NotNull Call<JsonObject> call, @NotNull Response<JsonObject> response) {
                setProfile(data, method);
            }

            @Override
            public void onFailure(@NotNull Call<JsonObject> call, @NotNull Throwable t) {
                setProfile(data, method);
            }
        });
    }


    private void registerFCMToken() {
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                firebasetoken = instanceIdResult.getToken();
                Log.e("TAG", "registerFCMToken: " + SPData.getAppPreferences().getUsertoken());
                ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("registrationToken", firebasetoken);
                apiService.addFCMToken(jsonObject).enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NotNull Call<JsonObject> call, @NotNull Response<JsonObject> response) {
                        getAllCart();
                        if (response.isSuccessful()) {
                            Log.e("TAG", "onResponse: " + "Notification setting done successfully");
                        } else {
                            Log.e("TAG", "onResponse: " + "Can not set notification setting");
                        }
                    }

                    @Override
                    public void onFailure(@NotNull Call<JsonObject> call, @NotNull Throwable t) {
                        SPData.getAppPreferences().setUsertoken("");
                        CommonUtils.hideProgressDialog(customProgressDialog);
                        Toast.makeText(LoginActivity.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
                        getAllCart();
                    }
                });
                Log.e("TAG", "onSuccess: " + firebasetoken);
            }
        });


    }


    private void requestHint() {
        try {
            GoogleApiClient apiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        }
                    })
                    .addApi(Auth.CREDENTIALS_API)
                    .build();
            HintRequest hintRequest = new HintRequest.Builder()
                    .setHintPickerConfig(new CredentialPickerConfig.Builder()
                            .setShowCancelButton(true)
                            .build())
                    .setPhoneNumberIdentifierSupported(true)
                    .build();

            PendingIntent intent = Auth.CredentialsApi.getHintPickerIntent(
                    apiClient, hintRequest);

            startIntentSenderForResult(intent.getIntentSender(),
                    RESOLVE_HINT, null, 0, 0, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Obtain the phone number from the result
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            super.onActivityResult(requestCode, resultCode, data);
            if (mCallbackManager != null)
                mCallbackManager.onActivityResult(requestCode, resultCode, data);
            if (requestCode == RC_SIGN_IN) {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    verifyGoogleLogin(account.getIdToken());
                } catch (ApiException e) {
                    Toast.makeText(this, "" + e, Toast.LENGTH_SHORT).show();
                }
            }
            if (requestCode == RESOLVE_HINT) {
                if (resultCode == RESULT_OK) {
                    Credential credential = data.getParcelableExtra(Credential.EXTRA_KEY);
                    assert credential != null;
                    binding.etMobileNo.setText(credential.getId().replace("+91", ""));
                }
            }
            if (resultCode == RESULT_OK && data.getStringExtra("staff") != null && data.getStringExtra("staff").equals("true"))
                finish();
        }

    }

    private void verifyGoogleLogin(String idtoken) {
        customProgressDialog = CommonUtils.showProgressDialog(LoginActivity.this,
                getString(R.string.please_wait));
        final ApiService apiService = ApiUtils.getAPIService();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("token", idtoken);
        jsonObject.addProperty("tempu", SPData.getAppPreferences().getUserUniqueId());
        apiService.googleLogin(jsonObject).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NotNull Call<JsonObject> call, @NotNull Response<JsonObject> response) {
                setDataFromResponse(response, AppPreferences.SharedPrefrencesKey.GoogleSignIn.toString());
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                CommonUtils.hideProgressDialog(customProgressDialog);
                Toast.makeText(LoginActivity.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void openTermsCondition(View view) {
        startActivity(new Intent(LoginActivity.this, PrivacyPolicy.class).putExtra("type", ApiService.TNC));
    }

    public void openPrivacyPolicy(View view) {
        startActivity(new Intent(LoginActivity.this, PrivacyPolicy.class).putExtra("type", ApiService.PRIVACY));
    }
}