package com.app.spark.activity.explore

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.app.spark.R
import com.app.spark.constants.PrefConstant
import com.app.spark.databinding.ActivitySessionOverBinding
import com.app.spark.utils.SharedPrefrencesManager
import com.google.android.gms.ads.*
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class SessionOverActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySessionOverBinding
    private var mRewardedAd: RewardedAd? = null

    private lateinit var viewModel: ExploreViewModel
    lateinit var pref: SharedPrefrencesManager
    var userId: String? = null

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_session_over)
        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory(application)
        ).get(ExploreViewModel::class.java)
        pref = SharedPrefrencesManager.getInstance(this)
        userId = pref.getString(PrefConstant.USER_ID, "")
        binding.activity = this
        when {
            intent.action!!.equals(getString(R.string.friend))-> {
                setEventAction(R.drawable.bg_green_ractangle,getColor(R.color.green_28C76F))
            }
            intent.action!!.equals(getString(R.string.date))-> {
                setEventAction(R.drawable.bg_red_ractangle,getColor(R.color.red_F94757))
            }
            intent.action!!.equals(getString(R.string.professional))-> {
                setEventAction(R.drawable.bg_blue_ractangle,getColor(R.color.bg_button_blue))
            }
        }
        binding.cvStartAgain.setOnClickListener {
            startActivity(Intent(this, FindMeFriendActivity::class.java).apply {action=intent.action})
            finish()
        }
        binding.tvTryAgain.setOnClickListener {
            onBackPressed()
            viewModel.isOnlineStatusApi(this,2, userId!!.toInt())
        }

        MobileAds.initialize(this) {}


        fulscreen()


        var adRequest = AdRequest.Builder().build()

        RewardedAd.load(this,"ca-app-pub-5067799942394351/2460259493", adRequest, object : RewardedAdLoadCallback() {
            // RewardedAd.load(this,"ca-app-pub-3601101210502228/7801541506", adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.e("Rewarded", adError?.message)
                mRewardedAd = null
            }

            override fun onAdLoaded(rewardedAd: RewardedAd) {
                Log.e("Rewarded", "Ad was loaded.")
                mRewardedAd = rewardedAd
                show()

            }
        })



















    }


    public fun fulscreen()

    {

        mRewardedAd?.fullScreenContentCallback = object: FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.e("Rewarded", "Ad was dismissed.")
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError?) {
                Log.e("Rewarded", "Ad failed to show.")
            }

            override fun onAdShowedFullScreenContent() {
                Log.e("Rewarded", "Ad showed fullscreen content.")
                // Called when ad is dismissed.
                // Don't set the ad reference to null to avoid showing the ad a second time.
                //  mRewardedAd = null
            }
        }
    }



    public fun show() {



        if (mRewardedAd != null) {
            mRewardedAd?.show(this, {
                fun onUserEarnedReward(rewardItem: RewardItem) {
                    var rewardAmount = rewardItem.amount
                    var rewardType = rewardItem.getType()
                    Log.e("Rewarded", "User earned the reward.")
                }
            })
        } else {
            Log.e("Rewarded", "The rewarded ad wasn't ready yet.")
        }
    }


    private fun setEventAction(bgRactangle: Int, color: Int) {

        getWindow().getDecorView().setBackgroundResource(bgRactangle)
        binding.tvStartAgain.setTextColor(color)
    }
}