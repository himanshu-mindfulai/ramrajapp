package com.mindfulai.Utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.request.RequestOptions;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.mindfulai.Activites.AddAddressActivity;
import com.mindfulai.Activites.CheckoutActivity;
import com.mindfulai.Activites.LoginActivity;
import com.mindfulai.Adapter.TimePickerSlotsAdapter;
import com.mindfulai.Adapter.UserAddressesAdapter;
import com.mindfulai.Models.UserBaseAddress;
import com.mindfulai.Models.UserDataAddress;
import com.mindfulai.NetworkRetrofit.ApiService;
import com.mindfulai.NetworkRetrofit.ApiUtils;
import com.mindfulai.ministore.R;
import com.valdesekamdem.library.mdtoast.MDToast;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommonUtils {
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public final static int BEST_SELLING_PRODUCT_REQUEST_CODE = 100;
    public final static int FEATURED_PRODUCTS_REQUEST_CODE = 101;
    public final static int TOP_FEATURED_PRODUCTS_REQUEST_CODE = 102;
    public final static int PRE_ORDER_PRODUCTS_REQUEST_CODE = 103;
    public final static int PRODUCTS_BY_VENDOR_REQUEST_CODE = 103;

    private GoogleSignInClient mGoogleSignInClient;
    private final Context context;
    private List<Address> addresses;

    private static AlertDialog alertDialog1;
    private static RecyclerView recyclerViewAddress;
    private static ArrayList<UserDataAddress> userDataAddressArrayList;

    public CommonUtils(Context contextactivity) {
        context = contextactivity;
        if (SPData.getAppPreferences().signInByGoogle()) {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(contextactivity.getString(R.string.google_webclient_id))
                    .requestEmail()
                    .build();
            mGoogleSignInClient = GoogleSignIn.getClient(contextactivity, gso);
        }
    }

    public static void showToastMessageAtCenter(Context context,String message){
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public static Bitmap getBitmapfromUri(String imageUri) {
        URL url;
        HttpURLConnection connection;
        InputStream input;
        Bitmap bitmap = null;
        try {
            url = new URL(imageUri);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            input = connection.getInputStream();
            bitmap = BitmapFactory.decodeStream(input);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("TAG", "getBitmapfromUri: " + e);
        }
        return bitmap;
    }

    public static void handleResponseError(Context context,Response response){
        try {
            String error = response.errorBody().string();
            JSONObject jsonObject1 = new JSONObject(error);
            Log.e("TAG", "onResponse: "+jsonObject1);
            Toast.makeText(context, "" + jsonObject1.getString("message"), Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            Log.e("TAG", "handleResponseError: "+e);
        }
    }
    @SuppressLint("SetTextI18n")
    public static void pickTime(Date date, boolean automatic, TextView timeSlot, Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View dialogView = activity.getLayoutInflater().inflate(R.layout.time_picker_custom_layout, null);
        builder.setView(dialogView);
        AlertDialog alertDialog = builder.create();
        Date currentDate = new Date();
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sDFormart = new SimpleDateFormat("dd-MM-YYYY");
        ArrayList<String> stringArrayList = new ArrayList<>(SPData.allTimeSlot());
        if (sDFormart.format(currentDate).equals(sDFormart.format(date))) {
            try {
                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm a");
                Date selectedDate = simpleDateFormat.parse(simpleDateFormat.format(date));
                int size = 0;
                for (int i = 0; i < stringArrayList.size(); i++) {
                    Date time1 = simpleDateFormat.parse(stringArrayList.get(i).split("-")[0]);
                    if (i == 0 && (selectedDate.before(time1) || selectedDate.equals(time1))) {
                        break;
                    }
                    if (selectedDate.before(time1) || selectedDate.equals(time1)) {
                        size = i;
                        break;
                    } else if (i == stringArrayList.size() - 1 && selectedDate.after(time1)) {
                        size = stringArrayList.size();
                        break;
                    }
                }
                for (int j = 0; j < size; j++) {
                    stringArrayList.remove(SPData.allTimeSlot().get(j));
                }
            } catch (Exception e) {
                Log.e("TAG", "pickTime: " + e);
            }
        }
        if (stringArrayList.size() > 0) {
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat sdf = new SimpleDateFormat(("dd-MM-yyyy EEEE"));
            String dateString = sdf.format(date);
            if (!automatic) {
                RecyclerView recyclerView = dialogView.findViewById(R.id.time_picker_recycler_layout);
                recyclerView.setLayoutManager(new LinearLayoutManager(activity));
                TimePickerSlotsAdapter slotsAdapter = new TimePickerSlotsAdapter(activity, stringArrayList, timeSlot, dateString, alertDialog);
                recyclerView.setAdapter(slotsAdapter);
                alertDialog.show();
            } else {
                timeSlot.setText(dateString + " " + stringArrayList.get(0));
            }
        } else if (automatic) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            Date tomorrow = calendar.getTime();
            @SuppressLint({"SimpleDateFormat", "WeekBasedYear"})
            SimpleDateFormat sdf = new SimpleDateFormat(("dd-MM-YYYY EEEE"));
            String dateString = sdf.format(tomorrow);
            timeSlot.setText(dateString + " " + SPData.allTimeSlot().get(0));
        } else {
            Toast.makeText(activity, "No time found for selected date", Toast.LENGTH_SHORT).show();
        }
    }


    public static void getAllAddress(Context context, String varientId, String name, String image, String attributes, double price, double sellingPrice) {
        try {
            ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());
            apiService.getUserBaseAddress().enqueue(new Callback<UserBaseAddress>() {
                @Override
                public void onResponse(@NonNull Call<UserBaseAddress> call, @NonNull Response<UserBaseAddress> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        userDataAddressArrayList = response.body().getData();
                        UserAddressesAdapter addressesAdapter = new UserAddressesAdapter(
                                context,
                                userDataAddressArrayList,
                                alertDialog1,
                                varientId,
                                name,
                                image,
                                attributes,
                                price,
                                sellingPrice
                        );
                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
                        recyclerViewAddress.setLayoutManager(linearLayoutManager);
                        recyclerViewAddress.setAdapter(addressesAdapter);
                        addressesAdapter.notifyDataSetChanged();
                        alertDialog1.show();
                    } else {
                        Log.e("TAG", "onResponse: " + response);
                    }
                }

                @Override
                public void onFailure(@NotNull Call<UserBaseAddress> call, @NotNull Throwable t) {
                    Log.e("TAG", "onFailure: " + t.getMessage());
                }
            });
        } catch (Exception e) {
            Toast.makeText(context, "" + e, Toast.LENGTH_SHORT).show();
            Log.e("TAG", "getAllAddress: " + e);
        }
    }

    public static void promptSelectAddressDialog(Context context, String varientId, String name, String image, String attributes, double price, double sellingPrice) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_pickk_address, null);
        recyclerViewAddress = view.findViewById(R.id.recycler_view_address);
        TextView timeSlottext = view.findViewById(R.id.select_time_text);
        TextView addAddress = view.findViewById(R.id.add_address);
        TextView timeSlot = view.findViewById(R.id.time_slot);
        ImageView close = view.findViewById(R.id.close);

        timeSlottext.setText("Select address");
        timeSlot.setVisibility(View.GONE);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setView(view);

        alertDialog1 = alertDialog.create();
        alertDialog1.setCanceledOnTouchOutside(false);

        getAllAddress(context, varientId, name, image, attributes, price, sellingPrice);

        addAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog1.dismiss();
                context.startActivity(new Intent(context, AddAddressActivity.class).putExtra("title", "Add Address"));
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog1.dismiss();
            }
        });
    }


    public static void checkForAppUpdate(Activity activity, double versonCode) {
        try {
            PackageInfo pInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
            double currentAppVersionCode = Double.parseDouble(pInfo.versionName);
            Log.e("TAG", "checkForAppUpdate:currentAppVersionCode " + currentAppVersionCode);
            if (versonCode > currentAppVersionCode) {
                String cancel = "Cancel";
                if (SPData.forceToUpdate()) {
                    cancel = "Close app";
                }
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
                alertDialogBuilder.setTitle("Update " + activity.getString(R.string.app_name));
                if (SPData.forceToUpdate())
                    alertDialogBuilder.setCancelable(false);
                alertDialogBuilder.setMessage("A new version of " + activity.getString(R.string.app_name) + " is available with bug fixes and improvement");
                alertDialogBuilder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + activity.getPackageName())));
                    }
                }).setNegativeButton(cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (SPData.forceToUpdate()) {
                            activity.finish();
                        } else
                            dialog.cancel();
                    }
                });
                alertDialogBuilder.create().show();
            }
        } catch (Exception e) {
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + activity.getPackageName())));
        }
    }

    public static boolean stringIsNotNullAndEmpty(String string) {
        return string != null && !string.replaceAll(" ", "").isEmpty();
    }

    public static String removeLastCharacter(String str) {
        if (str != null && str.length() > 0 && str.charAt(str.length() - 1) == ',') {
            str = str.substring(0, str.length() - 1);
        }
        return str;
    }

    public static int calculateNoOfColumns(Context context, float columnWidthDp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float screenWidthDp = displayMetrics.widthPixels / displayMetrics.density;
        return (int) (screenWidthDp / columnWidthDp + 0.5);
    }

    public static void rateApp(String appPackageName, Context context) {
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }

    public static void sendToGmail(Context context, String subject, String email) {
        try {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.CATEGORY_APP_EMAIL, true);
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
            final PackageManager pm = context.getPackageManager();
            @SuppressLint("QueryPermissionsNeeded") final List<ResolveInfo> matches = pm.queryIntentActivities(intent, 0);
            ResolveInfo best = null;
            for (final ResolveInfo info : matches)
                if (info.activityInfo.packageName.endsWith(".gm") || info.activityInfo.name.toLowerCase().contains("gmail"))
                    best = info;
            if (best != null)
                intent.setClassName(best.activityInfo.packageName, best.activityInfo.name);
            context.startActivity(intent);
        } catch (Exception e) {
            Log.e("TAG", "sendToGmail: " + e);
        }
    }

    public static void sendToWhatsApp(Context context) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://api.whatsapp.com/send?phone=+91" + SPData.whatsAppNumber() + "&text=&source=&data="));
            context.startActivity(intent);
        } catch (Exception e) {
            Log.e("TAG", "sendToWhatsApp: " + e);
        }
    }

    public static void sendToPhone(Context context, String mobile) {
        try {
            context.startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + mobile)));
        } catch (Exception e) {
            Log.e("TAG", "sendToPhone: " + e);
        }
    }

    public static void watchYoutubeVideo(Context context) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(SPData.getYoutube()));
            context.startActivity(intent);
        } catch (Exception e) {
            Log.e("TAG", "watchYoutubeVideo: " + e);
        }
    }

    public static void openWebsite(Context context) {
        openLink(context,SPData.websiteLink());
    }
    public static void openLink(Context context,String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            context.startActivity(intent);
        } catch (Exception e) {
            Log.e("TAG", "watchYoutubeVideo: " + e);
        }
    }
    public static void openLinkedIn(Context context) {
        try {
            Log.e("TAG", "openLinkedIn: "+SPData.getLinkedIn());
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(SPData.getLinkedIn()));
            context.startActivity(intent);
        } catch (Exception e) {
            Log.e("TAG", "watchYoutubeVideo: " + e);
        }
    }

    public static void newFacebookIntent(Context context) {
        try {
            Uri uri = Uri.parse(SPData.getFacebook());
            try {
                ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo("com.facebook.katana", 0);
                if (applicationInfo.enabled) {
                    uri = Uri.parse("fb://facewebmodal/f?href=" + SPData.getFacebook());
                }
            } catch (PackageManager.NameNotFoundException ignored) {
            }
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            context.startActivity(intent);
        } catch (Exception e) {
            Log.e("TAG", "newFacebookIntent: " + e);
        }
    }

    public static void openInstagram(Context context) {
        try {
            if (SPData.getInstagram() != null) {
                Uri uri = Uri.parse(SPData.getInstagram());
                Intent likeIng = new Intent(Intent.ACTION_VIEW, uri);
                likeIng.setPackage("com.instagram.android");
                try {
                    context.startActivity(likeIng);
                } catch (ActivityNotFoundException e) {
                    context.startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse(SPData.getInstagram())));
                }
            }
        } catch (Exception e) {
            Log.e("TAG", "openInstagram: " + e);
        }
    }

    @SuppressLint("CheckResult")
    public static RequestOptions getRequestOption() {
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.placeholder(R.drawable.noimage);
        requestOptions.error(R.drawable.noimage);
        return requestOptions;
    }

    public static String getFilePath(Context context, Uri uri) {
        String filePath = "";
        try {
            if (DocumentsContract.isDocumentUri(context, uri)) {
                String wholeID = DocumentsContract.getDocumentId(uri);
                String[] splits = wholeID.split(":");
                if (splits.length == 2) {
                    String id = splits[1];
                    String[] column = {MediaStore.Images.Media.DATA};
                    String sel = MediaStore.Images.Media._ID + "=?";
                    Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            column, sel, new String[]{id}, null);
                    int columnIndex = cursor.getColumnIndex(column[0]);
                    if (cursor.moveToFirst()) {
                        filePath = cursor.getString(columnIndex);
                    }
                    cursor.close();
                }
            } else {
                filePath = uri.getPath();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return filePath;
    }

    public static Date gmttoLocalDate(Date date) {
        String timeZone = Calendar.getInstance().getTimeZone().getID();
        return new Date(date.getTime() + TimeZone.getTimeZone(timeZone).getOffset(date.getTime()));
    }

    public static Date stringDateToGmt(String string) {
        Log.e("TAG", "stringDateToGmt: " + string);
        SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        try {
            return iso.parse(string);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String capitalizeWord(String str) {
        try {
            if (SPData.dontcaptializedWords()) {
                return str;
            } else if (str != null) {
                String[] words = str.split("\\s");
                StringBuilder capitalizeWord = new StringBuilder();
                for (String w : words) {
                    if (w.length() > 0) {
                        String first = w.substring(0, 1);
                        String afterfirst = w.substring(1);
                        if (first.equals("(")) {
                            afterfirst = capitalizeWord(afterfirst);
                        }
                        capitalizeWord.append(first.toUpperCase()).append(afterfirst).append(" ");
                    } else {
                        capitalizeWord.append(w).append(" ");
                    }
                }
                return capitalizeWord.toString().trim();
            } else {
                return "";
            }
        } catch (Exception e) {
            Log.e("TAG", "capitalizeWord: " + e);
            return str;
        }
    }

    public static String getTwoDigitsRoundOffValue(double value) {
        if (SPData.useDecimalPrices()) {
            double roundOffvalue = (double) Math.round(value * 100) / 100;
            if (("" + roundOffvalue).split("\\.")[1].length() == 2) {
                return "" + roundOffvalue;
            } else {
                return roundOffvalue + "0";
            }
        } else
            return ((int) value) + "";
    }

    public static String roundOffValue(double value) {
        double roundOffvalue = (double) Math.round(value * 100) / 100;
        if (("" + roundOffvalue).split("\\.")[1].length() == 2) {
            return "" + roundOffvalue;
        } else {
            return roundOffvalue + "0";
        }
    }

    public static CustomProgressDialog showProgressDialog(Context context, String message) {
        CustomProgressDialog mCustomeProgressDialog = new CustomProgressDialog(context, message);
        mCustomeProgressDialog.setCancelable();
        mCustomeProgressDialog.show();
        return mCustomeProgressDialog;
    }

    public static void hideProgressDialog(CustomProgressDialog mCustomeProgressDialog) {
        if (mCustomeProgressDialog != null && mCustomeProgressDialog.isShowing()) {
            mCustomeProgressDialog.dismiss();
        }
    }

    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void showSuccessMessage(Context context, String message) {
        MDToast.makeText(context, message, MDToast.LENGTH_SHORT, MDToast.TYPE_SUCCESS).show();
    }

    public static void showErrorMessage(Context context, String message) {
        MDToast.makeText(context, message, MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
    }

    public static void showInfoMessage(Context context, String message) {
        MDToast.makeText(context, message, MDToast.LENGTH_SHORT, MDToast.TYPE_INFO).show();
    }


    public GridLayoutManager getProductGridLayoutManager() {
        int prod_columns = calculateNoOfColumns(context, 200);
        return new GridLayoutManager(context, prod_columns);
    }

    public GridLayoutManager getCategoriesGridLayoutManager() {
        int cat_columns = CommonUtils.calculateNoOfColumns(context, 130);
        return new GridLayoutManager(context, cat_columns);
    }

    public void logout() {

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            try {
                FirebaseInstanceId.getInstance().deleteInstanceId();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("TAG", "logout: " + e);
            }
            handler.post(() -> {
                Log.e("TAG", "logout: done");
            });
        });

        if (SPData.getAppPreferences().signInByGoogle()) {
            mGoogleSignInClient.signOut()
                    .addOnCompleteListener((Activity) context, task -> {
                        if (task.isSuccessful()) {
                            logoutSuccessfull();
                        } else {
                            Toast.makeText(context, "" + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else if (SPData.getAppPreferences().signInByFacebook()) {
            if (AccessToken.getCurrentAccessToken() == null) {
                return;
            }
            new GraphRequest(AccessToken.getCurrentAccessToken(), "/me/permissions/", null, HttpMethod.DELETE, new GraphRequest.Callback() {
                @Override
                public void onCompleted(GraphResponse graphResponse) {
                    LoginManager.getInstance().logOut();
                    logoutSuccessfull();
                }
            }).executeAsync();
        } else
            logoutSuccessfull();

    }

    private void logoutSuccessfull() {
        SPData.getAppPreferences().clearAppPreference();
        String uniqueID = UUID.randomUUID().toString();
        SPData.getAppPreferences().setUserUniqueId(uniqueID);
        SPData.loginlogout = true;
        context.startActivity(new Intent(context, LoginActivity.class).putExtra("from", "logout"));
    }

    public List<Address> getAddress(final TextView textViewLocation) {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
            if (SPData.getAppPreferences().getLongitude().equals(""))
                fusedLocationClient.getLastLocation().addOnCompleteListener(task -> {
                    if (task.getResult() != null) {
                        double longitude = Objects.requireNonNull(task.getResult()).getLongitude();
                        double latitude = task.getResult().getLatitude();
                        SPData.getAppPreferences().setLatitude("" + latitude);
                        SPData.getAppPreferences().setLongitude("" + longitude);
                        setCurrentAddress(longitude, latitude, textViewLocation);
                    } else {
                        statusCheck();
                        if (textViewLocation != null)
                            textViewLocation.setText("-");
                        MDToast.makeText(context, "Unable to access location ", MDToast.LENGTH_SHORT, MDToast.TYPE_INFO).show();
                    }
                });
            else {
                double longitude = Double.parseDouble(SPData.getAppPreferences().getLongitude());
                double latitude = Double.parseDouble(SPData.getAppPreferences().getLatitude());
                setCurrentAddress(longitude, latitude, textViewLocation);
            }
        } else
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
        return addresses;
    }

    public void statusCheck() {
        final LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }
    }

    @SuppressLint("SetTextI18n")
    private void setCurrentAddress(double longitude, double latitude, TextView textViewLocation) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            String pincode = addresses.get(0).getPostalCode();
            String subLocality = addresses.get(0).getSubLocality();
            if (SPData.getAppPreferences().getAddress().isEmpty())
                SPData.getAppPreferences().setAddress(subLocality + "," + pincode);
            if (textViewLocation != null)
                textViewLocation.setText(SPData.getAppPreferences().getAddress());
            if (SPData.getAppPreferences().getPincode().isEmpty())
                SPData.getAppPreferences().setPincode(addresses.get(0).getPostalCode());
        } catch (Exception e) {
            e.printStackTrace();
            if (textViewLocation != null)
                textViewLocation.setText("Add address");
            String TAG = "CommonUtils";
            Log.e(TAG, "onComplete: " + e);
        }
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, id) -> {
                    dialog.cancel();
                    ((Activity) context).startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
                })
                .setNegativeButton("No", (dialog, id) -> dialog.cancel());
        final AlertDialog alert = builder.create();
        alert.show();
    }

}