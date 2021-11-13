package com.mindfulai.Activites;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.mindfulai.Adapter.VendorByCategoryAdapter;
import com.mindfulai.Models.VendorBase;
import com.mindfulai.NetworkRetrofit.ApiService;
import com.mindfulai.NetworkRetrofit.ApiUtils;
import com.mindfulai.Utils.CommonUtils;
import com.mindfulai.Utils.SPData;
import com.mindfulai.ministore.R;
import com.mindfulai.ministore.databinding.ActivityVendorByCategoryBinding;

import org.jetbrains.annotations.NotNull;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VendorByCategoryActivity extends AppCompatActivity {

    public ActivityVendorByCategoryBinding binding;
    public String categoryId = "";
    private VendorByCategoryAdapter vendorByCategoryAdapter;

    @SuppressLint("InflateParams")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater layoutInflater = getLayoutInflater();
        layoutInflater.inflate(R.layout.activity_vendor_by_category, null);
        binding = ActivityVendorByCategoryBinding.inflate(layoutInflater);
        setContentView(binding.getRoot());
        getSupportActionBar().setTitle(CommonUtils.capitalizeWord(getIntent().getStringExtra("categoryName")));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        binding.vendorList.setLayoutManager(manager);
        binding.searchVendors.setHint("Search "+SPData.noOfVendorTitle());
        binding.searchVendors.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                vendorByCategoryAdapter.filterByName(binding.searchVendors.getText().toString());
            }
        });
        getAllVendorByCategory();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void getAllVendorByCategory() {
        categoryId = getIntent().getStringExtra("category_id");
        ApiService apiService = ApiUtils.getAPIService();
        apiService.getVendorByCategoryId(categoryId).enqueue(new Callback<VendorBase>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(@NotNull Call<VendorBase> call, @NotNull Response<VendorBase> response) {
                Log.e("TAG", "onResponse: "+response);
                if (response.isSuccessful() && response.body() != null && response.body().getData().size() > 0) {
                    binding.noVendors.setText(response.body().getData().size()+" "+ SPData.noOfVendorTitle());
                    vendorByCategoryAdapter = new VendorByCategoryAdapter(VendorByCategoryActivity.this, response.body().getData());
                    binding.vendorList.setAdapter(vendorByCategoryAdapter);
                    vendorByCategoryAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(@NotNull Call<VendorBase> call, @NotNull Throwable t) {

            }
        });
    }

}