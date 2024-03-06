package com.example.antitheifproject.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.navigation.fragment.findNavController
import com.antitheftalarm.dont.touch.phone.finder.BuildConfig
import com.antitheftalarm.dont.touch.phone.finder.R
import com.antitheftalarm.dont.touch.phone.finder.databinding.InAppDialogFirstBinding
import com.example.antitheifproject.ads_manager.billing.PurchasePrefs
import com.example.antitheifproject.utilities.BaseFragment
import com.example.antitheifproject.utilities.clickWithThrottle
import com.example.antitheifproject.utilities.isShowInApp
import com.example.antitheifproject.utilities.monthly_price
import com.example.antitheifproject.utilities.yearly_price
import com.hypersoft.billing.BillingManager
import com.hypersoft.billing.dataClasses.ProductType
import com.hypersoft.billing.dataClasses.PurchaseDetail
import com.hypersoft.billing.interfaces.BillingListener
import com.hypersoft.billing.interfaces.OnPurchaseListener

class FragmentInAppScreen :
    BaseFragment<InAppDialogFirstBinding>(InAppDialogFirstBinding::inflate) {

    var billingManager: BillingManager? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isShowInApp = false
        updateUI(
            binding?.yearlyCheck!!,
            binding?.yearlyButton!!,
            binding?.monthlyCheck!!,
            binding?.monthlyButton!!
        )
        billingManager = BillingManager(context ?: return)
        val subsProductIdList =
            listOf("subs_product_id_1", "subs_product_id_2", "subs_product_id_3")
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
                if (productDetail.productType == ProductType.inapp) {
                    if (productDetail.productId == "gold_product" && productDetail.planId == "gold_plan_weekly") {
                        // productDetail (monthly)
                        _binding?.monthlyText?.text = "${productDetail.price} / Monthly"
                    } else if (productDetail.productId == "subs_product_id_2" && productDetail.planId == "subs_plan_id_2") {
                        // productDetail (3 months)
                    } else if (productDetail.productId == "gold_product" && productDetail.planId == "gold_plan_yearly") {
                        // productDetail (yearly)
                        _binding?.yearlyText?.text = "${productDetail.price} / Yearly"
                        productDetail.freeTrialDays = 3
                    }
                }

            }
        }

        _binding?.premiumButton?.clickWithThrottle {
            billingManager?.makeSubPurchase(activity, "", "", object : OnPurchaseListener {
                override fun onPurchaseResult(isPurchaseSuccess: Boolean, message: String) {
                    Log.d("in_app_TAG", "makeSubPurchase: $isPurchaseSuccess - $message")
                    PurchasePrefs(context).putBoolean("inApp", true)
                }
            })
        }

        _binding?.closeIcon?.clickWithThrottle {
            findNavController().navigateUp()
        }
        _binding?.monthlyButton?.clickWithThrottle {
            updateUI(
                binding?.monthlyCheck!!,
                binding?.monthlyButton!!,
                binding?.yearlyCheck!!,
                binding?.yearlyButton!!
            )
        }
        _binding?.yearlyButton?.clickWithThrottle {
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