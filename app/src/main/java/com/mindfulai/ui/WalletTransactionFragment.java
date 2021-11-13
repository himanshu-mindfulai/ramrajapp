package com.mindfulai.ui;

import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.JsonObject;
import com.mindfulai.Adapter.PaymentAdapter;
import com.mindfulai.Models.payments.PaymentBase;
import com.mindfulai.Models.payments.PaymentRecords;
import com.mindfulai.NetworkRetrofit.ApiService;
import com.mindfulai.NetworkRetrofit.ApiUtils;
import com.mindfulai.Utils.SPData;
import com.mindfulai.ministore.R;
import com.mindfulai.ministore.databinding.ActivityPaymentHistoryBinding;
import com.mindfulai.ministore.databinding.FragmentWalletTransactionBinding;
import org.jetbrains.annotations.NotNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;

public class WalletTransactionFragment extends Fragment {
    public FragmentWalletTransactionBinding binding;
    public ArrayList<PaymentRecords> paymentRecords;
    public int totalProducts = 0, currentPage = 1;
    private PaymentAdapter paymentAdapter;
    private boolean mHasReachedBottomOnce;
    public final String purpose;
    public final String type;

    public WalletTransactionFragment(String purpose,String type) {
        // Required empty public constructor
        this.purpose = purpose;
        this.type = type;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        inflater.inflate(R.layout.fragment_wallet_transaction, container, false);
        binding = FragmentWalletTransactionBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        paymentRecords = new ArrayList<>();
        paymentAdapter = new PaymentAdapter(requireActivity(), this);
        binding.paymentsRv.setLayoutManager(new LinearLayoutManager(requireActivity()));
        binding.paymentsRv.setAdapter(paymentAdapter);
        binding.paymentsRv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!binding.paymentsRv.canScrollVertically(1) && !mHasReachedBottomOnce) {
                    mHasReachedBottomOnce = true;
                    if (paymentRecords.size() < totalProducts) {
                        getPayments();
                    }
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && getActivity() != null) {
            currentPage = 1;
            paymentRecords.clear();
            if (purpose.equals("referral_credit")&&type.isEmpty()) {
                getTotalReferral();
            }
            getPayments();
        }
    }

    public void getPayments() {
        ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());
        apiService.getPayments(currentPage, purpose,type).enqueue(new Callback<PaymentBase>() {
            @Override
            public void onResponse(@NotNull Call<PaymentBase> call, @NotNull Response<PaymentBase> response) {
                Log.e("TAG", "onResponse: " + response);
                if (response.isSuccessful() && response.body() != null) {
                    currentPage++;
                    if (mHasReachedBottomOnce)
                        mHasReachedBottomOnce = false;
                    totalProducts = response.body().getData().getTotalCount();
                    paymentRecords.addAll(paymentRecords.size(), response.body().getData().getRecords());
                    paymentAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(@NotNull Call<PaymentBase> call, @NotNull Throwable t) {

            }
        });
    }

    public void getTotalReferral() {
        ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());
        apiService.getTotalReferral().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NotNull Call<JsonObject> call, @NotNull Response<JsonObject> response) {
                Log.e("TAG", "getTotalReferral onResponse: " + response.body());
                try {
                    if (response.isSuccessful() && response.body().get("status").getAsInt() == 200 && totalProducts > 0 && !purpose.isEmpty()) {
                        binding.totalReferralCredit.setVisibility(View.VISIBLE);
                        binding.totalReferralCredit.setText("Total " + SPData.referralCreditTitle() + " " + getString(R.string.rs) + response.body().get("data").getAsInt());
                    } else
                        binding.totalReferralCredit.setVisibility(View.GONE);
                } catch (Exception e) {
                    Log.e("TAG", "onResponse: " + e);
                    binding.totalReferralCredit.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(@NotNull Call<JsonObject> call, @NotNull Throwable t) {
                binding.totalReferralCredit.setVisibility(View.GONE);
            }
        });
    }
}