package com.mindfulai.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mindfulai.Activites.ProductDetailsActivity;
import com.mindfulai.Models.varientsByCategory.Attribute;
import com.mindfulai.Models.varientsByCategory.Varient;
import com.mindfulai.Utils.CommonUtils;
import com.mindfulai.ministore.databinding.RadioAttributeItemBinding;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;

public class BigBasketAttributesAdapter extends RecyclerView.Adapter<BigBasketAttributesAdapter.BigBasketViewHolder> {


    private ProductDetailsActivity context;
    private List<Varient> attributeList;

    private int lastCheckedPosition = 0;
    public BigBasketAttributesAdapter(ProductDetailsActivity context, List<Varient> attributeList){
        this.context = context;
        this.attributeList = attributeList;
    }

    @NonNull
    @Override
    public BigBasketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BigBasketViewHolder(RadioAttributeItemBinding.inflate(LayoutInflater.from(context),parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull BigBasketViewHolder holder, int position) {
        if(attributeList.get(holder.getAdapterPosition()).getAttributes().size()>0){
        StringBuilder allAttribute = new StringBuilder();
        for (Attribute attribute : attributeList.get(holder.getAdapterPosition()).getAttributes()) {
            allAttribute.append(attribute.getOption().getValue()).append(",");
        };
        holder.binding.radioBtn.setText(CommonUtils.removeLastCharacter(String.valueOf(allAttribute)));
        holder.binding.radioBtn.setChecked(holder.getAdapterPosition() == lastCheckedPosition);
        holder.binding.linearLayout.setSelected(holder.getAdapterPosition() ==lastCheckedPosition);
            holder.binding.radioBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int copyOfLastCheckedPosition = lastCheckedPosition;
                    lastCheckedPosition = holder.getAdapterPosition();
                    notifyItemChanged(copyOfLastCheckedPosition);
                    notifyItemChanged(lastCheckedPosition);
                    context.productDetailsBinding.attributes.setText(holder.binding.radioBtn.getText().toString());
                    context.handleVarient(attributeList.get(holder.getAdapterPosition()));
                }
            });
        }else {
            holder.binding.getRoot().setVisibility(GONE);
        }

    }

    @Override
    public int getItemCount() {
        return attributeList.size();
    }

    class BigBasketViewHolder extends RecyclerView.ViewHolder{
        RadioAttributeItemBinding binding;

        public BigBasketViewHolder(@NonNull RadioAttributeItemBinding itemView) {
            super(itemView.getRoot());
            this.binding = itemView;
        }
    }
}
