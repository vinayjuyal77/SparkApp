package com.app.spark.fragment.chat

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.app.spark.R
import com.app.spark.databinding.FragmentChatListingBinding
import com.app.spark.utils.SharedPrefrencesManager
import com.app.spark.constants.PrefConstant
import com.app.spark.dialogs.MuteNotificationDialog
import com.app.spark.interfaces.ChatConversationListInterface
import com.app.spark.interfaces.OnConnectionTypeSelected
import com.app.spark.utils.SwipeController
import com.app.spark.utils.SwipeListener
import com.app.spark.utils.isNetworkAvailable


class ProfessionalChatFragment : Fragment(), ChatConversationListInterface, SwipeListener {
    private var userId: String? = null
    private lateinit var binding: FragmentChatListingBinding

    private lateinit var viewModel: ProfessionalViewModel
    lateinit var pref: SharedPrefrencesManager
    var token: String? = null
    private var relativeLayout: RelativeLayout? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_chat_listing, container, false)
        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory(requireActivity().application)
        ).get(ProfessionalViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pref = SharedPrefrencesManager.getInstance(requireContext())
        token = pref.getString(PrefConstant.ACCESS_TOKEN, "")
        userId = pref.getString(PrefConstant.USER_ID, "")
        setTextMsg()
        viewModel.setInitialData(userId, 1)
        setObserver()
        setItemTouchHelper()
    }

    override fun onResume() {
        super.onResume()
        binding.shimmerLayout.startShimmer()
        viewModel.mInitializeSocket()
    }

    private fun setTextMsg() {
        binding.tvText1.text = getString(R.string.hello_sir_madam)
        binding.tvText2.text = getString(R.string.your_network_net_worth)
        binding.tvText3.text = getString(R.string.chat_professional_note)
    }

    private fun setObserver() {
        viewModel.chatList.observe(viewLifecycleOwner, Observer {
            if (!it.isNullOrEmpty()) {
                binding.llChatText.visibility = View.GONE
                binding.rvList.visibility = View.VISIBLE
                binding.rvList.adapter =
                    ConversationListAdapter(requireContext(), it.toMutableList(), 1, this)
            } else {
                binding.llChatText.visibility = View.VISIBLE
                binding.rvList.visibility = View.GONE
                binding.tvNoChats.visibility = View.VISIBLE
                removeNoChats()
            }
            binding.shimmerLayout.stopShimmer()
            binding.shimmerLayout.visibility=View.GONE
        })
    }

    private fun removeNoChats() {
        Handler(Looper.getMainLooper()).postDelayed({
            binding.tvNoChats.visibility = View.GONE
        }, 1000)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        if(isNetworkAvailable(requireActivity()))
        viewModel.removeSocket()
    }

    private fun setItemTouchHelper() {
        val swipeController = SwipeController(this)
        val itemTouchHelper = ItemTouchHelper(swipeController)
        itemTouchHelper.attachToRecyclerView(binding.rvList)
    }

    override fun onLeftToRightSwipe(viewHolder: RecyclerView.ViewHolder?) {
        relativeLayout?.visibility = View.GONE
        val view = viewHolder!!.itemView
        relativeLayout = view.findViewById<RelativeLayout>(R.id.rlSwipeView)
        relativeLayout?.visibility = View.VISIBLE
    }

    override fun onRightToLeftSwipe(viewHolder: RecyclerView.ViewHolder?) {
        val view = viewHolder!!.itemView
        relativeLayout = view.findViewById<RelativeLayout>(R.id.rlSwipeView)
        relativeLayout?.visibility = View.GONE
    }

    override fun onDelete(otherUserId: String) {
        relativeLayout?.visibility = View.GONE
        viewModel.deleteChatFromList(otherUserId)
    }

    override fun onPin(otherUserId: String, pinStatus: String) {
        relativeLayout?.visibility = View.GONE
        if (pinStatus == "0") {
            viewModel.pinUsers(otherUserId, "1")
        } else {
            viewModel.pinUsers(otherUserId, "0")
        }
    }

    override fun onMute(otherUserId: String,muteStatus:String) {
        relativeLayout?.visibility = View.GONE
        if (muteStatus == "0") {
            MuteNotificationDialog(
                requireContext(),
                R.drawable.bg_chat_delete_blue,
                object : OnConnectionTypeSelected {
                    override fun onSelectedConnection(type: String) {
                        viewModel.muteChatFromList(otherUserId, type)
                    }

                }).show()
        } else {
            viewModel.muteChatFromList(otherUserId, "0")
        }
    }
}