package com.mindfulai.Activites;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.mindfulai.ministore.R;
import com.mindfulai.ui.OrderHistoryFragment;

public class OrderHistoryActivity extends AppCompatActivity {

    private OrderHistoryFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_order_history);
            getSupportActionBar().setTitle("Order History");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            FragmentManager fm = getSupportFragmentManager();
            fragment = new OrderHistoryFragment();
            fm.beginTransaction().replace(R.id.nav_host_fragment, fragment).commit();
        } catch (Exception e) {
            Log.e("TAG", "onCreate: " + e);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

}