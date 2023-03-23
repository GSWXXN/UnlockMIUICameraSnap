package com.gswxxn.camerasnap.wrapper.camera.fragment.settings

import android.app.Activity
import android.provider.Settings
import com.gswxxn.camerasnap.constant.Key
import com.gswxxn.camerasnap.wrapper.camera.CameraSettings
import com.gswxxn.camerasnap.wrapper.camera.data.DataRepository
import com.gswxxn.camerasnap.wrapper.preference.PreferenceGroup
import com.gswxxn.camerasnap.wrapper.camera.ui.PreviewListPreference
import com.gswxxn.camerasnap.wrapper.camera.R
import com.highcapable.yukihookapi.hook.factory.current
import com.highcapable.yukihookapi.hook.type.java.IntType

class CameraPreferenceFragment(private val instance: Any) {

    private val mPreferenceGroup get() = instance.current()
        .field { superClass(); name = "mPreferenceGroup" }.any()!!.let { PreferenceGroup(it) }

    private fun getString(id: Int) = instance.current().method {
        superClass()
        name = "getString"
        param(IntType)
    }.string(id)

    private fun getActivity() = instance.current().method {
        superClass()
        name = "getActivity"
    }.invoke<Activity>()!!

    /**
     * 添加对街拍模式下拉框入口的处理
     */
    fun updatePreferenceEntries() {

        val previewListPreference =
            PreviewListPreference(mPreferenceGroup.findPreference(CameraSettings.KEY_CAMERA_SNAP)!!)

        val defaultValue = getString(R.string.pref_camera_snap_default)
        previewListPreference.setDefaultValue(defaultValue)
        previewListPreference.setValue(defaultValue)

        // 获取当前锁屏长按音量下的配置
        val currentConfig = Settings.Secure.getString(getActivity().contentResolver, Key.LONG_PRESS_VOLUME_DOWN)
        if (currentConfig == Key.LONG_PRESS_VOLUME_DOWN_PAY || currentConfig == Key.LONG_PRESS_VOLUME_DOWN_DEFAULT) {
            previewListPreference.setValue(getString(R.string.pref_camera_snap_value_off))
        } else {
            // 获取当前街拍模式的配置文本
            val currentConfigStr = DataRepository.dataItemGlobal().getString(CameraSettings.KEY_CAMERA_SNAP, null)
            when {
                currentConfigStr != null -> {
                    Settings.Secure.putString(
                        getActivity().contentResolver,
                        Key.LONG_PRESS_VOLUME_DOWN,
                        CameraSettings.getMiuiSettingsKeyForStreetSnap(currentConfigStr)
                    )
                    DataRepository.dataItemGlobal().editor().remove(CameraSettings.KEY_CAMERA_SNAP).apply()
                    previewListPreference.setValue(currentConfigStr)
                }
                currentConfig == Key.LONG_PRESS_VOLUME_DOWN_STREET_SNAP_PICTURE ->
                    previewListPreference.setValue(getString(R.string.pref_camera_snap_value_take_picture))
                currentConfig == Key.LONG_PRESS_VOLUME_DOWN_STREET_SNAP_MOVIE ->
                    previewListPreference.setValue(getString(R.string.pref_camera_snap_value_take_movie))
            }
        }
    }
}