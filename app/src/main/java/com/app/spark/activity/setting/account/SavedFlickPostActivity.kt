package com.app.spark.activity.setting.account

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.spark.R
import com.app.spark.activity.post_comments.PostCommentsViewModel
import com.app.spark.databinding.ActivitySavedFlickPostBinding

class SavedFlickPostActivity : AppCompatActivity(), View.OnClickListener {


    lateinit var  binding : ActivitySavedFlickPostBinding
    lateinit var viewModel: SavedFlickPostViewModel
    lateinit var gridLayoutManager: LinearLayoutManager
    lateinit var savedFlickPostAdapter: SavedFlickPostAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       // setContentView()
        binding = DataBindingUtil.setContentView(this,R.layout.activity_saved_flick_post)

        viewModel =
            ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(
                SavedFlickPostViewModel::class.java
            )
        binding.viewmodel = viewModel


        gridLayoutManager = GridLayoutManager(this, 3)
        binding.recyclerviewPostFlick.layoutManager = gridLayoutManager
        savedFlickPostAdapter = SavedFlickPostAdapter(this)
        binding.recyclerviewPostFlick.adapter = savedFlickPostAdapter
        savedFlickPostAdapter.notifyDataSetChanged()

        binding.postClick.setOnClickListener(this)
        binding.flickClick.setOnClickListener(this)


    }

    override fun onClick(p0: View?) {
       when(p0)
       {
           binding.postClick ->
           {
               binding.postClick.background = ContextCompat.getDrawable(this,R.drawable.bg_white_white_storke)
            //   binding.flickTxt.setTextColor(R.color.white)
               binding.flickTxt.setTextColor(ContextCompat.getColor(this, R.color.white));
               binding.postTxt.setTextColor(ContextCompat.getColor(this, R.color.colorBlack));
               binding.flickClick.setBackgroundResource(0)
               binding.title.text = "Saved Posts"
           }

           binding.flickClick ->
           {
               binding.flickClick.background = ContextCompat.getDrawable(this,R.drawable.bg_white_white_storke)
               binding.postClick.setBackgroundResource(0)
               binding.title.text = "Saved Flicks"
               binding.flickTxt.setTextColor(ContextCompat.getColor(this, R.color.colorBlack));
               binding.postTxt.setTextColor(ContextCompat.getColor(this, R.color.white));
           }
       }
    }
}