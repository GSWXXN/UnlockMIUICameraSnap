package com.gswxxn.camerasnap.dexkit.camera

import com.gswxxn.camerasnap.dexkit.CameraMembers
import com.gswxxn.camerasnap.dexkit.base.BaseFinder
import com.gswxxn.camerasnap.dexkit.const.CameraQueryKey
import com.gswxxn.camerasnap.hook.CameraHooker
import com.gswxxn.camerasnap.utils.DexKitHelper
import com.gswxxn.camerasnap.utils.DexKitHelper.getMethodInstance
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.toClass
import com.highcapable.yukihookapi.hook.type.android.HandlerClass
import com.highcapable.yukihookapi.hook.type.android.PowerManagerClass
import io.luckypray.dexkit.builder.BatchFindArgs

/** CameraTrigger 类被混淆成员的 Finder **/
object CameraTriggerMembersFinder: BaseFinder() {

    override fun prepareBatchFindClassesUsingStrings(): BatchFindArgs.Builder.() -> Unit = {
        addQuery(CameraQueryKey.SnapTrigger, arrayOf("shouldQuitSnap isNonUI = "))
        addQuery(CameraQueryKey.SnapCamera, arrayOf("takeSnap: CameraDevice is opening or was already closed."))
    }

    override fun prepareBatchFindMethodsUsingStrings(): BatchFindArgs.Builder.() -> Unit = {
        addQuery(CameraQueryKey.SnapTrigger_mVibrator, arrayOf("call vibrate to notify"))
        addQuery(CameraQueryKey.SnapTrigger_mShouldQuitSnap, arrayOf("shouldQuitSnap isNonUI = "))
        addQuery(CameraQueryKey.SnapTrigger_mSnapRunner, arrayOf("isScreenOn is true, stop take snap"))
    }

    /** 开始查找成员 **/
    override fun onFindMembers() {

        // 混淆前类名 com.android.camera.snap.SnapTrigger
        CameraMembers.SnapTriggerMembers.cSnapTrigger = batchFindClassesUsingStringsResultMap[CameraQueryKey.SnapTrigger]!!.first().name.toClass(CameraHooker.appClassLoader)

        // 混淆前方法名 vibrator
        CameraMembers.SnapTriggerMembers.mVibrator = batchFindMethodsUsingStringsResultMap[CameraQueryKey.SnapTrigger_mVibrator]!!.first{
            it.returnTypeSig == DexKitHelper.TypeSignature.VOID
        }.getMethodInstance()

        // 混淆前方法名 shouldQuitSnap
        CameraMembers.SnapTriggerMembers.mShouldQuitSnap = batchFindMethodsUsingStringsResultMap[CameraQueryKey.SnapTrigger_mShouldQuitSnap]!!.first{
            it.returnTypeSig == DexKitHelper.TypeSignature.BOOLEAN
        }.getMethodInstance()

        CameraMembers.SnapTriggerMembers.mSnapRunner =
            batchFindMethodsUsingStringsResultMap[CameraQueryKey.SnapTrigger_mSnapRunner]!!.first {
                it.parameterTypesSig == "" && it.returnTypeSig == DexKitHelper.TypeSignature.VOID
            }.getMethodInstance()

        val snapTriggerClass = CameraMembers.SnapTriggerMembers.cSnapTrigger
        val snapCameraClass = batchFindClassesUsingStringsResultMap[CameraQueryKey.SnapCamera]!!.first().name.toClass(CameraHooker.appClassLoader)

        CameraMembers.SnapTriggerMembers.fMPowerManager = snapTriggerClass.field { type = PowerManagerClass }.give()!!
        CameraMembers.SnapTriggerMembers.fMHandler = snapTriggerClass.field { type = HandlerClass }.give()!!
        CameraMembers.SnapTriggerMembers.fMCamera = snapTriggerClass.field { type = snapCameraClass }.give()!!
    }
}