package com.mindfulai.Adapter;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mindfulai.Models.faq.FaqData;
import com.mindfulai.ministore.databinding.FaqItemLayoutBinding;

import java.util.ArrayList;

public class FaqAdapter extends RecyclerView.Adapter<FaqAdapter.FaqHolder> {

    private final Context context;
    private final ArrayList<FaqData> faqData;

    public FaqAdapter(Context context, ArrayList<FaqData> faqDataArrayList) {
        this.context = context;
        this.faqData = faqDataArrayList;
    }

    @NonNull
    @Override
    public FaqHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FaqHolder(FaqItemLayoutBinding.inflate(LayoutInflater.from(context), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull FaqHolder holder, int position) {
        holder.binding.categoryName.setText(faqData.get(position).getName());

        holder.binding.questionAnswer.setText("");
        holder.binding.categoryName.setSelected(faqData.get(position).isSelected());
        if(faqData.get(position).isSelected()) {
            holder.binding.questionAnswer.setVisibility(View.VISIBLE);
        }   else{
            holder.binding.questionAnswer.setVisibility(View.GONE);
        }

        for (int i = 0; i < faqData.get(position).getQuestions().size(); i++) {
            holder.binding.questionAnswer.append(Html.fromHtml("<font><b>Q" + (i + 1) + ". " + faqData.get(position).getQuestions().get(i).getQuestion() + "</b></font>"));
            holder.binding.questionAnswer.append("\n" + faqData.get(position).getQuestions().get(i).getAnswer() + "\n\n\n");
        }
        holder.binding.categoryName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(holder.binding.categoryName.isSelected()){
                    holder.binding.questionAnswer.setVisibility(View.GONE);
                }else {
                    holder.binding.questionAnswer.setVisibility(View.VISIBLE);
                }
                faqData.get(position).setSelected(!holder.binding.categoryName.isSelected());
                holder.binding.categoryName.setSelected(!holder.binding.categoryName.isSelected());
            }
        });
    }

    @Override
    public int getItemCount() {
        return faqData.size();
    }

    static class FaqHolder extends RecyclerView.ViewHolder {
        private final FaqItemLayoutBinding binding;

        public FaqHolder(FaqItemLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
