package com.gswxxn.camerasnap.wrapper.camera.snap

import android.content.ContentValues
import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCaptureSession.CaptureCallback
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.params.OutputConfiguration
import android.hardware.camera2.params.SessionConfiguration
import android.media.CamcorderProfile
import android.media.MediaRecorder
import android.net.Uri
import android.os.Handler
import android.os.HandlerExecutor
import android.os.HandlerThread
import android.os.Process
import com.gswxxn.camerasnap.dexkit.CameraMembers
import com.gswxxn.camerasnap.utils.ReflectUtils.setValue
import com.gswxxn.camerasnap.wrapper.camera.storage.Storage.getAvailableSpace
import com.gswxxn.camerasnap.wrapper.camera.CameraSettings
import com.gswxxn.camerasnap.wrapper.camera.LocationManager
import com.gswxxn.camerasnap.wrapper.camera.Util
import com.gswxxn.camerasnap.wrapper.camera.module.video.VideoConfig
import com.gswxxn.camerasnap.wrapper.camera.storage.MediaProviderUtil
import com.gswxxn.camerasnap.wrapper.camera.storage.Storage
import com.gswxxn.camerasnap.wrapper.camera2.CameraCapabilities
import com.highcapable.yukihookapi.hook.factory.*
import com.highcapable.yukihookapi.hook.log.YLog
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale

class SnapCamera(val instance: Any?) {

    companion object {
        const val SUFFIX = "_SNAP"

        private val wrappers = mutableMapOf<Any?, SnapCamera>()
        fun getWrapper(instance: Any?) = wrappers.getOrPut(instance) { SnapCamera(instance) }
        fun removeWrapper(instance: Any?) = wrappers.remove(instance)
    }

    class SnapStatusListener(private val instance: Any) {
        fun onDone(uri: Uri?) = instance.current().method {
            param(Uri::class.java)
        }.call(uri)
    }

    // 原始变量
    var mIsCamcorder
        get() = CameraMembers.SnapCameraMembers.fIsCamcorder.getBoolean(instance)
        set(value) { CameraMembers.SnapCameraMembers.fIsCamcorder.setValue(instance!!, value) }
    private val mCameraId get() = CameraMembers.SnapCameraMembers.fMCameraId.getInt(instance)
    private val mCameraDevice get() = CameraMembers.SnapCameraMembers.fMCameraDevice.get(instance) as CameraDevice?
    private val mCameraCapabilities get() = CameraCapabilities(CameraMembers.SnapCameraMembers.fMCameraCapabilities.get(instance))
    private val mOrientation get() = CameraMembers.SnapCameraMembers.fMOrientation.getInt(instance)
    private val mCameraHandler get() = CameraMembers.SnapCameraMembers.fMCameraHandler.get(instance) as Handler?
    val mContext get() = CameraMembers.SnapCameraMembers.fMContext.get(instance) as Context
    private val mStatusListener get() = CameraMembers.SnapCameraMembers.fMStatusListener.get(instance)?.let { SnapStatusListener(it) }

    // 新增变量
    private var mBackgroundThread: HandlerThread? = null
    private var mBackgroundHandler: Handler? = null
    private var mMediaRecorder: MediaRecorder? = null
    private lateinit var mProfile: CamcorderProfile
    private lateinit var mContentValues: ContentValues
    private lateinit var mVideoRequestBuilder: CaptureRequest.Builder
    var mRecording = false

    /**
     * 根据设备方向和摄像头方向，设置录制视频时的方向提示
     */
    private fun getRecorderOrientationHint(): Int {
        var sensorOrientation = this.mCameraCapabilities.getSensorOrientation()
        if (this.mOrientation != -1) {
            sensorOrientation =
                if (this.mCameraCapabilities.getFacing() == 0) (sensorOrientation - this.mOrientation + 360) % 360
                else (sensorOrientation + this.mOrientation) % 360
        }
        YLog.debug(msg = "getOrientationHint: $sensorOrientation")
        return sensorOrientation
    }

    /**
     * 配置视频质量参数 [mProfile]
     */
    private fun initCamera() {
        if (this.mIsCamcorder) {
            val preferVideoQuality = CameraSettings.getPreferVideoQuality(this.mCameraId, 162)
            if (CamcorderProfile.hasProfile(this.mCameraId, preferVideoQuality)) {
                this.mProfile = CamcorderProfile.get(this.mCameraId, preferVideoQuality)
            } else {
                YLog.warn(msg = "invalid camcorder profile $preferVideoQuality")
                this.mProfile = CamcorderProfile.get(this.mCameraId, CamcorderProfile.QUALITY_720P)
            }
        }
    }

    /**
     * 创建并配置 [MediaRecorder]
     */
    private fun setupMediaRecorder() {
        initCamera()

        this.mProfile.apply { duration = SnapTrigger.MAX_VIDEO_DURATION }

        val currentLocation = LocationManager.instance().getCurrentLocation()

        val format = SimpleDateFormat("'VID'_yyyyMMdd_HHmmss", Locale.ENGLISH).format(System.currentTimeMillis())
        val filename = format + SUFFIX + Util.convertOutputFormatToFileExt(mProfile.fileFormat)
        val convertOutputFormatToMimeType = Util.convertOutputFormatToMimeType(mProfile.fileFormat)
        val mPath = Storage.DIRECTORY + "/" + filename

        var availableSpace = getAvailableSpace() - Storage.LOW_STORAGE_THRESHOLD
        if (availableSpace < VideoConfig.VIDEO_MIN_SINGLE_FILE_SIZE) {
            availableSpace = VideoConfig.VIDEO_MIN_SINGLE_FILE_SIZE
        }

        this.mMediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.CAMCORDER)
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            setProfile(mProfile)
            setOrientationHint(getRecorderOrientationHint())
            setMaxDuration(mProfile.duration)
            // TODO: 位置信息没有被写入, 可能是相机没有后台获取位置权限
            currentLocation?.let { setLocation(currentLocation.latitude.toFloat(), currentLocation.longitude.toFloat()) }
            setMaxFileSize(availableSpace)
            YLog.debug(msg = "save to $mPath")
            setOutputFile(mPath)

            setOnErrorListener { _, what, _ ->
                YLog.debug(msg = "stopCamcorder: MediaRecorder error: $what")
                stopCamcorder()
            }
            setOnInfoListener { _, what, _ ->
                if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED ||
                    what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED) {
                    YLog.debug(msg = "stopCamcorder: duration or file size reach MAX, $what")
                    stopCamcorder()
                }
            }
            try {
                prepare()
            } catch (e: IOException) {
                YLog.error( msg = "prepare failed for $mPath ${e.message}", e = e)
            }
        }

        this.mContentValues = ContentValues().apply {
            put("title", format)
            put("_display_name", filename)
            put("mime_type", convertOutputFormatToMimeType)
            put("_data", mPath)
            put("resolution", mProfile.videoFrameWidth.toString() + "x" + mProfile.videoFrameHeight.toString())
            currentLocation?.let { put("latitude", it.latitude) }
            currentLocation?.let { put("longitude", it.longitude) }
        }
    }

    /**
     * 开始后台线程
     */
    private fun startBackgroundThread() {
        mBackgroundThread = HandlerThread("CameraBackground").also { it.start() }
        Process.setThreadPriority(-16)
        mBackgroundHandler = Handler(mBackgroundThread!!.looper)
    }

    /**
     * 停止后台线程
     */
    private fun stopBackgroundThread() {
        mBackgroundThread!!.quitSafely()
        try {
            mBackgroundThread!!.join()
            mBackgroundThread = null
            mBackgroundHandler = null
        } catch (e: InterruptedException) {
            YLog.error(msg = "stopBackgroundThread: ${e.message}", e = e)
        }
    }

    /**
     * 结束录制
     */
    @Synchronized
    fun stopCamcorder() {
        var uri: Uri? = null
        if (mMediaRecorder == null) {
            YLog.debug(msg = "stopCamcorder: mMediaRecorder is null")
            return
        }
        YLog.debug(msg = "stopCamcorder: $mRecording")
        if (mRecording) {
            try {
                mMediaRecorder!!.stop()
            } catch (e: Exception) {
                mRecording = false
                YLog.error(msg = "mMediaRecorder stop failed, ${e.message}", e = e)
            }
        }
        mMediaRecorder!!.reset()
        mMediaRecorder!!.release()
        mMediaRecorder = null
        stopBackgroundThread()

        if (!mRecording) return

        val path = mContentValues["_data"] as String
        val size = File(path).length()
        if (size <= 0) {
            uri = null
        } else {
            val duration = Util.getDuration(path)
            if (duration == 0L) {
                File(path).delete()
            } else if (duration > 0) {
                try {
                    mContentValues.put("datetaken", System.currentTimeMillis())
                    mContentValues.put("_size", size)
                    mContentValues.put("duration", duration)
                    uri = MediaProviderUtil.getContentUriFromPath(this.mContext, path)
                } catch (e: Exception) {
                    YLog.error(msg = "Failed to write MediaStore ${e.message}", e = e)
                    mRecording = false
                }
            }
        }
        YLog.debug(msg = "stopCamcorder: uri = $uri")
        this.mStatusListener!!.onDone(uri)

        mRecording = false
    }

    /**
     * 开始录制
     */
    @Synchronized
    fun startCamcorder() {
        if (mCameraDevice == null) {
            YLog.error(msg = "startCamcorder: CameraDevice is opening or was already closed")
            return
        }
        startBackgroundThread()
        setupMediaRecorder()

        try {
            mVideoRequestBuilder = mCameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_RECORD).apply {
                addTarget(mMediaRecorder!!.surface)
            }
            val sessionStateCallback = object : CameraCaptureSession.StateCallback() {
                override fun onConfigureFailed(session: CameraCaptureSession) {
                    YLog.error(msg = "videoSessionCb::onConfigureFailed")
                    stopCamcorder()
                }

                override fun onConfigured(session: CameraCaptureSession) {
                    try {
                        session.setRepeatingRequest(
                            mVideoRequestBuilder.build(),
                            object : CaptureCallback() {
                                override fun onCaptureStarted(session: CameraCaptureSession, request: CaptureRequest, timestamp: Long, frameNumber: Long) {
                                    if (mRecording)  return
                                    try {
                                        mMediaRecorder!!.start()
                                    } catch (e: java.lang.Exception) {
                                        YLog.error(msg = "failed to start media recorder: ${e.message}", e = e)
                                        stopCamcorder()
                                    }
                                    mRecording = true
                                }
                            },
                            mBackgroundHandler
                        )
                    } catch (e: CameraAccessException) {
                        YLog.error(msg = "videoSessionCb::onConfigured: ${e.message}", e = e)
                    }
                }
            }

            val outputConfigurations = listOf(OutputConfiguration(mMediaRecorder!!.surface))
            val sessionConfiguration = SessionConfiguration(
                SessionConfiguration.SESSION_REGULAR,
                outputConfigurations,
                HandlerExecutor(mCameraHandler),
                sessionStateCallback
            )
            mCameraDevice!!.createCaptureSession(sessionConfiguration)
        } catch (e: CameraAccessException) {
            YLog.error(msg = "failed to startCamcorder: ${e.message}", e = e)
        }
    }
}