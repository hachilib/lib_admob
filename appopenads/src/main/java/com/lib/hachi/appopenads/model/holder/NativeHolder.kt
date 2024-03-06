package com.lib.hachi.appopenads.model.holder

import androidx.lifecycle.MutableLiveData
import com.google.android.gms.ads.nativead.NativeAd

open class NativeHolder(var ads: String) {
    var nativeAd : NativeAd?= null
    var isLoad = false
    var native_mutable: MutableLiveData<NativeAd> = MutableLiveData()
}