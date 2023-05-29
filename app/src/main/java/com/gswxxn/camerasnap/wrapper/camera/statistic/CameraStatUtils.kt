package com.gswxxn.camerasnap.wrapper.camera.statistic

import com.gswxxn.camerasnap.dexkit.CameraMembers

object CameraStatUtils {
    fun trackSnapInfo(z: Boolean) {
        CameraMembers.OtherMembers.mTrackSnapInfo.invoke(null, z)
    }
}