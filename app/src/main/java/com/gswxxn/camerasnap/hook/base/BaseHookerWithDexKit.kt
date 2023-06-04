package com.gswxxn.camerasnap.hook.base

import android.content.Context
import android.content.pm.PackageParser
import com.gswxxn.camerasnap.dexkit.base.BaseFinder.Companion.onFinishLoadFinder
import com.gswxxn.camerasnap.utils.DexKitHelper.loadMembers
import com.gswxxn.camerasnap.utils.DexKitHelper.storeMembers
import com.gswxxn.camerasnap.utils.ReflectUtils.getPackage
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.loggerI
import com.highcapable.yukihookapi.hook.type.android.ContextClass
import com.highcapable.yukihookapi.hook.type.android.ContextWrapperClass
import io.luckypray.dexkit.DexKitBridge

/** 使用 DexKit 的 Base Hooker, 在需要的作用域 Hooker 入口继承使用 **/
abstract class BaseHookerWithDexKit: YukiBaseHooker() {
    open var storeMemberClass: Any? = null
    var appVersionCode: Int? = null
    var appVersionName: String? = null

    override fun onHook() {
        val packageParser: PackageParser.Package = appInfo.getPackage()
        appVersionCode = packageParser.mVersionCode
        appVersionName = packageParser.mVersionName

        ContextWrapperClass.hook {
            injectMember {
                method {
                    name("attachBaseContext")
                    param(ContextClass)
                }
                afterHook {
                    val context = args(0).cast<Context>()!!

                    val startTime = System.currentTimeMillis()

                    if (storeMemberClass != null && loadMembers(context, storeMemberClass!!)) {
                        loggerI(msg = "load members from prefs")
                    } else {
                        loggerI(msg = "load members from dexkit")
                        System.loadLibrary("dexkit")
                        DexKitBridge.create(appInfo.sourceDir)?.use { bridge ->
                            onFindMembers(bridge)
                            bridge.onFinishLoadFinder()
                        }
                        storeMemberClass?.let { storeMembers(context, it) }
                    }

                    val endTime = System.currentTimeMillis()
                    loggerI(msg = "load obfuscated members in $packageName cost ${endTime - startTime}ms")

                    startHook()
                }
            }
        }
    }

    abstract fun onFindMembers(bridge: DexKitBridge)

    abstract fun startHook()

}