package com.app.spark.activity.setting.security

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.spark.R
import com.app.spark.databinding.ActivityLoginInfoBinding
import com.app.spark.databinding.ActivityPasswordChangedInfoBinding
import com.app.spark.databinding.ActivityUsernameChangesBinding

class UsernameChangesActivity : AppCompatActivity() {
    lateinit var binding:  ActivityUsernameChangesBinding
    lateinit var viewModel: UsernameChangesViewModel



    lateinit var gridLayoutManager: LinearLayoutManager
    lateinit var deletedFlickPostAdapter: UsernamechangesAdapter

    var type = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //  setContentView(R.layout.activity_setting)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_username_changes)
        viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(UsernameChangesViewModel::class.java)



        val ii  = intent

        if(ii!=null)
        {

            type = ii.getStringExtra("type")!!
            binding.title.text = type
        }

        gridLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recycerview.layoutManager = gridLayoutManager
        deletedFlickPostAdapter = UsernamechangesAdapter(this, type)
        binding.recycerview.adapter = deletedFlickPostAdapter
        deletedFlickPostAdapter.notifyDataSetChanged()



    }
}