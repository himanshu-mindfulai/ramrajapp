package com.mindfulai.customclass;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import com.google.gson.JsonObject;
import com.mindfulai.Activites.CheckMembershipActivity;
import com.mindfulai.Activites.MainActivity;
import com.mindfulai.Activites.WalletActivity;
import com.mindfulai.Models.WalletRechargeModel.WalletRechargeModel;
import com.mindfulai.Models.orderDetailInfo.OrderDetailInfo;
import com.mindfulai.NetworkRetrofit.ApiService;
import com.mindfulai.NetworkRetrofit.ApiUtils;
import com.mindfulai.Utils.CommonUtils;
import com.mindfulai.Utils.CustomProgressDialog;
import com.mindfulai.Utils.SPData;
import com.mindfulai.kwikapi.GasBillActivity;
import com.mindfulai.kwikapi.MobileRechargeActivity;
import com.mindfulai.kwikapi.PostpaidRechargeActivity;
import com.mindfulai.ministore.R;
import com.mindfulai.ui.WalletFragment;
import com.paytm.pgsdk.PaytmOrder;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;
import com.paytm.pgsdk.TransactionManager;
import com.razorpay.Checkout;
import com.valdesekamdem.library.mdtoast.MDToast;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;

public class DoPayment {

    private final Activity activity;
    private String orderID;
    private final Checkout checkout;
    private String txnToken;
    private String amount;

    public DoPayment(Activity activity, String orderID, String txnToken, String amount) {
        this.activity = activity;
        this.orderID = orderID;
        this.txnToken = txnToken;
        this.amount = amount;
        checkout = new Checkout();
        checkout.setImage(R.mipmap.ic_launcher);
        checkout.setKeyID(SPData.getRazorPayKey());
    }

    public void openCheckout() {
        if (SPData.usePaytm()) {
            if (txnToken != null && !txnToken.isEmpty())
                startPaytmPayment(txnToken, orderID, amount);
            else {
                Toast.makeText(activity, "Txn token is not available try again after some time", Toast.LENGTH_SHORT).show();
            }
        } else
            new RazorpayPayment().execute(new JSONObject());
    }

    public void rechargeWallet(double rechargeAmount) {
        final CustomProgressDialog customProgressDialog = CommonUtils.showProgressDialog(activity,
                "Please  wait...");
        ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("amount", rechargeAmount);
        apiService.walletRecharge(jsonObject).enqueue(new Callback<WalletRechargeModel>() {
            @Override
            public void onResponse(@NotNull Call<WalletRechargeModel> call, @NotNull Response<WalletRechargeModel> response) {
                CommonUtils.hideProgressDialog(customProgressDialog);
                if (response.isSuccessful()) {
                    WalletRechargeModel responseModel = response.body();
                    assert responseModel != null;
                    orderID = responseModel.getData().getOrderId();
                    txnToken = responseModel.getData().getTxnToken();
                    amount = String.valueOf(rechargeAmount);
                    Log.e("TAG", "onResponse:txnToken " + txnToken);
                    Log.e("TAG", "onResponse: " + orderID);
                    CommonUtils.hideProgressDialog(customProgressDialog);
                    openCheckout();
                } else {
                    Toast.makeText(activity, "" + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NotNull Call<WalletRechargeModel> call, @NotNull Throwable t) {
                CommonUtils.hideProgressDialog(customProgressDialog);
            }
        });
    }

    public void startPaytmPayment(String token, String orderId, String amount) {
        String callBackUrl = SPData.getPaytmCallbackurl() + orderId;
        PaytmOrder paytmOrder = new PaytmOrder(orderId, SPData.getPaytmMerchantId(), token, amount, callBackUrl);
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

                if (json.get("STATUS").getAsString().equals("TXN_SUCCESS"))
                    verifyPayment(json);
                else {
                    CommonUtils.showErrorMessage(activity, "Try again");
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
        transactionManager.startTransaction(activity, 100);

    }

    public void verifyPayment(JsonObject jsonObject) {
        final CustomProgressDialog customProgressDialog = CommonUtils.showProgressDialog(activity,
                "Verifying payment..");
        ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());
        apiService.verifyOnlinePayment(jsonObject).enqueue(new Callback<OrderDetailInfo>() {
            @Override
            public void onResponse(@NotNull Call<OrderDetailInfo> call, @NotNull Response<OrderDetailInfo> response) {
                CommonUtils.hideProgressDialog(customProgressDialog);
                if (response.isSuccessful() && response.body() != null) {
                    if (activity instanceof CheckMembershipActivity) {
                        ((CheckMembershipActivity) activity).membershipPurchased();
                    } else if (activity instanceof MobileRechargeActivity) {
                        ((MobileRechargeActivity) activity).handlePaymentSuccessful();
                    } else if (activity instanceof PostpaidRechargeActivity) {
                        ((PostpaidRechargeActivity) activity).handlePaymentSuccessful();
                    } else if (activity instanceof GasBillActivity) {
                        ((GasBillActivity) activity).handlePaymentSuccessful();
                    } else if (activity instanceof MainActivity) {
                        ((MainActivity) activity).walletFragment.walletRechargeFragment.handlePaymentSuccessful();
                    } else if (activity instanceof WalletActivity) {
                        ((WalletActivity) activity).walletFragment.walletRechargeFragment.handlePaymentSuccessful();
                    }
                } else {
                    Toast.makeText(activity, "Payment verification failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NotNull Call<OrderDetailInfo> call, @NotNull Throwable t) {
                CommonUtils.hideProgressDialog(customProgressDialog);
                Toast.makeText(activity, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    class RazorpayPayment extends AsyncTask<JSONObject, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(JSONObject... options) {
            try {
                options[0].put("name", activity.getString(R.string.app_name));
                if (activity instanceof CheckMembershipActivity)
                    options[0].put("description", "Membership purchase");
                else
                    options[0].put("description", "Wallet recharge");
                options[0].put("currency", "INR");
                options[0].put("receipt", "");
                options[0].put("payment_capture", true);
                options[0].put("order_id", orderID);
                options[0].put("prefill.email", SPData.getAppPreferences().getEmail());
                options[0].put("prefill.contact", SPData.getAppPreferences().getMobileNumber());
                checkout.open(activity, options[0]);
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
