package com.gswxxn.camerasnap.hook

import android.provider.Settings
import com.gswxxn.camerasnap.constant.Key
import com.gswxxn.camerasnap.hook.base.BaseHookerWithDexKit
import com.gswxxn.camerasnap.wrapper.camera.CameraSettings
import com.gswxxn.camerasnap.wrapper.camera.R
import com.gswxxn.camerasnap.wrapper.camera.fragment.settings.CameraPreferenceFragment
import com.gswxxn.camerasnap.wrapper.camera.snap.SnapCamera
import com.gswxxn.camerasnap.wrapper.camera.snap.SnapTrigger
import com.gswxxn.camerasnap.wrapper.camera.statistic.CameraStatUtils
import com.gswxxn.camerasnap.wrapper.camera.storage.Storage
import com.highcapable.yukihookapi.hook.factory.current
import com.highcapable.yukihookapi.hook.log.loggerD
import com.highcapable.yukihookapi.hook.log.loggerE
import com.highcapable.yukihookapi.hook.log.loggerW
import com.highcapable.yukihookapi.hook.type.java.CharSequenceClass
import com.highcapable.yukihookapi.hook.type.java.StringClass
import io.luckypray.dexkit.DexKitBridge
import io.luckypray.dexkit.enums.MatchType
import java.lang.reflect.Method


object CameraHooker: BaseHookerWithDexKit() {

    // Methods
    private lateinit var mGetSupportSnap: Method
    private lateinit var mSnapRunner: Method

    override fun onFindMembers(bridge: DexKitBridge) {
        bridge.batchFindMethodsUsingStrings {
            addQuery("SnapRunner", arrayOf("take snap"))
            matchType = MatchType.CONTAINS
        }.let { resultMap ->
            mSnapRunner = resultMap["SnapRunner"]!!.first {
                it.parameterTypesSig == "" && it.returnTypeSig == "V"
            }.getMethodInstance()
        }

        mGetSupportSnap = bridge.uniqueFindMethodInvoking {
            methodDeclareClass = "com.android.camera.CameraSettings"
            methodName = "getCameraSnapSettingNeed"
            methodReturnType = "Z"

            beInvokedMethodReturnType = "Z"
            beInvokedMethodParameterTypes = arrayOf()
        }
    }

    override fun startHook() {

        "com.android.camera.fragment.settings.CameraPreferenceFragment".hook {
            /**
             * 将街拍开关替换为下拉框
             *
             * 在同类下的 addModulePreferences() 方法中被调用
             */
            injectMember {
                method {
                    superClass()
                    name = "addCheckBoxPreference"
                }
                beforeHook {
                    val key = args[1].toString()
                    if (key == CameraSettings.KEY_CAMERA_SNAP) {
                        instance.current().method {
                            superClass()
                            name = "addPreviewListPreference"
                        }.call(
                            args[0],                                  // Preference Category
                            CameraSettings.KEY_CAMERA_SNAP,           // Key
                            R.string.pref_camera_snap_default,        // Default value
                            R.string.pref_camera_snap_enable_title,   // Enable Title
                            R.array.pref_camera_snap_entries,         // Entries
                            R.array.pref_camera_snap_entryvalues      // Entry Values
                        )
                        resultNull()
                    }
                }
            }
        }

        var isAllowFindCameraSnap = true
        /**
         * 替换街拍模式配置模式, 例如如何读取及写入配置
         */
        "com.android.camera.fragment.settings.CameraPreferenceFragment".hook {
            injectMember {
                method {
                    name = "updatePreferenceEntries"
                    emptyParam()
                }
                beforeHook {
                    CameraPreferenceFragment(instance).updatePreferenceEntries()
                    isAllowFindCameraSnap = false
                }
                afterHook { isAllowFindCameraSnap = true }
            }
        }
        /**
         * 移除原街拍配置模式
         *
         * 在 com.android.camera.fragment.settings.CameraPreferenceFragment 类中的
         * updatePreferenceEntries() 方法中被调用, 原理为替换返回值为 null 从而影响 if 判断
         */
        "androidx.preference.PreferenceGroup".hook {
            injectMember {
                method {
                    name = "findPreference"
                    param(CharSequenceClass)
                }
                beforeHook {
                    if (args[0] == CameraSettings.KEY_CAMERA_SNAP && !isAllowFindCameraSnap) {
                        resultNull()
                    }
                }
            }
        }

        /**
         * 替换获取街拍配置的方法
         *
         * 原方法无论街拍配置为何值, 均返回街拍模式 A, 即息屏拍照
         */
        "com.android.camera.CameraSettings".hook {
            injectMember {
                method {
                    name = "getMiuiSettingsKeyForStreetSnap"
                    param(StringClass)
                }
                replaceAny {
                    CameraSettings.getMiuiSettingsKeyForStreetSnap(args[0].toString())
                }
            }
        }

        "com.android.camera.snap.SnapCamera".hook {

            /**
             * 替换街拍时的模式获取, 原方法无论街拍配置为何值, 均禁用录像模式
             */
            injectMember {
                method {
                    name = "initSnapType"
                    emptyParam()
                }
                replaceUnit {
                    val snapCamera = SnapCamera.getWrapper(instance)

                    val snapType = Settings.Secure.getString(
                        snapCamera.mContext.contentResolver,
                        Key.LONG_PRESS_VOLUME_DOWN
                    )
                    snapCamera.mIsCamcorder =
                        snapType == Key.LONG_PRESS_VOLUME_DOWN_STREET_SNAP_MOVIE

                }
            }

            /**
             * 移除街拍时的声音
             */
            injectMember {
                method {
                    name = "playSound"
                    emptyParam()
                }
                intercept()
            }

            /**
             * 添加街拍结束后的视频资源释放, 原方法未对街拍录像模式进行处理
             */
            injectMember {
                method {
                    name = "release"
                    emptyParam()
                }
                beforeHook {
                    try {
                        loggerW(msg = "stopCamcorder: release")
                        SnapCamera.getWrapper(instance).stopCamcorder()
                        SnapCamera.removeWrapper(instance)
                    } catch (e: Exception) {
                        loggerE(msg = "release: ${e.message}", e = e)
                    }
                }
            }
        }

        /**
         * 替换是否支持街拍模式, 部分机型此方法始终返回 false
         *
         * 由于原方法被混淆, 所以此方法在 [onFindMembers] 中查找
         */
        mGetSupportSnap.methodHook {
            replaceToTrue()
        }

        /**
         * 在执行街拍前添加对录像模式的处理
         *
         * 原方法是匿名函数, 担心后续位置会有变动, 所以此方法在 [onFindMembers] 中查找
         *
         * 原始类位置为 com.android.camera.snap.SnapTrigger
         */
        mSnapRunner.methodHook {
            replaceUnit {
                val mSnapTrigger = SnapTrigger(field { name = "this\$0" }.get(instance).any()!!)

                if (mSnapTrigger.mCamera.instance == null || !mSnapTrigger.mCamera.mIsCamcorder) {
                    callOriginal()
                    return@replaceUnit
                }

                if (mSnapTrigger.mPowerManager == null || !mSnapTrigger.mPowerManager!!.isInteractive) {
                    if (!mSnapTrigger.shouldQuitSnap() && Storage.getAvailableSpace() >= Storage.LOW_STORAGE_THRESHOLD) {
                        mSnapTrigger.shutdownWatchDog()
                        mSnapTrigger.vibratorShort()
                        mSnapTrigger.mCamera.startCamcorder()
                        loggerD(msg = "take movie")
                        CameraStatUtils.trackSnapInfo(true)
                    }
                }
            }
        }
    }
}