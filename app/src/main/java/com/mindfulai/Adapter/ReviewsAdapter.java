package com.mindfulai.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mindfulai.Models.ReviewData.Datum;
import com.mindfulai.ministore.R;

import java.util.List;

public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.MyViewHolder> {

    private static final String TAG = "ReviewAdapter";
    private List<Datum> reviewList;

    public ReviewsAdapter(Context context, List<Datum> reviewList) {

        this.reviewList = reviewList;

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_review_layout, parent, false);

        return new MyViewHolder(itemView);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        try {
            holder.ratingbar.setRating(reviewList.get(position).getRating());
            if (reviewList.get(position).getCustomer() != null)
                holder.name.setText(reviewList.get(position).getCustomer().getFullName().toUpperCase());
            else
                holder.name.setText("N/A");
            holder.description.setText(reviewList.get(position).getComment());
            String date = reviewList.get(position).getCreatedDate();
            final String[] date1 = date.split("T");
            holder.date_posted.setText("" + date1[0]);
        } catch (Exception e) {
            Log.e("TAG", "onBindViewHolder: " + e);
            holder.itemView.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        RatingBar ratingbar;
        TextView name;
        TextView date_posted;
        TextView description;

        MyViewHolder(View view) {
            super(view);

            ratingbar = view.findViewById(R.id.rating_bar);
            name = view.findViewById(R.id.name);
            date_posted = view.findViewById(R.id.date_posted);
            description = view.findViewById(R.id.description);

        }
    }

}
