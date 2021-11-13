package com.mindfulai.ui;

import android.os.Bundle;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.mindfulai.Utils.CommonUtils;
import com.mindfulai.ministore.R;

public class RateUsFragment extends BottomSheetDialogFragment {

    public RateUsFragment() {
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
        View view = inflater.inflate(R.layout.fragment_rate_us, container, false);
        TextView title = view.findViewById(R.id.title);
        title.setText("Love using "+getString(R.string.app_name)+"?");
        RatingBar ratingBar = view.findViewById(R.id.rating_bar);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                dismiss();
                CommonUtils.rateApp(requireActivity().getPackageName(),requireActivity());
            }
        });

        return view;
    }
}