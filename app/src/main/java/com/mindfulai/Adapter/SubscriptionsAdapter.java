package com.mindfulai.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.gson.JsonObject;
import com.mindfulai.Activites.EditSubscriptionActivity;
import com.mindfulai.Models.subscription.DaysDataModel;
import com.mindfulai.Models.subscription.SubscriptionDataModel;
import com.mindfulai.Models.varientsByCategory.Attribute;
import com.mindfulai.Models.varientsByCategory.Images;
import com.mindfulai.NetworkRetrofit.ApiService;
import com.mindfulai.NetworkRetrofit.ApiUtils;
import com.mindfulai.Utils.CommonUtils;
import com.mindfulai.Utils.SPData;
import com.mindfulai.ministore.R;
import com.mindfulai.ministore.databinding.SubscriptionItemBinding;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SubscriptionsAdapter extends RecyclerView.Adapter<SubscriptionsAdapter.MyViewHolder> {

    private final ArrayList<SubscriptionDataModel> subscriptionDataModelArrayList;
    private final Context context;
    private ArrayList<Attribute> attribute;

    public SubscriptionsAdapter(Context context, ArrayList<SubscriptionDataModel> subscriptionDataModelArrayList) {
        this.context = context;
        this.subscriptionDataModelArrayList = subscriptionDataModelArrayList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(SubscriptionItemBinding.inflate(LayoutInflater.from(context), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Images images = subscriptionDataModelArrayList.get(holder.getAdapterPosition()).getProduct().getProduct().getImages();
        if (images != null) {
            Glide.with(context).load(SPData.getBucketUrl() + images.getPrimary()).into(holder.binding.image);
        }
        holder.binding.productName.setText(subscriptionDataModelArrayList.get(holder.getAdapterPosition()).getProduct().getProduct().getProduct().getName());
        attribute = subscriptionDataModelArrayList.get(holder.getAdapterPosition()).getProduct().getProduct().getAttributes();
        StringBuilder attributes1 = new StringBuilder();
        for (Attribute attribute1 : attribute) {
            attributes1.append(attribute1.getOption().getValue()).append(", ");
        }
        if(!attributes1.toString().isEmpty())
        holder.binding.attributes.setText(attributes1.substring(0, attributes1.length() - 2));

        if (subscriptionDataModelArrayList.get(holder.getAdapterPosition()).isStatus() != null) {
            holder.binding.status.setVisibility(View.VISIBLE);
            String status = subscriptionDataModelArrayList.get(holder.getAdapterPosition()).isStatus();
            if (status.toLowerCase().equals("active")){
                holder.binding.status.setSelected(true);
                holder.binding.status.setText("Pause");
            }
            else{
                holder.binding.status.setSelected(false);
                holder.binding.status.setText("Resume");}
        } else {
            holder.binding.status.setSelected(false);
            holder.binding.status.setText("Resume");
        }

        double mrp = subscriptionDataModelArrayList.get(holder.getAdapterPosition()).getProduct().getProduct().getPrice();
        double sellingPrice = subscriptionDataModelArrayList.get(holder.getAdapterPosition()).getProduct().getProduct().getSellingPrice();

        if (sellingPrice > 0) {
            holder.binding.productPrice.setText(context.getString(R.string.rs) + CommonUtils.getTwoDigitsRoundOffValue(sellingPrice));
        } else
            holder.binding.productPrice.setText(context.getString(R.string.rs) + CommonUtils.getTwoDigitsRoundOffValue(mrp));

        holder.binding.status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeStatus(holder.getAdapterPosition());
            }
        });

        holder.binding.delete.setOnClickListener(v -> deleteSubscription(holder.getAdapterPosition()));
        holder.binding.modify.setOnClickListener(view -> {
            Intent intent = new Intent(context, EditSubscriptionActivity.class);
            intent.putExtra("name", subscriptionDataModelArrayList.get(holder.getAdapterPosition()).getProduct().getProduct().getProduct().getName());
            if (subscriptionDataModelArrayList.get(holder.getAdapterPosition()).getProduct().getProduct().getImages() != null)
                intent.putExtra("image", subscriptionDataModelArrayList.get(holder.getAdapterPosition()).getProduct().getProduct().getImages().getPrimary());
            attribute = subscriptionDataModelArrayList.get(holder.getAdapterPosition()).getProduct().getProduct().getAttributes();
            StringBuilder attributes = new StringBuilder();
            for (Attribute attribute1 : attribute) {
                attributes.append(attribute1.getOption().getValue()).append(", ");
            }
            intent.putExtra("attributes", attributes.substring(0, attributes.length() - 2));
            intent.putExtra("title", "Edit Subscription");
            intent.putExtra("position", holder.getAdapterPosition());
            intent.putExtra("price", subscriptionDataModelArrayList.get(holder.getAdapterPosition()).getProduct().getProduct().getSellingPrice());
            intent.putExtra("sellingPrice", subscriptionDataModelArrayList.get(holder.getAdapterPosition()).getProduct().getProduct().getPrice());
            intent.putExtra("daysQty", subscriptionDataModelArrayList.get(holder.getAdapterPosition()).getDays());
            intent.putExtra("id", subscriptionDataModelArrayList.get(holder.getAdapterPosition()).get_id());
            ((Activity) context).startActivityForResult(intent, 0);
        });

    }

    private void deleteSubscription(int adapterPosition) {
        ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());
        apiService.deleteSubscription(subscriptionDataModelArrayList.get(adapterPosition).get_id()).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NotNull Call<JsonObject> call, @NotNull Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CommonUtils.showSuccessMessage(context, "Subscription delete successfully");
                    subscriptionDataModelArrayList.remove(adapterPosition);
                    notifyItemRemoved(adapterPosition);
                } else {
                    CommonUtils.showErrorMessage(context, "" + response.body().get("message").getAsString());
                }
            }

            @Override
            public void onFailure(@NotNull Call<JsonObject> call, @NotNull Throwable t) {
                CommonUtils.showErrorMessage(context, "" + t.getMessage());
            }
        });
    }

    private void changeStatus(int adapterPosition) {
        ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());
        JsonObject jsonObject = new JsonObject();
        String status = subscriptionDataModelArrayList.get(adapterPosition).isStatus();

        if (status.toLowerCase().equals("active"))
            jsonObject.addProperty("status", "Inactive");
        else
            jsonObject.addProperty("status", "Active");

        apiService.changeSubscriptionStatus(subscriptionDataModelArrayList.get(adapterPosition).get_id(), jsonObject).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NotNull Call<JsonObject> call, @NotNull Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(context, "" + response.body().get("message").getAsString(), Toast.LENGTH_SHORT).show();
                    if (status.toLowerCase().equals("active"))
                        subscriptionDataModelArrayList.get(adapterPosition).setStatus("inactive");
                    else
                        subscriptionDataModelArrayList.get(adapterPosition).setStatus("active");
                    notifyItemChanged(adapterPosition);
                } else {
                    Toast.makeText(context, "" + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NotNull Call<JsonObject> call, @NotNull Throwable t) {
                Toast.makeText(context, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return subscriptionDataModelArrayList.size();
    }

    public void changeDaysData(DaysDataModel daysDataModel, int position) {
        if (position != -1) {
            subscriptionDataModelArrayList.get(position).setDays(daysDataModel);
            notifyItemChanged(position);
        }
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        SubscriptionItemBinding binding;

        MyViewHolder(SubscriptionItemBinding view) {
            super(view.getRoot());
            binding = view;
        }
    }
}
