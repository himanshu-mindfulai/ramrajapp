package com.mindfulai.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.mindfulai.Activites.*;
import com.mindfulai.NetworkRetrofit.ApiService;
import com.mindfulai.Utils.CommonUtils;
import com.mindfulai.Utils.SPData;
import com.mindfulai.ministore.R;
import com.mindfulai.ministore.databinding.FragmentMoreBinding;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class MoreFragment extends Fragment {


    private FragmentMoreBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        inflater.inflate(R.layout.fragment_more, container, false);
        binding = FragmentMoreBinding.inflate(inflater);
        View view = binding.getRoot();

        if (SPData.useSkyBackgroundColorInMore()) {
            binding.cardProfile.setBackgroundColor(getResources().getColor(R.color.skycolor));
            binding.cardWallet.setBackgroundColor(getResources().getColor(R.color.skycolor));
            binding.cardOrderHistory.setBackgroundColor(getResources().getColor(R.color.skycolor));
            binding.cardWishlist.setBackgroundColor(getResources().getColor(R.color.skycolor));
            binding.cardTopProducts.setBackgroundColor(getResources().getColor(R.color.skycolor));
            binding.cardAbout.setBackgroundColor(getResources().getColor(R.color.skycolor));
            binding.profileTitle.setTextColor(getResources().getColor(R.color.colorWhite));
            binding.profileDescription.setTextColor(getResources().getColor(R.color.colorWhite));
            binding.profileArrow.setColorFilter(ContextCompat.getColor(requireActivity(), R.color.colorWhite), android.graphics.PorterDuff.Mode.SRC_IN);

            binding.walletTitle.setTextColor(getResources().getColor(R.color.colorWhite));
            binding.walletDescription.setTextColor(getResources().getColor(R.color.colorWhite));
            binding.walletArrow.setColorFilter(ContextCompat.getColor(requireActivity(), R.color.colorWhite), android.graphics.PorterDuff.Mode.SRC_IN);

            binding.orderHistoryTitle.setTextColor(getResources().getColor(R.color.colorWhite));
            binding.orderHistoryDescription.setTextColor(getResources().getColor(R.color.colorWhite));
            binding.orderHistoryArrow.setColorFilter(ContextCompat.getColor(requireActivity(), R.color.colorWhite), android.graphics.PorterDuff.Mode.SRC_IN);

            binding.wishlistTitle.setTextColor(getResources().getColor(R.color.colorWhite));
            binding.wishlistDescription.setTextColor(getResources().getColor(R.color.colorWhite));
            binding.wishlistArrow.setColorFilter(ContextCompat.getColor(requireActivity(), R.color.colorWhite), android.graphics.PorterDuff.Mode.SRC_IN);

            binding.topProductTitle.setTextColor(getResources().getColor(R.color.colorWhite));
            binding.topProductDescription.setTextColor(getResources().getColor(R.color.colorWhite));
            binding.topProductArrow.setColorFilter(ContextCompat.getColor(requireActivity(), R.color.colorWhite), android.graphics.PorterDuff.Mode.SRC_IN);

            binding.aboutTitle.setTextColor(getResources().getColor(R.color.colorWhite));
            binding.aboutDescription.setTextColor(getResources().getColor(R.color.colorWhite));
            binding.aboutArrow.setColorFilter(ContextCompat.getColor(requireActivity(), R.color.colorWhite), android.graphics.PorterDuff.Mode.SRC_IN);

        }

        binding.socialMediaLinksLayout.socialMediaLinksMsg.setText(SPData.socialMediaLinkMsg());
        if(SPData.showLoginLogoutMsg()){
            binding.loginLogoutMsg.setVisibility(VISIBLE);
        }else
            binding.loginLogoutMsg.setVisibility(GONE);
        if(SPData.showInstructions()){
            binding.cardInstructions.setVisibility(VISIBLE);
        }else {
            binding.cardInstructions.setVisibility(GONE);
        }
        if (SPData.showSocialMediaIcons()) {
            binding.socialMediaLinksLayout.socialMediaLinks.setVisibility(VISIBLE);
        } else {
            binding.socialMediaLinksLayout.socialMediaLinks.setVisibility(GONE);
        }
        if (SPData.useWebsiteLink()) {
            binding.cardWebsite.setVisibility(VISIBLE);
        } else
            binding.cardWebsite.setVisibility(GONE);

        if (SPData.getFacebook() != null && !SPData.getFacebook().isEmpty()) {
            binding.socialMediaLinksLayout.navFacebook.setVisibility(VISIBLE);
        } else
            binding.socialMediaLinksLayout.navFacebook.setVisibility(GONE);

        if (SPData.getYoutube() != null && !SPData.getYoutube().isEmpty()) {
            binding.socialMediaLinksLayout.navYoutube.setVisibility(VISIBLE);
        } else
            binding.socialMediaLinksLayout.navYoutube.setVisibility(GONE);

        if (SPData.getInstagram() != null && !SPData.getInstagram().isEmpty()) {
            binding.socialMediaLinksLayout.navInstagram.setVisibility(VISIBLE);
        } else {
            binding.socialMediaLinksLayout.navInstagram.setVisibility(GONE);
        }
        if (CommonUtils.stringIsNotNullAndEmpty(SPData.getLinkedIn())&&SPData.showLinkedIn()) {
            binding.socialMediaLinksLayout.navLinkedin.setVisibility(VISIBLE);
        } else {
            binding.socialMediaLinksLayout.navLinkedin.setVisibility(GONE);
        }

        if (SPData.whatsAppNumber() != null && !SPData.whatsAppNumber().isEmpty()) {
            binding.socialMediaLinksLayout.navWhatsapp.setVisibility(VISIBLE);
        } else {
            binding.socialMediaLinksLayout.navWhatsapp.setVisibility(GONE);
        }

        if (SPData.emailAddress() != null && !SPData.emailAddress().isEmpty()) {
            binding.socialMediaLinksLayout.navGmail.setVisibility(VISIBLE);
        } else {
            binding.socialMediaLinksLayout.navGmail.setVisibility(GONE);
        }

        if (SPData.useVendorPackageName()) {
            binding.cardVendor.setVisibility(VISIBLE);
        } else
            binding.cardVendor.setVisibility(GONE);
        if(SPData.showReturnPolicy()){
            binding.cardReturnPrivacy.setVisibility(VISIBLE);
        }else
            binding.cardReturnPrivacy.setVisibility(GONE);

        binding.socialMediaLinksLayout.navLinkedin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommonUtils.openLinkedIn(requireActivity());
            }
        });
        binding.socialMediaLinksLayout.navFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CommonUtils.newFacebookIntent(requireActivity());
            }
        });
        binding.socialMediaLinksLayout.navYoutube.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CommonUtils.watchYoutubeVideo(requireActivity());
            }
        });
        binding.socialMediaLinksLayout.navWhatsapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CommonUtils.sendToWhatsApp(requireActivity());
            }
        });
        binding.socialMediaLinksLayout.navInstagram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CommonUtils.openInstagram(requireActivity());
            }
        });
        binding.socialMediaLinksLayout.navGmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CommonUtils.sendToGmail(requireActivity(), "Bug/Issue in " + getString(R.string.app_name), SPData.emailAddress());
            }
        });
        binding.cardWebsite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommonUtils.openWebsite(requireActivity());
            }
        });
        binding.cardMembership.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(requireActivity(), PurchasedMembershipActivity.class));
            }
        });
        binding.cardVendor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommonUtils.rateApp(SPData.vendorPackageName(), requireActivity());
            }
        });
        binding.cardOrderHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(requireActivity(), OrderHistoryActivity.class));
            }
        });
        binding.cardPaymentHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(requireActivity(), PaymentHistoryActivity.class));
            }
        });
        binding.cardWishlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), CommonActivity.class).putExtra("show", "wish"));
            }
        });
        binding.cardSubscription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), SubscriptionActivity.class));
            }
        });
        binding.cardWallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), WalletActivity.class));
            }
        });
        binding.cardTopProducts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), TopProductsActivity.class));
            }
        });
        binding.cardShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                generateShareLink();
            }
        });
        binding.cardRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CommonUtils.rateApp(requireActivity().getPackageName(), requireActivity());
            }
        });
        binding.cardFaq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), FAQActivity.class));
            }
        });
        binding.cardReportPrblm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CommonUtils.sendToGmail(requireActivity(), "Bug/Issue In " + getString(R.string.app_name), SPData.emailAddress());
            }
        });
        binding.cardProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), ProfileActivity.class));
            }
        });
        binding.cardAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), PrivacyPolicy.class).putExtra("type", ApiService.ABOUTUS));

            }
        });
        binding.cardCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CommonUtils.sendToPhone(requireActivity(), SPData.supportCallNumber());
            }
        });
        binding.cardFaq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), FAQActivity.class));
            }
        });
        binding.cardLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!SPData.getAppPreferences().getUsertoken().isEmpty())
                    showPopup();
                else {
                    startActivity(new Intent(getActivity(), LoginActivity.class));
                }
            }
        });
        binding.cardInstructions.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), PrivacyPolicy.class).putExtra("type", ApiService.INSTRUCTION));
            }
        });
        binding.cardReturnPrivacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), PrivacyPolicy.class).putExtra("type", ApiService.RETURN_POLICY));
            }
        });
        binding.cardPrivacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), PrivacyPolicy.class).putExtra("type", ApiService.PRIVACY));
            }
        });
        binding.cardTermsCondition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), PrivacyPolicy.class).putExtra("type", ApiService.TNC));
            }
        });
        return view;
    }

    private void checkTopProductDiscount() {
        if(SPData.showTopDiscountProduct()){
        if (SPData.showTopDiscountProductInBottomNav()) {
            binding.cardOrderHistory.setVisibility(VISIBLE);
            binding.cardTopProducts.setVisibility(GONE);
        } else {
            binding.cardOrderHistory.setVisibility(GONE);
            binding.cardTopProducts.setVisibility(VISIBLE);
        }
        }else{
            binding.cardTopProducts.setVisibility(GONE);
        }
    }

    private void generateShareLink() {
        ShareAppBottomSheet bottomSheet = new ShareAppBottomSheet();
        bottomSheet.show(getActivity().getSupportFragmentManager(), "ShareBS");
    }

    private void showPopup() {
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setMessage("Are you sure you want to Logout?")
                .setPositiveButton("Logout", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        new CommonUtils(getActivity()).logout();
                    }
                }).setNegativeButton("Cancel", null);

        AlertDialog alert1 = alert.create();
        alert1.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (SPData.showLoginTxt()) {
            binding.cardLogout.setVisibility(VISIBLE);
        } else {
            binding.cardLogout.setVisibility(GONE);
        }
        checkTopProductDiscount();
        if (SPData.getAppPreferences().getUsertoken().isEmpty()) {
            binding.loginLogout.setText("Login");
            binding.loginLogoutMsg.setText("Unlock more features by login");
            binding.cardProfile.setVisibility(GONE);
            binding.cardWallet.setVisibility(GONE);
            binding.cardSubscription.setVisibility(GONE);
            binding.cardWishlist.setVisibility(GONE);
            binding.cardShare.setVisibility(GONE);
            binding.cardOrderHistory.setVisibility(GONE);
            binding.cardMembership.setVisibility(GONE);

        } else {
            binding.loginLogout.setText("Logout");
            binding.loginLogoutMsg.setText("Done for the day? Logout!");
            binding.cardProfile.setVisibility(VISIBLE);
            binding.cardMembership.setVisibility(VISIBLE);
            binding.cardOrderHistory.setVisibility(VISIBLE);
            if (SPData.showMembership())
                binding.cardMembership.setVisibility(VISIBLE);
            else
                binding.cardMembership.setVisibility(GONE);
            if (!SPData.showWalletInPlaceOfCategory()&&SPData.showWallet())
                binding.cardWallet.setVisibility(VISIBLE);
            else
                binding.cardWallet.setVisibility(GONE);

            if (SPData.showSubscription())
                binding.cardSubscription.setVisibility(VISIBLE);
            else
                binding.cardSubscription.setVisibility(GONE);
            if (!SPData.showFavouriteInBottomNav()&&SPData.showWishlist())
                binding.cardWishlist.setVisibility(VISIBLE);
            else
                binding.cardWishlist.setVisibility(GONE);
            binding.cardShare.setVisibility(VISIBLE);
        }
    }
}
