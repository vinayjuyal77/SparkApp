package com.app.spark.activity.create_chat_group

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.app.spark.R
import com.app.spark.activity.add_post.AddPostViewModel
import com.app.spark.activity.main.MainActivity
import com.app.spark.constants.IntentConstant
import com.app.spark.constants.PrefConstant
import com.app.spark.databinding.ActivityAddPostBinding
import com.app.spark.databinding.ActivityChatAddMembersBinding
import com.app.spark.models.ImportantDataResult
import com.app.spark.utils.BaseActivity
import com.app.spark.utils.SharedPrefrencesManager
import com.app.spark.utils.showSnackBar
import com.app.spark.utils.showToastShort
import com.google.gson.Gson

class AddChatMembersActivity : BaseActivity(), View.OnClickListener {
    lateinit var binding: ActivityChatAddMembersBinding
    lateinit var viewModel: AddChatMembersViewModel
    lateinit var pref: SharedPrefrencesManager
    private var token: String? = null
    private var userId: String? = null
    private var adapter: AddMembersAdaptor? = null
    private var selectedUserId: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat_add_members)
        initlizeViewModel()
        pref = SharedPrefrencesManager.getInstance(this)
        token = pref.getString(PrefConstant.ACCESS_TOKEN, "")
        userId = pref.getString(PrefConstant.USER_ID, "")
        setClickListener()
        viewModel.getFollowingAndFollowers(token!!, userId!!, "0", "following")
    }

    private fun setClickListener() {
        binding.imgBack.setOnClickListener(this)
        binding.imgSearch.setOnClickListener(this)
        binding.tvNext.setOnClickListener(this)
    }

    private fun initlizeViewModel() {

        viewModel =
            ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(
                AddChatMembersViewModel::class.java
            )
        viewModel.response.observe(this, {
            hideView(binding.progressBar)
            if (!it.isNullOrEmpty()) {
                adapter = AddMembersAdaptor(this, it)
                binding.rvMembers.adapter = adapter
            }
        })

        viewModel.errString.observe(this, { err: String? ->
            hideView(binding.progressBar)
            if (err != null)
                showToastShort(this, err)
        })

        viewModel.errRes.observe(this, { err: Int ->
            binding.progressBar.visibility = View.GONE
            if (err != null)
                showSnackBar(binding.root, getString(err))
        })
    }

    override fun onClick(p0: View?) {
        when (p0) {
            binding.imgBack -> {
                onBackPressed()
            }
            binding.imgSearch -> {
                if (!binding.edtSearch.text.isNullOrEmpty()) {
                    showView(binding.progressBar)
                    viewModel.getSearchUser(token!!, userId!!, binding.edtSearch.text.toString())
                } else {
                    showSnackBar(binding.root, getString(R.string.search_is_empty))
                }
            }
            binding.tvNext -> {
                selectedUserId = userId
                if (adapter != null) {
                    for (item in adapter!!.getMembersList()) {
                        if (item.isSelected) {
                            selectedUserId = "${selectedUserId},${item.userId}"
                        }
                    }
                }
                val intent = Intent(this, CreateGroupActivity::class.java)
                intent.putExtra(IntentConstant.SELECTED_USER_ID, selectedUserId)
                startActivityForResult(intent, IntentConstant.REQUEST_CODE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IntentConstant.REQUEST_CODE && resultCode == RESULT_OK) {
            finish()
        }
    }

}