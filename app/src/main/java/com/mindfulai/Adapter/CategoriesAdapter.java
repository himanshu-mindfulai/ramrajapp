package com.mindfulai.Adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.mindfulai.Activites.AllProductsActivity;
import com.mindfulai.Activites.VendorByCategoryActivity;
import com.mindfulai.Models.categoryData.Datum;
import com.mindfulai.Utils.CommonUtils;
import com.mindfulai.Utils.SPData;
import com.mindfulai.ministore.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.MyViewHolder> {


    private static final String TAG = "CategoriesAdapter";
    private final Context context;
    private final List<Datum> categoryList;
    private FragmentManager fragmentManager;
    private final String type;

    public CategoriesAdapter(Context context, List<Datum> categoryList, String type) {

        this.context = context;
        this.categoryList = categoryList;
        this.type = type;

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        View itemView;
        switch (type) {
            case "dunzo":
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.dunzo_category_view, parent, false);
                break;
            case "grid":
                if(SPData.showDunzoTypeCategory())
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.circular_category_view2, parent, false);
                else{
                    itemView = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.square_category_view, parent, false);
                }
                break;
            case "grid-horizontal":
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.square_category_view_horizontal, parent, false);
                break;
            case "list":
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.categories_list_item, parent, false);
                break;
            default:
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.circular_category_view, parent, false);
                break;
        }
        return new MyViewHolder(itemView);
    }

    @SuppressLint("CheckResult")
    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        try {
            if(SPData.useCustomCircleCategoryHeight()&&type.equals("")){
                holder.cat_image.getLayoutParams().height = SPData.categoryHeight();
                holder.cat_image.getLayoutParams().width = SPData.categoryHeight();
            }
            holder.cat_name.setText(CommonUtils.capitalizeWord(categoryList.get(position).getName()));
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.placeholder(R.drawable.noimage);
            requestOptions.error(R.drawable.noimage);
            Glide.with(context)
                    .applyDefaultRequestOptions(requestOptions)
                    .load(SPData.getBucketUrl() + categoryList.get(position).getImage())
                    .into(holder.cat_image);
            holder.cat_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i;
                    if (!SPData.showVendors()){
                        i = new Intent(context, AllProductsActivity.class);
                        i.putExtra("title", categoryList.get(position).getName());
                        ArrayList<Datum> datumArrayList = new ArrayList<>(categoryList);
                        i.putParcelableArrayListExtra("list",datumArrayList);
                    }
                    else {
                        i = new Intent(context, VendorByCategoryActivity.class);
                        i.putExtra("categoryName", categoryList.get(position).getName());
                    }
                    i.putExtra("position",position);
                    i.putExtra("category_id", categoryList.get(position).getId());
                    ((Activity) context).startActivityForResult(i, 2);
                }
            });
        } catch (Exception e) {
            Log.e("TAG", "onBindViewHolder: " + e);
        }

    }

    @Override
    public int getItemCount() {
        try {
            return categoryList.size();
        } catch (Exception e) {
            return 0;
        }
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView cat_image;
        TextView cat_name;


        MyViewHolder(View view) {
            super(view);

            cat_image = view.findViewById(R.id.cat_image);
            cat_name = view.findViewById(R.id.cat_name);
        }
    }

}
