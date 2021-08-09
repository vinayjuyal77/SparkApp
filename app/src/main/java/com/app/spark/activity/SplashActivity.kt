package com.app.spark.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import androidx.databinding.DataBindingUtil
import com.app.spark.R
import com.app.spark.activity.main.MainActivity
import com.app.spark.databinding.ActivityLetSparkBinding
import com.app.spark.utils.SharedPrefrencesManager
import com.app.spark.constants.PrefConstant

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLetSparkBinding
    private var mDelayHandler: Handler? = null
    private val SPLASH_DELAY: Long = 3000 //3 seconds

    internal val mRunnable: Runnable = Runnable {
        if (!isFinishing) {
            if (SharedPrefrencesManager.getInstance(this)
                    .getBoolean(PrefConstant.IS_LOGIN, false)
            ) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                val intent = Intent(this, NewLoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    //    binding = DataBindingUtil.setContentView(this, R.layout.activity_let_spark)
//        if (!SharedPrefrencesManager.getInstance(this)
//                .getBoolean(PrefConstant.IS_LOGIN, false)
//        ) {
//          //  binding.imgLogo.visibility = View.VISIBLE
//          //  animateLogo()
//        } else {
//          //  binding.imgFlash.visibility = View.VISIBLE
//         //   binding.tvLetSpark.visibility = View.VISIBLE
//          //  binding.tvText2.visibility = View.VISIBLE
//        }

        if (SharedPrefrencesManager.getInstance(this)
                .getBoolean(PrefConstant.IS_LOGIN, false)
        ) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            val intent = Intent(this, NewLoginActivity::class.java)
            startActivity(intent)
            finish()
        }
     //   mDelayHandler = Handler()
      //  mDelayHandler!!.postDelayed(mRunnable, SPLASH_DELAY)
    }

    private fun animateLogo() {
        /*val animX = ObjectAnimator.ofFloat(binding.imgLogo, "scaleX", 1f, 20f)
        val animY = ObjectAnimator.ofFloat(binding.imgLogo, "scaleY", 1f, 20f)
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(animX, animY)
        animatorSet.duration = 2000
        animatorSet.startDelay = 1000
        animatorSet.start()*/
    }

    override fun onDestroy() {
        if (mDelayHandler != null) {
            mDelayHandler!!.removeCallbacks(mRunnable)
        }
        super.onDestroy()
    }

}