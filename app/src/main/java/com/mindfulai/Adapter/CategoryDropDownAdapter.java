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
import androidx.recyclerview.widget.RecyclerView;

import com.mindfulai.Models.categoryData.Datum;
import com.mindfulai.Utils.CommonUtils;
import com.mindfulai.ministore.R;

import java.util.ArrayList;

public class CategoryDropDownAdapter extends ArrayAdapter<com.mindfulai.Models.categoryData.Datum> implements ThemedSpinnerAdapter {
    private final ThemedSpinnerAdapter.Helper mDropDownHelper;
    private Context context;

    public CategoryDropDownAdapter(@NonNull Context context, ArrayList<Datum> categoryList) {
        super(context, 0,categoryList);
        this.context = context;
        mDropDownHelper = new ThemedSpinnerAdapter.Helper(context);
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
                    R.layout.category_dropdown_item, parent, false
            );
        }
        TextView textViewName = convertView.findViewById(R.id.dropdown_item);
        Datum currentItem = getItem(position);
        if (currentItem != null) {
            
            textViewName.setText(CommonUtils.capitalizeWord(currentItem.getName()));
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
