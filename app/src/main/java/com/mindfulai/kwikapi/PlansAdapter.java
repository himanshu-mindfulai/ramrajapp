package com.mindfulai.kwikapi;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.mindfulai.Models.kwikapimodels.PlanItem;
import com.mindfulai.ministore.R;
import com.mindfulai.ministore.databinding.PlanItemViewBinding;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;

public class PlansAdapter extends RecyclerView.Adapter<PlansAdapter.PlanHolder>{


    Context context;
    ArrayList<PlanItem> planItems;
    public PlansAdapter(Context context,ArrayList<PlanItem> planItems){
        this.context =context;
        this.planItems = planItems;
    }


    @NonNull
    @NotNull
    @Override
    public PlanHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        return new PlanHolder(PlanItemViewBinding.inflate(LayoutInflater.from(context),parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull PlanHolder holder, int position) {
        holder.binding.desc.setText(planItems.get(position).getDesc());
        holder.binding.type.setText(planItems.get(position).getType());
        holder.binding.rs.setText(context.getString(R.string.rs)+planItems.get(position).getRs());
        holder.binding.validity.setText("Validity "+planItems.get(position).getValidity());
        holder.binding.rs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ( (MobileRechargeActivity)context).binding.nestedScrollView.scrollTo(0,0);
                ( (MobileRechargeActivity)context).binding.etAmount.setText(""+planItems.get(position).getRs());

            }
        });
    }

    @Override
    public int getItemCount() {
        return planItems.size();
    }

    class PlanHolder extends RecyclerView.ViewHolder{
        PlanItemViewBinding binding;
        public PlanHolder(@NonNull @NotNull PlanItemViewBinding itemView) {
            super(itemView.getRoot());
            this.binding = itemView;
        }
    }
}
