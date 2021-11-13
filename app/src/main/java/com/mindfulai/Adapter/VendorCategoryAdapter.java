package com.mindfulai.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.mindfulai.Models.categoryData.Datum;
import com.mindfulai.Utils.CommonUtils;
import com.mindfulai.Utils.SPData;
import com.mindfulai.ministore.R;
import com.mindfulai.Activites.ProductsByVendorActivity;
import com.mindfulai.ministore.databinding.CategoryByvendorItemBinding;
import java.util.List;

public class VendorCategoryAdapter extends RecyclerView.Adapter<VendorCategoryAdapter.VendorCategoryViewHolder> {

    private Context context;
    private List<Datum> categoryList;
    private int selectedPosition = -1;
    private RecyclerView mRecyclerView;

    public VendorCategoryAdapter(Context context, List<Datum> categoryList, RecyclerView recyclerView) {
        this.categoryList = categoryList;
        this.context = context;
        this.mRecyclerView = recyclerView;
    }


    @NonNull
    @Override
    public VendorCategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VendorCategoryViewHolder(CategoryByvendorItemBinding.inflate(LayoutInflater.from(context), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VendorCategoryViewHolder holder, int position) {
        holder.binding.categoryName.setText(CommonUtils.capitalizeWord(categoryList.get(holder.getAdapterPosition()).getName()));
        if (CommonUtils.stringIsNotNullAndEmpty(categoryList.get(holder.getAdapterPosition()).getImage())) {
            Glide.with(context).load(SPData.getBucketUrl() + categoryList.get(holder.getAdapterPosition()).getImage()).into(holder.binding.categoryImg);
        }
        if (categoryList.get(holder.getAdapterPosition()).isSelected()) {
            holder.binding.categoryName.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
            holder.binding.categoryName.setSingleLine(false);
            holder.binding.categoryName.setSelected(true);
        } else {
            holder.binding.categoryName.setTextColor(context.getResources().getColor(R.color.colorBlack));
            holder.binding.categoryName.setSingleLine(true);
            holder.binding.categoryName.setSelected(false);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (context instanceof ProductsByVendorActivity) {
                    ((ProductsByVendorActivity) context).currentCategoryIndex = position;
                }
                checkCategory(holder.getAdapterPosition());
            }
        });
    }

    public void checkCategory(int adapterPosition) {
        if (selectedPosition != adapterPosition) {
            if (selectedPosition != -1) {
                categoryList.get(selectedPosition).setSelected(false);
                notifyItemChanged(selectedPosition);
            }
            selectedPosition = adapterPosition;
            categoryList.get(selectedPosition).setSelected(true);
            notifyItemChanged(selectedPosition);
            ((ProductsByVendorActivity) context).categoryId = categoryList.get(adapterPosition).getId();
            ((ProductsByVendorActivity) context).getProductByCategoryAndVendor();
        }
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }


    static class VendorCategoryViewHolder extends RecyclerView.ViewHolder {
        CategoryByvendorItemBinding binding;

        VendorCategoryViewHolder(CategoryByvendorItemBinding itemBinding) {
            super(itemBinding.getRoot());
            this.binding = itemBinding;
        }
    }
}
