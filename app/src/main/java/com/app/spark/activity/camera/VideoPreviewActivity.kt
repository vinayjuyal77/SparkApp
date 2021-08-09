package com.app.spark.activity.camera


import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
import com.app.spark.databinding.ActivityVideoPreviewBinding
import com.app.spark.photoeditor.*
import com.app.spark.utils.BaseActivity
import com.app.spark.utils.SharedPrefrencesManager
import com.bumptech.glide.Glide
import com.github.hiteshsondhi88.libffmpeg.FFmpeg
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler
import com.github.hiteshsondhi88.libffmpeg.FFmpegLoadBinaryResponseHandler
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.otaliastudios.cameraview.VideoResult
import com.simform.videooperations.*
import java.io.File
import java.io.IOException
import java.util.*


class VideoPreviewActivity : BaseActivity(), View.OnClickListener,
    EditingToolsAdapter.OnItemSelected,
    OnPhotoEditorListener, PropertiesBSFragment.Properties, StickerBSFragment.StickerListener,
    EmojiBSFragment.EmojiListener, FilterListener {
    companion object {

        var videoResult: VideoResult? = null

    }





    private lateinit var viewModel: CameraViewModel
    lateinit var pref: SharedPrefrencesManager
    var userId: String? = null

    private lateinit var binding: ActivityVideoPreviewBinding
    private var mPhotoEditor: PhotoEditor? = null
    private var propertiesBSFragment: PropertiesBSFragment? = null
    private var progressDialog: ProgressDialog? = null
    private var mStickerBSFragment: StickerBSFragment? = null
    private var mEmojiBSFragment: EmojiBSFragment? = null
    var fFmpeg: FFmpeg? = null
    private var mediaPlayer: MediaPlayer? = null
    private var videoPath = ""
    private var uriPath: File? = null
    private var exeCmd: ArrayList<String>? = null
    private var imagePath = ""

    private val DRAW_CANVASW = 0
    private val DRAW_CANVASH = 0

    val ffmpegQueryExtension = FFmpegQueryExtension()
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_video_preview)
        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory(application)
        ).get(CameraViewModel::class.java)
        pref = SharedPrefrencesManager.getInstance(this)
        userId = pref.getString(PrefConstant.USER_ID, "")

        progressDialog = ProgressDialog(this)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {

            } else {
                // No explanation needed; request the permission
                val MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE = 321
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE), MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE);
            }
        } else {

        }


        val result = videoResult ?: run {
            finish()
            return
        }
        uriPath = result.file
        videoPath = result.file.toString()


        observer();

        intiView()
        viewModel.errRes.observe(this, { err: Int ->
            if (err == 200) {
                dismisDilaog()
                finish()
                Toast.makeText(
                    applicationContext,
                    "Story add successfully...",
                    Toast.LENGTH_LONG
                ).show()
            } else {
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




    fun changeExtension(f: File, newExtension: String): File? {
        val i = f.name.lastIndexOf('.')
        val name = f.name.substring(0, i)
        return File(f.parent, name + newExtension)
    }


    private fun observer()
    {
        viewModel.errRes.observe(this,  {

            if(it !=null)
            {
                if(it == 200 )
                {
                    binding.progressBar.visibility =View.GONE
                }
                else
                {
                    binding.progressBar.visibility =View.GONE

                }
            }

        })
    }

    private fun intiView() {
        Glide.with(this).load(R.drawable.trans).centerCrop().into(binding.ivImage.source)
        fFmpeg = FFmpeg.getInstance(this)
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
        binding.videoSurface.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
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
                    mediaPlayer!!.setDataSource(uriPath.toString())
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
        exeCmd = ArrayList<String>()
        try {
            fFmpeg!!.loadBinary(object : FFmpegLoadBinaryResponseHandler {
                override fun onFailure() {
                    Log.d("binaryLoad", "onFailure")
                }

                override fun onSuccess() {
                    Log.d("binaryLoad", "onSuccess")
                }

                override fun onStart() {
                    Log.d("binaryLoad", "onStart")
                }

                override fun onFinish() {
                    Log.d("binaryLoad", "onFinish")
                }
            })
        } catch (e: FFmpegNotSupportedException) {
            e.printStackTrace()
        }
    }

    private val onCompletionListener = OnCompletionListener { mediaPlayer -> mediaPlayer.start() }
    override fun onBackPressed() {
        super.onBackPressed()
        if (mediaPlayer!!.isPlaying) {
            mediaPlayer!!.stop()
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivText -> {
                /*val textEditorDialogFragment: TextEditorDialogFragment =
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
                })*/
                val textEditorDialogFragment = TextEditorDialogFragment.show(this, 0)
                textEditorDialogFragment.setOnTextEditorListener(object :
                    TextEditorDialogFragment.TextEditor {
                    override fun onDone(inputText: String?, colorCode: Int, position: Int) {
                        val styleBuilder = TextStyleBuilder()
                        styleBuilder.withTextColor(colorCode)
                        val typeface = ResourcesCompat.getFont(
                            applicationContext,
                            TextEditorDialogFragment.getDefaultFontIds(applicationContext)[position]
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
                saveVideo()
            }
            R.id.ivEmoji -> {
                showBottomSheetDialogFragment(mEmojiBSFragment)
            }
            R.id.ivClose -> {

                startActivity(Intent(this, CameraCustom::class.java))
                finish()

            }
            R.id.ivDone -> {
                setDrawingMode()
            }
            R.id.ivUndo -> {
                mPhotoEditor!!.undo()
            }
            R.id.tvStoryAdd -> {

                binding.progressBar.visibility =View.VISIBLE
                viewModel.addStory(userId!!.toInt(), File(videoPath!!))
               // sendToApi()
            }
        }
    }


    @SuppressLint("MissingPermission")
    private fun sendToApi() {
        var file: File? = null
        file = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    .toString() + File.separator + ""
                        + System.currentTimeMillis() + ".png"
            )
            /*  var fileW=ContextWrapper(this).getExternalFilesDir(Environment.DIRECTORY_PICTURES)
              File(
                  fileW, File.separator + "" + System.currentTimeMillis() + ".png"
              )*/
        } else {
            File(
                Environment.getExternalStorageDirectory().toString() + File.separator + ""
                        + System.currentTimeMillis() + ".png"
            )
        }
        try {
            file.createNewFile()
            val saveSettings: SaveSettings = SaveSettings.Builder()
                .setClearViewsEnabled(true)
                .setTransparencyEnabled(false)
                .build()
            mPhotoEditor!!.saveAsFile(file.absolutePath, saveSettings, object :
                PhotoEditor.OnSaveListener {
                override fun onSuccess(imagePathN: String) {
                    imagePath = imagePathN
                    binding.ivImage.source.setImageURI(Uri.fromFile(File(imagePathN)))
                    /*Toast.makeText(
                        applicationContext,
                        "Saved successfully...",
                        Toast.LENGTH_LONG
                    ).show()*/
                    applayWaterMarkSend()
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

    private fun applayWaterMarkSend() {
        var output: File? = null
        output = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    .toString() + File.separator + ""
                        + System.currentTimeMillis() + ".mp4"
            )

            /* var fileW=ContextWrapper(this).getExternalFilesDir(Environment.DIRECTORY_MOVIES)
             File(
                 fileW, File.separator + "" + System.currentTimeMillis() + ".mp4"
             )*/
        } else {
            File(
                Environment.getExternalStorageDirectory().toString() + File.separator + ""
                        + System.currentTimeMillis() + ".mp4"
            )
        }


        output.createNewFile()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            try {

                exeCmd!!.add("-y")
                exeCmd!!.add("-i")
                exeCmd!!.add(videoPath)
                //            exeCmd.add("-framerate 30000/1001 -loop 1");
                exeCmd!!.add("-i")
                exeCmd!!.add(imagePath)
                exeCmd!!.add("-filter_complex")

                exeCmd!!.add("[1:v]scale=$DRAW_CANVASW:$DRAW_CANVASH[ovrl];[0:v][ovrl]overlay=x=0:y=0")
                exeCmd!!.add("-c:v")
                exeCmd!!.add("libx264")
                exeCmd!!.add("-preset")
                exeCmd!!.add("ultrafast")
                exeCmd!!.add(output.absolutePath)




                newCommand = arrayOfNulls<String>(exeCmd!!.size)
                for (j in exeCmd!!.indices) {
                    newCommand!![j] = exeCmd!![j]
                }
                for (k in newCommand!!.indices) {
                    Log.d("CMD==>>", newCommand!!.get(k).toString() + "")
                }
                executeCommandSend(newCommand, output.absolutePath)

            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }

        } else {
            var posX: Float? = 0.1F
            var posY: Float? = 0.1F

            exeCmd!!.clear()

          //   exeCmd!!.add("-y")
            exeCmd!!.add("-i")
            exeCmd!!.add(videoPath)
            //            exeCmd.add("-framerate 30000/1001 -loop 1");
            exeCmd!!.add("-i")
            exeCmd!!.add(imagePath)
            exeCmd!!.add("-filter_complex")

         //   exeCmd!!.add("[1:v]scale=$DRAW_CANVASW:$DRAW_CANVASH[ovrl];[0:v][ovrl]overlay=x=0:y=0")
            exeCmd!!.add("overlay=0.48:0.84800005")
           // exeCmd!!.add("[0:v] [3:a] [1:v] [1:a] [2:v] [3:a] concat=n=3:v=1:a=1 [v] [a]")
           // exeCmd!!.add("[1:v]scale=200:200 [ovrl], [0:v][ovrl]overlay=main_w-overlay_w-5:main_h-overlay_h-5:enable='between(t,0,100)'")
          //  exeCmd!!.add("-c:v")
          //  exeCmd!!.add("libx264")
         //   exeCmd!!.add("-codec:a")
            exeCmd!!.add("-preset")
           exeCmd!!.add("ultrafast")
        //   exeCmd!!.add("-version")



//            exeCmd!!.add("-movflags")
//            exeCmd!!.add("faststart")
         //   exeCmd!!.add(output.absolutePath)


//            val commands = arrayOf(
//                "-y",
//                "-i",
//                videoPath,
//                "-i",
//                imagePath,
//                "-filter_complex",
//               // "[1:v]scale=200:200 [ovrl], [0:v][ovrl]overlay=main_w-overlay_w-5:main_h-overlay_h-5:enable='between(t,0,$timeInSec)'",
//                "[1:v]scale=$DRAW_CANVASW:$DRAW_CANVASH[ovrl];[0:v][ovrl]overlay=x=0:y=0\"",
//                "-codec:a",
//                "copy",
//                "-strict",
//                "-2",
//            //    "-c:v",
//            //    "libx264",
//                "-preset",
//                "ultrafast",
//                output.absolutePath
//            )

            newCommand = arrayOfNulls<String>(exeCmd!!.size)
            for (j in exeCmd!!.indices) {
                newCommand!![j] = exeCmd!![j]
            }
            for (k in newCommand!!.indices) {
                Log.d("CMD==>>", newCommand!!.get(k).toString() + "")
            }
            addWaterMarkProcess(output.absolutePath)

        }
    }


    private fun addWaterMarkProcess(outputPath: String) {

     //  progressDialog!!.show()
        binding.progressBar.visibility = View.VISIBLE
         val outputPath = Common.getFilePath(this, Common.VIDEO)
         exeCmd!!.add(outputPath)

//        val xPos = width?.let {
//            (edtXPos.text.toString().toFloat().times(it)).div(100)
//        }
//        val yPos = height?.let {
//            (edtYPos.text.toString().toFloat().times(it)).div(100)
//        }

     //   val query = FFmpegQueryExtension().addVideoWaterMark(videoPath, imagePath, 0.1F, 0.1F, outputPath)
       // CallBackOfQuery().callQuery(this ,query, object : FFmpegCallBack {
      //  CallBackOfQuery().callQuery(this, commands!!, object : FFmpegCallBack {
        CallBackOfQuery().callQuery(this, exeCmd!!.toArray(arrayOfNulls<String>(exeCmd!!.size)), object : FFmpegCallBack {
            override fun process(logMessage: LogMessage) {
            Log.e("OUTPUTPATH", outputPath)

                binding.progressBar.visibility = View.VISIBLE

          //      progressDialog!!.setMessage(logMessage.text.toString())

                /*  tvOutputPath.text = logMessage.text*/
            }

            override fun success() {
                binding.progressBar.visibility = View.GONE
                Log.e("OUTPUTPATH", outputPath)
//                tvOutputPath.text = String.format(getString(R.string.output_path), outputPath)
//                processStop()

               // Toast.makeText(this@VideoPreviewActivity, outputPath, Toast.LENGTH_SHORT).show()
                Log.e("OUTPUTPATH", "Sussess")
                /*  showDialog()*/
              //  progressDialog!!.hide()

                if (File(outputPath!!).exists()) {
                    viewModel.addStory(userId!!.toInt(), File(outputPath!!))
                } else {
                    Toast.makeText(this@VideoPreviewActivity, "File not Exists", Toast.LENGTH_SHORT)
                        .show()

                }
            }

            override fun cancel() {
                binding.progressBar.visibility = View.GONE
                Log.e("OUTPUTPATH", "cancle")
           //     progressDialog!!.hide()
//                processStop()
            }

            override fun failed() {
//                binding.progressBar.visibility = View.GONE
                Log.e("OUTPUTPATH", "failed")
          //      progressDialog!!.hide()
         //      Toast.makeText(this@VideoPreviewActivity, "Failed", Toast.LENGTH_SHORT).show()
//                processStop()
            }


        })
    }

    fun executeCommandSend(command: Array<String?>?, absolutePath: String?) {
        Log.d("CommandExecute", "command:-  $command")
        try {
            fFmpeg!!.execute(command, object : FFmpegExecuteResponseHandler {
                override fun onSuccess(s: String) {
                    Log.d("CommandExecute", "onSuccess  $s")
                    showDialog()
                    //Toast.makeText(applicationContext, "Sucess making video"+absolutePath, Toast.LENGTH_SHORT).show()
                    viewModel.addStory(userId!!.toInt(), File(absolutePath!!))
                }

                override fun onProgress(s: String) {
                    progressDialog!!.setMessage(s)
                    Log.d("CommandExecute", "onProgress  $s")
                }

                override fun onFailure(s: String) {
                    Log.d("CommandExecute", "onFailure  $s")
                    progressDialog!!.hide()
                }

                override fun onStart() {
                    progressDialog!!.setTitle("Preccesing")
                    progressDialog!!.setMessage("Starting")
                    progressDialog!!.show()
                }

                override fun onFinish() {
                    progressDialog!!.hide()
                }
            })
        } catch (e: FFmpegCommandAlreadyRunningException) {
            e.printStackTrace()
            Log.d("TAG", "executeCommand:Exception:-" + e.printStackTrace())
        }


// to execute "ffprobe -version" command you just need to pass "-version"
// to execute "ffprobe -version" command you just need to pass "-version"

    }


    /*save fun for all
    *
    *
    * */
    @SuppressLint("MissingPermission")
    private fun saveVideo() {
        /*val file = File(
            Environment.getExternalStorageDirectory()
                .toString() + File.separator + "--"
                    + System.currentTimeMillis() + ".png"
        )*/
        var file: File? = null
        file = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    .toString() + File.separator + ""
                        + System.currentTimeMillis() + ".png"
            )
            /*  var fileW=ContextWrapper(this).getExternalFilesDir(Environment.DIRECTORY_PICTURES)
              File(
                  fileW, File.separator + "" + System.currentTimeMillis() + ".png"
              )*/
        } else {
            File(
                Environment.getExternalStorageDirectory().toString() + File.separator + ""
                        + System.currentTimeMillis() + ".png"
            )
        }
        try {

            file.createNewFile()
            val saveSettings: SaveSettings = SaveSettings.Builder()
                .setClearViewsEnabled(true)
                .setTransparencyEnabled(false)
                .build()
            mPhotoEditor!!.saveAsFile(file.absolutePath, saveSettings, object :
                PhotoEditor.OnSaveListener {
                override fun onSuccess(imagePathN: String) {
                    imagePath = imagePathN
                    binding.ivImage.source.setImageURI(Uri.fromFile(File(imagePathN)))
                    /*Toast.makeText(
                        applicationContext,
                        "Saved successfully...",
                        Toast.LENGTH_LONG
                    ).show()*/
                    applayWaterMark()
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

    override fun onDestroy() {
        super.onDestroy()
        if (mediaPlayer != null) {
            mediaPlayer!!.release()
        }
    }

    override fun onPause() {
        super.onPause()
        if (mediaPlayer != null) {
            if (mediaPlayer!!.isPlaying) mediaPlayer!!.pause()
        }
    }

    override fun onResume() {
        super.onResume()
        if (mediaPlayer != null) {
            if (!mediaPlayer!!.isPlaying) mediaPlayer!!.start()
        }
    }

    private fun applayWaterMark() {
        /*val output = File(
            Environment.getExternalStorageDirectory()
                .toString() + File.separator + "--"
                    + System.currentTimeMillis() + ".mp4"
        )*/
        var output: File? = null
        output = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    .toString() + "/CONNECTED/" + File.separator + ""
                        + System.currentTimeMillis() + ".mp4"
            )
            /* var fileW=ContextWrapper(this).getExternalFilesDir(Environment.DIRECTORY_MOVIES)
             File(
                 fileW, File.separator + "" + System.currentTimeMillis() + ".mp4"
             )*/
        } else {
            File(
                Environment.getExternalStorageDirectory()
                    .toString() + "/CONNECTED/" + File.separator + ""
                        + System.currentTimeMillis() + ".mp4"
            )
        }
        try {
            output.createNewFile()
            exeCmd!!.add("-y")
            exeCmd!!.add("-i")
            exeCmd!!.add(videoPath)
            //            exeCmd.add("-framerate 30000/1001 -loop 1");
            exeCmd!!.add("-i")
            exeCmd!!.add(imagePath)
            exeCmd!!.add("-filter_complex")

            exeCmd!!.add("[1:v]scale=$DRAW_CANVASW:$DRAW_CANVASH[ovrl];[0:v][ovrl]overlay=x=0:y=0")
            exeCmd!!.add("-c:v")
            exeCmd!!.add("libx264")
            exeCmd!!.add("-preset")
            exeCmd!!.add("ultrafast")
            exeCmd!!.add(output.absolutePath)

            newCommand = arrayOfNulls<String>(exeCmd!!.size)

            for (j in exeCmd!!.indices) {
                newCommand!![j] = exeCmd!![j]
            }
            for (k in newCommand!!.indices) {
                Log.d("CMD==>>", newCommand!!.get(k).toString() + "")
            }
            executeCommand(newCommand, output.absolutePath)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private var newCommand: Array<String?>? = null

    fun executeCommand(command: Array<String?>?, absolutePath: String?) {
        Log.d("CommandExecute", "command:-  $command")
        try {
            fFmpeg!!.execute(command, object : FFmpegExecuteResponseHandler {
                override fun onSuccess(s: String) {
                    Log.d("CommandExecute", "onSuccess  $s")
                    Toast.makeText(
                        applicationContext,
                        "Sucess making video" + absolutePath,
                        Toast.LENGTH_SHORT
                    ).show()
                    /*val i = Intent(
                        this@VideoPreviewActivity,
                        VideoPreviewActivity::class.java
                    )
                    i.putExtra("DATA", absolutePath)
                    startActivity(i)*/
                }

                override fun onProgress(s: String) {
                    progressDialog!!.setMessage(s)
                    Log.d("CommandExecute", "onProgress  $s")
                }

                override fun onFailure(s: String) {
                    Log.d("CommandExecute", "onFailure  $s")
                    progressDialog!!.hide()
                }

                override fun onStart() {
                    progressDialog!!.setTitle("Preccesing")
                    progressDialog!!.setMessage("Starting")
                    progressDialog!!.show()
                }

                override fun onFinish() {
                    progressDialog!!.hide()
                }
            })
        } catch (e: FFmpegCommandAlreadyRunningException) {
            e.printStackTrace()
            Log.d("TAG", "executeCommand:Exception:-" + e.printStackTrace())
        }
    }

    private fun setDrawingMode() {
        if (mPhotoEditor!!.getBrushDrawableMode()) {
            mPhotoEditor!!.setBrushDrawingMode(false)
            binding.llSelect.visibility = View.VISIBLE
            binding.llPaint.visibility = View.GONE
        } else {
            mPhotoEditor!!.setBrushDrawingMode(true)
            binding.llSelect.visibility = View.GONE
            binding.llPaint.visibility = View.VISIBLE
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
        mPhotoEditor!!.brushSize = brushSize.toFloat()
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