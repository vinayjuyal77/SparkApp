package com.app.spark.fragment

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.spark.R
import com.app.spark.activity.camera.CameraActivity
import com.app.spark.activity.camera.CameraCustom
import com.app.spark.activity.camera.CameraXActivity
import com.app.spark.activity.custom_gallery.CustomGalleryActivity
import com.app.spark.activity.flick_gallery.FlickGalleryActivity
import com.app.spark.activity.flick_gallery.FlickGalleryAdapter
import com.app.spark.activity.gallery.GalleryActivity
import com.app.spark.activity.music_gallery.MusicGalleryActivity
import com.app.spark.activity.music_gallery.MusicGalleryAdapter
import com.app.spark.databinding.DialogAddPostBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.app.spark.constants.IntentConstant
import com.app.spark.fragment.groupcall.CreateGroupCallBottomFragment
import com.app.spark.fragment.groupcall.RoomActivity
import java.util.*

class AddPostBottomFragment : BottomSheetDialogFragment(), View.OnClickListener {
    lateinit var binding: DialogAddPostBinding
    var bundle: Bundle? = null
    private var behavior: BottomSheetBehavior<View>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // for testing scenario fail we added
        bundle = arguments
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_add_post, container, false)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener { setupBottomSheet(it) }
        return dialog
    }

    private fun setupBottomSheet(dialogInterface: DialogInterface) {
        val bottomSheetDialog = dialogInterface as BottomSheetDialog
        val bottomSheet = bottomSheetDialog.findViewById<View>(
            com.google.android.material.R.id.design_bottom_sheet
        )
            ?: return
        bottomSheet.setBackgroundResource(R.drawable.ic_bg_curve)
        behavior = BottomSheetBehavior.from(bottomSheet)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setClickListener()

    }

    private fun setClickListener() {
        binding.tvAudio.setOnClickListener(this)
        binding.tvPost.setOnClickListener(this)
        binding.tvFlick.setOnClickListener(this)
        binding.tvWriteSomething.setOnClickListener(this)
        binding.tvVideoEditor.setOnClickListener(this)
        binding.tvStory.setOnClickListener(this)
        binding.tvCreateRoom.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        when (p0) {
            binding.tvStory -> {
                dismiss()
                startActivity(Intent(context, CameraCustom::class.java))
            }
            binding.tvPost -> {
                dismiss()
                openGalleryIntent(binding.tvPost.text.toString())
            }
            binding.tvCreateRoom->{
                dismiss()
                CreateGroupCallBottomFragment().show(requireFragmentManager(), "create_group")
            }
            binding.tvFlick -> {
                dismiss()
                val intent = Intent(context, FlickGalleryActivity::class.java)
                startActivity(intent)
            }
            binding.tvAudio -> {
                dismiss()
                val intent = Intent(context, MusicGalleryActivity::class.java)
                intent.putExtra(IntentConstant.POST_TYPE, binding.tvAudio.text.toString())
                startActivity(intent)
            }
            binding.tvWriteSomething->{

            }
            binding.tvVideoEditor->{
                dismiss()
                val intent = Intent(context, CustomGalleryActivity::class.java)
                startActivity(intent)
            }


        }
    }

    private fun openGalleryIntent(postType: String) {
        val intent = Intent(context, GalleryActivity::class.java)
        intent.putExtra(IntentConstant.POST_TYPE, postType)
        startActivity(intent)
        //startActivity(Intent(context, CameraXActivity::class.java))
       // startActivity(Intent(context, CameraCustom::class.java))
        //startActivity(Intent(context, RoomActivity::class.java))

    }

}