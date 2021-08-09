package com.app.spark.activity.setting.privacy

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.spark.R
import com.app.spark.activity.setting.account.DeletedFlickPostAdapter
import com.app.spark.activity.setting.account.DeletedFlickPostViewModel
import com.app.spark.databinding.ActivityBlockedBinding
import com.app.spark.databinding.ActivityDeletedPostBinding

class BlockedActivity : AppCompatActivity() {

    lateinit var binding: ActivityBlockedBinding
    lateinit var viewModel: BlockedViewModel
    lateinit var gridLayoutManager: LinearLayoutManager
    lateinit var blockedAdapter: BlockedAdapter

    var type = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setContentView()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_blocked)

        viewModel =
            ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(
                BlockedViewModel::class.java
            )
        binding.viewmodel = viewModel


         val ii = intent
        if(ii!=null)
        {
            type = ii.getStringExtra("type")!!

        }


        if(type.equals("Blocked"))
        {

            binding.title.text = "Blocked Account"

        }
        else if(type.equals("Restricted"))
        {
            binding.title.text = type
        }
        else if(type.equals("Muted"))
        {
            binding.title.text = type
        }
        else if(type.equals("Reported"))
        {
            binding.title.text = type
        }


        binding.back.setOnClickListener {

            onBackPressed()
        }

        gridLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recyclerview.layoutManager = gridLayoutManager
        blockedAdapter = BlockedAdapter(this, type)
        binding.recyclerview.adapter = blockedAdapter
        blockedAdapter.notifyDataSetChanged()

    }
}