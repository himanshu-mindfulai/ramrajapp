package com.mindfulai.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.mindfulai.Utils.SPData;
import com.mindfulai.ministore.R;
import com.mindfulai.ministore.databinding.FragmentWalletBinding;

import java.util.ArrayList;
import java.util.List;


public class WalletFragment extends Fragment {


    private FragmentWalletBinding binding;
    public WalletRechargeFragment walletRechargeFragment;
    public WalletTransactionFragment walletTransactionFragment;
    public WalletTransactionFragment walletTransactionFragmentCredit;
    public WalletTransactionFragment walletTransactionFragmentSignupCredit;

    public WalletFragment() {
        // Required empty public constructor
    }


    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        inflater.inflate(R.layout.fragment_wallet, container, false);
        binding = FragmentWalletBinding.inflate(inflater);
        walletRechargeFragment = new WalletRechargeFragment();
        walletTransactionFragment = new WalletTransactionFragment("","");
        if (SPData.showReferralCredit())
            walletTransactionFragmentCredit = new WalletTransactionFragment("referral_credit","");
        if (SPData.showSignUpBonusReferral())
            walletTransactionFragmentSignupCredit = new WalletTransactionFragment("referral_credit","signup");
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViewPager(binding.viewpager);
        binding.tabsLayout.setupWithViewPager(binding.viewpager);
    }

    private void setupViewPager(final ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getActivity(), getChildFragmentManager());
        adapter.addFragment(walletRechargeFragment, "Recharge");
        adapter.addFragment(walletTransactionFragment, "Payments");
        if (SPData.showReferralCredit())
            adapter.addFragment(walletTransactionFragmentCredit, SPData.referralCreditTitle());
        if (SPData.showSignUpBonusReferral())
            adapter.addFragment(walletTransactionFragmentSignupCredit, SPData.signUpBonusReferralTitle());
        viewPager.setAdapter(adapter);
    }

    static class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        ViewPagerAdapter(Context context, FragmentManager manager) {
            super(manager);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}