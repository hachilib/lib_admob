package com.lib.hachi.appopenads

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.google.android.gms.ads.MobileAds

class MyApplication : Application(),Application.ActivityLifecycleCallbacks {

    override fun onCreate() {
        super.onCreate()
        AdmobUtils.initAdmob(this,10000,isDebug = true,isEnableAds = true)
        AppOpenManager().getInstance()!!.init(this,"")
        AppOpenManager().getInstance().disableAppResumeWithActivity(SplashActivity::class.java)
        registerActivityLifecycleCallbacks(this)
    }

    override fun onActivityCreated(p0: Activity, p1: Bundle?) {}

    override fun onActivityStarted(p0: Activity) {}

    override fun onActivityResumed(p0: Activity) {}

    override fun onActivityPaused(p0: Activity) {}

    override fun onActivityStopped(p0: Activity) {}

    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {}

    override fun onActivityDestroyed(p0: Activity) {}
}