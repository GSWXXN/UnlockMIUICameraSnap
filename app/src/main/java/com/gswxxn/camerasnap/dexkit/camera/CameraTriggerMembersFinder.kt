package com.gswxxn.camerasnap.dexkit.camera

import com.gswxxn.camerasnap.dexkit.CameraMembers
import com.gswxxn.camerasnap.dexkit.base.BaseFinder
import com.gswxxn.camerasnap.hook.CameraHooker
import com.gswxxn.camerasnap.hook.CameraHooker.getMethodInstance
import com.gswxxn.camerasnap.utils.DexKitHelper
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClass
import com.highcapable.yukihookapi.hook.type.android.HandlerClass
import com.highcapable.yukihookapi.hook.type.android.PowerManagerClass
import io.luckypray.dexkit.enums.MatchType

/** CameraTrigger 类被混淆成员的 Finder **/
object CameraTriggerMembersFinder: BaseFinder() {

    /** 开始查找成员 **/
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
        CameraMembers.SnapTriggerMembers.cSnapTrigger = "com.android.camera.snap.SnapTrigger".toClass(CameraHooker.appClassLoader)

        CameraMembers.SnapTriggerMembers.mVibrator = CameraMembers.SnapTriggerMembers.cSnapTrigger.method {
            name = "vibrator"
            paramCount(1)
        }.give()!!

        CameraMembers.SnapTriggerMembers.mShouldQuitSnap = CameraMembers.SnapTriggerMembers.cSnapTrigger.method {
            name = "shouldQuitSnap"
            emptyParam()
        }.give()!!
    }

    /** 查找新版相机(5.0 及以上)的方法 **/
    private fun newMembers() {
        val batchFindClassesUsingStringsResultMap = bridge.batchFindClassesUsingStrings {
            addQuery("SnapTrigger", arrayOf("shouldQuitSnap isNonUI = "))
            matchType = MatchType.FULL
        }

        // 混淆前类名 com.android.camera.snap.SnapTrigger
        CameraMembers.SnapTriggerMembers.cSnapTrigger = batchFindClassesUsingStringsResultMap["SnapTrigger"]!!.first().name.toClass(CameraHooker.appClassLoader)


        val batchFindMethodsUsingStringsResultMap = bridge.batchFindMethodsUsingStrings {
            addQuery("mVibrator", arrayOf("call vibrate to notify"))
            addQuery("mShouldQuitSnap", arrayOf("shouldQuitSnap isNonUI = "))
            matchType = MatchType.FULL
        }
        // 混淆前方法名 vibrator
        CameraMembers.SnapTriggerMembers.mVibrator = batchFindMethodsUsingStringsResultMap["mVibrator"]!!.first{
            it.returnTypeSig == DexKitHelper.TypeSignature.VOID
        }.getMethodInstance()

        // 混淆前方法名 shouldQuitSnap
        CameraMembers.SnapTriggerMembers.mShouldQuitSnap = batchFindMethodsUsingStringsResultMap["mShouldQuitSnap"]!!.first{
            it.returnTypeSig == DexKitHelper.TypeSignature.BOOLEAN
        }.getMethodInstance()
    }

    private fun commonSearch() {
        val batchFindMethodsUsingStringsResultMap = bridge.batchFindMethodsUsingStrings {
            addQuery("mSnapRunner", arrayOf("take snap"))
            matchType = MatchType.CONTAINS
        }


        CameraMembers.SnapTriggerMembers.mSnapRunner =
            batchFindMethodsUsingStringsResultMap["mSnapRunner"]!!.first {
                it.parameterTypesSig == "" && it.returnTypeSig == DexKitHelper.TypeSignature.VOID
            }.getMethodInstance()
    }

    private fun findFields() {
        val snapTriggerClass = CameraMembers.SnapTriggerMembers.cSnapTrigger

        CameraMembers.SnapTriggerMembers.fMPowerManager = snapTriggerClass.field { type = PowerManagerClass }.give()!!
        CameraMembers.SnapTriggerMembers.fMHandler = snapTriggerClass.field { type = HandlerClass }.give()!!
        CameraMembers.SnapTriggerMembers.fMCamera = snapTriggerClass.field { type = CameraMembers.SnapCameraMembers.cSnapCamera }.give()!!
    }
}