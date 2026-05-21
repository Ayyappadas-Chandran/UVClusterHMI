package com.ultraviolette.uvclusterhmi.ui.features.menus.tpms

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.ultraviolette.uvclusterhmi.R
import com.ultraviolette.uvclusterhmi.domain.ennumerate.ButtonNavigation
import com.ultraviolette.uvclusterhmi.ui.customWidget.CurvedProgressBarLeft
import com.ultraviolette.uvclusterhmi.ui.customWidget.CurvedProgressBarRight
import com.ultraviolette.uvclusterhmi.ui.viewModel.CarViewModel
import com.ultraviolette.uvclusterhmi.utils.Utilities
import com.ultraviolette.uvclusterhmi.utils.Utilities.setOnSoundClickListener
import com.ultraviolette.uvclusterhmi.utils.ViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TpmsFragment : Fragment() {
    private lateinit var ivBack: ImageView
    private lateinit var tvPurchaseTpms: TextView
    private lateinit var tvTpmsFront: TextView
    private lateinit var tvTpmsRear: TextView
    private lateinit var tvStatusFront: TextView
    private lateinit var tvStatusRear: TextView
    private lateinit var tvPressureFront: TextView
    private lateinit var tvPressureRear: TextView
    private lateinit var tvTemperatureFront: TextView
    private lateinit var tvTemperatureRear: TextView
    private lateinit var ivInfo: ImageView
    private lateinit var llTpmsFront: LinearLayout
    private lateinit var llTpmsRear: LinearLayout
    private lateinit var llPurchase: LinearLayout
    private lateinit var clTpms: ConstraintLayout
    private lateinit var pbFront: CurvedProgressBarLeft
    private lateinit var pbRear: CurvedProgressBarRight
    private var simulationJob: Job? = null
    //for bug no 46 - pop up exit on button press
    private var tpmsPurchaseDialog : AlertDialog ?= null
    private var pressureInfoDialog : AlertDialog ?= null
    private val carViewModel by activityViewModels<CarViewModel> { ViewModelFactory(context = requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tpms, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        initObserver()
        initClickListener()
        initView()
    }

    private fun initObserver() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    carViewModel.swiftButton.collect { swiftButton ->
                        val button = Utilities.getButtonState(swiftButton)
                        if(button == ButtonNavigation.None) return@collect
                        handleButtonNavigation(button.ordinal)
                    }
                }
            }
        }
    }

    /**
     * Binds UI components from the provided root view using their IDs.
     * @param view The root view containing the layout elements.
     * Initializes:
     * - tvBack,tvPurchaseTpms,tvTpmsFront,tvTpmsRear (TextViewS)
     * - tvStatusFront,tvStatusRear,tvTemperatureFront,tvTemperatureRear (TextViews)
     * - ivInfo (ImageView)
     * - llTpmsFront,llTpmsRear,llPurchase (LinearLayout)
     * - pbFront,pbRear (CurvedProgressBar)
     * - clTpms (ConstraintLayout)
     */
    private fun initViews(view: View) {
        ivBack = view.findViewById(R.id.ivBack)
        tvPurchaseTpms = view.findViewById(R.id.tvPurchaseTpms)
        tvTpmsFront = view.findViewById(R.id.tvTpmsFront)
        tvTpmsRear = view.findViewById(R.id.tvTpmsRear)
        tvStatusFront = view.findViewById(R.id.tvStatusFront)
        tvStatusRear = view.findViewById(R.id.tvStatusRear)
        tvPressureFront = view.findViewById(R.id.tvPressureFront)
        tvPressureRear = view.findViewById(R.id.tvPressureRear)
        tvTemperatureFront = view.findViewById(R.id.tvTemperatureFront)
        tvTemperatureRear = view.findViewById(R.id.tvTemperatureRear)

        ivInfo = view.findViewById(R.id.ivInfo)

        llTpmsFront = view.findViewById(R.id.llTpmsFront)
        llTpmsRear = view.findViewById(R.id.llTpmsRear)
        llPurchase = view.findViewById(R.id.llPurchase)

        pbFront = view.findViewById(R.id.customPbFront)
        pbRear = view.findViewById(R.id.customPbRear)

        clTpms = view.findViewById(R.id.clTpms)


    }

    fun handleButtonNavigation(button: Int) {
        when (button) {
            ButtonNavigation.Enter.ordinal -> {
                if (tvPurchaseTpms.text == getString(R.string.purchase_tpms_via_uv_app)) {
                    tvPurchaseTpms.performClick()
                }
            }

            ButtonNavigation.Back.ordinal -> {
                //for bug no 46 - pop up exit on button press
                if(tpmsPurchaseDialog?.isShowing == true){
                    tpmsPurchaseDialog?.dismiss()
                }else if(pressureInfoDialog?.isShowing == true){
                    pressureInfoDialog?.dismiss()
                }else {
                    findNavController().navigateUp()
                }
            }
        }
    }

    /**
     * Initializes click listeners for UI components.
     */
    private fun initClickListener() {
        ivBack.setOnSoundClickListener(requireContext()) {
            findNavController().navigateUp()
        }
        tvPurchaseTpms.setOnSoundClickListener(requireContext()) {
            return@setOnSoundClickListener
            showTpmsPurchaseDialog()
            tvPurchaseTpms.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.transparent
                )
            )
            llPurchase.setBackgroundResource(R.color.transparent)
            tvPurchaseTpms.text = getString(R.string.synced_5_mins_ago)
            tvPurchaseTpms.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.tpmsTextColorGrey
                )

            )
        }
        ivInfo.setOnSoundClickListener(requireContext()) {
            showPressureInfoDialog()
        }

    }

    /**
     * Displays a TPMS lock dialog with a blur effect on supported devices.
     * On dismiss, resets TPMS UI visibility and shows a follow-up dialog.
     */

    private fun showTpmsPurchaseDialog() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val blur = RenderEffect.createBlurEffect(4f, 4f, Shader.TileMode.CLAMP)
            clTpms.setRenderEffect(blur)
        }
        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.dialog_complete_message, null)
        val tvTitle = dialogView.findViewById<TextView>(R.id.tvTitle)
        val tvMessage = dialogView.findViewById<TextView>(R.id.tvMessage)
        //for bug no 46 - pop up exit on button press
        tpmsPurchaseDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .setOnDismissListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    clTpms.setRenderEffect(null)
                }
                tvTpmsFront.isVisible = false
                tvTpmsRear.isVisible = false
                llTpmsFront.isVisible = true
                llTpmsRear.isVisible = true
                //simulatePressureChange()
                tpmsPurchaseDialog = null
            }
            .create()
        tvTitle.text = getString(R.string.tpms_is_locked)
        tvMessage.text = getString(R.string.purchase_tpms_via_uv_app)
        tpmsPurchaseDialog?.show()
    }

    /**
     * Simulates pressure changes in the TPMS sensors.
     */
    private fun simulatePressureChange() {
        simulationJob = CoroutineScope(Dispatchers.Main).launch {
            delay(2000)
            pbFront.setProgress(20)
            pbFront.setProgressTint(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.activeSelectionRed
                )
            )
            tvStatusFront.text = getString(R.string.low)
            tvStatusFront.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.activeSelectionRed
                )
            )
            tvPressureFront.text = getString(R.string._19_psi)
            tvTemperatureFront.text = getString(R.string._28_c)
            delay(2000)
            pbFront.setProgress(60)
            pbFront.resetProgressTint()
            tvStatusFront.text = getString(R.string.ideal)
            tvStatusFront.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.greenTextColor
                )
            )
            tvPressureFront.text = getString(R.string._29_psi)
            tvTemperatureFront.text = getString(R.string._28_c)
            pbRear.setProgress(80)
            pbRear.setProgressTint(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.activeSelectionRed
                )
            )
            tvStatusRear.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.activeSelectionRed
                )
            )
            tvStatusRear.text = getString(R.string.high)
            tvPressureFront.text = getString(R.string._39_psi)
            tvTemperatureFront.text = getString(R.string._30_c)

        }
    }

    /**
     * Displays the TPMS dialog with a background blur on Android 12 (S) and above.
     * Clears the blur effect once the dialog is dismissed.
     */
    private fun showPressureInfoDialog() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val blur = RenderEffect.createBlurEffect(4f, 4f, Shader.TileMode.CLAMP)
            clTpms.setRenderEffect(blur)
        }
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_tpms, null)
        //for bug no 46 - pop up exit on button press
        pressureInfoDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .setOnDismissListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    clTpms.setRenderEffect(null)
                }
                pressureInfoDialog = null
            }
            .create()
        pressureInfoDialog?.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        simulationJob?.cancel()
    }

    fun initView(){
        tvTpmsFront.isVisible = false
        tvTpmsRear.isVisible = false
        llTpmsFront.isVisible = true
        llTpmsRear.isVisible = true
        llPurchase.setBackgroundResource(R.color.transparent)
        tvPurchaseTpms.setBackgroundResource(R.color.transparent)
        tvPurchaseTpms.text = getString(R.string.synced_5_mins_ago)
        tvPurchaseTpms.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.tpmsTextColorGrey
            )
        )
    }
}
