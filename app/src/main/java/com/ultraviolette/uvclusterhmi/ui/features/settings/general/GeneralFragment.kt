package com.ultraviolette.uvclusterhmi.ui.features.settings.general

import android.os.Bundle
import android.util.Log.d
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.ultraviolette.uvclusterhmi.R
import com.ultraviolette.uvclusterhmi.domain.dataModel.TimeZoneItem
import com.ultraviolette.uvclusterhmi.domain.ennumerate.ButtonNavigation
import com.ultraviolette.uvclusterhmi.ui.viewModel.CarViewModel
import com.ultraviolette.uvclusterhmi.ui.viewModel.SharedViewModel
import com.ultraviolette.uvclusterhmi.utils.Utilities.setOnSoundClickListener
import com.ultraviolette.uvclusterhmi.utils.ViewModelFactory

class GeneralFragment : Fragment() {
    private lateinit var tvManualTimeZone: TextView
    private lateinit var tvAutoTimeZone: TextView
    private lateinit var tvTimeZone: TextView
    private lateinit var tvLanguage: TextView
    private lateinit var tvZone: TextView
    private lateinit var tvTime: TextView
    private lateinit var tvTimeFormat: TextView
    private lateinit var tvNormalTimeFormat: TextView
    private lateinit var tvRailwayTimeFormat: TextView
    private lateinit var tvChargeTypeOn: TextView
    private lateinit var tvChargeTypeOff: TextView
    private lateinit var tvDistanceUnitKm: TextView
    private lateinit var tvDistanceUnitMiles: TextView
    private lateinit var ivNormalTimeFormatSelected: ImageView
    private lateinit var ivRailwayTimeFormatSelected: ImageView
    private lateinit var ivManualTimeZoneSelected: ImageView
    private lateinit var ivAutoTimeZoneSelected: ImageView
    private lateinit var ivChargeTyeOn: ImageView
    private lateinit var ivChargeTyeOff: ImageView
    private lateinit var ivDistanceUnitKm: ImageView
    private lateinit var ivDistanceUnitMiles: ImageView
    private lateinit var llTimeFormat: LinearLayout
    private lateinit var clLanguage: ConstraintLayout
    private lateinit var clTimeZone: ConstraintLayout
    private lateinit var clTimeZoneValue: ConstraintLayout
    private lateinit var clDistanceUnit: ConstraintLayout
    private val sharedViewModel by activityViewModels<SharedViewModel> { ViewModelFactory(context = requireContext()) }
    private val viewModel by viewModels<GeneralViewModel> { ViewModelFactory(context = requireContext()) }
    private val carViewModel by activityViewModels<CarViewModel> { ViewModelFactory(context = requireContext()) }
    private var clickedUiState: ClickedUiState = ClickedUiState.DateAndTime
    private var isLanguageClicked = true
    private var isTimeZoneClicked = true
    private var isTimeFormatClicked = true
    private var isDateTimeClicked = true
    private var isChargeClicked = true
    private var isDistanceUnitClicked = true
    private var buttonClickedState: ButtonClickedState = ButtonClickedState.DateAndTime
    private lateinit var scrollView: NestedScrollView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_general, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        initClickListener()
        initObserver()
        handleUiState()
    }

    /**
     * Binds UI components from the provided root view using their IDs.
     *
     * @param view The root view containing the layout elements.
     *
     * Initializes:
     * - tvManualTimeZone,tvAutoTimeZone,tvLanguage,tvTimeZone,tvTime,tvTimeFormat,tvNormalTimeFormat,tvRailwayTimeFormat (TextViews)
     * - ivManualTimeZoneSelected,ivAutoTimeZoneSelected,ivRailwayTimeFormatSelected,ivNormalTimeFormatSelected (ImageViews)
     * - clLanguage,clTimeZoneValue,clTimeZone (ConstraintLayout)
     * - llTimeFormat (LinearLayout)
     */
    private fun initViews(view: View) {
        tvManualTimeZone = view.findViewById(R.id.tvManualTimeZone)
        tvAutoTimeZone = view.findViewById(R.id.tvAutoTimeZone)
        tvLanguage = view.findViewById(R.id.tvLanguage)
        tvTimeZone = view.findViewById(R.id.tvTimeZone)
        tvZone = view.findViewById(R.id.tvZone)
        tvTime = view.findViewById(R.id.tvTime)
        tvTimeFormat = view.findViewById(R.id.tvTimeFormat)
        tvNormalTimeFormat = view.findViewById(R.id.tvNormalTimeFormat)
        tvRailwayTimeFormat = view.findViewById(R.id.tvRailwayTimeFormat)
        tvChargeTypeOn = view.findViewById(R.id.tvChargeTypeOn)
        tvChargeTypeOff = view.findViewById(R.id.tvChargeTypeOff)

        ivManualTimeZoneSelected = view.findViewById(R.id.ivManualTimeZoneSelected)
        ivAutoTimeZoneSelected = view.findViewById(R.id.ivAutoTimeZoneSelected)
        ivRailwayTimeFormatSelected = view.findViewById(R.id.ivRailwayTimeFormatSelected)
        ivNormalTimeFormatSelected = view.findViewById(R.id.ivNormalTimeFormatSelected)
        ivChargeTyeOff = view.findViewById(R.id.ivChargeTyeOff)
        ivChargeTyeOn = view.findViewById(R.id.ivChargeTyeOn)

        clLanguage = view.findViewById(R.id.clLanguage)
        clTimeZoneValue = view.findViewById(R.id.clTimeZoneValue)
        clTimeZone = view.findViewById(R.id.clTimeZone)

        llTimeFormat = view.findViewById(R.id.llTimeFormat)
        scrollView = view.findViewById(R.id.scrollView)
        tvDistanceUnitKm = view.findViewById(R.id.tvDistanceUnitKm)
        tvDistanceUnitMiles = view.findViewById(R.id.tvDistanceUnitMiles)
        ivDistanceUnitKm = view.findViewById(R.id.ivDistanceUnitKm)
        ivDistanceUnitMiles = view.findViewById(R.id.ivDistanceUnitMile)
        clDistanceUnit = view.findViewById(R.id.clDistanceUnit)

    }

    /**
     * Initialize click listeners for UI components.
     */
    private fun initClickListener() {
        tvManualTimeZone.setOnSoundClickListener(requireContext()) {
            viewModel.saveDateAndTime(false)
            sharedViewModel.handleSettingsChildClick(true)
            isDateTimeClicked = true
            clickedUiState = ClickedUiState.DateAndTime
            handleUiState()
        }
        tvAutoTimeZone.setOnSoundClickListener(requireContext()) {
            viewModel.saveDateAndTime(true)
            sharedViewModel.handleSettingsChildClick(true)
            isDateTimeClicked = true
            clickedUiState = ClickedUiState.DateAndTime
            handleUiState()
        }
        clTimeZoneValue.setOnSoundClickListener(requireContext()) {
            sharedViewModel.handleRvChildClick(type = getString(R.string.time_zone))
            isTimeZoneClicked = true
            sharedViewModel.handleSettingsChildClick(true)
            clickedUiState = ClickedUiState.TimeZone
            handleUiState()
        }

        tvLanguage.setOnSoundClickListener(requireContext()) {
            sharedViewModel.handleRvChildClick(type = getString(R.string.language))
            isLanguageClicked = true
            sharedViewModel.handleSettingsChildClick(true)
            clickedUiState = ClickedUiState.Language
            handleUiState()
        }

        tvNormalTimeFormat.setOnSoundClickListener(requireContext()) {
            isTimeFormatClicked = true
            viewModel.saveTimeFormat(true)
            clickedUiState = ClickedUiState.TimeFormat
            handleUiState()
        }

        tvRailwayTimeFormat.setOnSoundClickListener(requireContext()) {
            isTimeFormatClicked = true
            viewModel.saveTimeFormat(false)
            clickedUiState = ClickedUiState.TimeFormat
            handleUiState()
        }
        tvChargeTypeOff.setOnSoundClickListener(requireContext()) {
            isChargeClicked = true
            viewModel.saveChargeEnable(false)
            clickedUiState = ClickedUiState.ChargeType
            handleUiState()
        }

        tvChargeTypeOn.setOnSoundClickListener(requireContext()) {
            isChargeClicked = true
            viewModel.saveChargeEnable(true)
            clickedUiState = ClickedUiState.ChargeType
            handleUiState()
        }
        tvDistanceUnitKm.setOnSoundClickListener(requireContext()) {
            isDistanceUnitClicked = true
            viewModel.saveDistanceUnit("km")
            clickedUiState= ClickedUiState.DistanceUnit
            sendDistanceUnitKM(true)
            handleUiState()
        }
        tvDistanceUnitMiles.setOnSoundClickListener(requireContext()) {
            isDistanceUnitClicked = true
            viewModel.saveDistanceUnit("miles")
            sendDistanceUnitKM(false)
            clickedUiState= ClickedUiState.DistanceUnit
            handleUiState()
        }
    }

        fun handleButtonNavigation(button: Int) {
            when (button) {
                ButtonNavigation.Left.ordinal -> {
                    when (buttonClickedState) {
                        ButtonClickedState.DateAndTime -> {
                            handleButtonNavigationDateTimeHorizontal()
                        }

                        ButtonClickedState.TimeZone -> {
                            return
                        }

                        ButtonClickedState.TimeFormat -> {
                            handleButtonNavigationTimeFormatHorizontal()
                        }

                        ButtonClickedState.Language -> {
                            return
                        }

                        ButtonClickedState.Charging -> {
                            handleButtonNavigationChargingHorizontal()
                        }

                        ButtonClickedState.DistanceUnit ->{
                            handleButtonNavigationDistanceUnitHorizontal()
                        }
                    }
                }

                ButtonNavigation.Right.ordinal -> {
                    when (buttonClickedState) {
                        ButtonClickedState.DateAndTime -> {
                            handleButtonNavigationDateTimeHorizontal()
                        }

                        ButtonClickedState.TimeZone -> {
                            return
                        }

                        ButtonClickedState.TimeFormat -> {
                            handleButtonNavigationTimeFormatHorizontal()
                        }

                        ButtonClickedState.Language -> {
                            return
                        }

                        ButtonClickedState.Charging -> {
                            handleButtonNavigationChargingHorizontal()
                        }

                        ButtonClickedState.DistanceUnit ->
                            handleButtonNavigationDistanceUnitHorizontal()
                    }

                }

                ButtonNavigation.Top.ordinal -> {
                    when (buttonClickedState) {
                        ButtonClickedState.DateAndTime -> {
                            scrollView.post {
                                scrollView.fullScroll(View.FOCUS_DOWN)
                            }
                            handleButtonNavigationDistanceUnitVertical() // wrap to bottom
                        }

                        ButtonClickedState.TimeZone -> {
                            handleButtonNavigationDateAndTimeVertical()
                        }

                        ButtonClickedState.TimeFormat -> {
                            handleButtonNavigationTimeZoneVertical()
                        }

                        ButtonClickedState.Language -> {
                            if (viewModel.isAutoDateTimeEnabled) {
                                handleButtonNavigationDateAndTimeVertical()
                            } else {
                                handleButtonNavigationTimeFormatVertical()
                            }
                        }

                        ButtonClickedState.Charging -> {
                            handleButtonNavigationLanguageVertical()
                        }

                        ButtonClickedState.DistanceUnit -> {
                            scrollView.post {
                                scrollView.fullScroll(View.FOCUS_DOWN)
                            }
                            handleButtonNavigationChargingVertical() // go up to Charging
                        }
                    }
                }

                ButtonNavigation.Enter.ordinal -> {
                    if (buttonClickedState == ButtonClickedState.Language) {
                        tvLanguage.performClick()
                    } else {
                        tvTimeFormat.performClick()
                    }
                }

                ButtonNavigation.Bottom.ordinal -> {
                    when (buttonClickedState) {
                        ButtonClickedState.DateAndTime -> {
                            if (viewModel.isAutoDateTimeEnabled) {
                                handleButtonNavigationLanguageVertical()
                            } else {
                                handleButtonNavigationTimeZoneVertical()
                            }
                        }

                        ButtonClickedState.TimeZone -> {
                            handleButtonNavigationTimeFormatVertical()
                        }

                        ButtonClickedState.TimeFormat -> {
                            handleButtonNavigationLanguageVertical()
                        }

                        ButtonClickedState.Language -> {
                            handleButtonNavigationChargingVertical()
                        }

                        ButtonClickedState.Charging -> {
                            scrollView.post {
                                scrollView.fullScroll(View.FOCUS_DOWN) // scroll DOWN to DistanceUnit
                            }
                            handleButtonNavigationDistanceUnitVertical()
                        }

                        ButtonClickedState.DistanceUnit -> {
                            scrollView.post {
                                scrollView.fullScroll(View.FOCUS_UP) // back to top
                            }
                            handleButtonNavigationDateAndTimeVertical() // wrap to DateAndTime
                        }
                    }
                }

                ButtonNavigation.Back.ordinal -> {
                    ivManualTimeZoneSelected.isVisible = false
                    ivAutoTimeZoneSelected.isVisible = false
                    ivRailwayTimeFormatSelected.isVisible = false
                    ivNormalTimeFormatSelected.isVisible = false
                    ivChargeTyeOn.isVisible = false
                    ivChargeTyeOff.isVisible = false
                    ivDistanceUnitKm.isVisible = false
                    ivDistanceUnitMiles.isVisible = false
                    dateAndTimeUnclickedUiState()
                    timeZoneUnclickedUiState()
                    chargeTypeUnclickedUiState()
                    languageUnclickedUiState()
                    timeFormatUnclickedUiState()
                    DistanceUnitUnclickedUiState()
                }
            }
        }

        private fun handleButtonNavigationChargingHorizontal() {
            if (viewModel.isChargeEnable) {
                tvChargeTypeOff.performClick()
            } else {
                tvChargeTypeOn.performClick()
            }
        }
    private fun handleButtonNavigationDistanceUnitHorizontal() {
        if (viewModel.distanceUnit=="km") {

            tvDistanceUnitMiles.performClick()
        } else {
            tvDistanceUnitKm.performClick()
        }
    }

        private fun handleButtonNavigationTimeFormatHorizontal() {
            if (viewModel.isNormalTimeFormat) {
                tvRailwayTimeFormat.performClick()
            } else {
                tvNormalTimeFormat.performClick()
            }
        }

        private fun handleButtonNavigationDateTimeHorizontal() {
            if (viewModel.isAutoDateTimeEnabled) {
                tvManualTimeZone.performClick()
            } else {
                tvAutoTimeZone.performClick()
            }
        }

        private fun handleButtonNavigationLanguageVertical() {
            buttonClickedState = ButtonClickedState.Language
            scrollView.post {
                scrollView.scrollTo(0, clLanguage.bottom)
                clLanguage.focusable
            }
            clickedUiState = ClickedUiState.Language
            handleUiState()
        }

        private fun handleButtonNavigationTimeFormatVertical() {
            buttonClickedState = ButtonClickedState.TimeFormat
            scrollView.post {
                scrollView.fullScroll(View.FOCUS_UP)
            }
            if (viewModel.isNormalTimeFormat) {
                tvNormalTimeFormat.performClick()
            } else {
                tvRailwayTimeFormat.performClick()
            }
        }

        private fun handleButtonNavigationTimeZoneVertical() {
            buttonClickedState = ButtonClickedState.TimeZone
            clickedUiState = ClickedUiState.TimeZone
            handleUiState()
        }

        private fun handleButtonNavigationDateAndTimeVertical() {
            buttonClickedState = ButtonClickedState.DateAndTime
            if (viewModel.isAutoDateTimeEnabled) {
                tvAutoTimeZone.performClick()
            } else {
                tvManualTimeZone.performClick()
            }
        }


        private fun handleButtonNavigationChargingVertical() {
            buttonClickedState = ButtonClickedState.Charging
            if (viewModel.isChargeEnable) {
                tvChargeTypeOn.performClick()
            } else {
                tvChargeTypeOff.performClick()
            }
        }
    private fun handleButtonNavigationDistanceUnitVertical() {
        buttonClickedState = ButtonClickedState.DistanceUnit
        if (viewModel.distanceUnit=="km") {
            tvDistanceUnitKm.performClick()
        } else {
            tvDistanceUnitMiles.performClick()
        }
    }

        /**
         * Observes changes to the selected item from the recycler view.
         */
        private fun initObserver() {
            sharedViewModel.rvChildClick.observe(viewLifecycleOwner) { action ->
                if (sharedViewModel.hasGeneralFragmentDestroyed) {
                    sharedViewModel.hasGeneralFragmentDestroyed = false
                    return@observe
                }
                if (action.second.isNotEmpty()) return@observe
                when (action.first?.type) {
                    getString(R.string.time_zone) -> {
                        timeZoneClickedUiState(
                            TimeZoneItem(
                                action.first?.title ?: "", action.first?.content ?: ""
                            )
                        )
                    }

                    getString(R.string.language) -> {
                        languageClickedUiState(action?.first?.title ?: "")
                    }
                }
            }
        }


        override fun onDestroyView() {
            super.onDestroyView()
            sharedViewModel.hasGeneralFragmentDestroyed = true
        }

        /**
         * Handles the UI state based on the current clicked UI state.
         */
        private fun handleUiState() {
            when (clickedUiState) {
                ClickedUiState.Unclicked -> {
                    unClickedUiState()
                }

                ClickedUiState.DateAndTime -> {
                    dateAndTimeClickedUiState()
                    timeZoneUnclickedUiState()
                    chargeTypeUnclickedUiState()
                    languageUnclickedUiState()
                    timeFormatUnclickedUiState()
                    DistanceUnitUnclickedUiState()
                }

                ClickedUiState.Language -> {
                    dateAndTimeUnclickedUiState()
                    timeZoneUnclickedUiState()
                    chargeTypeUnclickedUiState()
                    languageClickedUiState()
                    timeFormatUnclickedUiState()
                    DistanceUnitUnclickedUiState()
                }

                ClickedUiState.TimeZone -> {
                    dateAndTimeUnclickedUiState()
                    timeZoneClickedUiState()
                    chargeTypeUnclickedUiState()
                    languageUnclickedUiState()
                    timeFormatUnclickedUiState()
                    DistanceUnitUnclickedUiState()
                }

                ClickedUiState.TimeFormat -> {
                    timeFormatClickedUiState()
                    dateAndTimeUnclickedUiState()
                    chargeTypeUnclickedUiState()
                    timeZoneUnclickedUiState()
                    languageUnclickedUiState()
                    DistanceUnitUnclickedUiState()
                }

                ClickedUiState.ChargeType -> {
                    dateAndTimeUnclickedUiState()
                    chargeTypeUnclickedUiState()
                    timeFormatUnclickedUiState()
                    languageUnclickedUiState()
                    chargeTypeClickedUiState()
                    DistanceUnitUnclickedUiState()

                }

                ClickedUiState.DistanceUnit -> {
                    distanceUnitClickedUiState()
                    dateAndTimeUnclickedUiState()
                    chargeTypeUnclickedUiState()
                    timeZoneUnclickedUiState()
                    languageUnclickedUiState()
                    timeFormatUnclickedUiState()


                }
            }

        }


        /**
         * Handles the unclicked UI state.
         */
        private fun unClickedUiState() {
            dateAndTimeUnclickedUiState()
            timeZoneUnclickedUiState()
            languageUnclickedUiState()
            timeFormatUnclickedUiState()
            chargeTypeUnclickedUiState()
            DistanceUnitUnclickedUiState()
        }

        /**
         * Handles the clicked UI state for date and time.
         */
        private fun dateAndTimeClickedUiState() {
            ivManualTimeZoneSelected.isVisible = !viewModel.isAutoDateTimeEnabled
            ivAutoTimeZoneSelected.isVisible = viewModel.isAutoDateTimeEnabled
            ivNormalTimeFormatSelected.isVisible = false
            ivRailwayTimeFormatSelected.isVisible = false
            ivChargeTyeOff.isVisible = false
            ivChargeTyeOn.isVisible = false
            tvTimeZone.isVisible = !viewModel.isAutoDateTimeEnabled
            clTimeZone.isVisible = !viewModel.isAutoDateTimeEnabled
            tvTimeFormat.isVisible = !viewModel.isAutoDateTimeEnabled
            llTimeFormat.isVisible = !viewModel.isAutoDateTimeEnabled
            val selectedTextColor = ContextCompat.getColor(requireContext(), R.color.white)
            val selectedBackgroundColor =
                ContextCompat.getColor(requireContext(), R.color.activeSelectionRed)
            val unselectedTextColor = ContextCompat.getColor(requireContext(), R.color.unSelected)
            val unselectedBackgroundColor =
                ContextCompat.getColor(requireContext(), R.color.transparent)
            tvManualTimeZone.apply {
                setTextColor(if (viewModel.isAutoDateTimeEnabled) unselectedTextColor else selectedTextColor)
                setBackgroundColor(if (viewModel.isAutoDateTimeEnabled) unselectedBackgroundColor else selectedBackgroundColor)
            }
            tvAutoTimeZone.apply {
                setTextColor(if (viewModel.isAutoDateTimeEnabled) selectedTextColor else unselectedTextColor)
                setBackgroundColor(if (viewModel.isAutoDateTimeEnabled) selectedBackgroundColor else unselectedBackgroundColor)
            }
        }


        /**
         * Handles the unclicked UI state for date and time.
         */
        private fun dateAndTimeUnclickedUiState() {
            tvTimeZone.isVisible = !viewModel.isAutoDateTimeEnabled
            clTimeZone.isVisible = !viewModel.isAutoDateTimeEnabled
            tvTimeFormat.isVisible = !viewModel.isAutoDateTimeEnabled
            llTimeFormat.isVisible = !viewModel.isAutoDateTimeEnabled
            val selectedTextColor = ContextCompat.getColor(
                requireContext(), if (isDateTimeClicked) R.color.black else R.color.white
            )
            val selectedBackgroundColor = ContextCompat.getColor(
                requireContext(), if (isDateTimeClicked) R.color.white else R.color.grey
            )
            val unselectedTextColor = ContextCompat.getColor(requireContext(), R.color.unSelected)
            val unselectedBackgroundColor =
                ContextCompat.getColor(requireContext(), R.color.transparent)
            tvManualTimeZone.apply {
                setTextColor(if (viewModel.isAutoDateTimeEnabled) unselectedTextColor else selectedTextColor)
                setBackgroundColor(if (viewModel.isAutoDateTimeEnabled) unselectedBackgroundColor else selectedBackgroundColor)
            }
            tvAutoTimeZone.apply {
                setTextColor(if (viewModel.isAutoDateTimeEnabled) selectedTextColor else unselectedTextColor)
                setBackgroundColor(if (viewModel.isAutoDateTimeEnabled) selectedBackgroundColor else unselectedBackgroundColor)
            }
        }

        /**
         * Handles the clicked UI state for language.
         */
        private fun languageClickedUiState(language: String = getString(R.string.english)) {
            ivChargeTyeOff.isVisible = false
            ivChargeTyeOn.isVisible = false
            ivAutoTimeZoneSelected.isVisible = false
            ivManualTimeZoneSelected.isVisible = false
            ivNormalTimeFormatSelected.isVisible = false
            ivRailwayTimeFormatSelected.isVisible = false
            tvLanguage.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            tvLanguage.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(), R.color.activeSelectionRed
                )
            )
            tvLanguage.text = language
        }
    private fun distanceUnitClickedUiState() {
        ivChargeTyeOff.isVisible = false
        ivChargeTyeOn.isVisible = false
        ivDistanceUnitKm.isVisible=("km"==viewModel.distanceUnit)
        ivDistanceUnitMiles.isVisible=("miles"==viewModel.distanceUnit)
        ivManualTimeZoneSelected.isVisible = false
        ivAutoTimeZoneSelected.isVisible = false
        ivNormalTimeFormatSelected.isVisible = false
        ivRailwayTimeFormatSelected.isVisible = false
        val selectedTextColor = ContextCompat.getColor(requireContext(), R.color.white)
        val selectedBackgroundColour =
            ContextCompat.getColor(requireContext(), R.color.activeSelectionRed)
        val unselectedTextColor = ContextCompat.getColor(requireContext(), R.color.unSelected)
        val unselectedBackgroundColor =
            ContextCompat.getColor(requireContext(), R.color.transparent)
        tvDistanceUnitKm.apply {
            setTextColor(if (viewModel.distanceUnit=="km") selectedTextColor else unselectedTextColor)
            setBackgroundColor(if (viewModel.distanceUnit=="km") selectedBackgroundColour else unselectedBackgroundColor)

        }
        tvDistanceUnitMiles.apply {
            setTextColor(if ((viewModel.distanceUnit=="miles")) selectedTextColor else unselectedTextColor)
            setBackgroundColor(if ((viewModel.distanceUnit=="miles")) selectedBackgroundColour else unselectedBackgroundColor)
        }
    }



    /**
         * Handles the unclicked UI state for language.
         */
        private fun languageUnclickedUiState() {
            if (isLanguageClicked) {
                tvLanguage.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                tvLanguage.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
            } else {
                tvLanguage.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                tvLanguage.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(), R.color.transparent
                    )
                )
            }
        }

        /**
         * Handles the clicked UI state for time format.
         */
        private fun timeFormatClickedUiState() {
            ivChargeTyeOff.isVisible = false
            ivChargeTyeOn.isVisible = false
            ivNormalTimeFormatSelected.isVisible = viewModel.isNormalTimeFormat
            ivRailwayTimeFormatSelected.isVisible = !viewModel.isNormalTimeFormat
            ivManualTimeZoneSelected.isVisible = false
            ivAutoTimeZoneSelected.isVisible = false
            val selectedTextColor = ContextCompat.getColor(requireContext(), R.color.white)
            val selectedBackgroundColour =
                ContextCompat.getColor(requireContext(), R.color.activeSelectionRed)
            val unselectedTextColor = ContextCompat.getColor(requireContext(), R.color.unSelected)
            val unselectedBackgroundColor =
                ContextCompat.getColor(requireContext(), R.color.transparent)
            tvNormalTimeFormat.apply {
                setTextColor(if (viewModel.isNormalTimeFormat) selectedTextColor else unselectedTextColor)
                setBackgroundColor(if (viewModel.isNormalTimeFormat) selectedBackgroundColour else unselectedBackgroundColor)
            }
            tvRailwayTimeFormat.apply {
                setTextColor(if (!viewModel.isNormalTimeFormat) selectedTextColor else unselectedTextColor)
                setBackgroundColor(if (!viewModel.isNormalTimeFormat) selectedBackgroundColour else unselectedBackgroundColor)
            }
        }

        /**
         * Handles the unclicked UI state for time format.
         */
        private fun timeFormatUnclickedUiState() {
            val selectedTextColor = ContextCompat.getColor(
                requireContext(), if (isTimeFormatClicked) R.color.black else R.color.white
            )
            val selectedBackgroundColor = ContextCompat.getColor(
                requireContext(), if (isTimeFormatClicked) R.color.white else R.color.grey
            )
            val unselectedTextColor = ContextCompat.getColor(requireContext(), R.color.unSelected)
            val unselectedBackgroundColor =
                ContextCompat.getColor(requireContext(), R.color.transparent)
            tvNormalTimeFormat.apply {
                setTextColor(if (viewModel.isNormalTimeFormat) selectedTextColor else unselectedTextColor)
                setBackgroundColor(if (viewModel.isNormalTimeFormat) selectedBackgroundColor else unselectedBackgroundColor)
            }
            tvRailwayTimeFormat.apply {
                setTextColor(if (!viewModel.isNormalTimeFormat) selectedTextColor else unselectedTextColor)
                setBackgroundColor(if (!viewModel.isNormalTimeFormat) selectedBackgroundColor else unselectedBackgroundColor)
            }
        }

        /**
         * Handles the clicked UI state for time zone.
         */
        private fun timeZoneClickedUiState(
            timeZone: TimeZoneItem = TimeZoneItem(
                getString(R.string.india), getString(R.string.india_ist)
            )
        ) {
            ivChargeTyeOff.isVisible = false
            ivChargeTyeOn.isVisible = false
            ivManualTimeZoneSelected.isVisible = false
            ivAutoTimeZoneSelected.isVisible = false
            ivNormalTimeFormatSelected.isVisible = false
            ivRailwayTimeFormatSelected.isVisible = false
            clTimeZoneValue.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(), R.color.activeSelectionRed
                )
            )
            tvTime.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            tvZone.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            tvTime.text = timeZone.time
            tvZone.text = timeZone.timeZone
        }

        /**
         * Handles the unclicked UI state for time zone.
         */
        private fun timeZoneUnclickedUiState() {
            tvTime.setTextColor(
                ContextCompat.getColor(
                    requireContext(), if (isTimeZoneClicked) R.color.black else R.color.white
                )
            )
            tvZone.setTextColor(
                ContextCompat.getColor(
                    requireContext(), if (isTimeZoneClicked) R.color.black else R.color.white
                )
            )
            if (isTimeZoneClicked) {
                clTimeZoneValue.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(), R.color.white
                    )
                )
            } else {
                clTimeZoneValue.setBackgroundResource(R.drawable.bg_stroke)
            }
        }

        private fun chargeTypeClickedUiState() {
            ivManualTimeZoneSelected.isVisible = false
            ivAutoTimeZoneSelected.isVisible = false
            ivNormalTimeFormatSelected.isVisible = false
            ivRailwayTimeFormatSelected.isVisible = false
            ivChargeTyeOff.isVisible = !viewModel.isChargeEnable
            ivChargeTyeOn.isVisible = viewModel.isChargeEnable
            val selectedTextColor = ContextCompat.getColor(requireContext(), R.color.white)
            val selectedBackgroundColor =
                ContextCompat.getColor(requireContext(), R.color.activeSelectionRed)
            val unselectedTextColor = ContextCompat.getColor(requireContext(), R.color.unSelected)
            val unselectedBackgroundColor =
                ContextCompat.getColor(requireContext(), R.color.transparent)
            tvChargeTypeOff.apply {
                setTextColor(if (viewModel.isChargeEnable) unselectedTextColor else selectedTextColor)
                setBackgroundColor(if (viewModel.isChargeEnable) unselectedBackgroundColor else selectedBackgroundColor)
            }
            tvChargeTypeOn.apply {
                setTextColor(if (viewModel.isChargeEnable) selectedTextColor else unselectedTextColor)
                setBackgroundColor(if (viewModel.isChargeEnable) selectedBackgroundColor else unselectedBackgroundColor)
            }
        }

        private fun chargeTypeUnclickedUiState() {
            val selectedTextColor = ContextCompat.getColor(
                requireContext(), if (isChargeClicked) R.color.black else R.color.white
            )
            val selectedBackgroundColor = ContextCompat.getColor(
                requireContext(), if (isChargeClicked) R.color.white else R.color.grey
            )
            val unselectedTextColor = ContextCompat.getColor(requireContext(), R.color.unSelected)
            val unselectedBackgroundColor =
                ContextCompat.getColor(requireContext(), R.color.transparent)
            tvChargeTypeOn.apply {
                setTextColor(if (viewModel.isChargeEnable) selectedTextColor else unselectedTextColor)
                setBackgroundColor(if (viewModel.isChargeEnable) selectedBackgroundColor else unselectedBackgroundColor)
            }
            tvChargeTypeOff.apply {
                setTextColor(if (viewModel.isChargeEnable) unselectedTextColor else selectedTextColor)
                setBackgroundColor(if (viewModel.isChargeEnable) unselectedBackgroundColor else selectedBackgroundColor)
            }
        }
    private fun DistanceUnitUnclickedUiState() {
        val selectedTextColor = ContextCompat.getColor(
            requireContext(), if (isDistanceUnitClicked) R.color.black else R.color.white
        )
        val selectedBackgroundColor = ContextCompat.getColor(
            requireContext(), if (isDistanceUnitClicked) R.color.white else R.color.grey
        )
        val unselectedTextColor = ContextCompat.getColor(requireContext(), R.color.unSelected)
        val unselectedBackgroundColor =
            ContextCompat.getColor(requireContext(), R.color.transparent)
        tvDistanceUnitKm.apply {
            setTextColor(if (viewModel.distanceUnit=="km") selectedTextColor else unselectedTextColor)
            setBackgroundColor(if (viewModel.distanceUnit=="km") selectedBackgroundColor else unselectedBackgroundColor)
        }
        tvDistanceUnitMiles.apply {
            setTextColor(if (viewModel.distanceUnit=="km") unselectedTextColor else selectedTextColor)
            setBackgroundColor(if (viewModel.distanceUnit=="km") unselectedBackgroundColor else selectedBackgroundColor)
        }
    }



        /**
         * Clicked UI states.
         */
        sealed class ClickedUiState() {
            object DateAndTime : ClickedUiState()
            object Language : ClickedUiState()
            object TimeZone : ClickedUiState()
            object TimeFormat : ClickedUiState()
            object ChargeType : ClickedUiState()
            object Unclicked : ClickedUiState()
            object DistanceUnit : ClickedUiState()
        }

        enum class ButtonClickedState {
            DateAndTime,
            TimeZone,
            TimeFormat,
            Language,
            Charging,
            DistanceUnit
        }
    private fun sendDistanceUnitKM(isKm: Boolean) {
        val value: Byte = if (isKm) 0.toByte() else 1.toByte()
        d("GeneralFragment", "isKM:$isKm")
        val packet = byteArrayOf(value)
        carViewModel.sendByteArrayProperty(0x217002E0, packet)
    }

}

