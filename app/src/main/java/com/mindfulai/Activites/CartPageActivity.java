package com.mindfulai.Activites;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.MenuItem;

import com.mindfulai.ministore.R;
import com.mindfulai.ui.CartPageFragment;

import java.util.Objects;

public class CartPageActivity extends AppCompatActivity {

   CartPageFragment cartPageFragment;
   public boolean moveToCheckout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart_page);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Cart");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        moveToCheckout = getIntent().getBooleanExtra("moveToCheckout",false);
        cartPageFragment = new CartPageFragment(this);
        transaction.replace(R.id.framelayout_container, cartPageFragment).commit();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}