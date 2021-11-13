package com.mindfulai.Activites;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.mindfulai.Models.AppInfoModel;
import com.mindfulai.Utils.CustomTag;
import com.mindfulai.ministore.R;

import org.json.JSONObject;

import java.util.Objects;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.mindfulai.NetworkRetrofit.ApiService.*;

public class PrivacyPolicy extends AppCompatActivity {
    private static final String TAG = "PrivacyPolicy";
    private Response response;
    private TextView textView;
    private ProgressBar progressBar;
    private AppInfoModel appInfoModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_privacy_policy);
            textView = findViewById(R.id.privacy_policy);
            String type = getIntent().getStringExtra("type");
            if (type != null) {
                if (type.equals(RETURN_POLICY)) {
                    Objects.requireNonNull(getSupportActionBar()).setTitle("Return Policy");
                }
                if (type.equals(TNC)) {
                    Objects.requireNonNull(getSupportActionBar()).setTitle("Terms and Condition");
                }
                if (type.equals(ABOUTUS)) {
                    Objects.requireNonNull(getSupportActionBar()).setTitle("About Us");
                }
                if (type.equals(PRIVACY)) {
                    Objects.requireNonNull(getSupportActionBar()).setTitle("Privacy Policy");
                }
                if(type.equals(INSTRUCTION)){
                    Objects.requireNonNull(getSupportActionBar()).setTitle("Instructions");
                }
            }
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            progressBar = findViewById(R.id.progressBarPrivacy);
            new Privacy().execute(getIntent().getStringExtra("type"));
        } catch (Exception e) {
            Log.e(TAG, "onCreate: " + e);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("StaticFieldLeak")
    class Privacy extends AsyncTask<String, Void, Response> {

        @Override
        protected okhttp3.Response doInBackground(String... strings) {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(strings[0])
                    .get()
                    .addHeader("Content-Type", "application/json;charset=utf-8")
                    .build();
            try {
                response = client.newCall(request).execute();
                Log.e("TAG", "doInBackground: "+response);
                String jsonData = response.body().string();
                JSONObject jsonObject = new JSONObject(jsonData);
                Log.e("TAG", "doInBackground: "+jsonObject);
                Gson gson = new Gson();
                appInfoModel = gson.fromJson(jsonObject.getJSONObject("data").toString(), AppInfoModel.class);
            } catch (Exception e) {
                Log.e("TAG", "doInBackground: " + e);
            }
            return response;
        }

        @Override
        protected void onPostExecute(okhttp3.Response response) {
            super.onPostExecute(response);
            try {
                progressBar.setVisibility(View.GONE);
                if (appInfoModel != null && appInfoModel.getParagraph() != null){
                    textView.setText(Html.fromHtml("<p align=justify>"+appInfoModel.getParagraph()+"</p>", null, new CustomTag()));
                }
                else {
                    textView.setText("N/A");
                }
            } catch (Exception e) {
                progressBar.setVisibility(View.GONE);
                if (appInfoModel != null && appInfoModel.getParagraph() != null)
                    textView.setText(appInfoModel.getParagraph());
            }
        }
    }
}
