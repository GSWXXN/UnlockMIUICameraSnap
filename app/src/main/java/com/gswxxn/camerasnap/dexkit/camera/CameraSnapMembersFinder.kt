package com.gswxxn.camerasnap.dexkit.camera

import com.gswxxn.camerasnap.dexkit.CameraMembers
import com.gswxxn.camerasnap.dexkit.base.BaseFinder
import com.gswxxn.camerasnap.hook.CameraHooker
import com.highcapable.yukihookapi.hook.factory.toClass
import io.luckypray.dexkit.enums.MatchType

/** CameraSnap 类被混淆成员的 Finder **/
object CameraSnapMembersFinder: BaseFinder(){

    /** 开始查找 **/
    override fun onFindMembers() {
        if (CameraHooker.versionCode < 500000000) {
            oldMembers()
        } else {
            newMembers()
        }
        commonSearch()
        findFields()
    }

    /** 查找旧版相机(5.0 以下)的方法 **/
    private fun oldMembers() {
        CameraMembers.SnapCameraMembers.cSnapCamera = "com.android.camera.snap.SnapCamera".toClass(CameraHooker.appClassLoader)

    }

    /** 查找新版相机(5.0 及以上)的方法 **/
    private fun newMembers() {
        val batchFindClassesUsingStringsResultMap = bridge.batchFindClassesUsingStrings {
            addQuery("SnapCamera", arrayOf("takeSnap: CameraDevice is opening or was already closed."))
            matchType = MatchType.FULL
        }

        // 混淆前类名 com.android.camera.snap.SnapTrigger
        CameraMembers.SnapCameraMembers.cSnapCamera = batchFindClassesUsingStringsResultMap["SnapCamera"]!!.first().name.toClass()

        bridge.findMethodUsingField {  }
    }

    private fun commonSearch() {

    }

    private fun findFields() {

    }
}