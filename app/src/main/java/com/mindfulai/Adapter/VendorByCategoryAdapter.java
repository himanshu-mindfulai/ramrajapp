package com.mindfulai.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mindfulai.Activites.ProductsByVendorActivity;
import com.mindfulai.Activites.VendorByCategoryActivity;
import com.mindfulai.Models.VendorChild;
import com.mindfulai.Utils.CommonUtils;
import com.mindfulai.Utils.SPData;
import com.mindfulai.ministore.R;
import com.mindfulai.ministore.databinding.VendorProfileViewBinding;

import java.util.ArrayList;

public class VendorByCategoryAdapter extends RecyclerView.Adapter<VendorByCategoryAdapter.MyViewHolder> {
    private final VendorByCategoryActivity context;
    private final ArrayList<VendorChild> vendorChildList;
    private final ArrayList<VendorChild> allVendorsList;

    public VendorByCategoryAdapter(VendorByCategoryActivity context, ArrayList<VendorChild> vendorChildList) {
        this.context = context;
        this.vendorChildList = vendorChildList;
        this.allVendorsList = new ArrayList<>();
        allVendorsList.addAll(vendorChildList);
    }

    @NonNull
    @Override
    public VendorByCategoryAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(VendorProfileViewBinding.inflate(LayoutInflater.from(context), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VendorByCategoryAdapter.MyViewHolder holder, final int position) {
        if (vendorChildList.get(position).isActive()) {
            holder.binding.rl.setAlpha(1f);
            holder.binding.inactiveMsg.setVisibility(View.GONE);
        } else {
            holder.binding.rl.setAlpha(0.5f);
            holder.binding.inactiveMsg.setVisibility(View.VISIBLE);
        }
        holder.binding.vendorName.setText(CommonUtils.capitalizeWord(vendorChildList.get(position).getFull_name()));

        if (CommonUtils.stringIsNotNullAndEmpty(vendorChildList.get(position).getProfile_picture())) {
            Glide.with(context).load(SPData.getBucketUrl() + vendorChildList.get(position).getProfile_picture()).into(holder.binding.vendorImage);
        } else {
            Glide.with(context).load(context.getResources().getDrawable(R.drawable.noimage)).into(holder.binding.vendorImage);
        }

        if (vendorChildList.get(position).getAddress() != null) {
            holder.binding.vendorAddress.setText(vendorChildList.get(position).getAddress());
        }

        if (vendorChildList.get(position).getEmail() != null) {
            holder.binding.vendorEmail.setText(vendorChildList.get(position).getEmail());
        }

        if (vendorChildList.get(position).getMobile_number() != null) {
            holder.binding.vendorPhone.setText(vendorChildList.get(position).getMobile_number());
        }

        holder.itemView.setOnClickListener(v -> {
            if (vendorChildList.get(position).isActive()) {
                Intent intent = new Intent(context, ProductsByVendorActivity.class);
                intent.putExtra("vendor_id", vendorChildList.get(position).get_id());
                intent.putExtra("vendor_name", vendorChildList.get(position).getFull_name());
                intent.putExtra("vendor_address",vendorChildList.get(position).getAddress());
                intent.putExtra("vendor", true);
                context.startActivity(intent);
            }
        });
    }

    public void filterByName(String name) {
        vendorChildList.clear();
        if (CommonUtils.stringIsNotNullAndEmpty(name))
            for (VendorChild vendorChild : allVendorsList) {
                if (vendorChild.getFull_name().toLowerCase().contains(name)) {
                    vendorChildList.add(vendorChild);
                }
            }
        else {
            vendorChildList.addAll(allVendorsList);
        }
        context.binding.noVendors.setText(vendorChildList.size()+" Vendors");
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return vendorChildList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private final VendorProfileViewBinding binding;

        MyViewHolder(VendorProfileViewBinding vendorProfileViewBinding) {
            super(vendorProfileViewBinding.getRoot());
            this.binding = vendorProfileViewBinding;
        }
    }

}
