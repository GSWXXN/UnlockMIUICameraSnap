package com.gswxxn.camerasnap.ui.page

import android.provider.Settings
import android.widget.Toast
import androidx.annotation.Keep
import cn.fkj233.ui.activity.annotation.BMMainPage
import cn.fkj233.ui.activity.data.BasePage
import cn.fkj233.ui.activity.view.SpinnerV
import cn.fkj233.ui.activity.view.TextSummaryV
import com.gswxxn.camerasnap.R
import com.gswxxn.camerasnap.constant.Key
import com.topjohnwu.superuser.Shell

@Keep
@BMMainPage(" UnlockMIUICameraSnap")
class MainPage: BasePage() {
    private lateinit var snapModeSpinner: SpinnerV

    private var snapConfig: String
        get() = Settings.Secure.getString(activity.contentResolver, Key.LONG_PRESS_VOLUME_DOWN)
        set(value) {
            Shell.cmd("settings put secure ${Key.LONG_PRESS_VOLUME_DOWN} $value").submit()
        }

    override fun onCreate() {
        TextSummaryWithSpinner(
            TextSummaryV(textId = R.string.pref_camera_snap_enable_title),
            SpinnerV(currentValue = getSnapConfigString(snapConfig), dropDownWidth = 180F) {
                arrayOf(
                    Key.LONG_PRESS_VOLUME_DOWN_DEFAULT,
                    Key.LONG_PRESS_VOLUME_DOWN_STREET_SNAP_PICTURE,
                    Key.LONG_PRESS_VOLUME_DOWN_STREET_SNAP_MOVIE
                ).forEach {
                    add(name = getSnapConfigString(it)) { writeConfig(it) }
                }
            }.also { snapModeSpinner = it }
        )
    }

    private fun writeConfig(value: String) {
        fun setSpinnerText(text: String) {
            snapModeSpinner.select.text = text
            snapModeSpinner.currentValue = text
        }

        snapConfig = value

        Thread.sleep(200)
        if (snapConfig != value) {
            Toast.makeText(activity, activity.getString(R.string.config_write_failed), Toast.LENGTH_SHORT).show()
            setSpinnerText(getSnapConfigString(snapConfig))
        }
    }

    private fun getSnapConfigString(config: String): String = when (config) {
        Key.LONG_PRESS_VOLUME_DOWN_PAY, Key.LONG_PRESS_VOLUME_DOWN_DEFAULT ->
            activity.getString(R.string.pref_camera_snap_default)

        Key.LONG_PRESS_VOLUME_DOWN_STREET_SNAP_PICTURE ->
            activity.getString(R.string.pref_camera_snap_value_take_picture)

        Key.LONG_PRESS_VOLUME_DOWN_STREET_SNAP_MOVIE ->
            activity.getString(R.string.pref_camera_snap_value_take_movie)

        else ->
            activity.getString(R.string.pref_camera_snap_value_unknown)
    }
}