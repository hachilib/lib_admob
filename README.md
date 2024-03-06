# Document

## JitPack
> Step 1. Add the JitPack repository to your build file
```gradle
dependencyResolutionManagement {
		repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
		repositories {
			mavenCentral()
			maven { url 'https://jitpack.io' }
		}
	}
```
> Step 2. Add the dependency
```gradle
dependencies {
	        implementation 'com.github.hachilib:lib_admob:Tag'
	}
```

## Admob
> Confix manifest
```gradle
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="com.google.android.gms.permission.AD_ID" />
```
```gradle
 <meta-data
            android:name="com.google.android.gms.ads.APPLICATION_ID"
            android:value="ca-app-pub-3940256099942544~3347511713" />
```
> Dependencies google ads
```gradle
implementation ("com.google.android.gms:play-services-ads:22.4.0")
```

> AppOpenAd(AOA)
```gradle
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
```
> Load Native
```gradle
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
```
