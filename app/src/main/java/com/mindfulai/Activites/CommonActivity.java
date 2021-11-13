package com.mindfulai.Activites;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.mindfulai.ministore.R;
import com.mindfulai.ui.NotificationFragment;
import com.mindfulai.ui.WishListFragment;

public class CommonActivity extends AppCompatActivity  {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String show = getIntent().getStringExtra("show");
         if (show.equals("wish")) {
            setContentView(R.layout.activity_common);
            getSupportActionBar().setTitle("WishList");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            FragmentManager fm = getSupportFragmentManager();
            WishListFragment fragment = new WishListFragment();
            fm.beginTransaction().replace(R.id.nav_host_fragment, fragment).commit();
        } else {
            setContentView(R.layout.activity_common);
            getSupportActionBar().setTitle("Notification");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            FragmentManager fm = getSupportFragmentManager();
            NotificationFragment fragment = new NotificationFragment();
            fm.beginTransaction().replace(R.id.nav_host_fragment, fragment).commit();
        }
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void finishAllActivity(View view) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("EXIT", true);
        startActivity(intent);
    }
}