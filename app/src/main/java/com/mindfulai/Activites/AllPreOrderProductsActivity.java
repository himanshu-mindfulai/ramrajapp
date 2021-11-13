package com.mindfulai.Activites;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.RecyclerView;
import com.mindfulai.Adapter.ProductsAdapter;
import com.mindfulai.Models.varientsByCategory.Datum;
import com.mindfulai.Models.varientsByCategory.PageVarientsByCategory;
import com.mindfulai.NetworkRetrofit.ApiService;
import com.mindfulai.NetworkRetrofit.ApiUtils;
import com.mindfulai.Utils.CommonUtils;
import com.mindfulai.ministore.R;
import com.mindfulai.ministore.databinding.ActivityAllPreOrderProductsBinding;
import org.jetbrains.annotations.NotNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;

public class AllPreOrderProductsActivity extends AppCompatActivity {

    ActivityAllPreOrderProductsBinding binding;
    ArrayList<Datum> preOrderProductsList =new ArrayList<>();
    public final static int PRE_ORDER_PRODUCTS_REQUEST_CODE = 103;
    private ProductsAdapter preOrderProductAdapter;
    private boolean mHasReachedBottomOnce;
    public int totalProducts = 0, currentPage = 1;

    @SuppressLint("InflateParams")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater layoutInflater = getLayoutInflater();
        layoutInflater.inflate(R.layout.activity_all_pre_order_products,null);
        binding = ActivityAllPreOrderProductsBinding.inflate(layoutInflater);
        setContentView(binding.getRoot());
        getSupportActionBar().setTitle(getString(R.string.pre_order_products));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        binding.rvCategoriesPreorderProducts.setLayoutManager(new CommonUtils(this).getProductGridLayoutManager());
        preOrderProductAdapter = new ProductsAdapter(this, preOrderProductsList, "grid", PRE_ORDER_PRODUCTS_REQUEST_CODE);
        binding.rvCategoriesPreorderProducts.setAdapter(preOrderProductAdapter);
        getAllPreOrderProducts();
        binding.rvCategoriesPreorderProducts.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!recyclerView.canScrollVertically(1) && !mHasReachedBottomOnce) {
                    mHasReachedBottomOnce = true;
                    if (preOrderProductsList.size() < totalProducts) {
                        Log.e("TAG", "onScrolled: ");
                        getAllPreOrderProducts();
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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void getAllPreOrderProducts() {
        preOrderProductsList.clear();
        ApiService apiService = ApiUtils.getAPIService();
        apiService.getPreOrderProducts(currentPage).enqueue(new Callback<PageVarientsByCategory>() {
            @Override
            public void onResponse(@NotNull Call<PageVarientsByCategory> call, @NotNull Response<PageVarientsByCategory> response) {
                if (response.isSuccessful() && response.body() != null ) {
                    currentPage++;
                    if (mHasReachedBottomOnce)
                        mHasReachedBottomOnce = false;
                    totalProducts = response.body().getData().getTotalCount();
                    preOrderProductsList.addAll(response.body().getData().getRecords());
                    preOrderProductAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(@NotNull Call<PageVarientsByCategory> call, @NotNull Throwable t) {

            }
        });
    }
}