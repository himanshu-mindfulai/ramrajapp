package com.mindfulai.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.bumptech.glide.Glide;
import com.mindfulai.Activites.AllPreOrderProductsActivity;
import com.mindfulai.Adapter.ProductsAdapter;
import com.mindfulai.Models.DeliveryMethod.DeliveryMethodBase;
import com.mindfulai.Models.DeliveryMethod.DeliveryMethodData;
import com.mindfulai.Models.varientsByCategory.Datum;
import com.mindfulai.Models.varientsByCategory.PageVarientsByCategory;
import com.mindfulai.NetworkRetrofit.ApiService;
import com.mindfulai.NetworkRetrofit.ApiUtils;
import com.mindfulai.Utils.CommonUtils;
import com.mindfulai.Utils.SPData;
import com.mindfulai.ministore.R;
import com.mindfulai.ministore.databinding.FragmentHome2Binding;
import com.mindfulai.ministore.databinding.FragmentHomeBinding;
import org.jetbrains.annotations.NotNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class Home2Fragment extends Fragment {


    private FragmentHome2Binding fragmentHomeBinding;
    private final List<Datum> preOrderProductsList = new ArrayList<>();
    private ProductsAdapter preOrderProductAdapter;
    private boolean fabExpanded = false;
    public Home2Fragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        inflater.inflate(R.layout.fragment_home, container, false);
        fragmentHomeBinding = FragmentHome2Binding.inflate(inflater);
        fragmentHomeBinding.errorMsg.setText(SPData.zoneEnabledDisabledMsg());
        hideFabSubmenu();

        fragmentHomeBinding.seeAllPreorderProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(requireActivity(), AllPreOrderProductsActivity.class));
            }
        });
        fragmentHomeBinding.floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SPData.showBothCallAndWhatsAppOnHome())
                    if (fabExpanded) {
                        hideFabSubmenu();
                    } else {
                        showFabSubmenu();
                    }
                else if (SPData.showOnlyWhatsAppOnHome()) {
                    CommonUtils.sendToWhatsApp(requireActivity());
                } else {
                    CommonUtils.sendToPhone(requireActivity(),SPData.supportCallNumber());
                }
            }
        });
        fragmentHomeBinding.rvCategoriesPreorderProducts.setLayoutManager(new CommonUtils(getActivity()).getProductGridLayoutManager());
        preOrderProductAdapter = new ProductsAdapter(requireActivity(), preOrderProductsList, "grid", 103);
        fragmentHomeBinding.rvCategoriesPreorderProducts.setAdapter(preOrderProductAdapter);

        fragmentHomeBinding.preorderProductLayout.setVisibility(VISIBLE);
        getAllPreOrderProducts();

        return fragmentHomeBinding.getRoot();
    }

    private void getDeliveryMethods(String pincode) {
        ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());
        apiService.getDeliveryMethods(pincode).enqueue(new Callback<DeliveryMethodBase>() {
            @Override
            public void onResponse(@NotNull Call<DeliveryMethodBase> call, @NotNull retrofit2.Response<DeliveryMethodBase> response) {
                if (response.isSuccessful() && response.body() != null) {
                    DeliveryMethodBase deliveryMethodBase = response.body();
                    ArrayList<DeliveryMethodData> deliveryMethodDataArrayList = deliveryMethodBase.getData();
                    ArrayList<String> availableDays = new ArrayList<>();
                    for (DeliveryMethodData deliveryMethodData : deliveryMethodDataArrayList) {
                        if (deliveryMethodData.getZoneData() != null && deliveryMethodData.getZoneData().size() > 0 && deliveryMethodData.getZoneData().get(0).getZone() != null && deliveryMethodData.getZoneData().get(0).getZone().getDays() != null) {
                            if (!availableDays.contains("Mon") && deliveryMethodData.getZoneData().get(0).getZone().getDays().getMon()) {
                                availableDays.add("Monday");
                            }
                            if (!availableDays.contains("Tue") && deliveryMethodData.getZoneData().get(0).getZone().getDays().getTue()) {
                                availableDays.add("Tuesday");
                            }
                            if (!availableDays.contains("Wed") && deliveryMethodData.getZoneData().get(0).getZone().getDays().getWed()) {
                                availableDays.add("Wednesday");
                            }
                            if (!availableDays.contains("Thu") && deliveryMethodData.getZoneData().get(0).getZone().getDays().getThu()) {
                                availableDays.add("Thursday");
                            }
                            if (!availableDays.contains("Fri") && deliveryMethodData.getZoneData().get(0).getZone().getDays().getFri()) {
                                availableDays.add("Friday");
                            }
                            if (!availableDays.contains("Sat") && deliveryMethodData.getZoneData().get(0).getZone().getDays().getSat()) {
                                availableDays.add("Saturday");
                            }
                            if (!availableDays.contains("Sun") && deliveryMethodData.getZoneData().get(0).getZone().getDays().getSun()) {
                                availableDays.add("Sunday");
                            }
                        }
                    }
                    if (availableDays.size() > 0) {
                        StringBuilder days = new StringBuilder();
                        for (String day : availableDays) {
                            days.append(day).append(",");
                        }
                        fragmentHomeBinding.cardShippingDays.setVisibility(VISIBLE);
                        fragmentHomeBinding.shippingDays.setText("Delivery available on " + CommonUtils.removeLastCharacter(days.toString()));
                    } else {
                        fragmentHomeBinding.cardShippingDays.setVisibility(GONE);
                    }
                } else {
                    fragmentHomeBinding.cardShippingDays.setVisibility(GONE);
                }
            }

            @Override
            public void onFailure(@NotNull Call<DeliveryMethodBase> call, @NotNull Throwable t) {
            }
        });
    }

    private void getAllPreOrderProducts() {
        preOrderProductsList.clear();
        ApiService apiService = ApiUtils.getAPIService();
        apiService.getPreOrderProducts(1).enqueue(new Callback<PageVarientsByCategory>() {
            @Override
            public void onResponse(@NotNull Call<PageVarientsByCategory> call, @NotNull Response<PageVarientsByCategory> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData().getRecords().size() > 0) {
                    preOrderProductsList.addAll(response.body().getData().getRecords());
                    preOrderProductAdapter.notifyDataSetChanged();
                } else {
                    hidePreOrderProduct();
                }
            }

            @Override
            public void onFailure(@NotNull Call<PageVarientsByCategory> call, @NotNull Throwable t) {
                hidePreOrderProduct();
            }
        });
    }
    private void hideFabSubmenu() {
        fragmentHomeBinding.fabSubmenu.setVisibility(View.GONE);
        if (SPData.useCompanyLogoInCall() && CommonUtils.stringIsNotNullAndEmpty(SPData.getLogo()))
            Glide.with(requireActivity()).load(SPData.getBucketUrl() + SPData.getLogo()).into(fragmentHomeBinding.floatingActionButton);
        else if (SPData.showOnlyWhatsAppOnHome()) {
            fragmentHomeBinding.floatingActionButton.setImageDrawable(requireActivity().getResources().getDrawable(R.drawable.ic_whtsapp));
        } else
            fragmentHomeBinding.floatingActionButton.setImageDrawable(requireActivity().getResources().getDrawable(R.drawable.ic_baseline_call_24));
        fabExpanded = false;
    }

    private void showFabSubmenu() {
        fragmentHomeBinding.fabSubmenu.setVisibility(View.VISIBLE);
        fragmentHomeBinding.floatingActionButton.setImageResource(R.drawable.ic_baseline_close_24);
        fabExpanded = true;
    }

    private void hidePreOrderProduct() {
        fragmentHomeBinding.preorderProductLayout.setVisibility(GONE);
    }

}