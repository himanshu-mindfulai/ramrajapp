package com.mindfulai.Adapter;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.JsonObject;
import com.mindfulai.Activites.OrderHistoryDetailsActivity;
import com.mindfulai.Activites.ProfileActivity;
import com.mindfulai.Models.AllOrderHistory.Attribute;
import com.mindfulai.Models.AllOrderHistory.Product;
import com.mindfulai.NetworkRetrofit.ApiService;
import com.mindfulai.NetworkRetrofit.ApiUtils;
import com.mindfulai.Utils.CommonUtils;
import com.mindfulai.Utils.CustomProgressDialog;
import com.mindfulai.Utils.SPData;
import com.mindfulai.ministore.R;
import com.mindfulai.ministore.databinding.OrderDetailsProductsLayoutBinding;
import com.mindfulai.ui.OrderActionBottomSheet;
import com.valdesekamdem.library.mdtoast.MDToast;
import org.jetbrains.annotations.NotNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;


public class OrderHistoryDetailsAdapter extends RecyclerView.Adapter<OrderHistoryDetailsAdapter.MyViewHolder> {


    private final Context context;
    private final List<Product> orderHistoryDetailsList;
    private final String orderId;
    private double totalSavings;
    private float ratingvalue;

    public OrderHistoryDetailsAdapter(Context context, List<Product> orderHistoryDetailsList, String id) {

        this.context = context;
        this.orderHistoryDetailsList = orderHistoryDetailsList;
        this.orderId = id;
    }

    @Override
    public @NotNull MyViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        return new MyViewHolder(OrderDetailsProductsLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @SuppressLint({"SetTextI18n", "CheckResult"})
    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        try {

            calculateEachPrice(holder,position);
            StringBuilder sb = new StringBuilder();
            if (orderHistoryDetailsList.get(position).getProduct() != null) {
                if (orderHistoryDetailsList.get(position).getProduct().getAttributes() != null) {
                    List<Attribute> attributeList = orderHistoryDetailsList.get(position).getProduct().getAttributes();
                    for (Attribute attribute : attributeList) {
                        sb.append(attribute.getOption().getValue());
                        sb.append(" ");
                    }
                }
                holder.binding.varients.setText(sb);
                holder.binding.name.setText(orderHistoryDetailsList.get(position).getProduct().getProduct().getName());
                RequestOptions requestOptions = new RequestOptions();
                requestOptions.placeholder(R.drawable.noimage);
                requestOptions.error(R.drawable.noimage);

                try {
                    if (orderHistoryDetailsList.get(position).getProduct().getImages() != null)
                        Glide.with(context)
                                .applyDefaultRequestOptions(requestOptions)
                                .load(SPData.getBucketUrl() + orderHistoryDetailsList.get(position).getProduct().getImages().getPrimary())
                                .into(holder.binding.prodImage);
                    else if (orderHistoryDetailsList.get(position).getProduct().getProduct().getImages() != null) {
                        Glide.with(context)
                                .applyDefaultRequestOptions(requestOptions)
                                .load(SPData.getBucketUrl() + orderHistoryDetailsList.get(position).getProduct().getProduct().getImages().getPrimary())
                                .into(holder.binding.prodImage);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("TAG", "onBindViewHolder: " + e);
                }
                holder.binding.noItems.setText(orderHistoryDetailsList.get(position).getQuantity() + " item");
                String orderStatus = null;
                if (context instanceof OrderHistoryDetailsActivity) {
                    orderStatus = ((OrderHistoryDetailsActivity) context).order.getStatus();
                }
                if (orderStatus != null && (orderStatus.equals("Cancelled") || orderHistoryDetailsList.get(position).getStatus() != null || SPData.dontallowIndividualOrderItemAction())) {
                    String msg = orderStatus;
                    if (orderHistoryDetailsList.get(position).getStatus() != null)
                        msg = orderHistoryDetailsList.get(position).getStatus();
                    hideMoreBtn(holder, msg);
                }
                try {
                    SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, dd MMM");
                    String from = "", to = "";
                    if (orderHistoryDetailsList.get(position).getProduct().getProduct().getAvailableFrom() != null) {
                        Date date = iso.parse(orderHistoryDetailsList.get(position).getProduct().getProduct().getAvailableFrom());
                        Date date1 = CommonUtils.gmttoLocalDate(Objects.requireNonNull(date));
                        from = simpleDateFormat.format(date1);
                    }
                    if (orderHistoryDetailsList.get(position).getProduct().getProduct().getExpireOn() != null) {
                        Date date = iso.parse(orderHistoryDetailsList.get(position).getProduct().getProduct().getExpireOn());
                        Date date1 = CommonUtils.gmttoLocalDate(Objects.requireNonNull(date));
                        to = simpleDateFormat.format(date1);
                    }
                    if (from.isEmpty() || to.isEmpty()) {
                        holder.binding.availableFrom.setVisibility(GONE);
                    } else {
                        holder.binding.availableFrom.setVisibility(VISIBLE);
                        holder.binding.availableFrom.setText("Available from " + from + " to " + to);
                    }
                } catch (Exception e) {
                    Log.e("TAG", "handleProduct: " + e);
                    holder.binding.availableFrom.setVisibility(GONE);
                }

                String finalOrderStatus = orderStatus;
                if(orderStatus.equals("Delivered")&&(orderHistoryDetailsList.get(position).getStatus()==null||(orderHistoryDetailsList.get(position).getStatus()!=null&&!orderHistoryDetailsList.get(position).getStatus().equals("Cancelled")))){
                    holder.binding.addReview.setVisibility(VISIBLE);
                }else
                    holder.binding.addReview.setVisibility(GONE);
                holder.binding.addReview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDialogToGetReview(orderHistoryDetailsList.get(position).getProduct().getId(),orderHistoryDetailsList.get(position).getProduct().getProduct().getId());
                    }
                });
                holder.binding.moreItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        OrderActionBottomSheet bottomSheet = new OrderActionBottomSheet(context, orderId, holder, orderHistoryDetailsList.get(position), finalOrderStatus, holder.binding.moreItem, OrderHistoryDetailsAdapter.this, position);
                        if (context instanceof OrderHistoryDetailsActivity) {
                            bottomSheet.show(((OrderHistoryDetailsActivity) context).getSupportFragmentManager(), "OrderStatus");
                        }
                    }
                });
            }
        } catch (Exception e) {
            Log.e("TAG", "onBindViewHolder: " + e);
        }
    }
    private void showDialogToGetReview(String varientId,String productId) {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.custom_rating_dialog);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
        final EditText et_comment = dialog.findViewById(R.id.et_comment);
        final RatingBar ratingBar = dialog.findViewById(R.id.rating_bar);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                ratingvalue = rating;
            }
        });
        Button btn_save = dialog.findViewById(R.id.save);
        TextView btn_cancel = dialog.findViewById(R.id.cancel);

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(et_comment.getText().toString())) {
                    et_comment.setError("Please enter comment");
                    Toast.makeText(context, "Please enter comment", Toast.LENGTH_SHORT).show();
                    et_comment.setFocusable(true);
                } else if (ratingvalue == 0f) {
                    Toast.makeText(context, "Please select rating.", Toast.LENGTH_SHORT).show();
                } else if (!SPData.getAppPreferences().getUserName().equals("")) {
                    postReview(varientId, dialog, et_comment.getText().toString(), ratingvalue,productId);
                } else {
                    MDToast.makeText(context, "Please provide your name", MDToast.LENGTH_SHORT, MDToast.TYPE_INFO).show();
                    context.startActivity(new Intent(context, ProfileActivity.class));
                }
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    private void postReview(final String varient_id, final Dialog dialog, String comment, final float ratingvalue,String productId) {

        final CustomProgressDialog customProgressDialog = CommonUtils.showProgressDialog(context,
                "Posting ... ");
        ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("varient", varient_id);
        jsonObject.addProperty("comment", comment);
        jsonObject.addProperty("product",productId);
        jsonObject.addProperty("rating", ratingvalue);
        apiService.addReview(jsonObject).enqueue(new Callback<JsonObject>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                CommonUtils.hideProgressDialog(customProgressDialog);
                Log.e("TAG", "onResponse: " + response);
                if (response.isSuccessful()) {
                    Toast.makeText(context, "Review added successfully.", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                Log.e("TAG", "" + t.toString());
                Toast.makeText(context, "Failed to connect", Toast.LENGTH_SHORT).show();
                CommonUtils.hideProgressDialog(customProgressDialog);
            }
        });
    }

    private void calculateEachPrice(MyViewHolder holder, int position) {
        double mrp = orderHistoryDetailsList.get(holder.getAdapterPosition()).getProduct().getPrice();
        double sellingPrice = orderHistoryDetailsList.get(holder.getAdapterPosition()).getSellingPrice();
        int quantity = orderHistoryDetailsList.get(holder.getAdapterPosition()).getQuantity();
        double discsaving = orderHistoryDetailsList.get(holder.getAdapterPosition()).getDiscountAmount();
        double realPrice = sellingPrice - discsaving;

        double saving;
        if (mrp>realPrice)
            saving = (mrp - realPrice) * quantity;
        else {
            saving = discsaving * quantity;
        }

        totalSavings = totalSavings + saving;

        if (saving > 0) {
            holder.binding.discount.setVisibility(View.VISIBLE);
            holder.binding.discount.setText("Saved " + context.getResources().getString(R.string.rs) + CommonUtils.getTwoDigitsRoundOffValue(saving));
        } else {
            holder.binding.discount.setVisibility(View.GONE);
        }

        if (context instanceof OrderHistoryDetailsActivity) {
            if (orderHistoryDetailsList.size() - 1 == position) {
                ((OrderHistoryDetailsActivity) context).binding.totalSaving.setText(context.getString(R.string.rs) + CommonUtils.getTwoDigitsRoundOffValue((totalSavings + ((OrderHistoryDetailsActivity) context).ordercouponDiscount)));
            }
        }

        if (mrp > realPrice) {
            holder.binding.productMrp.setVisibility(View.VISIBLE);
            holder.binding.productMrp.setText(context.getString(R.string.rs) + CommonUtils.getTwoDigitsRoundOffValue(mrp));
        } else {
            holder.binding.productMrp.setVisibility(View.GONE);
        }

        if (orderHistoryDetailsList.get(position).getTaxAmt() != 0.0) {
            double taxAmt = orderHistoryDetailsList.get(position).getTaxAmt();
            double withoutGstPrice =  realPrice - orderHistoryDetailsList.get(position).getTaxAmt();
            String taxType = orderHistoryDetailsList.get(position).getTaxType();
            if(taxType!=null&& taxType.equalsIgnoreCase("gst")) {
                String gstPrice = context.getResources().getString(R.string.rs) + CommonUtils.getTwoDigitsRoundOffValue(taxAmt / 2);
                holder.binding.sellingPrice.setText(context.getResources().getString(R.string.rs) + CommonUtils.getTwoDigitsRoundOffValue(realPrice) + " ( " + context.getResources().getString(R.string.rs) + CommonUtils.getTwoDigitsRoundOffValue(withoutGstPrice) + " + SGST " + gstPrice + "\n +  CGST " + gstPrice + " )");
            }else if(taxType!=null){
                holder.binding.sellingPrice.setText(context.getResources().getString(R.string.rs) + CommonUtils.getTwoDigitsRoundOffValue(realPrice) + " ( " + context.getResources().getString(R.string.rs) + CommonUtils.getTwoDigitsRoundOffValue(withoutGstPrice) + " + "+ taxType +" "+context.getResources().getString(R.string.rs) + CommonUtils.getTwoDigitsRoundOffValue(taxAmt) +" )");
            }else{
                holder.binding.sellingPrice.setText(context.getResources().getString(R.string.rs) + CommonUtils.getTwoDigitsRoundOffValue(realPrice) + " ( " + context.getResources().getString(R.string.rs) + CommonUtils.getTwoDigitsRoundOffValue(withoutGstPrice) + " + Tax "+ context.getResources().getString(R.string.rs)  + CommonUtils.getTwoDigitsRoundOffValue(taxAmt) +" )");
            }
        } else {
            holder.binding.sellingPrice.setText(context.getResources().getString(R.string.rs) + CommonUtils.getTwoDigitsRoundOffValue(realPrice));
        }
    }

    private void hideMoreBtn(MyViewHolder holder, String msg) {
        holder.binding.cvOrderItem.setEnabled(false);
        holder.binding.cvOrderItem.setBackgroundColor(context.getResources().getColor(R.color.colorAdd));
        holder.binding.status.setText("" + msg);
        holder.binding.status.setVisibility(View.VISIBLE);
        holder.binding.moreItem.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        if (orderHistoryDetailsList != null)
            return orderHistoryDetailsList.size();
        else
            return 0;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public OrderDetailsProductsLayoutBinding binding;

        public MyViewHolder(OrderDetailsProductsLayoutBinding view) {
            super(view.getRoot());
            binding = view;

        }
    }


}
