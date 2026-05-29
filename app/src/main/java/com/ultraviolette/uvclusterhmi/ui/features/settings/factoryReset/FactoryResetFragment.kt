package com.ultraviolette.uvclusterhmi.ui.features.settings.factoryReset

import android.content.Context
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.util.Log.d
import android.util.Log.e
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.ultraviolette.uvclusterhmi.R
import com.ultraviolette.uvclusterhmi.domain.ennumerate.ButtonNavigation
import com.ultraviolette.uvclusterhmi.ui.viewModel.SharedViewModel
import com.ultraviolette.uvclusterhmi.utils.Utilities.setOnSoundClickListener
import com.ultraviolette.uvclusterhmi.utils.ViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FactoryResetFragment : Fragment() {
    private lateinit var tvReset: TextView
    private lateinit var clFactoryReset: ConstraintLayout
    //for bug no 46 - pop up exit on button press
    private var factoryResetDialog : AlertDialog ?= null
    private val viewModel by activityViewModels<SharedViewModel> { ViewModelFactory(context = requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_factory_reset, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        initClickListener()
    }

    fun handleButtonNavigation(button: Int) {
        when (button) {
            ButtonNavigation.Enter.ordinal -> {
                tvReset.performClick()
            }

            ButtonNavigation.Back.ordinal -> {
                //for bug no 46 - pop up exit on button press
                if(factoryResetDialog?.isShowing == true){
                    factoryResetDialog?.dismiss()
                }else{
                   findNavController().navigateUp()
                }
                tvReset.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.transparent
                    )
                )
                tvReset.setTextColor(ContextCompat.getColor(requireContext(), R.color.unSelected))
            }
        }
    }

    /**
     * Binds UI components from the provided root view using their IDs.
     *
     * @param view The root view containing the layout elements.
     *
     * Initializes:
     * - tvReset (TextView)
     * - clFactoryReset (ConstraintLayout)
     */
    private fun initViews(view: View) {
        tvReset = view.findViewById(R.id.tvReset)
        clFactoryReset = view.findViewById(R.id.clFactoryReset)
    }

    /**
     * Initialize click listeners for UI components.
     */
    private fun initClickListener() {
        tvReset.setOnSoundClickListener(requireContext()) {
            viewModel.handleSettingsChildClick(true)
            tvReset.apply {
                setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.activeSelectionRed
                    )
                )
                setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            }
            //showFactoryResetDialog()
	    startLogging(requireContext())
        }
    }

    /**
     * Displays a confirmation dialog indicating factory reset completion.
     *
     * Applies a blur effect to the background (Android 12+)
     * while the dialog is shown and removes it on dismissal.
     */
    private fun showFactoryResetDialog() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            viewModel.handleSettingsBlur(true)
            val blur = RenderEffect.createBlurEffect(4f, 4f, Shader.TileMode.CLAMP)
            clFactoryReset.setRenderEffect(blur)
        }
        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.dialog_complete_message, null)
        val tvTitle = dialogView.findViewById<TextView>(R.id.tvTitle)
        val tvMessage = dialogView.findViewById<TextView>(R.id.tvMessage)
        tvTitle.text = getString(R.string.factory_reset)
        tvMessage.text = getString(R.string.reset_done)
        //for bug no 46 - pop up exit on button press
        factoryResetDialog = AlertDialog.Builder(requireContext())
            .setCancelable(true)
            .setView(dialogView)
            .setOnDismissListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    viewModel.handleSettingsBlur(false)
                    clFactoryReset.setRenderEffect(null)
                }
                factoryResetDialog = null
            }
            .create()
        factoryResetDialog?.show()
    }
    fun startLogging(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val process = Runtime.getRuntime().exec(arrayOf("sh", "/system/etc/onDemand_logcat.sh"))

                // Read stdout
                val stdout = process.inputStream.bufferedReader().readText()
                // Read stderr — this is where your error will be hiding
                val stderr = process.errorStream.bufferedReader().readText()

                val exitCode = process.waitFor()

                d("LOGGING", "Exit code: $exitCode")
                d("LOGGING", "stdout: $stdout")
                e("LOGGING", "stderr: $stderr")  // <-- Check this in logcat
            } catch (e: Exception) {
                e("LOGGING", "Exception: ${e.message}", e)
            }
        }
    }
}
