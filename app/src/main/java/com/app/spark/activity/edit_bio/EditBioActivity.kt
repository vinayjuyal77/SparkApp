package com.app.spark.activity.edit_bio

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.app.spark.R
import com.app.spark.constants.IntentConstant
import com.app.spark.databinding.ActivityEditBioBinding
import com.app.spark.utils.isNetworkAvailable
import com.app.spark.utils.showSnackBar
import com.app.spark.utils.showToastLong
import com.app.spark.viewmodel.ProfileGetViewModel
import com.app.spark.utils.SharedPrefrencesManager
import com.app.spark.constants.PrefConstant

class EditBioActivity : AppCompatActivity(), View.OnClickListener {

    private var token: String?=""
    lateinit var binding: ActivityEditBioBinding
    private lateinit var viewModel: ProfileGetViewModel
    private lateinit var pref: SharedPrefrencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_bio)
        binding.bio=this
        pref = SharedPrefrencesManager.getInstance(this@EditBioActivity)
        token = pref.getString(PrefConstant.ACCESS_TOKEN, "")
        viewModel = ViewModelProvider(this,ViewModelProvider.AndroidViewModelFactory(application)).get(ProfileGetViewModel::class.java)
        setClickListener()
        // set prev user bio
        var prevBio = intent.getStringExtra(IntentConstant.PROFILE_BIO)
        binding.edtBio.setText(prevBio)
        obServerBio()
    }

    private fun obServerBio() {
        viewModel.responseEditBio.observe(this@EditBioActivity, Observer {
           showToastLong(this@EditBioActivity,it.aPICODERESULT)
            finish()
        })

        viewModel.errString.observe(this@EditBioActivity, Observer { err: String ->
           // binding.progressBar.visibility = View.GONE
            if (!err.isNullOrEmpty())
                showSnackBar(binding.root, err)
        })

        viewModel.errRes.observe(this, { err: Int ->
           // binding.progressBar.visibility = View.GONE
            if (err != null)
                showSnackBar(binding.root, getString(err))
        })
    }


    fun updateBio() {
         if(binding.edtBio.text.toString().trim().isNotEmpty()) {
             if (isNetworkAvailable(this@EditBioActivity)) {
                 // binding.progressBar.visibility = View.VISIBLE
                 var userId = pref.getString(PrefConstant.USER_ID, "")
            //     viewModel.editBioAdd(token!!, userId!!, binding.edtBio.text.toString(), "")
             } else {
                 showSnackBar(
                     binding.root,
                     resources.getString(R.string.please_check_internet)
                 )
             }
         }else{
             showSnackBar(
                 binding.root,
                 "Please enter your Bio."
             )
         }
    }

    @SuppressLint("SetTextI18n")
    private fun setClickListener() {
        binding.btnSave.setOnClickListener(this)
        binding.tvBack.setOnClickListener(this)
        binding.edtBio.addTextChangedListener {
            if (it?.length ?: 0 > 0) {
                binding.tvChar.text = it!!.length.toString()+"/400"
            }
        }
    }

    override fun onClick(p0: View?) {
        when (p0) {
            binding.tvBack -> finish()
            binding.btnSave -> {
            }
        }
    }
}