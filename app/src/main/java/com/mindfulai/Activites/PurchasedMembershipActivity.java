package com.mindfulai.Activites;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.mindfulai.Adapter.PurchasedMembershipAdapter;
import com.mindfulai.Models.membership.GetMembershipBase;
import com.mindfulai.Models.membership.MembershipBaseModel;
import com.mindfulai.NetworkRetrofit.ApiService;
import com.mindfulai.NetworkRetrofit.ApiUtils;
import com.mindfulai.Utils.SPData;
import com.mindfulai.ministore.R;
import com.mindfulai.ministore.databinding.ActivityPurchasedMembershipBinding;
import org.jetbrains.annotations.NotNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.Objects;

public class PurchasedMembershipActivity extends AppCompatActivity {

    ActivityPurchasedMembershipBinding binding;
    @SuppressLint("InflateParams")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater layoutInflater = getLayoutInflater();
        layoutInflater.inflate(R.layout.activity_purchased_membership,null);
        binding = ActivityPurchasedMembershipBinding.inflate(layoutInflater);
        setContentView(binding.getRoot());

        Objects.requireNonNull(getSupportActionBar()).setTitle("Purchased Membership");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.recyclerview.setLayoutManager(new LinearLayoutManager(this));
        getPurchasedMemberShip();

    }

    private void getPurchasedMemberShip() {
        ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());
        apiService.getPurchasedMembership().enqueue(new Callback<GetMembershipBase>() {
            @Override
            public void onResponse(@NotNull Call<GetMembershipBase> call, @NotNull Response<GetMembershipBase> response) {
                if(response.isSuccessful()&&response.body()!=null){
                    PurchasedMembershipAdapter adapter = new PurchasedMembershipAdapter(PurchasedMembershipActivity.this,response.body().getData());
                    binding.recyclerview.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(@NotNull Call<GetMembershipBase> call, @NotNull Throwable t) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}