package com.mindfulai.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mindfulai.Activites.AllProductsActivity;
import com.mindfulai.Models.varientsByCategory.Option;
import com.mindfulai.Models.varientsByCategory.ProductAttribute;
import com.mindfulai.Utils.CommonUtils;
import com.mindfulai.ministore.R;
import com.mindfulai.ministore.databinding.ProductDetailOptionValueItemBinding;

import java.util.ArrayList;

public class AttributeAdapter extends RecyclerView.Adapter<AttributeAdapter.OptionViewHolder> {

    private final ArrayList<ProductAttribute> productAttributes;
    private final Context context;
    public final ArrayList<String> listOfSelectedOptions;


    public AttributeAdapter(Context context, ArrayList<ProductAttribute> optionsAttributeArrayList) {
        this.context = context;
        this.productAttributes = optionsAttributeArrayList;
        this.listOfSelectedOptions = new ArrayList<>();
    }

    @NonNull
    @Override
    public OptionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new OptionViewHolder(ProductDetailOptionValueItemBinding.inflate(LayoutInflater.from(context), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull OptionViewHolder holder, int baseposition) {
        String name = productAttributes.get(baseposition).getAttribute().getName();
        //  holder.binding.optionItemName.setText(CommonUtils.capitalizeWord(name));
        final ArrayList<Option> available = productAttributes.get(baseposition).getOptions();

        ArrayList<String> allOptionList = new ArrayList<>();
        allOptionList.add(CommonUtils.capitalizeWord(name));
        for (Option option : available) {
            allOptionList.add(option.getValue());
        }
        if (allOptionList.size() > 0) {
            ArrayAdapter arrayAdapter = new ArrayAdapter(context, R.layout.dropdown_item, allOptionList);
            arrayAdapter.setDropDownViewResource(R.layout.dropdown_item);
            holder.binding.spinnerAttributes.setAdapter(arrayAdapter);
        } else {
            ViewGroup.LayoutParams params = holder.binding.linearLayout.getLayoutParams();
            params.height = 0;
            holder.itemView.setLayoutParams(params);
        }
        holder.binding.spinnerAttributes.setSelection(0, false);
        listOfSelectedOptions.add(baseposition, "None");
        holder.binding.spinnerAttributes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = holder.binding.spinnerAttributes.getSelectedItem().toString();
                if (position == 0) {
                    selectedItem = "None";
                }
                if (!listOfSelectedOptions.contains(selectedItem) || selectedItem.equals("None")) {
                    listOfSelectedOptions.set(baseposition, selectedItem);
                } else {
                    listOfSelectedOptions.remove(selectedItem);
                }
                if (context instanceof AllProductsActivity) {
                    ((ProductsAdapter) ((AllProductsActivity) context).smartRecyclerAdapter.getWrappedAdapter()).filterByOption(listOfSelectedOptions);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
//        if (available.size() > 0) {
//            FlexboxLayoutManager flexboxLayoutManager = new FlexboxLayoutManager(context, FlexDirection.ROW);
//            flexboxLayoutManager.setJustifyContent(JustifyContent.FLEX_START);
//            holder.binding.recyclerviewValues.setLayoutManager(flexboxLayoutManager);
//            AttributeValueAdapter attributeValueAdapter = new AttributeValueAdapter(context, available, listOfSelectedOptions, position, flexboxLayoutManager);
//            holder.binding.recyclerviewValues.setAdapter(attributeValueAdapter);
//            attributeValueAdapter.notifyDataSetChanged();
//        } else {
//            ViewGroup.LayoutParams params = holder.binding.linearLayout.getLayoutParams();
//            params.height = 0;
//            holder.itemView.setLayoutParams(params);
//        }
    }


    @Override
    public int getItemCount() {
        if (productAttributes != null)
            return productAttributes.size();
        else
            return 0;
    }


    static class OptionViewHolder extends RecyclerView.ViewHolder {
        ProductDetailOptionValueItemBinding binding;

        OptionViewHolder(@NonNull ProductDetailOptionValueItemBinding itemView) {
            super(itemView.getRoot());
            binding = itemView;
        }
    }
}
