package com.mindfulai.Activites;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.JsonObject;
import com.mindfulai.Adapter.MembershipAdapter;
import com.mindfulai.Models.membership.GetMembershipBase;
import com.mindfulai.Models.membership.MembershipBaseModel;
import com.mindfulai.NetworkRetrofit.ApiService;
import com.mindfulai.NetworkRetrofit.ApiUtils;
import com.mindfulai.Utils.SPData;
import com.mindfulai.customclass.DoPayment;
import com.mindfulai.ministore.R;
import com.mindfulai.ministore.databinding.ActivityCheckMembershipBinding;
import com.razorpay.PaymentData;
import com.razorpay.PaymentResultWithDataListener;
import org.jetbrains.annotations.NotNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckMembershipActivity extends AppCompatActivity implements PaymentResultWithDataListener {

    public ActivityCheckMembershipBinding binding;
    private DoPayment doPayment;

    @SuppressLint("InflateParams")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater layoutInflater = getLayoutInflater();
        layoutInflater.inflate(R.layout.activity_check_membership, null);
        binding = ActivityCheckMembershipBinding.inflate(layoutInflater);
        setContentView(binding.getRoot());
        binding.title.setText(getString(R.string.app_name) + " Membership \nProgram");
        LinearLayoutManager manager = new LinearLayoutManager(CheckMembershipActivity.this);
        manager.setOrientation(RecyclerView.HORIZONTAL);
        binding.plansMembership.setLayoutManager(manager);
        setTitle("Membership");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getMembership();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void membershipPurchased() {
        Toast.makeText(CheckMembershipActivity.this, "Membership purchased successfully", Toast.LENGTH_SHORT).show();
        SPData.getAppPreferences().setMembershipPurchased(true);
        Intent i = new Intent(CheckMembershipActivity.this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }

    public void purchaseMembership(String membershipid) {
        ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("membership", membershipid);
        apiService.purchaseMembership(jsonObject).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NotNull Call<JsonObject> call, @NotNull Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject jsonObject1 = response.body().get("data").getAsJsonObject();
                    String type = jsonObject1.get("action").getAsString();
                    if (type.equals("membership")) {
                        //membership purchase
                        membershipPurchased();
                    } else {
                        String orderId = jsonObject1.get("order_id").getAsString();
                        String txnToken = "";
                        if (jsonObject1.has("txnToken")) {
                            txnToken = jsonObject1.get("txnToken").getAsString();
                        }
                        String amount = "" + jsonObject1.get("amount").getAsLong();
                        doPayment = new DoPayment(CheckMembershipActivity.this, orderId, txnToken, amount);
                        doPayment.openCheckout();
                    }
                }
            }

            @Override
            public void onFailure(@NotNull Call<JsonObject> call, @NotNull Throwable t) {

            }
        });
    }

    private void getMembership() {
        ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());
        apiService.getMemberships().enqueue(new Callback<MembershipBaseModel>() {
            @Override
            public void onResponse(@NotNull Call<MembershipBaseModel> call, @NotNull Response<MembershipBaseModel> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MembershipAdapter membershipAdapter = new MembershipAdapter(CheckMembershipActivity.this, response.body().getData());
                    binding.plansMembership.setAdapter(membershipAdapter);
                    membershipAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(@NotNull Call<MembershipBaseModel> call, @NotNull Throwable t) {

            }
        });
    }

    @Override
    public void onPaymentSuccess(String s, PaymentData paymentData) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("paymentId", paymentData.getPaymentId());
        jsonObject.addProperty("orderId", paymentData.getOrderId());
        jsonObject.addProperty("signature", paymentData.getSignature());
        doPayment.verifyPayment(jsonObject);
    }

    @Override
    public void onPaymentError(int i, String s, PaymentData paymentData) {

    }
}