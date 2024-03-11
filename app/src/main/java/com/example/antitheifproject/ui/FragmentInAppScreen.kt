package com.example.antitheifproject.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.antitheftalarm.dont.touch.phone.finder.BuildConfig
import com.antitheftalarm.dont.touch.phone.finder.R
import com.antitheftalarm.dont.touch.phone.finder.databinding.InAppDialogFirstBinding
import com.example.antitheifproject.ads_manager.billing.PurchasePrefs
import com.example.antitheifproject.utilities.BaseFragment
import com.example.antitheifproject.utilities.clickWithThrottle
import com.example.antitheifproject.utilities.isShowInApp
import com.example.antitheifproject.utilities.monthly_price
import com.example.antitheifproject.utilities.setupBackPressedCallback
import com.example.antitheifproject.utilities.yearly_price
import com.hypersoft.billing.BillingManager
import com.hypersoft.billing.dataClasses.ProductType
import com.hypersoft.billing.dataClasses.PurchaseDetail
import com.hypersoft.billing.interfaces.BillingListener
import com.hypersoft.billing.interfaces.OnPurchaseListener

class FragmentInAppScreen :
    BaseFragment<InAppDialogFirstBinding>(InAppDialogFirstBinding::inflate) {

    var billingManager: BillingManager? = null
    var planId: String? = null
    var isSplashFrom: Boolean? = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            isSplashFrom = it.getBoolean("Is_From_Splash")
        }
        isShowInApp = false
        updateUI(
            binding?.yearlyCheck!!,
            binding?.yearlyButton!!,
            binding?.monthlyCheck!!,
            binding?.monthlyButton!!
        )
        planId = "gold-plan-yearly"
        billingManager = BillingManager(context ?: return)
        val subsProductIdList =
            listOf("gold_product")
        val inAppProductIdList = when (BuildConfig.DEBUG) {
            true -> listOf(billingManager?.getDebugProductIDList())
            false -> listOf("inapp_product_id_1", "inapp_product_id_2")
        }
        billingManager?.initialize(
            productInAppPurchases = inAppProductIdList as List<String>,
            productSubscriptions = subsProductIdList,
            billingListener = object : BillingListener {
                override fun onConnectionResult(isSuccess: Boolean, message: String) {
                    Log.d(
                        "in_app_TAG",
                        "Billing: initBilling: onConnectionResult: isSuccess = $isSuccess - message = $message"
                    )
                    if (!isSuccess) {
//                        proceedApp()
                    }
                }

                override fun purchasesResult(purchaseDetailList: List<PurchaseDetail>) {
                    if (purchaseDetailList.isEmpty()) {
                        // No purchase found, reset all sharedPreferences (premium properties)
                    }
                    purchaseDetailList.forEachIndexed { index, purchaseDetail ->
                        Log.d(
                            "in_app_TAG",
                            "Billing: initBilling: purchasesResult: $index) $purchaseDetail "
                        )
                    }
//                    proceedApp()
                }
            }
        )
        billingManager?.productDetailsLiveData?.observe(viewLifecycleOwner) { productDetailList ->
            Log.d("in_app_TAG", "Billing: initObservers: $productDetailList")

            productDetailList.forEach { productDetail ->
                if (productDetail.productType == ProductType.subs) {
                    if (productDetail.productId == "gold_product" && productDetail.planId == "gold-plan-monthly") {
                        // productDetail (monthly)
                        _binding?.monthlyText?.text = "${productDetail.price}"
                        productDetail.freeTrialDays = 3
                    } else if (productDetail.productId == "subs_product_id_2" && productDetail.planId == "subs_plan_id_2") {
                        // productDetail (3 months)
                    } else if (productDetail.productId == "gold_product" && productDetail.planId == "gold-plan-yearly") {
                        // productDetail (yearly)
                        _binding?.yearlyText?.text = "${productDetail.currencyCode} 0.0"
                        val originalString = getString(R.string.annual_free_trail_second)
                        originalString.replace("12.0", productDetail.price)
                        _binding?.yearlyTextTDetail?.text = originalString
                        productDetail.freeTrialDays = 3
                    }
                }

            }
        }

        setupBackPressedCallback {

            if(isSplashFrom == true){
                findNavController().navigate(R.id.myMainMenuFragment)
            }else{
                findNavController().navigateUp()
            }
        }
        _binding?.premiumButton?.clickWithThrottle {
            billingManager?.makeSubPurchase(
                activity,
                "gold_product",
                planId?:return@clickWithThrottle,
                object : OnPurchaseListener {
                    override fun onPurchaseResult(isPurchaseSuccess: Boolean, message: String) {
                        Log.d("in_app_TAG", "makeSubPurchase: $isPurchaseSuccess - $message")
                        if(isPurchaseSuccess){
                            PurchasePrefs(context).putBoolean("inApp", true)
                        }
                    }
                })
        }
        _binding?.closeIcon?.clickWithThrottle {
            if(isSplashFrom == true){
                findNavController().navigate(R.id.myMainMenuFragment)
            }else{
                findNavController().navigateUp()
            }
        }
        _binding?.closeTop?.clickWithThrottle {
            if(isSplashFrom == true){
                findNavController().navigate(R.id.myMainMenuFragment)
            }else{
                findNavController().navigateUp()
            }
        }
        _binding?.monthlyButton?.clickWithThrottle {
            planId="gold-plan-monthly"
            updateUI(
                binding?.monthlyCheck!!,
                binding?.monthlyButton!!,
                binding?.yearlyCheck!!,
                binding?.yearlyButton!!
            )
        }
        _binding?.yearlyButton?.clickWithThrottle {
            planId="gold-plan-yearly"
            updateUI(
                binding?.yearlyCheck!!,
                binding?.yearlyButton!!,
                binding?.monthlyCheck!!,
                binding?.monthlyButton!!
            )
        }

    }

    fun updateUI(image: View, layout: View, image1: View, layout1: View) {
        image.setBackgroundResource(R.drawable.in_app_ss)
        layout.setBackgroundResource(R.drawable.premium_button_selected)
        image1.setBackgroundResource(R.drawable.in_app_un)
        layout1.setBackgroundResource(R.drawable.premium_button_unselected)
    }
}