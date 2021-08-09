package com.app.spark.activity.explore

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.*
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS
import android.text.Editable
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.app.spark.R
import com.app.spark.activity.chatroom.ChatRoomActivity
import com.app.spark.activity.reciver.*
import com.app.spark.constants.AppConstants
import com.app.spark.constants.AppConstants.BundleConstants.USER_ID
import com.app.spark.constants.AppConstants.BundleConstants.USER_NAME
import com.app.spark.constants.IntentConstant
import com.app.spark.constants.PrefConstant
import com.app.spark.databinding.ActivityFindMeFriendBinding
import com.app.spark.dialogs.UnableMatchDialog
import com.app.spark.models.MatchingResponse
import com.app.spark.models.TagListModel
import com.app.spark.utils.BaseActivity
import com.app.spark.utils.SharedPrefrencesManager
import com.app.spark.utils.date.getRevealTime
import com.app.spark.utils.returnChatType
import com.app.spark.utils.showSnackBar
import com.cancan.Utility.PermissionsUtil
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.gms.ads.*
import com.google.android.gms.ads.initialization.InitializationStatus
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd.load
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.dialog_location_permission.*
import kotlinx.coroutines.delay
import java.lang.System.load
import kotlin.math.log

class FindMeFriendActivity : BaseActivity(),View.OnClickListener {
    private lateinit var binding: ActivityFindMeFriendBinding
    private lateinit var adapterCustom: TagListAdapterFriend
    lateinit var pref: SharedPrefrencesManager
    private lateinit var viewModel: ExploreViewModel
    private var tagListNew : ArrayList<TagListModel> = ArrayList()
    private var selectList : ArrayList<String> = ArrayList()
    var token: String? = null
    var userId: String? = null
    var gender:String="Male"
    var latitute = ""
    var longtitute= ""
    var count:Int=0

    private var mRewardedAd: RewardedAd? = null


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_find_me_friend)
        binding.activity = this
        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory(application)
        ).get(ExploreViewModel::class.java)
        pref = SharedPrefrencesManager.getInstance(this)
        token = pref.getString(PrefConstant.ACCESS_TOKEN, "")
        userId = pref.getString(PrefConstant.USER_ID, "")
        viewModel.setUserData(token, userId)
        setToolbar()
        //setList()
        binding.npRadious.apply { minValue=0
            value=1
            maxValue=500
            wrapSelectorWheel=true
        }
        binding.npAgeMin.apply { minValue=16
            maxValue=59
            wrapSelectorWheel=true
        }
        binding.npAgeMax.apply { minValue=17
            maxValue=60
            wrapSelectorWheel=true
        }
        binding.cvProceed.setOnClickListener(this)
        binding.cvProceedNew.setOnClickListener(this)

        binding.addTag.setOnClickListener {
            if(binding.etEditTag.isVisible){
                if(binding.etEditTag.text.isNullOrEmpty()){
                    Toast.makeText(applicationContext,"Please add tag name !",Toast.LENGTH_LONG).show()
                }else{
                    //addChipToGroup(binding.etEditTag.text.toString(), colorCode)
                    adapterCustom.updateList(TagListModel(binding.etEditTag.text.toString(),true,true))
                    binding.etEditTag.text = Editable.Factory.getInstance().newEditable("")
                }
            }else {
                binding.etEditTag.visibility=View.VISIBLE
                binding.addTag.setImageDrawable(getDrawable(R.drawable.ic_baseline_send_24))
            }
        }
        binding.ivMale.setOnClickListener(this)
        binding.ivFemale.setOnClickListener(this)
        binding.ivOther.setOnClickListener(this)
        binding.ivIncAge.setOnClickListener(this)
        binding.ivDecAge.setOnClickListener(this)
        binding.ivIncAge2.setOnClickListener(this)
        binding.ivDecAge2.setOnClickListener(this)
        binding.ivIncAge3.setOnClickListener(this)
        binding.ivDecAge3.setOnClickListener(this)

        val flexboxLayoutManager = FlexboxLayoutManager(this).apply {
            flexWrap = FlexWrap.WRAP
            flexDirection = FlexDirection.ROW
            alignItems = AlignItems.STRETCH
        }
        adapterCustom=TagListAdapterFriend(applicationContext,tagListNew,colorCode)
        binding.rvTaglist.apply {
            layoutManager = flexboxLayoutManager
            adapter = adapterCustom
        }
        viewModel.isOnlineStatusApi(this,1, userId!!.toInt())

        viewModel.errRes.observe(this, { err: Int ->
            if (err != null)
                showSnackBar(binding.root, getString(err))
            dismisDilaog()
        })
        viewModel.errString.observe(this, { err: String ->
            if (!err.isNullOrEmpty())
                showSnackBar(binding.root, err)
            dismisDilaog()
        })
        viewModel.errCode.observe(this, { err: Int ->
            if (err != null){
                if(err==0){
                    if(count>5) {
                        try{cTimer!!.cancel()}
                        catch (e:Exception){e.printStackTrace()}
                        binding.progressBar.visibility=View.GONE
                        MatchFoundActivity.gender = gender
                        MatchFoundActivity.minAge = binding.tvMinAge.text.toString()
                        MatchFoundActivity.maxAge = binding.tvMinAge2.text.toString()
                        MatchFoundActivity.radius = binding.tvMinAge3.text.toString()
                        MatchFoundActivity.selectList = selectList
                        MatchFoundActivity.lat = latitute
                        MatchFoundActivity.long = longtitute
                        startActivity(Intent(this, MatchFoundActivity::class.java).apply {
                            action = intent.action
                        })
                        finish()
                    } else {
                        count++
                        val handler = Handler()
                        handler.postDelayed({
                            viewModel.setMatchApi(
                                userId!!.toInt(),
                                gender,
                                binding.tvMinAge.text.toString().toInt(),
                                binding.tvMinAge2.text.toString().toInt(),
                                binding.tvMinAge3.text.toString().toInt(),
                                selectList,
                                latitute,
                                longtitute
                            )}, 5000)
                    }
                }else if(err==1){
                    try{cTimer!!.cancel()}
                    catch (e:Exception){e.printStackTrace()}
                    binding.progressBar.visibility=View.GONE
                    val intent = Intent(this, ChatRoomActivity::class.java).apply { action=intent.action}
                    intent.putExtra(IntentConstant.CHAT_ID, viewModel.chatID.toString())
                    intent.putExtra(IntentConstant.CHAT_NAME, viewModel.chatName.toString())
                    intent.putExtra(IntentConstant.CHAT_IMG, "")
                    intent.putExtra(IntentConstant.CHAT_TYPE, returnChatType("2"))
                    intent.putExtra(IntentConstant.CHAT_TYPE_DATE, "yes")
                    startActivity(intent)
                    //finish()
                } else {
                    binding.progressBar.visibility=View.GONE
                    binding.cvProceed.visibility=View.VISIBLE
                    binding.cvTimer.visibility=View.GONE
                    //UnableMatchDialog.Builder(this)
                    //  .setOkFunction {}
                    //  .build()
                }
            }
        })
        viewModel.tagList.observe(this, { tag: String ->
            if (!tag.isNullOrEmpty()) {
                val lstValues: List<String> = tag.split(",").map { it -> it.trim() }
                lstValues.forEach { it ->
                    Log.i("Values", "value=$it")
                    adapterCustom.updateList(TagListModel(it,false,false))
                }
            }
        })

        viewModel.successStatus.observe(this, { response: MatchingResponse ->
            dismisDilaog()
            /*if (response.status==1) sendToMatch((response.result[0].user_id).toString(),response.result[0].name)
            else setTimmer()*/
            setTimmer()
        })
        viewModel.faliedRes.observe(this, { faliedRes: String ->
            if (!faliedRes.isNullOrEmpty()) showSnackBar(binding.root, faliedRes)
            dismisDilaog()
        })

        /*location update calling*/
        permissionsUtil = PermissionsUtil(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (doesUserHavePermission() == 0) {
            askPermission()
        } else {
            locationPermissionDialog()
        }





        //
//        MobileAds.initialize(this, object : OnInitializationCompleteListener {
//            override fun onInitializationComplete(initializationStatus: InitializationStatus) {
//
//                //Showing a simple Toast Message to the user when The Google AdMob Sdk Initialization is Completed
//                Toast.makeText(
//                    this@FindMeFriendActivity,
//                    "AdMob Sdk Initialize " + initializationStatus.toString(),
//                    Toast.LENGTH_LONG
//                ).show()
//            }
//        })
//
//
//           rewardedAd = RewardedAd( this@FindMeFriendActivity, "ca-app-pub-3940256099942544/5224354917")
//    //    rewardedAd = RewardedAd(requireActivity(), "ca-app-pub-3601101210502228/7801541506")
//
//
//
//        rewardedAdLoadCallback = object : RewardedAdLoadCallback() {
//            override fun onRewardedAdLoaded() {
//                // Showing a simple Toast message to user when Rewarded Ad Failed to Load
//                Toast.makeText( this@FindMeFriendActivity, "Rewarded Ad is Loaded", Toast.LENGTH_LONG).show()
//            }
//
//            override fun onRewardedAdFailedToLoad(adError: LoadAdError?) {
//                // Showing a simple Toast message to user when Rewarded Ad Failed to Load
//                Toast.makeText( this@FindMeFriendActivity, "Rewarded Ad is Loaded", Toast.LENGTH_LONG).show()
//            }
//        }
//
//
//
//        loadRewardedAd()


        MobileAds.initialize(this) {}


        fulscreen()


        var adRequest = AdRequest.Builder().build()

        RewardedAd.load(this,"ca-app-pub-5067799942394351/2460259493", adRequest, object : RewardedAdLoadCallback() {
            // RewardedAd.load(this,"ca-app-pub-3601101210502228/7801541506", adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.e("Rewarded", adError?.message)
                mRewardedAd = null
            }

            override fun onAdLoaded(rewardedAd: RewardedAd) {
                Log.e("Rewarded", "Ad was loaded.")
                mRewardedAd = rewardedAd
                show()

            }
        })















        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReciver,
            IntentFilter(ACTION_MATCH)
        )
    }
    fun sendToMatch(matchId:String?,matchName:String?){
        startActivity(Intent(this, MatchFoundActivity::class.java).apply {
            action = intent.action
            putExtra(USER_ID,""+matchId)
            putExtra(USER_NAME,""+matchName)
        })
        finish()
    }
    private val broadcastReciver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when(intent!!.action){
                ACTION_MATCH->{
                    var matchId=intent!!.getStringExtra(EXTRA_MATCH_ID)
                    var matchName=intent!!.getStringExtra(EXTRA_MATCH_NAME)
                    if(cTimer!=null) cTimer!!.cancel()
                    sendToMatch(intent!!.getStringExtra(EXTRA_MATCH_ID),intent!!.getStringExtra(EXTRA_MATCH_NAME))
                }
            }
        }
    }
    fun fulscreen() {
        mRewardedAd?.fullScreenContentCallback = object: FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.e("Rewarded", "Ad was dismissed.")
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError?) {
                Log.e("Rewarded", "Ad failed to show.")
            }

            override fun onAdShowedFullScreenContent() {
                Log.e("Rewarded", "Ad showed fullscreen content.")
                // Called when ad is dismissed.
                // Don't set the ad reference to null to avoid showing the ad a second time.
                //  mRewardedAd = null
            }
        }
    }



    public fun show() {
        if (mRewardedAd != null) {
            mRewardedAd?.show(this, {
                fun onUserEarnedReward(rewardItem: RewardItem) {
                    var rewardAmount = rewardItem.amount
                    var rewardType = rewardItem.getType()
                    Log.e("Rewarded", "User earned the reward.")
                }
            })
        } else {
            Log.e("Rewarded", "The rewarded ad wasn't ready yet.")
        }
    }







    private lateinit var permissionsUtil: PermissionsUtil
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setToolbar() {
        when {
            intent.action!!.equals(getString(R.string.friend))-> {
                setEventAction("Friend",R.drawable.bg_green_ractangle,getString(R.string.find_me_friend),
                    View.VISIBLE,R.color.green_28C76F)
            }
            intent.action!!.equals(getString(R.string.date))-> {
                setEventAction("Date",R.drawable.bg_red_ractangle,getString(R.string.find_me_date),
                    View.VISIBLE,R.color.red_F94757)
            }
            intent.action!!.equals(getString(R.string.professional))-> {
                setEventAction("Professional",R.drawable.bg_blue_ractangle,getString(R.string.find_me_professional),
                    View.VISIBLE,R.color.bg_button_blue)
            }
            intent.action!!.equals(getString(R.string.counsellor))-> {
                setEventAction("Counsellor",R.drawable.bg_yellow_ractangle,getString(R.string.connect_a_counsellor),
                    View.GONE,R.color.yellow_FFA602)
            }
        }
    }
    var colorCode=-1
    @RequiresApi(Build.VERSION_CODES.M)
    private fun setEventAction(type:String,bgGreenRactangle: Int, tvToolbarName: String, visible: Int, color: Int) {
        viewModel.getTagListApi(userId!!.toInt(),type)
        colorCode=color
        //binding.toolbar.setBackgroundResource(bgGreenRactangle)
        binding.tvToolbarName.text = tvToolbarName
        binding.cvProceed.setCardBackgroundColor(getColor(color))
        //new button
        binding.cvProceedNew.setCardBackgroundColor(getColor(color))
        binding.ivMale.setBackgroundResource(R.drawable.circle_round)
        binding.ivMale.backgroundTintList=getColorStateList(color)

        binding.tvMinAge.backgroundTintList=getColorStateList(color)
        binding.tvMinAge2.backgroundTintList=getColorStateList(color)
        binding.tvMinAge3.backgroundTintList=getColorStateList(color)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        viewModel.isOnlineStatusApi(this,2, userId!!.toInt())
        if(cTimer!=null) cTimer!!.cancel()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onClick(v: View?) {
        when(v){
            binding.ivMale->{
                gender="Male"
                binding.ivMale.setBackgroundResource(R.drawable.circle_round)
                binding.ivMale.backgroundTintList=getColorStateList(colorCode)
                binding.ivFemale.setBackgroundColor(getColor(android.R.color.transparent))
                binding.ivOther.setBackgroundColor(getColor(android.R.color.transparent))
                binding.ivMale.alpha=1.0f
                binding.ivFemale.alpha=0.50f
                binding.ivOther.alpha=0.50f
            }
            binding.ivFemale->{
                gender="Female"
                binding.ivMale.setBackgroundColor(getColor(android.R.color.transparent))
                binding.ivFemale.setBackgroundResource(R.drawable.circle_round)
                binding.ivFemale.backgroundTintList=getColorStateList(colorCode)
                binding.ivOther.setBackgroundColor(getColor(android.R.color.transparent))
                binding.ivMale.alpha=0.50f
                binding.ivFemale.alpha=1.00f
                binding.ivOther.alpha=0.50f
            }
            binding.ivOther->{
                gender="Other"
                binding.ivMale.setBackgroundColor(getColor(android.R.color.transparent))
                binding.ivFemale.setBackgroundColor(getColor(android.R.color.transparent))
                binding.ivOther.setBackgroundResource(R.drawable.circle_round)
                binding.ivOther.backgroundTintList=getColorStateList(colorCode)
                binding.ivMale.alpha=0.50f
                binding.ivFemale.alpha=.50f
                binding.ivOther.alpha=1.00f
            }
            binding.ivIncAge->{
                var value=binding.tvMinAge.text.toString().toInt()
                Log.d("TAG", "onClick: ivDecAge:- "+value)
                if(value<59) {
                    binding.tvMinAge.text=Editable.Factory.getInstance().newEditable((value+1).toString())
                    binding.tvMinAge2.text=Editable.Factory.getInstance().newEditable((value+2).toString())
                }
            }
            binding.ivDecAge->{
                var value=binding.tvMinAge.text.toString().toInt()
                if(value>16) {
                    binding.tvMinAge.text=Editable.Factory.getInstance().newEditable((value-1).toString())
                    binding.tvMinAge2.text=Editable.Factory.getInstance().newEditable((value).toString())
                }
            }
            binding.ivIncAge2->{
                var value=binding.tvMinAge2.text.toString().toInt()
                if(value<60) {
                    binding.tvMinAge2.text=Editable.Factory.getInstance().newEditable((value+1).toString())
                }
            }
            binding.ivDecAge2->{
                var min=binding.tvMinAge.text.toString().toInt()
                var max=binding.tvMinAge2.text.toString().toInt()
                if((min+1)<max){
                    binding.tvMinAge2.text=Editable.Factory.getInstance().newEditable((max-1).toString())
                }
            }

            binding.ivDecAge3->{
                var value=binding.tvMinAge3.text.toString().toInt()
                if(value>1) {
                    binding.tvMinAge3.text=Editable.Factory.getInstance().newEditable((value-1).toString())
                }
            }
            binding.ivIncAge3->{
                var value=binding.tvMinAge3.text.toString().toInt()
                if(value<20){
                    binding.tvMinAge3.text=Editable.Factory.getInstance().newEditable((value+1).toString())
                }
            }
            binding.cvProceedNew->{
                selectList.clear()
                tagListNew.forEachIndexed { index, model ->
                    model.takeIf { it.isSelected == true}?.let {
                        selectList.add(model.tagName)
                    }
                }
                if (latitute.isEmpty() && longtitute.isEmpty()) {
                    askPermission()
                }else if(selectList.size!=0) {
                    showDialog()
                    viewModel.setMatchApiNew(this, userId!!.toInt(),
                        gender,
                        binding.tvMinAge.text.toString().toInt(),
                        binding.tvMinAge2.text.toString().toInt(),
                        binding.tvMinAge3.text.toString().toInt(),
                        selectList,
                        latitute,
                        longtitute)
                }else showSnackBar(binding.root, getString(R.string.select_one_teg))
            }
            binding.cvProceed->{
                binding.progressBar.visibility=View.VISIBLE
                selectList.clear()
                //selectList.add("music")
                tagListNew.forEachIndexed { index, model ->
                    model.takeIf { it.isSelected == true}?.let {
                        selectList.add(model.tagName)
                    }
                }
                viewModel.updateTagsApi(
                    userId!!.toInt(),selectList)

                if (latitute.isEmpty() && longtitute.isEmpty()) {
                    askPermission()
                } else if(selectList.size!=0) {
                    viewModel.setMatchApi(
                        userId!!.toInt(),
                        gender,
                        binding.tvMinAge.text.toString().toInt(),
                        binding.tvMinAge2.text.toString().toInt(),
                        binding.tvMinAge3.text.toString().toInt(),
                        selectList,
                        latitute,
                        longtitute
                    )
                    setTimmer()
                    /*viewModel.setMatchApi(
                        36,
                        gender,
                        15,
                        25,
                        100,
                        selectList,
                        "29.5038807",
                        "75.4504951"
                    )*/
                }else showSnackBar(binding.root, getString(R.string.select_one_teg))



                // startActivity(Intent(this, MatchFoundActivity::class.java).apply {action=intent.action})
            }
        }
    }

    /* override fun onLocationChanged(p0: Location) {
         Log.d("TAG", "onLocationChanged lat: "+p0.latitude)
         Log.d("TAG", "onLocationChanged long: "+p0.longitude)
     }*/

    private fun doesUserHavePermission(): Int {
        val permissionCheck =
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        return permissionCheck
    }
    private fun askPermission() {
        permissionsUtil.askPermissions(this,
            PermissionsUtil.LOCATION,
            PermissionsUtil.LOCATION_CORSE,
            object : PermissionsUtil.PermissionListener {
                override fun onPermissionResult(isGranted: Boolean) {
                    if (isGranted) {
                        requestLocation()
                    } else {
                        if (doesUserHavePermission() == 0)
                            askPermission()
                    }
                }

            })
    }
    private fun locationPermissionDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_location_permission)
        dialog.window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
        dialog.tvAllow.setOnClickListener {
            askPermission()
            dialog.dismiss()

        }
        dialog.tvDecline.setOnClickListener {
            dialog.dismiss()
            //finish()
        }
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
    }
    private var mLocationCallback: LocationCallback? = null
    private var mLocationRequest: LocationRequest? = null
    private val REQUEST_CHECK_SETTINGS = 0x1
    private fun requestLocation() {
        mLocationRequest = LocationRequest()
        mLocationRequest!!.interval = 1000
        mLocationRequest!!.fastestInterval = 1000
        mLocationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(mLocationRequest!!)
        val client = LocationServices.getSettingsClient(this)
        val task =
            client.checkLocationSettings(builder.build())
        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                if (locationResult.lastLocation != null) {
                    latitute = locationResult.lastLocation.latitude.toString()
                    longtitute = locationResult.lastLocation.longitude.toString()
                    stopLocationUpdates()
                }
            }
        }
        task.addOnSuccessListener(this) { startLocationUpdates() }
        task.addOnFailureListener(this) { e ->
            when ((e as ApiException).statusCode) {
                CommonStatusCodes.RESOLUTION_REQUIRED ->
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        val resolvable = e as ResolvableApiException
                        resolvable.startResolutionForResult(
                            this,
                            REQUEST_CHECK_SETTINGS
                        )
                    } catch (sendEx: IntentSender.SendIntentException) {
                        // Ignore the error.
                    }
                LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                }
            }
        }
    }
    private fun stopLocationUpdates() {
        if (mLocationCallback != null) fusedLocationClient.removeLocationUpdates(
            mLocationCallback
        )
    }
    override fun onStop() {
        super.onStop()
        stopLocationUpdates()
    }
    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null)
    }

    override fun onResume() {
        super.onResume()
       // binding.cvProceed.visibility=View.VISIBLE
       // binding.cvTimer.visibility=View.GONE
    }

    var cTimer: CountDownTimer? = null
    private fun setTimmer() {
        binding.cvProceedNew.visibility=View.GONE
        binding.cvTimer.visibility=View.VISIBLE
        cTimer=object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.tvTimer.text= getRevealTime(millisUntilFinished)
            }
            override fun onFinish() {
                /*MatchFoundActivity.gender = gender
                MatchFoundActivity.minAge = binding.tvMinAge.text.toString()
                MatchFoundActivity.maxAge = binding.tvMinAge2.text.toString()
                MatchFoundActivity.radius = binding.tvMinAge3.text.toString()
                MatchFoundActivity.selectList = selectList
                MatchFoundActivity.lat = latitute
                MatchFoundActivity.long = longtitute*/
                startActivity(Intent(applicationContext, SessionOverActivity::class.java).apply {
                    action = intent.action
                })
                finish()
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(broadcastReciver)
    }

}