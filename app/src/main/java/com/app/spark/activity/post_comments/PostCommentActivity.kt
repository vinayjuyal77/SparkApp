package com.app.spark.activity.post_comments

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.app.spark.R
import com.app.spark.activity.main.MainActivity
import com.app.spark.constants.IntentConstant
import com.app.spark.constants.PrefConstant
import com.app.spark.databinding.ActivityCommentsBinding
import com.app.spark.dialogs.ReportPostDialog
import com.app.spark.dialogs.SimpleCustomDialog
import com.app.spark.interfaces.OnCommentReplyInterface
import com.app.spark.interfaces.ReportPostDialogListner
import com.app.spark.interfaces.SimpleDialogListner
import com.app.spark.models.FeedsResponse
import com.app.spark.models.GetFlickResponse
import com.app.spark.models.ImportantDataResult
import com.app.spark.utils.*
import com.bumptech.glide.Glide
import com.google.gson.Gson

class PostCommentActivity : BaseActivity(), View.OnClickListener, OnCommentReplyInterface {

    lateinit var binding: ActivityCommentsBinding
    lateinit var viewModel: PostCommentsViewModel
    lateinit var pref: SharedPrefrencesManager
    private var loginDetails: ImportantDataResult? = null
    private var isCommentSelected: Boolean = false
    private var selectedView: View? = null
    private var selectedCommentId: String? = null
    private var replyUserId: String? = null
    private var parentCommentId: String? = null
    private var adaptor:CommentsAdapter?=null
    private var deletedUserId=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_comments)
        initlizeViewModel()
        pref = SharedPrefrencesManager.getInstance(this)
        loginDetails = Gson().fromJson(
            pref.getString(PrefConstant.LOGIN_RESPONSE, ""),
            ImportantDataResult::class.java
        )
        viewModel.setPostId(
            intent.getSerializableExtra(IntentConstant.POST_DETAIL) as FeedsResponse.Result?,
            intent.getSerializableExtra(IntentConstant.FLICK_DETAIL) as GetFlickResponse.Result?,
            pref.getString(PrefConstant.USER_ID, ""),
            pref.getString(PrefConstant.ACCESS_TOKEN, "")
        )
        setClickListeners()
        setUserDetails()
        viewModel.getCommentAPI()
    }

    private fun setClickListeners() {
        binding.imgBack.setOnClickListener(this)
        binding.imgSend.setOnClickListener(this)
        binding.imgReport.setOnClickListener(this)
        binding.imgDelete.setOnClickListener(this)
    }

    private fun setUserDetails() {
        binding.edtComment.hint = "${getString(R.string.comment_as)} ${loginDetails?.username}"
        Glide.with(this).load(loginDetails?.profile_pic).placeholder(R.drawable.ic_profile)
            .into(binding.imgProfilePic)
        if (loginDetails?.user_id == viewModel.postDetails.value?.userId) {
            binding.imgReport.visibility = View.GONE
        }
    }

    private fun initlizeViewModel() {

        viewModel =
            ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(
                PostCommentsViewModel::class.java
            )

        viewModel.response.observe(this, { res: String? ->
            if (!res.isNullOrEmpty()) {
                showToastShort(this, res)
               // recreate()
            }
        })

        viewModel.errString.observe(this, { err: String? ->
            if (err != null)
                showToastShort(this, err)
        })
        viewModel.errRes.observe(this, { err: Int ->
            // binding.progressBar.visibility = View.GONE
            if (err != null)
                showSnackBar(binding.root, getString(err))
        })
        viewModel.commentsList.observe(this, {
            if (!it.isNullOrEmpty()) {
                adaptor=CommentsAdapter(this, it.toMutableList(), this)
                binding.rvComments.adapter = adaptor//CommentsAdapter(this, it.toMutableList(), this)
            }else{
                adaptor=CommentsAdapter(this, it.toMutableList(), this)
                binding.rvComments.adapter = adaptor//CommentsAdapter(this, it.toMutableList(), this)
                adaptor?.notifyDataSetChanged()
            }
        })

        viewModel.postDetails.observe(this, {
            if (it != null) {
                binding.tvPostInfo.text = "${it.username} ${it.postInfo}"
                Glide.with(this).load(it.profilePic).placeholder(R.drawable.ic_profile)
                    .into(binding.imgUserProfilePic)
            }
        })
        viewModel.flickDetails.observe(this, {
            if (it != null) {
                binding.tvPostInfo.text = "${it.username} ${it.flickInfo}"
                Glide.with(this).load(it.profilePic).placeholder(R.drawable.ic_profile)
                    .into(binding.imgUserProfilePic)
            }
        })
    }

    override fun onClick(p0: View?) {
        when (p0) {
            binding.imgBack -> onBackPressed()
            binding.imgSend -> {
                hideKeyboard(this)
                if (!binding.edtComment.text.toString().isNullOrEmpty()) {
                    if (binding.edtComment.text[0].toString() != "@") {
                        parentCommentId = null
                        replyUserId = null
                    }
                    viewModel.addCommentAPI(
                        binding.edtComment.text.toString(),
                        parentCommentId,
                        replyUserId
                    )
                    binding.edtComment.text.clear()
                } else {
                    showSnackBar(binding.root, getString(R.string.error_comment))
                }
            }

            binding.imgReport -> {
                reportDialog()
            }
            binding.imgDelete -> {
                deleteDialog()
            }
        }
    }

    override fun onCommentReply(
        comment_id: String,
        user_refer_id: String,
        user_refer_name: String
    ) {
        replyUserId = user_refer_id
        parentCommentId = comment_id
        binding.edtComment.requestFocus()
        binding.edtComment.setText("@${user_refer_name} ")
        binding.edtComment.setSelection(binding.edtComment.text.length)
        showKeyboard(this, binding.edtComment)
    }

    override fun onCommentLike(comment_id: String, type: Boolean) {
        viewModel.likeUnlikeApi(comment_id, type)
    }

    override fun onCommentDeleteReport(comment_id: String, userId: String, view: View) {
        isCommentSelected = true
        deletedUserId=userId
        selectedView = view
        selectedCommentId = comment_id
        if(deletedUserId.equals(loginDetails?.user_id,true))
        binding.imgDelete.visibility = View.VISIBLE
        binding.imgReport.visibility = View.VISIBLE
    }

    private fun deleteDialog() {
        if(deletedUserId.equals(loginDetails?.user_id,true)){
            val dialog = SimpleCustomDialog(
                this,
                title = getString(R.string.delete),
                desc = getString(R.string.delete_comment_thread_note),
                positiveBtnName = getString(R.string.delete),
                onConnectionTypeSelected = object : SimpleDialogListner {
                    override fun submitSelected() {
                        if (isNetworkAvailable(this@PostCommentActivity)) {
                            viewModel.deleteComment(selectedCommentId)
                            selectedView?.visibility = View.GONE
                            binding.imgDelete.visibility = View.GONE
                            binding.imgReport.visibility = View.GONE
                            deletedUserId=""
                        }
                    }
                })
            dialog.show()
            dialog.setCancelable(true)
        }
    }

    private fun reportDialog() {
        val dialog = ReportPostDialog(
            this,
            onConnectionTypeSelected = object : ReportPostDialogListner {
                override fun submitSelected(isUnfollow: Boolean) {
                    if (isNetworkAvailable(this@PostCommentActivity)) {
                        viewModel.reportPost(isUnfollow)
                    }
                }
            })
        dialog.show()
        dialog.setCancelable(true)
    }

    override fun onBackPressed() {
        if (!isCommentSelected) {
            //super.onBackPressed()
            if (intent.getIntExtra(IntentConstant.PAGE_FLAG, 0) == 2) {
                var intents = Intent(this@PostCommentActivity, MainActivity::class.java)
                intents.putExtra(IntentConstant.PAGE_FLAG,2)
                intents.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intents)
                finish()
            }else if (intent.getIntExtra(IntentConstant.PAGE_FLAG, 0) == 22) {
                IntentConstant.FeedValue ="my_feed"
                finish()
            } else {
//                var intents = Intent(this@PostCommentActivity, MainActivity::class.java)
//                intents.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//                startActivity(intents)
                finish()
            }
        } else {
            isCommentSelected = false
            selectedView?.visibility = View.GONE
            binding.imgDelete.visibility = View.GONE
            binding.imgReport.visibility = View.GONE
        }
    }
}