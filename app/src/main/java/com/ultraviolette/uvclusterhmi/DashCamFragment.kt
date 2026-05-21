package com.ultraviolette.uvclusterhmi

import android.Manifest
import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureRequest
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresPermission
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.ultraviolette.uvclusterhmi.domain.dataModel.vcuData.VcuInfoMsg
import com.ultraviolette.uvclusterhmi.ui.viewModel.CarViewModel
import com.ultraviolette.uvclusterhmi.ui.viewModel.SharedViewModel
import com.ultraviolette.uvclusterhmi.utils.Utilities.ARG_BALLISTIC_PLUS
import com.ultraviolette.uvclusterhmi.utils.Utilities.applyMinMax
import com.ultraviolette.uvclusterhmi.utils.ViewModelFactory
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class DashCamFragment : Fragment(), SurfaceHolder.Callback {
    private val carViewModel: CarViewModel by activityViewModels { ViewModelFactory(requireContext()) }
    private val sharedViewModel: SharedViewModel by activityViewModels {
        ViewModelFactory(
            requireContext()
        )
    }
    private lateinit var tvRange: TextView
    private lateinit var tvOdo: TextView
    private lateinit var tvSpeed: TextView
    private lateinit var tvWhKm: TextView
    private lateinit var tvRangeBallistic: TextView
    private lateinit var tvSpeedBallistic: TextView
    private lateinit var tvRide: TextView
    private lateinit var tvODo: TextView
    private lateinit var tvSpeedUnit: TextView
    private lateinit var tvOdoUnit: TextView
    private lateinit var tvRangeUnit: TextView
    private lateinit var tvFrontBallistic: TextView
    private lateinit var ivRegenLevel1: ImageView
    private lateinit var ivRegenLevel2: ImageView
    private lateinit var ivRegenLevel3: ImageView
    private lateinit var ivRegenLevel4: ImageView
    private lateinit var ivRegenLevel5: ImageView
    private lateinit var ivRegenLevel6: ImageView
    private lateinit var ivRegenLevel7: ImageView
    private lateinit var ivRegenLevel8: ImageView
    private lateinit var ivRegenLevel9: ImageView
    private lateinit var cameraPlaceholder: ImageView
    private lateinit var clDashcamWithBallistic: ConstraintLayout
    private lateinit var clDashcamWithoutBallistic: ConstraintLayout
    private lateinit var ivBack: ImageView


    private lateinit var surfaceView: SurfaceView
    private lateinit var surfaceHolder: SurfaceHolder
    private lateinit var cameraInfoText: TextView

    private var unit = ""

    private var camera: Camera? = null
    private var currentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT

    private val TAG = "DashCamFragment"



    private var isBallistic = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dash_cam, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObserver()
        initView(view)
        initClickListeners()

//        isBallistic = arguments?.getBoolean(ARG_BALLISTIC_PLUS) ?: false
//        if (isBallistic) {
//            clDashcamWithBallistic.visibility = View.VISIBLE
//            clDashcamWithoutBallistic.visibility = View.INVISIBLE
//        } else {
        clDashcamWithBallistic.visibility = View.INVISIBLE
        clDashcamWithoutBallistic.visibility = View.VISIBLE
//        }
    }

    private fun initView(view: View) {
        tvRange = view.findViewById(R.id.tvRange)
        tvOdo = view.findViewById(R.id.tvOdo)
        tvSpeed = view.findViewById(R.id.tvSpeed)
        tvWhKm = view.findViewById(R.id.tvWhKm)
        tvRangeBallistic = view.findViewById(R.id.tvRangeBallistic)
        tvSpeedBallistic = view.findViewById(R.id.tvSpeedBallistic)
        tvRide = view.findViewById(R.id.tvRide)
        tvODo = view.findViewById(R.id.tvODo)
        tvFrontBallistic = view.findViewById(R.id.tvFrontBallistic)
        ivRegenLevel1 = view.findViewById(R.id.ivRegenLevel1)
        ivRegenLevel2 = view.findViewById(R.id.ivRegenLevel2)
        ivRegenLevel3 = view.findViewById(R.id.ivRegenLevel3)
        ivRegenLevel4 = view.findViewById(R.id.ivRegenLevel4)
        ivRegenLevel5 = view.findViewById(R.id.ivRegenLevel5)
        ivRegenLevel6 = view.findViewById(R.id.ivRegenLevel6)
        ivRegenLevel7 = view.findViewById(R.id.ivRegenLevel7)
        ivRegenLevel8 = view.findViewById(R.id.ivRegenLevel8)
        ivRegenLevel9 = view.findViewById(R.id.ivRegenLevel9)
        cameraPlaceholder = view.findViewById(R.id.cameraPlaceholder)
        clDashcamWithBallistic = view.findViewById(R.id.clDashcamWithBallistic)
        clDashcamWithoutBallistic = view.findViewById(R.id.clDashCamWithoutBallistic)

        tvSpeedUnit = view.findViewById(R.id.tvSpeedUnit)
        tvOdoUnit = view.findViewById(R.id.tvOdoUnit)
        tvRangeUnit = view.findViewById(R.id.tvRangeUnit)


        surfaceView = view.findViewById(R.id.cameraSurfaceView)
        surfaceHolder = surfaceView.holder
        surfaceHolder.addCallback(this)

        cameraInfoText = view.findViewById(R.id.cameraInfoText)

        unit = sharedViewModel.distanceUnit
        ivBack=view.findViewById(R.id.ivBack)

        val gestureDetector = GestureDetector(
            requireContext(),
            object : GestureDetector.SimpleOnGestureListener() {
                private val SWIPE_THRESHOLD = 100
                private val SWIPE_VELOCITY_THRESHOLD = 100

                override fun onFling(
                    e1: MotionEvent?,
                    e2: MotionEvent,
                    velocityX: Float,
                    velocityY: Float
                ): Boolean {
                    if (e1 == null || e2 == null) return false
                    val diffX = e2.x - e1.x
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        flipCamera()
                        return true
                    }
                    return false
                }

                override fun onDoubleTap(e: MotionEvent): Boolean {
                    Log.d(TAG, "User double tapped on SurfaceView at x=${e?.x}, y=${e?.y}")
                    //findNavController().navigate(R.id.advancedFeaturesFragment)
                    return true
                }

            })

        surfaceView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }

        cameraPlaceholder.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }
    }

    private fun initObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    carViewModel.vcuInfoMsg.collect { vcuInfoMsg ->
                        updateVcuMsg(vcuInfoMsg)
                    }
                }
                launch {
                    carViewModel.vehicleValue.collect { vehicleValue ->
                        updateVehicleValue(vehicleValue)
                    }
                }
            }
        }
    }

    fun updateVcuMsg(vcuInfoMsg: VcuInfoMsg) {
        val rawOdometer = vcuInfoMsg.odometer.toInt()
        val rawRange = vcuInfoMsg.range.toInt()
        val finalOdo = rawOdometer.applyMinMax(sharedViewModel.odoLimit)
        val finalRange = rawRange.applyMinMax(sharedViewModel.rangeLimit)

        tvOdoUnit.text = unit
        tvRangeUnit.text = unit

        val displayOdo =
            if (unit == "miles") {
                (finalOdo * 0.621371).roundToInt()
            } else
                finalOdo

        val displayRange =
            if (unit == "miles")
                (finalRange * 0.621371).roundToInt()
            else
                finalRange

//        if (isBallistic) {
//            tvODo.text = displayOdo.toString()
//            tvRangeBallistic.text = displayRange.toString()
//        } else {
        tvOdo.text = displayOdo.toString()
        tvRange.text = displayRange.toString()
//        }

	if (vcuInfoMsg.speed.isNotEmpty()) {
            val isMiles =
                sharedViewModel.distanceUnit.equals("miles", ignoreCase = true)
            val unitText = if (isMiles) "mph" else "km/h"

            if (vcuInfoMsg.speed[0].toInt() == 0)
            {
                tvSpeed.text = "000"
                tvSpeedUnit.text = unitText
            }
        }
    }

    private fun updateVehicleValue(value: FloatArray = floatArrayOf()) {
        if (value.isEmpty()) return
        if (value.size < 4) {
            return
        }

        if (::tvSpeed.isInitialized) {
            val rawSpeed = value.getOrNull(0)?.toInt()
            val finalSpeed = rawSpeed?.applyMinMax(sharedViewModel.speedLimit) ?: 0

            val displaySpeed =
                if (unit == "miles")
                    (finalSpeed * 0.621371).roundToInt()
                else
                    finalSpeed

            val displaySpeedUnit =
                if (unit == "miles")
                    "mph"
                else
                    "km/h"

            tvSpeedUnit.text = displaySpeedUnit

//            if (isBallistic) {
//                tvSpeedBallistic.text = displaySpeed?.let { String.format("%03d", it) } ?: "-"
//            } else {
            tvSpeed.text = String.format("%03d", displaySpeed)
//            }
        }
    }

    private fun initClickListeners() {
        ivBack.setOnClickListener { findNavController().navigate(R.id.advancedFeaturesFragment) }
    }

    private fun updateCameraInfo(cameraId: Int) {
        val text = if (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
            "FRONT CAMERA"
        } else {
            "REAR CAMERA"
        }
        Log.d(TAG, "updateCameraInfo: Current Camera :: $text")
        cameraInfoText.text = text
    }


    override fun surfaceCreated(holder: SurfaceHolder) {
        openCamera(currentCameraId)
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        if (surfaceHolder.surface == null) return

        try {
            camera?.stopPreview()
        } catch (e: Exception) {
            Log.e(TAG, "Preview stop failed", e)
        }

        try {
            camera?.setPreviewDisplay(surfaceHolder)
            camera?.startPreview()
        } catch (e: Exception) {
            Log.e(TAG, "Preview restart failed", e)
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        releaseCamera()
    }

    private fun openCamera(cameraId: Int) {
        releaseCamera()

        val id = getCameraIdForFacing(cameraId)
        if (id == -1) {
            showCameraPlaceholder(cameraId)
            return
        }

        try {
            camera = Camera.open(cameraId)
            camera?.setPreviewDisplay(surfaceHolder)
            camera?.startPreview()
            showCameraPreview()
            updateCameraInfo(cameraId)
        } catch (e: Exception) {
            Log.d("openCamera", "Error opening camera", e)
        }
    }

    fun showCameraPlaceholder(cameraId: Int){
        updateCameraInfo(cameraId)
        cameraPlaceholder.visibility = View.VISIBLE
        surfaceView.visibility = View.GONE
    }

    fun showCameraPreview(){
        surfaceView.visibility = View.VISIBLE
        cameraPlaceholder.visibility = View.GONE
    }

    private fun getCameraIdForFacing(facing: Int): Int {
        val numberOfCameras = Camera.getNumberOfCameras()
        val info = Camera.CameraInfo()
        for (id in 0 until numberOfCameras) {
            Camera.getCameraInfo(id, info)
            if (info.facing == facing) {
                return id
            }
        }
        return -1 // No camera found for this facing
    }


    private fun flipCamera() {
        currentCameraId = if (currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
            Camera.CameraInfo.CAMERA_FACING_FRONT
        } else {
            Camera.CameraInfo.CAMERA_FACING_BACK
        }
        openCamera(currentCameraId)
    }

    private fun releaseCamera() {
        try {
            camera?.stopPreview()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping preview", e)
        }
        camera?.release()
        camera = null
    }

    override fun onPause() {
        super.onPause()
        releaseCamera()
    }

    override fun onResume() {
        super.onResume()
    }

}


