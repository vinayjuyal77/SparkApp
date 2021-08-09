package com.app.spark.interfaces

import android.view.View

interface OnCommentReplyInterface {
    fun onCommentReply(comment_id: String, user_refer_id: String, user_refer_name: String)
    fun onCommentLike(comment_id: String, type: Boolean)
    fun onCommentDeleteReport(comment_id: String, userId: String,view: View)
}