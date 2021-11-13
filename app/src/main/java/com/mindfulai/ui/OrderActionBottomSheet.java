package com.mindfulai.ui;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mindfulai.Activites.OrderHistoryDetailsActivity;
import com.mindfulai.Activites.ProfileActivity;
import com.mindfulai.Adapter.OrderHistoryDetailsAdapter;
import com.mindfulai.Models.AllOrderHistory.DatumModel;
import com.mindfulai.Models.AllOrderHistory.Product;
import com.mindfulai.NetworkRetrofit.ApiService;
import com.mindfulai.NetworkRetrofit.ApiUtils;
import com.mindfulai.Utils.CommonUtils;
import com.mindfulai.Utils.CustomProgressDialog;
import com.mindfulai.Utils.SPData;
import com.mindfulai.ministore.R;
import com.valdesekamdem.library.mdtoast.MDToast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderActionBottomSheet extends BottomSheetDialogFragment {
    private Context context;
    private String orderId;
    private OrderHistoryDetailsAdapter.MyViewHolder myViewHolder;
    private Product product;
    private String orderStatus;
    private ImageView more;
    private OrderHistoryDetailsAdapter orderHistoryDetailsAdapter;
    private int position;
    private float ratingvalue;

    public OrderActionBottomSheet(Context context, String orderId, OrderHistoryDetailsAdapter.MyViewHolder myViewHolder, Product product, String orderStatus, ImageView moreItem, OrderHistoryDetailsAdapter orderHistoryDetailsAdapter, int position) {
        this.context = context;
        this.orderId = orderId;
        this.myViewHolder = myViewHolder;
        this.product = product;
        this.orderStatus = orderStatus;
        this.more = moreItem;
        this.orderHistoryDetailsAdapter = orderHistoryDetailsAdapter;
        this.position = position;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.order_action_layout, container, false);
        TextView orderReturn = view.findViewById(R.id.return_item);
        TextView orderReplace = view.findViewById(R.id.replace_item);
        TextView orderCancel = view.findViewById(R.id.cancel_item);

        if (orderStatus.equals("Delivered") || orderStatus.equals("Cancelled") || (SPData.allowOrderCancelBefore() &&  SPData.orderCancelTime() < ((OrderHistoryDetailsActivity) context).diffHours)) {
            orderCancel.setVisibility(View.GONE);
        } else {
            orderCancel.setVisibility(View.VISIBLE);
        }
        if (SPData.allowReturn() && orderStatus.equals("Delivered")) {
            orderReturn.setVisibility(View.VISIBLE);
        } else {
            orderReturn.setVisibility(View.GONE);
        }
        if (SPData.allowReplace() && orderStatus.equals("Delivered")) {
            orderReplace.setVisibility(View.VISIBLE);
        } else {
            orderReplace.setVisibility(View.GONE);
        }

        orderReplace.setOnClickListener(view13 -> {
            if (product.getProduct().getProduct().getReturnable())
                showDialog("replace");
            else
                Toast.makeText(getActivity(), "This product can't be replace", Toast.LENGTH_SHORT).show();
        });
        orderReturn.setOnClickListener(view12 -> {
            if (product.getProduct().getProduct().getReturnable())
                showDialog("return");
            else
                Toast.makeText(getActivity(), "This product can't be return", Toast.LENGTH_SHORT).show();
        });
        orderCancel.setOnClickListener(view1 -> showDialog("cancel"));
        return view;
    }

    private void askForNote(String action) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.order_action_note, null);
        TextView textViewReason = view.findViewById(R.id.text_reason);
        TextView textViewLetusknow = view.findViewById(R.id.letusKnow);
        textViewReason.setText("Reason for " + action);
        textViewLetusknow.setText("Let us know why you want to " + action);
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
                String note = editText.getText().toString();
                if (!note.replaceAll(" ", "").isEmpty()) {
                    alertDialog.cancel();
                    changeStatus(action, note);
                } else {
                    Toast.makeText(context, "Please enter a note", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showDialog(String action) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Are you sure you want to " + action + " this item");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
                askForNote(action);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.create().show();
    }

    private void changeStatus(String action, String note) {
        final CustomProgressDialog customProgressDialog = CommonUtils.showProgressDialog(context,
                "Please wait...");

        ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("action", action);
        jsonObject.addProperty("order", orderId);
        jsonObject.addProperty("note", note);
        JsonArray products = new JsonArray();
        products.add(product.getProduct().getId());
        jsonObject.add("products", products);
        apiService.orderAction(jsonObject).enqueue(new Callback<DatumModel>() {
            @Override
            public void onResponse(Call<DatumModel> call, Response<DatumModel> response) {
                CommonUtils.hideProgressDialog(customProgressDialog);
                if (response.isSuccessful()) {
                    myViewHolder.binding.cvOrderItem.setEnabled(false);
                    more.setVisibility(View.GONE);
                    myViewHolder.binding.cvOrderItem.setBackgroundColor(context.getResources().getColor(R.color.colorAdd));
                    if (action.equals("cancel")) {
                        myViewHolder.binding.status.setText("Cancelled");
                        product.setStatus("Cancelled");
                        ((OrderHistoryDetailsActivity) context).productOrderStatus = "Cancelled";
                        Toast.makeText(context, "Item cancel request send", Toast.LENGTH_SHORT).show();
                    } else if (action.equals("return")) {
                        myViewHolder.binding.status.setText("Returned");
                        product.setStatus("Returned");
                        ((OrderHistoryDetailsActivity) context).productOrderStatus = "Returned";
                        Toast.makeText(context, "Item returned request send", Toast.LENGTH_SHORT).show();
                    } else {
                        myViewHolder.binding.status.setText("Replaced");
                        product.setStatus("Replaced");
                        ((OrderHistoryDetailsActivity) context).productOrderStatus = "Replaced";
                        Toast.makeText(context, "Item replaced request send", Toast.LENGTH_SHORT).show();
                    }
                    ((OrderHistoryDetailsActivity) context).product_position = position;
                    orderHistoryDetailsAdapter.notifyItemChanged(position);
                    myViewHolder.binding.status.setVisibility(View.VISIBLE);
                } else
                    Toast.makeText(context, "" + response.body().getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<DatumModel> call, Throwable t) {
                CommonUtils.hideProgressDialog(customProgressDialog);
                Log.e("TAG", "onFailure: " + t.getMessage());
                Toast.makeText(context, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
