package com.app.spark.activity.setting.privacy

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.app.spark.R
import com.app.spark.activity.setting.SettingViewModel
import com.app.spark.databinding.ActivityPrivacyBinding
import com.app.spark.databinding.ActivitySettingBinding

class PrivacyActivity : AppCompatActivity(), View.OnClickListener {
    lateinit var binding: ActivityPrivacyBinding
    lateinit var viewModel: PrivacyViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //  setContentView(R.layout.activity_setting)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_privacy)
        viewModel =
            ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(
                PrivacyViewModel::class.java
            )

        binding.back.setOnClickListener {

            onBackPressed()
        }


        binding.whoCanLike.setOnClickListener (this)
        binding.whoCanComment.setOnClickListener (this)
        binding.whoCanMention.setOnClickListener (this)
        binding.whoCanTag.setOnClickListener (this)
        binding.whoCanMessage.setOnClickListener (this)
        binding.blockedAccount.setOnClickListener (this)
        binding.restrictedAccount.setOnClickListener (this)
        binding.mutedAccount.setOnClickListener (this)
        binding.reportedAccount.setOnClickListener (this)
        binding.deleetAccout.setOnClickListener (this)
        binding.transferAccount.setOnClickListener (this)
        binding.disableAcoount.setOnClickListener (this)







    }




    private fun showDialog(title: String) {
        val dialogBuilder = AlertDialog.Builder(this, R.style.MyDialogTheme)
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_select_intractions, null)
        dialogBuilder.setView(dialogView)


         val title_text = dialogView.findViewById<TextView>(R.id.title)

        title_text.text = title
        //dialogView.radioButton_user_chat.isChecked = true


        val alertDialog = dialogBuilder.create()
        alertDialog.show()



    }



    private fun showDialogDetele(title: String) {
        val dialogBuilder = AlertDialog.Builder(this, R.style.MyDialogTheme)
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_delete, null)
        dialogBuilder.setView(dialogView)


        val alertDialog = dialogBuilder.create()
        val no = dialogView.findViewById<LinearLayout>(R.id.no)
        val yes = dialogView.findViewById<LinearLayout>(R.id.yes)


        no.setOnClickListener {

            alertDialog.dismiss()
        }

        yes.setOnClickListener {

            startActivity(Intent(this, DeleteAccountPassword::class.java))
        }

        //dialogView.radioButton_user_chat.isChecked = true



        alertDialog.show()



    }



    private fun showDialogDisable(title: String) {
        val dialogBuilder = AlertDialog.Builder(this, R.style.MyDialogTheme)
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_disables, null)
        dialogBuilder.setView(dialogView)


        val yes = dialogView.findViewById<LinearLayout>(R.id.yes)
        val no = dialogView.findViewById<LinearLayout>(R.id.no)
        val alertDialog = dialogBuilder.create()

        yes.setOnClickListener {



        }
        no.setOnClickListener {

            alertDialog.dismiss()
        }



        alertDialog.show()



    }


    override fun onClick(p0: View?) {
        
        when(p0)
        {

            binding.whoCanLike->
            {
                showDialog(binding.whoCanLikeTxt.text.toString())

            }
            binding.whoCanComment->
            {
                showDialog(binding.whoCanCommentTxt.text.toString())

            }

            binding.whoCanMention->
            {
                showDialog(binding.whoCanMentionTxt.text.toString())

            }

            binding.whoCanTag->
            {
                showDialog(binding.whoCanTagTxt.text.toString())

            }
            binding.whoCanMessage->
            {
                showDialog(binding.whoCanMessageTxt.text.toString())

            }

            binding.blockedAccount->
            {
               startActivity(Intent(this, BlockedActivity::class.java)
                   .putExtra("type", "Blocked"))

            }

            binding.restrictedAccount->
            {
                startActivity(Intent(this, BlockedActivity::class.java)
                    .putExtra("type", "Restricted"))

            }

            binding.mutedAccount->
            {
                startActivity(Intent(this, BlockedActivity::class.java)
                    .putExtra("type", "Muted"))

            }

            binding.reportedAccount->
            {
                startActivity(Intent(this, BlockedActivity::class.java)
                .putExtra("type", "Reported"))

            }


            binding.deleetAccout->
            {
                showDialogDetele("")

            }


            binding.transferAccount->
            {
                startActivity(Intent(this, TransferAccountActivity::class.java)
                    .putExtra("type", "Reported"))

            }
            binding.disableAcoount->
            {
               showDialogDisable("")

            }


        }
    }
}