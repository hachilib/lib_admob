package com.lib.hachi.appopenads

import android.app.Activity
import android.app.Application
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.Window
import android.widget.LinearLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.AdActivity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback
import java.util.Date

class AppOpenManager : Application.ActivityLifecycleCallbacks, LifecycleObserver {
    private val TAG = "AOPManager"

    lateinit var INSTANCE: AppOpenManager

    private var appResumeAd: AppOpenAd? = null
    private var splashAd: AppOpenAd? = null
    lateinit var loadCallback: AppOpenAdLoadCallback
    private var fullScreenContentCallback: FullScreenContentCallback? = null

    private var isInitialized = false
    lateinit var myApplication: Application
    private var appResumeAdId: String? = null
    private var currentActivity: Activity? = null

    var isAppResumeEnabled = true

    private var isShowingAd = false
    var isShowingAdsOnResume = false
    var isShowingAdsOnResumeBanner = false
    private var appResumeLoadTime: Long = 0
    private var splashLoadTime: Long = 0

    private var disabledAppOpenList: ArrayList<Class<*>>? = ArrayList()
    private val splashActivity: Class<*>? = null

    private var isTimeout = false
    private val TIMEOUT_MSG = 11

    private var dialogFullScreen: Dialog? = null

    private val timeoutHandler = Handler { msg: Message ->
        if (msg.what == TIMEOUT_MSG) {
            isTimeout = true
        }
        false
    }

    fun isShowingAd(): Boolean {
        return isShowingAd
    }

    @Synchronized
    fun getInstance() = AppOpenManager().apply {
        INSTANCE = this
        return INSTANCE
    }

    fun init(application: Application, appOpenAdId: String) {
        isInitialized = true
        this.myApplication = application
        initAdRequest()
        if (AdmobUtils.isTesting) {
            this.appResumeAdId = application.getString(R.string.test_ad_admob_app_open)
        } else {
            this.appResumeAdId = appOpenAdId
        }
        this.myApplication.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        if (!isAdAvailable(false) && appOpenAdId != null) {
            fetchAd(false)
        }
    }

    fun isInitialized(): Boolean {
        return this.isInitialized
    }


    fun isShowingAdsOnResumes(): Boolean {
        return isShowingAdsOnResume
    }

    fun disableAppResumeWithActivity(activityClass: Class<*>) {
        Log.d(TAG, "disableAppResumeWithActivity: " + activityClass.name)
        disabledAppOpenList!!.add(activityClass)
    }

    fun enableAppResumeWithActivity(activityClass: Class<*>) {
        Log.d(TAG, "enableAppResumeWithActivity: " + activityClass.name)
        disabledAppOpenList!!.remove(activityClass)
    }


    fun setAppResumeAdId(appResumeAdId: String?) {
        this.appResumeAdId = appResumeAdId
    }

    fun setFullScreenContentCallback(callback: FullScreenContentCallback?) {
        fullScreenContentCallback = callback
    }

    fun removeFullScreenContentCallback() {
        fullScreenContentCallback = null
    }


    fun fetchAd(isSplash: Boolean) {
        Log.d(TAG, "fetchAd: isSplash = $isSplash")
        if (isAdAvailable(isSplash) || appResumeAdId == null) {
            return
        }
        loadCallback = object : AppOpenAdLoadCallback() {
            override fun onAdLoaded(ad: AppOpenAd) {
                Log.d(TAG, "onAdLoaded: isSplash = $isSplash")
                if (!isSplash) {
                    appResumeAd = ad
                    appResumeLoadTime = Date().time
                } else {
                    splashAd = ad
                    splashLoadTime = Date().time
                }
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                // Handle the error.
                val a = "fail"
            }
        }
        AppOpenAd.load(
            myApplication, appResumeAdId!!, adRequest,
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, loadCallback
        )
    }

    lateinit var adRequest: AdRequest

    fun initAdRequest() {
        adRequest = AdRequest.Builder()
            .setHttpTimeoutMillis(5000)
            .build()
    }

    fun isAdAvailable(isSplash: Boolean): Boolean {
        val loadTime: Long = if (isSplash) splashLoadTime else appResumeLoadTime
        val wasLoadTimeLessThanNHoursAgo: Boolean = wasLoadTimeLessThanNHoursAgo(loadTime, 4)
        Log.d(TAG, "isAdAvailable: $wasLoadTimeLessThanNHoursAgo")
        return ((if (isSplash) splashAd != null else appResumeAd != null)
                && wasLoadTimeLessThanNHoursAgo)
    }

    private fun wasLoadTimeLessThanNHoursAgo(loadTime: Long, numHours: Long): Boolean {
        val dateDifference = Date().time - loadTime
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference < numMilliSecondsPerHour * numHours
    }

    override fun onActivityCreated(p0: Activity, p1: Bundle?) {}

    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity

        if (splashActivity == null) {
            if (activity.javaClass.name != AdActivity::class.java.name) {
                fetchAd(false)
            }
        } else {
            if (activity.javaClass.name != splashActivity.getName() && activity.javaClass.name != AdActivity::class.java.name) {
                fetchAd(false)
            }
        }
    }

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, p1: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {
        currentActivity = null
        if (dialogFullScreen != null && dialogFullScreen!!.isShowing) {
            dialogFullScreen!!.dismiss()
        }
    }

    fun showAdIfAvailable(isSplash: Boolean) {
        if (!ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            Log.d("===Onresume", "STARTED")
            if (fullScreenContentCallback != null) {
                try {
                    dialogFullScreen!!.dismiss()
                    dialogFullScreen = null
                } catch (ignored: Exception) {
                }
                fullScreenContentCallback!!.onAdDismissedFullScreenContent()
            }
            return
        }
        Log.d("===Onresume", "FullScreenContentCallback")
        if (!isShowingAd && isAdAvailable(isSplash)) {
            val callback: FullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    try {
                        dialogFullScreen!!.dismiss()
                        dialogFullScreen = null
                    } catch (ignored: Exception) {
                    }
                    // Set the reference to null so isAdAvailable() returns false.
                    appResumeAd = null
                    if (fullScreenContentCallback != null) {
                        fullScreenContentCallback!!.onAdDismissedFullScreenContent()
                    }
                    isShowingAd = false
                    fetchAd(isSplash)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    try {
                        dialogFullScreen!!.dismiss()
                        dialogFullScreen = null
                    } catch (ignored: Exception) {
                    }
                    if (fullScreenContentCallback != null) {
                        fullScreenContentCallback!!.onAdFailedToShowFullScreenContent(adError)
                    }
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(
                        TAG,
                        "onAdShowedFullScreenContent: isSplash = $isSplash"
                    )
                    isShowingAd = true
                    if (isSplash) {
                        splashAd = null
                    } else {
                        appResumeAd = null
                    }
                }
            }
            showAdsResume(isSplash, callback)
        } else {
            Log.d(TAG, "Ad is not ready")
            if (!isSplash) {
                fetchAd(false)
            }
        }
    }

    private fun showAdsResume(isSplash: Boolean, callback: FullScreenContentCallback) {
        if (ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            Handler().postDelayed({
                if (isSplash) {
                    splashAd!!.fullScreenContentCallback = callback
                    if (currentActivity != null) showDialog(currentActivity)
                    splashAd!!.show(currentActivity!!)
                } else {
                    if (appResumeAd != null) {
                        appResumeAd!!.fullScreenContentCallback = callback
                        if (currentActivity != null) showDialog(currentActivity)
                        appResumeAd!!.show(currentActivity!!)
                    }
                }
            }, 100)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onResume() {
        Log.d("===Onresume", "onresume")
        if (currentActivity == null) {
            return
        }
        if (AdmobUtils.isAdShowing) {
            return
        }
        if (!AdmobUtils.isShowAds) {
            return
        }
        if (!isAppResumeEnabled) {
            Log.d("===Onresume", "isAppResumeEnabled")
            return
        } else {
            //if (AdmobUtils.dialog != null && AdmobUtils.dialog.isShowing()) AdmobUtils.dialog.dismiss()
        }
        for (activity in disabledAppOpenList!!) {
            if (activity.name == currentActivity!!.javaClass.name) {
                Log.d(TAG, "onStart: activity is disabled")
                return
            }
        }
        showAdIfAvailable(false)
    }

    fun showDialog(context: Context?) {
        isShowingAdsOnResume = true
        isShowingAdsOnResumeBanner = true
        dialogFullScreen = Dialog(context!!)
        dialogFullScreen!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogFullScreen!!.setContentView(R.layout.dialog_full_screen_onresume)
        dialogFullScreen!!.setCancelable(false)
        dialogFullScreen!!.window!!.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        dialogFullScreen!!.window!!.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        try {
            if (!currentActivity!!.isFinishing && dialogFullScreen != null && !dialogFullScreen!!.isShowing) {
                dialogFullScreen!!.show()
            }
        } catch (ignored: java.lang.Exception) {
        }
    }

}