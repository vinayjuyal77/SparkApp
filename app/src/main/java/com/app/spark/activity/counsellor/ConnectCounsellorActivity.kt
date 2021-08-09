package com.app.spark.activity.counsellor


import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.app.spark.R
import com.app.spark.databinding.ActivityConnectCounsellorBinding
import com.app.spark.models.TagListModel
import com.app.spark.utils.BaseActivity
import com.app.spark.viewmodel.ConnectCounsellorViewModel
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.activity_session_over.view.*

class ConnectCounsellorActivity : BaseActivity() {
    private lateinit var binding: ActivityConnectCounsellorBinding
    private val viewModel : ConnectCounsellorViewModel by lazy {
        ViewModelProvider(this,ViewModelProvider.AndroidViewModelFactory(application)).get(ConnectCounsellorViewModel::class.java)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_connect_counsellor)
        binding.activity = this
        binding.viewModel=viewModel
        /*viewModel.getResponse().observe(this@CreateYourProfileActivity, this)*/
        binding.cvProceed.setOnClickListener {
            startActivity(Intent(this, CounsellorInformationActivity::class.java))
        }

        tagList.add(TagListModel(getString(R.string.longle),false,false))
        tagList.add(TagListModel(getString(R.string.suicidal),false,false))
        tagList.add(TagListModel(getString(R.string.Burdened),false,false))
        tagList.add(TagListModel(getString(R.string.Irritated),false,false))
        tagList.add(TagListModel(getString(R.string.Overthinking),false,false))
        tagList.add(TagListModel(getString(R.string.Worrried),false,false))
        tagList.add(TagListModel(getString(R.string.Remembering),false,false))
        tagList.add(TagListModel(getString(R.string.Insecure),false,false))
        val flexboxLayoutManager = FlexboxLayoutManager(this).apply {
            flexWrap = FlexWrap.WRAP
            flexDirection = FlexDirection.ROW
            alignItems = AlignItems.STRETCH
        }
        binding.rvTaglist.apply {
            layoutManager = flexboxLayoutManager
            adapter = TagListAdapter(applicationContext,tagList)
        }
        /*binding.rvTagSituation.apply {
            layoutManager = flexboxLayoutManager
            adapter = TagListAdapter(applicationContext,tagList)
        }*/




        val adapter = ArrayAdapter.createFromResource(this,
            R.array.status, android.R.layout.simple_spinner_item)
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // Apply the adapter to the spinner
        binding.spinner.adapter = adapter

        binding.spinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.request.relationshipStatus=binding.spinner.selectedItem.toString()
            }
        }

    }
    private var tagList : ArrayList<TagListModel> = ArrayList()


}