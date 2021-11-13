package com.mindfulai.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.mindfulai.Activites.SearchPrdouctActivity
import com.mindfulai.Adapter.CategoryBannerSliderAdapter
import com.mindfulai.Adapter.ProductsAdapter
import com.mindfulai.Adapter.SubCategoriesAdapter
import com.mindfulai.Adapter.VendorsAdapter
import com.mindfulai.Models.BannerInfoData.BannerCategoryData
import com.mindfulai.Models.SubcategoryModel.SubcategoryModel
import com.mindfulai.Models.VendorBase
import com.mindfulai.Models.varientsByCategory.Datum
import com.mindfulai.Models.varientsByCategory.VarientsByCategory
import com.mindfulai.NetworkRetrofit.ApiUtils
import com.mindfulai.Utils.CommonUtils
import com.mindfulai.Utils.SPData
import com.mindfulai.customclass.PicassoImageLoadingService
import com.mindfulai.ministore.databinding.FragmentRamRajHomeBinding
import kotlinx.android.synthetic.main.fragment_ram_raj_home.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ss.com.bannerslider.Slider

class RamRajHomeFragment : Fragment() {


    private lateinit var bestSellingProductAdapter: ProductsAdapter
    var apiService = ApiUtils.getAPIService()
    lateinit var binding : FragmentRamRajHomeBinding
    private val bestSellingList: ArrayList<Datum> = ArrayList()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
       binding = FragmentRamRajHomeBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subcategoriesRecyclerView.layoutManager =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
        topvendorRecyclerView.layoutManager =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
        featured_productGrid.layoutManager = CommonUtils(activity).productGridLayoutManager
        featured_product_shimmerView.startShimmerAnimation()
        Slider.init(PicassoImageLoadingService(requireActivity().applicationContext))
        binding.bestSellingProductGrid.setLayoutManager(
            CommonUtils(
                activity
            ).productGridLayoutManager
        )
        bestSellingProductAdapter = ProductsAdapter(
            context,
            bestSellingList,
            "grid",
            CommonUtils.BEST_SELLING_PRODUCT_REQUEST_CODE
        )
        binding.bestSellingProductGrid.setAdapter(bestSellingProductAdapter)
        handOnclick()
    }

    fun handOnclick(){
       binding.edittextSearch2.setOnClickListener {
           startActivity(
               Intent(requireActivity(), SearchPrdouctActivity::class.java)
           )
       }
    }

    private fun getAllBestSelling() {
        bestSellingList.clear()
        binding.bestSellingShimmerView2.startShimmerAnimation()
        val apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().usertoken)
        apiService.getAllBestSelling(SPData.getAppPreferences().pincode)
            .enqueue(object : Callback<VarientsByCategory?> {
                override fun onResponse(
                    call: Call<VarientsByCategory?>,
                    response: Response<VarientsByCategory?>
                ) {
                    if (response.isSuccessful && response.body() != null && response.body()!!.data.size > 0) {
                        binding.bestSellingLayout.setVisibility(VISIBLE)
                        binding.bestSellingShimmerView2.setVisibility(GONE)
                        val productVarients = response.body()
                        productVarients?.data?.let { bestSellingList.addAll(it) }
                        bestSellingProductAdapter.notifyDataSetChanged()
                    } else {
                        binding.bestSellingLayout.setVisibility(GONE)
                    }
                }

                override fun onFailure(call: Call<VarientsByCategory?>, t: Throwable) {
                    Log.e("TAG", "" + t)
                    binding.bestSellingLayout.setVisibility(GONE)
                }
            })
    }


    fun loadHomePage(category: String) {
        getBanner(category)
        getSubCategory(category)
        getVendors(category)
        getAllFeaturedProducts(category)
        getAllBestSelling()
    }

    private fun getBanner(category: String) {
        card_view_banner_1.visibility = View.GONE
        shimmer_view_container.visibility = View.VISIBLE
        shimmer_view_container.startShimmerAnimation()
        apiService.getCategoryBannerData(category).enqueue(object : Callback<BannerCategoryData?> {
            override fun onResponse(
                call: Call<BannerCategoryData?>,
                response: Response<BannerCategoryData?>
            ) {
                if (response.isSuccessful && response.body() != null && response.body()!!.data.size > 0) {
                    hideCategoryShimmer()
                    card_view_banner_1.visibility = View.VISIBLE
                    banner_slider1.visibility= VISIBLE
                    banner_slider1.recyclerView.getRecycledViewPool().clear();
                    val bannerData = response.body()!!
                    val list = bannerData.data
                    val categoryBannerSliderAdapter =
                        CategoryBannerSliderAdapter(requireActivity(), list)
                    banner_slider1.setAdapter(categoryBannerSliderAdapter)
                    banner_slider1.setInterval(3000)
                } else {
                    card_view_banner_1.visibility = View.GONE
                    hideCategoryShimmer()
                }
            }

            override fun onFailure(call: Call<BannerCategoryData?>, t: Throwable) {
                hideCategoryShimmer()
                card_view_banner_1.visibility = View.GONE
                Toast.makeText(
                    requireActivity(),
                    "Failed to connect " + t.localizedMessage,
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun hideCategoryShimmer() {
        shimmer_view_container.stopShimmerAnimation()
        shimmer_view_container.visibility = View.GONE
    }

    private fun getSubCategory(category: String) {
        apiService.getAllSubCategory(category, SPData.getAppPreferences().pincode)
            .enqueue(object : Callback<SubcategoryModel?> {
                override fun onResponse(
                    call: Call<SubcategoryModel?>,
                    response: Response<SubcategoryModel?>
                ) {
                    if (response.isSuccessful) {
                        val subCategoryDetails = response.body()!!
                        if (subCategoryDetails.data.size > 0) {
                            subcategoriesRecyclerView.visibility = VISIBLE
                            val subcategoryList = subCategoryDetails.data
                            val subCategoriesAdapter =
                                SubCategoriesAdapter(requireActivity(), subcategoryList, 0)
                            subcategoriesRecyclerView.adapter = subCategoriesAdapter
                        } else {
                            subcategoriesRecyclerView.visibility = GONE
                        }
                    }
                }

                override fun onFailure(call: Call<SubcategoryModel?>, t: Throwable) {
                    Toast.makeText(
                        requireActivity(),
                        "Failed to connect " + t.localizedMessage,
                        Toast.LENGTH_SHORT
                    ).show()
                    subcategoriesRecyclerView.visibility = GONE
                }
            })
    }

    private fun getVendors(category: String) {
        apiService.getAllVendors(category,true).enqueue(object : Callback<VendorBase?> {
            override fun onResponse(call: Call<VendorBase?>, response: Response<VendorBase?>) {
                Log.e("TAG", "onResponse: "+response)

                if (response.isSuccessful && response.body() != null && response.body()!!.data.size > 0) {
                    if (activity != null) {
                        top_vendor_ll2.visibility = View.VISIBLE
                        val vendorChildArrayList = response.body()!!.data
                        val vendorsAdapter = VendorsAdapter(requireActivity(), vendorChildArrayList)
                        topvendorRecyclerView.adapter = vendorsAdapter
                    }
                } else {
                    hideVendors()
                }
            }

            override fun onFailure(call: Call<VendorBase?>, t: Throwable) {
                hideVendors()
            }
        })
    }

    private fun hideVendors() {
        top_vendor_ll2.visibility = GONE
    }

    private fun getAllFeaturedProducts(category: String) {
        featured_product_shimmerView.visibility = VISIBLE
        featured_product_shimmerView.startShimmerAnimation()
        featured_productGrid.visibility = GONE
        val apiService = ApiUtils.getHeaderAPIService(SPData.getAppPreferences().usertoken)
        apiService.getAllFeaturedProducts(SPData.getAppPreferences().pincode,category)
            .enqueue(object : Callback<VarientsByCategory?> {
                override fun onResponse(
                    call: Call<VarientsByCategory?>,
                    response: Response<VarientsByCategory?>
                ) {
                    Log.e("TAG", "onResponse: "+response)
                    Log.e("TAG", "onResponse: "+response.body())
                    if (response.isSuccessful && response.body() != null && response.body()!!.data.size > 0) {
                        hideFeaturedProductShimmer()
                        val productVarients = response.body()
                        val productList = productVarients!!.data
                        Log.e("TAG", "getAllFeaturedProducts onResponse: "+productList.size)
                        featured_productGrid.visibility = VISIBLE
                        val topfeaturedProductAdapter = ProductsAdapter(
                            context,
                            productList,
                            "grid",
                            CommonUtils.TOP_FEATURED_PRODUCTS_REQUEST_CODE
                        )
                        featured_productGrid.adapter = topfeaturedProductAdapter
                    } else {

                        featured_productGrid.visibility = GONE
                        hideFeaturedProductShimmer()
                    }
                }

                override fun onFailure(call: Call<VarientsByCategory?>, t: Throwable) {
                    featured_productGrid.visibility = GONE
                    Log.e("TAG", "onFailure: "+t.message)
                    hideFeaturedProductShimmer()
                    Toast.makeText(
                        requireActivity(),
                        "Failed to connect " + t.localizedMessage,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

   private fun hideFeaturedProductShimmer() {
        featured_product_shimmerView.stopShimmerAnimation()
        featured_product_shimmerView.visibility = GONE
    }

}