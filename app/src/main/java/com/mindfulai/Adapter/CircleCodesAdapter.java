package com.mindfulai.Adapter;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.ThemedSpinnerAdapter;
import com.mindfulai.Models.kwikapimodels.CircleCodeResponse;
import com.mindfulai.Models.kwikapimodels.OperatorCodeResponse;
import com.mindfulai.Utils.CommonUtils;
import com.mindfulai.ministore.R;

import java.util.ArrayList;

public class CircleCodesAdapter extends ArrayAdapter<CircleCodeResponse> implements ThemedSpinnerAdapter {
    private final Helper mDropDownHelper;
    private Context context;

    public CircleCodesAdapter(@NonNull Context context, ArrayList<CircleCodeResponse> categoryList) {
        super(context, 0,categoryList);
        this.context = context;
        mDropDownHelper = new Helper(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    private View initView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.operator_dropdown_item, parent, false
            );
        }
        TextView textViewName = convertView.findViewById(R.id.dropdown_item);
        textViewName.setTextColor(context.getResources().getColor(R.color.colorBlack));
        CircleCodeResponse currentItem = getItem(position);
        if (currentItem != null) {
            textViewName.setText(CommonUtils.capitalizeWord(currentItem.getCircle_name()));
        }
        return convertView;
    }
    @Override
    public void setDropDownViewTheme(Resources.Theme theme) {
        mDropDownHelper.setDropDownViewTheme(theme);
    }

    @Override
    public Resources.Theme getDropDownViewTheme() {
        return mDropDownHelper.getDropDownViewTheme();
    }
}
