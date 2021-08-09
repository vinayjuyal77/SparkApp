package com.app.spark.models

data class OtpResponse(
    val APICODERESULT: String,
    val result: OtpResult,
    val statusCode: Int
)
data class OtpResult(
    val accesstoken: String,
    val act_type: String,
    val completed: String,
    val country_code: String,
    val email: String,
    val mobile: String,
    val otp: String,
    val user_id: String,
    val username: String,
    val verified: String
)