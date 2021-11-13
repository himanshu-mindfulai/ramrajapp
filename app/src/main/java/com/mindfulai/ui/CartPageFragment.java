package com.mindfulai.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.mindfulai.Activites.*;
import com.mindfulai.Adapter.CartAdapater;
import com.mindfulai.Models.CartInformation.CartDetailsInformation;
import com.mindfulai.Models.CartInformation.Product;
import com.mindfulai.NetworkRetrofit.ApiService;
import com.mindfulai.NetworkRetrofit.ApiUtils;
import com.mindfulai.Utils.CommonUtils;
import com.mindfulai.Utils.SPData;
import com.mindfulai.ministore.R;
import com.mindfulai.ministore.databinding.FragmentCartPageBinding;

import com.valdesekamdem.library.mdtoast.MDToast;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartPageFragment extends Fragment {
    public List<Product> cartDataArrayList;
    public FragmentCartPageBinding binding;
    private CartDetailsInformation cartDetailsInformation;
    private CartAdapater cartAdapater;
    private Context context;;

    public CartPageFragment(Activity cartPageActivity) {
        // Required empty public constructor
      context = cartPageActivity;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        inflater.inflate(R.layout.fragment_cart_page, container, false);

        binding = FragmentCartPageBinding.inflate(inflater);
        View view = binding.getRoot();

        if (getActivity() != null) {
            LinearLayoutManager manager = new LinearLayoutManager(requireActivity());
            binding.cartRv.setLayoutManager(manager);
        }
        if(context instanceof CartPageActivity&&((CartPageActivity) context).moveToCheckout){
            moveToCheckout();
        }
        binding.proceedToCheckout.setOnClickListener(view1 -> {
            if (cartAdapater.inActiveProductName.isEmpty()) {
                 moveToCheckout();
            } else {
                MDToast.makeText(requireActivity(), "" + cartAdapater.inActiveProductName + " is no longer available", Toast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
            }
        });
        return view;
    }

    private void moveToCheckout() {
        if (!SPData.getAppPreferences().getUsertoken().isEmpty() && !SPData.getAppPreferences().getAddressId().isEmpty() && (!SPData.getAppPreferences().getUserShippingCoordinated().isEmpty() || !SPData.useDeliveryChargeByCoordinates())) {
            if (SPData.useMinCartValue() && SPData.minCartValue() > cartDetailsInformation.getData().getTotal()) {
                CommonUtils.showErrorMessage(requireActivity(), "Minimum cart value should be " + getString(R.string.rs) + SPData.minCartValue());
            } else if (getActivity() != null)
                startActivityForResult(new Intent(requireActivity(), CheckoutActivity.class), 10);
        } else if (SPData.getAppPreferences().getUsertoken().isEmpty()) {
            Toast.makeText(requireActivity(), "Please login to checkout", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(requireActivity(), LoginActivity.class));
        } else if (SPData.getAppPreferences().getAddressId().isEmpty() || SPData.getAppPreferences().getUserShippingCoordinated().isEmpty()) {
            if (SPData.getAppPreferences().getAddressId().isEmpty())
                Toast.makeText(requireActivity(), "Please select a shipping address", Toast.LENGTH_SHORT).show();
            else if (SPData.getAppPreferences().getUserShippingCoordinated().isEmpty())
                Toast.makeText(requireActivity(), "Please select a shipping address pick from map", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(requireActivity(), AddressSelectorActivity.class).putExtra("cartpage", true));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getAllCart();
    }

    private void getAllCart() {
        try {
            if (!SPData.getAppPreferences().getUsertoken().isEmpty()) {
                ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());
                if (SPData.showShippingMethods())
                    apiService.showCartItems(SPData.getAppPreferences().getPincode()).enqueue(new Callback<CartDetailsInformation>() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onResponse(@NotNull Call<CartDetailsInformation> call, @NotNull Response<CartDetailsInformation> response) {
                            if (getActivity() != null)
                                checkResponseFromCart(response);
                        }

                        @Override
                        public void onFailure(@NonNull Call<CartDetailsInformation> call, @NonNull Throwable t) {
                            handleNoCartItem();
                        }
                    });
                else {
                    apiService.showCartItems().enqueue(new Callback<CartDetailsInformation>() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onResponse(@NotNull Call<CartDetailsInformation> call, @NotNull Response<CartDetailsInformation> response) {
                            if (getActivity() != null)
                                checkResponseFromCart(response);
                        }

                        @Override
                        public void onFailure(@NonNull Call<CartDetailsInformation> call, @NonNull Throwable t) {
                            handleNoCartItem();
                        }
                    });
                }
            } else {
                ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUserUniqueId());
                if (SPData.showShippingMethods())
                    apiService.showCartItemsWithoutLogin(SPData.getAppPreferences().getPincode()).enqueue(new Callback<CartDetailsInformation>() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onResponse(@NotNull Call<CartDetailsInformation> call, @NotNull Response<CartDetailsInformation> response) {
                            if (getActivity() != null)
                                checkResponseFromCart(response);
                        }

                        @Override
                        public void onFailure(@NonNull Call<CartDetailsInformation> call, @NonNull Throwable t) {
                            handleNoCartItem();
                        }
                    });
                else
                    apiService.showCartItemsWithoutLogin().enqueue(new Callback<CartDetailsInformation>() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onResponse(@NotNull Call<CartDetailsInformation> call, @NotNull Response<CartDetailsInformation> response) {
                            if (getActivity() != null)
                                checkResponseFromCart(response);
                        }

                        @Override
                        public void onFailure(@NonNull Call<CartDetailsInformation> call, @NonNull Throwable t) {
                            handleNoCartItem();
                        }
                    });
            }
        } catch (Exception e) {
            Log.e("TAG", "getAllCart: " + e);
            if (getActivity() != null)
                Toast.makeText(requireActivity(), "" + e, Toast.LENGTH_SHORT).show();
            handleNoCartItem();
        }
    }

    @SuppressLint("SetTextI18n")
    public void setCartAmount(CartDetailsInformation cartDetailsInformation) {
        this.cartDetailsInformation = cartDetailsInformation;
        if (!SPData.getAppPreferences().getUsertoken().isEmpty())
            binding.proceedToCheckout.setText("Total " + getString(R.string.rs) + cartDetailsInformation.getData().getTotal() + " Proceed to checkout");
        else
            binding.proceedToCheckout.setText("Total " + getString(R.string.rs) + cartDetailsInformation.getData().getTotal() + " Proceed login to checkout");
    }

    private void checkResponseFromCart(Response<CartDetailsInformation> response) {
        try {

            if (response.isSuccessful() && getActivity() != null) {
                cartDetailsInformation = response.body();
                cartDataArrayList = cartDetailsInformation.getData().getProducts();
                if (cartDataArrayList.size() > 0 && getActivity() != null) {
                    cartAdapater = new CartAdapater(requireActivity(), CartPageFragment.this);
                    binding.cartRv.setAdapter(cartAdapater);
                    cartAdapater.notifyDataSetChanged();
                    binding.progressBar.setVisibility(View.GONE);
                    binding.noProducts.setVisibility(View.GONE);
                    binding.proceedToCheckout.setVisibility(View.VISIBLE);
                    setCartAmount(cartDetailsInformation);
                } else {
                    handleNoCartItem();
                }
            } else {
                handleNoCartItem();
            }
        } catch (Exception e) {
            if (getActivity() != null)
                Toast.makeText(requireActivity(), "" + e, Toast.LENGTH_SHORT).show();
            handleNoCartItem();
        }
    }

    private void handleNoCartItem() {
        binding.progressBar.setVisibility(View.GONE);
        binding.cartRv.setVisibility(View.GONE);
        binding.proceedToCheckout.setVisibility(View.GONE);
        binding.noProducts.setVisibility(View.VISIBLE);
        SPData.getAppPreferences().setTotalCartCount(0);
        SPData.getAppPreferences().setCartVendorId("");
        if (getActivity() != null && requireActivity() instanceof MainActivity) {
            ((MainActivity) requireActivity()).removeBadge();
        }
    }
}