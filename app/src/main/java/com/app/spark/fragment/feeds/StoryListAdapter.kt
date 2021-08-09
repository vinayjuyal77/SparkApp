package com.app.spark.fragment.feeds

import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Vibrator
import android.util.Log
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.app.spark.R
import com.app.spark.activity.camera.CameraCustom
import com.app.spark.activity.story.dataClass.StoryResult
import com.app.spark.activity.story.screen.StoryMainActivity
import com.app.spark.constants.PrefConstant
import com.app.spark.databinding.ItemAddsBinding
import com.app.spark.databinding.ItemStoryListBinding
import com.app.spark.interfaces.OnFeedSelectedInterface
import com.app.spark.utils.SharedPrefrencesManager
import com.bumptech.glide.Glide
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import java.util.*


class StoryListAdapter(
    var context: Context,
    val list: ArrayList<StoryResult>,
    val itemInterface: OnFeedSelectedInterface? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var selectedItem = 0

    var vibe = context.getSystemService(VIBRATOR_SERVICE) as Vibrator?

    private var adLoader: AdLoader? = null

    var like_count = 0
    var view_count = 0

    lateinit var pref: SharedPrefrencesManager


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        var binding: ItemStoryListBinding;
        var binding2: ItemAddsBinding;


        if (viewType == ADD_LAYOUT) {

            binding2 = DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.item_adds,
                parent,
                false
            )
            return ViewHolder2(binding2)
        } else if (viewType == FEED_LAYOUT) {

            binding = DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.item_story_list,
                parent,
                false
            )
            return ViewHolder(binding)
        }


        binding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.item_feed,
            parent,
            false
        )

        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {

        return list.size

    }


    override fun getItemViewType(position: Int): Int {

        if (position == 0) {
            return FEED_LAYOUT

        } else if (position % 6 == 0) {
            return FEED_LAYOUT
        } else {
            return FEED_LAYOUT
        }

    }




    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        //    holder.bindData(list[position], position, itemInterface)

        when (holder) {
            is ViewHolder -> holder.bindData(position,list[position])
            is ViewHolder2 -> holder.bindData(list[position], position, itemInterface)
            //  is ColleagueViewHolder -> holder.bind(element as ColleagueDataModel)
            else -> throw IllegalArgumentException()
        }

    }

//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        holder.bindData(list[position], position, itemInterface)
//    }


    inner class ViewHolder(var binding: ItemStoryListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindData(
            position: Int,
            item: StoryResult

        ) {
            //   binding.item = item
            pref = SharedPrefrencesManager.getInstance(context)

            if(position==0)
            {

                   // binding.adddd.visibility =View.VISIBLE
                    Glide.with(context).load(R.drawable.ic_add_plus).into(binding.imageStory)
                    binding.storyUsername.text = "Add Story"



        }
            else
            {
                Glide.with(context).load(item.user.profile_pic).into(binding.imageStory)
                binding.adddd.visibility =View.GONE
                binding.storyUsername.text = item.user.username


            }



            binding.imageStory.setOnClickListener {

                if(position==0) {

                    if(item.stories.size>0)
                    {




                        var bundle = Bundle()
                        var intent =  Intent(Intent(context, StoryMainActivity::class.java))
                        bundle.putParcelableArrayList("Stories", list)
                        bundle.putInt("count", position)
                        intent.putExtras(bundle)
                        context.startActivity(intent)

//                        context.startActivity(Intent(context, StoryMainActivity::class.java)
//                            .putExtra("Stories", list)
//                            .putExtra("count",position ))


                    }
                    else
                    {
                        context.startActivity(Intent(context, CameraCustom::class.java)
                        )
                    }


                }
                else {


                    list.removeAt(0)
                    var bundle = Bundle()
                    var intent =  Intent(Intent(context, StoryMainActivity::class.java))
                    bundle.putParcelableArrayList("Stories", list)
                    bundle.putInt("count", position-1)
                    intent.putExtras(bundle)
                    context.startActivity(intent)




                }

            }

        }
    }


    inner class ViewHolder2(var binding: ItemAddsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindData(
            item: StoryResult,
            position: Int,
            itemInterface: OnFeedSelectedInterface?
        )
        {

           // binding.item = item



            adLoader = AdLoader.Builder(context, "ca-app-pub-3601101210502228/6966876520")
                .forNativeAd { ad : NativeAd ->
                    val background: ColorDrawable? = null
                    val styles =
                        NativeTemplateStyle.Builder().withMainBackgroundColor(background)
                            .build()

                    binding.nativeTemplateView?.setStyles(styles)
                    binding.nativeTemplateView?.setNativeAd(ad)
                    ///     adLoaded = true
                }
                .withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        // Handle the failure by logging, altering the UI, and so on.
                        Log.e("EROROR", "Failed to load native ad: $adError")
                        binding.nativeTemplateView.visibility = View.GONE
                        binding.background.visibility = View.VISIBLE

                        binding.imgPost.setImageResource(R.drawable.admob_image1)
                    }
                })
                .withNativeAdOptions(
                    NativeAdOptions.Builder()
                    // Methods in the NativeAdOptions.Builder class can be
                    // used here to specify individual options settings.
                    .build())
                .build()


//            adLoader = AdLoader.Builder(context, "ca-app-pub-3940256099942544/2247696110").withAdListener(object : AdListener() {
//                override fun onAdFailedToLoad(errorCode: Int) {
//                    Toast.makeText(context, "Failed to load native ad: $errorCode",
//                        Toast.LENGTH_SHORT).show()
//                    Log.e("EROROR","Failed to load native ad: $errorCode",)
//                }
//            }).build()


            loadNativeAd();


        }


    }


    private fun loadNativeAd() {
        // Creating  an Ad Request
        val adRequest = AdRequest.Builder().build()

        // load Native Ad with the Request
        adLoader!!.loadAd(adRequest)

        // Showing a simple Toast message to user when Native an ad is Loading
        //  Toast.makeText(context, "Native Ad is loading ", Toast.LENGTH_LONG).show()
    }


    companion object {
        private const val FEED_LAYOUT = 0
        private const val ADD_LAYOUT = 1

    }
}