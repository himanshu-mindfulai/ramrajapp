package com.mindfulai.Activites;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.mindfulai.Adapter.FaqAdapter;
import com.mindfulai.Models.faq.FaqBase;
import com.mindfulai.NetworkRetrofit.ApiService;
import com.mindfulai.NetworkRetrofit.ApiUtils;
import com.mindfulai.ministore.R;
import com.mindfulai.ministore.databinding.ActivityFaqBinding;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;

public class FAQActivity extends AppCompatActivity {
    private static final String TAG = "FAQActivity";
    private TextView textView;
    private ProgressBar progressBar;
    private JSONArray jsonArray;
    ActivityFaqBinding binding;

    @SuppressLint("InflateParams")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

            LayoutInflater layoutInflater = getLayoutInflater();
            layoutInflater.inflate(R.layout.activity_faq, null);
            binding = ActivityFaqBinding.inflate(layoutInflater);
            setContentView(binding.getRoot());
            Objects.requireNonNull(getSupportActionBar()).setTitle("FAQ");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            binding.recyclerViewFaq.setLayoutManager(new LinearLayoutManager(this));
            getFAQ();

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void getFAQ() {
        ApiService apiService = ApiUtils.getHeaderAPIService();
        apiService.getAllFaq().enqueue(new Callback<FaqBase>() {
            @Override
            public void onResponse(@NotNull Call<FaqBase> call, @NotNull retrofit2.Response<FaqBase> response) {
                if (response.isSuccessful() && response.body() != null) {
                    FaqAdapter faqAdapter = new FaqAdapter(FAQActivity.this, response.body().getData());
                    binding.recyclerViewFaq.setAdapter(faqAdapter);
                    faqAdapter.notifyDataSetChanged();
                    binding.progressBarFAQ.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(@NotNull Call<FaqBase> call, @NotNull Throwable t) {
                binding.progressBarFAQ.setVisibility(View.GONE);
            }
        });
    }


}
