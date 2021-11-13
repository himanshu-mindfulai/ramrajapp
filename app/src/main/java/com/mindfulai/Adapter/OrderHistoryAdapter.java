package com.mindfulai.Adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonObject;
import com.mindfulai.Activites.CheckoutActivity;
import com.mindfulai.Activites.OrderHistoryDetailsActivity;
import com.mindfulai.Models.AllOrderHistory.Datum;
import com.mindfulai.Models.AllOrderHistory.Product;
import com.mindfulai.NetworkRetrofit.ApiService;
import com.mindfulai.NetworkRetrofit.ApiUtils;
import com.mindfulai.Utils.CommonUtils;
import com.mindfulai.Utils.CustomProgressDialog;
import com.mindfulai.Utils.SPData;
import com.mindfulai.ministore.R;
import com.mindfulai.ministore.databinding.OrderhistoryLayoutBinding;
import org.jetbrains.annotations.NotNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.mindfulai.Utils.CommonUtils.gmttoLocalDate;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.OrderHistoryViewHolder> {

    private final Context mContext;
    private final List<Datum> mInfo;
    private final SimpleDateFormat iso;
    public final List<Datum> allOrders;
    private final SimpleDateFormat simpleDateFormat;

    @SuppressLint("SimpleDateFormat")
    public OrderHistoryAdapter(Context context, List<Datum> datumList) {
        this.mContext = context;
        this.mInfo = datumList;
        allOrders = new ArrayList<>(mInfo);
        iso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        simpleDateFormat = new SimpleDateFormat("EEE, dd MMM YYYY");
    }

    @NonNull
    @Override
    public OrderHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new OrderHistoryViewHolder(OrderhistoryLayoutBinding.inflate(LayoutInflater.from(mContext), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull OrderHistoryViewHolder holder, int position) {

        holder.binding.orderId.setText("Order number: " + mInfo.get(position).getInvoiceNumber());
        String date = mInfo.get(position).getOrderDate();
        try {
            Date date_original = gmttoLocalDate(iso.parse(date));
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, dd MMM YYYY");
            String orderDate = simpleDateFormat.format(date_original);
            holder.binding.orderDate.setText("Placed On " + orderDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (mInfo.get(position).getStatus().toLowerCase().contains("cancelled")) {
            holder.binding.cancelledLayout.setVisibility(View.VISIBLE);
            holder.binding.stepLayout.setVisibility(View.GONE);
        } else {
            holder.binding.stepLayout.setVisibility(View.VISIBLE);
            holder.binding.cancelledLayout.setVisibility(View.GONE);
        }

        if (mInfo.get(position).getStatus().toLowerCase().contains("placed")) {
            holder.binding.statusPlaced.setBackground(mContext.getDrawable(R.drawable.step_done));
        } else if (mInfo.get(position).getStatus().toLowerCase().contains("confirmed")) {
            holder.binding.statusPlaced.setBackground(mContext.getDrawable(R.drawable.step_done));
            holder.binding.statusConfirmed.setText("");
            holder.binding.statusConfirmed.setBackground(mContext.getDrawable(R.drawable.step_done));
        } else if (mInfo.get(position).getStatus().toLowerCase().contains("dispatch")) {
            holder.binding.statusPlaced.setBackground(mContext.getDrawable(R.drawable.step_done));
            holder.binding.statusConfirmed.setBackground(mContext.getDrawable(R.drawable.step_done));
            holder.binding.statusDispatch.setBackground(mContext.getDrawable(R.drawable.step_done));
            holder.binding.statusDispatch.setText("");
            holder.binding.statusConfirmed.setText("");
        } else if (mInfo.get(position).getStatus().toLowerCase().contains("delivered")) {
            holder.binding.statusDispatch.setText("");
            holder.binding.statusDelivered.setText("");
            holder.binding.statusDispatch.setText("");
            holder.binding.statusConfirmed.setText("");
            holder.binding.statusConfirmed.setBackground(mContext.getDrawable(R.drawable.step_done));
            holder.binding.statusPlaced.setBackground(mContext.getDrawable(R.drawable.step_done));
            holder.binding.statusDispatch.setBackground(mContext.getDrawable(R.drawable.step_done));
            holder.binding.statusDelivered.setBackground(mContext.getDrawable(R.drawable.step_done));
        }
        holder.binding.orderPrice.setText(mContext.getResources().getString(R.string.rs) + CommonUtils.getTwoDigitsRoundOffValue(mInfo.get(position).getAmount()));

        holder.binding.dvCharge.setText(mContext.getResources().getString(R.string.rs) + CommonUtils.getTwoDigitsRoundOffValue(mInfo.get(position).getDeliveryCharge()));

        if (mInfo.get(position).getProducts().size() > 0)
            holder.binding.orderQuantity.setText("Qty " + mInfo.get(position).getProducts().get(0).getQuantity());
        else
            holder.binding.orderQuantity.setVisibility(View.GONE);

        if (SPData.showOrderRepeatBtn()) {
            holder.binding.repeatOrder.setVisibility(View.VISIBLE);
        } else
            holder.binding.repeatOrder.setVisibility(View.GONE);

        holder.binding.repeatOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addAllOrderItemToCart(mInfo.get(position).getProducts());
            }
        });
        holder.binding.viewDetail.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onClick(View view) {
                Intent i = new Intent(mContext, OrderHistoryDetailsActivity.class);
                i.putExtra("order", mInfo.get(position));
                i.putExtra("order_date", holder.binding.orderDate.getText());
                i.putExtra("position", position);
                ((Activity) mContext).startActivityForResult(i, 3);
            }
        });

    }

    private void addAllOrderItemToCart(List<Product> products) {
        ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());
        CustomProgressDialog progress = CommonUtils.showProgressDialog(mContext, "Please wait...");
        for (int i = 0; i < products.size(); i++) {
            int quantity = products.get(i).getQuantity();
            String id = products.get(i).getProduct().getId();
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("product", id);
            jsonObject.addProperty("quantity", quantity);
            apiService.addItemToCart(jsonObject).enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(@NotNull Call<JsonObject> call, @NotNull Response<JsonObject> response) {

                }

                @Override
                public void onFailure(@NotNull Call<JsonObject> call, @NotNull Throwable t) {

                }
            });
            if (i == products.size() - 1) {
                CommonUtils.hideProgressDialog(progress);
                mContext.startActivity(new Intent(mContext, CheckoutActivity.class));
            }
        }
    }

    public void cancelledOrder(int position) {
        if (position != -1) {
            mInfo.get(position).setStatus("Cancelled");
            notifyItemChanged(position);
        }
    }

    public void changeProductStatus(int orderposition, int product_position, String action) {
        if (orderposition != -1) {
            mInfo.get(orderposition).getProducts().get(product_position).setStatus(action);
            notifyItemChanged(orderposition);
        }
    }

    @Override
    public int getItemCount() {
        return mInfo.size();
    }

    public void filterByDate(String time) {
        mInfo.clear();
        for (Datum datum : allOrders) {
            String date = datum.getOrderDate();
            try {
                Date date_original = gmttoLocalDate(iso.parse(date));
                String orderDate = simpleDateFormat.format(date_original);
                if (orderDate.equals(time)) {
                    mInfo.add(datum);
                }
            } catch (Exception e) {
                Log.e("TAG", "filterByDate: " + e);
            }
        }
        notifyDataSetChanged();

    }

    static class OrderHistoryViewHolder extends RecyclerView.ViewHolder {
        OrderhistoryLayoutBinding binding;

        public OrderHistoryViewHolder(@NonNull OrderhistoryLayoutBinding itemView) {
            super(itemView.getRoot());
            this.binding = itemView;
        }
    }
}
