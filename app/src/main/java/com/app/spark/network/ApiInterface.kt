package com.app.spark.network

import android.app.Application
import com.app.spark.activity.story.dataClass.StoryResponse
import com.app.spark.constants.ServerConstant
import com.app.spark.models.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiInterface {


    @POST(ServerConstant.SIGNUP)
    suspend fun signUpAPi(@Body map: HashMap<String, String>): Response<ImportantDataResponse>

    @POST(ServerConstant.VERIFY_OTP)
    suspend fun otpAPi(@Body map: HashMap<String, String>): Response<ImportantDataResponse>

    @POST(ServerConstant.SIGNIN)
    suspend fun loginAPI(@Body map: HashMap<String, String>): Response<ImportantDataResponse>

    @POST(ServerConstant.EDIT_SIGNUP)
    suspend fun editsignupAPI(@Body map: HashMap<String, String>): Response<CommonResponse>

    @POST(ServerConstant.REPORT)
    suspend fun reportUserApi(
        @Header("accesstoken") header: String,
        @Body map: HashMap<String, String>
    ): Response<CommonResponse>

    @POST(ServerConstant.FOLLOW_GROUP_CONNECTION_TYPE)
    suspend fun folowgroupActionApi(
        @Header("accesstoken") header: String,
        @Body map: HashMap<String, String>
    ): Response<CommonResponse>

    @POST(ServerConstant.BLOCK_USER)
    suspend fun blockuserAction(
        @Header("accesstoken") header: String,
        @Body map: HashMap<String, String>
    ): Response<CommonResponse>

    @GET(ServerConstant.VERIFY_USERNAME)
    suspend fun verifyUserNameAPI(@QueryMap map: HashMap<String, String>): Response<CommonResponse>

    @GET(ServerConstant.NOTIFICATION_LIST)
    suspend fun notificationListAPI(
        @Header("accesstoken") header: String,
        @QueryMap map: HashMap<String, String>
    ): Response<NotificationListResponse>

    @GET(ServerConstant.COUNTRY_STATE_LIST)
    suspend fun countryStateAPI(@QueryMap map: HashMap<String, String>): Response<CountryStateResponse>

    @GET(ServerConstant.POPULAR_PROFILE)
    suspend fun getPopularProfile(
        @Header("accesstoken") header: String,
        @Query("user_id") userId: String
    ): Response<PopularProfileResponse>

    @POST(ServerConstant.FOLLOW_UNFOLLOW)
    suspend fun followUnfollowAPI(
        @Header("accesstoken") header: String,
        @Body map: HashMap<String, String>
    ): Response<CommonResponse>


    @POST(ServerConstant.DELETE_FEED)
    suspend fun deleteFeedAPI(
        @Header("accesstoken") header: String,
        @Body map: HashMap<String, String>
    ): Response<CommonResponse>

    @Multipart
    @POST(ServerConstant.COMPLETE_PROFILE)
    suspend fun completeProfileAPI(
        @Header("accesstoken") header: String,
        @PartMap map: HashMap<String, RequestBody>,
        @Part part: MultipartBody.Part?
    ): Response<ImportantDataResponse>


    @Multipart
    @POST(ServerConstant.EDIT_BIO_PROFILE)
    suspend fun editBioAPI(
        @Header("accesstoken") header: String,
        @PartMap map: HashMap<String, RequestBody>,
        @Part part: MultipartBody.Part?
    ): Response<EditBioResponse>


    @GET(ServerConstant.PROFILE_GET)
    suspend fun getProfileApi(
        @Header("accesstoken") header: String,
        @QueryMap map: HashMap<String, String>
    ): Response<ProfileGetResponse>


    @GET(ServerConstant.SEARCH_USER)
    suspend fun searchUserApi(
        @Header("accesstoken") header: String,
        @QueryMap map: HashMap<String, String>
    ): Response<SearchModelResponse>

    @GET(ServerConstant.FOLLOWING_FOLLOWER_GET)
    suspend fun getFollowingAndFollowerApi(
        @Header("accesstoken") header: String,
        @QueryMap map: HashMap<String, String>
    ): Response<FollowingResponse>

    @Multipart
    @POST(ServerConstant.POST_FEED)
    suspend fun addPostAPI(
        @Header("accesstoken") header: String,
        @PartMap map: HashMap<String, RequestBody>,
        @Part part: MultipartBody.Part?
    ): Response<CommonResponse>

    @GET(ServerConstant.GET_FEEDS)
    suspend fun getFeedsApi(
        @Header("accesstoken") header: String?,
        @QueryMap map: HashMap<String, String?>
    ): Response<FeedsResponse>

    @POST(ServerConstant.FEED_LIKE_ACTION)
    suspend fun likeUnlikeAPI(
        @Header("accesstoken") header: String?,
        @Body map: HashMap<String, String?>
    ): Response<CommonResponse>

    @POST(ServerConstant.REPORT_FEED)
    suspend fun reportFeedApi(
        @Header("accesstoken") header: String?,
        @Body map: HashMap<String, String?>
    ): Response<CommonResponse>

    @POST(ServerConstant.FEED_COMMENT)
    suspend fun commentAPI(
        @Header("accesstoken") header: String?,
        @Body map: HashMap<String, String?>
    ): Response<CommonResponse>

    @POST(ServerConstant.UPDATE_DEVICE_TOKEN)
    suspend fun updateDeviceTokenAPI(
        @Header("accesstoken") header: String?,
        @Body map: HashMap<String, String?>
    ): Response<CommonResponse>

    @GET(ServerConstant.GET_COMMENTS)
    suspend fun getCommentsApi(
        @Header("accesstoken") header: String?,
        @QueryMap map: HashMap<String, String?>
    ): Response<GetCommentsResponse>

    @POST(ServerConstant.COMMENTS_LIKE_ACTION)
    suspend fun commentLikeUnlikeAPI(
        @Header("accesstoken") header: String?,
        @Body map: HashMap<String, String?>
    ): Response<CommonResponse>

    @POST(ServerConstant.DELETE_COMMENT)
    suspend fun deleteCommentAPI(
        @Header("accesstoken") header: String?,
        @Body map: HashMap<String, String?>
    ): Response<CommonResponse>

    @Multipart
    @POST(ServerConstant.POST_FLICK)
    suspend fun addFlickAPI(
        @Header("accesstoken") header: String,
        @PartMap map: HashMap<String, RequestBody>,
        @Part part: MultipartBody.Part?,
        @Part part1: MultipartBody.Part?
    ): Response<CommonResponse>

    @GET(ServerConstant.GET_FLICKS)
    suspend fun getFlicksApi(
        @Header("accesstoken") header: String?,
        @QueryMap map: HashMap<String, String?>
    ): Response<GetFlickResponse>

    @POST(ServerConstant.VIEW_COUNT)
    suspend fun viewCountAPI(
        @Header("accesstoken") header: String?,
        @Body map: HashMap<String, String?>
    ): Response<CommonResponse>

    @Multipart
    @POST(ServerConstant.CREATE_GROUP)
    suspend fun createGroupAPI(
        @Header("accesstoken") header: String,
        @PartMap map: HashMap<String, RequestBody>,
        @Part part: MultipartBody.Part?
    ): Response<CommonResponse>

    @Multipart
    @POST(ServerConstant.UPLOAD_MEDIA)
    suspend fun uploadMediaAPI(
        @Header("accesstoken") header: String,
        @PartMap map: HashMap<String, RequestBody>,
        @Part part: MultipartBody.Part?
    ): Response<UploadMediaResponse>

    @GET(ServerConstant.GET_PROFILE_POST)
    suspend fun getProfilePostApi(
        @Header("accesstoken") header: String?,
        @QueryMap map: HashMap<String, String?>
    ): Response<FeedsResponse>

    @GET(ServerConstant.GET_PROFILE_POST)
    suspend fun getProfileFlickApi(
        @Header("accesstoken") header: String?,
        @QueryMap map: HashMap<String, String?>
    ): Response<GetFlickResponse>

    @POST(ServerConstant.SEND_OTP)
    suspend fun sendOtpApi(
        @Body map: HashMap<String, String?>
    ): Response<CommonResponse>

    @POST(ServerConstant.RESET_PASSWORD)
    suspend fun resetPasswordApi(
        @Body map: HashMap<String, String?>
    ): Response<CommonResponse>

    @POST(ServerConstant.UPDATE_PERSONAL_INFO)
    suspend fun updatePersonalInfoApi(
        @Header("accesstoken") header: String?,
        @Body map: HashMap<String, Any?>
    ): Response<CommonResponse>

    @POST(ServerConstant.ADD_WORK_EXP)
    suspend fun addWorkExpApi(
        @Header("accesstoken") header: String?,
        @Body map: HashMap<String, Any?>
    ): Response<AddWorkExpResponse>

    @POST(ServerConstant.ADD_EDUCATION)
    suspend fun addEducationApi(
        @Header("accesstoken") header: String?,
        @Body map: HashMap<String, Any?>
    ): Response<AddEducationResponse>

    @POST(ServerConstant.UPDATE_PROFESSIONAL_INFO)
    suspend fun updateProfessionalInfoApi(
        @Header("accesstoken") header: String?,
        @Body map: HashMap<String, Any?>
    ): Response<CommonResponse>


    @POST(ServerConstant.ONLINE_STATUS)
    suspend fun isOnlineStatus(
        @Body map: HashMap<String, Any?>
    ): Response<OnlineStatusResponse>

    @POST(ServerConstant.TAG_LIST)
    suspend fun getTagList(
        @Body map: HashMap<String, Any?>
    ): Response<GetTagListResponse>

    @POST(ServerConstant.MATCHING_PEREFRENCES)
    suspend fun setMatchFound(
        @Body map: HashMap<String, Any?>
    ): Response<MatchingResponse>



    @POST(ServerConstant.GET_STORIES)
    suspend fun getusersStories(
        @Body map: HashMap<String, Any?>
    ): Response<StoryResponse>


    @POST(ServerConstant.UPDATE_TAGS)
    suspend fun setTagsAPI(
        @Body map: HashMap<String, Any?>
    ): Response<MatchingResponse>

    companion object {
        operator fun invoke(application: Application): ApiInterface {
            return ApiClients.callRetrofit(application.applicationContext)
        }
    }
    @POST(ServerConstant.UPDATE_ENGAGE)
    suspend fun isUpdateEngageApi(
        @Body map: HashMap<String, Any?>
    ): Response<OnlineStatusResponse>


    @POST(ServerConstant.CREATE_ROOM)
    suspend fun setCreateRoom(
        @Body map: HashMap<String, Any?>
    ): Response<CreateRoomResponse>

    @Multipart
    @POST(ServerConstant.ADD_STORY)
    suspend fun addStory(
        @PartMap map: HashMap<String, RequestBody>,
        @Part part: MultipartBody.Part?
    ): Response<CommonResponse>

    @GET(ServerConstant.GET_ADDUSER_FOLLOWER)
    suspend fun getFollowerApi(
        @QueryMap map: HashMap<String, String>
    ): Response<FollowingResponse>

    @POST(ServerConstant.GET_ROOM_LIST)
    suspend fun getRoomListAPI(
        @Body map: HashMap<String, Any?>
    ): Response<GetRoomResponse>


    @POST(ServerConstant.ADD_USER_TO_ROOM)
    suspend fun addUserToRoom(
        @Body map: HashMap<String, Any?>
    ): Response<OnlineStatusResponse>

    @POST(ServerConstant.DELETE_ROOM)
    suspend fun onDeleteRoom(
        @Body map: HashMap<String, Any?>
    ): Response<OnlineStatusResponse>

    @POST(ServerConstant.GET_LIST)
    suspend fun getListAPI(
        @Body map: HashMap<String, Any?>
    ): Response<GetListResponse>

    @POST(ServerConstant.REQUEST_LIST_ROOM)
    suspend fun getRequestListRoom(
        @Body map: HashMap<String, Any?>
    ): Response<RaiseHandListResponse>

    @POST(ServerConstant.SET_RAISE_HAND)
    suspend fun setRaiseHandAPI(
        @Body map: HashMap<String, Any?>
    ): Response<onRaiseHandResponse>

    @POST(ServerConstant.ACCEPTED_ROOM_REQUEST)
    suspend fun setAllowRoomRequest(
        @Body map: HashMap<String, Any?>
    ): Response<onRaiseHandResponse>

    @POST(ServerConstant.REMOVE_FROM_ROOM)
    suspend fun onRemoveFromRoomAPI(
        @Body map: HashMap<String, Any?>
    ): Response<onRaiseHandResponse>

    @POST(ServerConstant.CHANGE_ROOM_USER_TYPE)
    suspend fun onChangeRoomTypeAPI(
        @Body map: HashMap<String, Any?>
    ): Response<onRaiseHandResponse>

}