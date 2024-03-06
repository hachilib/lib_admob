package com.lib.hachi.appopenads

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.nativead.NativeAd
import com.lib.hachi.appopenads.callback.interfaces.NativeCallbackAd
import com.lib.hachi.appopenads.enumclass.CollapsibleBanner
import com.lib.hachi.appopenads.enumclass.GoogleSizeNative
import com.lib.hachi.appopenads.model.holder.BannerHolder
import com.lib.hachi.appopenads.model.holder.InterHolder
import com.lib.hachi.appopenads.model.holder.NativeHolder

object AdManager {

    val nativeMain = NativeHolder("")
    val bannerMain = BannerHolder("")
    val interManin = InterHolder("")

    var count = 1
    var timeCount = 0L
    var time_for_inter = 20000

    @JvmStatic
    fun loadAdsNative(context: Context, holder: NativeHolder) {
        AdmobUtils.loadAndGetNativeAds(
            context,
            holder,
            object :  NativeCallbackAd{
                override fun onLoadedAndGetNativeAd(ad: NativeAd?) {
                }
                override fun onNativeAdLoaded() {
                }
                override fun onAdFail(error: String?) {

                }
                override fun onAdPaid(adValue: AdValue?, adUnitAds: String?) {
                }
            })
    }

    @JvmStatic
    fun showNative(activity: Activity, viewGroup: ViewGroup, holder: NativeHolder) {
        if (!AdmobUtils.isNetworkConnected(activity)) {
            viewGroup.visibility = View.GONE
            return
        }
        AdmobUtils.loadAndShowNativeAdsWithLayout(
            activity,
            holder.ads,
            viewGroup,
            R.layout.ad_template_medium,
            GoogleSizeNative.UNIFIED_SMALL,
            object : AdmobUtils.NativeAdCallback{
                override fun onLoadedAndGetNativeAd(ad: NativeAd?) {

                }

                override fun onNativeAdLoaded() {
                    viewGroup.visibility = View.VISIBLE
                }

                override fun onAdFail(error: String?) {
                    loadAdsNative(activity,holder)
                    viewGroup.visibility = View.GONE
                }

                override fun onAdPaid(adValue: AdValue?, adUnitAds: String?) {

                }
            }
            )
    }

    fun loadAndShowNative(activity: Activity, nativeAdContainer: ViewGroup, nativeHolder: NativeHolder){
        if (!AdmobUtils.isNetworkConnected(activity)) {
            nativeAdContainer.visibility = View.GONE
            return
        }
        AdmobUtils.loadAndShowNativeAdsWithLayout(activity,nativeHolder.ads, nativeAdContainer,R.layout.ad_template_medium,GoogleSizeNative.UNIFIED_MEDIUM,object : AdmobUtils.NativeAdCallback{
            override fun onLoadedAndGetNativeAd(ad: NativeAd?) {
            }

            override fun onNativeAdLoaded() {
                Log.d("===nativeload","true")
                nativeAdContainer.visibility = View.VISIBLE
            }

            override fun onAdFail(error: String?) {
                nativeAdContainer.visibility = View.GONE
            }

            override fun onAdPaid(adValue: AdValue?, adUnitAds: String?) {
                Log.d("===AdValue","Native: ${adValue?.currencyCode}|${adValue?.valueMicros}")
            }
        })
    }

    @JvmStatic
    fun showAdBanner(activity: Activity, bannerHolder: BannerHolder, view: ViewGroup, line: View) {
        if (AdmobUtils.isNetworkConnected(activity)) {
            AdmobUtils.loadAdBanner(activity, bannerHolder.ads, view, object :
                AdmobUtils.BannerCallBack {
                override fun onClickAds() {

                }

                override fun onLoad() {
                    view.visibility = View.VISIBLE
                    line.visibility = View.VISIBLE
                }

                override fun onFailed(message: String) {
                    view.visibility = View.GONE
                    line.visibility = View.GONE
                }

                override fun onPaid(adValue: AdValue?, mAdView: AdView?) {
                    Log.d("===AdValue","Banner: ${adValue?.currencyCode}|${adValue?.valueMicros}")
                }
            })
        } else {
            view.visibility = View.GONE
            line.visibility = View.GONE
        }
    }

    @JvmStatic
    fun showAdBannerCollapsible(activity: Activity, bannerHolder: BannerHolder, view: ViewGroup, line: View) {
        if (AdmobUtils.isNetworkConnected(activity)) {
            AdmobUtils.loadAdBannerCollapsible(
                activity,
                bannerHolder.ads,
                CollapsibleBanner.BOTTOM,
                view,
                object : AdmobUtils.BannerCollapsibleAdCallback {
                    override fun onClickAds() {

                    }

                    override fun onBannerAdLoaded(adSize: AdSize) {
                        view.visibility = View.VISIBLE
                        line.visibility = View.VISIBLE
                        val params: ViewGroup.LayoutParams = view.layoutParams
                        params.height = adSize.getHeightInPixels(activity)
                        view.layoutParams = params
                    }

                    override fun onAdFail(message: String) {
                        view.visibility = View.GONE
                        line.visibility = View.GONE
                    }

                    override fun onAdPaid(adValue: AdValue, mAdView: AdView) {
                        Log.d("===AdValue","Banner: ${adValue.currencyCode}|${adValue.valueMicros}")
                    }
                })
        } else {
            view.visibility = View.GONE
            line.visibility = View.GONE
        }
    }

    @JvmStatic
    fun loadInter(context: Context, interHolder: InterHolder) {
        AdmobUtils.loadAndGetAdInterstitial(context, interHolder,
            object : AdmobUtils.AdCallBackInterLoad {
                override fun onAdClosed() {
                }

                override fun onEventClickAdClosed() {
                }

                override fun onAdShowed() {
                }

                override fun onAdLoaded(interstitialAd: com.google.android.gms.ads.interstitial.InterstitialAd?, isLoading: Boolean) {

                }

                override fun onAdFail(message: String?) {

                }
            }
        )
    }

    interface AdListener {
        fun onAdClosedOrFailed()
    }

    @JvmStatic
    fun showInter(
        context: Context,
        interHolder: InterHolder,
        adListener: AdListener, event: String, type: Int,
        enableLoadingDialog: Boolean = true
    ) {
        when (type) {
            1 -> {
                if (count % 2 != 0) {
                    count++
                    adListener.onAdClosedOrFailed()
                    return
                } else {
                    count++
                }
            }
        }
        logEventFirebase(context, event + "_load")
        AdmobUtils.showAdInterstitialWithCallbackNotLoadNew(
            context as Activity, interHolder, 10000,
            object : AdmobUtils.AdsInterCallBack {
                override fun onAdLoaded() {
                }

                override fun onAdFail(error: String?) {
                    logEventFirebase(context, event + "_fail")
                    loadInter(context, interHolder)
                    adListener.onAdClosedOrFailed()
                }

                override fun onPaid(adValue: AdValue?, adUnitAds: String?) {

                }

                override fun onStartAction() {
                    adListener.onAdClosedOrFailed()
                }

                override fun onEventClickAdClosed() {
                    timeCount = System.currentTimeMillis()
                    logEventFirebase(context, event + "_close")
                    loadInter(context, interHolder)
                }

                override fun onAdShowed() {
                    Handler().postDelayed({
                        try {
                            AdmobUtils.dismissAdDialog()
                        } catch (_: Exception) {

                        }
                    }, 800)
                    logEventFirebase(context, event + "_show")
                }
            }, enableLoadingDialog
        )
    }

    @JvmStatic
    fun logEventFirebase(context: Context, eventName: String?) {
        /*val firebaseAnalytics = FirebaseAnalytics.getInstance(context)
        val bundle = Bundle()
        bundle.putString("onEvent", context.javaClass.simpleName)
        firebaseAnalytics.logEvent(eventName + "_" + BuildConfig.VERSION_CODE, bundle)
        Log.d("===Event", eventName + "_" + BuildConfig.VERSION_CODE)*/
    }
}