package com.mindfulai.Activites;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;

import com.mindfulai.Adapter.PaymentAdapter;
import com.mindfulai.Models.payments.PaymentBase;
import com.mindfulai.Models.payments.PaymentRecords;
import com.mindfulai.NetworkRetrofit.ApiService;
import com.mindfulai.NetworkRetrofit.ApiUtils;
import com.mindfulai.Utils.SPData;
import com.mindfulai.ministore.R;
import com.mindfulai.ministore.databinding.ActivityPaymentHistoryBinding;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.view.View.VISIBLE;

public class PaymentHistoryActivity extends AppCompatActivity {


    ActivityPaymentHistoryBinding binding;
    ArrayList<PaymentRecords> paymentRecords;
    public int totalProducts = 0, currentPage = 1;
    private PaymentAdapter paymentAdapter;
    private boolean mHasReachedBottomOnce;

    @SuppressLint("InflateParams")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater layoutInflater = getLayoutInflater();
        layoutInflater.inflate(R.layout.activity_payment_history,null);
        binding = ActivityPaymentHistoryBinding.inflate(layoutInflater);
        setContentView(binding.getRoot());
        setTitle("Payments");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

}