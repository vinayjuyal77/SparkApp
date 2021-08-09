package com.app.spark.activity.camera.fragment

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.display.DisplayManager
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.app.spark.R
import com.app.spark.activity.camera.utill_camera.*
import com.app.spark.activity.explore.SessionOverActivity
import com.app.spark.constants.AppConstants.BundleConstants.ACTION_INT
import com.app.spark.constants.AppConstants.BundleConstants.BUNDLE
import com.app.spark.database.SharedPrefsManager
import com.app.spark.databinding.FragmentCameraBinding
import com.app.spark.utils.date.getOneMintTime
import com.app.spark.utils.date.getRevealTime
import kotlinx.android.synthetic.main.fragment_camera.*
import kotlinx.coroutines.*
import java.io.File
import java.nio.ByteBuffer
import java.util.concurrent.ExecutionException
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.properties.Delegates


class CameraFragment : BaseFragment<FragmentCameraBinding>(R.layout.fragment_camera){
    companion object {
        private const val TAG = "CameraFragment"
        const val KEY_FLASH = "sPrefFlashCamera"
        const val KEY_GRID = "sPrefGridCamera"
        private const val RATIO_4_3_VALUE = 4.0 / 3.0 // aspect ratio 4x3
        private const val RATIO_16_9_VALUE = 16.0 / 9.0 // aspect ratio 16x9
    }
    private val displayManager by lazy { requireContext().getSystemService(Context.DISPLAY_SERVICE) as DisplayManager }
    private val prefs by lazy { SharedPrefsManager.newInstance(requireContext()) }
    private var preview: Preview? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var imageCapture: ImageCapture? = null
    private var imageAnalyzer: ImageAnalysis? = null
    override val binding: FragmentCameraBinding by lazy {
        FragmentCameraBinding.inflate(
            layoutInflater
        )
    }
    private var displayId = -1
    private var lensFacing = CameraSelector.DEFAULT_BACK_CAMERA
    private var videoCapture: VideoCapture? = null
    private var isRecording = false


    // Selector showing which flash mode is selected (on, off or auto)
    private var flashMode by Delegates.observable(ImageCapture.FLASH_MODE_OFF) { _, _, new ->
        binding.btnFlash.setImageResource(
            when (new) {
                ImageCapture.FLASH_MODE_ON -> R.drawable.ic_flash_on
                ImageCapture.FLASH_MODE_AUTO -> R.drawable.ic_flash_auto
                else -> R.drawable.ic_flash_off
            }
        )
    }

    // Selector showing is grid enabled or not
    private var hasGrid = false

    private val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) = Unit
        override fun onDisplayRemoved(displayId: Int) = Unit

        @SuppressLint("UnsafeExperimentalUsageError", "RestrictedApi")
        override fun onDisplayChanged(displayId: Int) = view?.let { view ->
            if (displayId == this@CameraFragment.displayId) {
                preview?.targetRotation = view.display.rotation
                imageCapture?.targetRotation = view.display.rotation
                imageAnalyzer?.targetRotation = view.display.rotation
                videoCapture?.setTargetRotation(view.display.rotation)
            }
        } ?: Unit
    }
    private var gestureDetector: GestureDetector? = null
    @RequiresApi(Build.VERSION_CODES.P)
    @SuppressLint("ClickableViewAccessibility", "RestrictedApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        flashMode = prefs.getInt(KEY_FLASH, ImageCapture.FLASH_MODE_OFF)
        hasGrid = prefs.getBoolean(KEY_GRID, false)
        initViews()
        gestureDetector = GestureDetector(requireContext(), SingleTapConfirm())
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
                if (flagChange) takePicture()
                //else videoCapture()
            }
            // btnGallery.setOnClickListener { openPreview() }
            btnSwitchCamera.setOnClickListener { toggleCamera() }
            // btnTimer.setOnClickListener { selectTimer() }
            btnGrid.setOnClickListener { toggleGrid() }
            btnFlash.setOnClickListener {
                when (flashMode) {
                    ImageCapture.FLASH_MODE_OFF -> closeFlashAndSelect(ImageCapture.FLASH_MODE_ON)
                    ImageCapture.FLASH_MODE_ON -> closeFlashAndSelect(ImageCapture.FLASH_MODE_AUTO)
                    ImageCapture.FLASH_MODE_AUTO -> closeFlashAndSelect(ImageCapture.FLASH_MODE_OFF)
                }
            }
            tvZoom!!.setOnClickListener {
                if (progressChanged < 24) setZoomView(25)
                else if (progressChanged > 24 && progressChanged < 49) setZoomView(50)
                else if (progressChanged > 49 && progressChanged < 74) setZoomView(75)
                else if (progressChanged > 74 && progressChanged < 99) setZoomView(100)
                else if (progressChanged == 100) setZoomView(0)
                //mySeekBar!!.progress=progressChanged
            }
            // btnHdr.setOnClickListener { toggleHdr() }
            /*   btnTimerOff.setOnClickListener { closeTimerAndSelect(CameraTimer.OFF) }
               btnTimer3.setOnClickListener { closeTimerAndSelect(CameraTimer.S3) }
               btnTimer10.setOnClickListener { closeTimerAndSelect(CameraTimer.S10) }*/
            /*   btnFlashOff.setOnClickListener { closeFlashAndSelect(ImageCapture.FLASH_MODE_OFF) }
               btnFlashOn.setOnClickListener { closeFlashAndSelect(ImageCapture.FLASH_MODE_ON) }
               btnFlashAuto.setOnClickListener { closeFlashAndSelect(ImageCapture.FLASH_MODE_AUTO) }*/

            // This swipe gesture adds a fun gesture to switch between video and photo

            /*val swipeGestures = SwipeGestureDetector().apply {
                setSwipeCallback(right = {
                    Navigation.findNavController(view).navigate(R.id.action_camera_to_video)
                })
            }
            val gestureDetectorCompat = GestureDetector(requireContext(), swipeGestures)
            viewFinder.setOnTouchListener { _, motionEvent ->
                if (gestureDetectorCompat.onTouchEvent(motionEvent)) return@setOnTouchListener false
                return@setOnTouchListener true
            }*/

            btnTakePicture.setOnLongClickListener {
                flagChange=false
                startCamera()
                Handler(Looper.getMainLooper()).postDelayed({
                    videoCapture()
                    setTimmer()
                }, 100)
                true
            }
            btnTakePicture.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    /*btnTakePicture.setImageDrawable(requireActivity().getDrawable(R.drawable.ic_capture_video))
                    videoCapture()*/
                    handler.postDelayed(runnable,100)
                } else if (event.action == MotionEvent.ACTION_UP) {
                    onActionUp()
                }
                false
            }
        }
    }
    var cTimer: CountDownTimer? = null
    @SuppressLint("RestrictedApi")
    fun onActionUp(){
        handler.removeCallbacksAndMessages(null)
        videoCapture!!.stopRecording()
        btnTakePicture.setImageDrawable(requireActivity().getDrawable(R.drawable.ic_capture))
        Log.i(tag, "Video File stopped")
        flagChange=true
        binding.tvTimer.text= ""
        if(cTimer!=null) cTimer!!.cancel()
        binding.progressBar.setProgress(0)
    }
    private fun setTimmer() {
        cTimer=object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.tvTimer.text= getRevealTime(millisUntilFinished)
                binding.progressBar.setProgress(getOneMintTime(millisUntilFinished))
            }
            @SuppressLint("RestrictedApi")
            override fun onFinish() {
                onActionUp()
            }
        }.start()
    }
    private val handler = Handler(Looper.getMainLooper())
    private val runnable: Runnable = object : Runnable {
        override fun run() {
            btnTakePicture.setImageDrawable(requireActivity().getDrawable(R.drawable.ic_capture_video))
        }
    }

    var flagChange:Boolean=true
    @SuppressLint("RestrictedApi")
    @RequiresApi(Build.VERSION_CODES.P)
    private fun videoCapture() {
        val localVideoCapture = videoCapture ?: throw IllegalStateException("Camera initialization failed.")
        val outputOptions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, System.currentTimeMillis())
                put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
                put(MediaStore.MediaColumns.RELATIVE_PATH, outputDirectory)
            }
            requireContext().contentResolver.run {
                val contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                VideoCapture.OutputFileOptions.Builder(this, contentUri, contentValues)
            }
        } else {
            File(outputDirectory).mkdirs()
            val file = File("$outputDirectory/${System.currentTimeMillis()}.mp4")

            VideoCapture.OutputFileOptions.Builder(file)
        }.build()

            localVideoCapture.startRecording(
                outputOptions, // the options needed for the final video
                requireActivity().mainExecutor, // the executor, on which the task will run
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
                        val msg = "Video capture failed: $message"
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                        Log.e(TAG, msg)
                        cause?.printStackTrace()
                    }
                })
    }

    private fun saveVideoThumbnail(uri: Uri) {
        Log.e(TAG, "my new uri: $uri")
        findNavController().navigate(R.id.action_camera_to_preview,Bundle().apply {
            putString(BUNDLE,uri.toString())
            putInt(ACTION_INT,2)})
    }
    private fun setLastPictureThumbnail() {
        Log.d(TAG, "Not Yet")
    }

    fun divide(a: Int): Float {
        return a.toFloat() / 100
    }

    var progressChanged = 0

    @SuppressLint("RestrictedApi")
    private fun setZoomView(progress: Int) {
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

    /**
     * Create some initial states
     * */
    private fun initViews() {
        binding.btnGrid.setImageResource(if (hasGrid) R.drawable.ic_grid_on else R.drawable.ic_grid_off)
        binding.groupGridLines.visibility = if (hasGrid) View.VISIBLE else View.GONE
        adjustInsets()
    }

    /**
     * This methods adds all necessary margins to some views based on window insets and screen orientation
     * */
    private fun adjustInsets() {
        activity?.window?.fitSystemWindows()
    }

    /**
     * Change the facing of camera
     *  toggleButton() function is an Extension function made to animate button rotation
     * */
    @SuppressLint("RestrictedApi")
    fun toggleCamera() = binding.btnSwitchCamera.toggleButton(
        flag = lensFacing == CameraSelector.DEFAULT_BACK_CAMERA,
        rotationAngle = 180f,
        firstIcon = R.drawable.ic_camera_switch,
        secondIcon = R.drawable.ic_camera_switch,
    ) {
        lensFacing = if (it) {
            CameraSelector.DEFAULT_BACK_CAMERA
        } else {
            CameraSelector.DEFAULT_FRONT_CAMERA
        }
        startCamera()
    }

    /**
     * This function is called from XML view via Data Binding to select a FlashMode
     *  possible values are ON, OFF or AUTO
     *  circularClose() function is an Extension function which is adding circular close
     * */
    private fun closeFlashAndSelect(@ImageCapture.FlashMode flash: Int) {
        flashMode = flash
        binding.btnFlash.setImageResource(
            when (flash) {
                ImageCapture.FLASH_MODE_ON -> R.drawable.ic_flash_on
                ImageCapture.FLASH_MODE_OFF -> R.drawable.ic_flash_off
                else -> R.drawable.ic_flash_auto
            }
        )
        imageCapture?.flashMode = flashMode
        prefs.putInt(KEY_FLASH, flashMode)
    }

    /**
     * Turns on or off the grid on the screen
     * */
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

    override fun onPermissionGranted() {
        // Each time apps is coming to foreground the need permission check is being processed
        binding.viewFinder.let { vf ->
            vf.post {
                // Setting current display ID
                displayId = vf.display.displayId
                startCamera()
                lifecycleScope.launch(Dispatchers.IO) {
                    // Do on IO Dispatcher
                   // setLastPictureThumbnail()
                }
            }
        }
    }

    /**
     * Unbinds all the lifecycles from CameraX, then creates new with new parameters
     * */

    @SuppressLint("RestrictedApi")
    private fun startCamera() {
        // This is the CameraX PreviewView where the camera will be rendered
        val viewFinder = binding.viewFinder
        var cameraProviderFuture=ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
            } catch (e: InterruptedException) {
                Toast.makeText(requireContext(), "Error starting camera", Toast.LENGTH_SHORT).show()
                return@addListener
            } catch (e: ExecutionException) {
                Toast.makeText(requireContext(), "Error starting camera", Toast.LENGTH_SHORT).show()
                return@addListener
            }
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

            // The Configuration of image capture
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY) // setting to have pictures with highest quality possible (may be slow)
                .setFlashMode(flashMode) // set capture flash
                .setTargetAspectRatio(aspectRatio) // set the capture aspect ratio
                .setTargetRotation(rotation) // set the capture rotation
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

            val videoCaptureConfig = VideoCapture.DEFAULT_CONFIG.config // default config for video capture
            // The Configuration of video capture
            videoCapture = VideoCapture.Builder
                .fromConfig(videoCaptureConfig)
                .build()

            localCameraProvider.unbindAll() // unbind the use-cases before rebinding them
            try {
                // Bind all use cases to the camera with lifecycle
                if(flagChange)localCameraProvider.bindToLifecycle(
                    viewLifecycleOwner, // current lifecycle owner
                    lensFacing, // either front or back facing
                    preview, // camera preview use case
                    imageCapture,// image capture use case
                    imageAnalyzer, // image analyzer use case
                )else localCameraProvider.bindToLifecycle(
                    viewLifecycleOwner, // current lifecycle owner
                    lensFacing, // either front or back facing
                    preview, // camera preview use case
                    videoCapture, // video capture use case
                )
                // Attach the viewfinder's surface provider to preview use case
                preview?.setSurfaceProvider(viewFinder.surfaceProvider)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to bind use cases", e)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    /**
     *  Detecting the most suitable aspect ratio for current dimensions
     *
     *  @param width - preview width
     *  @param height - preview height
     *  @return suitable aspect ratio
     */
    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    @Suppress("NON_EXHAUSTIVE_WHEN")
    private fun takePicture() = lifecycleScope.launch(Dispatchers.Main) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            captureImage()
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun captureImage() {
        val localImageCapture =
            imageCapture ?: throw IllegalStateException("Camera initialization failed.")
        localImageCapture?.takePicture(requireActivity().mainExecutor,
            object : ImageCapture.OnImageCapturedCallback() {
                @SuppressLint("UnsafeExperimentalUsageError")
                override fun onCaptureSuccess(imageProxy: ImageProxy) {
                    setGalleryThumbnail(imageProxyToBitmap(imageProxy))
                    super.onCaptureSuccess(imageProxy)
                }
                override fun onError(exception: ImageCaptureException) {
                    Log.d(TAG, "onError: "+exception.message)
                    Toast.makeText(requireContext(),"Photo capture failed: ${exception.message}",Toast.LENGTH_LONG).show()
                    super.onError(exception)
                }
            })
    }
    private fun imageProxyToBitmap(image: ImageProxy): Bitmap? {
        val planeProxy = image.planes[0]
        val buffer: ByteBuffer = planeProxy.buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    private fun setGalleryThumbnail(imageBitmap: Bitmap?) {
        var value=getImageUri(requireContext(),imageBitmap!!)
        findNavController().navigate(R.id.action_camera_to_preview,Bundle().apply {
            putString(BUNDLE,value.toString())
            putInt(ACTION_INT,1)})

        /*binding.constraintLayout.visibility=View.VISIBLE
        binding.imageView.load(savedUri) {
            placeholder(R.drawable.dummy_emojy)
            listener(object : ImageRequest.Listener {
                override fun onError(request: ImageRequest, throwable: Throwable) {
                    super.onError(request, throwable)
                    binding.imageView.load(savedUri) {
                        placeholder(R.drawable.dummy_emojy)
                        transformations(CircleCropTransformation())
                        fetcher(VideoFrameUriFetcher(requireContext()))
                    }
                }
            })
        }*/
    }
    /*private fun setGalleryThumbnail(savedUri: Uri?) {
        binding.constraintLayout.visibility=View.VISIBLE
       // openPreview()
       // Toast.makeText(requireContext(), "Save Image:- $savedUri", Toast.LENGTH_LONG).show()
        placeholder(R.drawable.ic_no_picture)
        transformations(CircleCropTransformation())
        listener(object : ImageRequest.Listener {
            override fun onError(request: ImageRequest, throwable: Throwable) {
                super.onError(request, throwable)
                binding.btnGallery.load(savedUri) {
                    placeholder(R.drawable.ic_no_picture)
                    transformations(CircleCropTransformation())
                    fetcher(VideoFrameUriFetcher(requireContext()))
                }
            }
        })
    }*/

    override fun onDestroyView() {
        super.onDestroyView()
        displayManager.unregisterDisplayListener(displayListener)
    }

    override fun onBackPressed() {
        if(binding.constraintLayout.isVisible)
            binding.constraintLayout.visibility=View.GONE
        else requireActivity().finish()
    }

    /*override fun onBackPressed() = when {
        binding.llTimerOptions.visibility == View.VISIBLE -> binding.llTimerOptions.circularClose(binding.btnTimer)
        binding.llFlashOptions.visibility == View.VISIBLE -> binding.llFlashOptions.circularClose(binding.btnFlash)
        else -> requireActivity().finish()
    }*/
}