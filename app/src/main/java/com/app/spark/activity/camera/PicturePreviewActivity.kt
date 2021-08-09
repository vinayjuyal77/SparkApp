package com.app.spark.activity.camera

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.app.spark.R
import com.app.spark.activity.custom_gallery.adapter.EditingToolsAdapter
import com.app.spark.activity.custom_gallery.adapter.FilterListener
import com.app.spark.activity.custom_gallery.adapter.ToolType
import com.app.spark.activity.custom_gallery.editor_fragment.EmojiBSFragment
import com.app.spark.activity.custom_gallery.editor_fragment.PropertiesBSFragment
import com.app.spark.activity.custom_gallery.editor_fragment.StickerBSFragment
import com.app.spark.activity.custom_gallery.editor_fragment.TextEditorDialogFragment
import com.app.spark.constants.PrefConstant
import com.app.spark.databinding.ActivityPicturePreviewBinding
import com.app.spark.fragment.groupcall.CreateGroupCallViewModel
import com.app.spark.fragment.groupcall.RoomActivity
import com.app.spark.photoeditor.*
import com.app.spark.utils.BaseActivity
import com.app.spark.utils.SharedPrefrencesManager
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.otaliastudios.cameraview.PictureResult
import java.io.File
import java.io.IOException


class PicturePreviewActivity : BaseActivity(), View.OnClickListener, EditingToolsAdapter.OnItemSelected,
    OnPhotoEditorListener, PropertiesBSFragment.Properties, StickerBSFragment.StickerListener,
EmojiBSFragment.EmojiListener, FilterListener {
    companion object {
        var pictureResult: PictureResult? = null
    }
    private lateinit var viewModel: CameraViewModel
    lateinit var pref: SharedPrefrencesManager
    var userId: String? = null
    private var mPhotoEditor: PhotoEditor? = null
    private lateinit var binding: ActivityPicturePreviewBinding
    private var propertiesBSFragment: PropertiesBSFragment? = null
    private var progressDialog: ProgressDialog? = null
    private var mStickerBSFragment: StickerBSFragment? = null
    private var mEmojiBSFragment: EmojiBSFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_picture_preview)
        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory(application)
        ).get(CameraViewModel::class.java)
        pref = SharedPrefrencesManager.getInstance(this)
        userId = pref.getString(PrefConstant.USER_ID, "")
        intiView()
        val result = pictureResult ?: run {
            finish()
            return
        }
        try {
            result.toBitmap() { bitmap -> binding.imageView.setImageBitmap(bitmap)
                Glide.with(this).load(bitmap).into(binding.ivImage.getSource())
                /*mPhotoEditorView!!.getSource().setImageBitmap(bitmap)*/}
        } catch (e: UnsupportedOperationException) {
            binding.imageView.setImageDrawable(ColorDrawable(Color.GREEN))
            Toast.makeText(this, "Can't preview this format: " + result.getFormat(), Toast.LENGTH_LONG).show()
        }

        viewModel.errRes.observe(this, { err: Int ->
            if (err == 200) {
                dismisDilaog()
                finish()
                Toast.makeText(
                    applicationContext,
                    "Story add successfully...",
                    Toast.LENGTH_LONG
                ).show()
            }else{
                dismisDilaog()
                Toast.makeText(
                    applicationContext,
                    getString(err),
                    Toast.LENGTH_LONG
                ).show()
            }
        })
        viewModel.errString.observe(this, { msg: String ->
            dismisDilaog()
            Toast.makeText(
                applicationContext,
                msg,
                Toast.LENGTH_LONG
            ).show()
        })
    }

    private fun intiView() {
        Glide.with(this).load(R.drawable.trans).centerCrop().into(binding.ivImage.source)
        progressDialog = ProgressDialog(this)
        mStickerBSFragment = StickerBSFragment()
        mStickerBSFragment!!.setStickerListener(this)
        mEmojiBSFragment = EmojiBSFragment()
        mEmojiBSFragment!!.setEmojiListener(this)
        propertiesBSFragment = PropertiesBSFragment()
        propertiesBSFragment!!.setPropertiesChangeListener(this)
        binding.ivText.setOnClickListener(this)
        binding.ivEmoji.setOnClickListener(this)
        binding.ivSticker.setOnClickListener(this)
        binding.ivBrush.setOnClickListener(this)
        binding.ivDone.setOnClickListener(this)
        binding.ivUndo.setOnClickListener(this)
        binding.ivSave.setOnClickListener(this)
        binding.ivClose.setOnClickListener(this)
        binding.tvStoryAdd.setOnClickListener(this)

        mPhotoEditor = PhotoEditor.Builder(this, binding.ivImage)
            .setPinchTextScalable(true)
            .setDeleteView(binding.imgDelete)
            .build()
        mPhotoEditor!!.setOnPhotoEditorListener(this)
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.ivText -> {
                val textEditorDialogFragment: TextEditorDialogFragment =
                    TextEditorDialogFragment.show(this, 0)
                textEditorDialogFragment.setOnTextEditorListener(object :
                    TextEditorDialogFragment.TextEditor {
                    override fun onDone(inputText: String?, colorCode: Int, position: Int) {
                        val styleBuilder = TextStyleBuilder()
                        styleBuilder.withTextColor(colorCode)
                        val typeface: Typeface? = ResourcesCompat.getFont(
                            applicationContext,
                            TextEditorDialogFragment.getDefaultFontIds(applicationContext)
                                .get(position)
                        )
                        styleBuilder.withTextFont(typeface!!)
                        mPhotoEditor!!.addText(inputText, styleBuilder, position)
                    }
                })
            }
            R.id.ivBrush -> {
                setDrawingMode()
            }
            R.id.ivSticker -> {
                showBottomSheetDialogFragment(mStickerBSFragment)
            }
            R.id.ivSave -> {
                saveImage()
            }
            R.id.ivEmoji -> {
                showBottomSheetDialogFragment(mEmojiBSFragment)
            }
            R.id.ivClose -> {
                startActivity(Intent(this, CameraCustom::class.java))
                finish()
            }
            R.id.ivDone->{
                setDrawingMode()
            }
            R.id.ivUndo->{
                mPhotoEditor!!.undo()
            }

            R.id.tvStoryAdd->{
                sendToApi()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun sendToApi() {
        showDialog()
        var file: File? = null
        file = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    .toString() +  File.separator + ""
                        + System.currentTimeMillis() + ".png"
            )
        } else {
            File(Environment.getExternalStorageDirectory().toString() +  File.separator + ""
                    + System.currentTimeMillis() + ".png")
        }
        try {
            file!!.createNewFile()
            mPhotoEditor!!.saveAsFile(file.absolutePath,object :
                PhotoEditor.OnSaveListener {
                override fun onSuccess(imagePath: String) {
                    binding.ivImage.source.setImageURI(Uri.fromFile(File(imagePath)))
                    viewModel.addStory(userId!!.toInt(),File(imagePath))
                }
                override fun onFailure(exception: Exception) {
                    Toast.makeText(
                        applicationContext,
                        "Saving Failed...",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            })
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @SuppressLint("MissingPermission")
    private fun saveImage() {
        progressDialog!!.setMessage("Saving Image ....")
        progressDialog!!.show()
        progressDialog!!.setCancelable(false)
        /*val file = File(
            Environment.getExternalStorageDirectory()
                .toString() + File.separator + ""
                    + System.currentTimeMillis() + ".png"
        )*/
        var file: File? = null
        file = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    .toString() +  File.separator + ""
                        + System.currentTimeMillis() + ".png"
            )
        } else {
            File(Environment.getExternalStorageDirectory().toString() +  File.separator + ""
                    + System.currentTimeMillis() + ".png")
        }
        try {
            file!!.createNewFile()
            val saveSettings: SaveSettings = SaveSettings.Builder()
                .setClearViewsEnabled(true)
                .setTransparencyEnabled(false)
                .build()
            mPhotoEditor!!.saveAsFile(file.absolutePath, saveSettings, object :
                PhotoEditor.OnSaveListener {
                override fun onSuccess(imagePath: String) {
                    progressDialog!!.dismiss()
                    binding.ivImage.source.setImageURI(Uri.fromFile(File(imagePath)))
                    Toast.makeText(
                        applicationContext,
                        "Saved successfully...",
                        Toast.LENGTH_LONG
                    ).show()
                }
                override fun onFailure(exception: Exception) {
                    Toast.makeText(
                        applicationContext,
                        "Saving Failed...",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            })
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    private fun setDrawingMode() {
        if (mPhotoEditor!!.getBrushDrawableMode()) {
            mPhotoEditor!!.setBrushDrawingMode(false)
            binding.llSelect.visibility=View.VISIBLE
            binding.llPaint.visibility=View.GONE
        } else {
            mPhotoEditor!!.setBrushDrawingMode(true)
            binding.llSelect.visibility=View.GONE
            binding.llPaint.visibility=View.VISIBLE
            propertiesBSFragment!!.show(supportFragmentManager, propertiesBSFragment!!.getTag())
        }
    }
    private fun showBottomSheetDialogFragment(fragment: BottomSheetDialogFragment?) {
        if (fragment == null || fragment.isAdded) {
            return
        }
        fragment.show(supportFragmentManager, fragment.tag)
    }

    override fun onToolSelected(toolType: ToolType?) {

    }

    override fun onEditTextChangeListener(
        rootView: View?,
        text: String?,
        colorCode: Int,
        pos: Int
    ) {

    }

    override fun onAddViewListener(viewType: ViewType?, numberOfAddedViews: Int) {

    }

    override fun onRemoveViewListener(viewType: ViewType?, numberOfAddedViews: Int) {

    }

    override fun onStartViewChangeListener(viewType: ViewType?) {

    }

    override fun onStopViewChangeListener(viewType: ViewType?) {

    }

    override fun onColorChanged(colorCode: Int) {
        mPhotoEditor!!.brushColor = colorCode
    }

    override fun onOpacityChanged(opacity: Int) {
        mPhotoEditor!!.setOpacity(opacity)
    }

    override fun onBrushSizeChanged(brushSize: Int) {
        mPhotoEditor!!.brushSize=brushSize.toFloat()
    }

    override fun onStickerClick(bitmap: Bitmap?) {
        mPhotoEditor!!.addImage(bitmap)
    }

    override fun onEmojiClick(emojiUnicode: String?) {
        mPhotoEditor!!.addEmoji(emojiUnicode)
    }

    override fun onFilterSelected(photoFilter: PhotoFilter?) {

    }
}