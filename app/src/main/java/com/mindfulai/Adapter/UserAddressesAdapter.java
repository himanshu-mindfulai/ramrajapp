package com.mindfulai.Adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonObject;
import com.mindfulai.Activites.AddAddressActivity;
import com.mindfulai.Activites.CheckoutActivity;
import com.mindfulai.Activites.EditSubscriptionActivity;
import com.mindfulai.Activites.ProductDetailsActivity;
import com.mindfulai.Models.UserDataAddress;
import com.mindfulai.NetworkRetrofit.ApiService;
import com.mindfulai.NetworkRetrofit.ApiUtils;
import com.mindfulai.Utils.CommonUtils;
import com.mindfulai.Utils.CustomProgressDialog;
import com.mindfulai.Utils.SPData;
import com.mindfulai.ministore.databinding.AddressViewBinding;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;

public class UserAddressesAdapter extends RecyclerView.Adapter<UserAddressesAdapter.MyViewHolder> {

    private static final String TAG = "UserAddressAdapter";
    private final List<UserDataAddress> userDataAddressList;
    private final Context context;
    private CustomProgressDialog customProgressDialog;
    private final androidx.appcompat.app.AlertDialog alertDialog;
    private String varient_id;
    private String sub_productName;
    private String sub_productImage;
    private String sub_attributes;
    private double sub_varientprice;
    private double sub_varientsellingPrice;
    private CheckoutActivity checkoutActivity;


    public UserAddressesAdapter(Context allProductsActivity, ArrayList<UserDataAddress> userDataAddressArrayList, androidx.appcompat.app.AlertDialog alertDialog1, String varient_id, String name, String image, String attributes, double price, double sellingPrice) {
        this.userDataAddressList = userDataAddressArrayList;
        this.context = allProductsActivity;
        this.alertDialog = alertDialog1;
        this.varient_id = varient_id;
        this.sub_productName = name;
        this.sub_productImage = image;
        this.sub_attributes = attributes;
        this.sub_varientprice = price;
        this.sub_varientsellingPrice = sellingPrice;
    }

    public UserAddressesAdapter(CheckoutActivity requireActivity, ArrayList<UserDataAddress> userDataAddressArrayList, androidx.appcompat.app.AlertDialog alertDialog) {
        this.userDataAddressList = userDataAddressArrayList;
        this.context = requireActivity;
        this.alertDialog = alertDialog;
        this.checkoutActivity = requireActivity;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        return new MyViewHolder(AddressViewBinding.inflate(LayoutInflater.from(context), parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        try {
            holder.binding.addressType.setText("Address " + (position + 1));
            if (userDataAddressList.get(position).getName() != null && userDataAddressList.get(position).getMobile_number() != null)
                holder.binding.completeAddress.setText(userDataAddressList.get(position).getName() + "\n" + userDataAddressList.get(position).getAddressLine1() + "\n" + userDataAddressList.get(position).getAddressLine2() + ", " + userDataAddressList.get(position).getCity() + "\n" + userDataAddressList.get(position).getState() + ", " + userDataAddressList.get(position).getPincode() + "\n" + userDataAddressList.get(position).getMobile_number());
            else
                holder.binding.completeAddress.setText(userDataAddressList.get(position).getAddressLine1() + "\n" + userDataAddressList.get(position).getAddressLine2() + ", " + userDataAddressList.get(position).getCity() + "\n" + userDataAddressList.get(position).getState() + ", " + userDataAddressList.get(position).getPincode());

            holder.binding.deliverHere.setText("Select");
            if (checkoutActivity != null) {
                holder.binding.edit.setVisibility(View.GONE);
                holder.binding.delete.setVisibility(View.GONE);
            }

            holder.binding.edit.setOnClickListener(v -> {
                alertDialog.dismiss();
                Intent intent = new Intent(context, AddAddressActivity.class);
                intent.putExtra("title", "Update Address");
                intent.putExtra("id", userDataAddressList.get(position).get_id());
                intent.putExtra("houseno", userDataAddressList.get(position).getAddressLine1());
                intent.putExtra("locality", userDataAddressList.get(position).getAddressLine2());
                intent.putExtra("city", userDataAddressList.get(position).getCity());
                intent.putExtra("state", userDataAddressList.get(position).getState());
                intent.putExtra("pincode", userDataAddressList.get(position).getPincode());
                intent.putExtra("address", userDataAddressList.get(position));
                context.startActivity(intent);
            });

            holder.binding.deliverHere.setOnClickListener(v -> {
                alertDialog.dismiss();
                try {
                    if (checkoutActivity == null) {
                        Intent intent = new Intent(context, EditSubscriptionActivity.class);
                        intent.putExtra("title", "Add Subscription");
                        intent.putExtra("varient_id", varient_id);
                        intent.putExtra("address_id", userDataAddressList.get(position).get_id());
                        intent.putExtra("name", sub_productName);
                        if (sub_productImage != null)
                            intent.putExtra("image", sub_productImage);
                        intent.putExtra("price", sub_varientprice);
                        intent.putExtra("sellingPrice", sub_varientsellingPrice);
                        if (context instanceof ProductDetailsActivity)
                            intent.putExtra("attributes", sub_attributes.substring(0, sub_attributes.length() - 2));
                        else
                            intent.putExtra("attributes", sub_attributes);
                        context.startActivity(intent);
                    }else{
                        checkoutActivity.pickupCenterAddressId = userDataAddressList.get(position).get_id();
                        checkoutActivity.checkPaymentMethod();
                    }
                } catch (Exception e) {
                    Toast.makeText(context, "" + e, Toast.LENGTH_SHORT).show();
                }
            });

            holder.binding.delete.setOnClickListener(v -> showPopup(userDataAddressList.get(position).get_id(), holder));
        } catch (Exception e) {
            Log.e(TAG, "onBindViewHolder: " + e);
        }

    }


    private void showPopup(final String id, final MyViewHolder holder) {
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setMessage("Are you sure you want to delete this address")
                .setPositiveButton("Yes", (dialog, which) -> {
                    customProgressDialog = CommonUtils.showProgressDialog(context, "Please wait.. ");
                    deleteAddress(id, holder);
                }).setNegativeButton("Cancel", null);

        AlertDialog alert1 = alert.create();
        alert1.show();
    }

    private void deleteAddress(String id, MyViewHolder holder) {
        ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());
        apiService.deleteAddress(id).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NotNull Call<JsonObject> call, @NotNull retrofit2.Response<JsonObject> response) {
                CommonUtils.hideProgressDialog(customProgressDialog);
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(context, "" + response.body().get("message").getAsString(), Toast.LENGTH_SHORT).show();
                    holder.itemView.setVisibility(View.GONE);
                    userDataAddressList.remove(holder.getAdapterPosition());
                    notifyItemRemoved(holder.getAdapterPosition());
                } else {
                    Toast.makeText(context, "" + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NotNull Call<JsonObject> call, @NotNull Throwable t) {
                CommonUtils.hideProgressDialog(customProgressDialog);
                Toast.makeText(context, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return userDataAddressList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        AddressViewBinding binding;

        MyViewHolder(AddressViewBinding view) {
            super(view.getRoot());
            binding = view;
        }
    }

}
