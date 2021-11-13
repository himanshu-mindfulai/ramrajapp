package com.mindfulai.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mindfulai.Models.payments.PaymentRecords;
import com.mindfulai.Utils.CommonUtils;
import com.mindfulai.Utils.SPData;
import com.mindfulai.ministore.R;
import com.mindfulai.ministore.databinding.PaymentItemViewBinding;
import com.mindfulai.ui.WalletTransactionFragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.mindfulai.Utils.CommonUtils.gmttoLocalDate;

public class PaymentAdapter extends RecyclerView.Adapter<PaymentAdapter.PaymentViewHolder> {

    private final Context context;
    private final ArrayList<PaymentRecords> paymentRecords;
    private final String refpurpose;
    WalletTransactionFragment walletTransactionFragment;

    public PaymentAdapter(Context context, WalletTransactionFragment walletTransactionFragment) {
        this.context = context;
        this.walletTransactionFragment = walletTransactionFragment;
        this.paymentRecords = walletTransactionFragment.paymentRecords;
        this.refpurpose = walletTransactionFragment.purpose;
    }

    @NonNull
    @Override
    public PaymentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PaymentViewHolder(PaymentItemViewBinding.inflate(LayoutInflater.from(context), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentViewHolder holder, int position) {

        String purpose = paymentRecords.get(position).getPurpose();
        String amntPaid = context.getString(R.string.rs);
        if (purpose.equals("order") || purpose.equals("wallet_deduction") || purpose.equals("membership_purchased")) {
            if (purpose.equals("membership_purchased")) {
                amntPaid = amntPaid + (paymentRecords.get(position).getPaidFromWallet() + paymentRecords.get(position).getAmountPaid());
            } else if (purpose.equals("order"))
                amntPaid = amntPaid + paymentRecords.get(position).getPaidFromWallet();
            else
                amntPaid = amntPaid + paymentRecords.get(position).getAmountPaid();

            holder.binding.amountPaid.setText("- " + amntPaid);
            holder.binding.amountPaid.setTextColor(context.getResources().getColor(R.color.colorError));
            holder.binding.remarks.setVisibility(View.GONE);
        } else if (purpose.equals("other_transaction")) {
            amntPaid = amntPaid + paymentRecords.get(position).getAmountPaid();
            if (paymentRecords.get(position).getType().equalsIgnoreCase("credit")) {
                holder.binding.amountPaid.setText("+ " + amntPaid);
                holder.binding.amountPaid.setTextColor(context.getResources().getColor(R.color.colorGreen));
            } else {
                holder.binding.amountPaid.setText("- " + amntPaid);
                holder.binding.amountPaid.setTextColor(context.getResources().getColor(R.color.colorError));
            }
            if(paymentRecords.get(position).getRemarks()!=null){
                holder.binding.remarks.setText(paymentRecords.get(position).getRemarks());
                holder.binding.remarks.setVisibility(View.VISIBLE);
            }else
                holder.binding.remarks.setVisibility(View.GONE);
        } else {
            amntPaid = amntPaid + paymentRecords.get(position).getAmountPaid();
            holder.binding.amountPaid.setText("+ " + amntPaid);
            holder.binding.amountPaid.setTextColor(context.getResources().getColor(R.color.colorGreen));
            holder.binding.remarks.setVisibility(View.GONE);
        }
        if(refpurpose.isEmpty()) {
            holder.binding.paymentType.setText(CommonUtils.capitalizeWord(purpose.replace("_", " ")));
        }else if(walletTransactionFragment.type.isEmpty()){
            holder.binding.paymentType.setText(SPData.buddyCreditTitle());
        }else {
            holder.binding.paymentType.setText(SPData.signUpBonusReferralTitle());
        }
        String date = paymentRecords.get(position).getAt();
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        try {
            Date date_original = gmttoLocalDate(iso.parse(date));
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, dd MMM YYYY");
            String orderDate = simpleDateFormat.format(date_original);
            holder.binding.on.setText("On " + orderDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return paymentRecords.size();
    }

    static class PaymentViewHolder extends RecyclerView.ViewHolder {
        PaymentItemViewBinding binding;

        public PaymentViewHolder(@NonNull PaymentItemViewBinding itemView) {
            super(itemView.getRoot());
            binding = itemView;
        }
    }
}
