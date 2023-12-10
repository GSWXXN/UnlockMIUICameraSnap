package com.gswxxn.camerasnap.wrapper.camera

import android.annotation.SuppressLint
import com.gswxxn.camerasnap.hook.CameraHooker

object R {
    object string {

        val pref_camera_snap_entry_take_picture = getID("string", "pref_camera_snap_entry_take_picture")

        val pref_camera_snap_entry_take_movie = getID("string", "pref_camera_snap_entry_take_movie")

        val pref_camera_snap_entry_off = getID("string", "pref_camera_snap_entry_off")

        val pref_camera_snap_default = getID("string", "pref_camera_snap_default")

        val pref_camera_snap_enable_title = getID("string", "pref_camera_snap_enable_title")

        val pref_camera_snap_value_off = getID("string", "pref_camera_snap_value_off")

        val pref_camera_snap_value_take_picture = getID("string", "pref_camera_snap_value_take_picture")

        val pref_camera_snap_value_take_movie = getID("string", "pref_camera_snap_value_take_movie")
    }

    @SuppressLint("DiscouragedApi")
    fun getID(type: String, name: String): Int =
        CameraHooker.appResources!!.getIdentifier(name, type, "com.android.camera")
}