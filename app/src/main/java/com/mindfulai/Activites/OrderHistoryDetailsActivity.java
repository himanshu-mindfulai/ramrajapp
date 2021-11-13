package com.mindfulai.Activites;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.gson.JsonObject;
import com.mindfulai.Adapter.CustomOrderAdapter;
import com.mindfulai.Adapter.OrderHistoryDetailsAdapter;
import com.mindfulai.Models.AllOrderHistory.Datum;
import com.mindfulai.Models.AllOrderHistory.DatumModel;
import com.mindfulai.Models.AllOrderHistory.Product;
import com.mindfulai.Models.CustomOrderData;
import com.mindfulai.Models.UserDataAddress;
import com.mindfulai.NetworkRetrofit.ApiService;
import com.mindfulai.NetworkRetrofit.ApiUtils;
import com.mindfulai.Utils.CommonUtils;
import com.mindfulai.Utils.SPData;
import com.mindfulai.ministore.R;
import com.mindfulai.ministore.databinding.ActivityOrderHistoryDetailsBinding;
import com.valdesekamdem.library.mdtoast.MDToast;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.view.View.GONE;

public class OrderHistoryDetailsActivity extends AppCompatActivity {

    public String productOrderStatus = "";
    public double totalSavings = 0.0;
    public int product_position = -1;
    private List<Product> productsList = new ArrayList<>();
    private UserDataAddress userDataAddress;
    private OrderHistoryDetailsAdapter orderHistoryDetailsAdapter;
    private String orderDate, orderType;
    private boolean fabExpanded = false;
    private JSONObject jsonObject;
    private ProgressDialog progressDialog;
    public ActivityOrderHistoryDetailsBinding binding;
    public Datum order;
    public double ordercouponDiscount;
    private String note;
    private SimpleDateFormat iso;
    public long diffHours;

    @SuppressLint({"SetTextI18n", "InflateParams"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater layoutInflater = getLayoutInflater();
        layoutInflater.inflate(R.layout.activity_order_history_details, null);
        binding = ActivityOrderHistoryDetailsBinding.inflate(layoutInflater);
        setContentView(binding.getRoot());


        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        checkIntent();

        findOrderDateDiff();

        checkOrderStatus();

        hideFabSubmenu();

        setOnClick();

        setOrderInfo();

        if (order.getItems() != null) {
            binding.paymentSummaryCard.setVisibility(GONE);
            binding.paymentSummaryTxt.setVisibility(GONE);
            setPaymentMethod();
            setAllCustomOrderItems();
        } else {
            showAllOrderItems();
            setPaymentSummary();
        }


    }

    private void findOrderDateDiff() {
        try {
            iso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            Date dateOrder = CommonUtils.gmttoLocalDate(Objects.requireNonNull(iso.parse(order.getOrderDate())));
            Date dateNow = new Date();
            long diff = iso.parse(iso.format(dateNow)).getTime() - dateOrder.getTime();
            diffHours = diff / (60 * 60 * 1000);
            Log.e("TAG", "setOrderInfo:diffHours " + diffHours);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setAllCustomOrderItems() {
        binding.productsRecyclerView.setHasFixedSize(true);
        binding.productsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ArrayList<CustomOrderData> customOrderData = new ArrayList<>();
        customOrderData.addAll(order.getItems());
        CustomOrderAdapter customOrderAdapter = new CustomOrderAdapter(this, customOrderData);
        binding.productsRecyclerView.setAdapter(customOrderAdapter);
        customOrderAdapter.notifyDataSetChanged();
    }

    @SuppressLint("SetTextI18n")
    private void setOrderInfo() {

        if (order.getAdminNote() != null && !order.getAdminNote().isEmpty()) {
            binding.msg.setVisibility(View.VISIBLE);
            binding.msg.setText(order.getAdminNote());
        } else
            binding.msg.setVisibility(GONE);

        binding.orderId.setText("Order number: " + order.getInvoiceNumber());
        binding.placedOn.setText(orderDate);

        if (order.getDeliveryDetails() != null && showButton()&&SPData.trackOrder())
            binding.deliveryDetails.setVisibility(View.VISIBLE);
        else
            binding.deliveryDetails.setVisibility(GONE);

        if (!order.getStatus().equals("cancelled")) {
            if (SPData.showTimeSlotPicker() && order.getDeliverySlot() != null && !order.getDeliverySlot().equals(getString(R.string.pickTime)))
                binding.deliverySlot.setText("Your order will be delivered between " + order.getDeliverySlot());
            else {
                binding.deliverySlot.setVisibility(GONE);
            }
        } else {
            binding.deliverySlot.setVisibility(GONE);
        }

        if (userDataAddress != null) {
            String ot = "";
            if (orderType.equals("pickup"))
                ot = "Pickup \n";
            binding.orderAddress.setText(ot +
                    CommonUtils.capitalizeWord(userDataAddress.getName()) + " " + userDataAddress.getMobile_number() + "\n" +
                    userDataAddress.getAddressLine1() + ", " + userDataAddress.getAddressLine2() + "\n" +
                    CommonUtils.capitalizeWord(userDataAddress.getCity()) + "\n" +
                    CommonUtils.capitalizeWord(userDataAddress.getState()) + ", " +
                    userDataAddress.getPincode() + "\n");
        } else if (orderType.equals("pickup")) {
            binding.orderAddress.setText("PICKUP ORDER");
        } else {
            binding.orderAddress.setVisibility(GONE);
        }
    }

    @SuppressLint("SetTextI18n")
    private void setPaymentSummary() {
        binding.totalAmount.setText(getResources().getString(R.string.rs) + CommonUtils.getTwoDigitsRoundOffValue(order.getAmount()));
        binding.carybagCharge.setText(getString(R.string.rs) + CommonUtils.getTwoDigitsRoundOffValue(order.getCarryBagCharge()));
        binding.dvCharge.setText(getString(R.string.rs) + CommonUtils.getTwoDigitsRoundOffValue(order.getDeliveryCharge()));
        binding.walletAmount.setText(getResources().getString(R.string.rs) + CommonUtils.getTwoDigitsRoundOffValue(order.getPaidFromWallet()));
        binding.totalPaid.setText(getResources().getString(R.string.rs) + CommonUtils.getTwoDigitsRoundOffValue(order.getAmount() - order.getPaidFromWallet()));

        double subtotal;
        ordercouponDiscount = 0.0;

        if (order.getCoupon() != null) {
            ordercouponDiscount = order.getCoupon().getDiscountAmt();
            binding.couponDiscount.setText(getString(R.string.rs) + CommonUtils.getTwoDigitsRoundOffValue(order.getCoupon().getDiscountAmt()));
        } else
            binding.couponDiscount.setText(getString(R.string.rs) + "0.00");


        subtotal = order.getAmount() - ((order.getCarryBagCharge() + order.getDeliveryCharge()) - ordercouponDiscount);

        binding.subtotalAmount.setText(getString(R.string.rs) + CommonUtils.getTwoDigitsRoundOffValue(subtotal));
        if (order.getCoupon() != null)
            binding.totalSaving.setText(getString(R.string.rs) + CommonUtils.getTwoDigitsRoundOffValue(totalSavings + order.getCoupon().getDiscountAmt()));
        else
            binding.totalSaving.setText(getString(R.string.rs) + CommonUtils.getTwoDigitsRoundOffValue(totalSavings));


        setPaymentMethod();
    }

    private void setPaymentMethod() {
        if (order.getPaidFromWallet() == 0) {
            if (order.getPaymentMethod() != null)
                binding.orderPayment1.setText(order.getPaymentMethod());
            else
                binding.orderPayment1.setText("N/A");
        } else {
            binding.orderPayment1.setText(order.getPaymentMethod() + " + Wallet");
        }

    }

    private void showAllOrderItems() {
        binding.productsRecyclerView.setHasFixedSize(true);
        binding.productsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        productsList = order.getProducts();
        orderHistoryDetailsAdapter = new OrderHistoryDetailsAdapter(OrderHistoryDetailsActivity.this, productsList, order.getId());
        binding.productsRecyclerView.setAdapter(orderHistoryDetailsAdapter);
    }

    private void setOnClick() {

        binding.deliveryDetails.setOnClickListener(v -> {
            String url = order.getDeliveryDetails();
            if (CommonUtils.stringIsNotNullAndEmpty(url) && URLUtil.isValidUrl(url)) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });
        binding.cancelOrder.setOnClickListener(view -> askForNote());


        binding.floatingActionButton.setOnClickListener(v -> {
            if (SPData.showBothCallAndWhatsAppOnHome())
                if (fabExpanded) {
                    hideFabSubmenu();
                } else {
                    showFabSubmenu();
                }
            else if (SPData.showOnlyWhatsAppOnHome()) {
                CommonUtils.sendToWhatsApp(OrderHistoryDetailsActivity.this);
            } else {
                CommonUtils.sendToPhone(OrderHistoryDetailsActivity.this, SPData.supportCallNumber());
            }
        });
        binding.fabCall.setOnClickListener(v -> CommonUtils.sendToPhone(OrderHistoryDetailsActivity.this, SPData.supportCallNumber()));
        binding.fabWhatsapp.setOnClickListener(v -> CommonUtils.sendToWhatsApp(OrderHistoryDetailsActivity.this));
    }

    private void askForNote() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.order_action_note, null);
        TextView textViewReason = view.findViewById(R.id.text_reason);
        TextView textViewLetusknow = view.findViewById(R.id.letusKnow);
        textViewReason.setText("Reason for Cancellation");
        textViewLetusknow.setText("Let us know why you want to cancel the order");
        EditText editText = view.findViewById(R.id.et_note);
        TextView cancel = view.findViewById(R.id.cancel);
        Button save = view.findViewById(R.id.save);
        builder.setView(view);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.cancel();
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                note = editText.getText().toString();
                if (!note.replaceAll(" ", "").isEmpty()) {
                    alertDialog.cancel();
                    progressDialog = new ProgressDialog(OrderHistoryDetailsActivity.this);
                    progressDialog.setMessage("Please wait...");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();
                    cancelBooking(order.getId());
                } else {
                    Toast.makeText(OrderHistoryDetailsActivity.this, "Please enter a note", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void checkOrderStatus() {

        binding.orderStatus.setText(order.getStatus());
        if (showButton()) {
            if (SPData.showOrderCancelInOrderHistoryDetail()) {
                if ((SPData.allowOrderCancelBefore() && diffHours > SPData.orderCancelTime()))
                    binding.cancelOrder.setVisibility(View.GONE);
                else
                    binding.cancelOrder.setVisibility(View.VISIBLE);
            } else {
                binding.cancelOrder.setVisibility(GONE);
            }
            binding.deliverySlot.setVisibility(View.VISIBLE);
        } else {
            binding.cancelOrder.setVisibility(GONE);
            binding.deliverySlot.setVisibility(GONE);
        }
        if (order.getStatus().equals("Cancelled") || order.getStatus().equals("Delivered")) {
            handleCancelledOrDelivered(order.getStatus());
        }
    }

    private void handleCancelledOrDelivered(String orderStatus) {
        binding.orderStatus.setText(orderStatus);
        binding.deliverySlot.setVisibility(GONE);
        binding.cancelOrder.setVisibility(GONE);
    }

    private boolean showButton() {
        return !order.getStatus().equals("Delivered") && !order.getStatus().equals("Cancelled");
    }


    private void checkIntent() {
        if (getIntent() != null) {
            Intent intent = getIntent();
            intent.setExtrasClassLoader(Datum.class.getClassLoader());
            order = intent.getParcelableExtra("order");
            orderDate = intent.getStringExtra("order_date");
            userDataAddress = order.getAddress();
            orderType = order.getOrderType();
            if (orderType == null) {
                orderType = "delivery";
            }
        }
    }


    @Override
    public void onBackPressed() {
        handleBackPressed();
    }


    private void hideFabSubmenu() {
        binding.fabSubmenu.setVisibility(GONE);
        if (SPData.useCompanyLogoInCall() && CommonUtils.stringIsNotNullAndEmpty(SPData.getLogo()))
            Glide.with(this).load(SPData.getBucketUrl() + SPData.getLogo()).into(binding.floatingActionButton);
        else
            binding.floatingActionButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_call_24));
        fabExpanded = false;
    }

    private void showFabSubmenu() {
        binding.fabSubmenu.setVisibility(View.VISIBLE);
        binding.floatingActionButton.setImageResource(R.drawable.ic_baseline_close_24);
        fabExpanded = true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            handleBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    private void handleBackPressed() {
        if (order.getStatus().equals("Cancelled")) {
            setResult(RESULT_OK, getIntent().putExtra("for", "order"));
        }
        if (!productOrderStatus.isEmpty() && product_position != -1) {
            setResult(RESULT_OK, getIntent().putExtra("for", "product").putExtra("action", productOrderStatus).putExtra("product_position", product_position));
        }
        finish();
    }

    private void cancelBooking(String id) {
        JsonObject params = new JsonObject();
        params.addProperty("status", "Cancelled");
        params.addProperty("reason", note);
        ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());
        apiService.cancelOrder(id, params).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NotNull Call<JsonObject> call, @NotNull Response<JsonObject> response) {
                progressDialog.cancel();
                if (response.isSuccessful() && response.body() != null && response.body().get("status").getAsInt() == 200) {
                    MDToast.makeText(OrderHistoryDetailsActivity.this, "Order Cancelled Successfully", Toast.LENGTH_SHORT, MDToast.TYPE_SUCCESS).show();
                    order.setStatus("Cancelled");
                    handleCancelledOrDelivered(order.getStatus());
                    binding.productsRecyclerView.setAdapter(null);
                    orderHistoryDetailsAdapter = new OrderHistoryDetailsAdapter(OrderHistoryDetailsActivity.this, productsList, order.getId());
                    binding.productsRecyclerView.setAdapter(orderHistoryDetailsAdapter);
                } else {
                    Toast.makeText(OrderHistoryDetailsActivity.this, "" + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NotNull Call<JsonObject> call, @NotNull Throwable t) {
                progressDialog.cancel();
            }
        });
    }
}
