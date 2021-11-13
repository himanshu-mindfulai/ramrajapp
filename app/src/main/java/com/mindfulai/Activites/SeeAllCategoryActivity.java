package com.mindfulai.Activites;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mindfulai.Adapter.CategoriesAdapter;
import com.mindfulai.Models.categoryData.CategoryInfo;
import com.mindfulai.Models.categoryData.Datum;
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

public class SeeAllCategoryActivity extends AppCompatActivity {
    private RecyclerView rv_categories;
    private ProgressBar progressBar;
    private List<Datum> categoryList;
    private CategoriesAdapter categoriesAdapter;
    private Intent intent;
    private String currentType = "grid";
    private MenuItem list, grid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_all_category);
        getSupportActionBar().setTitle("All Categories");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        intent = new Intent();
        progressBar = findViewById(R.id.progressAllCategories);
        categoryList = new ArrayList<>();
        rv_categories = findViewById(R.id.rv_categories);
        rv_categories.setLayoutManager(new CommonUtils(SeeAllCategoryActivity.this).getCategoriesGridLayoutManager());
        try {
            getCategories();
        } catch (Exception e) {
            Log.e("SeeAllCatAct", e.getMessage());
            getCategories();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.browse_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        list = menu.findItem(R.id.navigation_list_view);
        grid = menu.findItem(R.id.navigation_grid_view);
        menu.findItem(R.id.search).setVisible(false);
        menu.findItem(R.id.cart_notification).setVisible(false);
        menu.findItem(R.id.sort).setVisible(false);
        if (currentType.equals("grid")) {
            list.setVisible(true);
            grid.setVisible(false);
        } else {
            grid.setVisible(true);
            list.setVisible(false);
        }
        return true;
    }

    private void showListView() {

        categoriesAdapter = new CategoriesAdapter(SeeAllCategoryActivity.this, categoryList, "list");
        LinearLayoutManager manager = new LinearLayoutManager(SeeAllCategoryActivity.this);
        manager.setOrientation(RecyclerView.VERTICAL);
        rv_categories.setLayoutManager(manager);
        rv_categories.setAdapter(categoriesAdapter);
        currentType = "list";
    }

    private void showGridView() {
        categoriesAdapter = new CategoriesAdapter(SeeAllCategoryActivity.this, categoryList, "grid");
        rv_categories.setLayoutManager(new CommonUtils(SeeAllCategoryActivity.this).getCategoriesGridLayoutManager());
        rv_categories.setAdapter(categoriesAdapter);
        currentType = "grid";
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else if (item.getItemId() == R.id.navigation_list_view) {
            showListView();
            list.setVisible(false);
            grid.setVisible(true);

        } else if (item.getItemId() == R.id.navigation_grid_view) {
            showGridView();
            grid.setVisible(false);
            list.setVisible(true);
        }
        return super.onOptionsItemSelected(item);
    }


    private void getCategories() {
        ApiService apiService = ApiUtils.getHeaderAPIService();

        apiService.getAllProductCategory(SPData.getAppPreferences().getPincode()).enqueue(new Callback<CategoryInfo>() {
            @Override
            public void onResponse(@NonNull Call<CategoryInfo> call, @NonNull Response<CategoryInfo> response) {
                progressBar.setVisibility(View.GONE);
                rv_categories.setVisibility(View.VISIBLE);
                if (response.isSuccessful()) {
                    categoryList.clear();
                    CategoryInfo categoryInfo = response.body();
                    assert categoryInfo != null;
                    categoryList = categoryInfo.getData();
                    categoriesAdapter = new CategoriesAdapter(SeeAllCategoryActivity.this, categoryList, "grid");
                    rv_categories.setAdapter(categoriesAdapter);
                    categoriesAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(SeeAllCategoryActivity.this, "Something went wrong !!", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(@NonNull Call<CategoryInfo> call, @NonNull Throwable t) {
                Toast.makeText(SeeAllCategoryActivity.this, "Failed to connect", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}