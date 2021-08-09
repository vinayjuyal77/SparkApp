package com.app.spark.activity.camera

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.os.*
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ImageCapture
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.ORIENTATION_HORIZONTAL
import com.app.spark.R
import com.app.spark.activity.camera.adapter.FilterLiveAdapter
import com.app.spark.activity.camera.custom_camera_utill.Option
import com.app.spark.activity.camera.fragment.CameraFragment
import com.app.spark.activity.camera.utill_camera.toggleButton
import com.app.spark.activity.custom_gallery.AddItemAdapter
import com.app.spark.database.SharedPrefsManager
import com.app.spark.databinding.ActivityCameraCustomBinding
import com.app.spark.utils.date.getOneMintTime
import com.app.spark.utils.date.getRevealTime
import com.otaliastudios.cameraview.*
import com.otaliastudios.cameraview.controls.Facing
import com.otaliastudios.cameraview.controls.Flash
import com.otaliastudios.cameraview.controls.Grid
import com.otaliastudios.cameraview.controls.Mode
import com.otaliastudios.cameraview.filter.Filters
import com.otaliastudios.cameraview.frame.Frame
import com.otaliastudios.cameraview.frame.FrameProcessor
import kotlinx.android.synthetic.main.activity_camera_custom.*
import kotlinx.android.synthetic.main.fragment_camera.*
import kotlinx.android.synthetic.main.fragment_camera.btnTakePicture
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.properties.Delegates

class CameraCustom : AppCompatActivity(),View.OnClickListener {
    companion object {
        private val LOG = CameraLogger.create("Connectd_India")
        private const val USE_FRAME_PROCESSOR = false
        private const val DECODE_BITMAP = false
    }

    private lateinit var filterAdapter: FilterLiveAdapter
    private lateinit var binding: ActivityCameraCustomBinding
    private val allFilters:Array<Filters> = Filters.values()
    private var flashMode by Delegates.observable(ImageCapture.FLASH_MODE_OFF) { _, _, new ->
        binding.btnFlash.setImageResource(
            when (new) {
                ImageCapture.FLASH_MODE_ON -> R.drawable.ic_flash_on
                ImageCapture.FLASH_MODE_AUTO -> R.drawable.ic_flash_auto
                else -> R.drawable.ic_flash_off
            }
        )
    }
    private var hasGrid by Delegates.observable(false) { _, _, new ->
        binding.btnGrid.setImageResource(
            when (new) {
                true -> R.drawable.ic_grid_on
                false -> R.drawable.ic_grid_off
            }
        )
    }
    private val prefs by lazy { SharedPrefsManager.newInstance(this) }
    var progressChanged = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_camera_custom)
        flashMode = prefs.getInt(CameraFragment.KEY_FLASH, ImageCapture.FLASH_MODE_OFF)
        hasGrid = prefs.getBoolean(CameraFragment.KEY_GRID, false)
        initial()
        if (USE_FRAME_PROCESSOR) {
            binding.camera.addFrameProcessor(object : FrameProcessor {
                private var lastTime = System.currentTimeMillis()
                override fun process(frame: Frame) {
                    val newTime = frame.time
                    val delay = newTime - lastTime
                    lastTime = newTime
                    LOG.v("Frame delayMillis:", delay, "FPS:", 1000 / delay)
                    if (DECODE_BITMAP) {
                        if (frame.format == ImageFormat.NV21
                            && frame.dataClass == ByteArray::class.java) {
                            val data = frame.getData<ByteArray>()
                            val yuvImage = YuvImage(data,
                                frame.format,
                                frame.size.width,
                                frame.size.height,
                                null)
                            val jpegStream = ByteArrayOutputStream()
                            yuvImage.compressToJpeg(
                                Rect(0, 0,
                                frame.size.width,
                                frame.size.height), 100, jpegStream)
                            val jpegByteArray = jpegStream.toByteArray()
                            val bitmap = BitmapFactory.decodeByteArray(jpegByteArray,
                                0, jpegByteArray.size)
                            bitmap.toString()
                        }
                    }
                }
            })
        }




        binding.run{
            btnTakePicture.setOnLongClickListener {
                var output: File? = null
                output = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    File(
                         Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                            .toString() + File.separator + ""
                                /*+ System.currentTimeMillis() + ".mp4"*/
                    )

                    /* var fileW=ContextWrapper(this).getExternalFilesDir(Environment.DIRECTORY_MOVIES)
                     File(
                         fileW, File.separator + "" + System.currentTimeMillis() + ".mp4"
                     )*/
                } else {
                    File(
                        Environment.getExternalStorageDirectory().toString() + File.separator + ""
                                /*+ System.currentTimeMillis() + ".mp4"*/
                    )
                }
//                var output_ = "file://" + output.absoluteFile
               camera.takeVideoSnapshot(File(output.absolutePath,  System.currentTimeMillis().toString() + ".mp4"))
               // camera.takeVideoSnapshot(File(filesDir, "video.mp4"))
                setTimmer()
                true
            }
            btnTakePicture.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    /*btnTakePicture.setImageDrawable(requireActivity().getDrawable(R.drawable.ic_capture_video))
                    videoCapture()*/
                    handler.postDelayed(runnable,100)
                } else if (event.action == MotionEvent.ACTION_UP) {
                    onActionUp()
                    camera.stopVideo()
                }
                false
            }
        }



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.USE_FULL_SCREEN_INTENT
                )
                != PackageManager.PERMISSION_GRANTED
            ) {

                // Permission is not granted
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.USE_FULL_SCREEN_INTENT
                    )
                ) {
                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                } else {
                    // No explanation needed; request the permission
                    val MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE = 321

                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.USE_FULL_SCREEN_INTENT
                        ),
                        MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE
                    )
                };

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.


            }
        }
    }
    private fun setTimmer() {
        cTimer=object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                //binding.tvTimer.text= getRevealTime(millisUntilFinished)
                binding.progressBar.setProgress(getOneMintTime(millisUntilFinished))
            }
            @SuppressLint("RestrictedApi")
            override fun onFinish() {
                onActionUp()
            }
        }.start()
    }
    var cTimer: CountDownTimer? = null
    @SuppressLint("RestrictedApi")
    fun onActionUp(){
        handler.removeCallbacksAndMessages(null)
        btnTakePicture.setImageDrawable(this.getDrawable(R.drawable.ic_capture))
        if(cTimer!=null) cTimer!!.cancel()
        binding.progressBar.setProgress(0)
    }
    private val handler = Handler(Looper.getMainLooper())
    private val runnable: Runnable = object : Runnable {
        override fun run() {
            btnTakePicture.setImageDrawable(getDrawable(R.drawable.ic_capture_video))
        }
    }

    private fun initial() {
        CameraLogger.setLogLevel(CameraLogger.LEVEL_VERBOSE)
        binding.camera.setLifecycleOwner(this)
        binding.camera.addCameraListener(Listener())
        binding.btnSwitchCamera.setOnClickListener(this)
        binding.btnFlash.setOnClickListener(this)
        binding.tvZoom.setOnClickListener(this)
        binding.btnGrid.setOnClickListener(this)
        binding.btnTakePicture.setOnClickListener(this)

        binding.camera.grid=if(hasGrid) Grid.DRAW_4X4 else Grid.OFF
        var  contentTransitionManager= CompositePageTransformer()
            contentTransitionManager.addTransformer(MarginPageTransformer(16))
            contentTransitionManager.addTransformer{ page, position ->
                page.apply {
                    val  r: Float=1-Math.abs(position)
                    page.scaleY=0.90f+r*0.12f
                }
        }
        filterAdapter = FilterLiveAdapter(this,allFilters)
            binding.viewPager2?.apply {
                offscreenPageLimit = 5
                val recyclerView = getChildAt(0) as RecyclerView
                recyclerView.apply {
                    val padding = resources.getDimensionPixelOffset(R.dimen._100sdp) +
                            resources.getDimensionPixelOffset(R.dimen._15sdp)
                    setPadding(padding, 0, padding, 0)
                    clipChildren = false
                    clipToPadding = false
                    setPageTransformer(contentTransitionManager)
                }
                adapter = filterAdapter
            }
        //binding.viewPager2.adapter = filterAdapter
        filterAdapter.onFilterChange = {filter->
            binding.camera.filter = filter.newInstance()
        }
    }

    private inner class Listener : CameraListener() {
        override fun onCameraOpened(options: CameraOptions) {
        }
        override fun onCameraError(exception: CameraException) {
            super.onCameraError(exception)
        }
        override fun onPictureTaken(result: PictureResult) {
            super.onPictureTaken(result)
            //Toast.makeText(this@CameraCustom,"Image:- "+result,Toast.LENGTH_LONG).show()
            PicturePreviewActivity.pictureResult = result
            startActivity(Intent(this@CameraCustom, PicturePreviewActivity::class.java))
            finish()
        }
        override fun onVideoTaken(result: VideoResult) {
            super.onVideoTaken(result)
            VideoPreviewActivity.videoResult = result
            startActivity(Intent(this@CameraCustom, VideoPreviewActivity::class.java))
            finish()
        }
        override fun onVideoRecordingStart() {
            super.onVideoRecordingStart()
        }
        override fun onVideoRecordingEnd() {
            super.onVideoRecordingEnd()
        }
        override fun onExposureCorrectionChanged(newValue: Float, bounds: FloatArray, fingers: Array<PointF>?) {
            super.onExposureCorrectionChanged(newValue, bounds, fingers)
        }
        override fun onZoomChanged(newValue: Float, bounds: FloatArray, fingers: Array<PointF>?) {
            super.onZoomChanged(newValue, bounds, fingers)
            if (newValue < .24) {
                progressChanged=15
                binding.tvZoom!!.text = "1X"
            }
            else if (newValue > .24 && newValue < .49) {
                binding.tvZoom!!.text = "2X"
                progressChanged=35
            }
            else if (newValue > .49 && newValue < .74) {
                binding.tvZoom!!.text = "4X"
                progressChanged=65
            }
            else if (newValue > .74 && newValue < .99) {
                binding.tvZoom!!.text = "8X"
                progressChanged=85
            }
            else if (newValue > 0.99) {
                binding.tvZoom!!.text = "16X"
                progressChanged=100
            }
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.btnSwitchCamera -> toggleCamera()
            R.id.btnFlash-> {
                when (flashMode) {
                    ImageCapture.FLASH_MODE_OFF -> {
                        binding.camera.flash=Flash.ON
                        flashCamera(ImageCapture.FLASH_MODE_ON)
                    }
                    ImageCapture.FLASH_MODE_ON -> {
                        binding.camera.flash=Flash.AUTO
                        flashCamera(ImageCapture.FLASH_MODE_AUTO)
                    }
                    ImageCapture.FLASH_MODE_AUTO -> {
                        binding.camera.flash=Flash.OFF
                        flashCamera(ImageCapture.FLASH_MODE_OFF)
                    }
                }
            }
            R.id.tvZoom->{
                if (progressChanged < 24) setZoomView(25)
                else if (progressChanged > 24 && progressChanged < 49) setZoomView(50)
                else if (progressChanged > 49 && progressChanged < 74) setZoomView(75)
                else if (progressChanged > 74 && progressChanged < 99) setZoomView(100)
                else if (progressChanged == 100) setZoomView(0)
            }
            R.id.btnGrid->{
                toggleGrid()
            }
            R.id.btnTakePicture->{
                camera.mode= Mode.PICTURE
                binding.camera.takePictureSnapshot()
            }
        }
    }
    private fun toggleGrid() {
        binding.btnGrid.toggleButton(
            flag = hasGrid,
            rotationAngle = 180f,
            firstIcon = R.drawable.ic_grid_off,
            secondIcon = R.drawable.ic_grid_on,
        ) { flag ->
            hasGrid = flag
            prefs.putBoolean(CameraFragment.KEY_GRID, flag)
            binding.camera.grid=if(hasGrid)Grid.DRAW_4X4 else Grid.OFF
        }
    }

    fun divide(a: Int): Float {
        return a.toFloat() / 100
    }
    @SuppressLint("RestrictedApi")
    private fun setZoomView(progress: Int) {
        progressChanged = progress
        binding.camera.zoom=divide(progress)
        binding.camera.zoom.apply {
            if (progress < 24) binding.tvZoom!!.text = "1X"
            else if (progress > 24 && progress < 49) binding.tvZoom!!.text = "2X"
            else if (progress > 49 && progress < 74) binding.tvZoom!!.text = "4X"
            else if (progress > 74 && progress < 99) binding.tvZoom!!.text = "8X"
            else if (progress == 100) binding.tvZoom!!.text = "16X"
        }
    }
    private fun flashCamera(@ImageCapture.FlashMode flash: Int) {
        flashMode = flash
        binding.btnFlash.setImageResource(
            when (flash) {
                ImageCapture.FLASH_MODE_ON -> R.drawable.ic_flash_on
                ImageCapture.FLASH_MODE_OFF -> R.drawable.ic_flash_off
                else -> R.drawable.ic_flash_auto
            }
        )
        prefs.putInt(CameraFragment.KEY_FLASH, flashMode)
    }

    private fun toggleCamera() {
        if (binding.camera.isTakingPicture || binding.camera.isTakingVideo) return
        when (binding.camera.toggleFacing()) {
            Facing.BACK -> Log.d("TAG", "BACK Camera: "+false)
            Facing.FRONT -> Log.d("TAG", "FRONT camera: "+false)
        }
    }

}