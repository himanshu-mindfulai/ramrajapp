package com.mindfulai.Adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mindfulai.Activites.AllProductsActivity;
import com.mindfulai.Models.BannerInfoData.BannerInfo;
import com.mindfulai.Utils.CommonUtils;
import com.mindfulai.Utils.SPData;
import com.mindfulai.ministore.databinding.BannerItemviewBinding;
import com.mindfulai.ministore.databinding.BannerItemviewVerticalBinding;

import java.util.ArrayList;
import java.util.List;

public class SecondBannerAdapter extends RecyclerView.Adapter<SecondBannerAdapter.SecondBannerHolder> {

    private List<BannerInfo> bannerImages_List2;
    private final Context context;
    private int banner;

    public SecondBannerAdapter(Context context, List<BannerInfo> bannerImages_List, int banner) {
        this.bannerImages_List2 = bannerImages_List;
        this.context = context;
        this.banner = banner;
    }

    @NonNull
    @Override
    public SecondBannerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (banner == 1 && checkShowBannerInSlider()) {
            return new SecondBannerHolder(BannerItemviewBinding.inflate(LayoutInflater.from(context), parent, false));
        } else {
            return new SecondBannerHolder(BannerItemviewVerticalBinding.inflate(LayoutInflater.from(context), parent, false));
        }
    }

    private boolean checkShowBannerInSlider() {
        return SPData.showBannerInSlider() && bannerImages_List2.size() > 1;
    }


    @Override
    public void onBindViewHolder(@NonNull SecondBannerHolder holder, int position) {


        if (checkShowBannerInSlider() && banner == 1) {
            if(SPData.useCustomHeight())
            holder.bannerItemviewBinding.rv.getLayoutParams().height = SPData.middleBannerHeight();
            if (CommonUtils.stringIsNotNullAndEmpty(bannerImages_List2.get(position).getImage()))
                Glide.with(context).load(SPData.getBucketUrl() + bannerImages_List2.get(position).getImage()).into(holder.bannerItemviewBinding.img);
        } else if (CommonUtils.stringIsNotNullAndEmpty(bannerImages_List2.get(position).getImage())) {
            if (SPData.useCustomHeight())
            holder.bannerItemviewVerticalBinding.rv.getLayoutParams().height = SPData.middleBannerHeight();
            Glide.with(context).load(SPData.getBucketUrl() + bannerImages_List2.get(position).getImage()).into(holder.bannerItemviewVerticalBinding.img);
        }
        holder.itemView.setOnClickListener(v -> {
            Log.e("TAG", ": "+(bannerImages_List2.get(position).getLink()));
            if (bannerImages_List2.get(position).getTarget() != null) {
                Intent i = new Intent(context, AllProductsActivity.class);
                i.putExtra("category_id", bannerImages_List2.get(position).getTarget().getId());
                i.putExtra("title", bannerImages_List2.get(position).getTarget().getFullName());
                context.startActivity(i);
            }else if(bannerImages_List2.get(position).getLink()!=null){
                 CommonUtils.openLink(context,bannerImages_List2.get(position).getLink());
            }
        });
    }

    @Override
    public int getItemCount() {
        return bannerImages_List2.size();
    }

    static class SecondBannerHolder extends RecyclerView.ViewHolder {
        BannerItemviewBinding bannerItemviewBinding;
        BannerItemviewVerticalBinding bannerItemviewVerticalBinding;

        public SecondBannerHolder(@NonNull BannerItemviewBinding itemView) {
            super(itemView.getRoot());
            this.bannerItemviewBinding = itemView;
        }

        public SecondBannerHolder(BannerItemviewVerticalBinding inflate) {
            super(inflate.getRoot());
            this.bannerItemviewVerticalBinding = inflate;
        }
    }
}
