package com.mindfulai.Utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mindfulai.Activites.AddAddressActivity;
import com.mindfulai.Activites.AllProductsActivity;
import com.mindfulai.Adapter.UserAddressesAdapter;
import com.mindfulai.Models.UserBaseAddress;
import com.mindfulai.Models.UserDataAddress;
import com.mindfulai.NetworkRetrofit.ApiService;
import com.mindfulai.NetworkRetrofit.ApiUtils;
import com.mindfulai.ministore.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomProgressDialog {

    private final Dialog dialog;
    private final Context mContext;

    public CustomProgressDialog(Context context, String titleText) {

        dialog = new Dialog(context);
        Objects.requireNonNull(dialog.getWindow()).requestFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        mContext = context;

        dialog.getWindow().setContentView(R.layout.custom_progress_dialog);
        TextView title = dialog.findViewById(R.id.textView);
        title.setText(titleText);

        dialog.getWindow().setBackgroundDrawable(
                new ColorDrawable(Color.TRANSPARENT));


    }

    public void show() {
        try {
            if (((Activity) mContext).isFinishing()) {
                return;
            }
            if (!dialog.isShowing() && dialog != null) {
                dialog.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    void dismiss() {
        if (((Activity) mContext).isFinishing()) {
            return;
        }
        if (dialog.isShowing() && dialog != null) {
            dialog.dismiss();
        }
    }

    void setCancelable() {
        dialog.setCancelable(false);
    }


    boolean isShowing() {
        return dialog.isShowing();
    }

}
