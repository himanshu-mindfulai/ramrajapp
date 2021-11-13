package com.mindfulai.Activites;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.fragment.app.FragmentTransaction;
import com.mindfulai.Adapter.ProductsAdapter;
import com.mindfulai.Models.varientsByCategory.Attribute__;
import com.mindfulai.Models.varientsByCategory.Datum;
import com.mindfulai.Models.varientsByCategory.DatumForTopDiscount;
import com.mindfulai.Models.varientsByCategory.Product;
import com.mindfulai.Models.varientsByCategory.TopDiscountProductModel;
import com.mindfulai.Models.varientsByCategory.Varient;
import com.mindfulai.NetworkRetrofit.ApiService;
import com.mindfulai.NetworkRetrofit.ApiUtils;
import com.mindfulai.Utils.CommonUtils;
import com.mindfulai.Utils.SPData;
import com.mindfulai.ministore.R;
import com.mindfulai.ministore.databinding.ActivityTopProductsBinding;

import com.mindfulai.ui.TopDiscountProductFragment;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TopProductsActivity extends AppCompatActivity {

    ActivityTopProductsBinding binding;
    TopDiscountProductFragment fragment;

    @SuppressLint("InflateParams")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater layoutInflater = getLayoutInflater();
        layoutInflater.inflate(R.layout.activity_top_products, null);
        binding = ActivityTopProductsBinding.inflate(layoutInflater);
        setContentView(binding.getRoot());
        fragment = new TopDiscountProductFragment();
        getSupportActionBar().setTitle("Top Discount products");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.framelayout_container, fragment).commit();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

}