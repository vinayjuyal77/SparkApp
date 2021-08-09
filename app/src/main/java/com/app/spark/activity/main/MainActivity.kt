package com.app.spark.activity.main

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.*
import android.view.Gravity.START
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.app.spark.BuildConfig
import com.app.spark.R
import com.app.spark.activity.FixYourProfileActivity
import com.app.spark.activity.NewLoginActivity
import com.app.spark.constants.IntentConstant
import com.app.spark.constants.PrefConstant
import com.app.spark.create_page.CreateAPgae
import com.app.spark.create_page.PageViewActivity
import com.app.spark.databinding.ActivityMainBinding
import com.app.spark.fragment.AddPostBottomFragment
import com.app.spark.fragment.groupcall.CreateGroupCallBottomFragment
import com.app.spark.fragment.chat.ChatFragment
import com.app.spark.fragment.explore.ExploreFragment
import com.app.spark.fragment.feeds.FeedFragment
import com.app.spark.fragment.groupcall.GroupCallList
import com.app.spark.fragment.newflick.FlicksFragment
import com.app.spark.fragment.newflick.FlicksNewFragment
import com.app.spark.fragment.profile.ProfileFragment
import com.app.spark.interfaces.DrawerListener
import com.app.spark.interfaces.MainActivityInterface
import com.app.spark.utils.*
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlin.math.roundToInt


class MainActivity : BaseActivity(), BottomNavigationView.OnNavigationItemSelectedListener,
    MainActivityInterface, DrawerListener, SplashMainListener {

    private lateinit var binding: ActivityMainBinding
    private var profilePageRedirect: ProfileFragment? = null
    private var viewModel: MainViewModel? = null
    private var pref: SharedPrefrencesManager? = null
    private var fragment: BaseFragment? = null
    var fragment_value = "3"
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    @SuppressLint("WrongConstant")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        getActionBar()?.hide()

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        pref = SharedPrefrencesManager.getInstance(this)


        if(intent!=null) {
            if (intent.getBooleanExtra("splash", false)) {
                binding.bottomNavigation.visibility = View.VISIBLE
                binding.layoutVisibl.spalshsh.visibility = View.GONE
            }
        }

        initializeViewModel()
        setClickListener()
        update_dialog()

        otherProfileDataRedirect(intent)
        navCenterIconSizeIncrease()
        setNavigationBack()

        firebaseAnalytics = Firebase.analytics


        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, pref!!.getString(PrefConstant.USERNAME ,""))
        firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)


        /*if( pref!!.getString(PrefConstant.DATE_INFO, "")!="")
        {

            if(pref!!.getString(PrefConstant.DATE_INFO, "")=="first")
            {

                pref!!.setString(PrefConstant.DATE_INFO, "second")
                open_dilog_date_notifiaction()

            }
            else if( pref!!.getString(PrefConstant.DATE_INFO, "")=="second")
            {
                pref!!.setString(PrefConstant.DATE_INFO, "third")

                open_dilog_date_notifiaction()
            }
            else{

            }
        }
        else{
            pref!!.setString(PrefConstant.DATE_INFO, "first")
            open_dilog_date_notifiaction()
        }*/




        if(pref!!.getString(PrefConstant.PROFILE_PIC,"") == null || pref!!.getString(PrefConstant.NAME,"") == null ||pref!!.getString(PrefConstant.DOB,"") == null || pref!!.getString(PrefConstant.GENDER,"") == null)

            {
                open_dilog_date_profile()

            }
            else {

            if (pref!!.getString(PrefConstant.PROFILE_PIC, "") == "" || pref!!.getString(PrefConstant.NAME, "") == "" || pref!!.getString(PrefConstant.DOB, "") == "" || pref!!.getString(PrefConstant.GENDER, "") == "")
            {
                open_dilog_date_profile()
            }
        }










        binding.name.text = pref!!.getString(PrefConstant.USER_ID, "username")
        binding.username.text = "@" + pref!!.getString(PrefConstant.USER_ID, "username")

        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

        if(pref!!.getString(PrefConstant.PROFILE_PIC, "")!=null) {
            Glide.with(this)
                    .load(pref!!.getString(PrefConstant.PROFILE_PIC, ""))
                    .thumbnail(Glide.with(this).load(R.drawable.ic_app_logo))
                    .placeholder(R.drawable.ic_app_logo)
                    .centerCrop()
                    .into(binding.drawerProfile)
        }
        binding.closeBtn.setOnClickListener {

            binding.drawerLayout.closeDrawer(START)
        }

        binding.ivDropDown.setOnClickListener {
            if(binding.llCreatePage.isVisible) {
                binding.ivDropDown.rotation = 0F
                binding.llCreatePage.visibility=View.GONE
            }else {
                binding.ivDropDown.rotation = 180F
                binding.llCreatePage.visibility=View.VISIBLE
            }
        }
        binding.tvItemPage.setOnClickListener {
            binding.drawerLayout.closeDrawer(START)
            startActivity(Intent(this, PageViewActivity::class.java))
        }
        binding.tvClickCreate.setOnClickListener {
            binding.drawerLayout.closeDrawer(START)
            startActivity(Intent(this, CreateAPgae::class.java))
        }
        binding.logout.setOnClickListener {

            viewModel?.updateDeviceTokenApi(
                pref?.getString(PrefConstant.ACCESS_TOKEN, ""),
                pref?.getString(PrefConstant.USER_ID, ""),
                ""
            )
            pref?.clear()

            clearAllgoToActivity(this, NewLoginActivity::class.java)

        }



        binding.createProfile.setOnClickListener {

            Toast.makeText(this@MainActivity, "Coming soon", Toast.LENGTH_SHORT).show()
//            val intent = Intent(this, SettingActivity::class.java)
//            startActivity(intent)
        }

        binding.activeService.setOnClickListener {

            Toast.makeText(this@MainActivity, "Coming soon", Toast.LENGTH_SHORT).show()
//            val intent = Intent(this, SettingActivity::class.java)
//            startActivity(intent)
        }

        binding.analytics.setOnClickListener {

            Toast.makeText(this@MainActivity, "Coming soon", Toast.LENGTH_SHORT).show()
//            val intent = Intent(this, SettingActivity::class.java)
//            startActivity(intent)
        }

        binding.earning.setOnClickListener {

            Toast.makeText(this@MainActivity, "Coming soon", Toast.LENGTH_SHORT).show()
//            val intent = Intent(this, SettingActivity::class.java)
//            startActivity(intent)
        }
        binding.joinUs.setOnClickListener {

            Toast.makeText(this@MainActivity, "Coming soon", Toast.LENGTH_SHORT).show()
//            val intent = Intent(this, SettingActivity::class.java)
//            startActivity(intent)
        }
        binding.archivie.setOnClickListener {

            Toast.makeText(this@MainActivity, "Coming soon", Toast.LENGTH_SHORT).show()
//            val intent = Intent(this, SettingActivity::class.java)
//            startActivity(intent)
        }
        binding.saved.setOnClickListener {

            Toast.makeText(this@MainActivity, "Coming soon", Toast.LENGTH_SHORT).show()
//            val intent = Intent(this, SettingActivity::class.java)
//            startActivity(intent)
        }
        binding.setting.setOnClickListener {

            Toast.makeText(this@MainActivity, "Coming soon", Toast.LENGTH_SHORT).show()
//            val intent = Intent(this, SettingActivity::class.java)
//            startActivity(intent)
        }
        binding.discoverPeople.setOnClickListener {

            Toast.makeText(this@MainActivity, "Coming soon", Toast.LENGTH_SHORT).show()
//            val intent = Intent(this, SettingActivity::class.java)
//            startActivity(intent)
        }
        binding.help.setOnClickListener {

            Toast.makeText(this@MainActivity, "Coming soon", Toast.LENGTH_SHORT).show()
//            val intent = Intent(this, SettingActivity::class.java)
//            startActivity(intent)
        }

        binding.setting.setOnClickListener {

            Toast.makeText(this@MainActivity, "Coming soon", Toast.LENGTH_SHORT).show()
//            val intent = Intent(this, SettingActivity::class.java)
//            startActivity(intent)
        }






    }

    private fun setNavigationBack() {


        if(intent.getIntExtra(IntentConstant.PAGE_FLAG, 0)==2){
            binding.bottomNavigation.visibility = View.VISIBLE
            binding.layoutVisibl.spalshsh.visibility = View.GONE
            binding.bottomNavigation.selectedItemId=R.id.tab_flick
        }

    }


    private fun updateDeviceToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if (!it.isSuccessful) {
                return@addOnCompleteListener
            }
            // Get new FCM registration token
            val token = it.result
            viewModel?.updateDeviceTokenApi(
                pref?.getString(PrefConstant.ACCESS_TOKEN, ""),
                pref?.getString(PrefConstant.USER_ID, ""),
                token
            )
        }

    }

    private fun initializeViewModel() {
        viewModel =
            ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(
                MainViewModel::class.java
            )
        updateDeviceToken()
    }

    private fun setClickListener() {
        binding.bottomNavigation.setOnNavigationItemSelectedListener(this)
        binding.bottomNavigation.itemIconTintList = null
        binding.bottomNavigation.selectedItemId = R.id.tab_flick
    }


    private fun otherProfileDataRedirect(intent: Intent?) {
        if (intent != null && intent.hasExtra(IntentConstant.PROFILE_ID)) {
            this.intent = intent
            binding.bottomNavigation.itemIconTintList = null
            binding.bottomNavigation.selectedItemId = R.id.tab_profile
        }
    }


    private fun navCenterIconSizeIncrease() {
        var bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        var menuView: BottomNavigationMenuView =
            bottomNavigationView.getChildAt(0) as BottomNavigationMenuView
        for (i in 0 until menuView.childCount) {
            if (i == 2) {
                val iconView: View =
                    menuView.getChildAt(i).findViewById(com.google.android.material.R.id.icon)
                val layoutParams: ViewGroup.LayoutParams = iconView.layoutParams
                val displayMetrics: DisplayMetrics = resources.displayMetrics
                layoutParams.height = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 42F,
                    displayMetrics
                ).roundToInt()
                layoutParams.width = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 42F,
                    displayMetrics
                ).roundToInt()
                iconView.layoutParams = layoutParams
            }
        }
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
           /* R.id.tab_chat -> {
              this.window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
               // setNavigationBackground()
                binding.bottomNavigation.setBackgroundResource(R.color.backgroundColorBottomBar)
                removeListener()
                fragment = ChatFragment()
                fragment?.registerMainInterface(this)
                setFragments(fragment!!)
                fragment_value = "0"
                return true

            }*/
            R.id.tab_timeline -> {
                this.window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                binding.bottomNavigation.setBackgroundResource(R.color.backgroundColorBottomBar)
                removeListener()
                fragment = FeedFragment()
                fragment?.registerMainInterface(this)
                setFragments(fragment!!)
                fragment_value = "0"
                return true
            }
              R.id.tab_activities -> {
                  binding.bottomNavigation.setBackgroundResource(R.color.backgroundColorBottomBar)
                 this.window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                removeListener()
                fragment = ExploreFragment()
                fragment!!.registerMainInterface(this)
                setFragments(fragment!!)
                  fragment_value = "1"

                  return true
            }
            R.id.tab_feed -> {

                this.window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                binding.bottomNavigation.setBackgroundResource(R.color.backgroundColorBottomBar)
       //         setNavigationBackground()
//                removeListener()
//                fragment = FeedFragment()
//                fragment?.registerMainInterface(this)
//                setFragments(fragment!!)
//                fragment_value = "2"

                removeListener()
                fragment = GroupCallList()
                fragment?.registerMainInterface(this)
                setFragments(fragment!!)
                fragment_value = "2"

                return true

            }

            R.id.tab_flick -> {

                firebaseAnalytics = Firebase.analytics


                val bundle = Bundle()
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, pref!!.getString(PrefConstant.USERNAME ,""))
                firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)

                this.window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                binding.bottomNavigation.setBackgroundResource(R.color.backgroundColorBottomBar)
                removeListener()
//                fragment = FlicksFragment()
//                fragment?.registerMainInterface(this)
//                setFragments(fragment!!)
//                fragment_value = "3"


                fragment = FlicksNewFragment()
                fragment?.registerMainInterface(this)
                setFragments(fragment!!)
                fragment_value = "3"



                return true

            }
            R.id.tab_profile -> {
                binding.bottomNavigation.setBackgroundResource(R.color.backgroundColorBottomBar)
                removeListener()
                if (intent.hasExtra(IntentConstant.PROFILE_ID)) {
                    val bundle = Bundle()
                    val ids = intent.getStringExtra(IntentConstant.PROFILE_ID)
                    bundle.putString(IntentConstant.PROFILE_ID, ids)
                    profilePageRedirect = ProfileFragment()
                    profilePageRedirect?.registerMainInterface(this)
                    profilePageRedirect?.arguments = bundle
                    intent.putExtra(IntentConstant.PROFILE_ID, "")
                    setFragments(profilePageRedirect!!)
                } else {
                    val bundle = Bundle()
                    bundle.putString(IntentConstant.PROFILE_ID, "")
                    profilePageRedirect = ProfileFragment()
                    profilePageRedirect?.registerMainInterface(this)
                    profilePageRedirect?.arguments = bundle
                    setFragments(profilePageRedirect!!)
                }
                fragment_value = "4"
                return true
            }

        }
        return false
    }

    private fun removeListener() {
        fragment?.stopMusic()
        fragment?.removeInterface()
        profilePageRedirect?.removeInterface()
    }

    private fun setNavigationBackground() {
        binding.bottomNavigation.setBackgroundResource(R.color.backgroundColorApp)
        binding.bottomNavigation.itemBackgroundResource = R.color.semi_trans_parent
    }

    private fun setFragments(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(
            R.id.container,
            fragment,
            fragment.javaClass.simpleName
        ).commit()
    }

    override fun onAddPost() {
        AddPostBottomFragment().show(supportFragmentManager, "add_post")
    }

    override fun onCreateGroup() {
       CreateGroupCallBottomFragment().show(supportFragmentManager, "create group")
    }

    override fun onLogOut() {
        viewModel?.updateDeviceTokenApi(
            pref?.getString(PrefConstant.ACCESS_TOKEN, ""),
            pref?.getString(PrefConstant.USER_ID, ""),
            ""
        )
        pref?.clear()
        clearAllgoToActivity(this, NewLoginActivity::class.java)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        otherProfileDataRedirect(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        profilePageRedirect?.onActivityResult(requestCode, resultCode, data)
    }

    @SuppressLint("WrongConstant")
    override fun open(dataItem: String) {

     //   showToastShort(this, "Yes")

        Glide.with(this)
            .load(pref!!.getString(PrefConstant.PROFILE_PIC, ""))
            .thumbnail(Glide.with(this).load(R.drawable.ic_app_logo))
            .placeholder(R.drawable.ic_app_logo)
            .centerCrop()
            .into(binding.drawerProfile)

        binding.drawerLayout.openDrawer(Gravity.START)
        binding.name.text = dataItem
        binding.username.text = "@" + dataItem

    }

    @SuppressLint("WrongConstant")
    override fun colse(dataItem: String) {
        TODO("Not yet implemented")

        binding.drawerLayout.closeDrawer(Gravity.START)
    }

    override fun backclick(dataItem: String) {
        onBackPressed()
    }


    fun update_dialog()
    {



        var firebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        val configBuilder = FirebaseRemoteConfigSettings.Builder()


        val cacheInterval: Long = 0
        configBuilder.minimumFetchIntervalInSeconds = cacheInterval

        // finally build config settings and sets to Remote Config
        // finally build config settings and sets to Remote Config

        firebaseRemoteConfig.setConfigSettingsAsync(configBuilder.build())

        firebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)


        firebaseRemoteConfig.fetchAndActivate()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val onlineVersion = firebaseRemoteConfig.getString("update_enable")
                    //        Toast.makeText(Dashboard.this, onlineVersion, Toast.LENGTH_SHORT).show();
                    val versionCode: Int = BuildConfig.VERSION_CODE
                    if (onlineVersion.toInt() > versionCode) {
                        open_dilog_box_ask(
                            "0",
                            "New version update is available on play store please update your app for better perfomance."
                        )

                    } else {

                    }
                }
            }


    }
    fun open_dilog_box_ask(status: String, message: String?) {
        val factory = LayoutInflater.from(this)
        val deleteDialogView: View = factory.inflate(R.layout.dialog_update, null)
        val deleteDialog = AlertDialog.Builder(this).create()
        deleteDialog.setView(deleteDialogView)
        deleteDialog.setCancelable(false)
        val yes = deleteDialogView.findViewById<Button>(R.id.yes)
        val no = deleteDialogView.findViewById<Button>(R.id.no)
        val text_message = deleteDialogView.findViewById<TextView>(R.id.text_message)
        text_message.text = message
        if (status == "0") {
            no.visibility = View.VISIBLE
        } else {
            no.visibility = View.GONE
        }
        yes.setOnClickListener {
            try {
                val viewIntent = Intent(
                    "android.intent.action.VIEW",
                    Uri.parse("https://play.google.com/store/apps/details?id=com.app.spark")
                )
                startActivity(viewIntent)
            } catch (e: Exception) {
                Toast.makeText(
                    applicationContext, "Unable to Connect Try Again...",
                    Toast.LENGTH_LONG
                ).show()
                e.printStackTrace()
            }

        }
        no.setOnClickListener { deleteDialog.dismiss() }
        deleteDialog.show()
    }



    fun open_dilog_date_notifiaction() {
        val factory = LayoutInflater.from(this)
        val deleteDialogView: View = factory.inflate(R.layout.dialog_date_new, null)
        val deleteDialog = AlertDialog.Builder(this).create()
        deleteDialog.setView(deleteDialogView)
        val image_view = deleteDialogView.findViewById<ImageView>(R.id.image_view)

        image_view.setOnClickListener {


            this.window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            removeListener()
            setNavigationBackground()
            fragment = ExploreFragment()
            fragment!!.registerMainInterface(this)
            setFragments(fragment!!)
            fragment_value = "1"

            deleteDialog.dismiss()

        }

        deleteDialog.show()
    }
    fun open_dilog_date_profile() {
        val factory = LayoutInflater.from(this)
        val deleteDialogView: View = factory.inflate(R.layout.dialog_profile_update, null)
        val deleteDialog = AlertDialog.Builder(this).create()
        deleteDialog.setView(deleteDialogView)
        deleteDialog.setCancelable(false)
        val update = deleteDialogView.findViewById<TextView>(R.id.update)

        update.setOnClickListener {



            this.window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            removeListener()
            startActivity(Intent(this@MainActivity, FixYourProfileActivity::class.java))
            deleteDialog.dismiss()



        }

        deleteDialog.show()

    }



    override fun onBackPressed() {

        if(fragment_value.equals("3")) {

            super.onBackPressed()

        }
        else
        {
            setClickListener()
        }
    }

    override fun dismissSplash() {

        binding.layoutVisibl.spalshsh.visibility =View.GONE
        binding.bottomNavigation.visibility =View.VISIBLE

       // Toast.makeText(this, "Working", Toast.LENGTH_SHORT).show()
    }
}