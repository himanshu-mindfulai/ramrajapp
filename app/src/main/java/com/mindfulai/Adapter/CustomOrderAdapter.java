package com.mindfulai.Adapter;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mindfulai.Activites.OrderHistoryDetailsActivity;
import com.mindfulai.Models.CustomOrderData;
import com.mindfulai.Models.varientsByCategory.Datum;
import com.mindfulai.Models.varientsByCategory.VarientsByCategory;
import com.mindfulai.NetworkRetrofit.ApiService;
import com.mindfulai.NetworkRetrofit.ApiUtils;
import com.mindfulai.Utils.SPData;
import com.mindfulai.customclass.CityData;
import com.mindfulai.ministore.R;
import com.mindfulai.ministore.databinding.CustomOrderItemBinding;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomOrderAdapter extends RecyclerView.Adapter<CustomOrderAdapter.CustomViewHolder> {

    private final Context context;
    private final ArrayList<CustomOrderData> customOrderDataArrayList;
    private final ApiService apiService;
    private final ArrayList<String> arraylist;
    private ListViewAdapter adapter;

    public CustomOrderAdapter(Context context, ArrayList<CustomOrderData> customOrderData) {
        this.context = context;
        this.customOrderDataArrayList = customOrderData;
        if (!SPData.getAppPreferences().getUsertoken().equals(""))
            apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());
        else
            apiService = ApiUtils.getAPIService();
        arraylist=new ArrayList<>();
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new CustomViewHolder(CustomOrderItemBinding.inflate(LayoutInflater.from(context), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {

        if(!(context instanceof OrderHistoryDetailsActivity)){
            arraylist.clear();
            adapter = new ListViewAdapter(context, arraylist);
            holder.customOrderItemBinding.listProducts.setAdapter(adapter);
            holder.customOrderItemBinding.listProducts.setExpanded(true);
            holder.customOrderItemBinding.listProducts.setOnItemClickListener((parent, view, position1, id) -> {
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(holder.customOrderItemBinding.item.getWindowToken(), 0);
                holder.customOrderItemBinding.item.clearFocus();
                holder.customOrderItemBinding.item.setText(arraylist.get(position1));
                holder.customOrderItemBinding.listProducts.setVisibility(View.GONE);
            });
            holder.customOrderItemBinding.item.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                Log.e("TAG", "afterTextChanged: "+editable.toString());
                holder.customOrderItemBinding.item.setVisibility(View.VISIBLE);
                customOrderDataArrayList.get(holder.getAdapterPosition()).setItem(editable.toString());
                getProducts(editable.toString());
            }
        });
        holder.customOrderItemBinding.qty.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!editable.toString().equals(""))
                    customOrderDataArrayList.get(holder.getAdapterPosition()).setQty(Integer.parseInt(editable.toString()));
                else
                    customOrderDataArrayList.get(holder.getAdapterPosition()).setQty(0);
            }
        });
        holder.customOrderItemBinding.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.customOrderItemBinding.item.setText("");
                holder.customOrderItemBinding.qty.setText("");
                customOrderDataArrayList.remove(holder.getAdapterPosition());
                notifyItemRemoved(holder.getAdapterPosition());
            }
        });
        }else{
            holder.customOrderItemBinding.delete.setVisibility(View.GONE);
            holder.customOrderItemBinding.item.setEnabled(false);
            holder.customOrderItemBinding.qty.setEnabled(false);

            holder.customOrderItemBinding.item.setBackgroundResource(0);
            holder.customOrderItemBinding.item.setTextColor(context.getResources().getColor(R.color.colorText));

            holder.customOrderItemBinding.qty.setBackgroundResource(0);
            holder.customOrderItemBinding.qty.setTextColor(context.getResources().getColor(R.color.colorText));

            holder.customOrderItemBinding.qty.setText(customOrderDataArrayList.get(position).getQty()+" qty");
            holder.customOrderItemBinding.item.setText(customOrderDataArrayList.get(position).getItem());
        }
    }

    private void getProducts(String query) {
        try {
            apiService.getSearchProducts(query, SPData.getAppPreferences().getPincode()).enqueue(new Callback<VarientsByCategory>() {
                @Override
                public void onResponse(@NonNull Call<VarientsByCategory> call, @NonNull Response<VarientsByCategory> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().getData().size() > 0) {
                        arraylist.clear();
                        for(Datum datum:response.body().getData()){
                            arraylist.add(datum.getProduct().getName());
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.e("TAG", "onResponse: " + response);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<VarientsByCategory> call, @NonNull Throwable t) {
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return customOrderDataArrayList.size();
    }

    static class CustomViewHolder extends RecyclerView.ViewHolder {
        CustomOrderItemBinding customOrderItemBinding;

        public CustomViewHolder(@NonNull CustomOrderItemBinding itemView) {
            super(itemView.getRoot());
            customOrderItemBinding = itemView;
        }
    }
}
