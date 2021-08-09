package com.app.spark.fragment.newflickimport android.app.Activityimport android.app.Dialogimport android.content.Intentimport android.net.Uriimport android.opengl.Visibilityimport android.os.Buildimport android.os.Bundleimport android.util.Logimport android.view.LayoutInflaterimport android.view.Viewimport android.view.ViewGroupimport android.view.WindowManagerimport android.view.animation.AlphaAnimationimport android.widget.ImageViewimport androidx.core.content.ContextCompatimport androidx.databinding.DataBindingUtilimport androidx.lifecycle.Observerimport androidx.lifecycle.ViewModelProviderimport androidx.recyclerview.widget.LinearLayoutManagerimport androidx.recyclerview.widget.RecyclerViewimport androidx.viewpager2.widget.ViewPager2import com.app.spark.Rimport com.app.spark.activity.main.MainActivityimport com.app.spark.activity.search.SearchActivityimport com.app.spark.constants.IntentConstantimport com.app.spark.constants.PrefConstantimport com.app.spark.database.ConnectedAppDatabaseimport com.app.spark.databinding.FragmentFlickNewBindingimport com.app.spark.dialogs.SimpleCustomDialogimport com.app.spark.interfaces.OnFeedSelectedInterfaceimport com.app.spark.interfaces.SimpleDialogListnerimport com.app.spark.models.GetFlickResponseimport com.app.spark.models.ImportantDataResultimport com.app.spark.utils.*import com.cancan.Utility.PermissionsUtilimport com.google.android.exoplayer2.*import com.google.android.exoplayer2.extractor.DefaultExtractorsFactoryimport com.google.android.exoplayer2.source.MediaSourceimport com.google.android.exoplayer2.source.ProgressiveMediaSourceimport com.google.android.exoplayer2.ui.AspectRatioFrameLayoutimport com.google.android.exoplayer2.ui.PlayerViewimport com.google.android.exoplayer2.upstream.DataSourceimport com.google.android.exoplayer2.upstream.DefaultDataSourceFactoryimport com.google.android.exoplayer2.util.Utilimport com.google.firebase.remoteconfig.FirebaseRemoteConfigimport com.google.firebase.remoteconfig.FirebaseRemoteConfigSettingsimport com.google.gson.Gsonimport kotlinx.android.synthetic.main.dialog_feed_flick_menu.*import kotlinx.android.synthetic.main.exo_player_controller.*class FlicksNewFragment : BaseFragment(), View.OnClickListener,    OnFeedSelectedInterface {    private var fliksResponseList = ArrayList<GetFlickResponse.Result>()    private var loginDetails: ImportantDataResult? = null    private lateinit var binding: FragmentFlickNewBinding    private lateinit var viewModel: FlickViewModel    lateinit var pref: SharedPrefrencesManager    private var exoPlayer: SimpleExoPlayer? = null    private var exoPlayerA: SimpleExoPlayer? = null    private var exoPlayerB: SimpleExoPlayer? = null    private var exoPlayerC: SimpleExoPlayer? = null    private var flicksNewAdapter: FlicksNewVideoViewAdapter2? = null    private var dialog: Dialog? = null    var token: String? = null    var userId: String? = null    private var layoutManager: LinearLayoutManager? = null    private var permissionsUtil: PermissionsUtil? = null    private var  connectedAppDatabase : ConnectedAppDatabase? = null    var global_posiiton = 0;    var global_flick_value = 0;    var flick_count = 0;    var listt = ArrayList<SimpleExoPlayer>()  lateinit var splashMainListener : SplashMainListener    override fun stopMusic() {        if (exoPlayer != null) {            exoPlayer?.stop()            exoPlayer?.release()        }    }    override fun onCreateView(        inflater: LayoutInflater,        container: ViewGroup?,        savedInstanceState: Bundle?    ): View? {        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_flick_new, container, false)        binding.progressBar.visibility = View.VISIBLE        viewModel = ViewModelProvider(            this,            ViewModelProvider.AndroidViewModelFactory(requireActivity().application)        ).get(FlickViewModel::class.java)        return binding.root    }    override fun onAttach(activity: Activity) {        super.onAttach(activity)        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)    }    override fun onDetach() {        super.onDetach()        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)    }    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {        super.onViewCreated(view, savedInstanceState)        //  hideStatusBar()        pref = SharedPrefrencesManager.getInstance(requireContext())        token = pref.getString(PrefConstant.ACCESS_TOKEN, "")        userId = pref.getString(PrefConstant.USER_ID, "")        viewModel.setUserData(token, userId)        viewModel.pagingFeedListingApi(0)        connectedAppDatabase = ConnectedAppDatabase.getInstance(requireContext())        splashMainListener = requireActivity() as SplashMainListener        update_dialog()      //  open_dialog_splash()        loginDetails = Gson().fromJson(            pref.getString(PrefConstant.LOGIN_RESPONSE, ""),            ImportantDataResult::class.java        )         binding.progressBar.visibility = View.VISIBLE        permissionsUtil = PermissionsUtil(requireActivity())        binding.rvFlicks.visibility = View.VISIBLE        //  binding.tvNoFlicks.visibility = View.VISIBLE        exoPlayer = SimpleExoPlayer.Builder(requireContext()).build()        setClickListener()        askPermission()        observePopularProfiles()//        binding.tvTitle.setOnClickListener {////            startActivity(Intent(requireContext(), AddTestActivity::class.java))//        }        binding.imgSearch.setOnClickListener {            startActivity(Intent(requireContext(), SearchActivity::class.java))        }    }    private fun hideStatusBar() {        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {            requireActivity().window.decorView.systemUiVisibility =                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN            requireActivity().window.statusBarColor =                ContextCompat.getColor(requireActivity(), android.R.color.transparent)        }    }    private fun setClickListener() {        binding.imgAddPost.setOnClickListener(this)    }    override fun onClick(p0: View?) {        when (p0) {            binding.imgAddPost -> {                mainActivityInterface?.onAddPost()            }        }    }    private fun askPermission() {        permissionsUtil?.askPermissions(requireActivity(), PermissionsUtil.CAMERA,            PermissionsUtil.STORAGE, object : PermissionsUtil.PermissionListener {                override fun onPermissionResult(isGranted: Boolean) {                    if (isGranted) {                        // getAllAudio()                    } else {                        askPermission()                    }                }            })    }    override fun onRequestPermissionsResult(        requestCode: Int,        permissions: Array<String>,        grantResults: IntArray    ) {        permissionsUtil?.onRequestPermissionsResult(requestCode, permissions, grantResults)    }    private fun observePopularProfiles() {        viewModel.flickList.observe(viewLifecycleOwner, Observer {            binding.progressBar.visibility = View.GONE//            binding.shimmerLayout.stopShimmer()//            binding.shimmerLayout.visibility = View.GONE            if (!it.isNullOrEmpty()) {                binding.tvNoFlicks.visibility = View.GONE                if (flicksNewAdapter == null || fliksResponseList.isEmpty()) {                    splashMainListener.dismissSplash()                    fliksResponseList.addAll(it)                   flicksNewAdapter = FlicksNewVideoViewAdapter2(requireActivity() , fliksResponseList, this)                    // flicksNewAdapter = FlicksNewAdapter2(requireContext(), fliksResponseList)                    binding.rvFlicks.adapter = flicksNewAdapter                  //  flicksNewAdapter!!.notifyDataSetChanged()                    val view: RecyclerView? = binding.rvFlicks.getChildAt(0) as RecyclerView?                    layoutManager = view?.layoutManager as LinearLayoutManager                    binding.rvFlicks.registerOnPageChangeCallback(pageChangeCallback)//                    for(i in 0 until it.size)//                    {////                        val playerView =//                            layoutManager?.findViewByPosition(i)//                                ?.findViewById(R.id.pvVideo) as PlayerView?//                        setPlayer(it[i].flickMedia,playerView )////                    }         ////                    if(isNetworkAvailable(requireContext())) {//                        binding.rvFlicks.registerOnPageChangeCallback(pageChangeCallback)//                    }//                    else//                    {//                        binding.rvFlicks.registerOnPageChangeCallback(pageChangeCallback2)//                    }                } else {                    val size = fliksResponseList.size                    fliksResponseList.addAll(it)                    flicksNewAdapter?.notifyItemRangeInserted(size, fliksResponseList.size)                    //flicksAdapter?.updateList(it)                }//               if (isNetworkAvailable(requireContext())) {//////                    if (connectedAppDatabase?.appDao()?.getFlick() != null) {////                        connectedAppDatabase?.appDao()?.deleteallflick()//                    }////                    //   flickList.postValue(!!)////                    if (it.size >= 3) {////                        //  for (i in it.size - 1 downTo it.size - 4) {////                        for (i in 0  until 3) {////                            var apkStorage = File(//                                Environment.getExternalStorageDirectory()//                                    .toString() + "/" + "connectedindia"//                            )//                            DownloadTask(requireContext(), it[i].flickMedia)//                            var downloadFileName =//                                it[i].flickMedia?.substring(//                                    it[i].flickMedia!!.lastIndexOf('/'),//                                    it[i].flickMedia!!.length//                                )////                            it.get(i)!!.flickMedia = apkStorage.path + downloadFileName//                        }////                    }////                    connectedAppDatabase?.appDao()?.insertFlickData(it)////                }            }        })        viewModel.errString.observe(viewLifecycleOwner, Observer { err: String ->            binding.progressBar.visibility = View.GONE            if (!err.isNullOrEmpty())                showSnackBar(binding.root, err)        })        viewModel.errStringFliks.observe(viewLifecycleOwner, Observer { err: String ->            binding.progressBar.visibility = View.GONE            /* if (!err.isNullOrEmpty())                 showSnackBar(binding.root, err)*/        })        viewModel.errRes.observe(viewLifecycleOwner, Observer { err: Int ->            binding.progressBar.visibility = View.GONE            if (err != null)                showSnackBar(binding.root, getString(err))        })        viewModel.deleteFeed.observe(viewLifecycleOwner, Observer {            if (it.statusCode == 200) {                showToastLong(requireActivity(), it.APICODERESULT)                if (isNetworkAvailable(requireActivity())) {                    var intents = Intent(requireActivity(), MainActivity::class.java)                    intents.putExtra(IntentConstant.PAGE_FLAG, 2)                    intents.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK                    startActivity(intents)                    requireActivity().finish()                }            }        })        viewModel.resultFollowed.observe(viewLifecycleOwner, Observer {            if (it.statusCode == 200) {                showToastLong(requireActivity(), it.APICODERESULT)//                if (isNetworkAvailable(requireActivity())) {//                    if (fliksResponseList.isNotEmpty()) {//                        fliksResponseList.clear()//                    }//                    viewModel.pagingFeedListingApi(0)//                }            }        })    }    private val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {        override fun onPageSelected(position: Int) {            super.onPageSelected(position)            if (position <= flicksNewAdapter?.itemCount!!) {                val playerView =                    layoutManager?.findViewByPosition(position)                        ?.findViewById(R.id.pvVideo1) as PlayerView?                val imageview =                    layoutManager?.findViewByPosition(position)                        ?.findViewById(R.id.imgPost) as ImageView?                val item = flicksNewAdapter?.list?.get(position)                if(playerView!=null)                {                    setPlayer(item!!.flickMedia, playerView, imageview!! )                    Log.e("ADDDDPostion",position.toString())                }                else                {                    Log.e("ADDDD","ADDDD")                    Log.e("ADDDDPostion",position.toString())                    val playerView =                        layoutManager?.findViewByPosition(position)                            ?.findViewById(R.id.pvVideo) as PlayerView?                    val imageview =                        layoutManager?.findViewByPosition(position)                            ?.findViewById(R.id.impost) as ImageView?                    setPlayer("http://15.206.254.159/app/uploads/flick/1625837746657.mp4", playerView, imageview!! )                }                if (position == (flicksNewAdapter?.itemCount!! - 3 )) {                    viewModel.pagingFeedListingApi(flicksNewAdapter?.itemCount)                }                 viewModel.viewCountApi(item?.flickId!!)            }        }    }    inline fun View.fadeOut_image(durationMillis: Long = 400) {        this.startAnimation(AlphaAnimation(1F, 0F).apply {            duration = durationMillis            fillAfter = true        })    }    private fun setPlayer(videoUrl: String, playerView: PlayerView?, imageview : ImageView) {        exoPlayer?.stop()         exoPlayer?.release()         exoPlayer = SimpleExoPlayer.Builder(requireContext()).build()        exoPlayer?.seekTo(1)         exoPlayer?.playWhenReady = true         exoPlayer?.repeatMode = Player.REPEAT_MODE_ONE         playerView?.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT         exoPlayer?.videoScalingMode = Renderer.VIDEO_SCALING_MODE_SCALE_TO_FIT        playerView?.player = exoPlayer        val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(            requireActivity(),            Util.getUserAgent(requireContext(), getString(R.string.app_name))        )        // This is the MediaSource representing the media to be played.        val videoSource: MediaSource =            ProgressiveMediaSource.Factory(dataSourceFactory, DefaultExtractorsFactory())                .createMediaSource(Uri.parse(videoUrl))        // Prepare the player with the source.       exoPlayer?.prepare(videoSource, true, false)        imageview.fadeOut_image(950)    }    private fun pause_exo(videoUrl: String, playerView: PlayerView?) {        exoPlayer?.stop()       exoPlayer?.release()        exoPlayer = SimpleExoPlayer.Builder(requireContext()).build()        exoPlayer?.playWhenReady = false        exoPlayer?.repeatMode = Player.REPEAT_MODE_ONE        playerView?.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT        exoPlayer?.videoScalingMode = Renderer.VIDEO_SCALING_MODE_SCALE_TO_FIT        playerView?.player = exoPlayer        val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(            requireActivity(),            Util.getUserAgent(requireContext(), getString(R.string.app_name))        )        // This is the MediaSource representing the media to be played.        val videoSource: MediaSource =            ProgressiveMediaSource.Factory(dataSourceFactory, DefaultExtractorsFactory())                .createMediaSource(Uri.parse(videoUrl))        // Prepare the player with the source.      // exoPlayer?.prepare(videoSource, true, false)    }    override fun onPlayPause(isPlay: Int) {        exoPlayer?.playWhenReady = !exoPlayer?.playWhenReady!!    }    override fun showPostMenu(        userId: String,        postId: String,        name: String,        profilePic: String?,        item: String    ) {        postMenuDialog(userId, postId, name, profilePic, item)    }    override fun onLike(postId: String, isLike: Boolean) {        viewModel.likeUnlikeApi(postId, isLike)    }    override fun onFlickListFollow(        item: GetFlickResponse.Result,        data: String    ) {        // for flick list item        if (isNetworkAvailable(requireActivity())) {            if (data.equals(requireActivity().getString(R.string.following), true)) {                viewModel.followUnfollowApi(token!!, userId!!, item?.userId, "Unfollow")            } else if (data.equals(requireActivity().getString(R.string.follow), true)) {                viewModel.followUnfollowApi(token!!, userId!!, item?.userId, "Follow")            }        }    }    override fun sendExoPlayer(list : ArrayList<SimpleExoPlayer>, exoPlayer: SimpleExoPlayer, position: Int, playerView: PlayerView) {        this.exoPlayerA = exoPlayer   //      this.listt = list//        this.global_posiiton = position//        exoPlayerA?.playWhenReady = true//        exoPlayerA?.repeatMode = Player.REPEAT_MODE_ONE////        exoPlayerA?.volume = 100.0F//        //  exoPlayerA?.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT//        exoPlayerA?.videoScalingMode = Renderer.VIDEO_SCALING_MODE_SCALE_TO_FIT//        playerView?.player = exoPlayer//        val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(//            requireContext(),//            Util.getUserAgent(requireContext(), getString(R.string.app_name))//        )//////        if(exoPlayerA!=null)//        {//            exoPlayerA?.volume = 0.0F//            exoPlayerA = null//        }    }    private fun postMenuDialog(        otherUserId: String,        reportId: String,        name: String,        profilePic: String?,        postId: String    ) {        dialog?.dismiss()        dialog = Dialog(requireContext())        dialog!!.setContentView(R.layout.dialog_feed_flick_menu)        dialog!!.window!!.setLayout(            ViewGroup.LayoutParams.MATCH_PARENT,            ViewGroup.LayoutParams.WRAP_CONTENT        )        dialog!!.window!!.setBackgroundDrawableResource(android.R.color.transparent)        dialog!!.show()        if (otherUserId.equals(userId, true)) {            dialog!!.tvReport.visibility = View.GONE            dialog!!.tvUnfollow.visibility = View.GONE            dialog!!.tvEdit.visibility = View.VISIBLE            dialog!!.tvDelete.visibility = View.VISIBLE        } else {            dialog!!.tvReport.visibility = View.VISIBLE            dialog!!.tvUnfollow.visibility = View.VISIBLE            dialog!!.tvEdit.visibility = View.GONE            dialog!!.tvDelete.visibility = View.GONE        }        dialog!!.tvReport.setOnClickListener {            reportDialog(reportId)            dialog?.dismiss()        }        dialog!!.tvShare.setOnClickListener {            share(requireActivity(), "https://www.connectd.com/flick/${reportId}")        }        dialog!!.tvCopyLink.setOnClickListener {            copyText(requireActivity(), "https://www.connectd.com/flick/${reportId}")        }        dialog!!.tvUnfollow.setOnClickListener {            // viewModel.followUnfollowApi(token!!, userId!!, otherUserId, "Unfollow")            acceptanceDialog(token!!, userId!!, otherUserId, "Unfollow", postId)            dialog?.dismiss()        }        dialog!!.tvMute.setOnClickListener {            acceptanceDialog(token!!, userId!!, otherUserId, "Mute", postId)            dialog?.dismiss()        }        dialog!!.tvDelete.setOnClickListener {            // viewModel.followUnfollowApi(token!!, userId!!, otherUserId, "Unfollow")            acceptanceDialog(token!!, userId!!, otherUserId, "Delete", postId)            dialog?.dismiss()        }        dialog!!.llDialogRoot.setOnClickListener {            dialog!!.dismiss()        }        dialog!!.setCancelable(true)        dialog!!.setCanceledOnTouchOutside(true)    }    private fun acceptanceDialog(        token: String,        userId: String,        otherUserId: String,        type: String,        postId: String    ) {        var desc = ""        when {            type.equals("Unfollow", true) -> {                desc = "Do you want to unfollow this account?\n you can follow them back anytime."            }            type.equals("Mute", true) -> {                desc = "Do you want to mute all the \n posts from  @${loginDetails?.username!!}?."            }            type.equals("Delete", true) -> {                desc = "Do you want to delete feed?."            }        }        val dialog = SimpleCustomDialog(            requireActivity(),            title = type,            desc = desc,            positiveBtnName = type,            onConnectionTypeSelected = object : SimpleDialogListner {                override fun submitSelected() {                    if (isNetworkAvailable(requireActivity())) {                        if (type.equals("Unfollow", true)) {                            viewModel.followUnfollowApi(token, userId, otherUserId, "Unfollow")                        } else if (type.equals("Delete", true)) {                            viewModel.deleteFeedApi(token, userId, postId, "flick")                        }                        dialog?.dismiss()                    }                }            })        dialog.show()        dialog.setCancelable(true)    }    private fun reportDialog(reportId: String) {        val dialog = SimpleCustomDialog(            requireActivity(),            title = "Report",            desc = "Do you want to report this flick?",            positiveBtnName = "Report",            onConnectionTypeSelected = object : SimpleDialogListner {                override fun submitSelected() {                    if (isNetworkAvailable(requireActivity())) {                        viewModel.reportPost(                            token!!,                            userId!!,                            reportId                        )                        dialog?.dismiss()                    }                }            })        dialog.show()        dialog.setCancelable(true)    }    override fun onResume() {        super.onResume()        binding.shimmerLayout.startShimmer()//        if (fliksResponseList.isNotEmpty()) {//            fliksResponseList.clear()//        }////        if(isNetworkAvailable(requireContext())) {//            viewModel.flickListingApi(0)//        }//        else//        {//            viewModel.getLcoalData()//        }    }    override fun onPause() {        super.onPause()        if (isAdded) {            if (exoPlayer != null) {                exoPlayer?.stop()            }        }        if (exoPlayerA != null) {            exoPlayerA?.stop()            exoPlayer?.release()          for(i in 0 until listt.size)          {              listt.get(i).stop()              listt.get(i).release()          }        }    }    override fun onDestroy() {        super.onDestroy()        if (exoPlayer != null) {            exoPlayer?.stop()            exoPlayer?.release()            listt.clear()        }        if (exoPlayerA != null) {            exoPlayerA?.stop()            exoPlayerA?.release()            listt.clear()        }        binding.rvFlicks.unregisterOnPageChangeCallback(pageChangeCallback)    }    fun update_dialog()    {        var firebaseRemoteConfig = FirebaseRemoteConfig.getInstance()        val configBuilder = FirebaseRemoteConfigSettings.Builder()            val cacheInterval: Long = 0            configBuilder.minimumFetchIntervalInSeconds = cacheInterval        firebaseRemoteConfig.setConfigSettingsAsync(configBuilder.build())        firebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)        firebaseRemoteConfig.fetchAndActivate()            .addOnCompleteListener(requireActivity()) { task ->                if (task.isSuccessful) {                    val onlineVersion = firebaseRemoteConfig.getString("update_enable")                    //        Toast.makeText(Dashboard.this, onlineVersion, Toast.LENGTH_SHORT).show();                    val versionCode: Int = BuildConfig.VERSION_CODE                    if (onlineVersion == versionCode.toString()) {                    } else {//                        open_dilog_box_ask(//                            "0",//                            "New version update is available on play store please update your app for better perfomance."//                        )                    }                }            }    }}