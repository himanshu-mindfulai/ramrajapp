package com.mindfulai.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mindfulai.Activites.ProductsByVendorActivity;
import com.mindfulai.Models.VendorChild;
import com.mindfulai.Utils.CommonUtils;
import com.mindfulai.Utils.SPData;
import com.mindfulai.ministore.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class VendorsAdapter extends RecyclerView.Adapter<VendorsAdapter.MyViewHolder> {


    private static final String TAG = "CategoriesAdapter";
    private final Context context;
    private final List<VendorChild> vendorList;

    public VendorsAdapter(Context context, List<VendorChild> brandList) {
        this.context = context;
        this.vendorList = brandList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.square_category_view_horizontal, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
    //    try {
            holder.brand_name.setText(CommonUtils.capitalizeWord(vendorList.get(position).getFull_name()));
            if (vendorList.get(position).getProfile_picture() != null)
                Glide.with(context).load(SPData.getBucketUrl() + vendorList.get(position).getProfile_picture()).into(holder.circleImageView);
            else
                holder.circleImageView.setImageDrawable(context.getDrawable(R.drawable.noimage));
            holder.circleImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, ProductsByVendorActivity.class);
                    intent.putExtra("category_id","");
                    intent.putExtra("vendor_address",vendorList.get(position).getAddress());
                    intent.putExtra("vendor_id", vendorList.get(position).get_id());
                    intent.putExtra("vendor_name", vendorList.get(position).getFull_name());
                    context.startActivity(intent);
                }
            });
//        } catch (Exception e) {
//            Log.e("TAG", "onBindViewHolder: " + e);
//        }

    }

    @Override
    public int getItemCount() {
        return vendorList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView brand_name;
        ImageView circleImageView;

        MyViewHolder(View view) {
            super(view);
            brand_name = view.findViewById(R.id.cat_name);
            circleImageView = view.findViewById(R.id.cat_image);
        }
    }

}
