package com.app.spark.activity.search

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.spark.R
import com.app.spark.activity.UsersProfileActivity
import com.app.spark.activity.main.MainActivity
import com.app.spark.adapter.SearchUserListAdaptor
import com.app.spark.constants.IntentConstant
import com.app.spark.constants.PrefConstant
import com.app.spark.databinding.ActivitySearchBinding
import com.app.spark.models.ImportantDataResult
import com.app.spark.models.ResultFollowing
import com.app.spark.utils.*
import com.google.gson.Gson

class SearchActivity : BaseActivity(), SearchUserListAdaptor.UserSearchListner {
    private lateinit var binding: ActivitySearchBinding
    private lateinit var viewModel: SearchUserViewModel
    private lateinit var pref: SharedPrefrencesManager
    private var loginDetails: ImportantDataResult? = null

    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var adapter: SearchUserListAdaptor
    private var searchList = ArrayList<ResultFollowing>()
    private var isFromChat = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search)
        binding.search = this
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        focuSTrue()
        pref = SharedPrefrencesManager.getInstance(this)
        viewModel =
            ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(
                SearchUserViewModel::class.java
            )
        loginDetails = Gson().fromJson(
            pref.getString(PrefConstant.LOGIN_RESPONSE, ""),
            ImportantDataResult::class.java
        )
        if (intent.hasExtra(IntentConstant.FROM_CHAT)) {
            isFromChat = intent.getBooleanExtra(IntentConstant.FROM_CHAT, false)
        }
        inItAdaptor()
        editTextTextWatcher()
        observer()
    }

    private fun focuSTrue() {
        if(binding.edtSearch.requestFocus()) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    private fun inItAdaptor() {
        adapter = SearchUserListAdaptor(this, searchList, this, isFromChat)
        layoutManager = LinearLayoutManager(this)
        binding.rvSearchData.layoutManager = layoutManager
        binding.rvSearchData.itemAnimator = DefaultItemAnimator()
        binding.rvSearchData.adapter = adapter
    }

    private fun observer() {
        viewModel.responseSearch.observe(this@SearchActivity, Observer {
            if (searchList.isNotEmpty()) {
                searchList.clear()
            }
            searchList.addAll(it.result)
            adapter.notifyDataSetChanged()
            if (searchList.isEmpty()) {
                binding.rvSearchData.visibility = View.GONE
                binding.tvNamaste.visibility = View.VISIBLE
                binding.tvWeWelcomeYou.visibility = View.VISIBLE
                binding.tvFollowPeoplePages.visibility = View.VISIBLE
            } else {
                binding.rvSearchData.visibility = View.VISIBLE
                binding.tvNamaste.visibility = View.GONE
                binding.tvWeWelcomeYou.visibility = View.GONE
                binding.tvFollowPeoplePages.visibility = View.GONE
            }

        })

        viewModel.errString.observe(this, Observer { err: String ->
            //hideView(binding.progressBar)
            showToastShort(this, err)
        })
        viewModel.errRes.observe(this, { err: Int ->
            // binding.progressBar.visibility = View.GONE
            if (err != null)
                showSnackBar(binding.root, getString(err))
        })
    }

    private fun editTextTextWatcher() {
        binding.edtSearch.addTextChangedListener(object :
            TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().trim().isNotEmpty()) {
                    Handler().postDelayed({
                        searchUser(s.toString())
                        binding.imgCancel.visibility = View.VISIBLE
                    }, 300)
                } else {
                    if (s.toString().trim().isEmpty()) {
                        binding.imgCancel.visibility = View.GONE
                        if (searchList.isNotEmpty()) {
                            searchList.clear()
                        }
                        adapter.notifyDataSetChanged()
                        if (searchList.isEmpty()) {
                            binding.rvSearchData.visibility = View.GONE
                            binding.tvNamaste.visibility = View.VISIBLE
                            binding.tvWeWelcomeYou.visibility = View.VISIBLE
                            binding.tvFollowPeoplePages.visibility = View.VISIBLE
                        } else {
                            binding.rvSearchData.visibility = View.VISIBLE
                            binding.tvNamaste.visibility = View.GONE
                            binding.tvWeWelcomeYou.visibility = View.GONE
                            binding.tvFollowPeoplePages.visibility = View.GONE
                        }
                    }
                }
            }

        })
    }

    fun clearSearch() {
        binding.edtSearch.setText("")
        hideKeyboard(this)
        binding.imgCancel.visibility = View.GONE
        if (searchList.isNotEmpty()) {
            searchList.clear()
        }
        adapter.notifyDataSetChanged()
        if (searchList.isEmpty()) {
            binding.rvSearchData.visibility = View.GONE
            binding.tvNamaste.visibility = View.VISIBLE
            binding.tvWeWelcomeYou.visibility = View.VISIBLE
            binding.tvFollowPeoplePages.visibility = View.VISIBLE
        } else {
            binding.rvSearchData.visibility = View.VISIBLE
            binding.tvNamaste.visibility = View.GONE
            binding.tvWeWelcomeYou.visibility = View.GONE
            binding.tvFollowPeoplePages.visibility = View.GONE
        }
    }

    private fun searchUser(key: String) {
        if (isNetworkAvailable(this@SearchActivity)) {
            viewModel.getSearchUser(
                pref.getString(PrefConstant.ACCESS_TOKEN, "")!!,
                loginDetails!!.user_id,
                key
            )
        } else {
            showToastShort(this, resources.getString(R.string.please_check_internet))
        }
    }


    override fun onBackPressed() {
        finish()
    }

    override fun profileDetails(model: ResultFollowing) {
        if (model != null) {
            var userIds = pref.getString(PrefConstant.USER_ID, "")
            if (userIds!!.trim().isNotEmpty() && !model.userId.equals(userIds, true)) {
                val ins = Intent(this@SearchActivity, UsersProfileActivity::class.java)
                ins.putExtra(IntentConstant.PROFILE_ID, model.userId)
                startActivity(ins)
                finish()
            }
        }

    }


}