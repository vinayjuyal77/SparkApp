package com.app.spark.activity.explore

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.app.spark.R
import com.app.spark.activity.chatroom.ChatRoomActivity
import com.app.spark.constants.AppConstants
import com.app.spark.constants.AppConstants.BundleConstants.USER_ID
import com.app.spark.constants.AppConstants.BundleConstants.USER_NAME
import com.app.spark.constants.IntentConstant
import com.app.spark.constants.PrefConstant
import com.app.spark.databinding.ActivityFindMeFriendBinding
import com.app.spark.databinding.ActivityMatchFoundBinding
import com.app.spark.dialogs.UnableMatchDialog
import com.app.spark.models.TagListModel
import com.app.spark.utils.BaseActivity
import com.app.spark.utils.SharedPrefrencesManager
import com.app.spark.utils.returnChatType
import com.app.spark.utils.showSnackBar
import com.otaliastudios.cameraview.PictureResult

class MatchFoundActivity : BaseActivity() {
    companion object {
        var gender: String? = null
        var minAge: String? = null
        var maxAge: String? = null
        var radius: String? = null
        var selectList : ArrayList<String> = ArrayList()
        var lat: String? = null
        var long: String? = null
    }
    lateinit var pref: SharedPrefrencesManager
    private lateinit var viewModel: ExploreViewModel
    private lateinit var binding: ActivityMatchFoundBinding
    var userId: String? = null

    lateinit var chatId:String
    lateinit var chatName:String

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_match_found)
        binding.activity = this
        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory(application)
        ).get(ExploreViewModel::class.java)
        pref = SharedPrefrencesManager.getInstance(this)
        userId = pref.getString(PrefConstant.USER_ID, "")
        when {
            intent.action!!.equals(getString(R.string.friend))-> {
                setEventAction(R.drawable.bg_green_ractangle,R.drawable.ic_icon_friend_match,getColor(R.color.green_28C76F))
            }
            intent.action!!.equals(getString(R.string.date))-> {
                setEventAction(R.drawable.bg_red_ractangle,R.drawable.ic_baseline_favorite_24,getColor(R.color.red_F94757))
            }
            intent.action!!.equals(getString(R.string.professional))-> {
                setEventAction(R.drawable.bg_blue_ractangle,R.drawable.ic_activity_tie,getColor(R.color.bg_button_blue))
            }
        }
        chatId = intent.extras!!.getString(USER_ID)!!
        chatName = intent.extras!!.getString(USER_NAME)!!

        binding.tvAcceptMatch.setOnClickListener {
            val intent = Intent(this@MatchFoundActivity, ChatRoomActivity::class.java).apply { action=intent.action}
            intent.putExtra(IntentConstant.CHAT_ID, ""+chatId)
            intent.putExtra(IntentConstant.CHAT_NAME, ""+chatName) //chatName
            intent.putExtra(IntentConstant.CHAT_IMG, "")
            intent.putExtra(IntentConstant.CHAT_TYPE, returnChatType("2"))
            intent.putExtra(IntentConstant.CHAT_TYPE_DATE, "yes")
            startActivity(intent)
            finish()
   /*//        startActivity(Intent(this, FriendChatActivity::class.java).apply {
//                action=intent.action})*/
        }
        viewModel.errCode.observe(this, { err: Int ->
            val handler = Handler()
            handler.postDelayed({
                if (err != null){
                    if(err==0){
                        UnableMatchDialog.Builder(this)
                            .setOkFunction {
                                startActivity(Intent(applicationContext, SessionOverActivity::class.java).apply { action=intent.action})
                                finish()
                            }
                            .build()
                    }else if(err==1){
                        val intent = Intent(this, ChatRoomActivity::class.java).apply { action=intent.action}
                        intent.putExtra(IntentConstant.CHAT_ID, viewModel.chatID.toString())
                        intent.putExtra(IntentConstant.CHAT_NAME, viewModel.chatName.toString())
                        intent.putExtra(IntentConstant.CHAT_IMG, "")
                        intent.putExtra(IntentConstant.CHAT_TYPE, returnChatType("2"))
                        intent.putExtra(IntentConstant.CHAT_TYPE_DATE, "yes")
                        startActivity(intent)
                        finish()
                    } else {
                        UnableMatchDialog.Builder(this)
                            .setOkFunction {
                                startActivity(Intent(applicationContext, SessionOverActivity::class.java).apply { action=intent.action})
                                finish()
                            }
                            .build()
                    }
                }
                dismisDilaog()
            }, 2000)

        })
        viewModel.errRes.observe(this, { err: Int ->
            if (err != null)
                showSnackBar(binding.root, getString(err))
            dismisDilaog()
        })

        binding.tvTryAgain.setOnClickListener {
            startActivity(Intent(this, FindMeFriendActivity::class.java).apply {action=intent.action})
            finish()
        }
    }
    private fun setEventAction(bgRactangle: Int,matchIcon: Int, color: Int) {
        getWindow().getDecorView().setBackgroundResource(bgRactangle)
        binding.tvAcceptMatch.setTextColor(color)
        binding.ivFav.setImageDrawable(getDrawable(matchIcon))
        binding.ivFav2.setImageDrawable(getDrawable(matchIcon))
        binding.ivCheck.setColorFilter(color)
    }


    override fun onBackPressed() {
        //super.onBackPressed()
        startActivity(Intent(this, FindMeFriendActivity::class.java).apply {action=intent.action})
        finish()
    }



}