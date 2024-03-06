package com.lib.hachi.appopenads

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.ads.AdValue

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        AdManager.loadAdsNative(this,AdManager.nativeMain)
        val aoa = AOAManager(this,"",20000,object : AOAManager.AppOpenAdsListener{
            override fun onAdsClose() {
                startActivity(Intent(this@SplashActivity,MainActivity::class.java))
            }

            override fun onAdsFailed(message: String) {
                startActivity(Intent(this@SplashActivity,MainActivity::class.java))
            }

            override fun onAdPaid(adValue: AdValue, adUnitAds: String) {

            }

        })
        aoa.loadAndShowAoA()

    }
}