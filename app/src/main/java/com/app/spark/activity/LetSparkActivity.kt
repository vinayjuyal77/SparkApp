package com.app.spark.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.app.spark.R
import kotlinx.android.synthetic.main.activity_let_spark.*

class LetSparkActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_let_spark)
        // imgFlash.visibility= View.VISIBLE
        tvLetSpark.visibility= View.VISIBLE
       // tvText2.visibility= View.VISIBLE
    }
}