package com.gswxxn.camerasnap.wrapper.camera

import com.gswxxn.camerasnap.constant.Key
import com.gswxxn.camerasnap.dexkit.CameraMembers
import com.gswxxn.camerasnap.hook.CameraHooker

object CameraSettings {

    const val KEY_CAMERA_SNAP = "pref_camera_snap_key"
    const val KEY_CATEGORY_MODULE_SETTING = "category_module_setting"
    const val KEY_CATEGORY_PHOTO_SETTING = "category_photo_setting"

    fun getPreferVideoQuality(cameraId: Int, moduleIndex: Int) =
        CameraMembers.SettingsMembers.mGetPreferVideoQuality.invoke(null, cameraId, moduleIndex) as Int

    private fun getString(i: Int) = CameraHooker.appContext!!.getString(i)

    /**
     * 根据下拉框名称获取对应街拍模式的设置键
     *
     * @param str 下拉框选中名称
     * @return [String] 对应的设置键
     */
    fun getMiuiSettingsKeyForStreetSnap(str: String) = when (str) {
        getString(R.string.pref_camera_snap_value_take_picture) ->
            Key.LONG_PRESS_VOLUME_DOWN_STREET_SNAP_PICTURE
        getString(R.string.pref_camera_snap_value_take_movie) ->
            Key.LONG_PRESS_VOLUME_DOWN_STREET_SNAP_MOVIE
        else -> Key.LONG_PRESS_VOLUME_DOWN_DEFAULT
    }
}