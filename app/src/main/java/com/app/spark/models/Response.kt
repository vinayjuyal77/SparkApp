package com.app.spark.models

data class OnlineStatusResponse(
    val statusCode: Int,
    val APICODERESULT: String,
    val result: String
)

data class CreateRoomResponse(
    val statusCode: Int,
    val APICODERESULT: String,
    val result: Int
)

data class GetTagListResponse(
    val statusCode: Int,
    val APICODERESULT: String,
    val status:Int,
    val result: Result
){
    data class Result(
        val id: Int,
        val type: String,
        val tags: String
    )
}
data class MatchingResponse(
    val statusCode: Int,
    val APICODERESULT: String,
    val status:Int,
    val result: ArrayList<UserData>
){
    data class UserData(
        val user_id: Int,
        val gender: String,
        val name: String,
        val profile_pic: String,
        val distance: Float
    )
}
data class GetRoomResponse(
    val statusCode: Int,
    val APICODERESULT: String,
    val result: ArrayList<UserData>
){ data class UserData(
    val room: RoomData,
    val users: ArrayList<USER_INFO>,
    val user_ids:ArrayList<Int>)
}
data class RoomData(
    val room_id: Int,
    val room_title: String,
    val room_description: String,
    val room_img: String,
    val room_type: String,
    val user_count: Int,
    val created_date: String,
    val created_by: Int,
    val updated_date: String,
    val updated_by: Int,
    val is_active:Int,
    val is_deleted:Int
)
data class USER_INFO(
    val user_id: Int,
    val name:String,
    val profile_pic:String,
    val type: String,
)
data class GetListResponse(
    val statusCode: Int,
    val APICODERESULT: String,
    val result: UserData
){
    data class UserData(
        val room: RoomData,
        val users: ArrayList<USER_INFO>,
        val requests:Int)

}

data class RaiseHandListResponse(
    val statusCode: Int,
    val APICODERESULT: String,
    val response: ArrayList<ActiveUser>
){ data class ActiveUser(
    val id: Int,
    val room_id: Int,
    val status:String,
    val user_id:Int,
    val name: String,
    val profile_pic: String)
}

data class onRaiseHandResponse(
    val statusCode: Int,
    val APICODERESULT: String,
    val response: String
)


data class ConvertMessageReceiver(
    val result: ResultData
){
    data class ResultData( val id: Int,
                           val s_id: Int,
                           val chat_type:String,
                           val msg:String,
                           val msg_type: String,
                           val media_type: String,
                           val thumb_url:String,
                           val url: String,
                           val date_added: String,
                           val group_id: Int)
}




