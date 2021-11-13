package com.mindfulai.kwikapi;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.google.gson.JsonObject;
import com.mindfulai.Activites.LoginActivity;
import com.mindfulai.Adapter.OperationCodesAdapter;
import com.mindfulai.Models.CustomerInfo.CustomerData;
import com.mindfulai.Models.WalletRechargeModel.WalletRechargeModel;
import com.mindfulai.Models.kwikapimodels.BillFetchResponse;
import com.mindfulai.Models.kwikapimodels.OperatorCodeBase;
import com.mindfulai.Models.kwikapimodels.OperatorCodeResponse;
import com.mindfulai.Models.kwikapimodels.RechargeResponse;
import com.mindfulai.Models.orderDetailInfo.OrderDetailInfo;
import com.mindfulai.NetworkRetrofit.ApiService;
import com.mindfulai.NetworkRetrofit.ApiUtils;
import com.mindfulai.Utils.CommonUtils;
import com.mindfulai.Utils.CustomProgressDialog;
import com.mindfulai.Utils.SPData;
import com.mindfulai.customclass.DoPayment;
import com.mindfulai.ministore.R;
import com.mindfulai.ministore.databinding.ActivityMobileRechargeBinding;
import com.mindfulai.ministore.databinding.ActivityPostpaidRechargeBinding;
import com.razorpay.Checkout;
import com.razorpay.PaymentData;
import com.razorpay.PaymentResultWithDataListener;
import com.valdesekamdem.library.mdtoast.MDToast;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class PostpaidRechargeActivity extends AppCompatActivity implements PaymentResultWithDataListener {
    ActivityPostpaidRechargeBinding binding;
    private double walletBalance;
    private String opid;
    private CustomProgressDialog customProgressDialog;
    private String type = "";
    private DoPayment doPayment;
    private double clientBalance;
    private String refrenceId;

    @SuppressLint("InflateParams")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater layoutInflater = getLayoutInflater();
        layoutInflater.inflate(R.layout.activity_postpaid_recharge, null);
        binding = ActivityPostpaidRechargeBinding.inflate(layoutInflater);
        setContentView(binding.getRoot());
        type = getIntent().getStringExtra("type");
        setTitle(type + " bill payment");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        doPayment = new DoPayment(PostpaidRechargeActivity.this, "", "", "");
        if (typeIsFastag()) {
            binding.accountTitle.setVisibility(View.VISIBLE);
            binding.etAccount.setVisibility(View.VISIBLE);
        } else {
            binding.accountTitle.setVisibility(View.GONE);
            binding.etAccount.setVisibility(View.GONE);
        }

        getWalletBalance();

        getAllOperatorCode();

        binding.etPhoneNo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                checkTypeToFetchBill();
            }
        });

        binding.etAccount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                checkTypeToFetchBill();
            }
        });

        binding.rechargeBtn.setOnClickListener(v -> {
            if (!SPData.getAppPreferences().getUsertoken().isEmpty()) {
                if (binding.rechargeBtn.getText().toString().toLowerCase().contains("fetch bill")) {
                    if (typeIsFastag())
                        fetchBill(binding.etAccount.getText().toString());
                    else
                        fetchBill(binding.etPhoneNo.getText().toString());
                } else {
                    if (CommonUtils.stringIsNotNullAndEmpty(binding.etPhoneNo.getText().toString()) && CommonUtils.stringIsNotNullAndEmpty(binding.etAmount.getText().toString())) {
                        if (typeIsFastag() && !binding.etAccount.getText().toString().isEmpty()) {
                            completeOrder();
                        } else if (typeIsFastag()) {
                            Toast.makeText(PostpaidRechargeActivity.this, "Fields required", Toast.LENGTH_SHORT).show();
                        } else {
                            completeOrder();
                        }
                    } else if (binding.etPhoneNo.getText().toString().isEmpty()) {
                        Toast.makeText(PostpaidRechargeActivity.this, "Enter a valid phone number", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(PostpaidRechargeActivity.this, "Enter a valid recharge amount", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                startActivity(new Intent(PostpaidRechargeActivity.this, LoginActivity.class));
            }
        });
    }

    private boolean typeIsFastag() {
        return type.toLowerCase().contains("fastag");
    }

    private void completeOrder() {
        double amount = Double.parseDouble(binding.etAmount.getText().toString());
        if (walletBalance >= amount) {
            getOrderId();
        } else {
            doPayment.rechargeWallet(amount - walletBalance);
        }
    }

    private void checkTypeToFetchBill() {
        if (typeIsFastag() && !binding.etAccount.getText().toString().isEmpty() && binding.etPhoneNo.getText().toString().length() == 10) {
            binding.rechargeBtn.setText("Fetch bill");
        } else if (!typeIsFastag() && binding.etPhoneNo.getText().toString().length() == 10)
            binding.rechargeBtn.setText("Fetch bill");
    }

    private void fetchBill(String number) {
        customProgressDialog = CommonUtils.showProgressDialog(PostpaidRechargeActivity.this,
                "Fetching bill...");
        ApiService apiService = ApiUtils.getKwikAPIService();
        apiService.getBill(getString(R.string.kwik_api_key), number, opid, "0", "0", "0000", binding.etPhoneNo.getText().toString(), "0").enqueue(new Callback<BillFetchResponse>() {
            @Override
            public void onResponse(@NotNull Call<BillFetchResponse> call, @NotNull Response<BillFetchResponse> response) {
                CommonUtils.hideProgressDialog(customProgressDialog);
                Log.e("TAG", "onResponse: " + response);
                if (response.isSuccessful() && response.body() != null && response.body().getStatus().equals("SUCCESS")) {
                    String amount = response.body().getDue_amount();
                    refrenceId = response.body().getRef_id();
                    binding.rechargeBtn.setText("Bill payment");
                    Toast.makeText(PostpaidRechargeActivity.this, "Total due amount Rs." + amount, Toast.LENGTH_SHORT).show();
                    binding.etAmount.setText(amount);
                    binding.errorMsg.setVisibility(View.GONE);
                } else if (response.body() != null) {
                    binding.errorMsg.setText("Error msg: " + response.body().getMessage());
                    binding.errorMsg.setVisibility(View.VISIBLE);
                    if (typeIsFastag()) {
                        binding.rechargeBtn.setText("Fetch bill");
                    } else {
                        binding.rechargeBtn.setText("Bill payment");
                    }
                }
            }

            @Override
            public void onFailure(@NotNull Call<BillFetchResponse> call, @NotNull Throwable t) {
                CommonUtils.hideProgressDialog(customProgressDialog);
                if (typeIsFastag()) {
                    binding.rechargeBtn.setText("Fetch bill");
                } else {
                    binding.rechargeBtn.setText("Bill payment");
                }
                Toast.makeText(PostpaidRechargeActivity.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void completeRecharge(String account, long orderID) {
        ApiService apiService = ApiUtils.getKwikAPIService();
        apiService.postpaidFastagRecharge(getString(R.string.kwik_api_key), account, opid, binding.etAmount.getText().toString(), "0", "" + orderID,refrenceId).enqueue(new Callback<RechargeResponse>() {
            @Override
            public void onResponse(@NotNull Call<RechargeResponse> call, @NotNull Response<RechargeResponse> response) {
                handleRechargeResponse(call, response);
            }

            @Override
            public void onFailure(@NotNull Call<RechargeResponse> call, @NotNull Throwable t) {
                Log.e("TAG", "onFailure: " + t.getMessage());
                CommonUtils.hideProgressDialog(customProgressDialog);
                getKwikBalance();
            }
        });
    }

    private void getKwikBalance() {
        ApiService apiService = ApiUtils.getKwikAPIService();
        apiService.getBalance(getString(R.string.kwik_api_key)).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NotNull Call<JsonObject> call, @NotNull Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    double currentBalance = response.body().get("response").getAsJsonObject().get("balance").getAsDouble();
                    if (currentBalance < clientBalance) {
                        clientBalance = currentBalance;
                        deductAmtFromWallet(Double.parseDouble(binding.etAmount.getText().toString()));
                        binding.etPhoneNo.setText("");
                        binding.etAmount.setText("");
                    } else if (clientBalance == 0) {
                        clientBalance = currentBalance;
                    }
                }
            }

            @Override
            public void onFailure(@NotNull Call<JsonObject> call, @NotNull Throwable t) {
                CommonUtils.hideProgressDialog(customProgressDialog);
                CommonUtils.showErrorMessage(PostpaidRechargeActivity.this, "" + t.getMessage());
            }
        });
    }

    private void getOrderId() {
        customProgressDialog = CommonUtils.showProgressDialog(PostpaidRechargeActivity.this,
                getString(R.string.please_wait));
        ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());
        apiService.getKwikOrderID().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NotNull Call<JsonObject> call, @NotNull Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    long orderID = response.body().get("data").getAsLong();
                    if (typeIsFastag()) {
                        completeRecharge(binding.etAccount.getText().toString(), orderID);
                    } else
                        completeRecharge(binding.etPhoneNo.getText().toString(), orderID);

                } else {
                    CommonUtils.hideProgressDialog(customProgressDialog);
                    CommonUtils.showErrorMessage(PostpaidRechargeActivity.this, "" + response.message());
                }
            }

            @Override
            public void onFailure(@NotNull Call<JsonObject> call, @NotNull Throwable t) {
                CommonUtils.hideProgressDialog(customProgressDialog);
                CommonUtils.showErrorMessage(PostpaidRechargeActivity.this, "" + t.getMessage());
            }
        });

    }


    private void handleRechargeResponse(Call<RechargeResponse> call, Response<RechargeResponse> response) {
        if (response.isSuccessful() && response.body() != null && (response.body().getStatus().equals("SUCCESS")||response.body().getStatus().equals("PENDING"))) {
            deductAmtFromWallet(Double.parseDouble(binding.etAmount.getText().toString()));
            binding.etPhoneNo.setText("");
            binding.etAmount.setText("");
            CommonUtils.showSuccessMessage(PostpaidRechargeActivity.this, "" + response.body().getMessage());
        } else if (response.body() != null) {
            CommonUtils.hideProgressDialog(customProgressDialog);
            if (!response.body().getMessage().isEmpty())
                CommonUtils.showErrorMessage(PostpaidRechargeActivity.this, "" + response.body().getMessage());
            else
                CommonUtils.showErrorMessage(PostpaidRechargeActivity.this, "Recharge Failed");
        } else {
            CommonUtils.hideProgressDialog(customProgressDialog);
            CommonUtils.showErrorMessage(PostpaidRechargeActivity.this, "" + response.message());
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
                if (response.isSuccessful() && response.body() != null) {
                    CommonUtils.showSuccessMessage(PostpaidRechargeActivity.this, "Recharge Successful");
                    finish();
                }
            }

            @Override
            public void onFailure(@NotNull Call<JsonObject> call, @NotNull Throwable t) {
                CommonUtils.showErrorMessage(PostpaidRechargeActivity.this, "" + t.getMessage());
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
            binding.rechargeBtn.setText("Fetch bill");
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
                        if (operator.getServiceType().toLowerCase().contains(type.toLowerCase())) {
                            filterList.add(operator);
                        }
                    }
                    OperationCodesAdapter categoryDropDownAdapter = new OperationCodesAdapter(PostpaidRechargeActivity.this, filterList);
                    binding.spinnerOperator.setAdapter(categoryDropDownAdapter);
                    binding.spinnerOperator.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            OperatorCodeResponse codeResponse = (OperatorCodeResponse) parent.getItemAtPosition(position);
                            opid = codeResponse.getOperatorID();
                            if (typeIsFastag()) {
                                String title = codeResponse.getMessage().split("\\bin\\b")[0].replace("pass", "");
                                binding.accountTitle.setText(title);
                                binding.etAccount.setHint("Enter" + title);
                            }
                            checkTypeToFetchBill();
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