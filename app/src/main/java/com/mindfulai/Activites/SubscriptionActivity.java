package com.mindfulai.Activites;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mindfulai.Adapter.SubscriptionsAdapter;
import com.mindfulai.Models.subscription.DaysDataModel;
import com.mindfulai.Models.subscription.SubscriptionBaseModel;
import com.mindfulai.Models.subscription.SubscriptionDataModel;
import com.mindfulai.NetworkRetrofit.ApiService;
import com.mindfulai.NetworkRetrofit.ApiUtils;
import com.mindfulai.Utils.SPData;
import com.mindfulai.ministore.R;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SubscriptionActivity extends AppCompatActivity {
    ProgressBar progressBar;
    RecyclerView recyclerView;
    ArrayList<SubscriptionDataModel> subscriptionDataModelArrayList;
    private SubscriptionsAdapter subscriptionsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription);
        getSupportActionBar().setTitle("Subscriptions");
        progressBar = findViewById(R.id.progressBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        recyclerView = findViewById(R.id.recyclerview_subscriptions);
        LinearLayoutManager manager = new LinearLayoutManager(SubscriptionActivity.this);
        recyclerView.setLayoutManager(manager);
        getAllSubscriptions();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            DaysDataModel daysDataModel = data.getParcelableExtra("daysQty");
            int position = data.getIntExtra("position", -1);
            subscriptionsAdapter.changeDaysData(daysDataModel, position);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void getAllSubscriptions() {

        ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());
        apiService.getSubscriptions().enqueue(new Callback<SubscriptionBaseModel>() {
            @Override
            public void onResponse(@NonNull Call<SubscriptionBaseModel> call, @NonNull Response<SubscriptionBaseModel> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    SubscriptionBaseModel orderHistory = response.body();
                    assert orderHistory != null;
                    subscriptionDataModelArrayList = orderHistory.getData();
                    if (subscriptionDataModelArrayList != null) {
                        subscriptionsAdapter = new SubscriptionsAdapter(SubscriptionActivity.this, subscriptionDataModelArrayList);
                        recyclerView.setAdapter(subscriptionsAdapter);
                        subscriptionsAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<SubscriptionBaseModel> call, @NonNull Throwable t) {
                Log.e("TAG", t.toString());
                Toast.makeText(SubscriptionActivity.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}