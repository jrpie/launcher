package de.jrpie.android.launcher.actions

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Build.VERSION_CODES
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import de.jrpie.android.launcher.R

@RequiresApi(VERSION_CODES.M)
class TorchManager(context: Context) {
    fun logAvailableCameras(context: Context) {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cameraManager.cameraIdList.forEach { cameraId ->
            val characteristics = cameraManager.getCameraCharacteristics(cameraId)
            val available =
                mutableListOf<CameraCharacteristics.Key<out Any>>(
                    CameraCharacteristics.LENS_FACING,
                    CameraCharacteristics.FLASH_INFO_AVAILABLE
                )
            if (Build.VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) {
                available.addAll(
                    listOf(
                        CameraCharacteristics.FLASH_INFO_STRENGTH_MAXIMUM_LEVEL,
                        CameraCharacteristics.FLASH_INFO_STRENGTH_DEFAULT_LEVEL
                    )
                )
            }
            if (Build.VERSION.SDK_INT >= VERSION_CODES.VANILLA_ICE_CREAM) {
                available.addAll(
                    listOf(
                        CameraCharacteristics.FLASH_SINGLE_STRENGTH_DEFAULT_LEVEL,
                        CameraCharacteristics.FLASH_TORCH_STRENGTH_DEFAULT_LEVEL,
                        CameraCharacteristics.FLASH_TORCH_STRENGTH_MAX_LEVEL,
                    )
                )
            }
            val info = available.map { cc ->
                "${cc.name}: ${characteristics.get(cc)}"
            }.joinToString(", ") { it }
            Log.i("Launcher", "Camera: $cameraId, characteristics: $info")
        }
    }

    private val camera = getCameraId(context)
    private var torchEnabled = false

    private val torchCallback = object : CameraManager.TorchCallback() {
        override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
            synchronized(this@TorchManager) {
                if (cameraId == camera) {
                    torchEnabled = enabled
                }
            }
        }
    }

    init {
        registerCallback(context)
    }

    private fun getCameraId(context: Context): String? {
        Log.i("Launcher", "selecting camera")
        logAvailableCameras(context)
        val cameraManager =
            context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

        return cameraManager.cameraIdList.firstOrNull { c ->
            cameraManager
                .getCameraCharacteristics(c)
                .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false
        }
    }

    private fun registerCallback(context: Context) {
        val cameraManager =
            context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

        cameraManager.registerTorchCallback(
            torchCallback,
            Handler(Looper.getMainLooper())
        )
    }

    fun toggleTorch(context: Context) {
        logAvailableCameras(context)
        Log.i("Launcher", "selected camera: $camera")
        synchronized(this) {
            val cameraManager =
                context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

            if (camera == null) {
                Toast.makeText(
                    context,
                    context.getString(R.string.alert_no_torch_found),
                    Toast.LENGTH_LONG
                ).show()
                return
            }

            try {
                if (!torchEnabled && Build.VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) {
                    cameraManager.turnOnTorchWithStrengthLevel(
                        camera,
                        cameraManager.getCameraCharacteristics(camera)
                            .get(CameraCharacteristics.FLASH_INFO_STRENGTH_MAXIMUM_LEVEL) ?: 1
                    )
                } else {
                    cameraManager.setTorchMode(camera, !torchEnabled)
                }
            } catch (e: Exception) {
                Log.w("Launcher", "Can't access camera: ", e)
                // CameraAccessException, IllegalArgumentException
                Toast.makeText(
                    context,
                    context.getString(R.string.alert_torch_access_exception),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}