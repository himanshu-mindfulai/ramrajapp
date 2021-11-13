package com.mindfulai.Adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mindfulai.Activites.PromoCodeActivity;
import com.mindfulai.Models.coupon.CouponDataModel;
import com.mindfulai.ministore.R;
import com.mindfulai.ministore.databinding.CouponItemViewBinding;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import static com.mindfulai.Utils.CommonUtils.gmttoLocalDate;

public class AllCouponsAdapter extends RecyclerView.Adapter<AllCouponsAdapter.CouponViewHolder> {
    private PromoCodeActivity context;
    private ArrayList<CouponDataModel> couponDataModelArrayList;
    private final SimpleDateFormat iso;
    private final SimpleDateFormat simpleDateFormat;


    public AllCouponsAdapter(PromoCodeActivity context, ArrayList<CouponDataModel> couponDataModels) {
        this.context = context;
        this.couponDataModelArrayList = couponDataModels;
        iso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        simpleDateFormat = new SimpleDateFormat("EEE, dd MMM YYYY");
    }

    @NonNull
    @Override
    public CouponViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CouponViewHolder(CouponItemViewBinding.inflate(LayoutInflater.from(context),parent,false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull CouponViewHolder holder, int position) {
        holder.binding.name.setText(couponDataModelArrayList.get(position).getName());
        holder.binding.code.setText(couponDataModelArrayList.get(position).getCode().toUpperCase());
        try {
            String validFromDate = couponDataModelArrayList.get(position).getValidFrom();
            String from = simpleDateFormat.format(gmttoLocalDate(Objects.requireNonNull(iso.parse(validFromDate))));

            String validToDate = couponDataModelArrayList.get(position).getValidUpto();
            String to = simpleDateFormat.format(gmttoLocalDate(Objects.requireNonNull(iso.parse(validToDate))));

            holder.binding.validDate.setText("Valid from " + from + " to " + to);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (couponDataModelArrayList.get(position).getType().equals("percent"))
            holder.binding.value.setText(couponDataModelArrayList.get(position).getValue() + "% OFF");
        else
            holder.binding.value.setText("Rs." + couponDataModelArrayList.get(position).getValue() + " OFF");
        if (couponDataModelArrayList.get(position).getMinCartValue() > context.subtotal) {
            holder.binding.validAmount.setText("Valid on minimum order value of Rs." + couponDataModelArrayList.get(position).getMinCartValue() + " and above");
            holder.binding.validAmount.setVisibility(View.VISIBLE);
            holder.binding.apply.setVisibility(View.GONE);
        } else {
            holder.binding.validAmount.setVisibility(View.GONE);
            holder.binding.apply.setVisibility(View.VISIBLE);
        }
        holder.binding.apply.setOnClickListener(view -> {
            context.binding.tilCoupon.getEditText().setText(couponDataModelArrayList.get(position).getCode());
            context.applyCoupon();
        });
    }

    @Override
    public int getItemCount() {
        return couponDataModelArrayList.size();
    }

    static class CouponViewHolder extends RecyclerView.ViewHolder {
        CouponItemViewBinding binding;
        public CouponViewHolder(@NonNull CouponItemViewBinding itemView) {
            super(itemView.getRoot());
            this.binding = itemView;
        }
    }
}
