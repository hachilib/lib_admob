package com.lib.hachi.appopenads

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.LinearLayout
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.airbnb.lottie.LottieAnimationView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.OnPaidEventListener
import com.google.android.gms.ads.formats.NativeAdOptions
import com.google.android.gms.ads.initialization.InitializationStatus
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.lib.hachi.appopenads.callback.interfaces.NativeCallbackAd
import com.lib.hachi.appopenads.enumclass.CollapsibleBanner
import com.lib.hachi.appopenads.enumclass.GoogleSizeNative
import com.lib.hachi.appopenads.enumclass.natives.NativeFunc.Companion.populateNativeAdView
import com.lib.hachi.appopenads.model.holder.InterHolder
import com.lib.hachi.appopenads.model.holder.NativeHolder


object AdmobUtils {
    @JvmField
    var dialog: SweetAlertDialog? = null

    var shimmerFrameLayout: ShimmerFrameLayout? =null

    var timeOut = 0
    @JvmField
    var isShowAds = true
    @JvmField
    var isAdShowing = false
    @JvmField
    var isTesting = false
    var testDevices: MutableList<String> = ArrayList()

    //id thật
    var idIntersitialReal: String? = null
    var isClick = false
    var mInterstitialAd: InterstitialAd? = null

    var dialogFullScreen: Dialog? = null

    @JvmStatic
    fun initAdmob(context: Context?, timeout: Int, isDebug: Boolean, isEnableAds: Boolean) {
        timeOut = timeout
        if (timeOut < 5000 && timeout != 0) {
            Toast.makeText(context, "Nên để limit time ~10000", Toast.LENGTH_LONG).show()
        }
        timeOut = if (timeout > 0) {
            timeout
        } else {
            10000
        }
        isTesting = isDebug
        isShowAds = isEnableAds
        MobileAds.initialize(context!!) { initializationStatus: InitializationStatus? -> }
        initListIdTest()
        /*val requestConfiguration = RequestConfiguration.Builder()
            .setTestDeviceIds(testDevices)
            .build()
        MobileAds.setRequestConfiguration(requestConfiguration)*/
        initAdRequest(timeout)
    }
    var adRequest: AdRequest? = null
    @JvmStatic
    fun initAdRequest(timeOut: Int) {
        adRequest = AdRequest.Builder()
            .setHttpTimeoutMillis(timeOut)
            .build()
    }

    fun initListIdTest() {
        /*testDevices.add("727D4F658B63BDFA0EFB164261AAE54")*/
    }

    @JvmStatic
    fun isNetworkConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo != null && cm.activeNetworkInfo!!.isConnected
    }

    @JvmStatic
    fun loadAndGetNativeAds(
        context: Context,
        nativeHolder: NativeHolder,
        adCallback: NativeCallbackAd
    ) {
        if (!isShowAds || !isNetworkConnected(context)) {
            adCallback.onAdFail("No internet")
            return
        }
        //If native is loaded return
        if (nativeHolder.nativeAd != null) {
            Log.d("===AdsLoadsNative", "Native not null")
            return
        }
        if (isTesting) {
            nativeHolder.ads = context.getString(R.string.test_ads_admob_native_id)
        }
        nativeHolder.isLoad = true
        val adLoader: AdLoader = AdLoader.Builder(context, nativeHolder.ads)
            .forNativeAd { nativeAd ->
                nativeHolder.nativeAd = nativeAd
                nativeHolder.isLoad = false
                nativeHolder.native_mutable.value = nativeAd
                nativeAd.setOnPaidEventListener { adValue: AdValue? -> adCallback.onAdPaid(adValue,nativeHolder.ads) }
                adCallback.onLoadedAndGetNativeAd(nativeAd)
            }.withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e("Admodfail", "onAdFailedToLoad" + adError.message)
                    Log.e("Admodfail", "errorCodeAds" + adError.cause)
                    nativeHolder.nativeAd = null
                    nativeHolder.isLoad = false
                    nativeHolder.native_mutable.value = null
                    adCallback.onAdFail("errorId2_"+adError.message)
                }
            })
            .withNativeAdOptions(NativeAdOptions.Builder().build()).build()
        if (adRequest != null) {
            adLoader.loadAd(adRequest!!)
        }
    }

    @JvmStatic
    fun showNativeAdsWithLayout(
        activity: Activity,
        nativeHolder: NativeHolder,
        viewGroup: ViewGroup,
        layout: Int,
        size: GoogleSizeNative,
        callback: AdsNativeCallBackAdmod
    ) {
        if (!isShowAds || !isNetworkConnected(activity)) {
            viewGroup.visibility = View.GONE
            return
        }
        if (shimmerFrameLayout != null) {
            shimmerFrameLayout?.stopShimmer()
        }
        viewGroup.removeAllViews()
        if (!nativeHolder.isLoad) {
            if (nativeHolder.nativeAd != null) {
                val adView = activity.layoutInflater.inflate(layout, null) as NativeAdView
                populateNativeAdView(nativeHolder.nativeAd!!, adView, GoogleSizeNative.UNIFIED_MEDIUM)
                if (shimmerFrameLayout != null) {
                    shimmerFrameLayout?.stopShimmer()
                }
                nativeHolder.native_mutable.removeObservers((activity as LifecycleOwner))
                viewGroup.removeAllViews()
                viewGroup.addView(adView)
                callback.NativeLoaded()
            } else {
                if (shimmerFrameLayout != null) {
                    shimmerFrameLayout?.stopShimmer()
                }
                nativeHolder.native_mutable.removeObservers((activity as LifecycleOwner))
                callback.NativeFailed("None Show")
            }
        } else {
            val tagView: View = if (size === GoogleSizeNative.UNIFIED_MEDIUM) {
                activity.layoutInflater.inflate(R.layout.layout_native_loading_medium, null, false)
            } else {
                activity.layoutInflater.inflate(R.layout.layout_native_loading_small, null, false)
            }
            viewGroup.addView(tagView, 0)
            if (shimmerFrameLayout == null) shimmerFrameLayout =
                tagView.findViewById(R.id.shimmer_view_container)
            shimmerFrameLayout?.startShimmer()
            nativeHolder.native_mutable.observe((activity as LifecycleOwner)) { nativeAd: NativeAd? ->
                if (nativeAd != null) {
                    nativeAd.setOnPaidEventListener {
                        callback.onPaidNative(it,nativeHolder.ads)
                    }
                    val adView = activity.layoutInflater.inflate(layout, null) as NativeAdView
                    populateNativeAdView(nativeAd, adView, GoogleSizeNative.UNIFIED_MEDIUM)
                    if (shimmerFrameLayout != null) {
                        shimmerFrameLayout?.stopShimmer()
                    }
                    viewGroup.removeAllViews()
                    viewGroup.addView(adView)
                    callback.NativeLoaded()
                    nativeHolder.native_mutable.removeObservers((activity as LifecycleOwner))
                } else {
                    if (shimmerFrameLayout != null) {
                        shimmerFrameLayout?.stopShimmer()
                    }
                    callback.NativeFailed("None Show")
                    nativeHolder.native_mutable.removeObservers((activity as LifecycleOwner))
                }
            }
        }
    }

    @JvmStatic
    fun loadAndShowNativeAdsWithLayout(
        activity: Activity,
        s: String?,
        viewGroup: ViewGroup,
        layout: Int,
        size: GoogleSizeNative,
        adCallback: NativeAdCallback
    ) {
        if (!isShowAds || !isNetworkConnected(activity)) {
            viewGroup.visibility = View.GONE
            return
        }
        var s = s
        val tagView: View = if (size === GoogleSizeNative.UNIFIED_MEDIUM) {
            activity.layoutInflater.inflate(R.layout.layout_native_loading_medium, null, false)
        } else {
            activity.layoutInflater.inflate(R.layout.layout_native_loading_small, null, false)
        }
        viewGroup.addView(tagView, 0)
        val shimmerFrameLayout =
            tagView.findViewById<ShimmerFrameLayout>(R.id.shimmer_view_container)
        shimmerFrameLayout.startShimmer()
        if (isTesting) {
            s = activity.getString(R.string.test_ads_admob_native_id)
        }
        val adLoader: AdLoader = AdLoader.Builder(activity, s!!)
            .forNativeAd { nativeAd ->
                nativeAd.setOnPaidEventListener { adValue: AdValue -> adCallback.onAdPaid(adValue,s) }
                adCallback.onNativeAdLoaded()
                val adView = activity.layoutInflater
                    .inflate(layout, null) as NativeAdView
                populateNativeAdView(nativeAd, adView, GoogleSizeNative.UNIFIED_MEDIUM)
                shimmerFrameLayout.stopShimmer()
                viewGroup.removeAllViews()
                viewGroup.addView(adView)
                //viewGroup.setVisibility(View.VISIBLE);
            }.withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e("Admodfail", "onAdFailedToLoad" + adError.message)
                    Log.e("Admodfail", "errorCodeAds" + adError.cause)
                    shimmerFrameLayout.stopShimmer()
                    viewGroup.removeAllViews()
                    adCallback.onAdFail(adError.message)
                }
                override fun onAdClicked() {
                    super.onAdClicked()

                }
            })
            .withNativeAdOptions(NativeAdOptions.Builder().build()).build()
        if (adRequest != null) {
            adLoader.loadAd(adRequest!!)
        }
        Log.e("Admod", "loadAdNativeAds")
    }

    @JvmStatic
    fun loadAndShowNativeAdsWithLayoutAds(
        activity: Activity,
        nativeHolder: NativeHolder,
        viewGroup: ViewGroup,
        layout: Int,
        size: GoogleSizeNative,
        adCallback: NativeAdCallbackNew
    ) {
        Log.d("===Native", "Native1")
        if (!isShowAds || !isNetworkConnected(activity)) {
            viewGroup.visibility = View.GONE
            return
        }
        viewGroup.removeAllViews()
        var s = nativeHolder.ads
        val tagView: View = if (size === GoogleSizeNative.UNIFIED_MEDIUM) {
            activity.layoutInflater.inflate(R.layout.layout_native_loading_medium, null, false)
        } else {
            activity.layoutInflater.inflate(R.layout.layout_native_loading_small, null, false)
        }
        viewGroup.addView(tagView, 0)
        val shimmerFrameLayout =
            tagView.findViewById<ShimmerFrameLayout>(R.id.shimmer_view_container)
        shimmerFrameLayout.startShimmer()

        if (isTesting) {
            s = activity.getString(R.string.test_ads_admob_native_video_id)
        }
        val adLoader: AdLoader = AdLoader.Builder(activity, s)
            .forNativeAd { nativeAd ->
                adCallback.onNativeAdLoaded()
                val adView = activity.layoutInflater
                    .inflate(layout, null) as NativeAdView
                populateNativeAdView(nativeAd, adView, GoogleSizeNative.UNIFIED_MEDIUM)
                shimmerFrameLayout.stopShimmer()
                viewGroup.removeAllViews()
                viewGroup.addView(adView)
                nativeAd.setOnPaidEventListener { adValue: AdValue ->
                    adCallback.onAdPaid(adValue,s)
                }
                //viewGroup.setVisibility(View.VISIBLE);
            }.withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e("Admodfail", "onAdFailedToLoad" + adError.message)
                    Log.e("Admodfail", "errorCodeAds" + adError.cause)
                    shimmerFrameLayout.stopShimmer()
                    viewGroup.removeAllViews()
                    nativeHolder.isLoad = false
                    adCallback.onAdFail(adError.message)
                }

                override fun onAdClicked() {
                    super.onAdClicked()
                    adCallback.onClickAds()
                }
            })
            .withNativeAdOptions(NativeAdOptions.Builder().build()).build()
        if (adRequest != null) {
            adLoader.loadAd(adRequest!!)
        }
        Log.e("Admod", "loadAdNativeAds")
    }

    @JvmStatic
    fun loadAdBanner(
        activity: Activity,
        bannerId: String?,
        viewGroup: ViewGroup,
        bannerAdCallback: BannerCallBack
    ) {
        var bannerId = bannerId
        if (!isShowAds || !isNetworkConnected(activity)) {
            viewGroup.visibility = View.GONE
            bannerAdCallback.onFailed("None Show")
            return
        }
        val mAdView = AdView(activity)
        if (isTesting) {
            bannerId = activity.getString(R.string.test_ads_admob_banner_id)
        }
        mAdView.adUnitId = bannerId!!
        val adSize = getAdSize(activity)
        mAdView.setAdSize(adSize)
        viewGroup.removeAllViews()
        val tagView = activity.layoutInflater.inflate(R.layout.layout_banner_loading, null, false)
        viewGroup.addView(tagView, 0)
        viewGroup.addView(mAdView, 1)
        shimmerFrameLayout = tagView.findViewById(R.id.shimmer_view_container)
        shimmerFrameLayout?.startShimmer()
        mAdView.onPaidEventListener =
            OnPaidEventListener { adValue -> bannerAdCallback.onPaid(adValue, mAdView) }
        mAdView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                shimmerFrameLayout?.stopShimmer()
                viewGroup.removeView(tagView)
                bannerAdCallback.onLoad()
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.e(" Admod", "failloadbanner" + adError.message)
                shimmerFrameLayout?.stopShimmer()
                viewGroup.removeView(tagView)
                bannerAdCallback.onFailed(adError.message)
            }

            override fun onAdOpened() {}
            override fun onAdClicked() {
                bannerAdCallback.onClickAds()
            }

            override fun onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }
        }
        if (adRequest != null) {
            mAdView.loadAd(adRequest!!)
        }
        Log.e(" Admod", "loadAdBanner")
    }

    @JvmStatic
    fun loadAdBannerCollapsible(
        activity: Activity,
        bannerId: String?,
        collapsibleBannerSize: CollapsibleBanner,
        viewGroup: ViewGroup,
        callback: BannerCollapsibleAdCallback
    ) {
        var bannerId = bannerId
        if (!isShowAds || !isNetworkConnected(activity)) {
            viewGroup.visibility = View.GONE
            return
        }
        val mAdView = AdView(activity)
        if (isTesting) {
            bannerId = activity.getString(R.string.test_ads_admob_banner_id)
        }
        mAdView.adUnitId = bannerId!!
        viewGroup.removeAllViews()
        val adSize = getAdSize(activity)
        mAdView.setAdSize(adSize)

        val tagView = activity.layoutInflater.inflate(R.layout.layout_banner_loading, null, false)
        viewGroup.addView(tagView, 0)
        viewGroup.addView(mAdView, 1)
        shimmerFrameLayout = tagView.findViewById(R.id.shimmer_view_container)
        shimmerFrameLayout?.startShimmer()

        mAdView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                mAdView.onPaidEventListener =
                    OnPaidEventListener { adValue -> callback.onAdPaid(adValue, mAdView) }
                shimmerFrameLayout?.stopShimmer()
                viewGroup.removeView(tagView)
                callback.onBannerAdLoaded(adSize)
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.e(" Admod", "failloadbanner" + adError.message)
                shimmerFrameLayout?.stopShimmer()
                viewGroup.removeView(tagView)
                callback.onAdFail(adError.message)
            }

            override fun onAdOpened() {}
            override fun onAdClicked() {
                callback.onClickAds()
            }

            override fun onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }
        }
        val extras = Bundle()
        var anchored = "top"
        anchored = if (collapsibleBannerSize === CollapsibleBanner.TOP) {
            "top"
        } else {
            "bottom"
        }
        extras.putString("collapsible", anchored)
        val adRequest2 = AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter::class.java, extras).build()
        mAdView.loadAd(adRequest2)
        Log.e(" Admod", "loadAdBanner")
    }

    @JvmStatic
    fun loadAndGetAdInterstitial(
        activity: Context,
        interHolder: InterHolder,
        adLoadCallback: AdCallBackInterLoad
    ) {
        isAdShowing = false
        if (!isShowAds || !isNetworkConnected(activity)) {
            adLoadCallback.onAdFail("None Show")
            return
        }
        if (interHolder.inter != null) {
            Log.d("===AdsInter", "inter not null")
            return
        }
        interHolder.check = true
        if (adRequest == null) {
            initAdRequest(timeOut)
        }
        if (isTesting) {
            interHolder.ads = activity.getString(R.string.test_ads_admob_inter_id)
        }
        idIntersitialReal = interHolder.ads
        InterstitialAd.load(
            activity,
            idIntersitialReal!!,
            adRequest!!,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    if (isClick) {
                        interHolder.mutable.value = interstitialAd
                    }
                    interHolder.inter = interstitialAd
                    interHolder.check = false
                    adLoadCallback.onAdLoaded(interstitialAd, false)
                    Log.i("adLog", "onAdLoaded")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    isAdShowing = false
                    if (mInterstitialAd != null) {
                        mInterstitialAd = null
                    }
                    interHolder.check = false
                    if (isClick) {
                        interHolder.mutable.value = null
                    }
                    adLoadCallback.onAdFail(loadAdError.message)
                }
            })
    }

    @JvmStatic
    fun showAdInterstitialWithCallbackNotLoadNew(
        activity: Activity,
        interHolder: InterHolder,
        timeout: Long,
        adCallback: AdsInterCallBack?,
        enableLoadingDialog: Boolean
    ) {
        isClick = true
        //Check internet
        if (!isShowAds || !isNetworkConnected(activity)) {
            isAdShowing = false
            if (AppOpenManager().getInstance().isInitialized()) {
                AppOpenManager().getInstance().isAppResumeEnabled = true
            }
            adCallback?.onAdFail("No internet")
            return
        }
        adCallback?.onAdLoaded()
        val handler = Handler(Looper.getMainLooper())
        //Check timeout show inter
        val runnable = Runnable {
            if (interHolder.check) {
                if (AppOpenManager().getInstance().isInitialized()) {
                    AppOpenManager().getInstance().isAppResumeEnabled = true
                }
                isClick = false
                interHolder.mutable.removeObservers((activity as LifecycleOwner))
                isAdShowing = false
                dismissAdDialog()
                adCallback?.onAdFail("timeout")
            }
        }
        handler.postDelayed(runnable, timeout)
        //Inter is Loading...
        if (interHolder.check) {
            if (enableLoadingDialog) {
                dialogLoading(activity)
            }
            interHolder.mutable.observe((activity as LifecycleOwner)) { aBoolean: InterstitialAd? ->
                if (aBoolean != null) {
                    interHolder.mutable.removeObservers((activity as LifecycleOwner))
                    isClick = false
                    Handler(Looper.getMainLooper()).postDelayed({
                        Log.d("===DelayLoad", "delay")
                        aBoolean.fullScreenContentCallback = object : FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                isAdShowing = false
                                if (AppOpenManager().getInstance().isInitialized()) {
                                    AppOpenManager().getInstance().isAppResumeEnabled = true
                                }
                                isClick = false
                                //Set inter = null
                                interHolder.inter = null
                                interHolder.mutable.removeObservers((activity as LifecycleOwner))
                                interHolder.mutable.value = null
                                adCallback?.onEventClickAdClosed()
                                dismissAdDialog()
                                Log.d("TAG", "The ad was dismissed.")
                            }

                            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                isAdShowing = false
                                if (AppOpenManager().getInstance().isInitialized()) {
                                    AppOpenManager().getInstance().isAppResumeEnabled = true
                                }
                                isClick = false
                                isAdShowing = false
                                //Set inter = null
                                interHolder.inter = null
                                dismissAdDialog()
                                Log.e("Admodfail", "onAdFailedToLoad" + adError.message)
                                Log.e("Admodfail", "errorCodeAds" + adError.cause)
                                interHolder.mutable.removeObservers((activity as LifecycleOwner))
                                interHolder.mutable.value = null
                                adCallback?.onAdFail(adError.message)
                            }

                            override fun onAdShowedFullScreenContent() {
                                handler.removeCallbacksAndMessages(null)
                                isAdShowing = true
                                adCallback?.onAdShowed()
                                try {
                                    aBoolean.setOnPaidEventListener {
                                            adValue -> adCallback?.onPaid(adValue,interHolder.inter?.adUnitId)
                                    }
                                } catch (e: Exception) {
                                }
                            }
                        }
                        showInterstitialAdNew(activity, aBoolean, adCallback)
                    }, 400)
                }
            }
            return
        }
        //Load inter done
        if (interHolder.inter == null) {
            if (adCallback != null) {
                isAdShowing = false
                if (AppOpenManager().getInstance().isInitialized()) {
                    AppOpenManager().getInstance().isAppResumeEnabled = true
                }
                adCallback.onAdFail("inter null")
            }
        } else {
            if (enableLoadingDialog) {
                dialogLoading(activity)
            }
            Handler(Looper.getMainLooper()).postDelayed({
                interHolder.inter?.fullScreenContentCallback =
                    object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            isAdShowing = false
                            if (AppOpenManager().getInstance().isInitialized()) {
                                AppOpenManager().getInstance().isAppResumeEnabled = true
                            }
                            isClick = false
                            interHolder.mutable.removeObservers((activity as LifecycleOwner))
                            interHolder.inter = null
                            adCallback?.onEventClickAdClosed()
                            dismissAdDialog()
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            isAdShowing = false
                            if (AppOpenManager().getInstance().isInitialized()) {
                                AppOpenManager().getInstance().isAppResumeEnabled = true
                            }
                            isClick = false
                            interHolder.inter = null
                            interHolder.mutable.removeObservers((activity as LifecycleOwner))
                            isAdShowing = false
                            dismissAdDialog()
                            adCallback?.onAdFail(adError.message)
                            Log.e("Admodfail", "onAdFailedToLoad" + adError.message)
                            Log.e("Admodfail", "errorCodeAds" + adError.cause)
                        }

                        override fun onAdShowedFullScreenContent() {
                            handler.removeCallbacksAndMessages(null)
                            isAdShowing = true
                            adCallback?.onAdShowed()
                        }
                    }
                showInterstitialAdNew(activity, interHolder.inter, adCallback)
            }, 400)
        }
    }

    @JvmStatic
    private fun showInterstitialAdNew(
        activity: Activity,
        mInterstitialAd: InterstitialAd?,
        callback: AdsInterCallBack?
    ) {
        if (ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) && mInterstitialAd != null) {
            isAdShowing = true
            Handler(Looper.getMainLooper()).postDelayed({
                callback?.onStartAction()
                mInterstitialAd.setOnPaidEventListener { adValue -> callback?.onPaid(adValue,mInterstitialAd.adUnitId) }
                mInterstitialAd.show(activity)
            },400)
        } else {
            isAdShowing = false
            if (AppOpenManager().getInstance().isInitialized()) {
                AppOpenManager().getInstance().isAppResumeEnabled = true
            }
            dismissAdDialog()
            callback?.onAdFail("onResume")
        }
    }

    @JvmStatic
    fun dismissAdDialog() {
        try {
            if (dialog != null && dialog!!.isShowing) {
                dialog!!.dismiss()
            }
            if (dialogFullScreen != null && dialogFullScreen?.isShowing == true) {
                dialogFullScreen?.dismiss()
            }
        }catch (_: Exception){

        }
    }

    fun dialogLoading(context: Activity) {
        dialogFullScreen = Dialog(context)
        dialogFullScreen?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogFullScreen?.setContentView(R.layout.dialog_full_screen)
        dialogFullScreen?.setCancelable(false)
        dialogFullScreen?.window!!.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        dialogFullScreen?.window!!.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        val img = dialogFullScreen?.findViewById<LottieAnimationView>(R.id.imageView3)
        img?.setAnimation(R.raw.gifloading)
        try {
            if (!context.isFinishing && dialogFullScreen != null && dialogFullScreen?.isShowing == false) {
                dialogFullScreen?.show()
            }
        } catch (ignored: Exception) {
        }

    }


    private fun getAdSize(context: Activity): AdSize {
        // Step 2 - Determine the screen width (less decorations) to use for the ad width.
        val display = context.windowManager.defaultDisplay
        val outMetrics = DisplayMetrics()
        display.getMetrics(outMetrics)
        val widthPixels = outMetrics.widthPixels.toFloat()
        val density = outMetrics.density
        val adWidth = (widthPixels / density).toInt()
        // Step 3 - Get adaptive ad size and return for setting on the ad view.
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth)
    }
    interface NativeAdCallback {
        fun onLoadedAndGetNativeAd(ad: NativeAd?)
        fun onNativeAdLoaded()
        fun onAdFail(error: String?)
        fun onAdPaid(adValue: AdValue?, adUnitAds: String?)
    }
    interface NativeAdCallbackNew{
        fun onLoadedAndGetNativeAd(ad: NativeAd?)
        fun onNativeAdLoaded()
        fun onAdFail(error: String)
        fun onAdPaid(adValue: AdValue?, adUnitAds: String?)
        fun onClickAds()

    }
    interface AdsNativeCallBackAdmod {
        fun NativeLoaded()
        fun NativeFailed(massage : String)
        fun onPaidNative(adValue : AdValue, adUnitAds : String)
    }

    interface BannerCallBack {
        fun onClickAds()
        fun onLoad()
        fun onFailed(message : String)
        fun onPaid(adValue: AdValue?, mAdView: AdView?)
    }

    interface BannerCollapsibleAdCallback {
        fun onClickAds()
        fun onBannerAdLoaded(adSize: AdSize)
        fun onAdFail(message : String)
        fun onAdPaid(adValue: AdValue, mAdView: AdView)
    }

    interface AdCallBackInterLoad {
        fun onAdClosed()
        fun onEventClickAdClosed()
        fun onAdShowed()
        fun onAdLoaded(interstitialAd: InterstitialAd?, isLoading: Boolean)
        fun onAdFail(message: String?)
    }

    interface AdsInterCallBack {
        fun onStartAction()
        fun onEventClickAdClosed()
        fun onAdShowed()
        fun onAdLoaded()
        fun onAdFail(error: String?)
        fun onPaid(adValue: AdValue?, adUnitAds: String?)
    }

}