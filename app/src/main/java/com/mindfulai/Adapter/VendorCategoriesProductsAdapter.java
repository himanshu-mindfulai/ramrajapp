package com.mindfulai.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.mindfulai.Models.VendorProductsByCategory;
import com.mindfulai.Utils.CommonUtils;
import com.mindfulai.ministore.databinding.VendorCategoryProductItemBinding;
import java.util.ArrayList;

public class VendorCategoriesProductsAdapter extends RecyclerView.Adapter<VendorCategoriesProductsAdapter.ViewHolder> {
    private final ArrayList<VendorProductsByCategory> vendorProductsByCategoryArrayList;
    private final Context context;
    ProductsAdapter productsAdapter;

    public VendorCategoriesProductsAdapter(Context context, ArrayList<VendorProductsByCategory> browseServiceModels) {
        this.vendorProductsByCategoryArrayList = browseServiceModels;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(VendorCategoryProductItemBinding.inflate(LayoutInflater.from(context), parent, false));
    }

    @SuppressLint("CheckResult")
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        try {
            holder.binding.title.setText(CommonUtils.capitalizeWord(vendorProductsByCategoryArrayList.get(position).getTitle()));
            holder.binding.title.setSelected(vendorProductsByCategoryArrayList.get(position).isSelected());
            productsAdapter = new ProductsAdapter(context, vendorProductsByCategoryArrayList.get(position).getProducts(), "list", CommonUtils.PRODUCTS_BY_VENDOR_REQUEST_CODE);
            LinearLayoutManager manager = new LinearLayoutManager(context);
            holder.binding.recyclerViewProducts.setLayoutManager(manager);
            holder.binding.recyclerViewProducts.setAdapter(productsAdapter);
            holder.binding.title.setOnClickListener(v -> {
                if (vendorProductsByCategoryArrayList.get(position).isSelected()) {
                    vendorProductsByCategoryArrayList.get(position).setSelected(false);
                    holder.binding.title.setSelected(false);
                    holder.binding.recyclerViewProducts.setVisibility(View.GONE);
                } else {
                    vendorProductsByCategoryArrayList.get(position).setSelected(true);
                    holder.binding.title.setSelected(true);
                    holder.binding.recyclerViewProducts.setVisibility(View.VISIBLE);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("TAG", "onBindViewHolder: " + e);
            Toast.makeText(context, "" + e, Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public int getItemCount() {
        return vendorProductsByCategoryArrayList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        VendorCategoryProductItemBinding binding;

        ViewHolder(@NonNull VendorCategoryProductItemBinding itemView) {
            super(itemView.getRoot());
            this.binding = itemView;
        }
    }
}