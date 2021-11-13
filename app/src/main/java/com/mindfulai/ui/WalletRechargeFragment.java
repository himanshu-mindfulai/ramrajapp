package com.mindfulai.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.gson.JsonObject;
import com.mindfulai.Activites.LoginActivity;
import com.mindfulai.Models.CustomerInfo.CustomerData;
import com.mindfulai.NetworkRetrofit.ApiService;
import com.mindfulai.NetworkRetrofit.ApiUtils;
import com.mindfulai.Utils.CommonUtils;
import com.mindfulai.Utils.SPData;
import com.mindfulai.customclass.DoPayment;
import com.mindfulai.ministore.R;
import com.mindfulai.ministore.databinding.FragmentWalletRechargeBinding;
import com.razorpay.PaymentData;
import com.razorpay.PaymentResultWithDataListener;
import com.valdesekamdem.library.mdtoast.MDToast;
import org.jetbrains.annotations.NotNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WalletRechargeFragment extends Fragment implements View.OnClickListener, PaymentResultWithDataListener {


    public WalletRechargeFragment() {
        // Required empty public constructor
    }


    FragmentWalletRechargeBinding binding;
    String symbolRupee;
    public DoPayment doPayment;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        inflater.inflate(R.layout.fragment_wallet_recharge, container, false);
        binding = FragmentWalletRechargeBinding.inflate(inflater);
        symbolRupee = getString(R.string.rs) + " ";
        doPayment = new DoPayment(requireActivity(), "", "", "");
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.walletLayout.btnPay.setOnClickListener(this);
        binding.btnDocs.setOnClickListener(this);

        if (SPData.showUploadDocsBtn()) {
            binding.btnDocs.setVisibility(View.VISIBLE);
        } else {
            binding.btnDocs.setVisibility(View.GONE);
        }

        if (SPData.useSecondWalletLayout()) {
            binding.walletHeader.setVisibility(View.GONE);
            binding.walletHeader2.setVisibility(View.VISIBLE);
            binding.walletLayout.walletLl.setVisibility(View.GONE);
        } else {
            binding.walletHeader.setVisibility(View.VISIBLE);
            binding.walletHeader2.setVisibility(View.GONE);
            binding.walletLayout.walletLl.setVisibility(View.VISIBLE);
        }

        binding.topupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WalletRechargeBottomSheetFragment walletRechargeBottomSheetFragment = new WalletRechargeBottomSheetFragment(WalletRechargeFragment.this);
                walletRechargeBottomSheetFragment.show(requireActivity().getSupportFragmentManager(), "");
            }
        });
        binding.walletLayout.etEnterAmount.setText(symbolRupee);
        Selection.setSelection(binding.walletLayout.etEnterAmount.getText(), binding.walletLayout.etEnterAmount.getText().length());
        binding.walletLayout.etEnterAmount.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().startsWith(symbolRupee)) {
                    binding.walletLayout.etEnterAmount.setText(symbolRupee);
                    Selection.setSelection(binding.walletLayout.etEnterAmount.getText(), binding.walletLayout.etEnterAmount.getText().length());
                }
            }
        });

        if (getActivity() != null)
            getWalletBalance();
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
        Toast.makeText(requireActivity(), "Payment failed", Toast.LENGTH_SHORT).show();
    }

    private void getWalletBalance() {
        ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());
        apiService.getProfileDetails().enqueue(new Callback<CustomerData>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(@NotNull Call<CustomerData> call, @NotNull Response<CustomerData> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CustomerData customerDataResponse = response.body();
                    Intent intent = getActivity().getIntent();
                    float walletAmt = customerDataResponse.getData().getUser().getWallet();
                    intent.putExtra("balance", walletAmt);
                    getActivity().setResult(Activity.RESULT_OK, intent);
                    binding.tvWalletBalance.setText(getString(R.string.rs) + CommonUtils.getTwoDigitsRoundOffValue(walletAmt));
                    binding.tvWalletBalance2.setText(getString(R.string.rs) + CommonUtils.getTwoDigitsRoundOffValue(customerDataResponse.getData().getUser().getWallet()));
                    if (SPData.lowBalanceAmt() >= walletAmt && SPData.useSecondWalletLayout()) {
                        binding.tvWalletBalanceMsg.setVisibility(View.VISIBLE);
                    } else
                        binding.tvWalletBalanceMsg.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(@NotNull Call<CustomerData> call, @NotNull Throwable t) {
                t.printStackTrace();
            }
        });
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        if (v == binding.walletLayout.btnPay) {
            handleButtonPayClick(binding.walletLayout.etEnterAmount.getText().toString().replaceAll(symbolRupee, ""));
        } else {
            openDocsScreen();
        }
    }

    public void handleButtonPayClick(String amount) {
        if (!SPData.getAppPreferences().getUsertoken().isEmpty()) {
            if (amount.isEmpty()) {
                MDToast.makeText(requireActivity(), "Enter an amount", MDToast.TYPE_INFO).show();
                return;
            }
            doPayment.rechargeWallet(Double.parseDouble(amount));
        } else {
            Toast.makeText(requireActivity(), "Please login to do wallet recharge", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(requireActivity(), LoginActivity.class));
        }
    }

    public void handlePaymentSuccessful() {
        binding.walletLayout.etEnterAmount.setText("");
        getWalletBalance();
    }


    public void openDocsScreen() {

    }


}