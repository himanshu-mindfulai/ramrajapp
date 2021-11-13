package com.mindfulai.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;

import android.speech.tts.TextToSpeech;
import android.view.MotionEvent;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.mindfulai.Activites.SearchPrdouctActivity;
import com.mindfulai.Adapter.ProductsAdapter;
import com.mindfulai.Models.varientsByCategory.Datum;
import com.mindfulai.Models.varientsByCategory.VarientsByCategory;
import com.mindfulai.NetworkRetrofit.ApiService;
import com.mindfulai.NetworkRetrofit.ApiUtils;
import com.mindfulai.Utils.CommonUtils;
import com.mindfulai.Utils.SPData;
import com.mindfulai.ministore.R;
import com.mindfulai.ministore.databinding.FragmentSearchBinding;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

import net.gotev.speech.*;
import org.jetbrains.annotations.NotNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class SearchFragment extends Fragment implements SpeechDelegate {


    public SearchFragment() {
        // Required empty public constructor
    }

    FragmentSearchBinding binding;
    private static final int PROUDUCT_REQUEST_CODE = 105;
    private ProductsAdapter productAdapter;
    private List<Datum> varientList;
    private VarientsByCategory productVarients;
    private final int PERMISSIONS_REQUEST = 1;
    private final TextToSpeech.OnInitListener mTttsInitListener = status -> {
        switch (status) {
            case TextToSpeech.SUCCESS:
                Logger.info("LOG_TAG", "TextToSpeech engine successfully started");
                break;

            case TextToSpeech.ERROR:
                Logger.error("LOG_TAG", "Error while initializing TextToSpeech engine!");
                break;

            default:
                Logger.error("LOG_TAG", "Unknown TextToSpeech status: " + status);
                break;
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        inflater.inflate(R.layout.fragment_search, container, false);
        binding = FragmentSearchBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.searchProducts.requestFocus();
        if (getActivity() != null) {
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(binding.searchProducts, InputMethodManager.SHOW_IMPLICIT);
        }
        Speech.init(requireActivity(), requireActivity().getPackageName(), mTttsInitListener);
        initializeRecyclerView();
        setOnClickListener();
    }

    private void initializeRecyclerView() {
        binding.shimmerView2.setVisibility(View.GONE);
        varientList = new ArrayList<>();
        if (getActivity() != null) {
            binding.productsGrid.setLayoutManager(new CommonUtils(requireActivity()).getProductGridLayoutManager());
            productAdapter = new ProductsAdapter(requireActivity(), varientList, "grid", PROUDUCT_REQUEST_CODE);
            binding.productsGrid.setAdapter(productAdapter);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setOnClickListener() {
//        binding.searchProducts.setOnTouchListener((v, event) -> {
//            final int DRAWABLE_RIGHT = 2;
//            if(event.getAction() == MotionEvent.ACTION_UP) {
//                if(event.getRawX() >= (binding.searchProducts.getRight() - binding.searchProducts.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
//                    onButtonClick();
//                    return true;
//                }
//            }
//            return false;
//        });
        binding.searchProducts.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                startShimmering();
                if (!binding.searchProducts.getText().toString().trim().isEmpty()) {
                    getProducts(binding.searchProducts.getText().toString());
                } else {
                    hideProducts();
                }
            }
        });
    }

    private void onButtonClick() {
        if (Speech.getInstance().isListening()) {
            Speech.getInstance().stopListening();
        } else {
            if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                onRecordAudioPermissionGranted();
            } else {
                ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST);
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != PERMISSIONS_REQUEST) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        } else {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onRecordAudioPermissionGranted();
            } else {
                Toast.makeText(requireActivity(), R.string.permission_required, Toast.LENGTH_LONG).show();
            }
        }
    }
    private void onRecordAudioPermissionGranted() {
        binding.linearLayout.setVisibility(View.VISIBLE);
        try {
            Speech.getInstance().stopTextToSpeech();
            Speech.getInstance().startListening(binding.progress, this);
        } catch (SpeechRecognitionNotAvailable exc) {
            showSpeechNotSupportedDialog();
        } catch (GoogleVoiceTypingDisabledException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStartOfSpeech() {
    }

    @Override
    public void onSpeechRmsChanged(float value) {
        //Log.d(getClass().getSimpleName(), "Speech recognition rms is now " + value +  "dB");
    }

    @Override
    public void onSpeechPartialResults(List<String> results) {
        binding.searchProducts.setText("");
        for (String partial : results) {
            binding.searchProducts.append(partial + " ");
        }
    }

    @Override
    public void onSpeechResult(String result) {

        binding.linearLayout.setVisibility(View.GONE);
        binding.searchProducts.setText(result);
        if (result.isEmpty()) {
            Speech.getInstance().say(getString(R.string.repeat));
        } else {
            Speech.getInstance().say(result);
        }
    }

    private void showSpeechNotSupportedDialog() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        SpeechUtil.redirectUserToGoogleAppOnPlayStore(requireActivity());
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setMessage(R.string.speech_not_available)
                .setCancelable(false)
                .setPositiveButton(R.string.yes, dialogClickListener)
                .setNegativeButton(R.string.no, dialogClickListener)
                .show();
    }
    private void startShimmering() {
        binding.shimmerView2.setVisibility(View.VISIBLE);
        binding.shimmerView2.startShimmerAnimation();
        binding.noProducts.setVisibility(View.GONE);
        binding.productsGrid.setVisibility(View.GONE);
    }

    private void showProducts() {
        binding.noProducts.setVisibility(View.GONE);
        binding.shimmerView2.stopShimmerAnimation();
        binding.shimmerView2.setVisibility(View.GONE);
        binding.productsGrid.setVisibility(View.VISIBLE);
    }


    private void getProducts(String query) {
        try {
            ApiService apiService;
            if (!SPData.getAppPreferences().getUsertoken().equals(""))
                apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());
            else
                apiService = ApiUtils.getAPIService();
            apiService.getSearchProducts(query, SPData.getAppPreferences().getPincode()).enqueue(new Callback<VarientsByCategory>() {
                @Override
                public void onResponse(@NonNull Call<VarientsByCategory> call, @NonNull Response<VarientsByCategory> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().getData().size() > 0) {
                        productVarients = response.body();
                        varientList.clear();
                        productAdapter.notifyDataSetChanged();
                        LinkedHashSet<Datum> hashSet = new LinkedHashSet<>(Objects.requireNonNull(productVarients).getData());
                        varientList.addAll(hashSet);
                        productAdapter.notifyDataSetChanged();
                        showProducts();
                    } else {
                        hideProducts();
                        Log.e("TAG", "onResponse: " + response);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<VarientsByCategory> call, @NonNull Throwable t) {
                    hideProducts();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void hideProducts() {
        binding.noProducts.setVisibility(View.VISIBLE);
        binding.productsGrid.setVisibility(View.GONE);
        binding.shimmerView2.stopShimmerAnimation();
        binding.shimmerView2.setVisibility(View.GONE);
    }

}