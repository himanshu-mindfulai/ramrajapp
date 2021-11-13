package com.mindfulai.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.mindfulai.Utils.CommonUtils;
import com.mindfulai.ministore.R;

public class WalletRechargeBottomSheetFragment extends BottomSheetDialogFragment {

    public WalletRechargeBottomSheetFragment() {
        // Required empty public constructor
    }

    private WalletRechargeFragment walletRechargeFragment;
    public WalletRechargeBottomSheetFragment(WalletRechargeFragment walletRechargeFragment){
        this.walletRechargeFragment = walletRechargeFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.wallet_recharge_layout, container, false);
        TextView title = view.findViewById(R.id.add_money_msg);
        title.setText("Enter Topup Amount");
        Button btn = view.findViewById(R.id.btn_pay);
        EditText editText = view.findViewById(R.id.et_enter_amount);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                walletRechargeFragment.handleButtonPayClick(editText.getText().toString().replaceAll(requireActivity().getString(R.string.rs), ""));
            }
        });
        return view;
    }
}