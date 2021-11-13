package com.mindfulai.Activites;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.JsonObject;
import com.mindfulai.Adapter.AllCouponsAdapter;
import com.mindfulai.Models.CartInformation.CartDetailsInformation;
import com.mindfulai.Models.coupon.CouponBaseModel;
import com.mindfulai.Models.coupon.CouponDataModel;
import com.mindfulai.NetworkRetrofit.ApiService;
import com.mindfulai.NetworkRetrofit.ApiUtils;
import com.mindfulai.Utils.CommonUtils;
import com.mindfulai.Utils.CustomProgressDialog;
import com.mindfulai.Utils.SPData;
import com.mindfulai.ministore.R;

import com.mindfulai.ministore.databinding.ActivityPromoCodeBinding;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.view.View.GONE;

public class PromoCodeActivity extends AppCompatActivity implements TextWatcher {
    public double subtotal;
    private Intent intent;
    public ActivityPromoCodeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater layoutInflater = getLayoutInflater();
        layoutInflater.inflate(R.layout.activity_promo_code, null);
        binding = ActivityPromoCodeBinding.inflate(layoutInflater);
        setContentView(binding.getRoot());
        getSupportActionBar().setTitle("Apply Promo Code");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        binding.recyclerViewCoupons.setLayoutManager(manager);
        binding.tilCoupon.getEditText().addTextChangedListener(this);
        intent = new Intent();
        subtotal = getIntent().getDoubleExtra("subtotal", 0.0);
        binding.done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        getCoupons();
    }

    private void getCoupons() {
        try {
            ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());
            apiService.getAlCoupons().enqueue(new Callback<CouponBaseModel>() {
                @Override
                public void onResponse(@NonNull Call<CouponBaseModel> call, @NonNull Response<CouponBaseModel> response) {
                    if (response.isSuccessful()) {
                        assert response.body() != null;
                        ArrayList<CouponDataModel> couponDataModelArrayList = response.body().getData();
                        assert couponDataModelArrayList != null;
                        if (couponDataModelArrayList.size() > 0) {
                            AllCouponsAdapter allCouponsAdapter = new AllCouponsAdapter(PromoCodeActivity.this, couponDataModelArrayList);
                            binding.recyclerViewCoupons.setAdapter(allCouponsAdapter);
                            allCouponsAdapter.notifyDataSetChanged();
                        } else
                            binding.recyclerViewCoupons.setVisibility(GONE);
                    } else {
                        Toast.makeText(PromoCodeActivity.this, "" + response, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<CouponBaseModel> call, @NonNull Throwable t) {
                    Log.e("TAG", call.toString());
                    Toast.makeText(PromoCodeActivity.this, "Failed to connect" + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            setResult(1, intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        setResult(1, intent);
        finish();
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (s.toString().isEmpty()) {
            binding.tilCoupon.setError(null);
            binding.tilCoupon.setHelperTextEnabled(false);
            binding.tilCoupon.setEndIconMode(TextInputLayout.END_ICON_NONE);
        } else {
            binding.tilCoupon.setError(null);
            binding.tilCoupon.setHelperTextEnabled(false);
            binding.tilCoupon.setEndIconMode(TextInputLayout.END_ICON_CUSTOM);
            binding.tilCoupon.setEndIconDrawable(R.drawable.ic_send);
            binding.tilCoupon.setEndIconOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    applyCoupon();
                }
            });
        }
    }

    public void applyCoupon() {
        CustomProgressDialog customProgressDialog = new CustomProgressDialog(
                PromoCodeActivity.this, "Applying coupon..."
        );
        customProgressDialog.show();
        ApiService service = ApiUtils.getHeaderAPIService(
                SPData.getAppPreferences().getUsertoken()
        );
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("coupon", binding.tilCoupon.getEditText().getText().toString());
        service.applyCoupon(jsonObject).enqueue(new Callback<CartDetailsInformation>() {
            @Override
            public void onResponse(@NotNull Call<CartDetailsInformation> call, @NotNull Response<CartDetailsInformation> response) {

                if (response.isSuccessful() && response.body() != null) {
                    CartDetailsInformation cartDetailsInformation = response.body();
                    binding.tilCoupon.setError(null);
                    binding.tilCoupon.setHelperTextEnabled(true);
                    intent.putExtra("discount", cartDetailsInformation.getData().getCoupon().getDiscountAmt());
                    intent.putExtra("code", "" + binding.tilCoupon.getEditText().getText().toString().toUpperCase());
                    intent.putExtra("total_amount", "" + cartDetailsInformation.getData().getTotal());
                    binding.tilCoupon.setHelperText("Coupon applied successfully.\nTotal discount: " + getString(R.string.rs) + CommonUtils.getTwoDigitsRoundOffValue(cartDetailsInformation.getData().getCoupon().getDiscountAmt()));
                    binding.tilCoupon.setHelperTextColor(ColorStateList.valueOf(
                            getResources().getColor(R.color.colorOrange)
                    ));
                    binding.tilCoupon.setEndIconMode(TextInputLayout.END_ICON_CUSTOM);
                    binding.tilCoupon.getEditText().setEnabled(false);
                    intent.putExtra("coupon_applied", true);
                    binding.tilCoupon.setEndIconDrawable(R.drawable.ic_close_black_24dp);
                    binding.tilCoupon.setEndIconOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            binding.tilCoupon.getEditText().setText("");
                            binding.tilCoupon.getEditText().setEnabled(true);
                            intent.putExtra("coupon_applied", false);
                            binding.tilCoupon.setError("Coupon remove successfully");
                        }
                    });
                    setResult(1, intent);
                    if (SPData.autoRedirectToCheckoutAfterApplyCoupon()) {
                        finish();
                    } else
                        binding.done.setVisibility(View.VISIBLE);
                } else {
                    binding.done.setVisibility(GONE);
                    intent.putExtra("coupon_applied", false);
                    binding.tilCoupon.setError("Coupon is either invalid or non applicable");
                    Toast.makeText(PromoCodeActivity.this, "Coupon is either invalid or non applicable", Toast.LENGTH_SHORT).show();
                }
                CommonUtils.hideProgressDialog(customProgressDialog);
            }

            @Override
            public void onFailure(Call<CartDetailsInformation> call, Throwable t) {
                intent.putExtra("coupon_applied", false);
                binding.done.setVisibility(GONE);
                CommonUtils.hideProgressDialog(customProgressDialog);
            }
        });
    }
}