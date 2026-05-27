package com.ultraviolette.uvclusterhmi.ui.features.myF77

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.ultraviolette.uvclusterhmi.ClusterApplication
import com.ultraviolette.uvclusterhmi.R
import com.ultraviolette.uvclusterhmi.domain.ennumerate.ButtonNavigation
import com.ultraviolette.uvclusterhmi.domain.model.ClusterUiState
import com.ultraviolette.uvclusterhmi.ui.viewModel.CarViewModel
import com.ultraviolette.uvclusterhmi.ui.viewModel.ClusterViewModel
import com.ultraviolette.uvclusterhmi.ui.viewModel.SharedViewModel
import com.ultraviolette.uvclusterhmi.utils.Utilities
import com.ultraviolette.uvclusterhmi.utils.Utilities.setOnSoundClickListener
import com.ultraviolette.uvclusterhmi.utils.ViewModelFactory
import kotlinx.coroutines.launch
import android.content.Context
import android.telephony.TelephonyManager
import android.util.Log.d
import android.widget.ImageView
import androidx.annotation.RequiresPermission
import kotlin.math.roundToInt
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.EditText
import androidx.navigation.NavController
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.addTextChangedListener
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

class InfoFragment : Fragment() {

    private lateinit var ivBack : ImageView
    private lateinit var tvImei: TextView
    private lateinit var unit: String
    private lateinit var tvOdoMeter: TextView
    private lateinit var tvOdoMeterUnit: TextView
    private lateinit var ivBikeImage: ImageView
    private var navController: NavController? = null

    private val carViewModel by activityViewModels<CarViewModel> { ViewModelFactory(context = requireContext()) }
    private val sharedViewModel by activityViewModels<SharedViewModel> { ViewModelFactory(context = requireContext()) }
    private val clusterViewModel: ClusterViewModel by activityViewModels {
        ClusterViewModel.Factory(requireActivity().application as ClusterApplication)
    }

    private lateinit var vinTextView: TextView
    private lateinit var vinEditText: EditText
    private var typingJob: Job? = null	

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_info,container,false)
    }

    @RequiresPermission("android.permission.READ_PRIVILEGED_PHONE_STATE")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        initObserver()
        initClickListener()
        tvImei.text=getFirstImei()
        vinTextView.text = sharedViewModel.vinTextValue

    }

     @RequiresPermission("android.permission.READ_PRIVILEGED_PHONE_STATE")
    private fun getFirstImei(): String {
        return try {
            val tm = requireContext()
                .getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            tm.getImei(0) ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    private fun initObserver(){
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    carViewModel.swiftButton.collect { swiftButton->
                        val button = Utilities.getButtonState(swiftButton)
                        if(button == ButtonNavigation.None) return@collect
                        handleButtonNavigation(button.ordinal)
                    }
                }
                launch {
                    clusterViewModel.uiState.collect { uiState ->
                        val active = uiState as? ClusterUiState.Active ?: return@collect
                        updateOdometer(active.dashboard.odo.odoDisplay, active.prefs.distanceUnit)
                    }
                }
            }
        }
    }

    fun handleButtonNavigation(button:Int){
        when(button){
            ButtonNavigation.Back.ordinal -> findNavController().navigateUp()
        }
    }

    /**
     * Binds UI components from the provided root view using their IDs.
     *
     * @param view The root view containing the layout elements.
     *
     * Initializes:
     * - tvBack (TextViews)
     */
    private fun initViews(view: View){
        ivBack = view.findViewById(R.id.ivBack)
        tvImei=view.findViewById(R.id.tvImei)
        tvOdoMeter=view.findViewById(R.id.tvOdoMeter)
        tvOdoMeterUnit=view.findViewById(R.id.tvOdoMeterUnit)
        unit = sharedViewModel.distanceUnit
	    vinTextView = view.findViewById(R.id.tvVin)
        vinEditText = view.findViewById(R.id.etVin)
        ivBikeImage = view.findViewById(R.id.ivBikeImage)

    }

    /**
     * Initializes click listeners for UI components.
     */
    private fun initClickListener(){
        ivBack.setOnSoundClickListener(requireContext()) {
            findNavController().navigateUp()
        }
	val gestureDetector = GestureDetector(
            requireContext(),
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onDoubleTap(e: MotionEvent): Boolean {
                    vinEditText.setText(vinTextView.text)
                    vinTextView.visibility = View.GONE
                    vinEditText.visibility = View.VISIBLE
                    vinEditText.requestFocus()
                    vinEditText.setSelection(vinEditText.text.length)
                    return true
                }
            })

        vinTextView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }

        // ✅ Detect typing + debounce (5 seconds)
        vinEditText.addTextChangedListener {
            typingJob?.cancel()

            typingJob = lifecycleScope.launch {
                delay(5000) // ⏳ 5 seconds idle
                handleVinDone()
            }
        }
        

        val bikeGestureDetector = GestureDetector(
            requireContext(),
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onDoubleTap(e: MotionEvent): Boolean {
                    d("InfoFragment", "Thermal runaway")
                    sendThermalRunawayToVcu()

                    return true
                }
            }
        )

        ivBikeImage.setOnTouchListener { _, event ->
            bikeGestureDetector.onTouchEvent(event)
            true
        }

    }
    private fun handleVinDone() {
        val vin = vinEditText.text.toString().trim()

        when {
            vin.isEmpty() -> {
                vinEditText.error = "VIN cannot be empty"
            }

            vin.length != 17 -> {
                vinEditText.error = "VIN must be exactly 17 characters"
            }

            else -> {
                // ✅ Save
                sharedViewModel.saveVimNumber(vin)

                // ✅ Update UI
                vinTextView.text = vin

                // ✅ Switch back
                vinEditText.visibility = View.GONE
                vinTextView.visibility = View.VISIBLE

                // ✅ Hide keyboard
                hideKeyboard()
            }
        }
    }
    private fun hideKeyboard() {
        val imm = requireContext()
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(vinEditText.windowToken, 0)
    }

    /**
     * Updates the odometer display from the already-converted ClusterViewModel value.
     * Unit conversion and limit clamping are done once inside ClusterViewModel.
     */
    private fun updateOdometer(odoDisplay: Int, unit: String) {
        tvOdoMeter.text = odoDisplay.toString()
        tvOdoMeterUnit.text = unit
    }
    private fun sendThermalRunawayToVcu() {
        d("InfoFragment", "Sent Thermal runaway")
        val value=1.toByte()
        val packet = byteArrayOf(value)
        carViewModel.sendByteArrayProperty(0x217002F0, packet)
        // thermalRunawayFragment removed — ThermalRunaway now shows via Compose ClusterNavHost

    }


}



