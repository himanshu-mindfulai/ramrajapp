package com.mindfulai.Adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.mindfulai.Activites.CheckMembershipActivity;
import com.mindfulai.Activites.LoginActivity;
import com.mindfulai.Activites.MainActivity;
import com.mindfulai.Models.membership.GetMembershipResponse;
import com.mindfulai.Models.membership.MembershipDataModel;
import com.mindfulai.Utils.CommonUtils;
import com.mindfulai.Utils.CustomTag;
import com.mindfulai.Utils.SPData;
import com.mindfulai.ministore.R;
import com.mindfulai.ministore.databinding.ActivityPurchasedMembershipBinding;
import com.mindfulai.ministore.databinding.MembershipItemPlanBinding;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class MembershipAdapter extends RecyclerView.Adapter<MembershipAdapter.MembershipHolder> {

    private final CheckMembershipActivity context;
    private final ArrayList<MembershipDataModel> membershipResponses;
    private final Handler handler;

    public MembershipAdapter(CheckMembershipActivity context, ArrayList<MembershipDataModel> membershipResponses) {
        this.context = context;
        this.membershipResponses = membershipResponses;
        handler = new Handler(Looper.getMainLooper());
    }

    @NonNull
    @NotNull
    @Override
    public MembershipHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        return new MembershipHolder(MembershipItemPlanBinding.inflate(LayoutInflater.from(context), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull MembershipHolder holder, int pos) {
        int position = holder.getAdapterPosition();
        holder.binding.month.setText(membershipResponses.get(position).getExpiry() + " days");
        holder.binding.price.setText(context.getString(R.string.rs) + CommonUtils.getTwoDigitsRoundOffValue(membershipResponses.get(position).getPrice()));

        if(CommonUtils.stringIsNotNullAndEmpty(membershipResponses.get(position).getDetails())){
            holder.binding.details.setVisibility(View.VISIBLE);
            holder.binding.details.setText(HtmlCompat.fromHtml(membershipResponses.get(position).getDetails(), HtmlCompat.FROM_HTML_SEPARATOR_LINE_BREAK_PARAGRAPH));
        }else{
            holder.binding.details.setVisibility(View.GONE);
        }
        holder.binding.select.setOnClickListener(v -> {
            holder.binding.select.setEnabled(false);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //Do something after 100ms
                    holder.binding.select.setEnabled(true);
                }
            }, 2000);
            if (!SPData.getAppPreferences().isMembershipPurchased()) {
                if (!SPData.getAppPreferences().getUsertoken().isEmpty())
                    context.purchaseMembership(membershipResponses.get(position).get_id());
                else {
                    Toast.makeText(context, "Login to purchase membership", Toast.LENGTH_SHORT).show();
                    context.startActivity(new Intent(context, LoginActivity.class));
                }
            } else {
                Toast.makeText(context, "You have already purchased a membership", Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    public int getItemCount() {
        return membershipResponses.size();
    }

    static class MembershipHolder extends RecyclerView.ViewHolder {
        MembershipItemPlanBinding binding;


        public MembershipHolder(@NonNull @NotNull MembershipItemPlanBinding itemView) {
            super(itemView.getRoot());
            this.binding = itemView;
        }
    }
}
