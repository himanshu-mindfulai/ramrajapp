package com.mindfulai.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mindfulai.Adapter.NotificationAdapter
import com.mindfulai.AppPrefrences.AppPreferences
import com.mindfulai.Models.NotificationModel
import com.mindfulai.Utils.SPData
import com.mindfulai.ministore.R
import java.util.*


class NotificationFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_notifications, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        SPData.getAppPreferences().notificationCount=0
        val rv = view.findViewById<RecyclerView>(R.id.rv_notifications)
        val no = view.findViewById<LinearLayout>(R.id.no_item_layout)
        rv.layoutManager = LinearLayoutManager(requireActivity())
        val gson = Gson()
        val json =SPData.getAppPreferences().notificationList
        val type = object : TypeToken<ArrayList<NotificationModel?>?>() {}.type
        val notificationModelArrayList = gson.fromJson<ArrayList<NotificationModel>>(json, type)
        if (notificationModelArrayList != null && notificationModelArrayList.size > 0) {
            no.visibility = View.GONE
            rv.adapter = context?.let { NotificationAdapter(it, notificationModelArrayList) }
        }

    }
}