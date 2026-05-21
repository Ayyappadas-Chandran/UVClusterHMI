package com.ultraviolette.uvclusterhmi.ui.features.settings.systemUpdates

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.util.Log.d
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.ultraviolette.uvclusterhmi.R
import com.ultraviolette.uvclusterhmi.domain.ennumerate.ButtonNavigation
import com.ultraviolette.uvclusterhmi.ui.customWidget.DonutProgressView
import com.ultraviolette.uvclusterhmi.ui.viewModel.CarViewModel
import com.ultraviolette.uvclusterhmi.ui.viewModel.SharedViewModel
import com.ultraviolette.uvclusterhmi.utils.Utilities
import com.ultraviolette.uvclusterhmi.utils.Utilities.readTextFileLineByLine
import com.ultraviolette.uvclusterhmi.utils.ViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class UpdateFragment : Fragment() {

    private lateinit var tvProgressValue: TextView
    private lateinit var tvUpdateMessage: TextView
    private lateinit var pbUpdate: DonutProgressView
    private lateinit var clSoftwareUpdate: ConstraintLayout
    private lateinit var otaUpdateManager: OtaUpdateManager
    private var systemUpdateDialog: AlertDialog? = null
    private val carViewModel by activityViewModels<CarViewModel> { ViewModelFactory(context = requireContext()) }
    private val sharedViewModel by activityViewModels<SharedViewModel> { ViewModelFactory(context = requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_update, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        initObserver()
        //  unzip(File("/data/ota_file"), "/data/ota_file")
        val headers = readTextFileLineByLine("/data/ota_file", "payload_properties.txt")
        val size = Utilities.readBinSize()
        d("OTA Update", "metaData :$headers")

        otaUpdateManager = OtaUpdateManager(
            object : OtaUpdateManager.OtaUpdateListener {

                override fun onStatus(status: Int, percent: Float) {
                    d("OTAA", "onStatus: $percent status:$status")
                    updateOtaPercent(percent)
                }

                override fun onCompleted(errorCode: Int) {
                    d("OTAA", "onCompleted: errorCode:$errorCode")
                    if (errorCode == 0) {
                        sharedViewModel.otaCompleted(true)
                    }
                    if (isAdded && !isDetached) {
                        checkAndShowDialog()
                    }
                }
            }, viewLifecycleOwner.lifecycleScope
        )
        otaUpdateManager.startUpdate(headers, size)
    }

    private fun updateOtaPercent(percent: Float) {
        val progress = if (percent <= 1f) (percent * 100).toInt() else percent.toInt()
        pbUpdate.setProgress(if (percent <= 1f) (percent * 100) else percent)
        tvProgressValue.text = "$progress%"
        tvUpdateMessage.text = when {
            progress <= 25 -> getString(R.string.please_keep_the_vehicle_parked_and_powered)
            progress <= 50 -> getString(R.string.do_not_turn_off_the_charger_during_update)
            progress <= 75 -> getString(R.string.do_not_turn_off_the_charger_during_update)
            else -> getString(R.string.avoid_disconnecting_from_wifi)
        }
    }

    private fun initObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    carViewModel.swiftButton.collect { swiftButton ->
                        val button = Utilities.getButtonState(swiftButton)
                        if (button == ButtonNavigation.None) return@collect
                        handleButtonNavigation(button.ordinal)
                    }
                }
            }
        }
    }

    private fun initViews(view: View) {
        tvProgressValue = view.findViewById(R.id.tvProgressValue)
        tvUpdateMessage = view.findViewById(R.id.tvUpdateMessage)
        pbUpdate = view.findViewById(R.id.pbUpdate)
        clSoftwareUpdate = view.findViewById(R.id.clSoftwareUpdate)
        pbUpdate.setProgress(0.0f)
    }

    fun handleButtonNavigation(button: Int) {
        when (button) {
            ButtonNavigation.Back.ordinal -> {
                //for bug no 46 - pop up exit on button press
                if (systemUpdateDialog?.isShowing == true) {
                    systemUpdateDialog?.dismiss()
                } else {
                    findNavController().popBackStack()
                }
            }
        }
    }

    /**
     * Simulates a progress sequence for the update process.
     */
    private fun simulateProgressSequence() {
        viewLifecycleOwner.lifecycleScope.launch {
            pbUpdate.setProgress(0.25f)
            tvProgressValue.text = getString(R.string._25)
            tvUpdateMessage.text = getString(R.string.please_keep_the_vehicle_parked_and_powered)
            delay(1500)

            pbUpdate.setProgress(0.5f)
            tvProgressValue.text = getString(R.string._50)
            tvUpdateMessage.text = getString(R.string.do_not_turn_off_the_charger_during_update)
            delay(1500)

            pbUpdate.setProgress(0.75f)
            tvProgressValue.text = getString(R.string._75)
            tvUpdateMessage.text = getString(R.string.avoid_disconnecting_from_wifi)
            delay(1500)

            pbUpdate.setProgress(1.0f)
            tvProgressValue.text = getString(R.string._100)
            tvUpdateMessage.text = getString(R.string.avoid_disconnecting_from_wifi)
            checkAndShowDialog()
        }
    }


    /**
     * Shows a system update completion dialog with a blurred background.
     *
     * - Applies blur effect to the background layout if the device supports it (API 31+).
     * - Inflates a custom dialog layout showing a title and message.
     * - Restores the background and navigates back when the dialog is dismissed.
     */
    private fun checkAndShowDialog() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val blur = RenderEffect.createBlurEffect(4f, 4f, Shader.TileMode.CLAMP)
            clSoftwareUpdate.setRenderEffect(blur)
        }
        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.dialog_complete_message, null)
        val tvTitle = dialogView.findViewById<TextView>(R.id.tvTitle)
        val tvMessage = dialogView.findViewById<TextView>(R.id.tvMessage)
        tvTitle.text = getString(R.string.system_update)
        tvMessage.text = getString(R.string.update_completed)
        //for bug no 46 - pop up exit on button press
        systemUpdateDialog =
            AlertDialog.Builder(requireContext()).setCancelable(true).setView(dialogView)
                .setOnDismissListener {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        clSoftwareUpdate.setRenderEffect(null)
                    }
                    val mainNavController =
                        requireActivity().findNavController(R.id.nav_host_fragment)
                    mainNavController.popBackStack()
                    systemUpdateDialog = null
                }.create()
        systemUpdateDialog?.show()
    }
}