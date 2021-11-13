package com.mindfulai.Activites;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.mindfulai.Models.subscription.DaysDataModel;
import com.mindfulai.NetworkRetrofit.ApiService;
import com.mindfulai.Utils.CommonUtils;
import com.mindfulai.Utils.SPData;
import com.mindfulai.ministore.R;

import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EditSubscriptionActivity extends AppCompatActivity {

    public ImageView image;
    TextView product_name;
    DaysDataModel daysDataModel;
    private String message;
    private boolean errors = true;
    private ProgressDialog progressDialog;
    private String addressId;
    private String varientId;
    private String subscription_id;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_subscription);
        try {
            getSupportActionBar().setTitle(getIntent().getStringExtra("title"));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            String strName = getIntent().getStringExtra("name");
            String strAttributes = getIntent().getStringExtra("attributes");
            String strImage = getIntent().getStringExtra("image");
            varientId = getIntent().getStringExtra("varient_id");
            addressId = getIntent().getStringExtra("address_id");
            subscription_id = getIntent().getStringExtra("id");
            double doublePrice = getIntent().getDoubleExtra("price", 0.0);
            double doubleSellingPrice = getIntent().getDoubleExtra("sellingPrice", 0.0);
            progressDialog = new ProgressDialog(this);
            daysDataModel = (DaysDataModel) getIntent().getParcelableExtra("daysQty");

            if (daysDataModel == null) {
                daysDataModel = new DaysDataModel();
            }

            product_name = findViewById(R.id.product_name);
            image = findViewById(R.id.image);

            TextView product_mrp = findViewById(R.id.product_mrp);
            TextView product_selling_price = findViewById(R.id.product_price);
            TextView attributes = findViewById(R.id.attributes);

            TextView textViewMonQty = findViewById(R.id.no_of_quantity_mon);
            Button textViewMonInc = findViewById(R.id.increase_mon);
            Button textViewMonDec = findViewById(R.id.decrease_mon);

            TextView textViewTueQty = findViewById(R.id.no_of_quantity_tue);
            Button textViewTueInc = findViewById(R.id.increase_tue);
            Button textViewTueDec = findViewById(R.id.decrease_tue);

            TextView textViewWedQty = findViewById(R.id.no_of_quantity_wed);
            Button textViewWedInc = findViewById(R.id.increase_wed);
            Button textViewWedDec = findViewById(R.id.decrease_wed);

            TextView textViewThuQty = findViewById(R.id.no_of_quantity_thu);
            Button textViewThuInc = findViewById(R.id.increase_thu);
            Button textViewThuDec = findViewById(R.id.decrease_thu);

            TextView textViewFriQty = findViewById(R.id.no_of_quantity_fri);
            Button textViewFriInc = findViewById(R.id.increase_fri);
            Button textViewFriDec = findViewById(R.id.decrease_fri);

            TextView textViewSatQty = findViewById(R.id.no_of_quantity_sat);
            Button textViewSatInc = findViewById(R.id.increase_sat);
            Button textViewSatDec = findViewById(R.id.decrease_sat);

            TextView textViewSunQty = findViewById(R.id.no_of_quantity_sun);
            Button textViewSunInc = findViewById(R.id.increase_sun);
            Button textViewSunDec = findViewById(R.id.decrease_sun);

            textViewMonQty.setText("" + daysDataModel.getMon());
            textViewTueQty.setText("" + daysDataModel.getTue());
            textViewWedQty.setText("" + daysDataModel.getWed());
            textViewThuQty.setText("" + daysDataModel.getThu());
            textViewFriQty.setText("" + daysDataModel.getFri());
            textViewSatQty.setText("" + daysDataModel.getSat());
            textViewSunQty.setText("" + daysDataModel.getSun());

            Button save = findViewById(R.id.save);
            save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    progressDialog.setMessage("Please wait...");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();
                    if (subscription_id != null)
                        saveSubscription(ApiService.SAVE_SUBSCRIPTION + subscription_id);
                    else
                        saveSubscription(ApiService.CREATE_SUBSCRIPTION);
                }
            });
            textViewMonInc.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int monQty = Integer.parseInt(textViewMonQty.getText().toString()) + 1;
                    textViewMonQty.setText("" + monQty);
                    daysDataModel.setMon(monQty);
                }
            });
            textViewMonDec.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int monQty = Integer.parseInt(textViewMonQty.getText().toString()) - 1;
                    if (monQty >= 0) {
                        textViewMonQty.setText("" + monQty);
                        daysDataModel.setMon(monQty);
                    }
                }
            });

            textViewTueInc.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int tueQty = Integer.parseInt(textViewTueQty.getText().toString()) + 1;
                    textViewTueQty.setText("" + tueQty);
                    daysDataModel.setTue(tueQty);
                }
            });
            textViewTueDec.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int tueQty = Integer.parseInt(textViewTueQty.getText().toString()) - 1;
                    if (tueQty >= 0) {
                        textViewTueQty.setText("" + tueQty);
                        daysDataModel.setTue(tueQty);
                    }
                }
            });

            textViewWedInc.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int wedQty = Integer.parseInt(textViewWedQty.getText().toString()) + 1;
                    textViewWedQty.setText("" + wedQty);
                    daysDataModel.setWed(wedQty);
                }
            });
            textViewWedDec.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int wedQty = Integer.parseInt(textViewWedQty.getText().toString()) - 1;
                    if (wedQty >= 0) {
                        textViewWedQty.setText("" + wedQty);
                        daysDataModel.setWed(wedQty);
                    }
                }
            });

            textViewThuInc.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int thuQty = Integer.parseInt(textViewThuQty.getText().toString()) + 1;
                    textViewThuQty.setText("" + thuQty);
                    daysDataModel.setThu(thuQty);
                }
            });
            textViewThuDec.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int thuQty = Integer.parseInt(textViewThuQty.getText().toString()) - 1;
                    if (thuQty >= 0) {
                        textViewThuQty.setText("" + thuQty);
                        daysDataModel.setThu(thuQty);
                    }
                }
            });

            textViewFriInc.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int friQty = Integer.parseInt(textViewFriQty.getText().toString()) + 1;
                    textViewFriQty.setText("" + friQty);
                    daysDataModel.setFri(friQty);
                }
            });
            textViewFriDec.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int friQty = Integer.parseInt(textViewFriQty.getText().toString()) - 1;
                    if (friQty >= 0) {
                        textViewFriQty.setText("" + friQty);
                        daysDataModel.setFri(friQty);
                    }
                }
            });

            textViewSatInc.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int satQty = Integer.parseInt(textViewSatQty.getText().toString()) + 1;
                    textViewSatQty.setText("" + satQty);
                    daysDataModel.setSat(satQty);
                }
            });
            textViewSatDec.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int satQty = Integer.parseInt(textViewSatQty.getText().toString()) - 1;
                    if (satQty >= 0) {
                        textViewSatQty.setText("" + satQty);
                        daysDataModel.setSat(satQty);
                    }
                }
            });

            textViewSunInc.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int sunQty = Integer.parseInt(textViewSunQty.getText().toString()) + 1;
                    textViewSunQty.setText("" + sunQty);
                    daysDataModel.setSun(sunQty);
                }
            });
            textViewSunDec.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int sunQty = Integer.parseInt(textViewSunQty.getText().toString()) - 1;
                    if (sunQty >= 0) {
                        textViewSunQty.setText("" + sunQty);
                        daysDataModel.setSun(sunQty);
                    }
                }
            });


            if (strImage != null && !strImage.isEmpty()) {
                Glide.with(this).load(SPData.getBucketUrl() + strImage).into(image);
            }
            product_name.setText(strName);
            if (doublePrice != 0.0)
                product_mrp.setText(getString(R.string.rs) + CommonUtils.getTwoDigitsRoundOffValue(doublePrice));
            else
                product_mrp.setVisibility(View.GONE);
            if (doubleSellingPrice != 0.0)
                product_selling_price.setText(getString(R.string.rs) + CommonUtils.getTwoDigitsRoundOffValue(doubleSellingPrice));
            else
                product_selling_price.setVisibility(View.GONE);
            attributes.setText(strAttributes);
        } catch (Exception e) {
            Toast.makeText(this, "" + e, Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveSubscription(String api) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(new Runnable() {
            @Override
            public void run() {

                //Background work here
                try {
                    OkHttpClient client = new OkHttpClient();
                    MediaType mediaType = MediaType.parse("application/json;charset=utf-8");

                    JSONObject params = new JSONObject();
                    params.put("sun", daysDataModel.getSun());
                    params.put("mon", daysDataModel.getMon());
                    params.put("tue", daysDataModel.getTue());
                    params.put("wed", daysDataModel.getWed());
                    params.put("thu", daysDataModel.getThu());
                    params.put("fri", daysDataModel.getFri());
                    params.put("sat", daysDataModel.getSat());
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("days", params);
                    if (subscription_id == null) {
                        jsonObject.put("address", addressId);
                        jsonObject.put("product", varientId);
                    }
                    RequestBody body = RequestBody.create(mediaType, jsonObject.toString());
                    Request.Builder builder = new Request.Builder()
                            .url(api);
                    if (subscription_id == null)
                        builder.post(body);
                    else
                        builder.put(body);

                    builder.addHeader("Content-Type", "application/json;charset=utf-8");
                    builder.addHeader("token", SPData.getAppPreferences().getUsertoken());
                    Request request = builder.build();
                    Response response = client.newCall(request).execute();
                    String json = response.body().string();
                    Log.e("TAG", "doInBackground: " + json);
                    JSONObject jsonObject1 = new JSONObject(json);
                    errors = jsonObject1.getBoolean("errors");
                    message = jsonObject1.getString("message");
                    int status = jsonObject1.getInt("status");
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.cancel();
                            Toast.makeText(EditSubscriptionActivity.this, "" + message, Toast.LENGTH_SHORT).show();
                            if (status==200) {
                                if(getCallingActivity()!=null){
                                Intent intent = getIntent();
                                intent.putExtra("daysQty", daysDataModel);
                                setResult(RESULT_OK, intent);
                                finish();}
                               else {
                                    Intent i = new Intent(EditSubscriptionActivity.this, MainActivity.class);
                                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(i);
                                }
                            }
                        }
                    });
                } catch (Exception e) {
                    Log.e("TAG", "run: " + e);
                }
            }
        });
    }
}