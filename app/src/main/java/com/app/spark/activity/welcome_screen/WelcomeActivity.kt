package com.app.spark.activity.welcome_screen

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.app.spark.R
import com.app.spark.activity.LoginSignUpActivity
import com.app.spark.activity.NewLoginActivity
import com.app.spark.activity.NewSignupActivity
import com.app.spark.activity.main.MainActivity
import com.app.spark.constants.PrefConstant
import com.app.spark.utils.SharedPrefrencesManager

class WelcomeActivity : AppCompatActivity() {
    private var viewPager: ViewPager? = null
    private var myViewPagerAdapter: MyViewPagerAdapter? = null
    private var dotsLayout: LinearLayout? = null
    private var dots: Array<TextView?>? = null
    private var layouts: IntArray? = null
    private var btnNext: Button? = null
    var btnSkip: TextView? = null
    var signup : LinearLayout? = null
    var login : LinearLayout? = null
    lateinit var pref: SharedPrefrencesManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome_screen)

        pref = SharedPrefrencesManager.getInstance(this)

        if (SharedPrefrencesManager.getInstance(this)
                .getBoolean(PrefConstant.IS_LOGIN, false)){
            val intent = Intent(this@WelcomeActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
//            val intent = Intent(this, NewLoginActivity::class.java)
//            startActivity(intent)
//            finish()
        }
        viewPager = findViewById<View>(R.id.view_pager) as ViewPager
        dotsLayout = findViewById<View>(R.id.layoutDots) as LinearLayout
        btnSkip = findViewById(R.id.btn_skip)
        btnNext = findViewById<View>(R.id.btn_next) as Button
        signup = findViewById<View>(R.id.signup) as LinearLayout
        login = findViewById<View>(R.id.llogin) as LinearLayout


        // layouts of all welcome sliders
        // add few more layouts if you want
        layouts = intArrayOf(
            R.layout.fragment_welcome1,
            R.layout.fragment_welcome2,
            R.layout.fragment_welcome3
        )

        // adding bottom dots
        addBottomDots(0)

        // making notification bar transparent
        changeStatusBarColor()
        myViewPagerAdapter = MyViewPagerAdapter()
        viewPager!!.adapter = myViewPagerAdapter
        viewPager!!.addOnPageChangeListener(viewPagerPageChangeListener)
        btnSkip?.setOnClickListener(View.OnClickListener {

            val intent  = Intent(this, NewLoginActivity::class.java)
            startActivity(intent)
        })
        btnNext!!.setOnClickListener {
            // checking for last page
            // if last page home screen will be launched
            val current = getItem(+1)
            if (current < layouts!!.size) {
                // move to next screen
                viewPager!!.currentItem = current
            } else {
                //  launchHomeScreen();
            }
        }



        signup?.setOnClickListener {


            val intent  = Intent(this, NewSignupActivity::class.java)
            startActivity(intent)
        }


        login?.setOnClickListener {


            val intent  = Intent(this, NewLoginActivity::class.java)
            startActivity(intent)


        }
    }

    private fun addBottomDots(currentPage: Int) {
        dots = arrayOfNulls(layouts!!.size)
        val colorsActive = resources.getIntArray(R.array.array_dot_active)
        val colorsInactive = resources.getIntArray(R.array.array_dot_inactive)
        dotsLayout!!.removeAllViews()
        for (i in dots!!.indices) {
            dots!![i] = TextView(this)
            dots!![i]!!.text = Html.fromHtml("&#8226;")
            dots!![i]!!.textSize = 35f
            dots!![i]!!.setTextColor(colorsInactive[currentPage])
            dotsLayout!!.addView(dots!![i])
        }
        if (dots!!.size > 0) dots!![currentPage]!!.setTextColor(colorsActive[currentPage])
    }

    private fun getItem(i: Int): Int {
        return viewPager!!.currentItem + i
    }

    //  viewpager change listener
    var viewPagerPageChangeListener: OnPageChangeListener = object : OnPageChangeListener {
        override fun onPageSelected(position: Int) {
            addBottomDots(position)

            // changing the next button text 'NEXT' / 'GOT IT'
            if (position == layouts!!.size - 1) {
                // last page. make button text to GOT IT
                // btnNext.setText(getString(R.string.start));
            //    btnSkip!!.visibility = View.GONE
            } else {
                // still pages are left
                //  btnNext.setText(getString(R.string.next));
                btnSkip!!.visibility = View.VISIBLE
            }
        }

        override fun onPageScrolled(arg0: Int, arg1: Float, arg2: Int) {}
        override fun onPageScrollStateChanged(arg0: Int) {}
    }

    /**
     * Making notification bar transparent
     */
    private fun changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.TRANSPARENT
        }
    }

    /**
     * View pager adapter
     */
    inner class MyViewPagerAdapter : PagerAdapter() {
        private var layoutInflater: LayoutInflater? = null
        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            layoutInflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = layoutInflater!!.inflate(layouts!![position], container, false)
            container.addView(view)
            return view
        }

        override fun getCount(): Int {
            return layouts!!.size
        }

        override fun isViewFromObject(view: View, obj: Any): Boolean {
            return view === obj
        }





        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            val view = `object` as View
            container.removeView(view)
        }
    }
}