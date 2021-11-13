package com.mindfulai.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mindfulai.Models.varientsByCategory.Datum;
import com.mindfulai.Models.varientsByCategory.Varient;
import com.mindfulai.Utils.SPData;
import com.mindfulai.ministore.R;
import com.mindfulai.ministore.databinding.DiamondProductItemBinding;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class DiamondProductItemAdapter extends RecyclerView.Adapter<DiamondProductItemAdapter.DiamondProductViewHolder> {

    ArrayList<String> stringArrayList;
    Context context;
    int baseposition;
    List<Datum> varient;
    ProductsAdapter.MyViewHolder baseholder;
    ProductsAdapter productsAdapter;

    public DiamondProductItemAdapter(Context context, ArrayList<String> stringArrayList, int position, List<Datum> varient, ProductsAdapter.MyViewHolder holder, ProductsAdapter productsAdapter) {
        this.stringArrayList = stringArrayList;
        this.context = context;
        this.baseposition = position;
        this.varient = varient;
        this.baseholder = holder;
        this.productsAdapter = productsAdapter;
    }

    @NonNull
    @Override
    public DiamondProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DiamondProductViewHolder(DiamondProductItemBinding.inflate(LayoutInflater.from(context), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull DiamondProductViewHolder holder, int position) {
        if (baseposition == 0) {
            holder.binding.linearLayout.setBackground(context.getResources().getDrawable(R.drawable.rectangle_solid));
            holder.binding.productPropeties.setTextColor(context.getResources().getColor(R.color.colorWhite));
            handleBasePosition(holder, position);
        } else {
            holder.binding.linearLayout.setBackground(context.getResources().getDrawable(R.drawable.rectangle));
            holder.binding.productPropeties.setTextColor(context.getResources().getColor(R.color.colorText));
            if (position == 0) {
                holder.binding.image.setVisibility(VISIBLE);
                holder.binding.productPropeties.setVisibility(GONE);
                if (!stringArrayList.get(position).isEmpty())
                    Glide.with(context).load(SPData.getBucketUrl() + stringArrayList.get(position)).into(holder.binding.image);
                holder.binding.addToCart.setVisibility(GONE);
            } else if (position == stringArrayList.size()-1) {
                holder.binding.image.setVisibility(GONE);
                holder.binding.productPropeties.setVisibility(GONE);
                holder.binding.addToCart.setVisibility(VISIBLE);
            } else {
                handleBasePosition(holder, position);
            }
        }
        holder.binding.addToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                productsAdapter.addVairent(varient.get(baseposition-1).getVarients().get(0), baseholder, baseposition);
            }
        });
    }

    private void handleBasePosition(DiamondProductViewHolder holder, int position) {
        holder.binding.image.setVisibility(GONE);
        holder.binding.productPropeties.setVisibility(VISIBLE);
        holder.binding.productPropeties.setText(stringArrayList.get(position));
        holder.binding.addToCart.setVisibility(GONE);
    }

    @Override
    public int getItemCount() {
        return stringArrayList.size();
    }

    static class DiamondProductViewHolder extends RecyclerView.ViewHolder {
        DiamondProductItemBinding binding;

        public DiamondProductViewHolder(@NonNull DiamondProductItemBinding itemView) {
            super(itemView.getRoot());
            this.binding = itemView;
        }

    }
}
