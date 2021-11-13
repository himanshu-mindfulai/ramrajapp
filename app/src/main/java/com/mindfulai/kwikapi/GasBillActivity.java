package com.mindfulai.kwikapi;

import android.annotation.SuppressLint;
import android.content.Intent;
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
import com.mindfulai.Models.kwikapimodels.BillFetchResponse;
import com.mindfulai.Models.kwikapimodels.OperatorCodeBase;
import com.mindfulai.Models.kwikapimodels.OperatorCodeResponse;
import com.mindfulai.Models.kwikapimodels.RechargeResponse;
import com.mindfulai.NetworkRetrofit.ApiService;
import com.mindfulai.NetworkRetrofit.ApiUtils;
import com.mindfulai.Utils.CommonUtils;
import com.mindfulai.Utils.CustomProgressDialog;
import com.mindfulai.Utils.SPData;
import com.mindfulai.customclass.DoPayment;
import com.mindfulai.ministore.R;
import com.mindfulai.ministore.databinding.ActivityGasBillBinding;
import com.mindfulai.ministore.databinding.ActivityPostpaidRechargeBinding;
import com.razorpay.PaymentData;
import com.razorpay.PaymentResultWithDataListener;
import org.jetbrains.annotations.NotNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;

public class GasBillActivity extends AppCompatActivity implements PaymentResultWithDataListener {
    ActivityGasBillBinding binding;
    private double walletBalance;
    private String opid;
    private CustomProgressDialog customProgressDialog;
    private String type = "Gas";
    private DoPayment doPayment;
    private double clientBalance;
    private String refId="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater layoutInflater = getLayoutInflater();
        layoutInflater.inflate(R.layout.activity_gas_bill, null);
        binding = ActivityGasBillBinding.inflate(layoutInflater);
        setContentView(binding.getRoot());
        type = getIntent().getStringExtra("type");
        setTitle(type + " bill payment");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        doPayment = new DoPayment(GasBillActivity.this, "", "", "");

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
                if (validateFields()) {
                    binding.rechargeBtn.setText("Fetch bill");
                }
            }
        });
        binding.etOpt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (validateFields()) {
                    binding.rechargeBtn.setText("Fetch bill");
                }
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
                if (validateFields()) {
                    binding.rechargeBtn.setText("Fetch bill");
                }
            }
        });
        binding.rechargeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!SPData.getAppPreferences().getUsertoken().isEmpty()) {
                    if (validateFields()) {

                        if (binding.rechargeBtn.getText().toString().toLowerCase().contains("fetch bill")) {
                            fetchBill();
                        } else {
                            double amount = Double.parseDouble(binding.etAmount.getText().toString());
                            if (walletBalance >= amount) {
                                getOrderId();
                            } else {
                                doPayment.rechargeWallet(amount - walletBalance);
                            }
                        }
                    } else if (binding.etPhoneNo.getText().toString().isEmpty()) {
                        Toast.makeText(GasBillActivity.this, "Enter a valid phone number", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(GasBillActivity.this, "Enter a valid recharge amount", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    startActivity(new Intent(GasBillActivity.this, LoginActivity.class));
                }
            }
        });
    }

    private void fetchBill() {
        customProgressDialog = CommonUtils.showProgressDialog(GasBillActivity.this,
                "Fetching bill...");
        ApiService apiService = ApiUtils.getKwikAPIService();
        String opt2 = binding.etOpt.getText().toString();
        if (opt2.isEmpty()) {
            opt2 = "0";
        }
        apiService.getBill(getString(R.string.kwik_api_key), binding.etAccount.getText().toString(), opid, "3240", "0", "0000", binding.etPhoneNo.getText().toString(), opt2).enqueue(new Callback<BillFetchResponse>() {
            @Override
            public void onResponse(@NotNull Call<BillFetchResponse> call, @NotNull Response<BillFetchResponse> response) {
                Log.e("TAG", "onResponse: " + response);
                CommonUtils.hideProgressDialog(customProgressDialog);
                if (response.isSuccessful() && response.body() != null && (response.body().getStatus().equals("SUCCESS")||response.body().getStatus().equals("PENDING"))) {
                    String amount = response.body().getDue_amount();
                    refId = response.body().getRef_id();
                    Toast.makeText(GasBillActivity.this, "Total due amount Rs." + amount, Toast.LENGTH_SHORT).show();
                    binding.etAmount.setText(amount);
                    binding.amntTitle.setVisibility(View.VISIBLE);
                    binding.etAmount.setVisibility(View.VISIBLE);
                    binding.errorMsg.setVisibility(View.GONE);
                    binding.rechargeBtn.setText("Bill payment");
                } else if (response.body() != null) {
                    binding.errorMsg.setText("Error msg: " + response.body().getMessage());
                    binding.errorMsg.setVisibility(View.VISIBLE);
                    binding.rechargeBtn.setText("Fetch bill");
                }
            }

            @Override
            public void onFailure(@NotNull Call<BillFetchResponse> call, @NotNull Throwable t) {
                CommonUtils.hideProgressDialog(customProgressDialog);
                binding.rechargeBtn.setText("Fetch bill");
            }
        });

    }

    private void completeRecharge(long orderID) {
        ApiService apiService = ApiUtils.getKwikAPIService();
        String opt2 = binding.etOpt.getText().toString();
        if (opt2.isEmpty()) {
            opt2 = "0";
        }
        apiService.billsRecharge(getString(R.string.kwik_api_key), binding.etAccount.getText().toString(), opid, binding.etAmount.getText().toString(), "0", "" + orderID, binding.etPhoneNo.getText().toString(), opt2,refId).enqueue(new Callback<RechargeResponse>() {
            @Override
            public void onResponse(@NotNull Call<RechargeResponse> call, @NotNull Response<RechargeResponse> response) {
                Log.e("TAG", "completeRecharge onResponse: "+response);
                handleRechargeResponse(response);
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
                CommonUtils.showErrorMessage(GasBillActivity.this, "" + t.getMessage());
            }
        });
    }

    private void getOrderId() {
        customProgressDialog = CommonUtils.showProgressDialog(GasBillActivity.this,
                getString(R.string.please_wait));
        ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());
        apiService.getKwikOrderID().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NotNull Call<JsonObject> call, @NotNull Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    long orderID = response.body().get("data").getAsLong();
                    completeRecharge(orderID);
                } else {
                    CommonUtils.hideProgressDialog(customProgressDialog);
                    CommonUtils.showErrorMessage(GasBillActivity.this, "" + response.message());
                }
            }

            @Override
            public void onFailure(@NotNull Call<JsonObject> call, @NotNull Throwable t) {
                CommonUtils.hideProgressDialog(customProgressDialog);
                CommonUtils.showErrorMessage(GasBillActivity.this, "" + t.getMessage());
            }
        });

    }


    private void handleRechargeResponse(Response<RechargeResponse> response) {
        if (response.isSuccessful() && response.body() != null && response.body().getStatus().equals("SUCCESS")) {
            deductAmtFromWallet(Double.parseDouble(binding.etAmount.getText().toString()));
            binding.etPhoneNo.setText("");
            binding.etAmount.setText("");
            CommonUtils.showSuccessMessage(GasBillActivity.this, "" + response.body().getMessage());
        } else if (response.body() != null) {
            CommonUtils.hideProgressDialog(customProgressDialog);
            if (!response.body().getMessage().isEmpty())
                CommonUtils.showErrorMessage(GasBillActivity.this, "" + response.body().getMessage());
            else
                CommonUtils.showErrorMessage(GasBillActivity.this, "Recharge Failed");
        } else {
            CommonUtils.hideProgressDialog(customProgressDialog);
            CommonUtils.showErrorMessage(GasBillActivity.this, "" + response.message());
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
                    CommonUtils.showSuccessMessage(GasBillActivity.this, "Recharge Successful");
                    finish();
                }
            }

            @Override
            public void onFailure(@NotNull Call<JsonObject> call, @NotNull Throwable t) {
                CommonUtils.showErrorMessage(GasBillActivity.this, "" + t.getMessage());
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
                    OperationCodesAdapter categoryDropDownAdapter = new OperationCodesAdapter(GasBillActivity.this, filterList);
                    binding.spinnerOperator.setAdapter(categoryDropDownAdapter);
                    binding.spinnerOperator.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            OperatorCodeResponse codeResponse = (OperatorCodeResponse) parent.getItemAtPosition(position);
                            opid = codeResponse.getOperatorID();
                            Log.e("TAG", "onItemSelected: " + codeResponse.getMessage().split("\\bin\\b")[0]);
                            String msg = codeResponse.getMessage().split("\\bin\\b")[0];
                            String title = msg;
                            if (msg.contains("Pass"))
                                title = msg.replace("Pass", "");
                            else if (msg.contains("pass"))
                                title = msg.replace("pass", "");
                            else if (msg.contains("Paas"))
                                title = msg.replace("Paas", "");
                            else if (msg.contains("paas"))
                                title = msg.replace("paas", "");

                            binding.accountTitle.setText(title);
                            binding.etAccount.setHint("Enter" + title);
                            String[] options = codeResponse.getMessage().split("\\bin\\b", -2);
                            if (options.length > 1) {
                                String opt = options[1];
                                Log.e("TAG", "onItemSelected: " + opt);
                                String optitle = "";
                                if (opt.contains("pass")) {
                                    optitle = opt.split("\\bpass\\b")[1];
                                } else if (opt.contains(",")) {
                                    optitle = opt.split(",")[1];
                                } else if (opt.contains("and")) {
                                    optitle = opt.split("\\band\\b")[1];
                                }
                                if (!optitle.isEmpty()) {
                                    binding.optTitle.setText(optitle);
                                    binding.etOpt.setHint("Enter " + optitle);
                                    binding.optTitle.setVisibility(View.VISIBLE);
                                    binding.etOpt.setVisibility(View.VISIBLE);
                                } else {
                                    binding.optTitle.setVisibility(View.GONE);
                                    binding.etOpt.setVisibility(View.GONE);
                                }
                            }
                            if (validateFields()) {
                                binding.rechargeBtn.setText("Fetch bill");
                            }
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

    private boolean validateFields() {
        if (binding.etOpt.getVisibility() == View.GONE)
            return binding.etPhoneNo.getText().length() == 10 && !binding.etAccount.getText().toString().isEmpty() && !binding.etPhoneNo.getText().toString().isEmpty();
        else
            return binding.etPhoneNo.getText().length() == 10 && !binding.etAccount.getText().toString().isEmpty() && !binding.etPhoneNo.getText().toString().isEmpty() && !binding.etOpt.getText().toString().isEmpty();
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