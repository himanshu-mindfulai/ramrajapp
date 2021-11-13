package com.mindfulai.Activites;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.flatdialoglibrary.dialog.FlatDialog;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.mindfulai.Adapter.PurchasedMembershipAdapter;
import com.mindfulai.Models.DeliveryMethod.DeliveryMethodBase;
import com.mindfulai.Models.DeliveryMethod.DeliveryMethodData;
import com.mindfulai.Models.LoginData.Data;
import com.mindfulai.Models.flashbanner.FlashBanner;
import com.mindfulai.Models.membership.GetMembershipBase;
import com.mindfulai.NetworkRetrofit.ApiService;
import com.mindfulai.NetworkRetrofit.ApiUtils;
import com.mindfulai.Utils.CommonUtils;
import com.mindfulai.Utils.SPData;
import com.mindfulai.ministore.R;
import com.valdesekamdem.library.mdtoast.MDToast;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.app.akplacepicker.utilities.Constants.MY_PERMISSIONS_REQUEST_LOCATION;

public class SplashActivity extends Activity implements Animation.AnimationListener {
    private static final String TAG = "SplashActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        try {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
            Animation animFadeIn = AnimationUtils.loadAnimation(getApplicationContext(),
                    R.anim.animation_fade_in);
            handleReferralsIfAny();
            Log.e("TAG", "onCreate: " + SPData.getAppPreferences().getSharedByReferralCode());
            if (!SPData.getAppPreferences().getUsertoken().isEmpty() && SPData.showMembership()) {
                getPurchasedMemberShip();
            }
            animFadeIn.setAnimationListener(this);
            RelativeLayout linearLayout = findViewById(R.id.layout_linear);
            linearLayout.setVisibility(View.VISIBLE);
            linearLayout.startAnimation(animFadeIn);
        } catch (Exception e) {
            Log.e(TAG, "onCreate: " + e);
            CommonUtils.showErrorMessage(SplashActivity.this, "" + e);
        }
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


    @Override
    public void onAnimationStart(Animation animation) {
        //under Implementation
    }

    public void onAnimationEnd(Animation animation) {
        animationEnd();
    }

    private void animationEnd() {
        if (SPData.forceToTakeLocation() && SPData.getAppPreferences().getPincode().isEmpty()) {
            askForName();
        } else {
            checkScreen();
        }
    }

    private void askForName() {
        FlatDialog flatDialog = new FlatDialog(SplashActivity.this);
        flatDialog.setTitle("Enter Pincode")
                .setSubtitle("Enter your location for checking product availability ")
                .setFirstTextFieldHint("pincode")
                .setFirstButtonText("Done")
                .setSecondButtonText("Cancel")
                .isCancelable(false)
                .setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark))
                .setFirstTextFieldInputType(InputType.TYPE_CLASS_NUMBER)
                .setFirstButtonTextColor(getResources().getColor(R.color.colorPrimaryDark))
                .setFirstButtonColor(getResources().getColor(R.color.colorWhite))
                .setSecondButtonColor(getResources().getColor(R.color.colorPrimaryDark))
                .withFirstButtonListner(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String pincode = flatDialog.getFirstTextField();
                        if (pincode.length() == 6) {
                            SPData.getAppPreferences().setPincode(pincode);
                            SPData.getAppPreferences().setAddress(pincode);
                            flatDialog.dismiss();
                            checkScreen();
                        }else{
                            Toast.makeText(SplashActivity.this, "Pincode is required", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).withSecondButtonListner(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flatDialog.dismiss();
                finish();
            }
        })
                .show();
    }


    private void checkScreen() {
        if (SPData.getAppPreferences().getUserUniqueId().isEmpty()) {
            String uniqueID = UUID.randomUUID().toString();
            SPData.getAppPreferences().setUserUniqueId(uniqueID);
        }
        Log.e("TAG", "checkScreen: " + SPData.getAppPreferences().getUserUniqueId());
        if (SPData.getAppPreferences().getUsertoken().isEmpty() && SPData.forceToLogin()) {
            Intent i = new Intent(SplashActivity.this, LoginActivity.class);
            i.putExtra("from", "login");
            startActivity(i);
            this.finish();
        } else {
            Intent i = new Intent();
            if (SPData.forceToAddAddress() && SPData.getAppPreferences().getPincode().isEmpty() && !SPData.getAppPreferences().getUsertoken().isEmpty()) {
                Toast.makeText(this, "Address is required", Toast.LENGTH_SHORT).show();
                i.setClass(SplashActivity.this, AddAddressActivity.class);
                i.putExtra("fromlogin", true);
                i.putExtra("title", "Add Address");
            } else
                i.setClass(SplashActivity.this, MainActivity.class);
            i.putExtra("show", true);
            startActivity(i);
            this.finish();
        }
    }

    private void handleReferralsIfAny() {
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, pendingDynamicLinkData -> {
                            if (pendingDynamicLinkData != null && pendingDynamicLinkData.getLink() != null) {
                                Uri deepLink = pendingDynamicLinkData.getLink();
                                String referralCode = deepLink.getQueryParameter("invitedby");
                                SPData.getAppPreferences().setSharedByReferralCode(referralCode);
                                Log.e("TAG", "code: " + referralCode);
                            }
                        }
                ).addOnFailureListener(e -> {
            Log.e("TAG", "handleReferralsIfAny onFailure: " + e);
            Toast.makeText(SplashActivity.this, "onFailure " + e, Toast.LENGTH_SHORT).show();
        });
    }


    @Override
    public void onAnimationRepeat(Animation animation) {
        //under Implementation
    }

}