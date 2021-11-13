package com.mindfulai.ui;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.mindfulai.Activites.TopProductsActivity;
import com.mindfulai.Adapter.ProductsAdapter;
import com.mindfulai.Models.varientsByCategory.*;
import com.mindfulai.NetworkRetrofit.ApiService;
import com.mindfulai.NetworkRetrofit.ApiUtils;
import com.mindfulai.Utils.CommonUtils;
import com.mindfulai.Utils.SPData;
import com.mindfulai.ministore.R;
import com.mindfulai.ministore.databinding.FragmentTopDiscountProductBinding;
import org.jetbrains.annotations.NotNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TopDiscountProductFragment extends Fragment {


    public TopDiscountProductFragment() {
        // Required empty public constructor
    }

    FragmentTopDiscountProductBinding binding;
    public final static int TOP_PRODUCTS_REQUEST_CODE = 102;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        inflater.inflate(R.layout.fragment_top_discount_product, container, false);
        binding = FragmentTopDiscountProductBinding.inflate(inflater);
        if (getActivity() != null)
            binding.topDiscountProductGrid.setLayoutManager(new CommonUtils(requireActivity()).getProductGridLayoutManager());
        getAllTopDiscountProduct();
        return binding.getRoot();
    }

    private void getAllTopDiscountProduct() {
        ApiService apiService;
        if (!SPData.getAppPreferences().getUsertoken().equals(""))
            apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());
        else
            apiService = ApiUtils.getAPIService();

        apiService.getTopDiscountedProducts().enqueue(new Callback<TopDiscountProductModel>() {
            @Override
            public void onResponse(@NotNull Call<TopDiscountProductModel> call, @NotNull Response<TopDiscountProductModel> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData().size() > 0) {
                    TopDiscountProductModel responseModel = response.body();
                    List<Datum> datumList = new ArrayList<>();
                    ArrayList<DatumForTopDiscount> datumForTopDiscountArrayList = responseModel.getData();
                    for (DatumForTopDiscount datumForTopDiscount : datumForTopDiscountArrayList) {
                        Varient varient = new Varient(datumForTopDiscount.getId(), datumForTopDiscount.getAttributes(), datumForTopDiscount.getDescription(), datumForTopDiscount.getPrice(), datumForTopDiscount.getSellingPrice(), datumForTopDiscount.getStock(), datumForTopDiscount.getImages(), datumForTopDiscount.getReviews(), datumForTopDiscount.isWishlist(), datumForTopDiscount.getInCart(), datumForTopDiscount.getMinOrderQuantity(), datumForTopDiscount.isSubscribable(), datumForTopDiscount.getTag(), datumForTopDiscount.getWholeSalePrices(), datumForTopDiscount.getMaxOrderQuantity(),datumForTopDiscount.isSubscribed());
                        ArrayList<Varient> varients = new ArrayList<>();
                        varients.add(varient);
                        List<Attribute__> attributes = new ArrayList<>();
                        Product product = datumForTopDiscount.getProduct();
                        if (product != null) {
                            com.mindfulai.Models.varientsByCategory.Datum datum = new com.mindfulai.Models.varientsByCategory.Datum(varients, attributes, product);
                            datumList.add(datum);
                        }
                    }
                    Collections.sort(datumList, (s1, s2) -> {
                                float selling = s2.getVarients().get(0).getSellingPrice();
                                float mrp = s2.getVarients().get(0).getPrice();
                                int discounts1 = 0;
                                if (mrp > selling)
                                    discounts1 = (int) (((mrp - selling) / mrp) * 100);


                                float selling2 = s1.getVarients().get(0).getSellingPrice();
                                float mrp2 = s1.getVarients().get(0).getPrice();
                                int discount2 = 0;
                                if (mrp2 > selling2)
                                    discount2 = (int) (((mrp2 - selling2) / mrp2) * 100);

                                return Double.compare(discounts1, discount2);
                            }
                    );
                    if (getActivity() != null) {
                        ProductsAdapter topDiscountProductAdapter = new ProductsAdapter(requireActivity(), datumList, "grid", TOP_PRODUCTS_REQUEST_CODE);
                        binding.topDiscountProductGrid.setAdapter(topDiscountProductAdapter);
                        topDiscountProductAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onFailure(@NotNull Call<TopDiscountProductModel> call, @NotNull Throwable t) {

            }
        });
    }
}