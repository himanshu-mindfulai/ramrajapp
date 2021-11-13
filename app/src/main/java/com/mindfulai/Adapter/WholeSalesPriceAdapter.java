package com.mindfulai.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mindfulai.Models.varientsByCategory.WholeSalePriceModel;
import com.mindfulai.Utils.CommonUtils;
import com.mindfulai.ministore.R;

import java.util.ArrayList;

public class WholeSalesPriceAdapter extends RecyclerView.Adapter<WholeSalesPriceAdapter.WholeSalesViewHolder> {
    private Context context;
    private ArrayList<WholeSalePriceModel> wholeSalePriceModelArrayList;

    public WholeSalesPriceAdapter(Context context, ArrayList<WholeSalePriceModel> wholeSalePriceModelArrayList) {
        this.context = context;
        this.wholeSalePriceModelArrayList = wholeSalePriceModelArrayList;
    }

    @NonNull
    @Override
    public WholeSalesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.wholesale_price__item_layout, parent, false);

        return new WholeSalesViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull WholeSalesViewHolder holder, int position) {
        if (position == 0)
            holder.upto.setText("2 - " + wholeSalePriceModelArrayList.get(position).getUpto() + " unit");
        else if (position == wholeSalePriceModelArrayList.size()) {
            holder.upto.setText("above " + (wholeSalePriceModelArrayList.get(position - 1).getUpto()+1));
        } else
            holder.upto.setText((wholeSalePriceModelArrayList.get(position - 1).getUpto()+1) + "-" + wholeSalePriceModelArrayList.get(position).getUpto() + " unit");

        if (position == wholeSalePriceModelArrayList.size())
            holder.price.setText(context.getString(R.string.rs) + CommonUtils.getTwoDigitsRoundOffValue(wholeSalePriceModelArrayList.get(position - 1).getPricePerUnit()));
        else
            holder.price.setText(context.getString(R.string.rs) + CommonUtils.getTwoDigitsRoundOffValue(wholeSalePriceModelArrayList.get(position).getPricePerUnit()));

    }

    @Override
    public int getItemCount() {
        if (wholeSalePriceModelArrayList != null&&wholeSalePriceModelArrayList.size()>0)
            return wholeSalePriceModelArrayList.size() + 1;
        else
            return 0;
    }

    static class WholeSalesViewHolder extends RecyclerView.ViewHolder {
        private TextView upto;
        private TextView price;

        public WholeSalesViewHolder(@NonNull View itemView) {
            super(itemView);
            upto = itemView.findViewById(R.id.upto);
            price = itemView.findViewById(R.id.price);
        }
    }
}
