package com.gswxxn.camerasnap.hook

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.UserHandle
import android.view.KeyEvent
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.current
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.log.loggerI
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.IntType


object AndroidHooker: YukiBaseHooker() {
    @SuppressLint("MissingPermission")
    override fun onHook() {
        // 部分相机没有声明前台服务权限
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

        // 为安卓 14 以上的系统框架加回对街拍模式的处理
        "com.android.server.policy.BaseMiuiPhoneWindowManager".toClass().hook {
            injectMember {
                method {
                    name = "interceptKeyBeforeQueueingInternal"
                    param(KeyEvent::class.java, IntType, BooleanType)
                }

                beforeHook {
                    val hasStreetSnapMethod = runCatching {
                        instance.current().method { name = "streetSnap" }
                    }.isSuccess

                    val mSystemBooted = instance.current().field {
                        superClass(true)
                        name = "mSystemBooted"
                    }.boolean()

                    val mLongPressVolumeDownBehavior = instance.current().field {
                        superClass()
                        name = "mLongPressVolumeDownBehavior"
                    }.int()

                    val mContext = instance.current().field {
                        superClass(true)
                        name = "mContext"
                    }.cast<Context>()


                    val isScreenOn = args.first { it is Boolean } as Boolean
                    val event = args.first { it is KeyEvent } as KeyEvent

                    val keyCode = event.keyCode
                    val down = event.action == 0

                    if (!hasStreetSnapMethod && mSystemBooted && !isScreenOn && mLongPressVolumeDownBehavior == 1) {
                        var keyIntent: Intent? = null
                        if (keyCode == 24 || keyCode == 25) {
                            keyIntent = Intent("miui.intent.action.CAMERA_KEY_BUTTON")
                        } else if (down && keyCode == 26) {
                            keyIntent = Intent("android.intent.action.KEYCODE_POWER_UP")
                        }
                        if (keyIntent != null) {
                            keyIntent.setClassName(
                                "com.android.camera",
                                "com.android.camera.snap.SnapKeyReceiver"
                            )
                            keyIntent.putExtra("key_code", keyCode)
                            keyIntent.putExtra("key_action", event.action)
                            keyIntent.putExtra("key_event_time", event.eventTime)
                            mContext?.sendBroadcastAsUser(keyIntent, UserHandle::class.java.field { name = "CURRENT" }.get().cast())
                        }
                    }
                }
            }
        }
    }
}