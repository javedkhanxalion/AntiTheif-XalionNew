package com.example.antitheifproject.ads_manager

import android.app.Activity
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import com.antitheftalarm.dont.touch.phone.finder.BuildConfig
import com.antitheftalarm.dont.touch.phone.finder.R
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdOptions.ADCHOICES_TOP_RIGHT
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.material.snackbar.Snackbar
import com.example.antitheifproject.ads_manager.billing.BillingUtil
import com.example.antitheifproject.ads_manager.interfaces.NativeListener

object NativeAdsSplash {

    private const val NativeAdsSplashLog = "splash_native_log"
    private const val NativeAdsSplashID = "ca-app-pub-3940256099942544/2247696110"

    private var nativeAds: NativeAdsSplash? = null

    private var isNativeLoading: Boolean = false
    private var nativeId: String? = null
    var currentNativeAd: NativeAd? = null


    private var isNativeLoading2nd: Boolean = false
    private var nativeId2nd: String? = null
    var currentNativeAd2nd: NativeAd? = null


    fun nativeAds(): NativeAdsSplash {
        if (nativeAds == null) {
            nativeAds = NativeAdsSplash
            Log.d("adsInit", "   NativeClass")
        }
        return nativeAds as NativeAdsSplash
    }


    fun loadNativeAd(
        activity: Activity,
        addConfig: Boolean,
        nativeAdId: String,
        nativeListener: NativeListener
    ) {
        try {
            Log.d(
                NativeAdsSplashLog,
                "validate ${!BillingUtil(activity).checkPurchased(activity)}    $addConfig"
            )

            if (AdsManager.isNetworkAvailable(activity) && !BillingUtil(activity).checkPurchased(
                    activity
                ) && addConfig
            ) {
                nativeId = nativeAdId
                val builder = AdLoader.Builder(
                    activity,
                    if (isDebug()) NativeAdsSplashID else nativeId ?: NativeAdsSplashID
                )
                if (isNativeLoading) {
                    Log.d(NativeAdsSplashLog, "Already loading Ad")
                    loadedShoeNative(activity, nativeListener, builder, nativeAdId)
                    return
                }
                if (currentNativeAd != null) {
                    nativeListener.nativeAdLoaded(currentNativeAd)
                    Log.d(NativeAdsSplashLog, "   Having loaded Ad")
                    builder.forNativeAd { nativeAd ->
                        if (currentNativeAd != null) {
                            currentNativeAd?.destroy()
                        }
                        isNativeLoading = false
                        currentNativeAd = nativeAd
                        Log.d(NativeAdsSplashLog, "   loaded native Ad")
                        nativeListener.nativeAdLoaded(currentNativeAd)
                    }
                    return
                }
                isNativeLoading = true


                builder.forNativeAd { nativeAd ->
                    if (currentNativeAd != null) {
                        currentNativeAd?.destroy()
                    }
                    isNativeLoading = false
                    currentNativeAd = nativeAd
                    Log.d(NativeAdsSplashLog, "   loaded native Ad")
                    nativeListener.nativeAdLoaded(currentNativeAd)
                }

                val videoOptions = VideoOptions.Builder().setStartMuted(true).build()

                val adOptions = NativeAdOptions.Builder().setVideoOptions(videoOptions)
                    .setAdChoicesPlacement(ADCHOICES_TOP_RIGHT).build()
                builder.withNativeAdOptions(adOptions)

                val adLoader = builder.withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        FullScreenAds.logEventForAds(NativeAdsSplashLog, "failed", nativeAdId)

                        if (isDebug()) {
                            Snackbar.make(
                                activity.window.decorView.rootView,
                                "AD Error Native: ${loadAdError.message}",
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                        Log.d(NativeAdsSplashLog, "failed native Ad  ${loadAdError.message}")
                        isNativeLoading = false
                        nativeListener.nativeAdFailed(loadAdError)

                    }

                    override fun onAdImpression() {
                        currentNativeAd = null
                        isNativeLoading = false
                        Log.d(NativeAdsSplashLog, "onAdImpression native Ad")
                        super.onAdImpression()
                    }

                    override fun onAdClicked() {
                        Log.d(NativeAdsSplashLog, "onAdClicked native Ad")
                        FullScreenAds.logEventForAds(NativeAdsSplashLog, "clicked", nativeAdId)
                        isNativeLoading = false
                        nativeListener.nativeAdClicked()
                        super.onAdClicked()
                    }

                    override fun onAdLoaded() {
                        isNativeLoading = false
                        FullScreenAds.logEventForAds(NativeAdsSplashLog, "loaded", nativeAdId)

                        Log.d(NativeAdsSplashLog, "onAdLoaded native Ad")
                        super.onAdLoaded()
                    }
                }).build()

                adLoader.loadAd(AdRequest.Builder().build())
            } else {
                nativeListener.nativeAdValidate("hideAll")
                if (isDebug()) {
                    Log.d(NativeAdsSplashLog, "config : $addConfig")
                    Snackbar.make(
                        activity.window.decorView.rootView,
                        activity.getString(R.string.check_ads),
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadedShoeNative(
        activity: Activity,
        nativeListener: NativeListener,
        builder: AdLoader.Builder,
        nativeAdId: String
    ) {
        nativeListener.nativeAdLoaded(currentNativeAd)
        Log.d(NativeAdsSplashLog, "   Having loaded Ad")
        builder.forNativeAd { nativeAd ->
            if (currentNativeAd != null) {
                currentNativeAd?.destroy()
            }
            isNativeLoading = false
            currentNativeAd = nativeAd
            Log.d(NativeAdsSplashLog, "   loaded native Ad")
            nativeListener.nativeAdLoaded(currentNativeAd)
        }

        val videoOptions = VideoOptions.Builder().setStartMuted(true).build()

        val adOptions = NativeAdOptions.Builder().setVideoOptions(videoOptions)
            .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT).build()
        builder.withNativeAdOptions(adOptions)

        val adLoader = builder.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                FullScreenAds.logEventForAds(NativeAdsSplashLog, "failed", nativeAdId)

                if (isDebug()) {
                    Snackbar.make(
                        activity.window.decorView.rootView,
                        "AD Error Native: ${loadAdError.message}",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
                Log.d(NativeAdsSplashLog, "failed native Ad  ${loadAdError.message}")
                isNativeLoading = false
                nativeListener.nativeAdFailed(loadAdError)

            }

            override fun onAdImpression() {
                currentNativeAd = null
                isNativeLoading = false
                Log.d(NativeAdsSplashLog, "onAdImpression native Ad")
                super.onAdImpression()
            }

            override fun onAdClicked() {
                Log.d(NativeAdsSplashLog, "onAdClicked native Ad")
                FullScreenAds.logEventForAds(NativeAdsSplashLog, "clicked", nativeAdId)
                isNativeLoading = false
                nativeListener.nativeAdClicked()
                super.onAdClicked()
            }

            override fun onAdLoaded() {
                isNativeLoading = false
                FullScreenAds.logEventForAds(NativeAdsSplashLog, "loaded", nativeAdId)

                Log.d(NativeAdsSplashLog, "onAdLoaded native Ad")
                super.onAdLoaded()
            }
        }).build()

        adLoader.loadAd(AdRequest.Builder().build())
        return
    }


    fun isDebug(): Boolean {
        return BuildConfig.BUILD_TYPE == "debug"
    }

    fun nativeViewPolicy(nativeAd: NativeAd, adView: NativeAdView) {

        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
        adView.iconView = adView.findViewById(R.id.ad_app_icon)
        adView.headlineView = adView.findViewById(R.id.ad_headline)
//        adView.advertiserView = adView.findViewById(R.id.ad_advertiser)
        adView.starRatingView = adView.findViewById(R.id.ad_stars)

        (adView.headlineView as TextView).text = nativeAd.headline

        if (nativeAd.starRating == null) {
            adView.starRatingView?.visibility = View.INVISIBLE
        } else {
            (adView.starRatingView as RatingBar).rating = nativeAd.starRating?.toFloat() ?: 0f
            adView.starRatingView?.visibility = View.VISIBLE
        }

        if (nativeAd.callToAction == null) {
            adView.callToActionView?.visibility = View.INVISIBLE
        } else {
            adView.callToActionView?.visibility = View.VISIBLE
            (adView.callToActionView as TextView).text = nativeAd.callToAction
        }

        if (nativeAd.icon == null) {
            adView.iconView?.visibility = View.INVISIBLE
        } else {
            (adView.iconView as ImageView).setImageDrawable(
                nativeAd.icon?.drawable
            )
            adView.iconView?.visibility = View.VISIBLE
        }
//
//        if (nativeAd.advertiser == null) {
//            adView.advertiserView?.visibility = View.INVISIBLE
//        } else {
//            (adView.advertiserView as TextView).text = nativeAd.advertiser
//            adView.advertiserView?.visibility = View.VISIBLE
//        }

        adView.setNativeAd(nativeAd)

    }

    fun nativeViewMedia(nativeAd: NativeAd, adView: NativeAdView) {
        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
        adView.iconView = adView.findViewById(R.id.ad_app_icon) as ImageView
        adView.headlineView = adView.findViewById(R.id.ad_headline)
        adView.bodyView = adView.findViewById(R.id.ad_body)
        adView.advertiserView = adView.findViewById(R.id.ad_advertiser)
        adView.starRatingView = adView.findViewById(R.id.ad_stars)
        adView.mediaView = adView.findViewById(R.id.ad_media)


        adView.mediaView?.mediaContent = (nativeAd.mediaContent ?: return)
        (adView.headlineView as TextView).text = nativeAd.headline

        if (nativeAd.starRating == null) {
            adView.starRatingView?.visibility = View.GONE
        } else {
            (adView.starRatingView as RatingBar).rating = nativeAd.starRating?.toFloat() ?: 0f
            adView.starRatingView?.visibility = View.GONE
        }

        if (nativeAd.body == null) {
            adView.bodyView?.visibility = View.INVISIBLE
        } else {
            adView.bodyView?.visibility = View.VISIBLE
            (adView.bodyView as TextView).text = nativeAd.body
        }
        if (nativeAd.callToAction == null) {
            adView.callToActionView?.visibility = View.INVISIBLE
        } else {
            adView.callToActionView?.visibility = View.VISIBLE
            (adView.callToActionView as TextView).text = nativeAd.callToAction
        }

        if (nativeAd.icon == null) {
            adView.iconView?.visibility = View.GONE
        } else {
            (adView.iconView as ImageView).setImageDrawable(
                nativeAd.icon?.drawable
            )
            adView.iconView?.visibility = View.GONE
        }

        if (nativeAd.advertiser == null) {
            adView.advertiserView?.visibility = View.INVISIBLE
        } else {
            (adView.advertiserView as TextView).text = nativeAd.advertiser
            adView.advertiserView?.visibility = View.VISIBLE
        }

        adView.setNativeAd(nativeAd)

    }


}