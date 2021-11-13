package com.mindfulai.Adapter;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mindfulai.Models.DifferentVarients;
import com.mindfulai.Models.categoryData.Datum;
import com.mindfulai.Models.varientsByCategory.Attribute;
import com.mindfulai.Models.varientsByCategory.Images;
import com.mindfulai.Models.varientsByCategory.OptionsAttribute;
import com.mindfulai.Models.varientsByCategory.VarientsByCategory;
import com.mindfulai.Models.varientsByCategory.WholeSalePriceModel;
import com.mindfulai.NetworkRetrofit.ApiService;
import com.mindfulai.NetworkRetrofit.ApiUtils;
import com.mindfulai.Utils.CommonUtils;
import com.mindfulai.Utils.SPData;
import com.mindfulai.ministore.R;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class FeaturedCategoriesAdapter extends RecyclerView.Adapter<FeaturedCategoriesAdapter.MyViewHolder> {


    private static final String TAG = "CategoriesAdapter";
    private final Context context;
    private final List<Datum> categoryList;
    public final static int FEATURED_PRODUCTS_REQUEST_CODE = 102;

    public FeaturedCategoriesAdapter(Context context, List<Datum> categoryList) {
        this.context = context;
        this.categoryList = categoryList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.featured_category_item_layout, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        try {
            holder.cat_name.setText(CommonUtils.capitalizeWord(categoryList.get(position).getName()));
            final Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(categoryList.size()>position)
                    getProductsFromCategory(categoryList.get(position).getId(), holder);
                }
            }, 2000);
        } catch (Exception e) {
            Log.e(TAG, "onBindViewHolder: " + e);
        }

    }

    private void getProductsFromCategory(String catId, MyViewHolder itemView) {
        ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());
        apiService.getAllProductsVarients(catId, SPData.getAppPreferences().getPincode()).enqueue(new Callback<VarientsByCategory>() {
            @Override
            public void onResponse(@NonNull Call<VarientsByCategory> call, @NonNull Response<VarientsByCategory> response) {
                showRelatedProductsFromResponse(response, itemView);
            }

            @Override
            public void onFailure(@NonNull Call<VarientsByCategory> call, @NonNull Throwable t) {
                Log.e("TAG", "onFailure: " + t.getMessage());
            }
        });
    }

    private void showRelatedProductsFromResponse(Response<VarientsByCategory> response, MyViewHolder itemView) {
        if (response.isSuccessful() && response.body() != null && response.body().getData().size() > 0) {
            VarientsByCategory productVarients = response.body();
            List<com.mindfulai.Models.varientsByCategory.Datum> varientList = productVarients.getData();
            itemView.recyclerViewProducts.setVisibility(VISIBLE);
            ProductsAdapter productAdapter = new ProductsAdapter(context, varientList, "list2", FEATURED_PRODUCTS_REQUEST_CODE);
            itemView.recyclerViewProducts.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
            itemView.recyclerViewProducts.setAdapter(productAdapter);
            productAdapter.notifyDataSetChanged();
            itemView.progressBar.setVisibility(GONE);
        } else {
            hideItem(itemView);
            Log.e("TAG", "onResponse: " + response);
        }
    }

    private void hideItem(MyViewHolder itemView) {
        itemView.progressBar.setVisibility(GONE);
        itemView.recyclerViewProducts.setVisibility(GONE);
        ViewGroup.LayoutParams params = itemView.itemView.getLayoutParams();
        params.height = 0;
        itemView.itemView.setLayoutParams(params);
    }

    @Override
    public int getItemCount() {
        try {
            return categoryList.size();
        } catch (Exception e) {
            return 0;
        }
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView cat_name;
        RecyclerView recyclerViewProducts;
        ProgressBar progressBar;
        LinearLayout linearLayout;

        MyViewHolder(View view) {
            super(view);
            cat_name = view.findViewById(R.id.cat_name);
            recyclerViewProducts = view.findViewById(R.id.recyclerView_products);
            progressBar = view.findViewById(R.id.progressBar);
            linearLayout = view.findViewById(R.id.linearLayout);
        }
    }

}
