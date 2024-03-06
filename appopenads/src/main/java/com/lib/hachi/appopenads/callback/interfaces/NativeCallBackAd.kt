package com.lib.hachi.appopenads.callback.interfaces

import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.nativead.NativeAd

interface NativeCallbackAd {
    fun onLoadedAndGetNativeAd(ad: NativeAd?)
    fun onNativeAdLoaded()
    fun onAdFail(error: String?)
    fun onAdPaid(adValue: AdValue?, adUnitAds: String?)
}