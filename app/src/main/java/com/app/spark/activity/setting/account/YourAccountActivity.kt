package com.app.spark.activity.setting.account

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.app.spark.R
import com.app.spark.activity.setting.SettingViewModel
import com.app.spark.databinding.ActivitySettingBinding
import com.app.spark.databinding.ActivityYourAccountBinding
import com.app.spark.generated.callback.OnClickListener

class YourAccountActivity : AppCompatActivity(), View.OnClickListener  {
    lateinit var binding : ActivityYourAccountBinding
    lateinit var viewModel : YourAccountViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //  setContentView(R.layout.activity_setting)

        binding = DataBindingUtil.setContentView(this,R.layout.activity_your_account)
        viewModel =
            ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(
                YourAccountViewModel::class.java
            )



        binding.changeUsername.setOnClickListener(this)
        binding.updateMobileNumber.setOnClickListener(this)
        binding.savedflickPost.setOnClickListener(this)
        binding.deletedPostFlick.setOnClickListener(this)
        binding.postQuality.setOnClickListener(this)
        binding.paymentMethod.setOnClickListener(this)
        binding.seeActivity.setOnClickListener(this)


    }



    private fun showDialog(title: String) {
        val dialogBuilder = AlertDialog.Builder(this, R.style.MyDialogTheme)
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_select_quality, null)
        dialogBuilder.setView(dialogView)
      //  val radioGroupChat = dialogView.radio_group_chat
        //dialogView.radioButton_user_chat.isChecked = true


        val alertDialog = dialogBuilder.create()
        alertDialog.show()



    }

    override fun onClick(p0: View?) {


        when(p0)
        {

            binding.changeUsername ->
            {

                startActivity(Intent(this, ChangeUsernameActivity::class.java))
            }

            binding.updateMobileNumber ->
            {

                startActivity(Intent(this, UpdateMobileNumber::class.java))
            }

            binding.savedflickPost ->
            {

                startActivity(Intent(this, SavedFlickPostActivity::class.java))
            }


            binding.deletedPostFlick ->
            {

                startActivity(Intent(this, DeletedPostActivity::class.java))
            }


            binding.postQuality ->
            {

                showDialog("")
            }

            binding.paymentMethod ->
            {

                startActivity(Intent(this, PurchasePaymentMethod::class.java))
            }

            binding.seeActivity ->
            {
                startActivity(Intent(this, SeeActivity::class.java))
            }

        }
    }
}