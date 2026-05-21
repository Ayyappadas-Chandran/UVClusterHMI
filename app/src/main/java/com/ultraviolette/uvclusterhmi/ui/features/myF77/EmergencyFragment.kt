package com.ultraviolette.uvclusterhmi.ui.features.myF77

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.util.Log.d
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.ultraviolette.uvclusterhmi.R
import com.ultraviolette.uvclusterhmi.domain.ennumerate.ButtonNavigation
import com.ultraviolette.uvclusterhmi.ui.viewModel.CarViewModel
import com.ultraviolette.uvclusterhmi.utils.Utilities
import com.ultraviolette.uvclusterhmi.utils.Utilities.setOnSoundClickListener
import com.ultraviolette.uvclusterhmi.utils.ViewModelFactory
import kotlinx.coroutines.launch


class EmergencyFragment : Fragment() {
    private lateinit var ivBack: ImageView
    private lateinit var tvEmergencySOS: TextView
    private lateinit var clEmergency: ConstraintLayout
    //for bug no 46 - pop up exit on button press
    private lateinit var tvEmergencyContactNumber: TextView
    private var emergencySosDialog : AlertDialog? = null
    private val carViewModel by activityViewModels<CarViewModel> { ViewModelFactory(context = requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_emergency, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        initObserver()
        initClickListener()
    }

    private fun initObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
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

    fun handleButtonNavigation(button: Int) {
        when (button) {
            ButtonNavigation.Enter.ordinal -> {
                tvEmergencySOS.performClick()
            }

            ButtonNavigation.Back.ordinal -> {
                //for bug no 46 - pop up exit on button press
                if(emergencySosDialog?.isShowing == true){
                    emergencySosDialog?.dismiss()
                }else{
                    findNavController().navigate(R.id.myF77MenuFragment)
                }
            }
        }
    }

    /**
     * Binds UI components from the provided root view using their IDs.
     *
     * @param view The root view containing the layout elements.
     *
     * Initializes:
     * - tvBack ,tvEmergencySOS (TextViews)
     * - clEmergency (ConstraintLayout)
     */
    private fun initViews(view: View) {
        ivBack = view.findViewById(R.id.ivBack)
        tvEmergencySOS = view.findViewById(R.id.tvEmergencySOS)
        clEmergency = view.findViewById(R.id.clEmergency)
        tvEmergencyContactNumber=view.findViewById(R.id.tvEmergencyContactNumber)
    }

    /**
     * Initializes click listeners for UI components.
     */
    private fun initClickListener() {

        val gestureDetector = GestureDetector(
            requireContext(),
            object : GestureDetector.SimpleOnGestureListener() {

                override fun onDown(e: MotionEvent): Boolean {
                    return true
                }

                override fun onDoubleTap(e: MotionEvent): Boolean {
                    d("EmergencyFragment", "Double tap detected")
                    findNavController().navigate(R.id.versionsFragment)
                    return true
                }
            }
        )

        tvEmergencyContactNumber.apply {
            isClickable = true
            isFocusable = true

            setOnTouchListener { _, event ->
                gestureDetector.onTouchEvent(event)
                false
            }
        }

        tvEmergencySOS.setOnSoundClickListener(requireContext()) {
            showEmergencySosDialog()
        }

        ivBack.setOnSoundClickListener(requireContext()) {
            findNavController().navigate(R.id.myF77MenuFragment)
        }
    }

    /**
     * Displays an emergency SOS dialog with a blurred background (API 31+).
     *
     * Applies a blur effect to the root view while the dialog is visible,
     * and removes it when dismissed.
     */
    private fun showEmergencySosDialog() {
        // Blur the background
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val blur = RenderEffect.createBlurEffect(10f, 10f, Shader.TileMode.CLAMP)
            clEmergency.setRenderEffect(blur)
        }
        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.dialog_complete_message, null)
        val tvTitle = dialogView.findViewById<TextView>(R.id.tvTitle)
        val tvMessage = dialogView.findViewById<TextView>(R.id.tvMessage)
        //for bug no 46 - pop up exit on button press
        emergencySosDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setOnDismissListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    clEmergency.setRenderEffect(null)
                }
                emergencySosDialog = null
            }
            .create()
        tvTitle.text = getString(R.string.emergency_sos)
        tvMessage.text = getString(R.string.emergency_alert_sent_to_aalbin)
        emergencySosDialog?.show()
    }

}

