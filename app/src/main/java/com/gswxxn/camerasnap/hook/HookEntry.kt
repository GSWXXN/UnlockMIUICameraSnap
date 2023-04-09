package com.gswxxn.camerasnap.hook

import android.content.pm.PackageManager
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.configs
import com.highcapable.yukihookapi.hook.factory.encase
import com.highcapable.yukihookapi.hook.log.loggerI
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit

@InjectYukiHookWithXposed
object HookEntry : IYukiHookXposedInit {

    override fun onInit() = configs {
        debugLog { tag = "UnlockMIUICameraSnap" }
        // 由于 Hook 了一个会被大量调用的函数, 所以这里关闭 Debug 日志输出
        isDebug = false
    }

    override fun onHook() = encase {
        loadApp("com.android.camera", CameraHooker)

        loadSystem {
            "com.android.server.am.ActivityManagerService".hook {
                injectMember {
                    method {
                        name = "enforcePermission"
                    }
                    beforeHook {
                        val permission = args(0).string()
                        val uid = args(2).int()
                        if (appContext!!.packageManager.getPackageUid("com.android.camera", PackageManager.MATCH_ALL) == uid &&
                            permission == "android.permission.FOREGROUND_SERVICE") {
                            loggerI(msg = "allow android.permission.FOREGROUND_SERVICE privilege")
                            resultNull()
                        }
                    }
                }
            }
        }
    }
}