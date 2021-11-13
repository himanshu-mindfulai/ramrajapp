package com.mindfulai.Models.config

import com.google.gson.annotations.SerializedName

data class DeliveryChargesModel(
        @SerializedName("belowValueCharge")
        var belowValueCharge: Double? = null,
        @SerializedName("orderValue")
        var orderValue: Double? = null,
        @SerializedName("aboveOrSameValueCharge")
        var aboveOrSameValueCharge: Double? = null
)