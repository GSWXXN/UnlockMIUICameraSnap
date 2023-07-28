package com.gswxxn.camerasnap.hook.camera

import android.content.Context
import android.provider.Settings
import com.gswxxn.camerasnap.constant.Key
import com.gswxxn.camerasnap.dexkit.CameraMembers
import com.gswxxn.camerasnap.hook.CameraHooker
import com.gswxxn.camerasnap.hook.CameraHooker.onFindMembers
import com.gswxxn.camerasnap.utils.ReflectUtils.isSubClassOf
import com.gswxxn.camerasnap.utils.ReflectUtils.methodHook
import com.gswxxn.camerasnap.wrapper.camera.CameraSettings
import com.gswxxn.camerasnap.wrapper.camera.R
import com.gswxxn.camerasnap.wrapper.camera.fragment.settings.CameraPreferenceFragment
import com.gswxxn.camerasnap.wrapper.camera.ui.PreviewListPreference
import com.gswxxn.camerasnap.wrapper.preference.PreferenceGroup
import com.highcapable.yukihookapi.hook.core.YukiMemberHookCreator
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.current
import com.highcapable.yukihookapi.hook.log.loggerE
import com.highcapable.yukihookapi.hook.param.HookParam
import com.highcapable.yukihookapi.hook.type.android.ContextClass
import com.highcapable.yukihookapi.hook.type.java.AnyClass


/** 相机配置及设置项的 Hooker **/
object SettingsHooker: YukiBaseHooker() {
    override fun onHook() {
        /**
         * 替换是否支持街拍模式, 部分机型此方法始终返回 false
         *
         * 由于原方法被混淆, 所以此方法在 [onFindMembers] 中查找
         */
        CameraMembers.SettingsMembers.mGetSupportSnap.methodHook {
            replaceToTrue()
        }

        /**
         * 替换获取街拍配置的方法
         *
         * 原方法无论街拍配置为何值, 均返回街拍模式 A, 即息屏拍照
         */
        CameraMembers.SettingsMembers.mGetMiuiSettingsKeyForStreetSnap.methodHook {
            replaceAny {
                CameraSettings.getMiuiSettingsKeyForStreetSnap(args[0].toString())
            }
        }

        // 创建街拍设置项 hookCreator
        val itemHookCreator: YukiMemberHookCreator.() -> Unit = {
            injectMember {
                method {
                    superClass()
                    name = CameraMembers.OtherMembers.mInitializeActivity.name
                    emptyParam()
                }
                afterHook {
                    addSnapSetting(CameraPreferenceFragment(instance), CameraSettings.KEY_CATEGORY_MODULE_SETTING)
                    addSnapSetting(CameraPreferenceFragment(instance), CameraSettings.KEY_CATEGORY_PHOTO_SETTING)
                }
            }

            // 注册点击事件, 选中后将结果写入系统设置
            // 旧版相机不需要, 因为其已包含此片段
            if (CameraHooker.isNewCameraVersion)
                injectMember {
                    method {
                        name = "onPreferenceChange"
                        param("androidx.preference.Preference", AnyClass)
                    }
                    beforeHook {
                        val key = args[0]!!.current().method { name = "getKey"; emptyParam(); superClass() }.string()
                        val preferenceValue = args[1]
                        if (key != CameraSettings.KEY_CAMERA_SNAP || preferenceValue == null) { return@beforeHook }

                        val cameraPreferenceFragment = CameraPreferenceFragment(instance)
                        var snapConfig = cameraPreferenceFragment.getString(R.string.pref_camera_snap_value_off)
                        when (preferenceValue) {
                            is Boolean ->
                                snapConfig = if (preferenceValue) cameraPreferenceFragment.getString(R.string.pref_camera_snap_value_take_picture)
                                else cameraPreferenceFragment.getString(R.string.pref_camera_snap_value_off)
                            is String ->
                                snapConfig = preferenceValue
                        }

                        Settings.Secure.putString(cameraPreferenceFragment.getActivity().contentResolver, Key.LONG_PRESS_VOLUME_DOWN, CameraSettings.getMiuiSettingsKeyForStreetSnap(snapConfig))
                        resultTrue()
                    }
                }
        }
        // 向相机添加街拍设置项
        arrayOf(
            "com.android.camera.fragment.settings.CameraPreferenceFragment",
            "com.android.camera2.compat.theme.custom.mm.setting.CameraPreferenceFragmentMM"
        ).forEach { clazz ->
            clazz.hook(itemHookCreator).onHookClassNotFoundFailure { loggerE(msg = "$clazz not found") }
        }
    }

    /**
     * 向指定模式的设置项中添加街拍设置项
     *
     * @param cameraPreferenceFragment [CameraPreferenceFragment] 实例
     * @param modePreferenceKey 模式设置项的 Key
     */
    private fun HookParam.addSnapSetting(cameraPreferenceFragment: CameraPreferenceFragment, modePreferenceKey: String) {
        val addCategory: PreferenceGroup

        val modePreference = cameraPreferenceFragment.mPreferenceGroup.findPreference(modePreferenceKey)
        if (modePreference != null) {
            addCategory = PreferenceGroup(modePreference)
        } else {
            loggerE(msg = "modePreference is null, modePreferenceKey: $modePreferenceKey")
            return
        }

        val context = if ("miuix.preference.PreferenceFragment".toClass() isSubClassOf "androidx.preference.PreferenceFragment".toClass())
            instance.current().field { name = "mStyledContext"; type = ContextClass; superClass() }.cast<Context>()!!
        else
            cameraPreferenceFragment.getContext()

        val snapPreviewListPreference = PreviewListPreference(context).apply {
            setKey(CameraSettings.KEY_CAMERA_SNAP)
            setDefaultValue(cameraPreferenceFragment.getString(R.string.pref_camera_snap_default))
            setTitle(R.string.pref_camera_snap_enable_title)
            setEntries(R.array.pref_camera_snap_entries)
            setEntryValues(R.array.pref_camera_snap_entryvalues)
            setPersistent(false)
            setOrder(3)
        }

        addCategory.removePreferenceRecursively(CameraSettings.KEY_CAMERA_SNAP)

        if (cameraPreferenceFragment.mFromWhere == 163 || modePreferenceKey == CameraSettings.KEY_CATEGORY_PHOTO_SETTING) {
            addCategory.addPreference(snapPreviewListPreference.getInstance())
            cameraPreferenceFragment.registerListener()

            // 获取当前锁屏长按音量下的配置
            when (Settings.Secure.getString(cameraPreferenceFragment.getActivity().contentResolver, Key.LONG_PRESS_VOLUME_DOWN)) {
                Key.LONG_PRESS_VOLUME_DOWN_PAY, Key.LONG_PRESS_VOLUME_DOWN_DEFAULT ->
                    snapPreviewListPreference.setValue(cameraPreferenceFragment.getString(R.string.pref_camera_snap_value_off))
                Key.LONG_PRESS_VOLUME_DOWN_STREET_SNAP_PICTURE ->
                    snapPreviewListPreference.setValue(cameraPreferenceFragment.getString(R.string.pref_camera_snap_value_take_picture))
                Key.LONG_PRESS_VOLUME_DOWN_STREET_SNAP_MOVIE ->
                    snapPreviewListPreference.setValue(cameraPreferenceFragment.getString(R.string.pref_camera_snap_value_take_movie))
                else ->
                    snapPreviewListPreference.setValue(cameraPreferenceFragment.getString(R.string.pref_camera_snap_default))
            }
        }
    }
}