package com.mindfulai.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.mindfulai.Adapter.ProductsAdapter;
import com.mindfulai.Models.varientsByCategory.VarientsByCategory;
import com.mindfulai.NetworkRetrofit.ApiService;
import com.mindfulai.NetworkRetrofit.ApiUtils;
import com.mindfulai.Utils.CommonUtils;
import com.mindfulai.Utils.SPData;
import com.mindfulai.ministore.R;
import com.mindfulai.ministore.databinding.FragmentWishlistBinding;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WishListFragment extends Fragment {

    private static final String TAG = "WishListFragment";
    private static final int PROUDUCT_REQUEST_CODE = 103;
    private ProductsAdapter wishListProductAdpater;
    private List<com.mindfulai.Models.varientsByCategory.Datum> bestSellingDataList;
    private FragmentWishlistBinding binding;

    public WishListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        inflater.inflate(R.layout.fragment_wishlist, container, false);
        binding = FragmentWishlistBinding.inflate(inflater);
        bestSellingDataList = new ArrayList<>();
        if(getActivity()!=null)
           binding.productsGrid.setLayoutManager(new CommonUtils(requireActivity()).getProductGridLayoutManager());
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!SPData.getAppPreferences().getUsertoken().isEmpty()) {
            binding.shimmerView2.startShimmerAnimation();
            getAllWishlist();
        } else {
            handleNoItem();
        }
    }

    private void getAllWishlist() {
        ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());
        apiService.getAllWishlist().enqueue(new Callback<VarientsByCategory>() {
            @Override
            public void onResponse(@NonNull Call<VarientsByCategory> call, @NonNull Response<VarientsByCategory> response) {
                binding.shimmerView2.setVisibility(View.GONE);
                binding.noProducts.setVisibility(View.GONE);
                binding.productsGrid.setVisibility(View.VISIBLE);
                Log.e("TAG", "onResponse: "+response);
                if (response.isSuccessful() && response.body() != null && response.body().getData().size() > 0&&getActivity()!=null) {
                    bestSellingDataList = response.body().getData();
                    wishListProductAdpater = new ProductsAdapter(requireActivity(), bestSellingDataList, "grid", PROUDUCT_REQUEST_CODE);
                    binding.productsGrid.setAdapter(wishListProductAdpater);
                    wishListProductAdpater.notifyDataSetChanged();
                } else {
                    handleNoItem();
                }
            }

            @Override
            public void onFailure(@NonNull Call<VarientsByCategory> call, @NonNull Throwable t) {
                Log.e("TAG", "onFailure: "+t);
                handleNoItem();
            }
        });

    }

    private void handleNoItem() {
        binding.noProducts.setVisibility(View.VISIBLE);
        binding.shimmerView2.setVisibility(View.GONE);
        binding.productsGrid.setVisibility(View.GONE);
    }
}
