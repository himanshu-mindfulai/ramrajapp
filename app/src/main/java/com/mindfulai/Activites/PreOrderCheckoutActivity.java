package com.mindfulai.Activites;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.bumptech.glide.Glide;
import com.google.gson.JsonObject;
import com.mindfulai.Models.orderDetailInfo.OrderDetailInfo;
import com.mindfulai.Models.varientsByCategory.Attribute;
import com.mindfulai.Models.varientsByCategory.Varient;
import com.mindfulai.NetworkRetrofit.ApiService;
import com.mindfulai.NetworkRetrofit.ApiUtils;
import com.mindfulai.Utils.CommonUtils;
import com.mindfulai.Utils.CustomProgressDialog;
import com.mindfulai.Utils.SPData;
import com.mindfulai.ministore.R;
import com.mindfulai.ministore.databinding.ActivityPreOrderCheckoutBinding;
import com.paytm.pgsdk.PaytmOrder;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;
import com.paytm.pgsdk.TransactionManager;
import com.razorpay.Checkout;
import com.razorpay.PaymentData;
import com.razorpay.PaymentResultWithDataListener;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.Set;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class PreOrderCheckoutActivity extends AppCompatActivity implements PaymentResultWithDataListener {

    ActivityPreOrderCheckoutBinding binding;
    String image;
    String productName;
    private Checkout checkout;
    private final int PAYTM_REQUEST_CODE = 102;
    private String orderId;
    private String variantId;

    @SuppressLint("InflateParams")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater layoutInflater = getLayoutInflater();
        layoutInflater.inflate(R.layout.activity_pre_order_checkout, null);
        binding = ActivityPreOrderCheckoutBinding.inflate(layoutInflater);
        setContentView(binding.getRoot());
        getSupportActionBar().setTitle("Pre-Order Checkout");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (SPData.useCodInPreorderCheckout()) {
            binding.radioBtnCod.setVisibility(View.VISIBLE);
        } else {
            binding.radioBtnCod.setVisibility(View.GONE);
        }

        if (SPData.showAvailableFromToDate()) {
            binding.productAvailableLayout.setVisibility(View.VISIBLE);
        } else {
            binding.productAvailableLayout.setVisibility(GONE);
        }
        if (SPData.useTimeSlotInPreorderCheckout()) {
            binding.timeSlotLayout.setVisibility(View.VISIBLE);
        } else {
            binding.timeSlotLayout.setVisibility(View.GONE);
        }
        binding.radioBtnOnline.setChecked(true);

        setProduct();

        checkAddress();

        binding.change.setOnClickListener(view -> startActivity(new Intent(PreOrderCheckoutActivity.this, AddressSelectorActivity.class)));

        binding.order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!SPData.getAppPreferences().getUsertoken().isEmpty())
                    checkPaymentMethod();
                else {
                    Toast.makeText(PreOrderCheckoutActivity.this, "Please login first", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(PreOrderCheckoutActivity.this, LoginActivity.class));
                }
            }
        });
        binding.timeSlot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickDate();
            }
        });
    }

    private void checkAddress() {
        if (!SPData.getAppPreferences().getAddressId().isEmpty()) {
            setUserAddress();
        } else {
            noAddressSelected();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkAddress();
    }

    @SuppressLint("SetTextI18n")
    private void setProduct() {
        productName = getIntent().getStringExtra("name");
        image = getIntent().getStringExtra("image");
        variantId = getIntent().getStringExtra("id");
        binding.productName.setText(productName);
        double sp = getIntent().getFloatExtra("sp", 0);
        double mrp = getIntent().getFloatExtra("mrp", 0);
        if (CommonUtils.stringIsNotNullAndEmpty(image))
            Glide.with(PreOrderCheckoutActivity.this).load(SPData.getBucketUrl() + image).into(binding.image);
        if (sp > 0 && sp != mrp) {
            binding.productMrp.setText(getString(R.string.rs) + CommonUtils.getTwoDigitsRoundOffValue(mrp));
            binding.productPrice.setText(getString(R.string.rs) + CommonUtils.getTwoDigitsRoundOffValue(sp));
        } else {
            binding.productMrp.setVisibility(View.GONE);
            binding.productPrice.setText(getString(R.string.rs) + CommonUtils.getTwoDigitsRoundOffValue(mrp));
        }
        String attributes = getIntent().getStringExtra("attribute");
        binding.attributes.setText(CommonUtils.removeLastCharacter(attributes.toString()));
        String fromIso = getIntent().getStringExtra("from");
        String toIso = getIntent().getStringExtra("to");
        try {
            SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, dd MMM");
            String from = "", to = "";
            if (fromIso != null) {
                Date date = iso.parse(fromIso);
                Date date1 = CommonUtils.gmttoLocalDate(Objects.requireNonNull(date));
                from = simpleDateFormat.format(date1);
            }
            if (toIso != null) {
                Date date = iso.parse(toIso);
                Date date1 = CommonUtils.gmttoLocalDate(Objects.requireNonNull(date));
                to = simpleDateFormat.format(date1);
            }
            if (from.isEmpty() || to.isEmpty()) {
                binding.productAvailable.setVisibility(GONE);
            } else {
                binding.productAvailable.setVisibility(VISIBLE);
                binding.productAvailable.setText(from + " to " + to);
            }
        } catch (Exception e) {
            Log.e("TAG", "handleProduct: " + e);
            binding.productAvailable.setVisibility(GONE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("SetTextI18n")
    private void setUserAddress() {

        binding.shippingAddressLayout.setVisibility(View.VISIBLE);
        binding.addressType.setText(SPData.getAppPreferences().getUserShippingName() + ", " + SPData.getAppPreferences().getUserShippingMobile());
        binding.completeAddress.setText(SPData.getAppPreferences().getUserShippingAddress());
    }

    private void noAddressSelected() {
        binding.shippingAddressLayout.setVisibility(View.GONE);
    }

    public void checkPaymentMethod() {

        if (SPData.useTimeSlotInPreorderCheckout() && binding.timeSlot.getText().toString().equals(getString(R.string.pickTime))) {
            Toast.makeText(PreOrderCheckoutActivity.this, "Please select a time slot", Toast.LENGTH_SHORT).show();
        } else if (binding.radioBtnOnline.isChecked()) {
            takePayment();
        } else if (binding.radioBtnCod.isChecked()) {
            placeOrderCOD();
        } else {
            Toast.makeText(PreOrderCheckoutActivity.this, "Please select a payment method", Toast.LENGTH_SHORT).show();
        }
    }


    private void takePayment() {
        checkout = new Checkout();
        checkout.setImage(R.mipmap.ic_launcher);
        checkout.setKeyID(SPData.getRazorPayKey());
        JSONObject options = new JSONObject();
        try {
            options.put("currency", "INR");
            options.put("receipt", "");
            options.put("payment_capture", true);
            generateOrderId(options);
        } catch (Exception e) {
            Log.e("TAG", "takePayment: " + e);
        }
    }

    private void placeOrderCOD() {
        try {
            final CustomProgressDialog customProgressDialog = CommonUtils.showProgressDialog(PreOrderCheckoutActivity.this,
                    "Please wait...");
            ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());
            JsonObject jsonObject1 = new JsonObject();

            jsonObject1.addProperty("address", SPData.getAppPreferences().getAddressId());
            jsonObject1.addProperty("deliverySlot", binding.timeSlot.getText().toString());
            jsonObject1.addProperty("varient", variantId);
            jsonObject1.addProperty("paymentMethod", "COD");

            apiService.PlacePreOrder(jsonObject1).enqueue(new Callback<OrderDetailInfo>() {
                @Override
                public void onResponse(@NonNull Call<OrderDetailInfo> call, @NonNull retrofit2.Response<OrderDetailInfo> response) {
                    CommonUtils.hideProgressDialog(customProgressDialog);
                    try {
                        if (response.isSuccessful()) {
                            OrderDetailInfo placeOrder = response.body();
                            SPData.getAppPreferences().setTotalCartCount(0);
                            String order_id = placeOrder.getData().getOrder().getInvoiceNumber();
                            String payment_method = placeOrder.getData().getOrder().getPaymentMethod();
                            String payment_amount = "" + placeOrder.getData().getOrder().getAmount();
                            Intent intent = new Intent(PreOrderCheckoutActivity.this, OrderPlacedActivity.class);
                            intent.putExtra("order_id", "" + order_id);
                            intent.putExtra("payment_method", "" + payment_method);
                            intent.putExtra("payment_amount", "" + payment_amount);
                            startActivity(intent);
                        } else {
                            Log.e("TAG", "onResponse: " + response.errorBody().string());
                            Toast.makeText(PreOrderCheckoutActivity.this, "Something went wrong !! " + response.message(), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("TAG", "onResponse: " + e);
                        Toast.makeText(PreOrderCheckoutActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<OrderDetailInfo> call, @NonNull Throwable t) {
                    CommonUtils.hideProgressDialog(customProgressDialog);
                    Toast.makeText(PreOrderCheckoutActivity.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
                    t.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void generateOrderId(final JSONObject options) {
        try {
            final CustomProgressDialog customProgressDialog = CommonUtils.showProgressDialog(PreOrderCheckoutActivity.this,
                    "Please wait...");
            ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());
            JsonObject jsonObject1 = new JsonObject();

            jsonObject1.addProperty("address", SPData.getAppPreferences().getAddressId());
            jsonObject1.addProperty("deliverySlot", binding.timeSlot.getText().toString());
            jsonObject1.addProperty("varient", variantId);
            jsonObject1.addProperty("paymentMethod", "Online");

            apiService.PlacePreOrder(jsonObject1).enqueue(new Callback<OrderDetailInfo>() {
                @Override
                public void onResponse(@NonNull Call<OrderDetailInfo> call, @NonNull retrofit2.Response<OrderDetailInfo> response) {
                    CommonUtils.hideProgressDialog(customProgressDialog);
                    if (response.isSuccessful()) {
                        try {
                            OrderDetailInfo placeOrder = response.body();
                            String action = placeOrder.getData().getAction();
                            if (action.equals("razorpay")) {
                                orderId = placeOrder.getData().getOrderId();
                                if (SPData.usePaytm())
                                    startPaytmPayment(placeOrder.getData().getTxnToken(), orderId, placeOrder.getData().getAmount());
                                else
                                    new DoPayment().execute(options);
                            } else if (action.equals("order")) {
                                SPData.getAppPreferences().setTotalCartCount(0);
                                String order_id = placeOrder.getData().getOrder().getInvoiceNumber();
                                String payment_method = placeOrder.getData().getOrder().getPaymentMethod();
                                String payment_amount = "" + placeOrder.getData().getOrder().getAmount();
                                Intent intent = new Intent(PreOrderCheckoutActivity.this, OrderPlacedActivity.class);
                                intent.putExtra("order_id", "" + order_id);
                                intent.putExtra("payment_method", "" + payment_method);
                                intent.putExtra("payment_amount", "" + payment_amount);
                                startActivity(intent);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            orderId = "";
                            Toast.makeText(PreOrderCheckoutActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(PreOrderCheckoutActivity.this, "" + response.message(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<OrderDetailInfo> call, @NonNull Throwable t) {
                    CommonUtils.hideProgressDialog(customProgressDialog);
                    t.printStackTrace();
                    Toast.makeText(PreOrderCheckoutActivity.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startPaytmPayment(String token, String orderId, Float amount) {
        String callBackUrl = SPData.getPaytmCallbackurl() + orderId;
        PaytmOrder paytmOrder = new PaytmOrder(orderId, SPData.getPaytmMerchantId(), token, "" + amount, callBackUrl);

        TransactionManager transactionManager = new TransactionManager(paytmOrder, new PaytmPaymentTransactionCallback() {
            @Override
            public void onTransactionResponse(Bundle bundle) {
                JsonObject json = new JsonObject();
                Set<String> keys = bundle.keySet();
                for (String key : keys) {
                    try {
                        json.addProperty(key, bundle.getString(key));
                    } catch (Exception e) {
                        Log.e("TAG", "onTransactionResponse: " + e);
                    }
                }
                Log.e("TAG", "onTransactionResponse: " + json);
                if (json.get("STATUS").getAsString().equals("TXN_SUCCESS"))
                    verifyPayment(json);
                else {
                    CommonUtils.showErrorMessage(PreOrderCheckoutActivity.this, "Try again");
                }
            }

            @Override
            public void networkNotAvailable() {
                Log.e("TAG", "network not available ");
            }

            @Override
            public void onErrorProceed(String s) {
                Log.e("TAG", " onErrorProcess " + s.toString());
            }

            @Override
            public void clientAuthenticationFailed(String s) {
                Log.e("TAG", "Clientauth " + s);
            }

            @Override
            public void someUIErrorOccurred(String s) {
                Log.e("TAG", " UI error " + s);
            }

            @Override
            public void onErrorLoadingWebPage(int i, String s, String s1) {
                Log.e("TAG", " error loading web " + s + "--" + s1);
            }

            @Override
            public void onBackPressedCancelTransaction() {
                Log.e("TAG", "backPress ");
            }

            @Override
            public void onTransactionCancel(String s, Bundle bundle) {
                Log.e("TAG", " transaction cancel " + s);
            }
        });
        transactionManager.setAppInvokeEnabled(false);
        transactionManager.setShowPaymentUrl(SPData.getPaytmHost() + "theia/api/v1/showPaymentPage");
        transactionManager.startTransaction(PreOrderCheckoutActivity.this, PAYTM_REQUEST_CODE);

    }

    private void pickDate() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(PreOrderCheckoutActivity.this, (view, year, month, dayOfMonth) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth);
            Date date = calendar.getTime();
            try {
                CommonUtils.pickTime(date, false, binding.timeSlot, PreOrderCheckoutActivity.this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    @Override
    public void onPaymentSuccess(String s, PaymentData paymentData) {
        try {
            Log.e("TAG", "onPaymentSuccess: ");
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("paymentId", paymentData.getPaymentId());
            jsonObject.addProperty("orderId", paymentData.getOrderId());
            jsonObject.addProperty("signature", paymentData.getSignature());
            verifyPayment(jsonObject);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(PreOrderCheckoutActivity.this, "" + e, Toast.LENGTH_SHORT).show();
            Log.e("TAG", "onPaymentSuccess: " + e);
        }
    }

    @Override
    public void onPaymentError(int i, String s, PaymentData paymentData) {
        Log.e("TAG", "onPaymentError:: response:: " + s);
        CommonUtils.showErrorMessage(this, "Payment cancelled");
    }


    private void verifyPayment(JsonObject jsonObject) {
        try {
            final CustomProgressDialog customProgressDialog = CommonUtils.showProgressDialog(PreOrderCheckoutActivity.this,
                    "Verifying payment...");
            ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());
            apiService.verifyOnlinePayment(jsonObject).enqueue(new Callback<OrderDetailInfo>() {
                @Override
                public void onResponse(@NotNull Call<OrderDetailInfo> call, @NotNull Response<OrderDetailInfo> response) {
                    CommonUtils.hideProgressDialog(customProgressDialog);
                    Log.e("TAG", "onResponse: " + response);
                    if (response.isSuccessful()) {
                        try {
                            SPData.getAppPreferences().setPaymentSuccess(true);
                            SPData.getAppPreferences().setTotalCartCount(0);
                            OrderDetailInfo placeOrder = response.body();
                            String order_id = placeOrder.getData().getInvoiceNumber();
                            String payment_method = placeOrder.getData().getPaymentMethod();
                            String payment_amount = "" + placeOrder.getData().getAmount();
                            Intent intent = new Intent(PreOrderCheckoutActivity.this, OrderPlacedActivity.class);
                            intent.putExtra("order_id", "" + order_id);
                            intent.putExtra("payment_method", "" + payment_method);
                            intent.putExtra("payment_amount", "" + payment_amount);
                            startActivity(intent);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(PreOrderCheckoutActivity.this, "" + e, Toast.LENGTH_SHORT).show();
                            Log.e("TAG", "onResponse: " + e);
                        }
                    } else {
                        Log.e("TAG", "onResponse: " + response.errorBody());
                        Toast.makeText(PreOrderCheckoutActivity.this, "" + response.message(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NotNull Call<OrderDetailInfo> call, @NotNull Throwable t) {
                    Log.e("TAG", "onFailure: " + t);
                    CommonUtils.hideProgressDialog(customProgressDialog);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("TAG", "verifyPayment: " + e);
        }
    }

    @SuppressLint("StaticFieldLeak")
    class DoPayment extends AsyncTask<JSONObject, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(JSONObject... options) {
            try {
                options[0].put("name", SPData.getRazorPayScreenTitle(getString(R.string.app_name)));
                options[0].put("description", SPData.getRazorPayScreenSubtitle());
                options[0].put("order_id", orderId);
                options[0].put("prefill.email", SPData.getAppPreferences().getEmail());
                options[0].put("prefill.contact", SPData.getAppPreferences().getMobileNumber());
                checkout.open(PreOrderCheckoutActivity.this, options[0]);
                return options[0];
            } catch (Exception e) {
                Log.e("TAG", "doInBackground: " + e.toString());
                e.printStackTrace();
            }
            return options[0];
        }

        @Override
        protected void onPostExecute(JSONObject options) {
            super.onPostExecute(options);
        }
    }


}