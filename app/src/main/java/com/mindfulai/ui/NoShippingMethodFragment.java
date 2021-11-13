package com.mindfulai.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.mindfulai.Activites.CheckoutActivity;
import com.mindfulai.Activites.MainActivity;
import com.mindfulai.Activites.OrderPlacedActivity;
import com.mindfulai.ministore.R;
import com.mindfulai.ministore.databinding.NoShippingMethodBinding;
import org.jetbrains.annotations.NotNull;

public class NoShippingMethodFragment extends BottomSheetDialogFragment {

    private NoShippingMethodBinding binding;
    private final CheckoutActivity checkoutActivity;
    public NoShippingMethodFragment(CheckoutActivity checkoutActivity){
        this.checkoutActivity = checkoutActivity;
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
         inflater.inflate(R.layout.no_shipping_method, container, false);
         binding = NoShippingMethodBinding.inflate(inflater);
         return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.changeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkoutActivity.handleProceedToCheckout();
                dismiss();
            }
        });
        binding.continueBrowsing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(requireActivity(), MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
            }
        });
    }

}
