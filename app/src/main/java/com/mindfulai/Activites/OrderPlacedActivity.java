package com.mindfulai.Activites;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.mindfulai.Utils.CommonUtils;
import com.mindfulai.Utils.SPData;
import com.mindfulai.ministore.R;
import com.mindfulai.ui.RateUsFragment;
import com.mindfulai.ui.ShareAppBottomSheet;

public class OrderPlacedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_placed);
        getSupportActionBar().hide();
        TextView orderNumber = findViewById(R.id.order_number);
        TextView orderAmount = findViewById(R.id.order_amount);
        TextView orderPaymentMethod = findViewById(R.id.payment_method);
        String order_number = getIntent().getStringExtra("order_id");
        String order_amount = getIntent().getStringExtra("payment_amount");
        String payment_method = getIntent().getStringExtra("payment_method") + "";
        orderNumber.setText(order_number);
        orderAmount.setText(getString(R.string.rs) + CommonUtils.getTwoDigitsRoundOffValue(Double.parseDouble(order_amount)));
        if (!payment_method.equals("null"))
            orderPaymentMethod.setText(payment_method);
        else
            orderPaymentMethod.setText("");
        TextView done = findViewById(R.id.done);
        if(SPData.askForRatingFromUser()){
        RateUsFragment rateUsFragment = new RateUsFragment();
        rateUsFragment.show(getSupportFragmentManager(), "ShareBS");
        }
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(OrderPlacedActivity.this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent(OrderPlacedActivity.this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }
}