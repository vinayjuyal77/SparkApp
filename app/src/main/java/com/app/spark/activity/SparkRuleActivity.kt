package com.app.spark.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.app.spark.R
import com.app.spark.activity.main.MainActivity
import com.app.spark.utils.SharedPrefrencesManager
import com.app.spark.constants.PrefConstant
import kotlinx.android.synthetic.main.activity_spark_rule.*

class SparkRuleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spark_rule)
        btnAccept.setOnClickListener {
            SharedPrefrencesManager.getInstance(this).setBoolean(PrefConstant.FIRST_LOGIN,true)
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finishAffinity()
        }
    }
}