package com.mindfulai.Activites;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.gson.JsonObject;
import com.mindfulai.Adapter.UserAddressesProfileAdapter;
import com.mindfulai.AppPrefrences.AppPreferences;
import com.mindfulai.Models.CustomerInfo.CustomerData;
import com.mindfulai.Models.UserBaseAddress;
import com.mindfulai.Models.UserDataAddress;
import com.mindfulai.NetworkRetrofit.ApiService;
import com.mindfulai.NetworkRetrofit.ApiUtils;
import com.mindfulai.Utils.CommonUtils;
import com.mindfulai.Utils.CustomProgressDialog;
import com.mindfulai.Utils.SPData;
import com.mindfulai.ministore.R;
import com.valdesekamdem.library.mdtoast.MDToast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final int REQUEST_CODE_OPEN_DOCUMENT_TREE = 2;
    final int cameraAndGalleryIntent = 100;
    Boolean isprofilePicChanges = false;
    Uri galleryImageUri;
    Boolean isGallery = false;
    File selectedFile;
    private EditText et_name, et_email,et_gst,et_alternate_number;
    private String name;
    private String profile;
    private CircleImageView profile_image;
    private MultipartBody.Part filePart;
    private AppPreferences appPreferences;
    private RecyclerView recyclerViewAddress;
    private ArrayList<UserDataAddress> userDataAddressArrayList;
    private UserAddressesProfileAdapter addressesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        try {
            appPreferences = new AppPreferences(this);
            setToolbar();
            et_name = findViewById(R.id.et_name);
            et_email = findViewById(R.id.et_email);
            et_gst = findViewById(R.id.et_gst);
            et_alternate_number= findViewById(R.id.et_alternate_number);

            Button tv_continue = findViewById(R.id.tv_continue);
            profile_image = findViewById(R.id.profile_image);
            userDataAddressArrayList = new ArrayList<>();
            TextView addAddress = findViewById(R.id.add_address);

            et_gst.setHint(SPData.postalCodeTitle());

            checkSPData();

            addAddress.setOnClickListener(v -> {
                if (!SPData.getAppPreferences().getUsertoken().equals("")) {
                    startActivityForResult(new Intent(ProfileActivity.this, AddAddressActivity.class).putExtra("title", "Add Address"), 3);
                } else {
                    Toast.makeText(ProfileActivity.this, "Please login to add address", Toast.LENGTH_SHORT).show();
                    startActivityForResult(new Intent(ProfileActivity.this, LoginActivity.class), 3);
                }
            });

            recyclerViewAddress = findViewById(R.id.recycler_view_address);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ProfileActivity.this);
            recyclerViewAddress.setLayoutManager(linearLayoutManager);

            tv_continue.setOnClickListener(v -> {
                if (!SPData.getAppPreferences().getUsertoken().equals("")) {
                    if (!et_name.getText().toString().replaceAll(" ", "").isEmpty()
                            && !et_email.getText().toString().isEmpty()) {
                        try {
                            uploadImage();
                        } catch (Exception e) {
                            Toast.makeText(ProfileActivity.this, "" + e, Toast.LENGTH_SHORT).show();
                        }
                    } else
                        MDToast.makeText(ProfileActivity.this, "Name or email can't be empty", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();

                } else {
                    Toast.makeText(ProfileActivity.this, "Please login to save profile", Toast.LENGTH_SHORT).show();
                    startActivityForResult(new Intent(ProfileActivity.this, LoginActivity.class), 3);
                }
            });
            profile_image.setOnClickListener(v -> {
                if (checkPermission()) {
                    askForGalleryAndCamera();
                } else {
                    requestPermission();
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "onCreate: " + e);
        }
    }

    private void checkSPData() {
        if (!SPData.getAppPreferences().getUsertoken().equals("")) {
            showProfileData();
            getAllAddress();
        }
        if(SPData.useGstinProfile()){
            et_gst.setVisibility(VISIBLE);
        }else {
            et_gst.setVisibility(GONE);
        }
    }

    @Override
    public void onBackPressed() {
        if (SPData.showProfileScreenAfterLogin() && getIntent().getBooleanExtra("from", false)) {
            startActivity(new Intent(ProfileActivity.this, MainActivity.class).putExtra("from", true));
        }
        finish();
    }

    private void getAllAddress() {
        final CustomProgressDialog customProgressDialog = CommonUtils.showProgressDialog(ProfileActivity.this,
                "Getting addresses ... ");
        ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());
        apiService.getUserBaseAddress().enqueue(new Callback<UserBaseAddress>() {
            @Override
            public void onResponse(@NonNull Call<UserBaseAddress> call, @NonNull Response<UserBaseAddress> response) {
                if (response.isSuccessful()) {
                    CommonUtils.hideProgressDialog(customProgressDialog);
                    assert response.body() != null;
                    userDataAddressArrayList = response.body().getData();
                    addressesAdapter = new UserAddressesProfileAdapter(ProfileActivity.this, userDataAddressArrayList);
                    recyclerViewAddress.setAdapter(addressesAdapter);
                    addressesAdapter.notifyDataSetChanged();
                } else {
                    CommonUtils.hideProgressDialog(customProgressDialog);
                    Log.e(TAG, "onResponse: " + response);
                }
            }

            @Override
            public void onFailure(Call<UserBaseAddress> call, Throwable t) {
                CommonUtils.hideProgressDialog(customProgressDialog);
                Log.e(TAG, "onFailure: " + t.getMessage());
            }
        });
    }

    private void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_top);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_white);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        String title = "Profile";
        toolbar.setTitle("" + title);
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorWhite));
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(ProfileActivity.this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

        int resultReadExternal = ContextCompat.checkSelfPermission(ProfileActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED && resultReadExternal == PackageManager.PERMISSION_GRANTED;
    }

    private void askForGalleryAndCamera() {

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        Intent chooser = Intent.createChooser(galleryIntent, "Some text here");
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{cameraIntent});
        startActivityForResult(chooser, cameraAndGalleryIntent);
    }

    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(ProfileActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(ProfileActivity.this, " Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
            openPermissionIntent();
        } else {
            ActivityCompat.requestPermissions(ProfileActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE);
        }
    }

    private void openPermissionIntent() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Audio.Media.DATA};
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_CANCELED) {
            if (requestCode == cameraAndGalleryIntent) {

                if (data != null) {
                    isprofilePicChanges = true;

                    if (data.getData() != null) {
                        handleGalleryData(data);
                    } else {
                        handleCameraData(data);
                    }

                }

            } else if (requestCode == REQUEST_CODE_OPEN_DOCUMENT_TREE) {
                if (resultCode == Activity.RESULT_OK) {
                    Uri treeUri = data.getData();
                    int takeFlags = data.getFlags();
                    takeFlags &= (Intent.FLAG_GRANT_READ_URI_PERMISSION |
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    assert treeUri != null;
                    this.getContentResolver().takePersistableUriPermission(treeUri, takeFlags);

                }
            }
        } else if (requestCode == 3) {
            if (!SPData.getAppPreferences().getUsertoken().equals("")) {
                showProfileData();
                getAllAddress();
            }
        }
    }

    private void handleGalleryData(Intent data) {
        isGallery = true;
        Uri uri = data.getData();
        Glide.with(ProfileActivity.this).load(uri).into(profile_image);
        galleryImageUri = uri;
        selectedFile = new File(getRealPathFromURI(uri));
    }

    private void handleCameraData(Intent data) {


        Bitmap thumbnail = (Bitmap) Objects.requireNonNull(data.getExtras()).get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        assert thumbnail != null;
        thumbnail.compress(Bitmap.CompressFormat.PNG, 90, bytes);
        File destination = new File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis() + ".png");
        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        profile_image.setImageBitmap(thumbnail);

        selectedFile = destination;
    }

    private void showProfileData() {

        ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());

        apiService.getProfileDetails().enqueue(new Callback<CustomerData>() {
            @Override
            public void onResponse(@NonNull Call<CustomerData> call, @NonNull Response<CustomerData> response) {
                try {
                    if (response.isSuccessful()&&response.body()!=null) {
                        CustomerData customerData;
                        customerData = response.body();
                        SPData.getAppPreferences().setUserName(customerData.getData().getUser().getFullName());
                        SPData.getAppPreferences().setUserProfilePic(customerData.getData().getUser().getProfilePicture());
                        SPData.getAppPreferences().setUser_mobile_no(customerData.getData().getUser().getMobileNumber());
                        SPData.getAppPreferences().setEmail(customerData.getData().getUser().getEmail());
                        name = customerData.getData().getUser().getFullName();
                        et_name.setText(CommonUtils.capitalizeWord(name));
                        et_email.setText(SPData.getAppPreferences().getEmail());
                        et_alternate_number.setText(customerData.getData().getUser().getAlternateNumber());
                        et_gst.setText(customerData.getData().getUser().getGstin());

                        profile = customerData.getData().getUser().getProfilePicture();
                        if (profile != null && !profile.isEmpty()) {
                            Glide.with(ProfileActivity.this).load(SPData.getBucketUrl() + profile).into(profile_image);
                        } else {
                            Glide.with(ProfileActivity.this).load(getResources().getDrawable(R.drawable.user)).into(profile_image);
                        }
                    } else {
                        Toast.makeText(ProfileActivity.this, "Try again", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(@NonNull Call<CustomerData> call, @NonNull Throwable t) {
                Log.e("fail", call.toString());
                Toast.makeText(ProfileActivity.this, "Failed to connect. " + " Please reload", Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void uploadImage() {


        final CustomProgressDialog customProgressDialog = CommonUtils.showProgressDialog(ProfileActivity.this, "Uploading Image.. ");
        ApiService apiService = ApiUtils.getImageAPIService(SPData.getAppPreferences().getUsertoken());
        RequestBody requestFile = null;
        if (selectedFile != null) {
            try {
                if (isGallery) {
                    requestFile = RequestBody.create(
                            MediaType.parse(Objects.requireNonNull(getContentResolver().getType(galleryImageUri))),
                            selectedFile
                    );
                } else {
                    requestFile = RequestBody.create(
                            MediaType.parse(Objects.requireNonNull(getContentResolver().getType(convertFileToContentUri(selectedFile)))),
                            selectedFile
                    );
                }

            } catch (Exception e) {
                e.printStackTrace();
                Log.e("TAG", "uploadImage: " + e);
            }

            if (requestFile != null) {
                filePart = MultipartBody.Part.createFormData("profile_picture", selectedFile.getName(), requestFile);
            }
        }

        RequestBody name = RequestBody.create(MediaType.parse("text/plain"), et_name.getText().toString());
        RequestBody email = RequestBody.create(MediaType.parse("text/plain"), et_email.getText().toString());
        RequestBody gstin = RequestBody.create(MediaType.parse("text/plain"), et_gst.getText().toString());
        RequestBody alternateNo = RequestBody.create(MediaType.parse("text/plain"), et_alternate_number.getText().toString());
        RequestBody mobileNumber = RequestBody.create(MediaType.parse("text/plain"), appPreferences.getMobileNumber());
        Call<JsonObject> call = apiService.uploadFile(filePart, name, email, mobileNumber,gstin,alternateNo);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                CommonUtils.hideProgressDialog(customProgressDialog);
                if (response.isSuccessful()&&response.body()!=null) {
                    SPData.getAppPreferences().setUserName("" + et_name.getText().toString());
                    SPData.getAppPreferences().setEmail("" + et_email.getText().toString());
                    if(response.body().has("data")&&response.body().getAsJsonObject("data")!=null&&response.body().has("profile_picture"))
                    SPData.getAppPreferences().setUserProfilePic(response.body().getAsJsonObject("data").get("profile_picture").toString());
                    Toast.makeText(ProfileActivity.this, "Profile updated successfully.", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Log.e("TAG", "onResponse: " + response);
                    Toast.makeText(ProfileActivity.this, "" + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                CommonUtils.hideProgressDialog(customProgressDialog);
                Log.e("TAG", "onFailure: " + t.getMessage());
            }
        });
    }

    protected Uri convertFileToContentUri(File file) throws Exception {
        ContentResolver cr = getContentResolver();
        String imagePath = file.getAbsolutePath();
        String imageName = null;
        String imageDescription = null;
        String uriString = MediaStore.Images.Media.insertImage(cr, imagePath, imageName, imageDescription);
        return Uri.parse(uriString);
    }
}