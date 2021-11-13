package com.mindfulai.Activites

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.mindfulai.Adapter.SelectAddressAdapter
import com.mindfulai.Models.UserBaseAddress
import com.mindfulai.Models.UserDataAddress
import com.mindfulai.NetworkRetrofit.ApiUtils
import com.mindfulai.Utils.CommonUtils
import com.mindfulai.Utils.SPData
import com.mindfulai.ministore.R
import com.mindfulai.ministore.databinding.ActivityAddressSelectorBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddressSelectorActivity : AppCompatActivity() {

    lateinit var binding: ActivityAddressSelectorBinding
    lateinit var addressesList: List<UserDataAddress>
    public var cartPage = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddressSelectorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = "Select a address"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        cartPage = intent.getBooleanExtra("cartpage", false)
        addressesList = ArrayList();
        binding.rvAddresses.layoutManager = LinearLayoutManager(this)
        binding.btAddAddress.setOnClickListener {
            startActivity(Intent(
                    this, AddAddressActivity::class.java
            ).putExtra("title", "Add Address"))
        }
    }

    override fun onResume() {
        super.onResume()
        getAddresses()
    }

    private fun getAddresses() {
        var customProgressDialog = CommonUtils.showProgressDialog(baseContext, "Getting addresses...")
        customProgressDialog.show()
        var apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().usertoken)
        apiService.userBaseAddress.enqueue(object : Callback<UserBaseAddress> {
            override fun onFailure(call: Call<UserBaseAddress>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<UserBaseAddress>, response: Response<UserBaseAddress>) {
                if (response.isSuccessful) {
                    CommonUtils.hideProgressDialog(customProgressDialog)
                    addressesList = response.body()?.data!!
                    if (addressesList.isEmpty()) {
                        binding.noItemLayout.visibility = View.VISIBLE
                        binding.rvAddresses.visibility = View.GONE
                    } else {
                        binding.noItemLayout.visibility = View.GONE
                        binding.rvAddresses.visibility = View.VISIBLE
                        var selectAddressAdapter = SelectAddressAdapter(this@AddressSelectorActivity, addressesList, this@AddressSelectorActivity)
                        binding.rvAddresses.adapter = selectAddressAdapter
                    }
                }
            }

        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

}