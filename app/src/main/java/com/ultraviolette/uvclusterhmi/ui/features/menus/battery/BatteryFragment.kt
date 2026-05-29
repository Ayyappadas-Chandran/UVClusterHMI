package com.ultraviolette.uvclusterhmi.ui.features.menus.battery

import android.os.Bundle
import android.util.Log.d
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.ultraviolette.uvclusterhmi.ClusterApplication
import com.ultraviolette.uvclusterhmi.R
import com.ultraviolette.uvclusterhmi.domain.ennumerate.ButtonNavigation
import com.ultraviolette.uvclusterhmi.domain.model.ClusterUiState
import com.ultraviolette.uvclusterhmi.ui.viewModel.ClusterViewModel
import com.ultraviolette.uvclusterhmi.ui.viewModel.SharedViewModel
import com.ultraviolette.uvclusterhmi.utils.Utilities
import com.ultraviolette.uvclusterhmi.utils.Utilities.applyMinMax
import com.ultraviolette.uvclusterhmi.utils.Utilities.setOnSoundClickListener
import com.ultraviolette.uvclusterhmi.utils.ViewModelFactory
import kotlinx.coroutines.launch
import kotlin.getValue

class BatteryFragment : Fragment() {
    private lateinit var tvBatteryLimitPercent: TextView
    private lateinit var tvBatterPercent : TextView
    private lateinit var tvWinterMode: TextView
    private lateinit var ivBack: ImageView
    private lateinit var ivBatteryLimitTriangle: ImageView
    private lateinit var sbBatteryLimit: SeekBar
    private lateinit var pbBatteryLevel : ProgressBar
    private val sharedViewModel by activityViewModels<SharedViewModel> { ViewModelFactory(context = requireContext()) }
    private val viewModel: BatteryViewModel by viewModels{ ViewModelFactory(context = requireContext()) }
    private val clusterViewModel: ClusterViewModel by activityViewModels {
        ClusterViewModel.Factory(requireActivity().application as ClusterApplication)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_battery, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        initClickListener()
        observeBatteryLimit()
        if (viewModel.batteryUiState.value.isWinterMode) {
            resetUi()
            tvWinterMode.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(), R.color.activeSelectionRed
                )
            )
        }
    }

    private fun observeBatteryLimit() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.batteryUiState.collect {
                        renderUi(it)
                    }
                }
                launch {
                    clusterViewModel.uiState.collect { uiState ->
                        val active = uiState as? ClusterUiState.Active ?: return@collect
                        val soc = active.toolbar.batterySoc
                        tvBatterPercent.text = "$soc %"
                        pbBatteryLevel.progress = soc
                    }
                }


                launch {
                    // Handlebar events via ClusterDataBus (replaces carViewModel.swiftButton)
                    clusterViewModel.handlebarButton.collect { swiftButton ->
                        val button = Utilities.getButtonState(swiftButton)
                        if (button == ButtonNavigation.None) return@collect
                        handleButtonNavigation(button.ordinal)
                    }
                }
            }
        }
    }

    private fun renderUi(uiState: BatteryUiState) {
        updateBatteryUI(uiState.batteryLimit)
        resetUi()
        if (uiState.isWinterMode) handleWinterModeSelect() else setSeekbarUi()
    }

    private fun updateBatteryUI(progress: Int) {
        sbBatteryLimit.progress = progress
        tvBatteryLimitPercent.text = buildString {
            append("Limit ")
            append(progress)
            append("%")
        }
        updateBatteryLimitIndicatorPosition()
    }

    fun handleButtonNavigation(button: Int) {
        val uiState = viewModel.batteryUiState.value
        val isWinterMode = uiState.isWinterMode
        when (button) {
            ButtonNavigation.Bottom.ordinal -> {
                if (isWinterMode) return
                viewModel.updateBatteryUi(true)
            }

            ButtonNavigation.Top.ordinal -> {
                if (!isWinterMode) return
                viewModel.updateBatteryUi(false)
            }

            ButtonNavigation.Right.ordinal -> {
                if (isWinterMode) return
                setSeekbarUi()
                val buttonProgress = (sbBatteryLimit.progress + 10).coerceAtMost(100)
                viewModel.updateBatteryLimitChange(buttonProgress)
            }

            ButtonNavigation.Left.ordinal -> {
                if (isWinterMode) return
                setSeekbarUi()
                val buttonProgress = (sbBatteryLimit.progress - 10).coerceAtLeast(60)
                viewModel.updateBatteryLimitChange(buttonProgress)
            }

            ButtonNavigation.Enter.ordinal -> {
                if (!isWinterMode) return
               // findNavController().navigate(R.id.winterModeFragment)
            }

            ButtonNavigation.Back.ordinal -> {
                findNavController().navigateUp()
            }
        }
    }

    /**
     * Binds UI components from the provided root view using their IDs.
     *
     * @param view The root view containing the layout elements.
     *
     * Initializes:
     * - tvBatteryLimitPercent,tvWinterMode,tvBack (TextViews)
     * - ivBatteryLimitTriangle (ImageView)
     * - sbBatteryLimit (Scrollbar)
     */
    private fun initViews(view: View) {
        tvBatteryLimitPercent = view.findViewById(R.id.tvBatteryLimitPercent)
        tvBatterPercent = view.findViewById(R.id.tvBatteryPercent)
        tvWinterMode = view.findViewById(R.id.tvWinterMode)
        ivBack = view.findViewById(R.id.ivBack)
        ivBatteryLimitTriangle = view.findViewById(R.id.ivBatteryLimitTriangle)
        sbBatteryLimit = view.findViewById(R.id.sbBatteryLimit)
        pbBatteryLevel = view.findViewById(R.id.pbBatteryLevel)
    }

    /**
     * Aligns a triangle indicator (`ivBatteryLimitTriangle`) with the current progress of the battery limit SeekBar.
     *
     * This method should be called *after layout is drawn* (i.e., using `post {}`),
     * because it depends on actual view dimensions (`seekBar.width` and `image.width`).
     */
    private fun updateBatteryLimitIndicatorPosition() {
        sbBatteryLimit.post {
            val seekBar = sbBatteryLimit
            val triangle = ivBatteryLimitTriangle
            val progress = seekBar.progress
            val clampedProgress = if (progress < 60) {
                seekBar.progress = 60
                60
            } else {
                progress
            }
            val max = seekBar.max
            val seekBarWidth = seekBar.width - seekBar.paddingLeft - seekBar.paddingRight
            val triangleWidth = triangle.width
            val ratio = clampedProgress.toFloat() / max
            val positionX = seekBar.paddingLeft + (ratio * seekBarWidth) - triangleWidth / 2
            triangle.translationX = positionX
        }
    }

    /**
     * Initializes click listeners for UI components.
     */
    private fun initClickListener() {
        ivBack.setOnSoundClickListener(requireContext()) {
            findNavController().navigateUp()
        }

        /*tvWinterMode.setOnSoundClickListener(requireContext()) {
            viewModel.updateBatteryUi(true)
            findNavController().navigate(R.id.action_batteryFragment_to_winterModeFragment)
        }*/

        sbBatteryLimit.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {

                    val progressValue = when(progress){
                        in 0..80 -> 80
                        in 81..90 -> 90
                        in 91..100 -> 100
                        else -> 80
                    }

                    val progress = progress.coerceAtLeast(60)
                    seekBar.progress = progressValue
                    viewModel.updateBatteryLimitChange(progressValue)
                    viewModel.updateBatteryUi(false)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                setSeekbarUi()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })


    }

    private fun setSeekbarUi() {
        d("BatterySeekbar","setSeekbarUi")
        ivBatteryLimitTriangle.setColorFilter(
            ContextCompat.getColor(
                requireContext(), R.color.activeSelectionRed
            )
        )
        sbBatteryLimit.thumb =
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.ic_battery_limit_line_red
            )

        tvWinterMode.setBackgroundDrawable(
            ContextCompat.getDrawable(
                requireContext(), R.drawable.bg_grey_stroke
            )
        )
        tvWinterMode.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )
    }

    private fun handleWinterModeSelect() {
        resetUi()
        tvWinterMode.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(), R.color.activeSelectionRed
            )
        )
        tvWinterMode.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
    }

    /**
     * Resets the winter mode UI to its default (unselected) state.
     *
     * - Tints battery limit indicators with unselected color.
     * - Applies a grey stroke background to the winter mode label.
     */
    private fun resetUi() {
        ivBatteryLimitTriangle.setColorFilter(
            ContextCompat.getColor(
                requireContext(), R.color.white
            )
        )
        sbBatteryLimit.thumb =
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_battery_limit_line)
        tvWinterMode.setBackgroundDrawable(
            ContextCompat.getDrawable(
                requireContext(), R.drawable.bg_grey_stroke
            )
        )
    }
}

