package com.mindfulai.kwikapi;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.mindfulai.ministore.R;
import com.mindfulai.ministore.databinding.ActivityAllKwikServicesBinding;

public class AllKwikServicesActivity extends AppCompatActivity {
    ActivityAllKwikServicesBinding binding;

    @SuppressLint("InflateParams")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater layoutInflater = getLayoutInflater();
        layoutInflater.inflate(R.layout.activity_all_kwik_services, null);
        binding = ActivityAllKwikServicesBinding.inflate(layoutInflater);
        setContentView(binding.getRoot());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("All Services");
        binding.mobileRecharge.setOnClickListener(v -> startActivity(new Intent(AllKwikServicesActivity.this, MobileRechargeActivity.class).putExtra("type", "Prepaid")));
        binding.dthRecharge.setOnClickListener(v -> startActivity(new Intent(AllKwikServicesActivity.this, MobileRechargeActivity.class).putExtra("type", "DTH")));

        binding.postpaidRecharge.setOnClickListener(v -> startActivity(new Intent(AllKwikServicesActivity.this, PostpaidRechargeActivity.class).putExtra("type","Postpaid")));
        binding.fastagRecharge.setOnClickListener(v->startActivity(new Intent(AllKwikServicesActivity.this, PostpaidRechargeActivity.class).putExtra("type","Fastag")));

        binding.gasBillRecharge.setOnClickListener(v->startActivity(new Intent(AllKwikServicesActivity.this,GasBillActivity.class).putExtra("type","Gas")));
        binding.electricityRecchrge.setOnClickListener(v->startActivity(new Intent(AllKwikServicesActivity.this,GasBillActivity.class).putExtra("type","Elc")));
        binding.broadbandRecchrge.setOnClickListener(v->startActivity(new Intent(AllKwikServicesActivity.this,GasBillActivity.class).putExtra("type","Broadband")));
        binding.landlineRecharge.setOnClickListener(v->startActivity(new Intent(AllKwikServicesActivity.this,GasBillActivity.class).putExtra("type","Landline")));
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}