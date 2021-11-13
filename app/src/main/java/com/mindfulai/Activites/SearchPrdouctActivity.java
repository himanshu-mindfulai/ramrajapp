package com.mindfulai.Activites;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.mindfulai.Adapter.ProductsAdapter;
import com.mindfulai.Models.DifferentVarients;
import com.mindfulai.Models.varientsByCategory.Datum;
import com.mindfulai.Models.varientsByCategory.OptionsAttribute;
import com.mindfulai.Models.varientsByCategory.VarientsByCategory;
import com.mindfulai.NetworkRetrofit.ApiService;
import com.mindfulai.NetworkRetrofit.ApiUtils;
import com.mindfulai.Utils.CommonUtils;
import com.mindfulai.Utils.SPData;
import com.mindfulai.ministore.R;
import com.mindfulai.ui.SearchFragment;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.nikartm.support.ImageBadgeView;

public class SearchPrdouctActivity extends AppCompatActivity {

    SearchFragment searchFragment;

    private ImageBadgeView cartBadge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_prdouct);
        Toolbar toolbar = findViewById(R.id.all_product_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_white);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setTitle("Search Products");
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorWhite));
        searchFragment = new SearchFragment();
        setFragment(searchFragment);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        searchFragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.cart_notification);
        item.setVisible(SPData.showcartIconAddToCart() && SPData.showProductsAndCart());
        cartBadge = item.getActionView().findViewById(R.id.notification_badge);

        int total = SPData.getAppPreferences().getTotalCartCount();
        cartBadge.setBadgeValue(total);
        cartBadge.setOnClickListener(v -> startActivityForResult(new Intent(SearchPrdouctActivity.this, CartPageActivity.class), 10));
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (cartBadge != null) {
            int total = SPData.getAppPreferences().getTotalCartCount();
            cartBadge.setBadgeValue(total);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        return true;
    }

    public void addBadge(String count) {
        cartBadge.setBadgeValue(Integer.parseInt(count));
    }

    public void removeBadge() {
        cartBadge.setBadgeValue(0);
    }

    private void setFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.framelayout_container, fragment).commit();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}