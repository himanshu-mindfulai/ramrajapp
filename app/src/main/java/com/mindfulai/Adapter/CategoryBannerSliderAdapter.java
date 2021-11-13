package com.mindfulai.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.mindfulai.Models.BannerInfoData.CategoryBannerData;
import com.mindfulai.Utils.SPData;

import java.util.List;

import ss.com.bannerslider.adapters.SliderAdapter;
import ss.com.bannerslider.viewholder.ImageSlideViewHolder;

/**
 * @author S.Shahini
 * @since 2/12/18
 */

public class CategoryBannerSliderAdapter extends SliderAdapter {


    private List<CategoryBannerData> bannerImages_List;
    private Context context;

    public CategoryBannerSliderAdapter(Context context, List<CategoryBannerData> bannerDataArrayList) {
        this.context = context;
        this.bannerImages_List = bannerDataArrayList;
    }

    @Override
    public int getItemCount() {
        return bannerImages_List.size();
    }

    @Override
    public void onBindImageSlide(final int position, ImageSlideViewHolder viewHolder) {

        if (bannerImages_List.size() > 0) {
            viewHolder.imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            try {
                viewHolder.imageView.setVisibility(View.VISIBLE);
                Log.e("TAG", ": "+SPData.getBucketUrl() + bannerImages_List.get(position).getImage());
                viewHolder.bindImageSlide(SPData.getBucketUrl() + bannerImages_List.get(position).getImage());
            } catch (Exception e) {
                Log.e("TAG", "onBindImageSlide: " + e);
                viewHolder.imageView.setVisibility(View.GONE);
            }
            viewHolder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }
    }

}
