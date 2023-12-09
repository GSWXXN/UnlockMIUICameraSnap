package com.gswxxn.camerasnap.hook.base

import android.content.Context
import com.gswxxn.camerasnap.dexkit.base.BaseFinder.Companion.onFinishLoadFinder
import com.gswxxn.camerasnap.utils.DexKitHelper.loadMembers
import com.gswxxn.camerasnap.utils.DexKitHelper.storeMembers
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.type.android.ContextClass
import com.highcapable.yukihookapi.hook.type.android.ContextWrapperClass
import io.luckypray.dexkit.DexKitBridge

/** 使用 DexKit 的 Base Hooker, 在需要的作用域 Hooker 入口继承使用 **/
abstract class BaseHookerWithDexKit: YukiBaseHooker() {
    open var storeMemberClass: Any? = null
    var appVersionCode: Long? = null
    var appVersionName: String? = null

    override fun onHook() {
        ContextWrapperClass.method {
            name("attachBaseContext")
            param(ContextClass)
        }.hook {
            after {

                val context = args(0).cast<Context>()!!
                val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                appVersionCode = packageInfo.longVersionCode
                appVersionName = packageInfo.versionName

                val startTime = System.currentTimeMillis()

                if (storeMemberClass != null && loadMembers(context, storeMemberClass!!)) {
                    YLog.info(msg = "load members from prefs")
                } else {
                    YLog.info(msg = "load members from dexkit")
                    System.loadLibrary("dexkit")
                    DexKitBridge.create(appInfo.sourceDir)?.use { bridge ->
                        onFindMembers(bridge)
                        bridge.onFinishLoadFinder()
                    }
                    storeMemberClass?.let { storeMembers(context, it) }
                }

                val endTime = System.currentTimeMillis()
                YLog.info(msg = "load obfuscated members in $packageName cost ${endTime - startTime}ms")

                startHook()
            }
        }
    }

    abstract fun onFindMembers(bridge: DexKitBridge)

    abstract fun startHook()

}