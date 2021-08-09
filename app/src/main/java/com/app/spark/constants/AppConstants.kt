package com.app.spark.constants

import android.provider.MediaStore

class AppConstants {
    object BundleConstants {
        const val BUNDLE = "bundle"
        const val USER_ID = "uswer_id"
        const val USER_NAME = "uswer_name"
        const val ACTION_INT = "action_value"
        var VIDEO_TEST = "video_test"
        const val CREATE_ROOM="create_room"
        const val ADD_USER_TO_ROOM="add_user_to_room"
        const val ROOM_TITLE="room_title"
        const val ROOM_DESC="room_description"
        const val ROOM_TYPE="room_type"
        const val ROOM_ID="room_id"
        var isVisible=true
        var isDialogVisible=true
        var isResume=false
        var isResumeCallList=false
    }
    object MediaConstant{
        @Suppress("DEPRECATION")
        val baseProjection = arrayOf(
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.ARTIST,
            MediaStore.Video.Media.DATE_TAKEN,
            MediaStore.Video.Media.ALBUM,
            MediaStore.Video.Media.DURATION
        )
        @Suppress("DEPRECATION")
        val baseProjection2 = arrayOf(
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATE_TAKEN
        )
    }

    object Initilize {
        const val MESSAGE_TYPE = "id_message_type"
        const val REMOVE_TYPE = "id_Remove_type"
        const val REPORT_TYPE = "id_report_type"
        const val MAKE_LISTENERS_TYPE = "id_make_listeners_type"
        const val MAKE_SPEAKERS_TYPE = "id_make_speakers_type"
        const val MAKE_HOST_SPEAKER="make_host_speaker"
        const val MAKE_OTHER_USER_SPEAKER="make_other_user_speaker"
        const val MAKE_HOST_LISTENER="make_host_listeners"
        const val MAKE_OTHER_USER_LISTENER="make_other_user_listeners"
    }


}