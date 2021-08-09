package com.app.spark.activity.camera

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.display.DisplayManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.camera.core.*
import androidx.camera.extensions.HdrImageCaptureExtender
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.animation.doOnCancel
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.app.spark.R
import com.app.spark.activity.camera.utill_camera.BaseActivityCamera
import com.app.spark.activity.camera.utill_camera.LuminosityAnalyzer
import com.app.spark.activity.camera.utill_camera.ThreadExecutor
import com.app.spark.activity.camera.utill_camera.toggleButton
import com.app.spark.database.SharedPrefsManager
import com.app.spark.databinding.ActivityCameraBinding
import java.io.File
import java.nio.ByteBuffer
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.properties.Delegates


class CameraActivity : BaseActivityCamera(){
    private lateinit var binding: ActivityCameraBinding

    private val displayManager by lazy { this.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager }
    private val prefs by lazy { SharedPrefsManager.newInstance(this) }
    private var preview: Preview? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var imageCapture: ImageCapture? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var displayId = -1
    private var lensFacing = CameraSelector.DEFAULT_BACK_CAMERA
    var progressChanged = 0
    private var hasGrid = false
    private var videoCapture: VideoCapture? = null
    private var isRecording = false

    private var flashMode by Delegates.observable(ImageCapture.FLASH_MODE_OFF) { _, _, new ->
        binding.ivFlash.setImageResource(
            when (new) {
                ImageCapture.FLASH_MODE_ON -> R.drawable.ic_flash_on
                ImageCapture.FLASH_MODE_AUTO -> R.drawable.ic_flash_auto
                else -> R.drawable.ic_flash_off
            }
        )
    }
    private val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) = Unit
        override fun onDisplayRemoved(displayId: Int) = Unit
        @SuppressLint("UnsafeExperimentalUsageError", "RestrictedApi")
        override fun onDisplayChanged(displayId: Int) = this@CameraActivity.let { view ->
            if (displayId == this@CameraActivity.displayId) {
                preview?.targetRotation = view.display!!.rotation
                imageCapture?.targetRotation = view.display!!.rotation
                imageAnalyzer?.targetRotation = view.display!!.rotation
                videoCapture?.setTargetRotation(view.display!!.rotation)
            }
        }
    }
    var imageVideoFalg=false
    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_camera)
        binding.activity = this
        flashMode = prefs.getInt(KEY_FLASH, ImageCapture.FLASH_MODE_OFF)
        initViews()
        onPermissionGranted()
        displayManager.registerDisplayListener(displayListener, null)
        binding.run {
            viewFinder.addOnAttachStateChangeListener(object :
                View.OnAttachStateChangeListener {
                override fun onViewDetachedFromWindow(v: View) =
                    displayManager.registerDisplayListener(displayListener, null)

                override fun onViewAttachedToWindow(v: View) =
                    displayManager.unregisterDisplayListener(displayListener)
            })
            btnTakePicture.setOnClickListener {
                /*if(imageVideoFalg) startVideoRecording()
                else */
                    captureImage()
            }
            tvTitle.setOnClickListener {
                if(imageVideoFalg) {
                    imageVideoFalg=false
                    tvTitle.text="Photo"
                    btnTakePicture.setImageDrawable(getDrawable(R.drawable.ic_capture))
                }
                else {
                    imageVideoFalg=true
                    tvTitle.text="Video"
                    btnTakePicture.setImageDrawable(getDrawable(R.drawable.ic_capture_video))
                }
                startCamera()
            }
            ivFlash!!.setOnClickListener {
                when (flashMode) {
                    ImageCapture.FLASH_MODE_OFF -> closeFlashAndSelect(ImageCapture.FLASH_MODE_ON)
                    ImageCapture.FLASH_MODE_ON -> closeFlashAndSelect(ImageCapture.FLASH_MODE_AUTO)
                    ImageCapture.FLASH_MODE_AUTO -> closeFlashAndSelect(ImageCapture.FLASH_MODE_OFF)
                }
            }
            tvZoom!!.setOnClickListener {
                if(progressChanged<24) setZoomView(25)
                else if(progressChanged>24 && progressChanged<49) setZoomView(50)
                else if(progressChanged>49 && progressChanged<74) setZoomView(75)
                else if(progressChanged>74 && progressChanged<99) setZoomView(100)
                else if(progressChanged==100) setZoomView(0)
                //mySeekBar!!.progress=progressChanged
            }
            btnSwitchCamera.setOnClickListener { toggleCamera() }
            btnGrid.setOnClickListener { toggleGrid() }

           /* btnTakePicture.setOnTouchListener(OnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_UP -> {
                        Toast.makeText(applicationContext,"UP",Toast.LENGTH_LONG).show()
                        btnTakePicture.setImageDrawable(getDrawable(R.drawable.ic_capture))
                        return@OnTouchListener true //indicate we're done listening to this touch listener
                    }
                    MotionEvent.ACTION_DOWN -> {
                        Toast.makeText(applicationContext,"DOWN",Toast.LENGTH_LONG).show()
                        btnTakePicture.setImageDrawable(getDrawable(R.drawable.ic_capture_video))
                        return@OnTouchListener true //indicate we're done listening to this touch listener
                    }
                }
                false
            })*/
        }
    }
    private val animateRecord by lazy {
        ObjectAnimator.ofFloat(binding.btnTakePicture,View.ALPHA,1f,0.5f).apply {
            repeatMode=ObjectAnimator.REVERSE
            repeatCount=ObjectAnimator.INFINITE
            doOnCancel { binding.btnTakePicture.alpha=1f}
        }
    }

    @SuppressLint("RestrictedApi")
    @RequiresApi(Build.VERSION_CODES.P)
    private fun startVideoRecording() {
        val localVideoCapture = videoCapture ?: throw IllegalStateException("Camera initialization failed.")
        // Options fot the output video file
        val outputOptions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, System.currentTimeMillis())
                put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
                put(MediaStore.MediaColumns.RELATIVE_PATH, outputDirectory)
            }

            this.contentResolver.run {
                val contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                VideoCapture.OutputFileOptions.Builder(this, contentUri, contentValues)
            }
        } else {
            File(outputDirectory).mkdirs()
            val file = File("$outputDirectory/${System.currentTimeMillis()}.mp4")

            VideoCapture.OutputFileOptions.Builder(file)
        }.build()

        if (!isRecording) {
            animateRecord.start()
            localVideoCapture.startRecording(
                outputOptions, // the options needed for the final video
                mainExecutor, // the executor, on which the task will run
                object : VideoCapture.OnVideoSavedCallback { // the callback after recording a video
                    override fun onVideoSaved(outputFileResults: VideoCapture.OutputFileResults) {
                        // Create small preview
                        outputFileResults.savedUri
                            ?.let { uri ->
                                saveVideoThumbnail(uri)
                                Log.d(TAG, "Video saved in $uri")
                            }
                            ?: setLastPictureThumbnail()
                    }

                    override fun onError(videoCaptureError: Int, message: String, cause: Throwable?) {
                        // This function is called if there is an error during recording process
                        animateRecord.cancel()
                        val msg = "Video capture failed: $message"
                        Toast.makeText(this@CameraActivity, msg, Toast.LENGTH_SHORT).show()
                        Log.e(TAG, msg)
                        cause?.printStackTrace()
                    }
                })
        } else {
            animateRecord.cancel()
            localVideoCapture.stopRecording()
        }
        isRecording = !isRecording
    }

    private fun setLastPictureThumbnail() {
        Log.d(TAG, "Not Yet")
    }

    private fun saveVideoThumbnail(uri: Uri) {
        Log.d(TAG, "Video saved in $uri")
        Toast.makeText(this,"Save: $uri",Toast.LENGTH_LONG).show()
    }

    private fun toggleGrid() {
        binding.btnGrid.toggleButton(
            flag = hasGrid,
            rotationAngle = 180f,
            firstIcon = R.drawable.ic_grid_off,
            secondIcon = R.drawable.ic_grid_on,
        ) { flag ->
            hasGrid = flag
            prefs.putBoolean(KEY_GRID, flag)
            binding.groupGridLines.visibility = if (flag) View.VISIBLE else View.GONE
        }
    }

    
     @SuppressLint("RestrictedApi")
     fun toggleCamera() = binding.btnSwitchCamera.toggleButton(
         flag = lensFacing == CameraSelector.DEFAULT_BACK_CAMERA,
         rotationAngle = 180f,
         firstIcon = R.drawable.ic_camera_switch,
         secondIcon = R.drawable.ic_camera_switch
     ) {
         lensFacing = if (it) {
             CameraSelector.DEFAULT_BACK_CAMERA
         } else {
             CameraSelector.DEFAULT_FRONT_CAMERA
         }
         startCamera()
     }

    fun divide(a: Int): Float {
        return a.toFloat() / 100
    }
    @SuppressLint("RestrictedApi")
    private fun setZoomView(progress:Int) {
        progressChanged = progress
        preview!!.apply {
            camera!!.cameraControl.setLinearZoom(divide(progress))
            if (progress < 24) binding.tvZoom!!.text = "1X"
            else if (progress > 24 && progress < 49) binding.tvZoom!!.text = "2X"
            else if (progress > 49 && progress < 74) binding.tvZoom!!.text = "4X"
            else if (progress > 74 && progress < 99) binding.tvZoom!!.text = "8X"
            else if (progress == 100) binding.tvZoom!!.text = "16X"
        }
    }
    private fun closeFlashAndSelect(@ImageCapture.FlashMode flash: Int){
            flashMode = flash
            binding.ivFlash.setImageResource(
                when (flash) {
                    ImageCapture.FLASH_MODE_ON -> R.drawable.ic_flash_on
                    ImageCapture.FLASH_MODE_OFF -> R.drawable.ic_flash_off
                    else -> R.drawable.ic_flash_auto
                }
            )
            imageCapture?.flashMode = flashMode
            prefs.putInt(KEY_FLASH, flashMode)
        }



    @RequiresApi(Build.VERSION_CODES.P)
    private fun captureImage() {
        Log.d(TAG, "click is ")
        val localImageCapture = imageCapture ?: throw IllegalStateException("Camera initialization failed.")

        localImageCapture?.takePicture(mainExecutor,
            object : ImageCapture.OnImageCapturedCallback() {
                @SuppressLint("UnsafeExperimentalUsageError")
                override fun onCaptureSuccess(imageProxy: ImageProxy) {
                    openCapture(imageProxyToBitmap(imageProxy))
                    super.onCaptureSuccess(imageProxy)
                }
                override fun onError(exception: ImageCaptureException) {
                    Log.d(TAG, "onError: "+exception.message)
                    Toast.makeText(this@CameraActivity,"Photo capture failed: ${exception.message}",Toast.LENGTH_LONG).show()
                    super.onError(exception)
                }
            })

    }

    /*directed connectedt too..... update on base activity...*/
    fun onPermissionGranted() {
        binding.viewFinder.let { vf ->
            vf.post {
                // Setting current display ID
                displayId = vf.display.displayId
                startCamera()
            }
        }
    }

    private fun openCapture(savedUri: Bitmap?) {
        binding.constraintLayout.visibility=View.VISIBLE
        binding.imageView!!.setImageBitmap(savedUri)
        //startActivity(Intent(this, CameraViewActivity::class.java).putExtra(AppConstants.BundleConstants.BUNDLE, savedUri.toString()))
        startCamera()
    }
    override fun onBackPressed() {
        if(binding.constraintLayout.isVisible) binding.constraintLayout.visibility=View.GONE
        else super.onBackPressed()
    }


    private fun imageProxyToBitmap(image: ImageProxy): Bitmap? {
        val planeProxy = image.planes[0]
        val buffer: ByteBuffer = planeProxy.buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }


    private fun initViews() {
        binding.btnGrid.setImageResource(if (hasGrid) R.drawable.ic_grid_on else R.drawable.ic_grid_off)
        binding.groupGridLines.visibility = if (hasGrid) View.VISIBLE else View.GONE
    }
    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    @SuppressLint("RestrictedApi")
    private fun startCamera() {
        // This is the CameraX PreviewView where the camera will be rendered
        val viewFinder = binding.viewFinder
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(Runnable {
            cameraProvider = cameraProviderFuture.get()

            // The display information
            val metrics = DisplayMetrics().also { viewFinder.display.getRealMetrics(it) }
            // The ratio for the output image and preview
            val aspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)
            // The display rotation
            val rotation = viewFinder.display.rotation

            val localCameraProvider = cameraProvider
                ?: throw IllegalStateException("Camera initialization failed.")

            // The Configuration of camera preview
            preview = Preview.Builder()
                .setTargetAspectRatio(aspectRatio) // set the camera aspect ratio
                .setTargetRotation(rotation) // set the camera rotation
                .build()

            /*if(imageVideoFalg){
                val videoCaptureConfig = VideoCapture.DEFAULT_CONFIG.config // default config for video capture
                // The Configuration of video capture
                videoCapture = VideoCapture.Builder
                    .fromConfig(videoCaptureConfig) // setting to have pictures with highest quality possible (may be slow)
                    .setTargetAspectRatio(aspectRatio) // set the capture aspect ratio
                    .setTargetRotation(rotation)
                    .build()
                localCameraProvider.unbindAll()
                try {
                    // Bind all use cases to the camera with lifecycle
                    camera = localCameraProvider.bindToLifecycle(
                        this, // current lifecycle owner
                        lensFacing, // either front or back facing
                        preview, // camera preview use case
                        videoCapture, // video capture use case
                    )
                    // Attach the viewfinder's surface provider to preview use case
                    preview?.setSurfaceProvider(viewFinder.surfaceProvider)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to bind use cases", e)
                }
            }
            else {*/

                // The Configuration of image capture
                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY) // setting to have pictures with highest quality possible (may be slow)
                    .setFlashMode(flashMode) // set capture flash
                    .setTargetAspectRatio(aspectRatio) // set the capture aspect ratio
                    .setTargetRotation(rotation) // set the capture rotation
                    .also {
                        // Create a Vendor Extension for HDR
                        val hdrImageCapture = HdrImageCaptureExtender.create(it)
                    }
                    .build()
                // The Configuration of image analyzing
                imageAnalyzer = ImageAnalysis.Builder()
                    .setTargetAspectRatio(aspectRatio) // set the analyzer aspect ratio
                    .setTargetRotation(rotation) // set the analyzer rotation
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST) // in our analysis, we care about the latest image
                    .build()
                    .apply {
                        // Use a worker thread for image analysis to prevent glitches
                        val analyzerThread = HandlerThread("LuminosityAnalysis").apply { start() }
                        setAnalyzer(
                            ThreadExecutor(Handler(analyzerThread.looper)),
                            LuminosityAnalyzer()
                        )
                    }

                localCameraProvider.unbindAll()
                // unbind the use-cases before rebinding them

                try {
                    // Bind all use cases to the camera with lifecycle
                    camera = localCameraProvider.bindToLifecycle(
                        this, // current lifecycle owner
                        lensFacing, // either front or back facing
                        preview, // camera preview use case
                        videoCapture, // image capture use case
                        imageAnalyzer// image analyzer use case
                    )
                    // Attach the viewfinder's surface provider to preview use case
                    preview?.setSurfaceProvider(viewFinder.surfaceProvider)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to bind use cases", e)
                }
        }, ContextCompat.getMainExecutor(this))
    }
    private var camera: Camera? = null
    companion object {
        private const val TAG = "CameraXDemo"
        const val KEY_FLASH = "sPrefFlashCamera"
        const val KEY_GRID = "sPrefGridCamera"
        const val KEY_HDR = "sPrefHDR"
        const val IMAGE_URI = "sPrefUriCapture"
        private const val RATIO_4_3_VALUE = 4.0 / 3.0 // aspect ratio 4x3
        private const val RATIO_16_9_VALUE = 16.0 / 9.0 // aspect ratio 16x9
    }
}