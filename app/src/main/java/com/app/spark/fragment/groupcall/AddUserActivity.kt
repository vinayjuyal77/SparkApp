package com.app.spark.fragment.groupcall

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.spark.R
import com.app.spark.constants.AppConstants
import com.app.spark.constants.AppConstants.BundleConstants.ADD_USER_TO_ROOM
import com.app.spark.constants.PrefConstant
import com.app.spark.databinding.ActivityAddUserBinding
import com.app.spark.databinding.ActivityRoomBinding
import com.app.spark.fragment.groupcall.adapter.AddUserListAdapter
import com.app.spark.fragment.groupcall.adapter.SpeakerListAdapter
import com.app.spark.fragment.groupcall.agora.AgoraMainActivity
import com.app.spark.fragment.groupcall.model.ConstantApp
import com.app.spark.models.ResultFollowing
import com.app.spark.utils.*
import java.util.*
import kotlin.collections.ArrayList

class AddUserActivity : BaseActivity(),View.OnClickListener{
    private lateinit var addUserListAdapter: AddUserListAdapter
    private lateinit var binding: ActivityAddUserBinding
    private var listItem : ArrayList<ResultFollowing> = ArrayList()
    private lateinit var viewModel: CreateGroupCallViewModel
    lateinit var pref: SharedPrefrencesManager
    var userId: String? = null
    private var token: String? = null
    lateinit var titleRoom:String
    lateinit var descRoom:String
    lateinit var roomId:String
    var typeRoom:Int=0
    private var offSet: Int = 0
    var searchText:String=""

    private lateinit var layoutManager: LinearLayoutManager
    private var loading = true
    private var pastVisiblesItems = 0
    private var visibleItemCount: Int = 0
    private var totalItemCount: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_user)
        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory(application)
        ).get(CreateGroupCallViewModel::class.java)
        pref = SharedPrefrencesManager.getInstance(this)
        token = pref.getString(PrefConstant.ACCESS_TOKEN, "")
        userId = pref.getString(PrefConstant.USER_ID, "")
        roomId = intent.extras!!.getString(AppConstants.BundleConstants.ROOM_ID)!!
        intilize()
        listiner()
        getFoLLowingFollow()
        recycleViewScrollListner()
        observer()
        editTextChange()
    }

    private fun editTextChange() {

        binding.etSerach.addTextChangedListener(object :
            TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().trim().isNotEmpty()) {
                    Handler().postDelayed({
                        offSet=0
                        searchText=s.toString()
                        getFoLLowingFollow()
                    }, 300)
                } else {
                    if (s.toString().trim().isEmpty()) {
                        offSet=0
                        searchText=""
                        getFoLLowingFollow()
                    }
                }
            }

        })
    }

    private var type : String = "followers"
    private fun observer() {
        viewModel.response.observe(this, Observer {
            dismisDilaog()
            if (listItem.isNotEmpty()) {
                listItem.clear()
            }
            for (i in it.result.indices) {
                it.result[i].myType = type
                listItem.add(it.result[i])
            }
            addUserListAdapter.notifyDataSetChanged()
            offSet = if (it.result.isNotEmpty()) {
                +10
            } else {
                offSet
            }
        })
        viewModel.errString.observe(this, Observer { err: String ->
            dismisDilaog()
            showToastShort(this, err)
        })
        /*viewModel.errRes.observe(this, Observer { err: Int ->
            //  binding.progressBar.visibility = View.GONE
            if (err != null)
                showSnackBar(binding.root, getString(err))
        })*/
        viewModel.errRes.observe(this, { err: Int ->
            if (err == 200) {
                dismisDilaog()
                if(intent.action!!.equals(AppConstants.BundleConstants.CREATE_ROOM)) {
                    startActivity(Intent(this, AgoraMainActivity::class.java).apply {
                        putExtra(ConstantApp.ACTION_KEY_CHANNEL_NAME, roomId)
                    })
                }else if(intent.action.equals(ADD_USER_TO_ROOM)) {
                    onBackPressed()
                }
            }
        })
    }
    private fun getFoLLowingFollow() {
       if (isNetworkAvailable(this)) {
               userId?.let { it1 ->
                   viewModel.getFollowingAndFollowers(
                       userID = it1,
                       offset = offSet.toString(),
                       searchText
                   )
               }
       }
    }

    private fun recycleViewScrollListner() {
        binding.rvUserList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) { //check for scroll down
                    visibleItemCount = layoutManager.childCount
                    totalItemCount = layoutManager.itemCount
                    pastVisiblesItems = layoutManager.findFirstVisibleItemPosition()
                    Log.e(
                        "PAGIGNATION",
                        "onScrolled: $loading$visibleItemCount$pastVisiblesItems$totalItemCount"
                    )
                    if (loading) {
                        if (visibleItemCount + pastVisiblesItems >= totalItemCount) {
                            loading = false
                            // Log.v("...", "Last Item Wow !")
                            // Do pagination.. i.e. fetch new data
                            getFoLLowingFollow()
                            loading = true
                        }
                    }
                }
            }
        })
    }

    private fun listiner() {
        binding.tvletsGo.setOnClickListener(this)
        binding.imageView6.setOnCheckedChangeListener { buttonView, isChecked ->
            for (i in listItem.indices)
                listItem[i].isSelected=isChecked
            addUserListAdapter.notifyDataSetChanged()
        }
    }
    private fun intilize() {
        addUserListAdapter= AddUserListAdapter(this,listItem)
        layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL, false)
        binding.rvUserList.layoutManager!!.apply {
            layoutManager
            scrollToPosition(0)
        }
        /*binding.rvUserList.layoutManager = LinearLayoutManager(this,
            LinearLayoutManager.VERTICAL, false).apply {
            scrollToPosition(0)
        }*/
        binding.rvUserList.itemAnimator = DefaultItemAnimator()
        binding.rvUserList.adapter = addUserListAdapter
    }

   // private var selectedList : ArrayList<UserAddToRoomModel> = ArrayList()
    private var selectedList : ArrayList<Int> = ArrayList()
    override fun onClick(v: View?) {
        when (v) {
            binding.tvletsGo -> {
                selectedList.clear()
                for (i in listItem.indices) {
                    if(listItem[i].isSelected) selectedList.add(listItem[i].userId.toInt())
                }
                showDialog()
                viewModel.addUserToRoom(userId!!.toInt(),roomId.toInt(),selectedList)

               /* selectedList.clear()
                for (i in listItem.indices) {
                    if(listItem[i].isSelected) selectedList.add(listItem[i].userId.toInt())
                }
              Log.d("TAG", "onClick: "+selectedList.toString())
             if(intent.action!!.equals(AppConstants.BundleConstants.CREATE_ROOM)) {
                    showDialog()
                   // viewModel.setCreateRoom(userId!!.toInt(),titleRoom,descRoom,typeRoom,selectedList)
                }else{
                     showDialog()
                     viewModel.addUserToRoom(userId!!.toInt(),roomId.toInt(),selectedList)
                }*/
            }
        }
    }
}