package com.ultraviolette.uvclusterhmi.ui.features.settings.display

import android.os.Bundle
import android.util.Log.d
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.ultraviolette.uvclusterhmi.R
import com.ultraviolette.uvclusterhmi.domain.ennumerate.ButtonNavigation
import com.ultraviolette.uvclusterhmi.ui.features.MainActivity
import com.ultraviolette.uvclusterhmi.ui.viewModel.SharedViewModel
import com.ultraviolette.uvclusterhmi.utils.Utilities.setOnSoundClickListener
import com.ultraviolette.uvclusterhmi.utils.ViewModelFactory
import java.time.LocalTime
import java.time.ZoneId

class DisplayFragment : Fragment() {
    private lateinit var tvBrightnessLevel: TextView
    private lateinit var tvAutoBrightness: TextView
    private lateinit var tvManualBrightness: TextView
    private lateinit var tvModeDay: TextView
    private lateinit var tvModeNight: TextView
    private lateinit var tvModeAuto: TextView
    private lateinit var tvAdjustBrightness: TextView
    private lateinit var ivDaySelectedRight: ImageView
    private lateinit var ivDaySelectedLeft: ImageView
    private lateinit var ivNightSelected: ImageView
    private lateinit var ivAutoModeSelected: ImageView
    private lateinit var ivAutoBrightnessSelected: ImageView
    private lateinit var ivManualBrightnessSelected: ImageView
    private lateinit var sbBrightness: SeekBar
    private lateinit var llBrightnessBar: LinearLayout
    private lateinit var scrollView: NestedScrollView
    private val sharedViewModel by activityViewModels<SharedViewModel> { ViewModelFactory(context = requireContext()) }
    private val viewModel by viewModels<DisplayViewModel> { ViewModelFactory(context = requireContext()) }
    private var clickedUiState: ClickedUiState = ClickedUiState.Brightness
    private var buttonClickedState: ButtonClickedState = ButtonClickedState.Brightness

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_display, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        initClickListener()
        d("ThemeChange","onViewCreated")
        initUi()
    }

    /**
     * Binds UI components from the provided root view using their IDs.
     *
     * @param view The root view containing the layout elements.
     *
     * Initializes:
     * - tvBrightnessLevel,tvAutoBrightness,tvManualBrightness,tvModeDay (TextViews)
     * - tvModeNight,tvModeAuto,tvAdjustBrightness (TextViews)
     * - ivDaySelectedRight,ivDaySelectedLeft,ivNightSelected,ivAutoModeSelected (ImageViews)
     * - ivAutoBrightnessSelected,ivManualBrightnessSelected,ivParallaxSelect,ivRadarSelect (ImageViews)
     * - sbBrightness (ScrollBar)
     * - llBrightnessBar (LinearLayout)
     */
    private fun initViews(view: View) {
        tvBrightnessLevel = view.findViewById(R.id.tvBrightnessLevel)
        tvAutoBrightness = view.findViewById(R.id.tvAutoBrightness)
        tvManualBrightness = view.findViewById(R.id.tvManualBrightness)
        tvModeDay = view.findViewById(R.id.tvModeDay)
        tvModeNight = view.findViewById(R.id.tvModeNight)
        tvModeAuto = view.findViewById(R.id.tvModeAuto)
        tvAdjustBrightness = view.findViewById(R.id.tvAdjustBrightness)
        ivDaySelectedRight = view.findViewById(R.id.ivDaySelectedRight)
        ivDaySelectedLeft = view.findViewById(R.id.ivDaySelectedLeft)
        ivNightSelected = view.findViewById(R.id.ivNightSelected)
        ivAutoModeSelected = view.findViewById(R.id.ivAutoModeSelected)
        ivAutoBrightnessSelected = view.findViewById(R.id.ivAutoBrightnessSelected)
        ivManualBrightnessSelected = view.findViewById(R.id.ivManualBrightnessSelected)
        sbBrightness = view.findViewById(R.id.sbBrightness)
        llBrightnessBar = view.findViewById(R.id.llBrightnessBar)
        scrollView = view.findViewById(R.id.scrollView)
    }

    fun handleButtonNavigation(button: Int) {
        when (button) {
            ButtonNavigation.Right.ordinal -> {
                when (buttonClickedState) {
                    ButtonClickedState.Brightness -> {
                        handleButtonNavigationBrightnessHorizontal()
                    }

                    ButtonClickedState.AdjustBrightness -> {
                        val progress = (viewModel.brightnessLevel + 10).coerceAtMost(100)
                        viewModel.saveBrightness(progress)
                        sbBrightness.progress = progress
                        tvBrightnessLevel.text = getString(R.string.brightness_level, progress)
                        (activity as MainActivity).checkSettingAndBrightness(progress)
                    }

                    ButtonClickedState.Mode -> {
                        when (viewModel.mode) {
                            getString(R.string.auto) -> tvModeDay.performClick()
                            getString(R.string.day) -> tvModeNight.performClick()
                            getString(R.string.night) -> tvModeAuto.performClick()
                        }
                    }
                }
            }

            ButtonNavigation.Left.ordinal -> {
                when (buttonClickedState) {
                    ButtonClickedState.Brightness -> {
                        handleButtonNavigationBrightnessHorizontal()
                    }


                    ButtonClickedState.AdjustBrightness -> {
                        val progress = (viewModel.brightnessLevel - 10).coerceAtLeast(0)
                        viewModel.saveBrightness(progress)
                        sbBrightness.progress = progress
                        tvBrightnessLevel.text = getString(R.string.brightness_level, progress)
                        (activity as MainActivity).checkSettingAndBrightness(progress)
                    }

                    ButtonClickedState.Mode -> {
                        when (viewModel.mode) {
                            getString(R.string.auto) -> tvModeNight.performClick()
                            getString(R.string.day) -> tvModeAuto.performClick()
                            getString(R.string.night) -> tvModeDay.performClick()
                        }
                    }

                }
            }

            ButtonNavigation.Top.ordinal -> {
                when (buttonClickedState) {
                    ButtonClickedState.Brightness -> {
                        handleButtonNavigationModeVertical()
                    }

                    ButtonClickedState.AdjustBrightness -> {
                        handleBrightnessSeek(R.color.white, R.drawable.thumb_white)
                        handleButtonNavigationBrightnessVertical()
                    }


                    ButtonClickedState.Mode -> {
                        buttonClickedState =
                            if (viewModel.isAutoBrightnessEnabled) ButtonClickedState.Brightness
                            else ButtonClickedState.AdjustBrightness
                        if (viewModel.isAutoBrightnessEnabled) {
                            handleButtonNavigationBrightnessVertical()
                        } else {
                            handleNavigationAdjustBrightnessVertical()
                        }
                    }
                }
            }

            ButtonNavigation.Bottom.ordinal -> {
                when (buttonClickedState) {
                    ButtonClickedState.Brightness -> {
                        buttonClickedState =
                            if (viewModel.isAutoBrightnessEnabled) ButtonClickedState.Mode
                            else ButtonClickedState.AdjustBrightness
                        if (viewModel.isAutoBrightnessEnabled) {
                            handleButtonNavigationModeVertical()
                        } else {
                            handleNavigationAdjustBrightnessVertical()
                        }
                    }

                    ButtonClickedState.AdjustBrightness -> {
                        handleButtonNavigationModeVertical()
                        handleBrightnessSeek(R.color.white, R.drawable.thumb_white)
                    }

                    ButtonClickedState.Mode -> {
                        handleButtonNavigationBrightnessVertical()
                    }
                }
            }

            ButtonNavigation.Back.ordinal -> {
                resetBrightness()
                resetTheme()
                resetMode()
                if (!viewModel.isAutoBrightnessEnabled) {
                    handleBrightnessSeek(R.color.white, R.drawable.thumb_white)
                }

            }
        }
    }

    private fun handleButtonNavigationBrightnessHorizontal() {
        if (viewModel.isAutoBrightnessEnabled) {
            tvManualBrightness.performClick()
        } else {
            tvAutoBrightness.performClick()
        }
    }


    private fun handleButtonNavigationBrightnessVertical() {
        scrollView.post {
            scrollView.fullScroll(View.FOCUS_UP)
        }
        buttonClickedState = ButtonClickedState.Brightness
        if (viewModel.isAutoBrightnessEnabled) tvAutoBrightness.performClick() else tvManualBrightness.performClick()
    }

    private fun handleButtonNavigationModeVertical() {
        buttonClickedState = ButtonClickedState.Mode
        scrollView.post {
            scrollView.fullScroll(View.FOCUS_DOWN)
        }
        when (viewModel.mode) {
            getString(R.string.day) -> tvModeDay.performClick()
            getString(R.string.night) -> tvModeNight.performClick()
            getString(R.string.auto) -> tvModeAuto.performClick()
        }
    }

    /**
     * Initialize the UI components.
     */
    private fun initUi() {
        sbBrightness.progress = viewModel.brightnessLevel
        handleBrightnessSeek(R.color.white, R.drawable.thumb_white)
        tvBrightnessLevel.text = getString(R.string.brightness_level, viewModel.brightnessLevel)
       /* if (sharedViewModel.hasThemeConfigChanged) {
            sharedViewModel.hasThemeConfigChanged = false
            clickedUiState = ClickedUiState.Mode
        }*/
        handleUi()
    }

    /**
     * Initialize click listeners for UI components.
     */
    private fun initClickListener() {
        tvAutoBrightness.setOnSoundClickListener(requireContext()) {
            buttonClickedState = ButtonClickedState.Brightness
            sharedViewModel.handleSettingsChildClick(true)
            viewModel.saveBrightnessState(true)
            clickedUiState = ClickedUiState.Brightness
            handleUi()
        }

        tvManualBrightness.setOnSoundClickListener(requireContext()) {
            buttonClickedState = ButtonClickedState.Brightness
            viewModel.saveBrightnessState(false)
            sharedViewModel.handleSettingsChildClick(true)
            clickedUiState = ClickedUiState.Brightness
            handleUi()

        }
        sbBrightness.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    buttonClickedState = ButtonClickedState.AdjustBrightness
                }
                if (!viewModel.isAutoBrightnessEnabled) {
                    handleNavigationAdjustBrightnessVertical()
                    viewModel.saveBrightness(progress)
                    tvBrightnessLevel.text =
                        getString(R.string.brightness_level, viewModel.brightnessLevel)
                    viewModel.saveBrightness(viewModel.brightnessLevel)
                    (activity as? MainActivity)?.checkSettingAndBrightness(viewModel.brightnessLevel)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                sharedViewModel.handleSettingsChildClick(true)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                handleBrightnessSeek(R.color.white, R.drawable.thumb_white)
            }
        })

        tvModeDay.setOnSoundClickListener(requireContext()) {
            buttonClickedState = ButtonClickedState.Mode
            sharedViewModel.handleSettingsChildClick(true)
            changeMode(getString(R.string.day))
        }
        tvModeNight.setOnSoundClickListener(requireContext()) {
            buttonClickedState = ButtonClickedState.Mode
            sharedViewModel.handleSettingsChildClick(true)
            changeMode(getString(R.string.night))
        }

        tvModeAuto.setOnSoundClickListener(requireContext()) {
            buttonClickedState = ButtonClickedState.Mode
            changeMode(getString(R.string.auto))
            sharedViewModel.handleSettingsChildClick(true)
        }
    }

    private fun handleNavigationAdjustBrightnessVertical() {
        resetBrightness()
        resetMode()
        resetTheme()
        handleBrightnessSeek(
            R.color.activeSelectionRed,
            R.drawable.ic_seekbar_thumb_1
        )
    }

    /**
     * Change theme
     */
    private fun changeMode(mode: String) {
        viewModel.saveMode(mode)
        when (mode) {
            getString(R.string.day) -> {
                if (AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_NO) {
                    sharedViewModel.hasThemeConfigChanged = true
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
            }

            getString(R.string.night) -> {
                if (AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_YES) {
                    sharedViewModel.hasThemeConfigChanged = true
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }
            }

            getString(R.string.auto) -> {
             /*   if (AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
                    sharedViewModel.hasThemeConfigChanged = true
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                }*/
                if (
                    LocalTime.now(ZoneId.of("Asia/Kolkata")).let {
                        it.isAfter(LocalTime.of(6, 0)) && it.isBefore(LocalTime.of(18, 0))
                    }
                ) {
                    if (AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_NO) {
                        sharedViewModel.hasThemeConfigChanged = true
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    }

                }else {
                    if (AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_YES) {
                        sharedViewModel.hasThemeConfigChanged = true
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    }
                }
            }
        }
        clickedUiState = ClickedUiState.Mode
        handleUi()
    }


    /**
     * Update the UI based on the current clicked state.
     */
    private fun handleUi() {
        d("ThemeChange","handleUi")
        when (clickedUiState) {
            ClickedUiState.Brightness -> {
                handleBrightnessClick()
                resetMode()
                resetTheme()
            }

            ClickedUiState.Mode -> {
                handleModeClick()
                resetBrightness()
                resetTheme()
            }
        }
    }

    /**
     * Update the UI based on the current brightness state.
     */
    private fun handleBrightnessClick() {
        ivAutoBrightnessSelected.isVisible = viewModel.isAutoBrightnessEnabled
        ivManualBrightnessSelected.isVisible = !viewModel.isAutoBrightnessEnabled
        tvAdjustBrightness.isVisible = !viewModel.isAutoBrightnessEnabled
        llBrightnessBar.isVisible = !viewModel.isAutoBrightnessEnabled
        tvBrightnessLevel.isVisible = !viewModel.isAutoBrightnessEnabled
        val selectedTextColor = ContextCompat.getColor(requireContext(), R.color.white)
        val selectedBackgroundColor =
            ContextCompat.getColor(requireContext(), R.color.activeSelectionRed)
        val unselectedTextColor = ContextCompat.getColor(requireContext(), R.color.unSelected)
        val unselectedBackgroundColor =
            ContextCompat.getColor(requireContext(), R.color.transparent)
        tvAutoBrightness.setTextColor(if (viewModel.isAutoBrightnessEnabled) selectedTextColor else unselectedTextColor)
        tvAutoBrightness.setBackgroundColor(if (viewModel.isAutoBrightnessEnabled) selectedBackgroundColor else unselectedBackgroundColor)
        tvManualBrightness.setTextColor(if (!viewModel.isAutoBrightnessEnabled) selectedTextColor else unselectedTextColor)
        tvManualBrightness.setBackgroundColor(if (!viewModel.isAutoBrightnessEnabled) selectedBackgroundColor else unselectedBackgroundColor)
    }

    /**
     * Reset the UI to its initial state.
     */
    private fun resetBrightness() {
        ivManualBrightnessSelected.isVisible = false
        ivAutoBrightnessSelected.isVisible = false
        tvAdjustBrightness.isVisible = !viewModel.isAutoBrightnessEnabled
        llBrightnessBar.isVisible = !viewModel.isAutoBrightnessEnabled
        tvBrightnessLevel.isVisible = !viewModel.isAutoBrightnessEnabled
        val selectedTextColor = ContextCompat.getColor(
            requireContext(),
            R.color.black
        )
        val selectedBackgroundColor = ContextCompat.getColor(
            requireContext(),
            R.color.white
        )
        val unselectedTextColor = ContextCompat.getColor(requireContext(), R.color.unSelected)
        val unselectedBackgroundColor =
            ContextCompat.getColor(requireContext(), R.color.transparent)
        tvManualBrightness.setTextColor(if (!viewModel.isAutoBrightnessEnabled) selectedTextColor else unselectedTextColor)
        tvManualBrightness.setBackgroundColor(if (!viewModel.isAutoBrightnessEnabled) selectedBackgroundColor else unselectedBackgroundColor)
        tvAutoBrightness.setTextColor(if (viewModel.isAutoBrightnessEnabled) selectedTextColor else unselectedTextColor)
        tvAutoBrightness.setBackgroundColor(if (viewModel.isAutoBrightnessEnabled) selectedBackgroundColor else unselectedBackgroundColor)
    }

    /**
     * Update the UI based on the current theme state.
     */
    private fun handleThemeClick() {
        val selectedTextColor = ContextCompat.getColor(requireContext(), R.color.white)
        val selectedBackgroundColor =
            ContextCompat.getColor(requireContext(), R.color.activeSelectionRed)
        val unselectedTextColor = ContextCompat.getColor(requireContext(), R.color.unSelected)
        val unselectedBackgroundColor =
            ContextCompat.getColor(requireContext(), R.color.transparent)
    }

    /**
     * Reset the UI to its initial state.
     */
    private fun resetTheme() {
        val selectedTextColor = ContextCompat.getColor(
            requireContext(),
            R.color.black
        )
        val selectBackgroundColor = ContextCompat.getColor(
            requireContext(),
            R.color.white
        )
        val unselectedTextColor = ContextCompat.getColor(requireContext(), R.color.unSelected)
        val unselectedBackgroundColor =
            ContextCompat.getColor(requireContext(), R.color.transparent)
    }

    /**
     * Update the UI based on the current mode state.
     */
    private fun handleModeClick() {
        d("Theme","handleModeClick")
        val selectedTextColor = ContextCompat.getColor(requireContext(), R.color.white)
        val selectedBackgroundColor =
            ContextCompat.getColor(requireContext(), R.color.activeSelectionRed)
        val unselectedTextColor = ContextCompat.getColor(requireContext(), R.color.unSelected)
        val unselectedBackgroundColor =
            ContextCompat.getColor(requireContext(), R.color.transparent)
        ivAutoModeSelected.isVisible = viewModel.mode == getString(R.string.auto)
        ivDaySelectedLeft.isVisible = viewModel.mode == getString(R.string.day)
        ivDaySelectedRight.isVisible = viewModel.mode == getString(R.string.day)
        ivNightSelected.isVisible = viewModel.mode == getString(R.string.night)
        changeModeUi(
            selectedTextColor,
            selectedBackgroundColor,
            unselectedTextColor,
            unselectedBackgroundColor
        )
    }


    /**
     * Reset the UI to its initial state.
     */
    private fun resetMode() {
        d("ThemeChange","resetMode")
        ivAutoModeSelected.isVisible = false
        ivDaySelectedLeft.isVisible = false
        ivDaySelectedRight.isVisible = false
        ivNightSelected.isVisible = false
        val selectedTextColor = ContextCompat.getColor(
            requireContext(),
            R.color.black
        )
        val selectedBackgroundColor = ContextCompat.getColor(
            requireContext(),
            R.color.white
        )
        val unselectedTextColor = ContextCompat.getColor(requireContext(), R.color.unSelected)
        val unselectedBackgroundColor =
            ContextCompat.getColor(requireContext(), R.color.transparent)
        changeModeUi(
            selectedTextColor,
            selectedBackgroundColor,
            unselectedTextColor,
            unselectedBackgroundColor
        )
    }

    /**
     * Update the UI based on the current mode state.
     */
    private fun changeModeUi(
        selectedTextColor: Int,
        selectedBackgroundColor: Int,
        unselectedTextColor: Int,
        unselectedBackgroundColor: Int
    ) {
        tvModeAuto.setTextColor(if (viewModel.mode == getString(R.string.auto)) selectedTextColor else unselectedTextColor)
        tvModeAuto.setBackgroundColor(if (viewModel.mode == getString(R.string.auto)) selectedBackgroundColor else unselectedBackgroundColor)
        tvModeDay.setTextColor(if (viewModel.mode == getString(R.string.day)) selectedTextColor else unselectedTextColor)
        tvModeDay.setBackgroundColor(if (viewModel.mode == getString(R.string.day)) selectedBackgroundColor else unselectedBackgroundColor)
        tvModeNight.setTextColor(if (viewModel.mode == getString(R.string.night)) selectedTextColor else unselectedTextColor)
        tvModeNight.setBackgroundColor(if (viewModel.mode == getString(R.string.night)) selectedBackgroundColor else unselectedBackgroundColor)
    }


    /**
     * Updates the SeekBar's progress drawable and thumb drawable tint.
     *
     * @param drawableTint Resource ID of the color to apply to the progress drawable.
     * @param thumbTint Resource ID of the drawable to set as the SeekBar thumb.
     */
    private fun handleBrightnessSeek(drawableTint: Int, thumbTint: Int) {
        DrawableCompat.setTint(
            DrawableCompat.wrap(sbBrightness.progressDrawable),
            ContextCompat.getColor(requireContext(), drawableTint)
        )
        sbBrightness.thumb = ContextCompat.getDrawable(requireContext(), thumbTint)
    }

    /**
     * Clicked UI state.
     */
    sealed class ClickedUiState() {
        object Brightness : ClickedUiState()
        object Mode : ClickedUiState()
    }

    enum class ButtonClickedState {
        Brightness,
        AdjustBrightness,
        Mode
    }
}
