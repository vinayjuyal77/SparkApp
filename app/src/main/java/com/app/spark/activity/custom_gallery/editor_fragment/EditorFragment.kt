package com.app.spark.activity.custom_gallery.editor_fragment

import android.app.ProgressDialog
import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.graphics.Typeface
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Surface
import android.view.TextureView.SurfaceTextureListener
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnticipateOvershootInterpolator
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.app.spark.R
import com.app.spark.activity.custom_gallery.EditorActivity
import com.app.spark.activity.custom_gallery.adapter.EditingToolsAdapter
import com.app.spark.activity.custom_gallery.adapter.FilterListener
import com.app.spark.activity.custom_gallery.adapter.FilterViewAdapter
import com.app.spark.activity.custom_gallery.adapter.ToolType
import com.app.spark.constants.AppConstants.BundleConstants.VIDEO_TEST
import com.app.spark.databinding.FragmentEditorBinding
import com.app.spark.photoeditor.*
import com.bumptech.glide.Glide
import com.github.hiteshsondhi88.libffmpeg.FFmpeg
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.io.IOException

class EditorFragment: Fragment(), View.OnClickListener,EditingToolsAdapter.OnItemSelected,
    OnPhotoEditorListener, PropertiesBSFragment.Properties, StickerBSFragment.StickerListener,
    EmojiBSFragment.EmojiListener, FilterListener {
    private lateinit var binding: FragmentEditorBinding
    private var videoPath=""
    private var mediaPlayer: MediaPlayer? = null
    private var mPhotoEditor: PhotoEditor? = null
    private var propertiesBSFragment: PropertiesBSFragment? = null
    var fFmpeg: FFmpeg? = null
    private var progressDialog: ProgressDialog? = null
    private var mStickerBSFragment: StickerBSFragment? = null
    private var mEmojiBSFragment: EmojiBSFragment? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_editor, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        videoPath=VIDEO_TEST
        initViews()
        initAdapter()
    }
    private val onCompletionListener =
        OnCompletionListener { mediaPlayer -> mediaPlayer.start() }

    private lateinit var toolAdapter: EditingToolsAdapter
    private lateinit var mFilterViewAdapter: FilterViewAdapter
    private fun initAdapter() {
        binding.ivPlayPause.setOnClickListener(this)
        binding.ivUndo.setOnClickListener(this)
        binding.ivRedo.setOnClickListener(this)
        binding.ivClear.setOnClickListener(this)
        binding.rvItemSelect.layoutManager = LinearLayoutManager(activity,
            LinearLayoutManager.HORIZONTAL, false).apply {
            scrollToPosition(0)
        }
        toolAdapter = EditingToolsAdapter(this)
        binding.rvItemSelect.adapter = toolAdapter

        binding.rvFilterView.layoutManager = LinearLayoutManager(activity,
            LinearLayoutManager.HORIZONTAL, false).apply {
            scrollToPosition(0)
        }
        mFilterViewAdapter = FilterViewAdapter(this)
        binding.rvFilterView.adapter = mFilterViewAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mediaPlayer!!.stop()
        mediaPlayer!!.release()
    }
    override fun onToolSelected(toolType: ToolType?) {
        when (toolType) {
            ToolType.MUSIC -> {
                Toast.makeText(activity,"MUSIC",Toast.LENGTH_LONG).show()
            }
            ToolType.STICKER -> {
                showBottomSheetDialogFragment(mStickerBSFragment)
            }
            ToolType.EMOJI -> {
               showBottomSheetDialogFragment(mEmojiBSFragment)
            }
            ToolType.TEXT -> {
                val textEditorDialogFragment: TextEditorDialogFragment =
                    TextEditorDialogFragment.show(activity as EditorActivity, 0)
                textEditorDialogFragment.setOnTextEditorListener(object :
                    TextEditorDialogFragment.TextEditor {
                    override fun onDone(inputText: String?, colorCode: Int, position: Int) {
                        val styleBuilder = TextStyleBuilder()
                        styleBuilder.withTextColor(colorCode)
                        val typeface: Typeface? = ResourcesCompat.getFont(
                            requireContext(),
                            TextEditorDialogFragment.getDefaultFontIds(activity)
                                .get(position)
                        )
                        styleBuilder.withTextFont(typeface!!)
                        mPhotoEditor!!.addText(inputText, styleBuilder, position)
                    }
                })
            }
            ToolType.FILTER -> {
                binding.ivClear.visibility=View.VISIBLE
                binding.rvFilterView.visibility=View.VISIBLE
                showFilter(true)
            }
            ToolType.BRUSH -> {
                setDrawingMode()
            }
            ToolType.SPLIT -> {
                Toast.makeText(activity,"SPLIT",Toast.LENGTH_LONG).show()
            }
            ToolType.ERASER -> {
                Toast.makeText(activity,"ERASER",Toast.LENGTH_LONG).show()
            }
            ToolType.TRIM -> {
                Toast.makeText(activity,"TRIM",Toast.LENGTH_LONG).show()
            }
            ToolType.CROP -> {
                Toast.makeText(activity,"CROP",Toast.LENGTH_LONG).show()
            }
            ToolType.ROTATE -> {
                Toast.makeText(activity,"ROTATE",Toast.LENGTH_LONG).show()
            }
            ToolType.SPEED -> {
                Toast.makeText(activity,"SPEED",Toast.LENGTH_LONG).show()
            }
            ToolType.DUPLICATE -> {
                Toast.makeText(activity,"DUPLICATE",Toast.LENGTH_LONG).show()
            }
            ToolType.TRANSITION -> {
                Toast.makeText(activity,"TRANSITION",Toast.LENGTH_LONG).show()
            }
        }
    }
    private var mIsFilterVisible = false
    private val mConstraintSet = ConstraintSet()
    fun showFilter(isVisible: Boolean) {
        mIsFilterVisible = isVisible
        mConstraintSet.clone(binding.rootView)
        if (isVisible) {
            mConstraintSet.clear(binding.rvFilterView.getId(), ConstraintSet.START)
            mConstraintSet.connect(
                binding.rvFilterView.getId(), ConstraintSet.START,
                ConstraintSet.PARENT_ID, ConstraintSet.START
            )
            mConstraintSet.connect(
                binding.rvFilterView.getId(), ConstraintSet.END,
                ConstraintSet.PARENT_ID, ConstraintSet.END
            )
        } else {
            mConstraintSet.connect(
                binding.rvFilterView.getId(), ConstraintSet.START,
                ConstraintSet.PARENT_ID, ConstraintSet.END
            )
            mConstraintSet.clear(binding.rvFilterView.getId(), ConstraintSet.END)
        }
        val changeBounds = ChangeBounds()
        changeBounds.duration = 350
        changeBounds.interpolator = AnticipateOvershootInterpolator(1.0f)
        TransitionManager.beginDelayedTransition(binding.rootView, changeBounds)
        mConstraintSet.applyTo(binding.rootView)
    }
    private fun showBottomSheetDialogFragment(fragment: BottomSheetDialogFragment?) {
        if (fragment == null || fragment.isAdded) {
            return
        }
        fragment.show(requireFragmentManager(), fragment.tag)
    }
    private fun initViews() {
        Glide.with(this).load(R.drawable.trans).centerCrop().into(binding.ivImage.source)
        fFmpeg = FFmpeg.getInstance(activity)
        progressDialog = ProgressDialog(activity)
        mStickerBSFragment = StickerBSFragment()
        mStickerBSFragment!!.setStickerListener(this)
        mEmojiBSFragment = EmojiBSFragment()
        mEmojiBSFragment!!.setEmojiListener(this)
        propertiesBSFragment = PropertiesBSFragment()
        propertiesBSFragment!!.setPropertiesChangeListener(this)
        mPhotoEditor = PhotoEditor.Builder(activity, binding.ivImage)
            .setPinchTextScalable(true) // set flag to make text scalable when pinch
            .setDeleteView(binding.imgDelete) //.setDefaultTextTypeface(mTextRobotoTf)
            //.setDefaultEmojiTypeface(mEmojiTypeFace)
            .build() // build photo editor sdk
        mPhotoEditor!!.setOnPhotoEditorListener(this)
        binding.videoSurface.surfaceTextureListener = object : SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(
                surfaceTexture: SurfaceTexture,
                i: Int,
                i1: Int
            ) {
                //   activityHomeBinding.videoSurface.getLayoutParams().height=640;
                //   activityHomeBinding.videoSurface.getLayoutParams().width=720;
                val surface = Surface(surfaceTexture)
                try {
                    mediaPlayer = MediaPlayer()
                    //                    mediaPlayer.setDataSource("http://daily3gp.com/vids/747.3gp");
                    mediaPlayer!!.setDataSource(videoPath)
                    mediaPlayer!!.setSurface(surface)
                    mediaPlayer!!.prepare()
                    mediaPlayer!!.setOnCompletionListener(onCompletionListener)
                    mediaPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
                    mediaPlayer!!.start()
                } catch (e: IllegalArgumentException) {
                    // TODO Auto-generated catch block
                    e.printStackTrace()
                } catch (e: SecurityException) {
                    // TODO Auto-generated catch block
                    e.printStackTrace()
                } catch (e: IllegalStateException) {
                    // TODO Auto-generated catch block
                    e.printStackTrace()
                } catch (e: IOException) {
                    // TODO Auto-generated catch block
                    e.printStackTrace()
                }
            }
            override fun onSurfaceTextureSizeChanged(
                surfaceTexture: SurfaceTexture,
                i: Int,
                i1: Int
            ) {
            }
            override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
                return false
            }

            override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) {}
        }
    }
    private fun setDrawingMode() {
        if (mPhotoEditor!!.getBrushDrawableMode()) {
            mPhotoEditor!!.setBrushDrawingMode(false)
            //binding.imgDraw.setBackgroundColor(ContextCompat.getColor(this, R.color.black_trasp))
        } else {
            mPhotoEditor!!.setBrushDrawingMode(true)
            //binding.imgDraw.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary))
            propertiesBSFragment!!.show(requireFragmentManager(), propertiesBSFragment!!.getTag())
        }
    }
    override fun onClick(v: View?) {
        when(v?.id){
            R.id.ivPlayPause->{
                if(mediaPlayer!!.isPlaying){
                    mediaPlayer!!.pause()
                    binding.ivPlayPause.setImageDrawable(getDrawable(requireContext(),R.drawable.exo_icon_play))
                }else {
                    mediaPlayer!!.start()
                    binding.ivPlayPause.setImageDrawable(getDrawable(requireContext(),R.drawable.exo_icon_pause))
                }
            }
            R.id.ivUndo->{
                mPhotoEditor!!.undo()
            }
            R.id.ivRedo->{
                mPhotoEditor!!.redo()
            }
            R.id.ivClear->{
                binding.ivClear.visibility=View.GONE
                binding.rvFilterView.visibility=View.GONE
                showFilter(false)
            }
        }
    }

    override fun onEditTextChangeListener(
        rootView: View?,
        text: String?,
        colorCode: Int,
        pos: Int
    ) {
        val textEditorDialogFragment = TextEditorDialogFragment.show(
            activity as EditorActivity,
            text!!, colorCode, pos
        )
        textEditorDialogFragment.setOnTextEditorListener(object :
            TextEditorDialogFragment.TextEditor {
            override fun onDone(inputText: String?, colorCode: Int, position: Int) {
                val styleBuilder = TextStyleBuilder()
                styleBuilder.withTextColor(colorCode)
                val typeface = ResourcesCompat.getFont(
                    requireContext(),
                    TextEditorDialogFragment.getDefaultFontIds(activity)[position]
                )
                styleBuilder.withTextFont(typeface!!)
                mPhotoEditor!!.editText(rootView!!, inputText, styleBuilder, position)
            }
        })
    }

    override fun onAddViewListener(viewType: ViewType?, numberOfAddedViews: Int) {
        Log.d("TAG",
            "onAddViewListener() called with: viewType = [$viewType], numberOfAddedViews = [$numberOfAddedViews]"
        )
    }

    override fun onRemoveViewListener(viewType: ViewType?, numberOfAddedViews: Int) {
        Log.d("TAG",
            "onAddViewListener() called with: viewType = [$viewType], numberOfAddedViews = [$numberOfAddedViews]"
        )
    }

    override fun onStartViewChangeListener(viewType: ViewType?) {
        Log.d("TAG", "onStartViewChangeListener() called with: viewType = [$viewType]")
    }

    override fun onStopViewChangeListener(viewType: ViewType?) {
        Log.d("TAG", "onStopViewChangeListener() called with: viewType = [$viewType]")
    }

    override fun onColorChanged(colorCode: Int) {
        mPhotoEditor!!.brushColor = colorCode
    }

    override fun onOpacityChanged(opacity: Int) {

    }

    override fun onBrushSizeChanged(brushSize: Int) {

    }

    override fun onStickerClick(bitmap: Bitmap?) {
        //binding.imgDraw.setBackgroundColor(ContextCompat.getColor(this, R.color.black_trasp))
        mPhotoEditor!!.addImage(bitmap)
    }
    override fun onEmojiClick(emojiUnicode: String?) {
        mPhotoEditor!!.addEmoji(emojiUnicode)
    }
    override fun onFilterSelected(photoFilter: PhotoFilter?) {
        mPhotoEditor!!.setFilterEffect(photoFilter)
    }
}