package com.mindfulai.Adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonObject;
import com.mindfulai.Activites.AddAddressActivity;
import com.mindfulai.Models.UserDataAddress;
import com.mindfulai.NetworkRetrofit.ApiService;
import com.mindfulai.NetworkRetrofit.ApiUtils;
import com.mindfulai.Utils.CommonUtils;
import com.mindfulai.Utils.CustomProgressDialog;
import com.mindfulai.Utils.SPData;
import com.mindfulai.ministore.R;
import com.valdesekamdem.library.mdtoast.MDToast;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import retrofit2.Call;
import retrofit2.Callback;

public class UserAddressesProfileAdapter extends RecyclerView.Adapter<UserAddressesProfileAdapter.MyViewHolder> {

    private static final String TAG = "UserAddressAdapter";
    private List<UserDataAddress> userDataAddressList;
    private Context context;
    private CustomProgressDialog customProgressDialog;
    private String message;


    public UserAddressesProfileAdapter(Context profileActivity, ArrayList<UserDataAddress> userDataAddressArrayList) {
        this.userDataAddressList = userDataAddressArrayList;
        this.context = profileActivity;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.address_view_profile, parent, false);

        return new MyViewHolder(itemView);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        try {
            holder.address_type.setText("Address " + (position + 1));
            if (userDataAddressList.get(position).getName() != null && userDataAddressList.get(position).getMobile_number() != null)
                holder.address_location.setText(userDataAddressList.get(position).getName() + "\n" + userDataAddressList.get(position).getAddressLine1() + "\n" + userDataAddressList.get(position).getAddressLine2() + ", " + userDataAddressList.get(position).getCity() + "\n" + userDataAddressList.get(position).getState() + ", " + userDataAddressList.get(position).getPincode() + "\n" + userDataAddressList.get(position).getMobile_number());
            else
                holder.address_location.setText(userDataAddressList.get(position).getAddressLine1() + "\n" + userDataAddressList.get(position).getAddressLine2() + ", " + userDataAddressList.get(position).getCity() + "\n" + userDataAddressList.get(position).getState() + ", " + userDataAddressList.get(position).getPincode());

            holder.edit_address.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, AddAddressActivity.class);
                    intent.putExtra("title", "Update Address");
                    intent.putExtra("id", userDataAddressList.get(position).get_id());
                    intent.putExtra("houseno", userDataAddressList.get(position).getAddressLine1());
                    intent.putExtra("locality", userDataAddressList.get(position).getAddressLine2());
                    intent.putExtra("city", userDataAddressList.get(position).getCity());
                    intent.putExtra("state", userDataAddressList.get(position).getState());
                    intent.putExtra("pincode", userDataAddressList.get(position).getPincode());
                    intent.putExtra("address", userDataAddressList.get(position));
                    ((Activity) context).startActivityForResult(intent, 3);
                }
            });

            holder.delete_address.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showPopup(userDataAddressList.get(holder.getAdapterPosition()).get_id(), holder);
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "onBindViewHolder: " + e);
        }

    }

    private void showPopup(final String id, final MyViewHolder holder) {
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setMessage("Are you sure you want to delete this address?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    customProgressDialog = CommonUtils.showProgressDialog(context, "Please wait.. ");
                    deleteAddress(id);
                    holder.itemView.setVisibility(View.GONE);
                    userDataAddressList.remove(holder.getAdapterPosition());
                    notifyItemRemoved(holder.getAdapterPosition());

                }).setNegativeButton("Cancel", null);

        AlertDialog alert1 = alert.create();
        alert1.show();
    }

    @Override
    public int getItemCount() {
        return userDataAddressList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView address_location;
        TextView address_type;
        TextView edit_address, delete_address;

        MyViewHolder(View view) {
            super(view);
            address_location = view.findViewById(R.id.complete_address);
            edit_address = view.findViewById(R.id.edit_address);
            delete_address = view.findViewById(R.id.delete_address);
            address_type = view.findViewById(R.id.address_type);
        }
    }
    private void deleteAddress(String id) {
        ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());
        apiService.deleteAddress(id).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NotNull Call<JsonObject> call, @NotNull retrofit2.Response<JsonObject> response) {
                CommonUtils.hideProgressDialog(customProgressDialog);
                if (response.isSuccessful() && response.body() != null && response.body().get("status").getAsInt() == 200) {
                    Toast.makeText(context, "Address Deleted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "" + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NotNull Call<JsonObject> call, @NotNull Throwable t) {

            }
        });
    }
}
