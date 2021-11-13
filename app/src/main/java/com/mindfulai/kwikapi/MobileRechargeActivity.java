package com.mindfulai.kwikapi;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.gson.JsonObject;
import com.mindfulai.Activites.LoginActivity;
import com.mindfulai.Adapter.CircleCodesAdapter;
import com.mindfulai.Adapter.OperationCodesAdapter;
import com.mindfulai.Models.CustomerInfo.CustomerData;
import com.mindfulai.Models.kwikapimodels.*;
import com.mindfulai.NetworkRetrofit.ApiService;
import com.mindfulai.NetworkRetrofit.ApiUtils;
import com.mindfulai.Utils.CommonUtils;
import com.mindfulai.Utils.CustomProgressDialog;
import com.mindfulai.Utils.SPData;
import com.mindfulai.customclass.DoPayment;
import com.mindfulai.ministore.R;
import com.mindfulai.ministore.databinding.ActivityMobileRechargeBinding;
import com.razorpay.PaymentData;
import com.razorpay.PaymentResultWithDataListener;
import okhttp3.MultipartBody;
import org.jetbrains.annotations.NotNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;

public class MobileRechargeActivity extends AppCompatActivity implements PaymentResultWithDataListener {

    public ActivityMobileRechargeBinding binding;
    private double walletBalance;
    private String opid;
    private CustomProgressDialog customProgressDialog;
    private String type;
    private String circleCode;
    private PlansResponse rechargePlans;
    private ArrayList<PlanItem> planItems;
    private PlansAdapter plansAdapter;
    private String stateCode;
    private DoPayment doPayment;
    private double clientBalance;

    @SuppressLint("InflateParams")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater layoutInflater = getLayoutInflater();
        layoutInflater.inflate(R.layout.activity_mobile_recharge, null);
        binding = ActivityMobileRechargeBinding.inflate(layoutInflater);
        setContentView(binding.getRoot());
        type = getIntent().getStringExtra("type");
        setTitle(type + " Recharge");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (type.equals("DTH")) {
            binding.numberTitle.setText("DTH/D2H Recharge");
        } else {
            binding.numberTitle.setText("Mobile Number");
        }
        planItems = new ArrayList<>();
        doPayment = new DoPayment(MobileRechargeActivity.this, "", "", "");
        binding.recyclerviewPlans.setLayoutManager(new LinearLayoutManager(MobileRechargeActivity.this));
        plansAdapter = new PlansAdapter(this, planItems);
        binding.recyclerviewPlans.setAdapter(plansAdapter);


        getKwikBalance();

        getWalletBalance();

        getAllOperatorCode();

        getAllCircleCode();

        setOnClick();

    }

    private void setOnClick() {
        binding.data.setOnClickListener(v -> {
            planItems.clear();
            planItems.addAll(rechargePlans.getDATA());
            plansAdapter.notifyDataSetChanged();
            binding.fulltt.setSelected(false);
            binding.topup.setSelected(false);
            binding.data.setSelected(true);
            binding.sms.setSelected(false);
            binding.roaming.setSelected(false);
            binding.frc.setSelected(false);
            binding.stv.setSelected(false);
        });
        binding.topup.setOnClickListener(v -> {
            planItems.clear();
            planItems.addAll(rechargePlans.getTOPUP());
            plansAdapter.notifyDataSetChanged();
            binding.fulltt.setSelected(false);
            binding.topup.setSelected(true);
            binding.data.setSelected(false);
            binding.sms.setSelected(false);
            binding.roaming.setSelected(false);
            binding.frc.setSelected(false);
            binding.stv.setSelected(false);
        });
        binding.stv.setOnClickListener(v -> {
            planItems.clear();
            planItems.addAll(rechargePlans.getSTV());
            plansAdapter.notifyDataSetChanged();
            binding.fulltt.setSelected(false);
            binding.topup.setSelected(false);
            binding.data.setSelected(false);
            binding.sms.setSelected(false);
            binding.roaming.setSelected(false);
            binding.frc.setSelected(false);
            binding.stv.setSelected(true);
        });
        binding.roaming.setOnClickListener(v -> {
            planItems.clear();
            planItems.addAll(rechargePlans.getRomaing());
            plansAdapter.notifyDataSetChanged();
            binding.fulltt.setSelected(false);
            binding.topup.setSelected(false);
            binding.data.setSelected(false);
            binding.sms.setSelected(false);
            binding.roaming.setSelected(true);
            binding.frc.setSelected(false);
            binding.stv.setSelected(false);
        });
        binding.frc.setOnClickListener(v -> {
            planItems.clear();
            planItems.addAll(rechargePlans.getFRC());
            plansAdapter.notifyDataSetChanged();
            binding.fulltt.setSelected(false);
            binding.topup.setSelected(false);
            binding.data.setSelected(false);
            binding.sms.setSelected(false);
            binding.roaming.setSelected(false);
            binding.frc.setSelected(true);
            binding.stv.setSelected(false);
        });
        binding.sms.setOnClickListener(v -> {
            planItems.clear();
            planItems.addAll(rechargePlans.getSMS());
            plansAdapter.notifyDataSetChanged();
            binding.fulltt.setSelected(false);
            binding.topup.setSelected(false);
            binding.data.setSelected(false);
            binding.sms.setSelected(true);
            binding.roaming.setSelected(false);
            binding.frc.setSelected(false);
            binding.stv.setSelected(false);
        });
        binding.fulltt.setOnClickListener(v -> {
            planItems.clear();
            planItems.addAll(rechargePlans.getFULLTT());
            plansAdapter.notifyDataSetChanged();
            binding.fulltt.setSelected(true);
            binding.topup.setSelected(false);
            binding.data.setSelected(false);
            binding.sms.setSelected(false);
            binding.roaming.setSelected(false);
            binding.frc.setSelected(false);
            binding.stv.setSelected(false);
        });
        binding.rechargeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!SPData.getAppPreferences().getUsertoken().isEmpty()) {
                    if (CommonUtils.stringIsNotNullAndEmpty(binding.etPhoneNo.getText().toString()) && CommonUtils.stringIsNotNullAndEmpty(binding.etAmount.getText().toString())) {
                        double amount = Double.parseDouble(binding.etAmount.getText().toString());
                        if (walletBalance >= amount) {
                            if (amount < 10 && type.equals("Prepaid")) {
                                CommonUtils.showErrorMessage(MobileRechargeActivity.this, "Minimum amount of recharge is Rs.10");
                            } else
                                getOrderId();
                        } else {
                            doPayment.rechargeWallet(amount - walletBalance);
                        }
                    } else if (binding.etPhoneNo.getText().toString().isEmpty()) {
                        Toast.makeText(MobileRechargeActivity.this, "Enter a valid phone number", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MobileRechargeActivity.this, "Enter a valid recharge amount", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    startActivity(new Intent(MobileRechargeActivity.this, LoginActivity.class));
                }
            }
        });
    }

    private void getAllCircleCode() {
        ApiService apiService = ApiUtils.getKwikAPIService();
        apiService.getCircleCode(getString(R.string.kwik_api_key)).enqueue(new Callback<CircleCodeBase>() {
            @Override
            public void onResponse(@NotNull Call<CircleCodeBase> call, @NotNull Response<CircleCodeBase> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ArrayList<CircleCodeResponse> operatorCodeResponses = response.body().getResponses();
                    CircleCodesAdapter categoryDropDownAdapter = new CircleCodesAdapter(MobileRechargeActivity.this, operatorCodeResponses);
                    binding.spinnerCircle.setAdapter(categoryDropDownAdapter);
                    binding.spinnerCircle.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            CircleCodeResponse codeResponse = (CircleCodeResponse) parent.getItemAtPosition(position);
                            circleCode = codeResponse.getCircle_name();
                            stateCode = codeResponse.getCircle_code();
                            if (circleCode != null && opid != null)
                                getRechargePlans();
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                }
            }

            @Override
            public void onFailure(@NotNull Call<CircleCodeBase> call, @NotNull Throwable t) {

            }
        });
    }

    private void getRechargePlans() {
        ApiService apiService = ApiUtils.getKwikAPIService();
        MultipartBody.Part apiKey = MultipartBody.Part.createFormData("api_key", getString(R.string.kwik_api_key));
        MultipartBody.Part sC = MultipartBody.Part.createFormData("state_code", stateCode);
        MultipartBody.Part op = MultipartBody.Part.createFormData("opid", opid);
        apiService.getPlans(apiKey, sC, op).enqueue(new Callback<GetPlanResponse>() {
            @Override
            public void onResponse(@NotNull Call<GetPlanResponse> call, @NotNull Response<GetPlanResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getPlans() != null && response.body().getPlans().getFULLTT() != null) {
                    binding.horizontalScrollView.setVisibility(View.VISIBLE);
                    rechargePlans = response.body().getPlans();
                    planItems.clear();
                    planItems.addAll(rechargePlans.getFULLTT());
                    plansAdapter.notifyDataSetChanged();
                    binding.fulltt.setSelected(true);
                    binding.topup.setSelected(false);
                    binding.data.setSelected(false);
                    binding.sms.setSelected(false);
                    binding.roaming.setSelected(false);
                    binding.frc.setSelected(false);
                    binding.stv.setSelected(false);
                }else {
                    binding.horizontalScrollView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(@NotNull Call<GetPlanResponse> call, @NotNull Throwable t) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            walletBalance = data.getDoubleExtra("balance", 0);
            binding.tvWalletBalance.setText(getString(R.string.rs) + walletBalance);
        }
    }

    private void completeRecharge(long orderID) {
        ApiService apiService = ApiUtils.getKwikAPIService();
        apiService.prepaidDthRecharge(getString(R.string.kwik_api_key), binding.etPhoneNo.getText().toString(), opid, binding.etAmount.getText().toString(), "0", "" + orderID).enqueue(new Callback<RechargeResponse>() {
            @Override
            public void onResponse(@NotNull Call<RechargeResponse> call, @NotNull Response<RechargeResponse> response) {
                Log.e("TAG", "completeRecharge onResponse: "+response);
                handleRechargeResponse(call, response);
            }

            @Override
            public void onFailure(@NotNull Call<RechargeResponse> call, @NotNull Throwable t) {
                Log.e("TAG", "completeRecharge onFailure: "+t);
                CommonUtils.hideProgressDialog(customProgressDialog);
                getKwikBalance();
            }
        });
    }

    private void getKwikBalance(){
        ApiService apiService = ApiUtils.getKwikAPIService();
        apiService.getBalance(getString(R.string.kwik_api_key)).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NotNull Call<JsonObject> call, @NotNull Response<JsonObject> response) {
                if(response.isSuccessful()&&response.body()!=null){
                    double currentBalance = response.body().get("response").getAsJsonObject().get("balance").getAsDouble();
                    if(currentBalance<clientBalance){
                        clientBalance = currentBalance;
                        deductAmtFromWallet(Double.parseDouble(binding.etAmount.getText().toString()));
                        binding.etPhoneNo.setText("");
                        binding.etAmount.setText("");
                    }else if(clientBalance==0){
                        clientBalance=currentBalance;
                    }
                }
            }

            @Override
            public void onFailure(@NotNull Call<JsonObject> call, @NotNull Throwable t) {
                CommonUtils.hideProgressDialog(customProgressDialog);
                CommonUtils.showErrorMessage(MobileRechargeActivity.this, "" + t.getMessage());
            }
        });
    }

    private void getOrderId() {
        customProgressDialog = CommonUtils.showProgressDialog(MobileRechargeActivity.this,
                getString(R.string.please_wait));
        ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());
        apiService.getKwikOrderID().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NotNull Call<JsonObject> call, @NotNull Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    long orderID = response.body().get("data").getAsLong();
                    Log.e("TAG", "onResponse:getOrderId " + orderID);
                    completeRecharge(orderID);
                } else {
                    CommonUtils.hideProgressDialog(customProgressDialog);
                    CommonUtils.showErrorMessage(MobileRechargeActivity.this, "" + response.message());
                }
            }

            @Override
            public void onFailure(@NotNull Call<JsonObject> call, @NotNull Throwable t) {
                CommonUtils.hideProgressDialog(customProgressDialog);
                Log.e("TAG", "onFailure: " + t.getMessage());
                CommonUtils.showErrorMessage(MobileRechargeActivity.this, "" + t.getMessage());
            }
        });

    }


    private void handleRechargeResponse(Call<RechargeResponse> call, Response<RechargeResponse> response) {
        if (response.isSuccessful() && response.body() != null && (response.body().getStatus().equals("SUCCESS")||response.body().getStatus().equals("PENDING"))) {
            deductAmtFromWallet(Double.parseDouble(binding.etAmount.getText().toString()));
            binding.etPhoneNo.setText("");
            binding.etAmount.setText("");
            CommonUtils.showSuccessMessage(MobileRechargeActivity.this, "" + response.body().getMessage());
        } else if (response.body() != null) {
            CommonUtils.hideProgressDialog(customProgressDialog);
            CommonUtils.showErrorMessage(MobileRechargeActivity.this, "" + response.body().getMessage() + " try again");
        } else {
            CommonUtils.hideProgressDialog(customProgressDialog);
            CommonUtils.showErrorMessage(MobileRechargeActivity.this, "" + response.message());
        }
    }

    private void deductAmtFromWallet(double amount) {
        ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("amount", amount);
        apiService.deductAmtFromWallet(jsonObject).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NotNull Call<JsonObject> call, @NotNull Response<JsonObject> response) {
                CommonUtils.hideProgressDialog(customProgressDialog);
                Log.e("TAG", "onResponse: deductAmtFromWallet " + response.body());
                if (response.isSuccessful() && response.body() != null) {
                    CommonUtils.showSuccessMessage(MobileRechargeActivity.this, "Recharge Successful");
                    finish();
                }
            }

            @Override
            public void onFailure(@NotNull Call<JsonObject> call, @NotNull Throwable t) {
                Log.e("TAG", "onFailure:deductAmtFromWallet " + t.getMessage());
                CommonUtils.showErrorMessage(MobileRechargeActivity.this, "" + t.getMessage());
            }
        });
    }

    private void getWalletBalance() {
        ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());
        apiService.getProfileDetails().enqueue(new Callback<CustomerData>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(@NotNull Call<CustomerData> call, @NotNull Response<CustomerData> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CustomerData customerDataResponse = response.body();
                    walletBalance = response.body().getData().getUser().getWallet();
                    binding.tvWalletBalance.setText(getString(R.string.rs) + CommonUtils.getTwoDigitsRoundOffValue(customerDataResponse.getData().getUser().getWallet()));
                }
            }

            @Override
            public void onFailure(@NotNull Call<CustomerData> call, @NotNull Throwable t) {
                t.printStackTrace();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (SPData.getAppPreferences().getUsertoken().isEmpty()) {
            binding.rechargeBtn.setText("Login to recharge");
        } else {
            getWalletBalance();
            binding.rechargeBtn.setText("Recharge");
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void getAllOperatorCode() {
        ApiService apiService = ApiUtils.getKwikAPIService();
        apiService.getOperatorCode(getString(R.string.kwik_api_key)).enqueue(new Callback<OperatorCodeBase>() {
            @Override
            public void onResponse(@NotNull Call<OperatorCodeBase> call, @NotNull Response<OperatorCodeBase> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ArrayList<OperatorCodeResponse> operatorCodeResponses = response.body().getResponses();
                    ArrayList<OperatorCodeResponse> filterList = new ArrayList<>();
                    for (OperatorCodeResponse operator : operatorCodeResponses) {
                        if (operator.getServiceType().equals(type)) {
                            filterList.add(operator);
                        }
                    }
                    OperationCodesAdapter categoryDropDownAdapter = new OperationCodesAdapter(MobileRechargeActivity.this, filterList);
                    binding.spinnerOperator.setAdapter(categoryDropDownAdapter);
                    binding.spinnerOperator.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            OperatorCodeResponse codeResponse = (OperatorCodeResponse) parent.getItemAtPosition(position);
                            opid = codeResponse.getOperatorID();
                            if (circleCode != null && opid != null)
                                getRechargePlans();
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                }
            }

            @Override
            public void onFailure(@NotNull Call<OperatorCodeBase> call, @NotNull Throwable t) {

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

    public void handlePaymentSuccessful() {
        double amount = Double.parseDouble(binding.etAmount.getText().toString());
        walletBalance = walletBalance + (amount - walletBalance);
        binding.tvWalletBalance.setText(getString(R.string.rs) + (walletBalance));
        getOrderId();
    }
}