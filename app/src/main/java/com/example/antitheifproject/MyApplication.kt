package com.example.antitheifproject

import android.app.Application
import android.util.Log
import com.example.antitheifproject.utilities.id_native_app_open_screen
import com.example.antitheifproject.utilities.val_ad_native_app_open_screen

class MyApplication : Application(){

    override fun onCreate() {
        super.onCreate()
        Log.d("application_class", "onCreate")
        if (val_ad_native_app_open_screen) {
            AppOpenManager(this,id_native_app_open_screen)
//            AdOpenApp(this, id_native_app_open_screen)
        }
    }


}