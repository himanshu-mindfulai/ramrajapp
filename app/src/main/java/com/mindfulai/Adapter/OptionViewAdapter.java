package com.mindfulai.Adapter;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mindfulai.Activites.ProductDetailsActivity;
import com.mindfulai.Models.varientsByCategory.Attribute;
import com.mindfulai.Models.varientsByCategory.Images;
import com.mindfulai.Models.varientsByCategory.OptionsAttribute;
import com.mindfulai.Models.varientsByCategory.Varient;
import com.mindfulai.Models.varientsByCategory.WholeSalePriceModel;
import com.mindfulai.Utils.CommonUtils;
import com.mindfulai.Utils.SPData;
import com.mindfulai.ministore.R;
import com.mindfulai.ministore.databinding.ActivityProductDetails2Binding;
import com.mindfulai.ministore.databinding.ActivityProductDetailsBigBasketBinding;
import com.mindfulai.ministore.databinding.ActivityProductDetailsBinding;
import com.mindfulai.ministore.databinding.ActivityProductDetailsMyntraBinding;

import java.util.ArrayList;
import java.util.Collections;

import static android.view.View.GONE;

public class OptionViewAdapter extends RecyclerView.Adapter<OptionViewAdapter.OptionViewHolder> {

    private final ArrayList<OptionsAttribute> optionsAttributeArrayList;
    private final ArrayList<Varient> differentVarientsArrayList;
    private final ArrayList<String> listforchecking;
    private final ArrayList<String> selectedOptions;
    private final ArrayList<ArrayList<String>> optionValueOfVarients;
    private final ActivityProductDetailsBinding binding;
    private final ProductDetailsActivity context;

    public OptionViewAdapter(ProductDetailsActivity productDetailsActivity) {
        this.listforchecking = new ArrayList<>();
        this.selectedOptions = new ArrayList<>();
        optionValueOfVarients = new ArrayList<>();
        this.context = productDetailsActivity;
        binding = productDetailsActivity.productDetailsBinding;
        optionsAttributeArrayList = productDetailsActivity.optionsAttributeArrayList;
        differentVarientsArrayList = productDetailsActivity.differentVarientsArrayList;
        for (Varient varient : differentVarientsArrayList) {
            ArrayList<String> values = new ArrayList<>();
            for (Attribute attribute : varient.getAttributes()) {
                values.add(attribute.getOption().getValue());
            }
            optionValueOfVarients.add(values);
        }
    }

    @NonNull
    @Override
    public OptionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.option_value_item_base, parent, false);
        return new OptionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final OptionViewHolder holder, final int positionBase) {
        try {
            showVarientsOnDropDown(holder, positionBase);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("TAG", "onBindViewHolder: " + e);
        }
    }

    private void showVarientsOnDropDown(final OptionViewHolder holder, int positionBase) {
        final ArrayList<String> available = optionsAttributeArrayList.get(positionBase).getValue();
        String optionName = optionsAttributeArrayList.get(positionBase).getName();
        int indexOfAttributeInVarient=positionBase;
        for (int j = 0; j < differentVarientsArrayList.get(0).getAttributes().size(); j++){
            if(optionName.equals(differentVarientsArrayList.get(0).getAttributes().get(j).getAttribute().getName())){
                indexOfAttributeInVarient =j;
            }
        }
        for (String s : available)
            if (differentVarientsArrayList.size() > 0 && differentVarientsArrayList.get(0).getAttributes().size() > 0
                    && s.equals(differentVarientsArrayList.get(0).getAttributes().get(indexOfAttributeInVarient).getOption().getValue())) {
                listforchecking.add(s);
            }
        if (listforchecking.size() > 0) {
            available.remove(listforchecking.get(positionBase));
            available.add(0, listforchecking.get(positionBase));
        }

        ArrayAdapter<String> spinnerAdapter;
        if (available.size() == 1) {
            spinnerAdapter = new ArrayAdapter<>(context, R.layout.spinner_item_view_for_one_item, R.id.textSpinner1);
            holder.recyclerViewValues.setEnabled(false);
        } else {
            spinnerAdapter = new ArrayAdapter<>(context, R.layout.spinner_item_view, R.id.textSpinner1);
            holder.recyclerViewValues.setEnabled(true);
        }
        spinnerAdapter.addAll(available);
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_drop_down_resource);

        holder.textViewOptionName.setVisibility(View.VISIBLE);
        holder.textViewOptionName.setText(CommonUtils.capitalizeWord(optionsAttributeArrayList.get(positionBase).getName()) + ":");

        holder.recyclerViewValues.setAdapter(spinnerAdapter);
        holder.recyclerViewValues.setPadding(20, 20, 20, 20);

        selectedOptions.add(positionBase, available.get(0));
        holder.recyclerViewValues.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                holder.recyclerViewValues.setPadding(22, 22, 22, 22);
                String item = available.get(position);
                if (!listforchecking.contains(item)) {
                    for (int k = 0; k < optionsAttributeArrayList.size(); k++) {
                        if (optionsAttributeArrayList.get(k).getValue().contains(item)) {
                            if (listforchecking.size() > 0) {
                                listforchecking.remove(k);
                                listforchecking.add(k, item);
                                break;
                            }
                        }
                    }
                }
                selectedOptions.set(positionBase, available.get(position));

                StringBuilder stringBuilderAttributes = new StringBuilder();
                for (String attribute1 : selectedOptions) {
                    stringBuilderAttributes.append(attribute1).append(", ");
                }
                binding.attributes.setText(stringBuilderAttributes);


                for (int i = 0; i < differentVarientsArrayList.size(); i++) {

                    if (SPData.showSubscription() && differentVarientsArrayList.get(i).isSubscribable() && optionValueOfVarients.get(i).containsAll(listforchecking))
                        binding.productRepeat.setVisibility(View.VISIBLE);
                    else
                        binding.productRepeat.setVisibility(GONE);

                    if (optionValueOfVarients.get(i).containsAll(listforchecking)) {
                        context.available = true;
                        context.handleVarient(differentVarientsArrayList.get(i));
                        break;
                    } else {
                        context.available = false;
                        context.productDetailsBinding.recyclerViewWholeSales.setVisibility(View.GONE);
                    }
                }
                if (!context.available) {
                    context.productDetailsBinding.icWishlist.setVisibility(GONE);
                    context.productDetailsBinding.rvAddToCart.setVisibility(GONE);
                    Toast.makeText(context, "Not available", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public int getItemCount() {
        if (optionsAttributeArrayList != null)
            return optionsAttributeArrayList.size();
        else
            return 0;
    }

    static class OptionViewHolder extends RecyclerView.ViewHolder {
        private final Spinner recyclerViewValues;
        private final TextView textViewOptionName;

        OptionViewHolder(@NonNull View itemView) {
            super(itemView);
            recyclerViewValues = itemView.findViewById(R.id.value_name_spinner);
            textViewOptionName = itemView.findViewById(R.id.option_name);
        }
    }
}