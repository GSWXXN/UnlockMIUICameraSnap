package com.gswxxn.camerasnap.ui

import android.os.Build
import android.os.Bundle
import android.view.WindowInsetsController
import cn.fkj233.ui.R
import cn.fkj233.ui.activity.MIUIActivity
import com.gswxxn.camerasnap.ui.page.MainPage

class MainActivity : MIUIActivity() {
    init {
        registerPage(MainPage::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.apply {
            statusBarColor = getColor(R.color.foreground)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                insetsController?.setSystemBarsAppearance(
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
            }
        }
    }
}