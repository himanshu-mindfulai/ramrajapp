package com.mindfulai.ui

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.mindfulai.Models.CustomerInfo.CustomerData
import com.mindfulai.NetworkRetrofit.ApiUtils
import com.mindfulai.Utils.CommonUtils
import com.mindfulai.Utils.SPData
import com.mindfulai.ministore.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ShareAppBottomSheet : BottomSheetDialogFragment() {
    lateinit var tvCode: TextView
    lateinit var tvCopy: TextView
    lateinit var btShare: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.share_app_bs_layout, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvCode = view.findViewById(R.id.code)
        tvCopy = view.findViewById(R.id.copy)
        btShare = view.findViewById(R.id.share)
        tvCopy.visibility = View.INVISIBLE
        btShare.visibility = View.INVISIBLE
        tvCode.text = "Generating unique code..."
        Log.e("TAG", "onViewCreated: "+SPData.getAppPreferences().referralCode)
        val invitationLink = context?.getString(R.string.linkUrl)+"invitedby=${SPData.getAppPreferences().referralCode}"
        FirebaseDynamicLinks
            .getInstance()
            .createDynamicLink()
            .setLink(Uri.parse(invitationLink))
            .setDomainUriPrefix(SPData.domainUriPrefix())
            .setAndroidParameters(DynamicLink.AndroidParameters.Builder().build())
            .buildShortDynamicLink()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.e("TAG", "onResponse: success" )
                    val shortLink = task.result?.shortLink
                    tvCode.text = shortLink.toString()
                    tvCopy.visibility = View.VISIBLE
                    btShare.visibility = View.VISIBLE
                    tvCopy.setOnClickListener {
                        val clipboardManager = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val data = ClipData.newPlainText("Share app", "${SPData.appShareMsg()+" "} ${tvCode.text}")
                        clipboardManager.setPrimaryClip(data)
                        Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                    }
                    btShare.setOnClickListener {
                        context?.startActivity(
                            Intent.createChooser(
                                Intent(Intent.ACTION_SEND)
                                    .setType("text/plain")
                                    .putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
                                    .putExtra(Intent.EXTRA_TEXT, SPData.appShareMsg() + " ${tvCode.text}"),
                                "Share"
                            )
                        )
                    }
                } else {
                    Log.e("TAG", task.exception.toString())
                }
            }
    }
}