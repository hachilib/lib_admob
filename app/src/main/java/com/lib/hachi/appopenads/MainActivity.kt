package com.lib.hachi.appopenads

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.ads.AdValue
import com.lib.hachi.appopenads.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AdManager.showAdBanner(this,AdManager.bannerMain,binding.banner1,binding.line1)
        AdManager.showAdBannerCollapsible(this,AdManager.bannerMain,binding.banner,binding.line)

        AdManager.showNative(this,binding.nativeAd,AdManager.nativeMain)
        AdManager.loadAndShowNative(this,binding.laoadAdShownative,AdManager.nativeMain)

        binding.btnInter.setOnClickListener {
            AdManager.showInter(this,AdManager.interManin,object : AdManager.AdListener{
                override fun onAdClosedOrFailed() {
                    startActivity(Intent(this@MainActivity,SplashActivity::class.java))
                }
            },"",1,true)
        }

    }
}