package com.mindfulai.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.mindfulai.Models.BannerInfoData.BannerInfo;
import com.mindfulai.Utils.SPData;
import com.mindfulai.ministore.R;
import com.smarteist.autoimageslider.SliderViewAdapter;
import java.util.List;

public class VendorGallerySliderAdapter extends
        SliderViewAdapter<VendorGallerySliderAdapter.SliderAdapterVH> {

    private final Context context;
    private final List<BannerInfo> mSliderItems;

    public VendorGallerySliderAdapter(Context context, List<BannerInfo> sliderItems) {
        this.context = context;
        this.mSliderItems = sliderItems;
        notifyDataSetChanged();
    }


    @Override
    public SliderAdapterVH onCreateViewHolder(ViewGroup parent) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.vendor_image_slider_item, null);
        return new SliderAdapterVH(inflate);
    }

    @Override
    public void onBindViewHolder(SliderAdapterVH viewHolder, final int position) {

        String sliderItem = mSliderItems.get(position).getImage();
        Glide.with(context).load(SPData.getBucketUrl() + sliderItem).into(viewHolder.imageViewBackground);
    }

    @Override
    public int getCount() {
        //slider view count could be dynamic size
        return mSliderItems.size();
    }

    static class SliderAdapterVH extends SliderViewAdapter.ViewHolder {
        ImageView imageViewBackground;

        SliderAdapterVH(View itemView) {
            super(itemView);
            imageViewBackground = itemView.findViewById(R.id.salon_image_slider_item);
        }
    }
}