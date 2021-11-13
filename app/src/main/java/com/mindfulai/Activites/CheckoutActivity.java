package com.mindfulai.Activites;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonObject;
import com.mindfulai.Adapter.CartAdapater;
import com.mindfulai.Adapter.UserAddressesAdapter;
import com.mindfulai.Models.CartInformation.CartDetailsInformation;
import com.mindfulai.Models.CartInformation.Product;
import com.mindfulai.Models.CustomerInfo.CustomerData;
import com.mindfulai.Models.DeliveryMethod.DeliveryMethodBase;
import com.mindfulai.Models.DeliveryMethod.Zones;
import com.mindfulai.Models.UserBaseAddress;
import com.mindfulai.Models.UserDataAddress;
import com.mindfulai.Models.WalletRechargeModel.WalletRechargeModel;
import com.mindfulai.Models.config.ConfigResponse;
import com.mindfulai.Models.orderDetailInfo.OrderDetailInfo;
import com.mindfulai.Models.shiprocket.ShiprocketDC;
import com.mindfulai.NetworkRetrofit.ApiService;
import com.mindfulai.NetworkRetrofit.ApiUtils;
import com.mindfulai.Utils.CommonUtils;
import com.mindfulai.Utils.CustomProgressDialog;
import com.mindfulai.Utils.SPData;
import com.mindfulai.customclass.Constants;
import com.mindfulai.ministore.R;
import com.mindfulai.ministore.databinding.CheckoutScreen2Binding;
import com.mindfulai.ui.NoShippingMethodFragment;
import com.paytm.pgsdk.PaytmOrder;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;
import com.paytm.pgsdk.TransactionManager;
import com.razorpay.Checkout;
import com.razorpay.PaymentData;
import com.razorpay.PaymentResultWithDataListener;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class CheckoutActivity extends AppCompatActivity implements PaymentResultWithDataListener {
    public static final String TAG = "CartFragment";
    private static final int APPLY_COUPON_REQUEST_CODE = 100;
    private static final int CHANGE_ADDRESS_REQUEST_CODE = 101;
    public List<Product> cartDataArrayList;
    public CartAdapater cartAdapater;
    public double orderValue, orderAboveOrSameChargeDeliveryCharge, orderBelowValueCharge, walletAmt = 0.00, coupondiscountAmt, subTotal, firstKg, afterFirstKg, totaldeliveryCharge;
    public boolean isCarryBagAdded, discountApplied;
    public String coupon, orderId, shippingScheme = "default";
    public CartDetailsInformation cartDetailsInformation;
    public ArrayList<String> allShippingMethods = new ArrayList<>();
    private Checkout checkout;
    public CheckoutScreen2Binding binding;
    public String pickupCenterAddressId;
    private final int PAYTM_REQUEST_CODE = 102;
    private float CARY_BAG_PRICE = 0;
    private double deliveryChargeByCoordinates;
    private double shiprocketDeliveyCharge;
    private double amntRemainingToPay;
    private String rechargeWalletID = "";

    @SuppressLint("InflateParams")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater layoutInflater = getLayoutInflater();
        layoutInflater.inflate(R.layout.checkout_screen2, null);
        binding = CheckoutScreen2Binding.inflate(layoutInflater);
        setContentView(binding.getRoot());

        getSupportActionBar().setTitle("Checkout");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initView();

        setView();

        getWallet();

        getConfig();

        if (SPData.allowAutomaticPickTimeSlot())
            CommonUtils.pickTime(new Date(), true, binding.timeSlot, CheckoutActivity.this);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == APPLY_COUPON_REQUEST_CODE && data != null) {
            boolean couponApplied = data.getBooleanExtra("coupon_applied", false);
            Log.e("TAG", "onActivityResult: "+couponApplied);
            if (couponApplied)
                applyCouponResult(data);
        } else {
            setUserAddress();
            if (SPData.showShippingMethods() || SPData.useShippingZonePincodeFeatureOnly()) {
                SPData.noShippingMsg = getString(R.string.not_product_available);
                getAllCart();
            } else if (SPData.useShiprocketDeliveryCharge()) {
                getShiprocketDeliveryCharge();
            } else if (SPData.useDeliveryChargeByCoordinates()) {
                getDeliveryChargeByCoordinates();
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void initView() {
        LinearLayoutManager verticalLayoutManager = new LinearLayoutManager(CheckoutActivity.this, LinearLayoutManager.VERTICAL, true);
        binding.rvCart.setLayoutManager(verticalLayoutManager);

        binding.subtotalRv.setVisibility(View.GONE);
        binding.couponDiscountLayout.setVisibility(View.GONE);
        binding.carybagLayout.setVisibility(View.GONE);
        binding.dvChargeLayout.setVisibility(View.GONE);
        binding.walletLayout.setVisibility(View.GONE);
        binding.shimmerViewContainer.setVisibility(View.GONE);

        cartDataArrayList = new ArrayList<>();

        Checkout.preload(CheckoutActivity.this);


        setUserAddress();
        checkSPData();

        cartDataArrayList.clear();

        binding.radioGroupDeliveryType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (binding.radioBtnPickup.isChecked()) {
                    binding.linearLayoutShippingAddressParent.setVisibility(View.GONE);
                } else {
                    binding.linearLayoutShippingAddressParent.setVisibility(View.VISIBLE);
                }
                setPaymentSummary();
            }
        });


        binding.radioGroupPaymentMethod.setOnCheckedChangeListener((radioGroup, i) -> {
            if (cartDetailsInformation != null) {
                if (SPData.useShiprocketDeliveryCharge())
                    getShiprocketDeliveryCharge();
                else
                    setPaymentSummary();

            }
        });

    }

    @SuppressLint("SetTextI18n")
    private void setUserAddress() {
        binding.addressType.setText(SPData.getAppPreferences().getUserShippingName() + ", " + SPData.getAppPreferences().getUserShippingMobile());
        binding.completeAddress.setText(SPData.getAppPreferences().getUserShippingAddress());
    }

    private void checkSPData() {
        if(SPData.showAgeCheckBox()){
            binding.checkboxAge.setVisibility(View.VISIBLE);
            binding.checkboxAge.setText(SPData.ageCheckBoxMsg());
        }else{
            binding.checkboxAge.setVisibility(View.GONE);
        }

        if (SPData.useShiprocketDeliveryCharge()) {
            shippingScheme = "shiprocket";
        }
        if (SPData.useDeliveryChargeByCoordinates()) {
            shippingScheme = "distance";
        }

        if (SPData.allowApplyCoupon()) {
            binding.applyCoupon.setVisibility(View.VISIBLE);
        } else {
            binding.applyCoupon.setVisibility(View.GONE);
        }

        if (SPData.allowPickUpOrder()) {
            binding.deliveryMethodLayout.setVisibility(View.VISIBLE);
        } else {
            binding.deliveryMethodLayout.setVisibility(View.GONE);
        }

        if (SPData.showCaryBag()) {
            binding.checkboxCaryBag.setVisibility(View.VISIBLE);
        } else
            binding.checkboxCaryBag.setVisibility(View.GONE);

        if (SPData.showOrderNote()) {
            binding.note.setVisibility(View.VISIBLE);
        } else {
            binding.note.setVisibility(View.GONE);
        }

        if (!SPData.cartPageMsgTxt().isEmpty()) {
            binding.msg.setVisibility(View.VISIBLE);
            binding.msg.setText(SPData.cartPageMsgTxt());
        } else
            binding.msg.setVisibility(View.GONE);

        if (SPData.showTimeSlotPicker()) {
            binding.timeSlotLayout.setVisibility(View.VISIBLE);
        } else {
            binding.timeSlotLayout.setVisibility(View.GONE);
        }

        if (SPData.showShippingMethods() && !SPData.useShippingZonePincodeFeatureOnly()) {
            binding.selectShippingMethodLayout.setVisibility(View.VISIBLE);
        } else {
            binding.selectShippingMethodLayout.setVisibility(View.GONE);
        }

        if (SPData.showOnlinePay()) {
            binding.radioBtnCod.setVisibility(View.GONE);
        }

        if (SPData.showCod()) {
            binding.radioBtnOnline.setVisibility(View.GONE);
        }

        if (SPData.showOnlineByDefault()) {
            binding.radioBtnOnline.setChecked(true);
        } else {
            binding.radioBtnCod.setChecked(true);
        }
    }

    private void getAllAddress(AlertDialog alertDialog, RecyclerView recyclerViewAddress) {
        try {
            ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());
            apiService.getAllShippingCenter().enqueue(new Callback<UserBaseAddress>() {
                @Override
                public void onResponse(@NonNull Call<UserBaseAddress> call, @NonNull Response<UserBaseAddress> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        ArrayList<UserDataAddress> userDataAddressArrayList = response.body().getData();
                        UserAddressesAdapter addressesAdapter = new UserAddressesAdapter(CheckoutActivity.this, userDataAddressArrayList, alertDialog);
                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(CheckoutActivity.this);
                        recyclerViewAddress.setLayoutManager(linearLayoutManager);
                        recyclerViewAddress.setAdapter(addressesAdapter);
                        addressesAdapter.notifyDataSetChanged();
                        alertDialog.show();
                    } else {
                        Log.e("TAG", "onResponse: " + response);
                    }
                }

                @Override
                public void onFailure(@NotNull Call<UserBaseAddress> call, @NotNull Throwable t) {
                    Log.e("TAG", "onFailure: " + t.getMessage());
                }
            });
        } catch (Exception e) {
            Toast.makeText(CheckoutActivity.this, "" + e, Toast.LENGTH_SHORT).show();
            Log.e("TAG", "getAllAddress: " + e);
        }
    }

    public void promptDialogToShowPickupCenterLst() {
        View view = LayoutInflater.from(CheckoutActivity.this).inflate(R.layout.dialog_pickk_address, null);
        RecyclerView recyclerViewAddress = view.findViewById(R.id.recycler_view_address);
        TextView timeSlottext = view.findViewById(R.id.select_time_text);
        TextView addAddress = view.findViewById(R.id.add_address);
        TextView timeSlot = view.findViewById(R.id.time_slot);
        ImageView close = view.findViewById(R.id.close);

        timeSlottext.setText("Select pickup center");
        timeSlot.setVisibility(View.GONE);
        addAddress.setVisibility(View.GONE);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(CheckoutActivity.this);
        alertDialog.setView(view);

        AlertDialog alertDialog1 = alertDialog.create();
        alertDialog1.setCanceledOnTouchOutside(false);

        getAllAddress(alertDialog1, recyclerViewAddress);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog1.dismiss();
            }
        });
    }


    private void setView() {
        binding.checkboxCaryBag.setOnCheckedChangeListener((compoundButton, b) -> {
            isCarryBagAdded = b;
            if (b) {
                handleCaryBagAdded();
            } else {
                binding.carybagLayout.setVisibility(View.GONE);
            }
            if(cartDetailsInformation!=null)
                setPaymentSummary();
        });

        binding.tvPayment.setOnClickListener(v -> {
            if(!SPData.showAgeCheckBox() || binding.checkboxAge.isChecked())
            if (!SPData.getAppPreferences().getUsertoken().isEmpty()) {
                if ((!SPData.getAppPreferences().getAddressId().isEmpty() && binding.radioBtnDelivery.isChecked()) || (pickupCenterAddressId != null && binding.radioBtnPickup.isChecked())) {
                    checkPaymentMethod();
                } else if (pickupCenterAddressId == null && binding.radioBtnPickup.isChecked()) {
                    promptDialogToShowPickupCenterLst();
                } else {
                    Toast.makeText(CheckoutActivity.this, "Please select a shipping address", Toast.LENGTH_SHORT).show();
                }
            } else {
                startActivity(new Intent(CheckoutActivity.this, LoginActivity.class));
            }
            else {
                Toast.makeText(this, "Please check the box to continue", Toast.LENGTH_SHORT).show();
            }
        });

        binding.applyCoupon.setOnClickListener(v -> {
            if (!SPData.getAppPreferences().getUsertoken().isEmpty())
                startActivityForResult(new Intent(CheckoutActivity.this, PromoCodeActivity.class).putExtra("subtotal", subTotal), APPLY_COUPON_REQUEST_CODE);
            else
                startActivity(new Intent(CheckoutActivity.this, LoginActivity.class));
        });
        binding.timeSlot.setOnClickListener(view -> pickDate());

        binding.proceedToCheckout.setOnClickListener(view -> {
            handleProceedToCheckout();
        });
        binding.change.setOnClickListener(view -> startActivityForResult(new Intent(CheckoutActivity.this, AddressSelectorActivity.class), CHANGE_ADDRESS_REQUEST_CODE));

    }

    public void handleProceedToCheckout() {
        if (SPData.getAppPreferences().getUsertoken().isEmpty())
            startActivity(new Intent(CheckoutActivity.this, LoginActivity.class));
        else
            startActivityForResult(new Intent(CheckoutActivity.this, AddressSelectorActivity.class), CHANGE_ADDRESS_REQUEST_CODE);
    }

    private void handleCaryBagAdded() {
        binding.carybagLayout.setVisibility(View.VISIBLE);
        binding.carybagCharge.setText("+ " + getString(R.string.rs) + CommonUtils.getTwoDigitsRoundOffValue(CARY_BAG_PRICE));
        binding.carybagCharge.setTextColor(getResources().getColor(R.color.colorError));
    }

    public void checkPaymentMethod() {
        Log.e("TAG", "checkPaymentMethod: " + amntRemainingToPay);
        if (SPData.showTimeSlotPicker() && binding.timeSlot.getText().toString().equals(getString(R.string.pickTime))) {
            Toast.makeText(CheckoutActivity.this, "Please select a time slot", Toast.LENGTH_SHORT).show();
        } else if (binding.radioBtnOnline.isChecked() || amntRemainingToPay > 0) {
            takePayment(amntRemainingToPay > 0);
        } else if (binding.radioBtnCod.isChecked()) {
            placeOrderCOD();
        } else {
            Toast.makeText(CheckoutActivity.this, "Please select a payment method", Toast.LENGTH_SHORT).show();
        }
    }

    private void takePayment(boolean forwallet) {
        checkout = new Checkout();
        checkout.setImage(R.mipmap.ic_launcher);
        checkout.setKeyID(SPData.getRazorPayKey());
        JSONObject options = new JSONObject();
        try {
            options.put("currency", "INR");
            options.put("receipt", "");
            options.put("payment_capture", true);
            if (forwallet) {
                rechargeWallet(options);
            } else {
                generateOrderId(options);
            }
        } catch (Exception e) {
            Log.e(TAG, "takePayment: " + e);
        }
    }

    private void rechargeWallet(JSONObject options) {
        final CustomProgressDialog customProgressDialog = CommonUtils.showProgressDialog(CheckoutActivity.this,
                "Please  wait...");
        ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("amount", amntRemainingToPay);
        apiService.walletRecharge(jsonObject).enqueue(new Callback<WalletRechargeModel>() {
            @Override
            public void onResponse(@NotNull Call<WalletRechargeModel> call, @NotNull Response<WalletRechargeModel> response) {
                CommonUtils.hideProgressDialog(customProgressDialog);
                try {
                    if (response.isSuccessful()) {
                        WalletRechargeModel responseModel = response.body();
                        assert responseModel != null;
                        rechargeWalletID = responseModel.getData().getOrderId();
                        options.put("id", rechargeWalletID);
                        CommonUtils.hideProgressDialog(customProgressDialog);

                        if (SPData.usePaytm())
                            startPaytmPayment(responseModel.getData().getTxnToken(), rechargeWalletID, responseModel.getData().getAmount());
                        else
                            new DoPayment().execute(options);
                    } else {
                        try {
                            Log.e("TAG", "onResponse: " + response.errorBody().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Toast.makeText(CheckoutActivity.this, "" + response.message(), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e("TAG", "onResponse: " + e);
                }
            }

            @Override
            public void onFailure(@NotNull Call<WalletRechargeModel> call, @NotNull Throwable t) {
                CommonUtils.hideProgressDialog(customProgressDialog);
            }
        });
    }

    private void placeOrderCOD() {
        try {
            final CustomProgressDialog customProgressDialog = CommonUtils.showProgressDialog(CheckoutActivity.this,
                    "Please wait...");
            ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());
            JsonObject jsonObject1 = requestBody();
            jsonObject1.addProperty("paymentMethod", "COD");
            apiService.PlaceOrder(jsonObject1).enqueue(new Callback<OrderDetailInfo>() {
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
                            Intent intent = new Intent(CheckoutActivity.this, OrderPlacedActivity.class);
                            intent.putExtra("order_id", "" + order_id);
                            intent.putExtra("payment_method", "" + payment_method);
                            intent.putExtra("payment_amount", "" + payment_amount);
                            CheckoutActivity.this.startActivity(intent);
                        } else {
                            CommonUtils.handleResponseError(CheckoutActivity.this,response);
                      }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(CheckoutActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<OrderDetailInfo> call, @NonNull Throwable t) {
                    CommonUtils.hideProgressDialog(customProgressDialog);
                    Toast.makeText(CheckoutActivity.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
                    t.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private JsonObject requestBody() {
        JsonObject jsonObject=new JsonObject();
        if (binding.radioBtnPickup.isChecked()) {
            jsonObject.addProperty("deliveryType", "pickup");
            jsonObject.addProperty("address", pickupCenterAddressId);
        } else {
            jsonObject.addProperty("address", SPData.getAppPreferences().getAddressId());
            jsonObject.addProperty("shippingScheme", shippingScheme);
            if (SPData.showTimeSlotPicker())
                jsonObject.addProperty("deliverySlot", binding.timeSlot.getText().toString());
            else
                jsonObject.addProperty("deliverySlot", " ");
            jsonObject.addProperty("deliveryType", "delivery");
        }
        jsonObject.addProperty("carryBag", isCarryBagAdded);
        if (coupon != null && !coupon.isEmpty()) {
            jsonObject.addProperty("coupon", coupon);
        }

        if (!binding.note.getText().toString().isEmpty()) {
            jsonObject.addProperty("note", binding.note.getText().toString());
        }
        return  jsonObject;
    }

    private void generateOrderId(final JSONObject options) {
        try {
            final CustomProgressDialog customProgressDialog = CommonUtils.showProgressDialog(CheckoutActivity.this,
                    "Please wait...");
            ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());
            JsonObject jsonObject1 = requestBody();
            jsonObject1.addProperty("paymentMethod", "Online");
            apiService.PlaceOrder(jsonObject1).enqueue(new Callback<OrderDetailInfo>() {
                @Override
                public void onResponse(@NonNull Call<OrderDetailInfo> call, @NonNull retrofit2.Response<OrderDetailInfo> response) {
                    CommonUtils.hideProgressDialog(customProgressDialog);
                    try {
                        if (response.isSuccessful()) {
                            OrderDetailInfo placeOrder = response.body();
                            String action = placeOrder.getData().getAction();
                            if (action.equals("razorpay")) {
                                orderId = placeOrder.getData().getOrderId();
                                options.put("id", orderId);
                                if (SPData.usePaytm())
                                    startPaytmPayment(placeOrder.getData().getTxnToken(), orderId, placeOrder.getData().getAmount());
                                else
                                    new DoPayment().execute(options);
                            } else if (action.equals("order")) {
                                SPData.getAppPreferences().setTotalCartCount(0);
                                String order_id = placeOrder.getData().getOrder().getInvoiceNumber();
                                String payment_method = placeOrder.getData().getOrder().getPaymentMethod();
                                String payment_amount = "" + placeOrder.getData().getOrder().getAmount();
                                Intent intent = new Intent(CheckoutActivity.this, OrderPlacedActivity.class);
                                intent.putExtra("order_id", "" + order_id);
                                intent.putExtra("payment_method", "" + payment_method);
                                intent.putExtra("payment_amount", "" + payment_amount);
                                CheckoutActivity.this.startActivity(intent);
                            }
                        } else {
                           CommonUtils.handleResponseError(CheckoutActivity.this,response);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        orderId = "";
                        Toast.makeText(CheckoutActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<OrderDetailInfo> call, @NonNull Throwable t) {
                    CommonUtils.hideProgressDialog(customProgressDialog);
                    t.printStackTrace();
                    Toast.makeText(CheckoutActivity.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
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
                    verifyPayment(json, orderId);
                else {
                    CommonUtils.showErrorMessage(CheckoutActivity.this, "Try again");
                }
            }

            @Override
            public void networkNotAvailable() {
                Log.e("TAG", "network not available ");
            }

            @Override
            public void onErrorProceed(String s) {
                Log.e("TAG", " onErrorProcess " + s);
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
        transactionManager.startTransaction(CheckoutActivity.this, PAYTM_REQUEST_CODE);

    }

    @SuppressLint("SetTextI18n")
    private void getDeliveryMethods(String pincode) {
        if (!SPData.getAppPreferences().getUsertoken().isEmpty() && !SPData.getAppPreferences().getAddressId().isEmpty()) {
            binding.proceedToCheckout.setVisibility(View.GONE);
            ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());
            apiService.getDeliveryMethods(pincode).enqueue(new Callback<DeliveryMethodBase>() {
                @Override
                public void onResponse(@NotNull Call<DeliveryMethodBase> call, @NotNull retrofit2.Response<DeliveryMethodBase> response) {
                    if (response.isSuccessful()) {
                        DeliveryMethodBase deliveryMethodBase = response.body();
                        allShippingMethods.clear();
                        binding.radioGroupDeliveryMethods.removeAllViews();
                        if (deliveryMethodBase != null && !deliveryMethodBase.isErrors()) {
                            RadioGroup radioGroup = new RadioGroup(CheckoutActivity.this);
                            for (int i = 0; i < deliveryMethodBase.getData().size(); i++) {
                                RadioButton radioButton = new RadioButton(CheckoutActivity.this);
                                radioButton.setText(deliveryMethodBase.getData().get(i).getName());
                                radioButton.setId(i);
                                radioGroup.addView(radioButton);
                                allShippingMethods.add(deliveryMethodBase.getData().get(i).getName());
                            }
                            binding.radioGroupDeliveryMethods.addView(radioGroup);

                            if (allShippingMethods.size() > 0 && !SPData.getAppPreferences().getAddressId().isEmpty()) {
                                radioGroup.check(radioGroup.getChildAt(0).getId());
                                ArrayList<Zones> zoneDataArrayList = deliveryMethodBase.getData().get(0).getZoneData();
                                shippingScheme = deliveryMethodBase.getData().get(0).get_id();
                                if (allShippingMethods.size() == 1) {
                                    binding.selectShippingMethodLayout.setVisibility(View.GONE);
                                } else {
                                    binding.selectShippingMethodLayout.setVisibility(View.VISIBLE);
                                }
                                if (zoneDataArrayList != null) {
                                    firstKg = zoneDataArrayList.get(0).getFirstKg();
                                    afterFirstKg = zoneDataArrayList.get(0).getAfterFirstKg();
                                    setPaymentSummary();
                                }
                            } else {
                                noaddressSelected(getString(R.string.no_shipping_method_available));
                            }

                            radioGroup.setOnCheckedChangeListener((radioGroup1, i) -> {
                                ArrayList<Zones> zoneDataArrayList = deliveryMethodBase.getData().get(i).getZoneData();
                                shippingScheme = deliveryMethodBase.getData().get(i).get_id();
                                if (zoneDataArrayList != null) {
                                    firstKg = zoneDataArrayList.get(0).getFirstKg();
                                    afterFirstKg = zoneDataArrayList.get(0).getAfterFirstKg();
                                    setPaymentSummary();
                                }
                            });
                        } else {
                            noaddressSelected(getString(R.string.no_shipping_method_available));
                            Toast.makeText(CheckoutActivity.this, "" + response.message(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(@NotNull Call<DeliveryMethodBase> call, @NotNull Throwable t) {
                    noaddressSelected(getString(R.string.no_shipping_method_available));
                }
            });
        } else {
            binding.paymentBottom.setVisibility(View.GONE);
            binding.deliveryNotification.setVisibility(View.GONE);
            binding.proceedToCheckout.setVisibility(View.VISIBLE);
            if (SPData.getAppPreferences().getUsertoken().isEmpty())
                binding.proceedToCheckout.setText("Cart Total Rs." + (cartDetailsInformation.getData().getTotal() - cartDetailsInformation.getData().getDeliveryFee()) + " Login To Proceed");
            else
                binding.proceedToCheckout.setText("Please select a shipping address");
        }
    }

    @SuppressLint("SetTextI18n")
    private void noaddressSelected(String message) {
        if(message.equals(getString(R.string.no_shipping_method_available))&&SPData.useNoShippingMethodBottomDialog()){
            NoShippingMethodFragment noShippingMethodFragment = new NoShippingMethodFragment(CheckoutActivity.this);
            noShippingMethodFragment.show(getSupportFragmentManager(),"");
        }
        binding.paymentBottom.setVisibility(View.GONE);
        binding.deliveryNotification.setVisibility(View.GONE);
        binding.proceedToCheckout.setVisibility(View.VISIBLE);
        binding.progressBar.setVisibility(View.GONE);
        SPData.noShippingMsg = message;
        binding.proceedToCheckout.setText(SPData.noShippingMsg + " " + SPData.getAppPreferences().getPincode() + "\n Cilck here to change address");
        for (Product product : cartDataArrayList) {
            product.setZoneValid(false);
        }
        cartAdapater.notifyDataSetChanged();
    }

    private void addressSelected() {
        binding.deliveryNotification.setVisibility(View.VISIBLE);
        binding.proceedToCheckout.setVisibility(View.GONE);
    }


    public void getShiprocketDeliveryCharge() {
        String paymentMode = "Online";
        if (binding.radioBtnCod.isChecked()) {
            paymentMode = "COD";
        }
        binding.proceedToCheckout.setVisibility(View.GONE);
        ApiService apiUtils = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());
        apiUtils.getShiprocketDeliveryCharge(SPData.getAppPreferences().getPincode(), paymentMode).enqueue(new Callback<ShiprocketDC>() {
            @Override
            public void onResponse(@NotNull Call<ShiprocketDC> call, @NotNull Response<ShiprocketDC> response) {
                Log.e("TAG", "getShiprocketDeliveryCharge onResponse: " + response);
                if (response.isSuccessful() && response.body() != null) {
                    ShiprocketDC shiprocketDC = response.body();
                    totaldeliveryCharge = shiprocketDC.getData().getDeliveryCharge().getRate();
                    shiprocketDeliveyCharge = totaldeliveryCharge;
                    Log.e("TAG", "onResponse: " + totaldeliveryCharge);
                    if (totaldeliveryCharge >= 0) {
                        addressSelected();
                        setPaymentSummary();
                    } else {
                        Log.e("TAG", "onResponse: " + response.errorBody());
                        noaddressSelected("Could not deliver to ");
                    }
                } else {
                    Log.e("TAG", "onResponse: " + response.errorBody());
                    noaddressSelected("Could not deliver to ");
                }
            }

            @Override
            public void onFailure(@NotNull Call<ShiprocketDC> call, @NotNull Throwable t) {
                noaddressSelected("Could not deliver to ");
            }
        });
    }

    public void getDeliveryChargeByCoordinates() {
        binding.proceedToCheckout.setVisibility(View.GONE);
        ApiService apiUtils = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());
        apiUtils.getDeliveryChargeByCoordinates(SPData.getAppPreferences().getUserShippingCoordinated().split(",")[0], SPData.getAppPreferences().getUserShippingCoordinated().split(",")[1]).enqueue(new Callback<ShiprocketDC>() {
            @Override
            public void onResponse(@NotNull Call<ShiprocketDC> call, @NotNull Response<ShiprocketDC> response) {
                Log.e("TAG", "getDeliveryChargeByCoordinates onResponse: " + response);
                if (response.isSuccessful() && response.body() != null) {
                    ShiprocketDC shiprocketDC = response.body();
                    totaldeliveryCharge = shiprocketDC.getData().getDeliveryCharge().getRate();
                    deliveryChargeByCoordinates = totaldeliveryCharge;
                    Log.e("TAG", "onResponse: " + totaldeliveryCharge);
                    if (totaldeliveryCharge >= 0) {
                        addressSelected();
                        setPaymentSummary();
                    } else {
                        noaddressSelected("Could not deliver to ");
                    }
                } else {
                    Log.e("TAG", "onResponse: " + response.errorBody());
                    noaddressSelected("Could not deliver to ");
                }
            }

            @Override
            public void onFailure(@NotNull Call<ShiprocketDC> call, @NotNull Throwable t) {
                noaddressSelected("Could not deliver to ");
            }
        });
    }

    @SuppressLint("SetTextI18n")
    public void setPaymentSummary() {
        try {
            double totalTaxAmt = 0.0;
            double totalSavingAmt = 0.0;
            double totalWeightAmt = 0.0;
            double mrp = 0.0;

            for (Product product : cartDetailsInformation.getData().getProducts()) {
                totalTaxAmt = totalTaxAmt + (product.getTaxAmt() * product.getQuantity());
                double discsaving = product.getDiscountAmount();
                mrp = mrp + product.getProduct().getPrice()*product.getQuantity();
                if (product.getProduct().getPrice() > 0) {
                    if(SPData.getAppPreferences().isMembershipPurchased()&&product.getProduct().getMemberPrice()>0){
                        totalSavingAmt = totalSavingAmt + (discsaving + (product.getProduct().getPrice() - product.getProduct().getMemberPrice())) * product.getQuantity();
                    }else{
                        totalSavingAmt = totalSavingAmt + (discsaving + (product.getProduct().getPrice() - product.getProduct().getSellingPrice())) * product.getQuantity();
                    }
                }else {
                    totalSavingAmt = totalSavingAmt + (discsaving) * product.getQuantity();
                }
                if (SPData.showShippingMethods() && !SPData.useShippingZonePincodeFeatureOnly()) {
                    totalWeightAmt = totalWeightAmt+product.getWeight() * product.getQuantity();
                }
            }

            subTotal = cartDetailsInformation.getData().getTotal() - cartDetailsInformation.getData().getDeliveryFee();

            if (!binding.radioBtnPickup.isChecked()) {
                if (!SPData.useShiprocketDeliveryCharge() && !SPData.useDeliveryChargeByCoordinates()) {
                    if (SPData.showShippingMethods() && !SPData.useShippingZonePincodeFeatureOnly()) {
                        if (totalWeightAmt > 1) {
                            totaldeliveryCharge = firstKg + (totalWeightAmt - 1) * afterFirstKg;
                        } else {
                            totaldeliveryCharge = firstKg;
                        }
                        if (cartDetailsInformation.getData().getTotal() <= orderValue) {
                            totaldeliveryCharge = totaldeliveryCharge + orderBelowValueCharge;
                        }
                        if (SPData.useFreeDeliveryCharge() && subTotal >= SPData.shippingChargeFreeAfterAmount()) {
                            totaldeliveryCharge = 0;
                        }
                    } else {
                        if (cartDetailsInformation.getData().getTotal() >= orderValue) {
                            totaldeliveryCharge = orderAboveOrSameChargeDeliveryCharge;
                        } else {
                            totaldeliveryCharge = orderBelowValueCharge;
                        }
                    }
                } else if (SPData.useDeliveryChargeByCoordinates()) {
                    totaldeliveryCharge = deliveryChargeByCoordinates;
                } else {
                    totaldeliveryCharge = shiprocketDeliveyCharge;
                }
            } else {
                totaldeliveryCharge = 0.0;
            }
            if(SPData.useExtraChargesForCOD()&&binding.radioBtnCod.isChecked()&&!SPData.getAppPreferences().isMembershipPurchased()){
                totaldeliveryCharge = totaldeliveryCharge+SPData.codExtraCharges();
                binding.codExtraAmount.setVisibility(View.VISIBLE);
                binding.codExtraAmount.setText("* "+getString(R.string.rs)+SPData.codExtraCharges()+" extra charges for COD.");
            }else{
                binding.codExtraAmount.setVisibility(View.GONE);
            }

            if (SPData.getAppPreferences().getUsertoken().isEmpty())
                binding.proceedToCheckout.setText("Cart Total Rs." + (cartDetailsInformation.getData().getTotal() - cartDetailsInformation.getData().getDeliveryFee()) + " Login To Proceed");

            if(SPData.getAppPreferences().isMembershipPurchased()){
                totaldeliveryCharge=0.0;
            }
            binding.deliveryFee.setText(getString(R.string.rs) + CommonUtils.getTwoDigitsRoundOffValue(totaldeliveryCharge));
            if (SPData.showTotalMrpValueInCheckout()) {
                binding.mrpRv.setVisibility(View.VISIBLE);
                binding.billValueAmount.setText(getString(R.string.rs) + CommonUtils.getTwoDigitsRoundOffValue(mrp));
            } else {
                binding.mrpRv.setVisibility(View.GONE);
            }

            binding.subtotalAmount.setText(getString(R.string.rs) + CommonUtils.getTwoDigitsRoundOffValue(subTotal));

            binding.tvSavings.setText(getString(R.string.rs) + CommonUtils.getTwoDigitsRoundOffValue(totalSavingAmt));
            if (totalTaxAmt > 0&&SPData.useGstCalculationInCheckout()) {
                binding.taxAmount.setVisibility(View.VISIBLE);
                binding.taxAmount.setText("CGST Included (" + getString(R.string.rs) + CommonUtils.getTwoDigitsRoundOffValue(totalTaxAmt / 2) + ")\n" + "SGST Included (" + getString(R.string.rs) + CommonUtils.getTwoDigitsRoundOffValue(totalTaxAmt / 2) + ")");
            } else {
                binding.taxAmount.setVisibility(View.GONE);
            }

            double totalAmt = subTotal + totaldeliveryCharge;
            Log.e("TAG", "setPaymentSummary:isCarryBagAdded "+isCarryBagAdded);
            Log.e("TAG", "setPaymentSummary:CARY_BAG_PRICE "+CARY_BAG_PRICE );
            if (isCarryBagAdded) {
                totalAmt = totalAmt + CARY_BAG_PRICE;
            }
            if (discountApplied) {
                totalAmt = totalAmt - coupondiscountAmt;
            }

            double amountToDeductFromCart = (SPData.amountToDeductFromWallet() * totalAmt) / 100;
            double onlinePayableAmt;
            if (amountToDeductFromCart <= walletAmt) {
                onlinePayableAmt = totalAmt - amountToDeductFromCart;
                binding.amountDeductFromWallet.setText("Amount deduct from wallet " + getResources().getString(R.string.rs) + CommonUtils.roundOffValue(amountToDeductFromCart));
            } else {
                onlinePayableAmt = totalAmt - walletAmt;
                if (SPData.forceToDeductAmntFromWallet() && binding.radioBtnCod.isChecked()) {
                    amntRemainingToPay = amountToDeductFromCart - walletAmt;
                    binding.amountDeductFromWallet.setText("You need to pay advance "+SPData.amountToDeductFromWallet()+"% amount of " + getResources().getString(R.string.rs) + CommonUtils.roundOffValue(totalAmt) + " by online to place the order by cod.");
                    binding.amountDeductFromWallet.setTextColor(getResources().getColor(R.color.colorError));
                } else {
                    amntRemainingToPay = 0;
                    binding.amountDeductFromWallet.setTextColor(getResources().getColor(R.color.colorText));
                    binding.amountDeductFromWallet.setText("Amount deduct from wallet " + getResources().getString(R.string.rs) + CommonUtils.roundOffValue(walletAmt));
                }
            }
            if(SPData.makeMandatoryToPayOnlineAfterAmount()&&onlinePayableAmt>=SPData.payOnlineAfterAmount()){
                binding.radioBtnCod.setVisibility(View.GONE);
                binding.radioBtnOnline.setVisibility(View.VISIBLE);
                binding.radioBtnOnline.setChecked(true);
            }
            binding.tvTotalAmount.setText("" + getResources().getString(R.string.rs) + CommonUtils.getTwoDigitsRoundOffValue(onlinePayableAmt));

            float cartt = Float.parseFloat(new DecimalFormat("#.#").format((subTotal)));
            if (cartt < orderValue && SPData.showDeliveryNotification()&&!SPData.getAppPreferences().isMembershipPurchased()) {
                if (!cartDataArrayList.isEmpty()) {
                    binding.deliveryNotification.setVisibility(View.VISIBLE);
                }
                binding.deliveryNotificationText.setText("Shop for " + getString(R.string.rs) + CommonUtils.getTwoDigitsRoundOffValue(orderValue - cartt) + " more and get this order delivered for " + getString(R.string.rs) + orderAboveOrSameChargeDeliveryCharge);
            } else {
                binding.deliveryNotification.setVisibility(View.GONE);
            }
            binding.layoutPayment.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.GONE);
        } catch (Exception e) {
            handleNoCartItem();
            Toast.makeText(this, "Try again after some time", Toast.LENGTH_SHORT).show();
            Log.e("TAG", "setPaymentSummary: " + e);
        }
    }

    private void getWallet() {
        if (!SPData.getAppPreferences().getUsertoken().isEmpty()) {
            ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());
            apiService.getProfileDetails().enqueue(new Callback<CustomerData>() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onResponse(@NotNull Call<CustomerData> call, @NotNull Response<CustomerData> response) {
                    if (response.isSuccessful()) {
                        walletAmt = response.body().getData().getUser().getWallet();
                        binding.tvWalletBalance.setText(getResources().getString(R.string.rs) + CommonUtils.getTwoDigitsRoundOffValue(response.body().getData().getUser().getWallet()));
                    }
                }

                @Override
                public void onFailure(@NotNull Call<CustomerData> call, @NotNull Throwable t) {
                    t.printStackTrace();
                }
            });
        }
    }

    private void pickDate() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(CheckoutActivity.this, (view, year, month, dayOfMonth) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth);
            Date date = calendar.getTime();
            try {
                CommonUtils.pickTime(date, false, binding.timeSlot, CheckoutActivity.this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void getConfig() {
        ApiService apiService;
        if (!SPData.getAppPreferences().getUsertoken().isEmpty()) {
            apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());
        } else {
            apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUserUniqueId());
        }
        apiService.getConfig().enqueue(new Callback<ConfigResponse>() {
            @Override
            public void onResponse(@NotNull Call<ConfigResponse> call, @NotNull Response<ConfigResponse> response) {
                if (response.isSuccessful() && response.body() != null&&response.body().getData()!=null) {
                    orderValue = Objects.requireNonNull(response.body().getData()).getDeliveryCharges().getOrderValue();
                    orderAboveOrSameChargeDeliveryCharge = response.body().getData().getDeliveryCharges().getAboveOrSameValueCharge();
                    CARY_BAG_PRICE = response.body().getData().getCarryBagPrice();
                    orderBelowValueCharge = response.body().getData().getDeliveryCharges().getBelowValueCharge();
                    if(SPData.useCaryBagChargeByDefault()&&SPData.showCaryBag()){
                        binding.checkboxCaryBag.setChecked(true);
                        binding.checkboxCaryBag.setEnabled(false);
                        handleCaryBagAdded();
                    }
                    getAllCart();
                } else {
                    Log.e("CartConfig", response.message());
                    handleNoCartItem();
                }
            }

            @Override
            public void onFailure(@NotNull Call<ConfigResponse> call, @NotNull Throwable t) {
                t.printStackTrace();
                handleNoCartItem();

            }
        });

    }

    @Override
    public void onPaymentSuccess(String s, PaymentData paymentData) {
        try {
            Log.e("TAG", "onPaymentSuccess: ");
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("paymentId", paymentData.getPaymentId());
            jsonObject.addProperty("orderId", paymentData.getOrderId());
            jsonObject.addProperty("signature", paymentData.getSignature());
            verifyPayment(jsonObject, paymentData.getOrderId());
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(CheckoutActivity.this, "" + e, Toast.LENGTH_SHORT).show();
            Log.e("TAG", "onPaymentSuccess: " + e);
        }
    }

    @Override
    public void onPaymentError(int i, String s, PaymentData paymentData) {
        Log.e("TAG", "onPaymentError:: response:: " + s);
        CommonUtils.showErrorMessage(this, "Payment cancelled");
    }


    private void verifyPayment(JsonObject jsonObject, String orderId) {
        try {
            final CustomProgressDialog customProgressDialog = CommonUtils.showProgressDialog(CheckoutActivity.this,
                    "Verifying payment...");
            ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());
            apiService.verifyOnlinePayment(jsonObject).enqueue(new Callback<OrderDetailInfo>() {
                @Override
                public void onResponse(@NotNull Call<OrderDetailInfo> call, @NotNull Response<OrderDetailInfo> response) {
                    CommonUtils.hideProgressDialog(customProgressDialog);
                    Log.e("TAG", "onResponse: " + response);
                    try {
                        if (response.isSuccessful()) {
                            if (orderId.equals(rechargeWalletID)) {
                                placeOrderCOD();
                            } else {
                                SPData.getAppPreferences().setPaymentSuccess(true);
                                SPData.getAppPreferences().setTotalCartCount(0);
                                OrderDetailInfo placeOrder = response.body();
                                String order_id = placeOrder.getData().getInvoiceNumber();
                                String payment_method = placeOrder.getData().getPaymentMethod();
                                String payment_amount = "" + placeOrder.getData().getAmount();
                                Intent intent = new Intent(CheckoutActivity.this, OrderPlacedActivity.class);
                                intent.putExtra("order_id", "" + order_id);
                                intent.putExtra("payment_method", "" + payment_method);
                                intent.putExtra("payment_amount", "" + payment_amount);
                                startActivity(intent);
                            }
                        } else {
                            String error = response.errorBody().string();
                            JSONObject jsonObject1 = new JSONObject(error);
                            Toast.makeText(CheckoutActivity.this, "" + jsonObject1.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(CheckoutActivity.this, "" + e, Toast.LENGTH_SHORT).show();
                        Log.e("TAG", "onResponse: " + e);
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


    @SuppressLint("SetTextI18n")
    private void applyCouponResult(Intent data) {
        discountApplied = true;
        coupondiscountAmt = data.getDoubleExtra("discount", 0);
        coupon = data.getStringExtra("code");
        binding.applyCoupon.setText("Coupon applied successfully.\nTotal discount: " + CheckoutActivity.this.getResources().getString(R.string.rs) + CommonUtils.getTwoDigitsRoundOffValue(coupondiscountAmt));
        binding.applyCoupon.setTextColor(CheckoutActivity.this.getResources().getColor(R.color.colorGreen));
        binding.couponDiscountLayout.setVisibility(View.VISIBLE);
        binding.couponDiscount.setText("- " + CheckoutActivity.this.getResources().getString(R.string.rs) + CommonUtils.getTwoDigitsRoundOffValue(coupondiscountAmt));
        binding.couponDiscount.setTextColor(CheckoutActivity.this.getResources().getColor(R.color.colorGreen));
        setPaymentSummary();
    }

    private void getAllCart() {
        ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());
        apiService.showCartItems(SPData.getAppPreferences().getPincode()).enqueue(new Callback<CartDetailsInformation>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(@NotNull Call<CartDetailsInformation> call, @NotNull Response<CartDetailsInformation> response) {
                Log.e("TAG", "get cart onResponse: " + response);
                checkResponseFromCart(response);
            }

            @Override
            public void onFailure(@NonNull Call<CartDetailsInformation> call, @NonNull Throwable t) {
                handleNoCartItem();
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void checkResponseFromCart(Response<CartDetailsInformation> response) {

        binding.shimmerViewContainer.stopShimmerAnimation();
        binding.shimmerViewContainer.setVisibility(View.GONE);
        if (response.isSuccessful()) {
            cartDetailsInformation = response.body();
            cartDataArrayList.clear();
            cartDataArrayList = cartDetailsInformation.getData().getProducts();
            if (cartDataArrayList.size() == 0) {
                binding.noProducts.setVisibility(View.VISIBLE);
                binding.rvCart.setVisibility(View.GONE);
                binding.layoutPayment.setVisibility(View.GONE);
                binding.paymentBottom.setVisibility(View.GONE);
                binding.deliveryNotification.setVisibility(View.GONE);
            } else {
                cartAdapater = new CartAdapater(CheckoutActivity.this);
                binding.rvCart.setAdapter(cartAdapater);
                cartAdapater.notifyDataSetChanged();

                binding.noProducts.setVisibility(View.GONE);
                binding.rvCart.setVisibility(View.VISIBLE);
                binding.paymentBottom.setVisibility(View.VISIBLE);

                binding.subtotalRv.setVisibility(View.VISIBLE);
                binding.dvChargeLayout.setVisibility(View.VISIBLE);
                binding.walletLayout.setVisibility(View.VISIBLE);

                binding.tvPayment.setVisibility(View.VISIBLE);


                if (SPData.showShippingMethods() && !SPData.useShippingZonePincodeFeatureOnly()) {
                    binding.selectShippingMethodLayout.setVisibility(View.VISIBLE);
                    getDeliveryMethods(SPData.getAppPreferences().getPincode());
                } else
                    binding.selectShippingMethodLayout.setVisibility(View.GONE);
            }
        } else {
            handleNoCartItem();
        }

    }

    private void handleNoCartItem() {
        binding.shimmerViewContainer.stopShimmerAnimation();
        binding.shimmerViewContainer.setVisibility(View.GONE);
        binding.noProducts.setVisibility(View.VISIBLE);
        binding.rvCart.setVisibility(View.GONE);
        binding.layoutPayment.setVisibility(View.GONE);
        binding.paymentBottom.setVisibility(View.GONE);
        binding.deliveryNotification.setVisibility(View.GONE);
        binding.progressBar.setVisibility(View.GONE);
    }

    @SuppressLint("StaticFieldLeak")
    class DoPayment extends AsyncTask<JSONObject, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(JSONObject... options) {
            try {
                options[0].put("name", SPData.getRazorPayScreenTitle(getString(R.string.app_name)));
                options[0].put("description", SPData.getRazorPayScreenSubtitle());
                options[0].put("order_id", options[0].getString("id"));
                options[0].put("prefill.email", SPData.getAppPreferences().getMobileNumber()+"@gmail.com");
                options[0].put("prefill.contact", SPData.getAppPreferences().getMobileNumber());
                options[0].remove("id");
                checkout.open(CheckoutActivity.this, options[0]);
                return options[0];
            } catch (Exception e) {
                Log.e("TAG", "doInBackground: " + e);
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