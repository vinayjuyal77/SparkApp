package com.app.spark.activity.custom_gallery

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.spark.R
import com.app.spark.constants.AppConstants
import com.app.spark.databinding.ActivityCustomGalleryBinding
import com.app.spark.utils.BaseActivity


class CustomGalleryActivity : BaseActivity(),View.OnClickListener{
    private lateinit var viewModel: VideoViewModel
    private lateinit var binding: ActivityCustomGalleryBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_custom_gallery)
        binding.activity = this
        initAdapter()
        viewModel = ViewModelProvider(this, VideoModelFactory(VideoRepository(this))).get(VideoViewModel::class.java)
        viewModel.videoLiveData.observe(this, {
            if (it.isNotEmpty()) {
                adapter.updateList(it)
            } else {
                adapter.updateList(emptyList())
            }
        })
    }
    override fun onResume() {
        super.onResume()
        viewModel.forceReload()
    }
    private lateinit var adapter: VideoListAdapter
    private lateinit var addAdapter: AddItemAdapter
    private fun initAdapter() {
        binding.cvNext.setOnClickListener(this)
        binding.rvVideo.layoutManager = GridLayoutManager(this,3)
        adapter = VideoListAdapter(this, mutableListOf())
        binding.rvVideo.adapter = adapter
        adapter.onItemClick = {Pos,VideoModel->
            if(VideoModel.isSelect) addAdapter.updateList(VideoModel)
            else addAdapter.removeItem(VideoModel)
        }
        binding.rvAddList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false).apply {
            scrollToPosition(0)
        }
        addAdapter = AddItemAdapter(this, list)
        binding.rvAddList.adapter = addAdapter
        addAdapter.onItemClaer = {id->
            adapter.isSelected(id)
        }
    }
    var list: MutableList<VideoModel> = mutableListOf()
    override fun onClick(v: View?) {
        when(v?.id){
            R.id.cvNext->{
                AppConstants.BundleConstants.VIDEO_TEST=list[0].path
                Log.d("TAG", "cvNext: "+list[0].path)
                Toast.makeText(this,"Next"+list[0].path,Toast.LENGTH_LONG).show()
                //startActivity(Intent(this, EditorActivity::class.java))
                val i = Intent(this, EditorActivity::class.java)
                i.putExtra("DATA", list[0].path)
                //binding.ivProfilePic.setImageURI(Uri.fromFile(selectedImageFile));
                //binding.ivProfilePic.setImageURI(Uri.fromFile(selectedImageFile));
                startActivity(i)
            }
        }
    }
}