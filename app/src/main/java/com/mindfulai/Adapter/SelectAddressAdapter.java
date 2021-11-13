package com.mindfulai.Adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mindfulai.Activites.AddAddressActivity;
import com.mindfulai.Activites.AddressSelectorActivity;
import com.mindfulai.Activites.CheckoutActivity;
import com.mindfulai.Models.UserDataAddress;
import com.mindfulai.Utils.CommonUtils;
import com.mindfulai.Utils.SPData;
import com.mindfulai.ministore.R;

import java.util.List;

public class SelectAddressAdapter extends RecyclerView.Adapter<SelectAddressAdapter.ViewHolder> {

    private final Context context;
    private final List<UserDataAddress> userDataAddressList;
    private final Activity activity;

    public SelectAddressAdapter(Context context, List<UserDataAddress> data, Activity activity) {
        this.context = context;
        this.userDataAddressList = data;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.select_address_item_layout, parent, false);

        return new ViewHolder(itemView);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.address_type.setText("Address " + (position + 1));
        String comma = ", ";
        String society = userDataAddressList.get(position).getSociety();
        if (!CommonUtils.stringIsNotNullAndEmpty(userDataAddressList.get(position).getSociety())) {
            comma = "";
            society = "";
        }
        String address = userDataAddressList.get(position).getAddressLine1() + "\n" + userDataAddressList.get(position).getAddressLine2() + comma + society + "\n" + userDataAddressList.get(position).getCity() + ", " + userDataAddressList.get(position).getState() + "\n" + userDataAddressList.get(position).getPincode();
        userDataAddressList.get(position).setShippingAddress(address);
        if (userDataAddressList.get(position).getName() != null && userDataAddressList.get(position).getMobile_number() != null)
            holder.address_location.setText(userDataAddressList.get(position).getName() + " " + userDataAddressList.get(position).getMobile_number() + "\n" + address);
        else
            holder.address_location.setText(address);
        holder.edit_address.setOnClickListener(v -> {
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
        });
        holder.address_select.setOnClickListener(v -> {

            Intent intent = new Intent();

            SPData.getAppPreferences().setPincode("" + userDataAddressList.get(position).getPincode());
            SPData.getAppPreferences().setAddressId(userDataAddressList.get(position).get_id());

            if (userDataAddressList.get(position).getLocationModel() != null)
                SPData.getAppPreferences().setUserShippingCoordinates(userDataAddressList.get(position).getLocationModel().getCoordinates()[0] + "," + userDataAddressList.get(position).getLocationModel().getCoordinates()[1]);

            SPData.getAppPreferences().setAddress(userDataAddressList.get(position).getAddressLine2() + ", " + userDataAddressList.get(position).getPincode());
            if (userDataAddressList.get(position).getName() != null)
                SPData.getAppPreferences().setUserShippingName(userDataAddressList.get(position).getName());
            if (userDataAddressList.get(position).getMobile_number() != null)
                SPData.getAppPreferences().setUserShippingMobile(userDataAddressList.get(position).getMobile_number());
            SPData.getAppPreferences().setUserShippingAddress(userDataAddressList.get(position).getShippingAddress());
            intent.putExtra("pincode", "" + userDataAddressList.get(position).getPincode());
            intent.putExtra("address", userDataAddressList.get(position).getAddressLine1() + " " + userDataAddressList.get(position).getAddressLine2());
            if (context instanceof AddressSelectorActivity) {
                if (((AddressSelectorActivity) context).getIntent().getBooleanExtra("cartPage", false)) {
                    context.startActivity(new Intent(context, CheckoutActivity.class));
                } else {
                    activity.setResult(Activity.RESULT_OK, intent);
                }
            } else {
                activity.setResult(Activity.RESULT_OK, intent);
            }
            activity.finish();
        });
    }

    @Override
    public int getItemCount() {
        return userDataAddressList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView address_location;
        TextView address_type;
        TextView address_select;
        TextView edit_address;

        public ViewHolder(@NonNull View view) {
            super(view);
            edit_address = view.findViewById(R.id.edit_address);
            address_location = view.findViewById(R.id.complete_address);
            address_type = view.findViewById(R.id.address_type);
            address_select = view.findViewById(R.id.select);
        }
    }

}
