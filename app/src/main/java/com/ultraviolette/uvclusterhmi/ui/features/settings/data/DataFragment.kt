package com.ultraviolette.uvclusterhmi.ui.features.settings.data

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.UserHandle
import android.util.Log
import android.util.Log.d
import android.util.Log.e
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ultraviolette.uvclusterhmi.R
import com.ultraviolette.uvclusterhmi.domain.ennumerate.ButtonNavigation
import com.ultraviolette.uvclusterhmi.ui.viewModel.SharedViewModel
import com.ultraviolette.uvclusterhmi.utils.Utilities.setOnSoundClickListener
import com.ultraviolette.uvclusterhmi.utils.ViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DataFragment : Fragment() {
    private lateinit var tvDataOn: TextView
    private lateinit var tvDataOff: TextView
    private lateinit var ivDataOnSelect: ImageView
    private lateinit var ivDataOffSelect: ImageView
    private lateinit var tvCollectLog: TextView
    private lateinit var tvHabitatreset: TextView
    private val TAG = "DataFragment"
    private val sharedViewModel by activityViewModels<SharedViewModel> { ViewModelFactory(context = requireContext()) }
    private val viewModel by activityViewModels<DataViewModel> { ViewModelFactory(context = requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_data, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        observeDataState()
        initClickListener()
    }

    private fun observeDataState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.onDataStateChange.collect { (state, isButton) ->
                        d("faizulla","state :$state button:$isButton")
                        handleDataUi(state, isButton)
                    }
                }
            }
        }
    }

    fun handleButtonNavigation(button: Int) {
        if (button == ButtonNavigation.Back.ordinal) {
            d("faizulla","back is called")
            viewModel.stateChange(true)
            return
        }
    }

    /**
     * Binds UI components from the provided root view using their IDs.
     *
     * @param view The root view containing the layout elements.
     *
     * Initializes:
     * - tvDataOn,tvDataOn (TextViews)
     * - ivDataOnSelect,ivDataOffSelect (ImageViews)
     */
    private fun initViews(view: View) {
        tvDataOn = view.findViewById(R.id.tvDataOn)
        tvDataOff = view.findViewById(R.id.tvDataOff)
        ivDataOnSelect = view.findViewById(R.id.ivDataOnSelect)
        ivDataOffSelect = view.findViewById(R.id.ivDataOffSelect)
        tvCollectLog = view.findViewById(R.id.tvCollectLog)
        tvHabitatreset = view.findViewById(R.id.tvHabitatreset)
    }

    /**
     * Initializes click listeners for UI components.
     */
    private fun initClickListener() {
        tvDataOn.setOnSoundClickListener(requireContext()) {
            sharedViewModel.handleSettingsChildClick(true)
            viewModel.setDataState(true)
        }
        tvDataOff.setOnSoundClickListener(requireContext()) {
            sharedViewModel.handleSettingsChildClick(true)
            viewModel.setDataState(true)
        }
        tvCollectLog.setOnSoundClickListener(requireContext()) {
            startLogging(requireContext())
        }
        tvHabitatreset.setOnSoundClickListener(requireContext()) {
            val intent = Intent("com.example.database.ACTION_RETAIN_LOGS").apply {
                setPackage("com.example.database")
//              //putExtra("used_pct", usedPct)
            }

            try {
                //requireContext().sendBroadcastAsUser(intent, UserHandle.ALL)
                d(TAG, "✅ Broadcast SENT successfully")
            } catch (e: Exception) {
                e(TAG, "❌ Broadcast FAILED", e)
            }
        }
    }

    /** Update the UI based on mobile data setting.*/
    private fun handleDataUi(isEnabled: Boolean, isButtonNavigation: Boolean) {
        d("faizulla","handleDataUi isEnabled:$isEnabled isButtonNavigation:$isButtonNavigation")
        ivDataOnSelect.isVisible = isEnabled && !isButtonNavigation
        ivDataOffSelect.isVisible = !isEnabled && !isButtonNavigation
        val selectedBackgroundColor =
            ContextCompat.getColor(
                requireContext(),
                if (isButtonNavigation) R.color.white else R.color.activeSelectionRed
            )
        val selectedTextColor = ContextCompat.getColor(
            requireContext(),
            if (isButtonNavigation) R.color.black else R.color.white
        )
        val unselectedBackgroundColor =
            ContextCompat.getColor(requireContext(), R.color.transparent)
        val unselectedTextColor = ContextCompat.getColor(requireContext(), R.color.unSelected)
        tvDataOn.setTextColor(if (isEnabled) selectedTextColor else unselectedTextColor)
        tvDataOff.setTextColor(if (!isEnabled) selectedTextColor else unselectedTextColor)
        tvDataOn.setBackgroundColor(if (isEnabled) selectedBackgroundColor else unselectedBackgroundColor)
        tvDataOff.setBackgroundColor(if (!isEnabled) selectedBackgroundColor else unselectedBackgroundColor)
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