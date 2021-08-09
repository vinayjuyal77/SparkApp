package com.app.spark.activity.setting.security

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.spark.R
import com.app.spark.databinding.ActivityLoginInfoBinding
import com.app.spark.databinding.ActivityPasswordChangedInfoBinding

class PasswordChangedInfo : AppCompatActivity() {
    lateinit var binding:  ActivityPasswordChangedInfoBinding
    lateinit var viewModel: LoginInfoViewModel



    lateinit var gridLayoutManager: LinearLayoutManager
    lateinit var deletedFlickPostAdapter: LoginInfoAdapter



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //  setContentView(R.layout.activity_setting)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_password_changed_info)
        viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(LoginInfoViewModel::class.java)


        gridLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recycerview.layoutManager = gridLayoutManager
        deletedFlickPostAdapter = LoginInfoAdapter(this)
        binding.recycerview.adapter = deletedFlickPostAdapter
        deletedFlickPostAdapter.notifyDataSetChanged()



    }
}