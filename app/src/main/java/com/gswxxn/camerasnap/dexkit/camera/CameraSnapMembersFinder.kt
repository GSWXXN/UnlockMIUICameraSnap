package com.gswxxn.camerasnap.dexkit.camera

import com.gswxxn.camerasnap.dexkit.CameraMembers
import com.gswxxn.camerasnap.dexkit.base.BaseFinder
import com.gswxxn.camerasnap.hook.CameraHooker
import com.gswxxn.camerasnap.hook.CameraHooker.getMethodInstance
import com.gswxxn.camerasnap.hook.CameraHooker.uniqueFindMethodInvoking
import com.gswxxn.camerasnap.hook.CameraHooker.uniqueFindMethodUsingField
import com.gswxxn.camerasnap.utils.DexKitHelper
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClass
import com.highcapable.yukihookapi.hook.type.android.ContextClass
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

        CameraMembers.SnapCameraMembers.fIsCamcorder = CameraMembers.SnapCameraMembers.cSnapCamera.field { name = "mIsCamcorder" }.give()!!

        CameraMembers.SnapCameraMembers.mInitSnapType = CameraMembers.SnapCameraMembers.cSnapCamera.method {
            name = "initSnapType"
            emptyParam()
        }.give()!!

        CameraMembers.SnapCameraMembers.mPlaySound = CameraMembers.SnapCameraMembers.cSnapCamera.method {
            name = "playSound"
            emptyParam()
        }.give()!!

        CameraMembers.SnapCameraMembers.mRelease = CameraMembers.SnapCameraMembers.cSnapCamera.method {
            name = "release"
            emptyParam()
        }.give()!!

        CameraMembers.SnapCameraMembers.fMCameraId = CameraMembers.SnapCameraMembers.cSnapCamera.field {
            name = "mCameraId"
        }.give()!!

        CameraMembers.SnapCameraMembers.fMCameraDevice = CameraMembers.SnapCameraMembers.cSnapCamera.field{
            name = "mCameraDevice"
        }.give()!!

        CameraMembers.SnapCameraMembers.fMCameraCapabilities = CameraMembers.SnapCameraMembers.cSnapCamera.field{
            name = "mCameraCapabilities"
        }.give()!!

        CameraMembers.SnapCameraMembers.fMOrientation = CameraMembers.SnapCameraMembers.cSnapCamera.field{
            name = "mOrientation"
        }.give()!!

        CameraMembers.SnapCameraMembers.fMCameraHandler = CameraMembers.SnapCameraMembers.cSnapCamera.field{
            name = "mCameraHandler"
        }.give()!!

        CameraMembers.SnapCameraMembers.fMContext = CameraMembers.SnapCameraMembers.cSnapCamera.field{
            name = "mContext"
        }.give()!!

        CameraMembers.SnapCameraMembers.fMStatusListener = CameraMembers.SnapCameraMembers.cSnapCamera.field{
            name = "mStatusListener"
        }.give()!!

    }

    /** 查找新版相机(5.0 及以上)的方法 **/
    private fun newMembers() {
        val batchFindClassesUsingStringsResultMap = bridge.batchFindClassesUsingStrings {
            addQuery("SnapCamera", arrayOf("takeSnap: CameraDevice is opening or was already closed."))
            addQuery("CameraCapabilities", arrayOf("addStreamConfigurationToList: but the key is null!"))
            addQuery("SnapTrigger", arrayOf("shouldQuitSnap isNonUI = "))
            matchType = MatchType.FULL
        }
        val batchFindMethodsUsingStringsResultMap = bridge.batchFindMethodsUsingStrings {
            addQuery("onPictureTaken", arrayOf("save picture failed "))
            addQuery("release", arrayOf("release(): E", "release(): X"))
            matchType = MatchType.FULL
        }

        // 混淆前类名 com.android.camera.snap.SnapTrigger
        CameraMembers.SnapCameraMembers.cSnapCamera = batchFindClassesUsingStringsResultMap["SnapCamera"]!!.first().name.toClass(CameraHooker.appClassLoader)

        // TODO: 使用 order 下标来查找 field, 不可靠
        CameraMembers.SnapCameraMembers.fIsCamcorder = CameraMembers.SnapCameraMembers.cSnapCamera.field {
//            type(BooleanType).index().first()
            name = "mIsCamcorder"
        }.give()!!

        CameraMembers.SnapCameraMembers.mInitSnapType = bridge.uniqueFindMethodUsingField {
            fieldDeclareClass = CameraMembers.SnapCameraMembers.fIsCamcorder.declaringClass.name
            fieldName = CameraMembers.SnapCameraMembers.fIsCamcorder.name

            callerMethodDeclareClass = CameraMembers.SnapCameraMembers.cSnapCamera.name
            callerMethodReturnType = DexKitHelper.TypeSignature.VOID
            callerMethodParamTypes = arrayOf()
        }

        val onPictureTakenDescriptor = batchFindMethodsUsingStringsResultMap["onPictureTaken"]!!.first {
            it.returnTypeSig == DexKitHelper.TypeSignature.VOID
        }
        CameraMembers.SnapCameraMembers.mPlaySound = bridge.uniqueFindMethodInvoking {
            methodDescriptor = onPictureTakenDescriptor.descriptor

            beInvokedMethodDeclareClass = CameraMembers.SnapCameraMembers.cSnapCamera.name
            beInvokedMethodReturnType = DexKitHelper.TypeSignature.VOID
            beInvokedMethodParameterTypes = arrayOf()
        }

        CameraMembers.SnapCameraMembers.mRelease = batchFindMethodsUsingStringsResultMap["release"]!!.first {
            it.declaringClassName == CameraMembers.SnapCameraMembers.cSnapCamera.name
        }.getMethodInstance()


        // TODO: 使用 order 下标来查找 field, 不可靠
        CameraMembers.SnapCameraMembers.fMCameraId = CameraMembers.SnapCameraMembers.cSnapCamera.field {
            //type(IntType).index(1)
            name = "mCameraId"
        }.give()!!


        CameraMembers.SnapCameraMembers.fMCameraDevice = CameraMembers.SnapCameraMembers.cSnapCamera.field{
            type("android.hardware.camera2.CameraDevice")
        }.give()!!

        val cameraCapabilitiesDescriptor = batchFindClassesUsingStringsResultMap["CameraCapabilities"]!!.first()
        CameraMembers.SnapCameraMembers.fMCameraCapabilities = CameraMembers.SnapCameraMembers.cSnapCamera.field {
            type(cameraCapabilitiesDescriptor.name)
        }.give()!!

        // TODO: 使用 order 下标来查找 field, 不可靠
        CameraMembers.SnapCameraMembers.fMOrientation = CameraMembers.SnapCameraMembers.cSnapCamera.field{
//            type(IntType).index().last()
            name = "mOrientation"
        }.give()!!

        // TODO: 使用 order 下标来查找 field, 不可靠
        CameraMembers.SnapCameraMembers.fMCameraHandler = CameraMembers.SnapCameraMembers.cSnapCamera.field{
//            type(HandlerClass).index(-2)
            name = "mCameraHandler"
        }.give()!!

        CameraMembers.SnapCameraMembers.fMContext = CameraMembers.SnapCameraMembers.cSnapCamera.field{
            type(ContextClass).index().first()
        }.give()!!

        val cSnapStatusListener = batchFindClassesUsingStringsResultMap["SnapTrigger"]!!.first().name.toClass(CameraHooker.appClassLoader)
            .interfaces.first()

        CameraMembers.SnapCameraMembers.fMStatusListener = CameraMembers.SnapCameraMembers.cSnapCamera.field {
            type(cSnapStatusListener)
        }.give()!!
    }

    private fun commonSearch() {

    }

    private fun findFields() {

    }
}