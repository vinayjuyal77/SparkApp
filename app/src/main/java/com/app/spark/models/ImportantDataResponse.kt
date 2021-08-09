package com.app.spark.models

data class ImportantDataResponse(
    val APICODERESULT: String,
    val result: ImportantDataResult,
    val statusCode: Int
)
data class ImportantDataResult(
    val accesstoken: String,
    val act_type: String,
    val completed: String,
    val country_code: String,
    val dob: String,
    val email: String,
    val gender: String,
    val location: String,
    val mobile: String,
    val name: String,
    val profile_pic: String,
    val user_id: String,
    val user_bio: String,
    val username: String,
    val lat: String,
    val long: String,
    val verified: String
)