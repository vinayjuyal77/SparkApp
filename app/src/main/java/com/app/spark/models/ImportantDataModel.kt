package com.app.spark.models

import java.io.Serializable

data class ImportantDataModel(
    val name:String,
    val gender:String,
    val dob:String,
    val location:String,
    val state:String,
    val country:String,
    val lat:String,
    val long:String
):Serializable
