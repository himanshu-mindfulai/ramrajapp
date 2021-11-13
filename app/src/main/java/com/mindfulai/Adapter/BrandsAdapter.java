package com.mindfulai.Adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mindfulai.Activites.AllProductsActivity;
import com.mindfulai.Models.Brand;
import com.mindfulai.Utils.CommonUtils;
import com.mindfulai.Utils.SPData;
import com.mindfulai.ministore.R;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class BrandsAdapter extends RecyclerView.Adapter<BrandsAdapter.MyViewHolder> {


    private static final String TAG = "CategoriesAdapter";
    private Context context;
    private ArrayList<Brand> brandList;

    public BrandsAdapter(Context context, ArrayList<Brand> brandList) {
        this.context = context;
        this.brandList = brandList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.circular_category_view3, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        try {
            holder.brand_name.setText(CommonUtils.capitalizeWord(brandList.get(position).getName()));
            if (brandList.get(position).getLogo() != null)
                Glide.with(context).load(SPData.getBucketUrl() + brandList.get(position).getLogo()).into(holder.circleImageView);
            else
                holder.circleImageView.setImageDrawable(context.getDrawable(R.drawable.noimage));
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, AllProductsActivity.class);
                    intent.putExtra("brand_id", brandList.get(position).getId());
                    intent.putExtra("title", brandList.get(position).getName());
                    context.startActivity(intent);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "onBindViewHolder: " + e);
        }

    }

    @Override
    public int getItemCount() {
        try {
            return brandList.size();
        } catch (Exception e) {
            return 0;
        }
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView brand_name;
        CircleImageView circleImageView;

        MyViewHolder(View view) {
            super(view);
            brand_name = view.findViewById(R.id.sub_category_name);
            circleImageView = view.findViewById(R.id.sub_category_img);
        }
    }

}
