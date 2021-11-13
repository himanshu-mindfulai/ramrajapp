package com.mindfulai.Activites;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.app.akplacepicker.models.AddressData;
import com.app.akplacepicker.utilities.Constants;
import com.app.akplacepicker.utilities.PlacePicker;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mindfulai.Models.UserDataAddress;
import com.mindfulai.Models.society.SocietyBase;
import com.mindfulai.Models.society.SocietyData;
import com.mindfulai.NetworkRetrofit.ApiService;
import com.mindfulai.NetworkRetrofit.ApiUtils;
import com.mindfulai.Utils.CommonUtils;
import com.mindfulai.Utils.CustomProgressDialog;
import com.mindfulai.Utils.SPData;
import com.mindfulai.customclass.CityData;
import com.mindfulai.ministore.R;
import com.mindfulai.ministore.databinding.ActivityAddAddress2Binding;
import com.valdesekamdem.library.mdtoast.MDToast;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Callback;

import static android.view.View.GONE;

public class AddAddressActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    FusedLocationProviderClient fusedLocationClient;
    private CityData cityData;
    private CustomProgressDialog customProgressDialog;
    private double lat, lon;
    private String houseno, street, city, state, postalCode, name, phone, selectedSociety = "";
    private ActivityAddAddress2Binding binding;

    @SuppressLint("InflateParams")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater layoutInflater = getLayoutInflater();
        layoutInflater.inflate(R.layout.activity_add_address2, null);
        binding = ActivityAddAddress2Binding.inflate(layoutInflater);
        setContentView(binding.getRoot());
        setToolbar();
        cityData = new CityData();

        if (SPData.getAppPreferences().getUsertoken().isEmpty() && !SPData.forceToAddAddress()) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(AddAddressActivity.this, LoginActivity.class));
            finish();
        } else {
            if (Objects.equals(getIntent().getStringExtra("title"), "Add Address") && SPData.selectLocationFromMapDirectly()) {
                if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(AddAddressActivity.this, Manifest.permission.ACCESS_FINE_LOCATION))
                    pickAddressFromMap();
                else
                    ActivityCompat.requestPermissions(AddAddressActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }


        binding.etName.setHint(SPData.getNameHintInAddAddress());
        binding.etPostalAddress.setHint(SPData.postalCodeTitle());
        binding.etCityAddress.setHint(SPData.cityTitle());
        ArrayAdapter arrayAdapter = new ArrayAdapter(AddAddressActivity.this, R.layout.dropdown_item, cityData.getState());
        arrayAdapter.setDropDownViewResource(R.layout.dropdown_item);
        binding.etStateAddress.setAdapter(arrayAdapter);

        binding.etPhoneNo.setText(SPData.countryCode());
        binding.etHouseNum.setHint(SPData.houseNoHint());
        Selection.setSelection(binding.etPhoneNo.getText(), binding.etPhoneNo.getText().length());
        binding.etPhoneNo.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().startsWith(SPData.countryCode())) {
                    binding.etPhoneNo.setText(SPData.countryCode());
                    Selection.setSelection(binding.etPhoneNo.getText(), binding.etPhoneNo.getText().length());
                }
            }
        });
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (!Objects.equals(getIntent().getStringExtra("title"), "Add Address"))
            setUserAddress();
        else {
            binding.etName.setText(SPData.getAppPreferences().getUserName());
            binding.etPhoneNo.setText(SPData.getAppPreferences().getMobileNumber());
            binding.etPostalAddress.setText(SPData.getAppPreferences().getPincode());
        }

        binding.pickLocationFromMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(AddAddressActivity.this, Manifest.permission.ACCESS_FINE_LOCATION))
                    pickAddressFromMap();
                else
                    ActivityCompat.requestPermissions(AddAddressActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
            }
        });
        binding.tvContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!SPData.getAppPreferences().getUsertoken().isEmpty()) {
                    if (validation()) {
                        customProgressDialog = CommonUtils.showProgressDialog(AddAddressActivity.this, "Please wait.. ");
                        if (getIntent().getStringExtra("title") != null && getIntent().getStringExtra("title").equals("Add Address")) {
                            saveUpdateAddress(null);
                        } else {
                            String id = getIntent().getStringExtra("id");
                            saveUpdateAddress(id);
                        }
                    }
                } else {
                    Toast.makeText(AddAddressActivity.this, "Please login to save address", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(AddAddressActivity.this, LoginActivity.class));
                }
            }
        });
        binding.spinnerSociety.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedSociety = ((TextView) view).getText().toString();
                Log.e("TAG", "onItemSelected: " + selectedSociety);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        if (SPData.useSocietyInAddress())
            getSociety();
        else {
            hideSociety();
        }
    }

    private void getSociety() {
        ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());
        apiService.getSociety().enqueue(new Callback<SocietyBase>() {
            @Override
            public void onResponse(@NotNull Call<SocietyBase> call, @NotNull retrofit2.Response<SocietyBase> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ArrayList<SocietyData> societyDataArrayList = response.body().getData();
                    if (societyDataArrayList.size() > 0) {
                        ArrayList<String> stringArrayList = new ArrayList<>();
                        for (SocietyData societyData : societyDataArrayList) {
                            stringArrayList.add(societyData.getName());
                        }
                        Collections.sort(stringArrayList, new Comparator<String>() {
                            @Override
                            public int compare(String o1, String o2) {
                                return o1.compareToIgnoreCase(o2);
                            }
                        });
                        stringArrayList.add(0, "Select your society");
                        ArrayAdapter arrayAdapter = new ArrayAdapter(AddAddressActivity.this, R.layout.dropdown_item, stringArrayList);
                        arrayAdapter.setDropDownViewResource(R.layout.dropdown_item);
                        binding.spinnerSociety.setAdapter(arrayAdapter);
                        binding.spinnerSociety.setVisibility(View.VISIBLE);
                        binding.spinnerSocietyTitle.setVisibility(View.VISIBLE);
                    } else {
                        hideSociety();
                    }
                } else {
                    hideSociety();
                }

            }

            @Override
            public void onFailure(@NotNull Call<SocietyBase> call, @NotNull Throwable t) {
                hideSociety();
            }
        });
    }

    private void hideSociety() {
        binding.spinnerSociety.setVisibility(GONE);
        binding.spinnerSocietyTitle.setVisibility(GONE);
    }

    private void pickAddressFromMap() {
        PlacePicker.IntentBuilder placeBuilder = new PlacePicker.IntentBuilder();
        placeBuilder.setGoogleMapApiKey(getString(R.string.maps_api_key));
        if (!SPData.getAppPreferences().getLatitude().isEmpty())
            placeBuilder.setLatLong(Double.parseDouble(SPData.getAppPreferences().getLatitude()), Double.parseDouble(SPData.getAppPreferences().getLongitude()));
        placeBuilder.setMapZoom(20.0f);
        placeBuilder.setAddressRequired(true);
        placeBuilder.setPrimaryTextColor(R.color.black);
        Intent intent = placeBuilder.build(this);
        startActivityForResult(intent, Constants.PLACE_PICKER_REQUEST);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.PLACE_PICKER_REQUEST) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                AddressData addressData = data.getParcelableExtra(Constants.ADDRESS_INTENT);
                lat = addressData.getLatitude();
                lon = addressData.getLongitude();
                Geocoder geocoder = new Geocoder(AddAddressActivity.this, Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
                    String houseno = addresses.get(0).getAddressLine(0).split(",")[0];
                    if (houseno != null)
                        binding.etHouseNum.setText(houseno.replaceAll("null", ""));


                    String street = addresses.get(0).getSubLocality();
                    if (street != null && addressData.getPlaceName() != null)
                        binding.etStreetAddress.setText(street.replaceAll("null", "") + " " + addressData.getPlaceName().replaceAll("null", ""));
                    else if (street != null) {
                        binding.etStreetAddress.setText(street.replaceAll("null", ""));
                    } else if (addressData.getPlaceName() != null) {
                        binding.etStreetAddress.setText(addressData.getPlaceName());
                    }

                    binding.etCityAddress.setText(addresses.get(0).getSubAdminArea());
                    int index = cityData.getState().indexOf(addresses.get(0).getAdminArea());
                    if (index >= 0)
                        binding.etStateAddress.setSelection(index);
                    binding.etPostalAddress.setText(addresses.get(0).getPostalCode());
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "" + e, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @SuppressLint({"MissingPermission", "MissingSuperCall"})
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {
            if (permissions.length > 0 && permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION) &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(AddAddressActivity.this);
                fusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.getResult() != null) {
                            double longitude = Objects.requireNonNull(task.getResult()).getLongitude();
                            double latitude = task.getResult().getLatitude();
                            SPData.getAppPreferences().setLatitude("" + latitude);
                            SPData.getAppPreferences().setLongitude("" + longitude);
                            pickAddressFromMap();
                        } else {
                            MDToast.makeText(AddAddressActivity.this, "Unable to access location ", MDToast.LENGTH_SHORT, MDToast.TYPE_INFO).show();
                        }
                    }
                });
            } else {
                new CommonUtils(this).getAddress(null);
            }
        }
    }

    private void setUserAddress() {
        UserDataAddress userDataAddress = getIntent().getParcelableExtra("address");
        if (userDataAddress != null) {
            if (userDataAddress.getLocationModel() != null) {
                lon = userDataAddress.getLocationModel().getCoordinates()[0];
                lat = userDataAddress.getLocationModel().getCoordinates()[1];
            }
            binding.etHouseNum.setText(userDataAddress.getAddressLine1());
            binding.etStreetAddress.setText(userDataAddress.getAddressLine2());
            binding.etCityAddress.setText(userDataAddress.getCity());
            binding.etStateAddress.setSelection(cityData.getState().indexOf(userDataAddress.getState()));
            binding.etPostalAddress.setText(userDataAddress.getPincode());
            if (userDataAddress.getMobile_number().startsWith(SPData.countryCode()))
                binding.etPhoneNo.setText(userDataAddress.getMobile_number());
            else
                binding.etPhoneNo.setText(SPData.countryCode() + userDataAddress.getMobile_number());
            binding.etName.setText(userDataAddress.getName());
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            handleBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        handleBackPressed();
    }

    private void handleBackPressed() {
        if (SPData.forceToAddAddress() && SPData.getAppPreferences().getPincode().isEmpty()) {
            Toast.makeText(this, "Address is required", Toast.LENGTH_SHORT).show();
        } else
            finish();
    }

    private void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_top);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_white);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        String title = getIntent().getStringExtra("title");
        toolbar.setTitle("" + title);
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorWhite));
    }


    private boolean validation() {
        //  String type = et_type.getText().toString();
        houseno = binding.etHouseNum.getText().toString();
        street = binding.etStreetAddress.getText().toString();
        city = binding.etCityAddress.getText().toString();
        state = binding.etStateAddress.getSelectedItem().toString();
        postalCode = binding.etPostalAddress.getText().toString();
        name = binding.etName.getText().toString();
        phone = binding.etPhoneNo.getText().toString();

        if (TextUtils.isEmpty(houseno.replaceAll(" ", ""))) {
            Toast.makeText(this, "House No can't be empty !!", Toast.LENGTH_SHORT).show();
            return false;
        } else if (TextUtils.isEmpty(street.replaceAll(" ", ""))) {
            Toast.makeText(this, "Street Address can't be empty !!", Toast.LENGTH_SHORT).show();
            return false;
        } else if (TextUtils.isEmpty(city.replaceAll(" ", ""))) {
            Toast.makeText(this, SPData.cityTitle() + " can't be empty !!", Toast.LENGTH_SHORT).show();
            return false;
        } else if (binding.etStateAddress.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Select State", Toast.LENGTH_SHORT).show();
            return false;
        } else if (TextUtils.isEmpty(postalCode)) {
            Toast.makeText(this, SPData.postalCodeTitle() + " field can't be empty !!", Toast.LENGTH_SHORT).show();
            return false;
        } else if (postalCode.length() != 6) {
            Toast.makeText(this, "Invalid " + SPData.postalCodeTitle(), Toast.LENGTH_SHORT).show();
            return false;
        } else if (name.replaceAll(" ", "").isEmpty()) {
            Toast.makeText(this, "Name can't be empty", Toast.LENGTH_SHORT).show();
            return false;
        } else if (phone.isEmpty()) {
            Toast.makeText(this, "Mobile no is required", Toast.LENGTH_SHORT).show();
            return false;
        } else if (phone.length() < 10) {
            Toast.makeText(this, "Invalid mobile number", Toast.LENGTH_SHORT).show();
            return false;
        } else if (binding.spinnerSociety.getSelectedItemPosition() == 0 && SPData.useSocietyInAddress()) {
            Toast.makeText(this, "Please select your society", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void saveUpdateAddress(String addressID) {
        try {
            JsonObject params = new JsonObject();
            JsonArray jsonArray = new JsonArray();
            jsonArray.add(lon);
            jsonArray.add(lat);
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("type", "Point");
            jsonObject.add("coordinates", jsonArray);
            params.addProperty("addressLine1", houseno);
            params.addProperty("addressLine2", street);
            params.addProperty("city", city);
            params.addProperty("state", state);
            params.addProperty("pincode", postalCode);
            params.addProperty("name", name);
            params.add("location", jsonObject);
            params.addProperty("mobile_number", phone);
            if (!selectedSociety.isEmpty())
                params.addProperty("society", selectedSociety);
            ApiService apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().getUsertoken());
            if (addressID == null)
                apiService.saveAddress(params).enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NotNull Call<JsonObject> call, @NotNull retrofit2.Response<JsonObject> response) {
                        CommonUtils.hideProgressDialog(customProgressDialog);
                        handleAddressResponse(call, response);
                    }

                    @Override
                    public void onFailure(@NotNull Call<JsonObject> call, @NotNull Throwable t) {
                        CommonUtils.hideProgressDialog(customProgressDialog);

                    }
                });
            else
                apiService.updateAddress(addressID, params).enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NotNull Call<JsonObject> call, @NotNull retrofit2.Response<JsonObject> response) {
                        CommonUtils.hideProgressDialog(customProgressDialog);
                        if (response.isSuccessful() && response.body() != null) {
                            Toast.makeText(AddAddressActivity.this, "" + response.body().get("message").getAsString(), Toast.LENGTH_SHORT).show();
                            if (!response.body().get("errors").getAsBoolean()) {
                                finish();
                            }
                        } else {
                            Toast.makeText(AddAddressActivity.this, "" + response.message(), Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onFailure(@NotNull Call<JsonObject> call, @NotNull Throwable t) {
                        CommonUtils.hideProgressDialog(customProgressDialog);
                    }
                });
        } catch (Exception e) {
            Log.e("TAG", "saveAddress: " + e);
            Toast.makeText(AddAddressActivity.this, "Could not connect to our server please try again later", Toast.LENGTH_SHORT).show();
        }

    }

    private void handleAddressResponse(Call<JsonObject> call, retrofit2.Response<JsonObject> response) {
        if (response.isSuccessful() && response.body() != null) {
            Toast.makeText(AddAddressActivity.this, "" + response.body().get("message").getAsString(), Toast.LENGTH_SHORT).show();
            String id = response.body().get("data").getAsJsonObject().get("_id").getAsString();
            SPData.getAppPreferences().setPincode("" + postalCode);
            SPData.getAppPreferences().setAddressId(id);
            SPData.getAppPreferences().setUserShippingCoordinates(lon + "," + lat);
            SPData.getAppPreferences().setAddress(street + ", " + postalCode);
            if (name != null)
                SPData.getAppPreferences().setUserShippingName(name);
            if (phone != null)
                SPData.getAppPreferences().setUserShippingMobile(phone);
            String comma = ", ";
            if (selectedSociety.isEmpty()) {
                comma = "";
            }
            SPData.getAppPreferences().setUserShippingAddress(houseno + "\n" + street + comma + selectedSociety + "\n" + city + ", " + state + "\n" + postalCode);
            if (getIntent().getBooleanExtra("fromlogin", false)) {
                startActivity(new Intent(AddAddressActivity.this, MainActivity.class).putExtra("from", true));
            }
            finish();
        } else {
            Toast.makeText(AddAddressActivity.this, "" + response.message(), Toast.LENGTH_SHORT).show();
        }
    }
}
