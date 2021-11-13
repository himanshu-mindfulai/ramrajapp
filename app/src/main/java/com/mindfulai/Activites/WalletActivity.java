package com.mindfulai.Activites;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.google.gson.JsonObject;
import com.mindfulai.Models.CustomerInfo.CustomerData;
import com.mindfulai.Models.WalletRechargeModel.WalletRechargeModel;
import com.mindfulai.Models.orderDetailInfo.OrderDetailInfo;
import com.mindfulai.NetworkRetrofit.ApiService;
import com.mindfulai.NetworkRetrofit.ApiUtils;
import com.mindfulai.Utils.CommonUtils;
import com.mindfulai.Utils.CustomProgressDialog;
import com.mindfulai.Utils.SPData;
import com.mindfulai.ministore.R;
import com.mindfulai.ui.WalletFragment;
import com.razorpay.Checkout;
import com.razorpay.PaymentData;
import com.razorpay.PaymentResultWithDataListener;
import com.valdesekamdem.library.mdtoast.MDToast;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WalletActivity extends AppCompatActivity implements PaymentResultWithDataListener {

    public WalletFragment walletFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_wallet);
            Objects.requireNonNull(getSupportActionBar()).setTitle("Wallet");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            walletFragment = new WalletFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.framelayout_container, walletFragment).commit();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPaymentSuccess(String s, PaymentData paymentData) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("paymentId", paymentData.getPaymentId());
        jsonObject.addProperty("orderId", paymentData.getOrderId());
        jsonObject.addProperty("signature", paymentData.getSignature());
        walletFragment.walletRechargeFragment.doPayment.verifyPayment(jsonObject);
    }

    @Override
    public void onPaymentError(int i, String s, PaymentData paymentData) {
        MDToast.makeText(this, "Payment failed!", MDToast.TYPE_ERROR).show();
    }

}