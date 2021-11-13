package com.mindfulai.Adapter;

import android.content.Context;
import android.widget.ImageView;

import com.mindfulai.Models.BannerInfoData.BannerInfo;
import com.mindfulai.Utils.SPData;

import java.util.List;

import ss.com.bannerslider.adapters.SliderAdapter;
import ss.com.bannerslider.viewholder.ImageSlideViewHolder;

/**
 * @author S.Shahini
 * @since 2/12/18
 */

public class MainSliderAdapter extends SliderAdapter {


    private final List<BannerInfo> bannerImages_List;
    private final Context context;

    public MainSliderAdapter(Context context, List<BannerInfo> bannerDataArrayList) {
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
         //   viewHolder.imageView.setAdjustViewBounds(true);
            viewHolder.bindImageSlide(SPData.getBucketUrl() + bannerImages_List.get(position).getImage());
        }
    }

}
