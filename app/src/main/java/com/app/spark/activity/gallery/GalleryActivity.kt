package com.app.spark.activity.gallery

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.CodeBoy.MediaFacer.MediaFacer
import com.CodeBoy.MediaFacer.PictureGet
import com.CodeBoy.MediaFacer.mediaHolders.pictureContent
import com.CodeBoy.MediaFacer.mediaHolders.videoContent
import com.app.spark.R
import com.app.spark.activity.add_post.AddPostActivity
import com.app.spark.constants.IntentConstant
import com.app.spark.databinding.ActivityAddPostGalleryBinding
import com.app.spark.interfaces.OnItemSelectedInterface
import com.app.spark.utils.GridSpacingItemDecoration
import com.app.spark.utils.ImagePickerUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.cancan.Utility.PermissionsUtil
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.android.material.tabs.TabLayout
import java.io.File
import java.net.URLConnection

class GalleryActivity : AppCompatActivity(), OnItemSelectedInterface {

    lateinit var binding: ActivityAddPostGalleryBinding
    lateinit var viewModel: ImageViewModel
    var allPhotos: ArrayList<pictureContent>? = null
    var allVideos: ArrayList<videoContent>? = null
    private var galleryAdapter: GalleryAdapter? = null
    private var player: SimpleExoPlayer? = null
    private var selectedPosition: Int = 0
    private var mediaType: String? = null
    private var mediaUri: String? = null
    private var mimeType: String? = null
    private lateinit var permissionsUtil: PermissionsUtil
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_post_gallery)
        viewModel =
            ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(
                ImageViewModel::class.java
            )

        permissionsUtil = PermissionsUtil(this)
        askPermission()
        setClickListener()
        setPlayer()
        setTabLayout()
    }



    private fun askPermission() {
        permissionsUtil.askPermissions(this,
            PermissionsUtil.STORAGE,
            PermissionsUtil.CAMERA,
            object : PermissionsUtil.PermissionListener {
                override fun onPermissionResult(isGranted: Boolean) {
                    if (isGranted) {
                        //getAllImages()
                        getImagesAndVideos()
                    }
                }

            })
    }

    private fun setClickListener() {
        binding.imgBack.setOnClickListener {
            onBackPressed()
        }
        binding.imgNext.setOnClickListener {
            if (mediaType == "photo") {
                ImagePickerUtil.cropUserImage(
                    this,
                    mediaUri, "post", getwidth(Uri.parse(mediaUri)), getHeight(Uri.parse(mediaUri))
                ) { imageFile, tag ->
                    mediaUri = imageFile.path
                    val intent = Intent(this@GalleryActivity, AddPostActivity::class.java)
                    intent.putExtra(IntentConstant.MEDIA_TYPE, mediaType)
                    intent.putExtra(
                        IntentConstant.MIME_TYPE,
                        mimeType
                    )
                    intent.putExtra(IntentConstant.MEDIA_URI, mediaUri)
                    startActivityForResult(intent, IntentConstant.REQUEST_CODE)
                }

            } else {

                player?.stop()
              //  player?.release()

                val intent = Intent(this@GalleryActivity, AddPostActivity::class.java)
                intent.putExtra(IntentConstant.MEDIA_TYPE, mediaType)
                intent.putExtra(
                    IntentConstant.MIME_TYPE,
                    mimeType
                )
                intent.putExtra(IntentConstant.MEDIA_URI, mediaUri)
                startActivityForResult(intent, IntentConstant.REQUEST_CODE)
            }
        }
    }


    private fun getwidth(uri: Uri): Int {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(File(uri.path).absolutePath, options)
        val imageWidth = options.outWidth

        return options.outWidth
    }

    private fun getHeight(uri: Uri): Int {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(File(uri.path).absolutePath, options)
       // val imageHeight = options.outHeight
        return options.outHeight
    }


    private fun setPlayer() {
        //  TrackSelection.Factory factory = new AdaptiveTrackSelection.Factory();
        player = SimpleExoPlayer.Builder(this).build()
        player!!.playWhenReady = true
        player?.repeatMode = Player.REPEAT_MODE_OFF
        binding.pvVideo.player = player
    }


    private fun setTabLayout() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab?.position == 1) {
                    ImagePickerUtil.selectImage(this@GalleryActivity,
                            { imageFile, tag -> mimeType = URLConnection.guessContentTypeFromName(imageFile?.getName())
                            mediaUri = imageFile?.path
                            mediaType = "photo"
                            binding.pvVideo.visibility = View.GONE
                            binding.imgImage.visibility = View.VISIBLE
                            Glide.with(this@GalleryActivity)
                                .load(imageFile)
                               // .apply(RequestOptions().centerCrop())
                                .into(binding.imgImage)

                                val intent = Intent(this@GalleryActivity, AddPostActivity::class.java)
                                intent.putExtra(IntentConstant.MEDIA_TYPE, mediaType)
                                intent.putExtra(
                                    IntentConstant.MIME_TYPE,
                                    mimeType
                                )
                                intent.putExtra(IntentConstant.MEDIA_URI, mediaUri)
                                startActivityForResult(intent, IntentConstant.REQUEST_CODE)


                            },
                        "feed_${System.currentTimeMillis()}",
                        true, true, false, true
                    )




                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

        })
    }

    private fun getAllImages() {
        allPhotos = MediaFacer
            .withPictureContex(this)
            .getAllPictureContents(PictureGet.externalContentUri)
        allVideos = MediaFacer
            .withVideoContex(this)
            .getAllVideoContent(PictureGet.externalContentUri)
        binding.rvGallery.layoutManager =
            GridLayoutManager(this, 3, RecyclerView.VERTICAL, false)
        binding.rvGallery.addItemDecoration(GridSpacingItemDecoration(3, 20, true, 0))
        //  binding.rvGallery.adapter = GalleryAdapter(this, allPhotos!!, this)
    }

    private fun observeImagesVideos() {
        viewModel.getImageList().observe(this, {
            if (it != null) {
                galleryAdapter?.updateList(it)
            }
        })
    }

    private fun getImagesAndVideos() {
        binding.rvGallery.layoutManager =
            GridLayoutManager(this, 3, RecyclerView.VERTICAL, false)
        binding.rvGallery.addItemDecoration(GridSpacingItemDecoration(3, 20, true, 0))
        galleryAdapter = GalleryAdapter(this, mutableListOf(), this)
        binding.rvGallery.adapter = galleryAdapter
        observeImagesVideos()
        viewModel.getAllImages()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionsUtil.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onItemSelected(position: Int, totalSize: Int?) {
        player?.stop()
        selectedPosition = position
        mimeType = viewModel.imagesVideos[selectedPosition].mimeType
        mediaUri = viewModel.imagesVideos[selectedPosition].path
        if (viewModel.imagesVideos[position].mediaType == 1) {
            mediaType = "photo"
            binding.pvVideo.visibility = View.GONE
            binding.imgImage.visibility = View.VISIBLE
            Glide.with(this)
                .load(viewModel.imagesVideos[position].path)
                .apply(RequestOptions().centerCrop())
                .into(binding.imgImage)
        } else {
            mediaType = "video"
            binding.pvVideo.visibility = View.VISIBLE
            binding.imgImage.visibility = View.INVISIBLE
            playVideo(viewModel.imagesVideos[position].path!!)
        }
    }

    /*
                Play Video Url using VideoView
                 */
    private fun playVideo(videoUrl: String) {
        val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(
            this,
            Util.getUserAgent(this, getString(R.string.app_name))
        )
        // This is the MediaSource representing the media to be played.
        val videoSource: MediaSource =
            ProgressiveMediaSource.Factory(dataSourceFactory, DefaultExtractorsFactory())
                .createMediaSource(Uri.parse(videoUrl))
        // Prepare the player with the source.
        player!!.prepare(videoSource, true, false)
    }

    override fun onBackPressed() {
        player?.stop()
        player?.release()
        super.onBackPressed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IntentConstant.REQUEST_CODE && resultCode == RESULT_OK) {
            finish()
        } else {
            ImagePickerUtil.onActivityResult(requestCode, resultCode, data)
        }
    }
}