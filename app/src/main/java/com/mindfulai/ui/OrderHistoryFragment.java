package com.mindfulai.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.badoualy.datepicker.DatePickerTimeline;
import com.mindfulai.Adapter.OrderHistoryAdapter;
import com.mindfulai.Models.AllOrderHistory.Datum;
import com.mindfulai.Models.AllOrderHistory.OrderHistory;
import com.mindfulai.NetworkRetrofit.ApiService;
import com.mindfulai.NetworkRetrofit.ApiUtils;
import com.mindfulai.Utils.CommonUtils;
import com.mindfulai.Utils.CustomProgressDialog;
import com.mindfulai.Utils.SPData;
import com.mindfulai.ministore.R;
import com.mindfulai.ministore.databinding.FragmentOrderHistoryBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;

public class OrderHistoryFragment extends Fragment {
    private List<Datum> orderHistoryDetailsList;
    private OrderHistoryAdapter orderHistoryAdapter;
    FragmentOrderHistoryBinding binding;
    private boolean mHasReachedBottomOnce;
    public int totalOrders = 0, currentPage = 1;
    private String selectedDateTime;
    private SimpleDateFormat simpleDateFormat;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        inflater.inflate(R.layout.fragment_order_history, container, false);
        binding = FragmentOrderHistoryBinding.inflate(inflater);
        return binding.getRoot();
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            simpleDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy");
            binding.shimmerViewContainer.startShimmerAnimation();
            LinearLayoutManager horizontalLayoutManagaer = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);


            binding.datePicker.setOnDateSelectedListener((year, month, day, index) -> {
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, day);
                filterOrderByDate(calendar.getTime());
            });
            if (SPData.showOrderHistoryByHorizontalDate()) {
                binding.datePicker.setVisibility(View.VISIBLE);
                selectedDateTime = simpleDateFormat.format(Calendar.getInstance().getTime());
            } else {
                binding.datePicker.setVisibility(View.GONE);
            }
            binding.recyclerViewOrders.setLayoutManager(horizontalLayoutManagaer);
            orderHistoryDetailsList = new ArrayList<>();
            orderHistoryAdapter = new OrderHistoryAdapter(getActivity(), orderHistoryDetailsList);
            binding.recyclerViewOrders.setAdapter(orderHistoryAdapter);
            currentPage = 1;

            binding.recyclerViewOrders.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    if (!recyclerView.canScrollVertically(1) && !mHasReachedBottomOnce) {
                        mHasReachedBottomOnce = true;
                        if (orderHistoryDetailsList.size() < totalOrders) {
                            Log.e("TAG", "onScrolled: ");
                            getOrderHistory();
                        }
                    }
                }

                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                }
            });
            if (!SPData.getAppPreferences().getUsertoken().isEmpty()) {
                binding.noOrderLayout.setVisibility(View.GONE);
                getOrderHistory();
            } else {
                binding.noOrderLayout.setVisibility(View.VISIBLE);
                binding.shimmerViewContainer.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Log.e("TAG", "onViewCreated: " + e);
            noOrderFound();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (Objects.requireNonNull(data.getStringExtra("for")).equals("order"))
                orderHistoryAdapter.cancelledOrder(data.getIntExtra("position", -1));
            else {
                orderHistoryAdapter.changeProductStatus(data.getIntExtra("position", -1), data.getIntExtra("product_position", -1), data.getStringExtra("action"));
            }
        }
    }

    private void getOrderHistory() {

        final CustomProgressDialog customProgressDialog = CommonUtils.showProgressDialog(getActivity(),
                "Getting orders ...");
        ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());
        apiService.getOrderHistory(currentPage).enqueue(new Callback<OrderHistory>() {
            @Override
            public void onResponse(@NonNull Call<OrderHistory> call, @NonNull Response<OrderHistory> response) {
                CommonUtils.hideProgressDialog(customProgressDialog);
                binding.shimmerViewContainer.stopShimmerAnimation();
                binding.shimmerViewContainer.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    currentPage++;
                    if (mHasReachedBottomOnce)
                        mHasReachedBottomOnce = false;
                    totalOrders = response.body().getData().getTotalCount();
                    orderHistoryAdapter.allOrders.addAll(response.body().getData().getRecords());

                    if (SPData.showOrderHistoryByHorizontalDate() && orderHistoryAdapter.allOrders.size() > 0 && selectedDateTime != null) {
                        orderHistoryAdapter.filterByDate(selectedDateTime);
                    } else if (orderHistoryAdapter.allOrders.size() > 0) {
                        orderHistoryDetailsList.addAll(response.body().getData().getRecords());
                        orderHistoryAdapter.notifyDataSetChanged();
                    } else {
                        orderHistoryAdapter.allOrders.size();
                        noOrderFound();
                    }

                }
            }

            @Override
            public void onFailure(@NonNull Call<OrderHistory> call, @NonNull Throwable t) {
                Log.e("TAG", t.toString());
                CommonUtils.hideProgressDialog(customProgressDialog);
                Toast.makeText(getActivity(), "Failed to connect", Toast.LENGTH_SHORT).show();
                noOrderFound();

            }
        });
    }

    private void noOrderFound() {
        binding.shimmerViewContainer.stopShimmerAnimation();
        binding.shimmerViewContainer.setVisibility(View.GONE);
        binding.noOrderLayout.setVisibility(View.VISIBLE);
    }

    private void filterOrderByDate(Date date) {
        String time = simpleDateFormat.format(date);
        orderHistoryAdapter.filterByDate(time);
        selectedDateTime = time;
    }
}
