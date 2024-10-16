package com.maliks.applocker.xtreme.ui.intruders.camera

import android.annotation.SuppressLint
import android.app.Application
import android.content.ContentValues
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.MutableLiveData
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.File

class FrontPictureLiveData(val app: Application, private val destinationImageFile: File) :
    MutableLiveData<FrontPictureState>() {

    private enum class State {
        IDLE, TAKING, TAKEN, ERROR
    }

    private var camera: Camera? = null
    private var state: State = State.IDLE

    override fun onInactive() {
        super.onInactive()
        stopCamera()
    }

    fun takePicture() {
        if (state == State.TAKEN || state == State.TAKING) {
            return
        }

        state = State.TAKING
        startCamera()
        camera?.takePicture(null, null, Camera.PictureCallback { data, _ -> savePicture(data) })
    }

    private fun startCamera() {
        val dummySurfaceTexture = SurfaceTexture(0)
        try {
            val cameraId = getFrontCameraId()
            if (cameraId == NO_CAMERA_ID) {
                value = FrontPictureState.Error(IllegalStateException("No front camera found"))
                state = State.ERROR
                return
            }
            camera = Camera.open(cameraId).apply {
                setPreviewTexture(dummySurfaceTexture)
                startPreview()
            }
            value = FrontPictureState.Started()
        } catch (e: RuntimeException) {
            value = FrontPictureState.Error(e)
            state = State.ERROR
        }
    }

    private fun stopCamera() {
        camera?.stopPreview()
        camera?.release()
        camera = null
        value = FrontPictureState.Destroyed()
    }

    private fun getFrontCameraId(): Int {
        val numberOfCameras = Camera.getNumberOfCameras()
        val cameraInfo = Camera.CameraInfo()
        for (i in 0 until numberOfCameras) {
            Camera.getCameraInfo(i, cameraInfo)
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                return i
            }
        }
        return NO_CAMERA_ID
    }

    @SuppressLint("CheckResult")
    private fun savePicture(data: ByteArray) {
        Single.create<String> { emitter ->
            try {
                val displayName = destinationImageFile.name
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    put(
                        MediaStore.MediaColumns.RELATIVE_PATH,
                        "${Environment.DIRECTORY_PICTURES}/Locked/Intruders"
                    )
                    put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
                }

                val uri = app.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                )

                if (uri != null) {
                    app.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(data)
                        outputStream.close()
                        emitter.onSuccess(uri.toString())
                    } ?: run {
                        emitter.onError(Exception("Failed to open output stream"))
                    }
                } else {
                    emitter.onError(Exception("Failed to create new MediaStore record"))
                }
            } catch (e: Exception) {
                emitter.onError(e)
            }
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { uriString ->
                    state = State.TAKEN
                    value = FrontPictureState.Taken(uriString)
                    // No need to refresh the file system; MediaStore handles it
                },
                { throwable ->
                    state = State.ERROR
                    value = FrontPictureState.Error(throwable)
                }
            )
    }

    companion object {
        private const val NO_CAMERA_ID = -1
    }
}
