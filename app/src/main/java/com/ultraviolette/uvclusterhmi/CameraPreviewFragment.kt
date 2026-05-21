package com.ultraviolette.uvclusterhmi

import android.Manifest
import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureRequest
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresPermission
import androidx.navigation.findNavController
import com.ultraviolette.uvclusterhmi.utils.Utilities.setOnSoundClickListener

class CameraPreviewFragment : Fragment() {
    private lateinit var tvBack: TextView
    private lateinit var textureView: TextureView
    private lateinit var cameraManager: CameraManager
    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null
    private var previewRequestBuilder: CaptureRequest.Builder? = null

    private val stateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            cameraDevice = camera
            createPreviewSession()
        }

        override fun onDisconnected(camera: CameraDevice) {
            camera.close()
        }

        override fun onError(camera: CameraDevice, error: Int) {
            camera.close()
        }
    }
    private val surfaceTextureListener: TextureView.SurfaceTextureListener =
        object : TextureView.SurfaceTextureListener {
            @RequiresPermission(Manifest.permission.CAMERA)
            override fun onSurfaceTextureAvailable(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                openCamera()
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean = true

            override fun onSurfaceTextureSizeChanged(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}

        }

    @RequiresPermission(Manifest.permission.CAMERA)
    private fun openCamera() {
        try {
            val cameraId = cameraManager.cameraIdList[0]  // First camera
            cameraManager.openCamera(cameraId, stateCallback, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_camera_preview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
        initClickListener()
        initCamera()
    }

    private fun initView(view: View) {
        tvBack = view.findViewById(R.id.tvBack)
        //textureView = view.findViewById(R.id.textureView)
    }

    private fun initClickListener() {
        tvBack.setOnSoundClickListener(requireContext()) {
            val mainNavController =
                requireActivity().findNavController(R.id.nav_host_fragment)
            mainNavController.popBackStack()
        }
    }

    private fun initCamera() {
        cameraManager = requireContext().getSystemService(Context.CAMERA_SERVICE) as CameraManager
        textureView.surfaceTextureListener = surfaceTextureListener
    }

    private fun createPreviewSession() {
        val texture = textureView.surfaceTexture!!
        texture.setDefaultBufferSize(1920, 1080)

        val surface = Surface(texture)

        previewRequestBuilder =
            cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)

        previewRequestBuilder!!.addTarget(surface)

        cameraDevice!!.createCaptureSession(
            listOf(surface),
            object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    captureSession = session
                    previewRequestBuilder!!.set(
                        CaptureRequest.CONTROL_MODE,
                        CameraMetadata.CONTROL_MODE_AUTO
                    )

                    session.setRepeatingRequest(
                        previewRequestBuilder!!.build(),
                        null,
                        null
                    )
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {}
            },
            null
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        captureSession?.close()
        cameraDevice?.close()
    }

}

