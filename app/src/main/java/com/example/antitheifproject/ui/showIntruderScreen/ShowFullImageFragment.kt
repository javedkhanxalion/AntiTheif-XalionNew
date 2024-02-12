package com.example.antitheifproject.ui.showIntruderScreen

import android.app.Dialog
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.FileProvider
import androidx.navigation.fragment.findNavController
import com.antitheftalarm.dont.touch.phone.finder.R
import com.antitheftalarm.dont.touch.phone.finder.databinding.FragmentShowFullImageScreenBinding
import com.bumptech.glide.Glide
import com.example.antitheifproject.ads_manager.AdsManager
import com.example.antitheifproject.ads_manager.interfaces.NativeListener
import com.example.antitheifproject.ads_manager.showTwoInterAd
import com.example.antitheifproject.model.IntruderModels
import com.example.antitheifproject.utilities.BaseFragment
import com.example.antitheifproject.utilities.firebaseAnalytics
import com.example.antitheifproject.utilities.id_inter_main_medium
import com.example.antitheifproject.utilities.id_inter_main_normal
import com.example.antitheifproject.utilities.id_native_show_image_screen
import com.example.antitheifproject.utilities.isBackShow
import com.example.antitheifproject.utilities.setupBackPressedCallback
import com.example.antitheifproject.utilities.showToast
import com.example.antitheifproject.utilities.val_ad_instertital_show_image_screen_is_B
import com.example.antitheifproject.utilities.val_ad_native_show_image_screen
import com.example.antitheifproject.utilities.val_ad_native_show_image_screen_is_H
import com.example.antitheifproject.utilities.val_inter_main_medium
import com.example.antitheifproject.utilities.val_inter_main_normal
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView

class ShowFullImageFragment :
    BaseFragment<FragmentShowFullImageScreenBinding>(FragmentShowFullImageScreenBinding::inflate) {

    var models: IntruderModels? = null
    private var adsManager: AdsManager? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAnalytics("show_image_fragment_load", "show_image_fragment_load -->  Click")

        adsManager = AdsManager.appAdsInit(activity ?: return)
        arguments?.let {
            models = it.getSerializable("obj") as IntruderModels
        }

        if (isBackShow) {
            adsManager?.let {
                showTwoInterAd(
                    ads = it,
                    activity = activity ?: return@let,
                    remoteConfigMedium = val_inter_main_medium,
                    remoteConfigNormal = val_inter_main_normal,
                    adIdMedium = id_inter_main_medium,
                    adIdNormal = id_inter_main_normal,
                    tagClass = "show_intruder_fragment_show",
                    isBackPress = true,
                    layout = binding?.adsLay!!
                ) {
                }
            }
        }
        Glide.with(this).load(Uri.fromFile(models?.file)).into(binding?.intruderimage!!)
        binding?.deleteBtn?.setOnClickListener {
            val dialog = Dialog(context ?: return@setOnClickListener)
            val inflate1 = layoutInflater.inflate(R.layout.delete_dialog, null as ViewGroup?)
            dialog.setContentView(inflate1)
            val window = dialog.window
            window?.attributes = window?.attributes
            dialog.window?.setLayout(-2, -2)
            dialog.window?.setBackgroundDrawable(ColorDrawable(0))
            dialog.setCanceledOnTouchOutside(true)
            dialog.show()
            (inflate1.findViewById<View>(R.id.cancl_btn) as Button).setOnClickListener { dialog.dismiss() }
            (inflate1.findViewById<View>(R.id.cnfrm_del_btn) as Button).setOnClickListener { view2 ->
                dialogDismiss(
                    dialog, view2
                )
            }
        }

        binding?.shareBtn?.setOnClickListener { shareImage() }
        binding?.backicon?.setOnClickListener {
            isBackShow = val_ad_instertital_show_image_screen_is_B
            findNavController().navigateUp()
        }
        setupBackPressedCallback {

            isBackShow = val_ad_instertital_show_image_screen_is_B
            findNavController().navigateUp()
        }
        loadNative()
    }

    private fun dialogDismiss(dialog: Dialog, view: View?) {
        dialog.dismiss()
        delPics()
    }

    private fun shareImage() {

        try {
            if (models?.file?.exists() == true) {
                val intent = Intent("android.intent.action.SEND")
                intent.type = "image/*"
                val uriForFile = FileProvider.getUriForFile(
                    context ?: return,
                    context?.applicationContext?.packageName + ".fileprovider",
                    models?.file ?: return
                )
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                intent.putExtra("android.intent.extra.STREAM", uriForFile)
                if (intent.resolveActivity(context?.packageManager ?: return) != null) {
                    startActivity(Intent.createChooser(intent, "Share via"))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun delPics() {
        if (models?.file?.exists() == true && models?.file?.delete() == true) {
            showToast(getString(R.string.image_deleted))
            findNavController().popBackStack()
        }
    }

    private fun loadNative() {
        adsManager?.nativeAds()?.loadNativeAd(
            activity ?: return,
            val_ad_native_show_image_screen,
            id_native_show_image_screen,
            object : NativeListener {
                override fun nativeAdLoaded(currentNativeAd: NativeAd?) {
                    if (!isAdded && !isVisible && isDetached) {
                        _binding?.nativeExitAd?.visibility = View.GONE
                        _binding?.adView?.visibility = View.GONE
                        return
                    }
                    _binding?.nativeExitAd?.visibility = View.VISIBLE
                    _binding?.adView?.visibility = View.GONE

                    val adView = if (val_ad_native_show_image_screen_is_H) {
                        layoutInflater.inflate(R.layout.ad_unified_hight, null) as NativeAdView
                    } else {
                        layoutInflater.inflate(R.layout.ad_unified_low, null) as NativeAdView
                    }
                    adsManager?.nativeAds()?.nativeViewPolicy(currentNativeAd ?: return, adView)
                    if (isAdded && isVisible && !isDetached) {
                        _binding?.nativeExitAd?.removeAllViews()
                        _binding?.nativeExitAd?.addView(adView)
                    }
                    super.nativeAdLoaded(currentNativeAd)
                }

                override fun nativeAdFailed(loadAdError: LoadAdError) {
                    _binding?.nativeExitAd?.visibility = View.GONE
                    _binding?.adView?.visibility = View.GONE
                    super.nativeAdFailed(loadAdError)
                }

                override fun nativeAdValidate(string: String) {
                    _binding?.nativeExitAd?.visibility = View.GONE
                    _binding?.adView?.visibility = View.GONE
                    super.nativeAdValidate(string)
                }
            })
    }


}