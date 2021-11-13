package com.mindfulai.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.mindfulai.Models.membership.GetMembershipResponse;
import com.mindfulai.Utils.CommonUtils;
import com.mindfulai.ministore.R;
import com.mindfulai.ministore.databinding.PurchasedMembershipItemViewBinding;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class PurchasedMembershipAdapter extends RecyclerView.Adapter<PurchasedMembershipAdapter.PurchasedHolder> {

    private Context context;
    private ArrayList<GetMembershipResponse> membershipResponses;

    public PurchasedMembershipAdapter(Context context,ArrayList<GetMembershipResponse> responses){
        this.context = context;
        this.membershipResponses = responses;
    }
    @NonNull
    @NotNull
    @Override
    public PurchasedHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        return new PurchasedHolder(PurchasedMembershipItemViewBinding.inflate(LayoutInflater.from(context), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull PurchasedHolder holder, int position) {
        holder.binding.amountPaid.setText(context.getString(R.string.rs)+membershipResponses.get(position).getPrice());
        holder.binding.name.setText(membershipResponses.get(position).getName());
        Date expirydate = CommonUtils.gmttoLocalDate(CommonUtils.stringDateToGmt(membershipResponses.get(position).getExpiredAt()));
        Date purchasedon = CommonUtils.gmttoLocalDate(CommonUtils.stringDateToGmt(membershipResponses.get(position).getPurchasedOn()));
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, dd MMM YYYY");
        holder.binding.expireOn.setText("Expire on "+simpleDateFormat.format(expirydate));
        holder.binding.purchasedOn.setText("Purchase on "+simpleDateFormat.format(purchasedon));

    }

    @Override
    public int getItemCount() {
        return membershipResponses.size();
    }

    static class PurchasedHolder extends RecyclerView.ViewHolder {
        private PurchasedMembershipItemViewBinding binding;

        public PurchasedHolder(@NonNull @NotNull PurchasedMembershipItemViewBinding itemView) {
            super(itemView.getRoot());
            binding= itemView;
        }
    }
}
