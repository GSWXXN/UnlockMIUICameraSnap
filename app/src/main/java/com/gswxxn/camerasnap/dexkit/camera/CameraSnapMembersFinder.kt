package com.gswxxn.camerasnap.dexkit.camera

import com.gswxxn.camerasnap.dexkit.CameraMembers
import com.gswxxn.camerasnap.dexkit.base.BaseFinder
import com.gswxxn.camerasnap.dexkit.const.CameraQueryKey
import com.gswxxn.camerasnap.hook.CameraHooker
import com.gswxxn.camerasnap.utils.DexKitHelper
import com.gswxxn.camerasnap.utils.DexKitHelper.findFieldUsingByMethod
import com.gswxxn.camerasnap.utils.DexKitHelper.uniqueFindMethodInvoking
import com.gswxxn.camerasnap.utils.DexKitHelper.uniqueFindMethodUsingField
import com.gswxxn.camerasnap.utils.DexKitHelper.getMethodInstance
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.toClass
import com.highcapable.yukihookapi.hook.type.android.ContextClass
import com.highcapable.yukihookapi.hook.type.android.HandlerClass
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.IntType
import io.luckypray.dexkit.builder.BatchFindArgs

/** CameraSnap 类被混淆成员的 Finder **/
object CameraSnapMembersFinder: BaseFinder(){

    override fun prepareBatchFindClassesUsingStrings(): BatchFindArgs.Builder.() -> Unit = {
        addQuery(CameraQueryKey.SnapCamera, arrayOf("takeSnap: CameraDevice is opening or was already closed."))
        addQuery(CameraQueryKey.CameraCapabilities, arrayOf("addStreamConfigurationToList: but the key is null!"))
        addQuery(CameraQueryKey.SnapTrigger, arrayOf("shouldQuitSnap isNonUI = "))
        addQuery(CameraQueryKey.PictureInfo, arrayOf("setFrontMirror JSONException occurs "))
    }

    override fun prepareBatchFindMethodsUsingStrings(): BatchFindArgs.Builder.() -> Unit = {
        addQuery(CameraQueryKey.SnapCamera_onPictureTaken, arrayOf("save picture failed "))
        addQuery(CameraQueryKey.SnapCamera_release, arrayOf("release(): E", "release(): X"))
        addQuery(CameraQueryKey.SnapTrigger_onCameraOpened, arrayOf("onCameraOpened: exit"))
        addQuery(CameraQueryKey.SnapCamera_takeSnap, arrayOf("takeSnap: CameraDevice is opening or was already closed."))
    }

    /** 开始查找 **/
    override fun onFindMembers() {

        // 混淆前类名 com.android.camera.snap.SnapTrigger
        CameraMembers.SnapCameraMembers.cSnapCamera = batchFindClassesUsingStringsResultMap[CameraQueryKey.SnapCamera]!!.first().name.toClass(CameraHooker.appClassLoader)
        val snapCameraFields = CameraMembers.SnapCameraMembers.cSnapCamera.declaredFields

        /**
         * 查找 mIsCamcorder 字段
         *
         * 查找路径:
         * -> 字符串查找 onCameraOpened() 方法
         * -> onCameraOpened() 调用了 isCamcorder() 方法
         * -> isCamcorder() 方法调用了 mIsCamcorder 字段
         */
        val snapTriggerOnCameraOpenedMethodDescriptor = batchFindMethodsUsingStringsResultMap[CameraQueryKey.SnapTrigger_onCameraOpened]!!.first {
            it.returnTypeSig == DexKitHelper.TypeSignature.VOID
        }
        val isCamcorderMethod = bridge.uniqueFindMethodInvoking {
            methodDescriptor = snapTriggerOnCameraOpenedMethodDescriptor.descriptor

            beInvokedMethodDeclareClass = CameraMembers.SnapCameraMembers.cSnapCamera.name
            beInvokedMethodParameterTypes = arrayOf()
            beInvokedMethodReturnType = DexKitHelper.TypeSignature.BOOLEAN
        }
        CameraMembers.SnapCameraMembers.fIsCamcorder =
            bridge.findFieldUsingByMethod(snapCameraFields.filter { it.type == BooleanType }, isCamcorderMethod)

        CameraMembers.SnapCameraMembers.mInitSnapType = bridge.uniqueFindMethodUsingField {
            fieldDeclareClass = CameraMembers.SnapCameraMembers.fIsCamcorder.declaringClass.name
            fieldName = CameraMembers.SnapCameraMembers.fIsCamcorder.name

            callerMethodDeclareClass = CameraMembers.SnapCameraMembers.cSnapCamera.name
            callerMethodReturnType = DexKitHelper.TypeSignature.VOID
            callerMethodParamTypes = arrayOf()
        }

        val onPictureTakenDescriptor = batchFindMethodsUsingStringsResultMap[CameraQueryKey.SnapCamera_onPictureTaken]!!.first {
            it.returnTypeSig == DexKitHelper.TypeSignature.VOID
        }
        CameraMembers.SnapCameraMembers.mPlaySound = bridge.uniqueFindMethodInvoking {
            methodDescriptor = onPictureTakenDescriptor.descriptor

            beInvokedMethodDeclareClass = CameraMembers.SnapCameraMembers.cSnapCamera.name
            beInvokedMethodReturnType = DexKitHelper.TypeSignature.VOID
            beInvokedMethodParameterTypes = arrayOf()
        }

        CameraMembers.SnapCameraMembers.mRelease = batchFindMethodsUsingStringsResultMap[CameraQueryKey.SnapCamera_release]!!.first {
            it.declaringClassName == CameraMembers.SnapCameraMembers.cSnapCamera.name
        }.getMethodInstance()

        /**
         * 查找字段 mCameraId
         *
         * 查找路径:
         * -> 通过字符串查找 onPictureTaken()
         * -> onPictureTaken() 调用 getPictureInfo()
         * -> getPictureInfo() 使用变量 mCameraId
         */
        val onPictureTakenMethodDescriptor = batchFindMethodsUsingStringsResultMap[CameraQueryKey.SnapCamera_onPictureTaken]!!.first {
            it.returnTypeSig == DexKitHelper.TypeSignature.VOID
        }
        val getPictureInfoMethodDescriptor = bridge.uniqueFindMethodInvoking {
            methodDescriptor = onPictureTakenMethodDescriptor.descriptor

            beInvokedMethodDeclareClass = CameraMembers.SnapCameraMembers.cSnapCamera.name
            beInvokedMethodReturnType = batchFindClassesUsingStringsResultMap[CameraQueryKey.PictureInfo]!!.first().name
            beInvokedMethodParameterTypes = arrayOf()
        }
        CameraMembers.SnapCameraMembers.fMCameraId = bridge.findFieldUsingByMethod(
            snapCameraFields.filter { it.type == IntType },
            getPictureInfoMethodDescriptor
        )

        /**
         * 查找字段 mCameraDevice
         *
         * 通过反射查找, 筛选出类型为 android.hardware.camera2.CameraDevice 的字段
         */
        CameraMembers.SnapCameraMembers.fMCameraDevice = CameraMembers.SnapCameraMembers.cSnapCamera.field{
            type("android.hardware.camera2.CameraDevice")
        }.give()!!

        val cameraCapabilitiesDescriptor = batchFindClassesUsingStringsResultMap[CameraQueryKey.CameraCapabilities]!!.first()
        CameraMembers.SnapCameraMembers.fMCameraCapabilities = CameraMembers.SnapCameraMembers.cSnapCamera.field {
            type(cameraCapabilitiesDescriptor.name)
        }.give()!!

        /**
         * 查找字段 mOrientation
         * -> 通过字符串查找 release()
         * -> release() 调用字段 mOrientation
         */
        CameraMembers.SnapCameraMembers.fMOrientation = bridge.findFieldUsingByMethod(
            snapCameraFields.filter { it.type == IntType },
            CameraMembers.SnapCameraMembers.mRelease
        )

        /**
         * 查找字段 mCameraHandler
         * -> 通过字符串查找 takeSnap()
         * -> takeSnap() 调用字段 mCameraHandler
         */
        val takeSnapMethodDescriptor = batchFindMethodsUsingStringsResultMap[CameraQueryKey.SnapCamera_takeSnap]!!.first {
            it.returnTypeSig == DexKitHelper.TypeSignature.VOID && it.parameterTypesSig == ""
        }
        CameraMembers.SnapCameraMembers.fMCameraHandler = bridge.findFieldUsingByMethod(
            snapCameraFields.filter { it.type == HandlerClass },
            takeSnapMethodDescriptor.getMethodInstance()
        )

        CameraMembers.SnapCameraMembers.fMContext = CameraMembers.SnapCameraMembers.cSnapCamera.field{
            type(ContextClass).index().first()
        }.give()!!

        val cSnapStatusListener = batchFindClassesUsingStringsResultMap[CameraQueryKey.SnapTrigger]!!.first().name.toClass(CameraHooker.appClassLoader)
            .interfaces.first()

        CameraMembers.SnapCameraMembers.fMStatusListener = CameraMembers.SnapCameraMembers.cSnapCamera.field {
            type(cSnapStatusListener)
        }.give()!!
    }
}