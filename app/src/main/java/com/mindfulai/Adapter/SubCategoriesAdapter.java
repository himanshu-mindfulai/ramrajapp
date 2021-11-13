package com.mindfulai.Adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mindfulai.Activites.AllProductsActivity;
import com.mindfulai.Activites.MainActivity;
import com.mindfulai.Models.SubcategoryModel.Datum;
import com.mindfulai.Models.SubcategoryModel.SubcategoryModel;
import com.mindfulai.NetworkRetrofit.ApiService;
import com.mindfulai.NetworkRetrofit.ApiUtils;
import com.mindfulai.Utils.CommonUtils;
import com.mindfulai.Utils.SPData;
import com.mindfulai.ministore.R;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SubCategoriesAdapter extends RecyclerView.Adapter<SubCategoriesAdapter.MyViewHolder> {


    private static final String TAG = "CategoriesAdapter";
    private final Context context;
    private final List<Datum> categoryList;
    private final int level;
    public int selectedPosition = -1;


    public SubCategoriesAdapter(Context context, List<Datum> categoryList, int level) {

        this.context = context;
        this.categoryList = categoryList;
        this.level = level;

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        View itemView;
        if (SPData.showSubcategoryWithTitleOnly() || (SPData.onlyfirstsubcategoryimage() && level > 1))
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.subcategory_title_only_view, parent, false);
        else {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.circular_category_view3, parent, false);
        }
        return new MyViewHolder(itemView);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        try {
            holder.cat_name.setText(CommonUtils.capitalizeWord(categoryList.get(position).getName()));
            if (categoryList.get(position).getImage() != null)
                Glide.with(context).load(SPData.getBucketUrl() + categoryList.get(position).getImage()).into(holder.circleImageView);
            else
                holder.circleImageView.setImageDrawable(context.getDrawable(R.drawable.noimage));

            if (holder.circleImageView.getVisibility() == View.GONE) {
                holder.cat_name.setSelected(categoryList.get(position).isSelected());
                if (categoryList.get(position).isSelected())
                    holder.cat_name.setTextColor(context.getResources().getColor(R.color.colorWhite));
                else
                    holder.cat_name.setTextColor(context.getResources().getColor(R.color.subcategoryColor));

            } else if (categoryList.get(position).isSelected()) {
                holder.cat_name.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
            }else {
                holder.cat_name.setTextColor(context.getResources().getColor(R.color.subcategoryColor));
            }

            holder.circleImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (context instanceof MainActivity){
                        Intent i = new Intent(context, AllProductsActivity.class);
                        i.putExtra("level", level + 1);
                        i.putExtra("category_id", categoryList.get(position).getId());
                        i.putExtra("title", categoryList.get(position).getName());
                        ((Activity) context).startActivityForResult(i, 10);
                    }else {
                        if (context instanceof AllProductsActivity) {
                            ((AllProductsActivity) context).currentCategoryIndex = position;
                        }
                        checkSubCategory(position);
                    }
                }
            });
        } catch (Exception e) {
            Log.e("TAG", "onBindViewHolder: " + e);
        }

    }

    public void checkSubCategory(int position) {
        if (selectedPosition != position) {
            Log.e("TAG", "onClick: get subcategory ");
            getSubCategories(categoryList.get(position).getId(), position);
        }
    }

    private void getSubCategories(final String catID, int position) {
        try {
            ApiService apiService = ApiUtils.getHeaderAPIService();

            apiService.getAllSubCategory(catID, SPData.getAppPreferences().getPincode()).enqueue(new Callback<SubcategoryModel>() {
                @Override
                public void onResponse(@NonNull Call<SubcategoryModel> call, @NonNull Response<SubcategoryModel> response) {
                    if (response.isSuccessful()) {
                        if (response.body().getData().size() > 0) {
                            Intent i = new Intent(context, AllProductsActivity.class);
                            i.putExtra("level", level + 1);
                            i.putExtra("category_id", categoryList.get(position).getId());
                            i.putExtra("title", categoryList.get(position).getName());
                            String brand_id = ((AllProductsActivity) context).brand_id;
                            if (brand_id != null)
                                i.putExtra("brand_id", ((AllProductsActivity) context).brand_id);
                            ((Activity) context).startActivityForResult(i, 10);
                        } else {
                            if (context instanceof AllProductsActivity) {
                                if (selectedPosition != -1) {
                                   deselectItem();
                                }
                                selectedPosition = position;
                                categoryList.get(selectedPosition).setSelected(true);
                                notifyItemChanged(selectedPosition);
                                refreshAllProductPage(categoryList.get(position).getId());
                            }
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<SubcategoryModel> call, @NonNull Throwable t) {
                    Log.e("TAG", "" + t);
                    Toast.makeText(context, "Failed to connect", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void refreshAllProductPage(String catid) {
        ((AllProductsActivity) context).category_id = catid;
        ((AllProductsActivity) context).level = level + 1;
        ((AllProductsActivity) context).currentPage = 1;
        ((AllProductsActivity) context).totalPages = 1;
        ((AllProductsActivity) context).productsList.clear();
        ((AllProductsActivity) context).checkForBanner();
        ((AllProductsActivity) context).getProductsVarients();
        if(SPData.showDiamondPageLayout()){
            ((AllProductsActivity) context).getAllProductAttribute();
        }
    }

    public void deselectItem() {
        categoryList.get(selectedPosition).setSelected(false);
        notifyItemChanged(selectedPosition);
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

        TextView cat_name;
        CircleImageView circleImageView;

        MyViewHolder(View view) {
            super(view);
            cat_name = view.findViewById(R.id.cat_name);
            circleImageView = view.findViewById(R.id.cat_image);
        }
    }

}
