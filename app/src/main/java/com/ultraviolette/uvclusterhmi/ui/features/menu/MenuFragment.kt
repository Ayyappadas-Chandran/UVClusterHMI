package com.ultraviolette.uvclusterhmi.ui.features.menu

import android.animation.Animator
import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.util.Log.d
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.LottieAnimationView
import com.ultraviolette.uvclusterhmi.R
import com.ultraviolette.uvclusterhmi.domain.dataModel.vcuData.TellTales
import com.ultraviolette.uvclusterhmi.domain.ennumerate.ButtonNavigation
import com.ultraviolette.uvclusterhmi.ui.features.MainActivity
import com.ultraviolette.uvclusterhmi.ui.viewModel.CarViewModel
import com.ultraviolette.uvclusterhmi.ui.viewModel.SharedViewModel
import com.ultraviolette.uvclusterhmi.utils.Utilities
import com.ultraviolette.uvclusterhmi.utils.Utilities.ARG_BALLISTIC_PLUS
import com.ultraviolette.uvclusterhmi.utils.Utilities.applyMinMax
import com.ultraviolette.uvclusterhmi.utils.Utilities.setOnSoundClickListener
import com.ultraviolette.uvclusterhmi.utils.ViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MenuFragment : Fragment() {

    // ==================================================================================
    // Single Logcat tag — filter all menu logs with:
    //   adb logcat -s MENU_NAV
    // ==================================================================================
    companion object {
        private const val TAG = "MENU_NAV"
    }

    // ==================================================================================
    // THE KEY FIX: isMenuActive
    //
    // This boolean tracks whether MenuFragment is the currently visible screen.
    //
    // WHY WE NEED IT:
    // carViewModel.swiftButton is a SharedFlow that keeps emitting hardware button events
    // continuously — even when the user has navigated into a child screen (Controls,
    // Battery, etc.). Because MenuFragment uses repeatOnLifecycle(STARTED), its coroutine
    // pauses when the fragment is STOPPED (hidden by a child) and RESUMES when it comes
    // back. When it resumes, any button events that were emitted while we were on the
    // child screen get replayed or re-emitted — causing wrong navigation or crashes.
    //
    // HOW IT WORKS:
    //   onResume()  → isMenuActive = true   (fragment is fully on screen, accept events)
    //   onPause()   → isMenuActive = false  (fragment going away, block events immediately)
    //
    // The swiftButton collector checks this flag FIRST. If false, the event is dropped
    // with a log. This means:
    //   - While on ControlFragment/BatteryFragment/etc: all button events are dropped
    //   - The instant MenuFragment comes back (onResume): events flow again
    //   - No time window, no guessing — purely lifecycle-driven
    //
    // WHY NOT A TIMESTAMP (the old approach):
    // The old code used "drop events within 300ms of onResume". This caused Controls,
    // Battery, and TPMS tiles to be unresponsive — because the user's first button press
    // after returning landed inside that 300ms window and was incorrectly dropped as stale.
    // ==================================================================================
    private var isMenuActive = false

    private lateinit var tvBikeName: TextView
    private lateinit var tvBattery: TextView
    private lateinit var tvMedia: TextView
    private lateinit var tvNowPlaying: TextView
    private lateinit var tvConnected: TextView
    private lateinit var tvConnectedDevice: TextView
    private lateinit var tvControls: TextView
    private lateinit var tvAbs: TextView
    private lateinit var tvTractionControl: TextView
    private lateinit var tvRegen: TextView
    private lateinit var tvHillHold: TextView
    private lateinit var tvTpms: TextView
    private lateinit var tvTpmsFront: TextView
    private lateinit var tvTpmsRear: TextView
    private lateinit var tvNavigate: TextView
    private lateinit var tvBatteryLimit: TextView
    private lateinit var tvBatteryPercent: TextView
    private lateinit var tvTpmsFrontUnit: TextView
    private lateinit var tvTpmsRearValue: TextView
    private lateinit var tvTpmsRearUnit: TextView
    private lateinit var tvSongName: TextView
    private lateinit var tvAbsValue: TextView
    private lateinit var tvTractionValue: TextView
    private lateinit var tvHillHoldState: TextView
    private lateinit var tvRegenValue: TextView
    private lateinit var tvPercent: TextView
    private lateinit var tvTpmsFrontValue: TextView
    private lateinit var tvDistance: TextView
    private lateinit var tvNavigating: TextView
    private lateinit var ivMusicSelected: ImageView
    private lateinit var ivTpmsStateFront: ImageView
    private lateinit var ivTpmsStateRear: ImageView
    private lateinit var ivManeuver: ImageView
    private lateinit var pbBattery: ProgressBar
    private lateinit var clBgMyF77: ConstraintLayout
    private lateinit var clBgBattery: ConstraintLayout
    private lateinit var clBgSetting: ConstraintLayout
    private lateinit var clBgMedia: ConstraintLayout
    private lateinit var clBgControls: ConstraintLayout
    private lateinit var clBgTpms: ConstraintLayout
    private lateinit var clBgNavigate: ConstraintLayout
    private lateinit var clNavigationOff: ConstraintLayout
    private lateinit var clNavigateStart: ConstraintLayout
    private lateinit var clMenu: ConstraintLayout

    private lateinit var laMusicAnim: LottieAnimationView
    private lateinit var laSettingsAnim: LottieAnimationView
    private lateinit var laNavigateAnim: LottieAnimationView
    private var navigateJob: Job? = null
    private var tpmsJob: Job? = null
    private lateinit var gestureDetector: GestureDetector

    private val animFade by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.fade) }
    private val animSlideUpReverse by lazy {
        AnimationUtils.loadAnimation(requireContext(), R.anim.slide_down_fade_reverse)
    }
    private val animSlideDownReverse by lazy {
        AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up_fade_reverse)
    }
    private val animSlideUp by lazy {
        AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up_fade)
    }
    private val animSlideDown by lazy {
        AnimationUtils.loadAnimation(requireContext(), R.anim.slide_down_fade)
    }
    private val animPressScale by lazy {
        AnimationUtils.loadAnimation(requireContext(), R.anim.press_scale)
    }
    private val animReversePressScale by lazy {
        AnimationUtils.loadAnimation(requireContext(), R.anim.release_scale)
    }

    private val carViewModel by activityViewModels<CarViewModel> { ViewModelFactory(context = requireContext()) }
    private val sharedViewModel by activityViewModels<SharedViewModel> { ViewModelFactory(context = requireContext()) }
    private val viewModel by activityViewModels<MenuViewModel> { ViewModelFactory(context = requireContext()) }

    private var currentPosition = 0
    private var isBallistic = false
    // Navigation maps — each map defines which button press moves to which menu item
    // from the current position. If a button is not in the map, it is ignored.
    private val f77Map = mapOf(
        ButtonNavigation.Right to MenuPosition.Battery,
        ButtonNavigation.Bottom to MenuPosition.Music,
        ButtonNavigation.Enter to MenuPosition.Enter
    )
    private val batteryMap = mapOf(
        ButtonNavigation.Right to MenuPosition.Setting,
        ButtonNavigation.Bottom to MenuPosition.Controls,
        ButtonNavigation.Left to MenuPosition.MyF77,
        ButtonNavigation.Enter to MenuPosition.Enter
    )
    private val settingMap = mapOf(
        ButtonNavigation.Left to MenuPosition.Battery,
        ButtonNavigation.Bottom to MenuPosition.Tpms,
        ButtonNavigation.Enter to MenuPosition.Enter
    )
    private val musicMap = mapOf(
        ButtonNavigation.Right to MenuPosition.Controls,
        ButtonNavigation.Top to MenuPosition.MyF77,
        ButtonNavigation.Enter to MenuPosition.Enter
    )
    private val controlMap = mapOf(
        ButtonNavigation.Right to MenuPosition.Tpms,
        ButtonNavigation.Left to MenuPosition.Music,
        ButtonNavigation.Top to MenuPosition.Battery,
        ButtonNavigation.Enter to MenuPosition.Enter
    )
    private val tpmsMap = mapOf(
        ButtonNavigation.Top to MenuPosition.Setting,
        ButtonNavigation.Bottom to MenuPosition.Navigate,
        ButtonNavigation.Left to MenuPosition.Controls,
        ButtonNavigation.Enter to MenuPosition.Enter
    )
    private val navigateMap = mapOf(
        ButtonNavigation.Left to MenuPosition.Controls,
        ButtonNavigation.Top to MenuPosition.Tpms,
        ButtonNavigation.Enter to MenuPosition.Enter
    )

    // ==================================================================================
    // Logging helpers — converts ordinals to readable names in logs
    // ==================================================================================
    private fun positionName(ordinal: Int): String =
        MenuPosition.values().getOrNull(ordinal)?.name ?: "UNKNOWN($ordinal)"

    private fun buttonName(ordinal: Int): String =
        ButtonNavigation.values().getOrNull(ordinal)?.name ?: "UNKNOWN($ordinal)"

    // ==================================================================================
    // LIFECYCLE
    // ==================================================================================

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView")
        return inflater.inflate(R.layout.fragment_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated — initial position=${positionName(viewModel.currentMenuPosition.value ?: 0)}")
        (activity as? MainActivity)?.handleToolbar(true)
        initView(view)
        initClickListener()
        initObserver()
        addSwipeGesture(clBgBattery)
        addSwipeGesture(clBgTpms)
        addSwipeGesture(clBgMedia)
        addSwipeGesture(clBgMyF77)
        addSwipeGesture(clMenu)
        addSwipeGesture(clBgControls)
        addSwipeGesture(clBgNavigate)
        addSwipeGesture(clBgSetting)
        showToolBar()

	//16April
	viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                carViewModel.swiftButton.collect { swiftButton ->

                    val button = Utilities.getButtonState(swiftButton)

                    if (button == ButtonNavigation.None) return@collect

                    if (!isMenuActive) {
                        Log.w(TAG, "DROPPED — fragment not active")
                        return@collect
                    }

                    Log.d(TAG, "swiftButton: ACCEPTED [${buttonName(button.ordinal)}] currentPosition=${positionName(currentPosition)}, navDest=${findNavController().currentDestination?.label}")
                    handleButtonNavigation(button.ordinal)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // -----------------------------------------------------------------------
        // Set isMenuActive = true HERE (onResume), not in onStart or onCreate.
        // onResume is the exact moment the fragment is fully visible and interactive.
        // Any button event received after this point is a real user action on this screen.
        // -----------------------------------------------------------------------
        isMenuActive = true
        Log.d(TAG, "onResume — isMenuActive=true, currentPosition=${positionName(currentPosition)}, navDest=${findNavController().currentDestination?.label}")
        (activity as? MainActivity)?.handleToolbar(true)
        initAnimation()
        updateCurrentState()
    }

    override fun onPause() {
        super.onPause()
        // -----------------------------------------------------------------------
        // Set isMenuActive = false HERE (onPause), not in onStop or onDestroyView.
        // onPause fires the instant the fragment starts going away (before the child
        // fragment appears). This ensures that any button events emitted DURING the
        // transition or WHILE on the child screen are blocked immediately.
        // -----------------------------------------------------------------------
        isMenuActive = false
        Log.d(TAG, "onPause — isMenuActive=false, currentPosition=${positionName(currentPosition)}")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView")
        navigateJob?.cancel()
        tpmsJob?.cancel()
    }

    // ==================================================================================
    // OBSERVER SETUP — this is where the swiftButton gate lives
    // ==================================================================================

    private fun initObserver() {
        // Observe menu position changes from the ViewModel (LiveData)
        viewModel.currentMenuPosition.observe(viewLifecycleOwner) { position ->
            Log.d(TAG, "currentMenuPosition changed → ${positionName(position)} (prev=${positionName(currentPosition)})")
            currentPosition = position
            selectedPosition(position)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                // ------------------------------------------------------------------
                // BUTTON EVENT COLLECTOR
                // This collects hardware button presses from the car's swift button.
                //
                // The gate: if (!isMenuActive) → drop the event.
                //
                // Timeline of what happens without this gate:
                //   1. User presses Enter on Controls → navigates to ControlFragment
                //   2. MenuFragment goes to STOPPED state, coroutine pauses
                //   3. User presses buttons inside ControlFragment
                //   4. User presses Back → returns to MenuFragment
                //   5. MenuFragment resumes, coroutine restarts (STARTED again)
                //   6. SharedFlow re-emits the last button value immediately
                //   7. handleButtonNavigation() fires with stale data → crash or wrong nav
                //
                // With isMenuActive:
                //   Step 1: onPause sets isMenuActive = false
                //   Step 6: collector checks isMenuActive → false → event DROPPED
                //   Step 4 (return): onResume sets isMenuActive = true
                //   Next real press: isMenuActive = true → event ACCEPTED
                // -------------------------------------------------------------
		

                // Observe ride mode to track ballistic state
                launch {
                    carViewModel.tellTales.collect { telltale ->
                        isBallistic = telltale.rideMode == 3
                    }
                }

                // Update battery SOC progress bar
                launch {
                    carViewModel.imxDbgMsg.collect { imxDbgMsg ->
                        val batterySoc = (imxDbgMsg.soc.toInt() and 0xFF)
                        pbBattery.progress = batterySoc
                        tvBatteryPercent.text = batterySoc.toString()
                    }
                }
            }
        }
    }

    // ==================================================================================
    // BUTTON NAVIGATION HANDLER
    // ==================================================================================

    fun handleButtonNavigation(button: Int) {
        val navController = findNavController()
        val currentDest = navController.currentDestination

        Log.d(TAG, "handleButtonNavigation — button=${buttonName(button)}, currentPosition=${positionName(currentPosition)}, navDest=${currentDest?.label}")

        // Secondary safety guard: even if isMenuActive is true, make sure the NavController
        // agrees we are on menuFragment. Handles edge cases during rapid navigation.
        if (currentDest?.id != R.id.menuFragment) {
            Log.w(TAG, "handleButtonNavigation: IGNORED — NavController not on menuFragment (actual=${currentDest?.label})")
            return
        }

        // Back button always goes to dashboard
        if (button == ButtonNavigation.Back.ordinal) {
            Log.d(TAG, "handleButtonNavigation: Back → dashboard")
            safeNavigate(R.id.dashboardFragment)
            return
        }

        var lastPosition = viewModel.lastMenuPosition
        Log.d(TAG, "handleButtonNavigation — lastMenuPosition=${positionName(lastPosition)}")
	
        when (currentPosition) {

            MenuPosition.MyF77.ordinal -> {
                if (button == ButtonNavigation.Enter.ordinal) {
                    Log.d(TAG, "handleButtonNavigation: Enter on MyF77 → myF77MenuFragment")
                    viewModel.lastMenuPosition = lastPosition
                    safeNavigate(R.id.action_menuFragment_to_myF77MenuFragment)
                    return
                }
                val next = f77Map[ButtonNavigation.values()[button]]
                Log.d(TAG, "handleButtonNavigation: MyF77 + ${buttonName(button)} → next=${next?.name ?: "NO MAPPING"}")
                next?.let { position ->
                    viewModel.handleCurrentMenuPosition(position.ordinal)
                    lastPosition = position.ordinal
                } ?: return
                viewModel.lastMenuPosition = lastPosition
                refreshLastPosition(MenuPosition.MyF77.ordinal)
            }

            MenuPosition.Battery.ordinal -> {
                if (button == ButtonNavigation.Enter.ordinal) {
                    Log.d(TAG, "handleButtonNavigation: Enter on Battery → batteryFragment")
                    viewModel.lastMenuPosition = lastPosition
                    safeNavigate(R.id.action_menuFragment_to_batteryFragment)
                    return
                }
                val next = batteryMap[ButtonNavigation.values()[button]]
                Log.d(TAG, "handleButtonNavigation: Battery + ${buttonName(button)} → next=${next?.name ?: "NO MAPPING"}")
                next?.let { position ->
                    viewModel.handleCurrentMenuPosition(position.ordinal)
                    lastPosition = position.ordinal
                } ?: return
                viewModel.lastMenuPosition = lastPosition
                refreshLastPosition(MenuPosition.Battery.ordinal)
            }

            MenuPosition.Setting.ordinal -> {
                if (button == ButtonNavigation.Enter.ordinal) {
                    Log.d(TAG, "handleButtonNavigation: Enter on Setting → settingMenuFragment")
                    sharedViewModel.handleSettingsChildClick(false)
                    viewModel.lastMenuPosition = lastPosition
                    safeNavigate(R.id.action_menuFragment_to_settingMenuFragment)
                    return
                }
                val next = settingMap[ButtonNavigation.values()[button]]
                Log.d(TAG, "handleButtonNavigation: Setting + ${buttonName(button)} → next=${next?.name ?: "NO MAPPING"}")
                next?.let { position ->
                    viewModel.handleCurrentMenuPosition(position.ordinal)
                    lastPosition = position.ordinal
                } ?: return
                viewModel.lastMenuPosition = lastPosition
                refreshLastPosition(MenuPosition.Setting.ordinal)
            }

            MenuPosition.Music.ordinal -> {
                if (button == ButtonNavigation.Enter.ordinal) {
                    Log.d(TAG, "handleButtonNavigation: Enter on Music → musicFragment")
                    viewModel.lastMenuPosition = lastPosition
                    safeNavigate(R.id.musicFragment)
                    return
                }
                val next = musicMap[ButtonNavigation.values()[button]]
                Log.d(TAG, "handleButtonNavigation: Music + ${buttonName(button)} → next=${next?.name ?: "NO MAPPING"}")
                next?.let { position ->
                    viewModel.handleCurrentMenuPosition(position.ordinal)
                    lastPosition = position.ordinal
                } ?: return
                viewModel.lastMenuPosition = lastPosition
                refreshLastPosition(MenuPosition.Music.ordinal)
            }

            MenuPosition.Controls.ordinal -> {
                if (button == ButtonNavigation.Enter.ordinal) {
                    Log.d(TAG, "handleButtonNavigation: Enter on Controls → controlFragment")
                    viewModel.lastMenuPosition = lastPosition
                    safeNavigate(R.id.action_menuFragment_to_controlFragment)
                    return
                }
                val next = controlMap[ButtonNavigation.values()[button]]
                Log.d(TAG, "handleButtonNavigation: Controls + ${buttonName(button)} → next=${next?.name ?: "NO MAPPING"}")
                next?.let { position ->
                    viewModel.handleCurrentMenuPosition(position.ordinal)
                    lastPosition = position.ordinal
                } ?: return
                viewModel.lastMenuPosition = lastPosition
                refreshLastPosition(MenuPosition.Controls.ordinal)
            }

            MenuPosition.Tpms.ordinal -> {
                if (button == ButtonNavigation.Enter.ordinal) {
                    Log.d(TAG, "handleButtonNavigation: Enter on Tpms → tpmsFragment")
                    viewModel.lastMenuPosition = lastPosition
                    safeNavigate(R.id.action_menuFragment_to_tpmsFragment)
                    return
                }
                val next = tpmsMap[ButtonNavigation.values()[button]]
                Log.d(TAG, "handleButtonNavigation: Tpms + ${buttonName(button)} → next=${next?.name ?: "NO MAPPING"}")
                next?.let { position ->
                    viewModel.handleCurrentMenuPosition(position.ordinal)
                    lastPosition = position.ordinal
                } ?: return
                viewModel.lastMenuPosition = lastPosition
                refreshLastPosition(MenuPosition.Tpms.ordinal)
            }

            MenuPosition.Navigate.ordinal -> {
                if (button == ButtonNavigation.Enter.ordinal) {
                    Log.d(TAG, "handleButtonNavigation: Enter on Navigate — no-op")
                    viewModel.lastMenuPosition = lastPosition
                    return
                }
                val next = navigateMap[ButtonNavigation.values()[button]]
                Log.d(TAG, "handleButtonNavigation: Navigate + ${buttonName(button)} → next=${next?.name ?: "NO MAPPING"}")
                next?.let { position ->
                    viewModel.handleCurrentMenuPosition(position.ordinal)
                    lastPosition = position.ordinal
                } ?: return
                viewModel.lastMenuPosition = lastPosition
                refreshLastPosition(MenuPosition.Navigate.ordinal)
            }

            else -> {
                Log.w(TAG, "handleButtonNavigation: UNHANDLED currentPosition=${positionName(currentPosition)}")
            }
        }

        Log.d(TAG, "handleButtonNavigation: done — newPosition=${positionName(viewModel.currentMenuPosition.value ?: -1)}, lastMenuPosition=${positionName(viewModel.lastMenuPosition)}")
    }

    // ==================================================================================
    // SAFE NAVIGATE — destination guard + crash safety net
    // This is the last line of defense. Even if isMenuActive somehow allows an event
    // through at the wrong time, this ensures we never call navigate() from a destination
    // that doesn't have the target action defined.
    // ==================================================================================
    private fun safeNavigate(actionOrDestId: Int, bundle: Bundle? = null) {
        try {
            val navController = findNavController()
            val currentDest = navController.currentDestination
            val destName = runCatching {
                resources.getResourceEntryName(actionOrDestId)
            }.getOrDefault("id=$actionOrDestId")

            if (currentDest?.id != R.id.menuFragment) {
                Log.w(TAG, "safeNavigate: SKIPPED — not on menuFragment (current=${currentDest?.label}, target=$destName)")
                return
            }
            Log.d(TAG, "safeNavigate: navigating → $destName")
            if (bundle != null) navController.navigate(actionOrDestId, bundle)
            else navController.navigate(actionOrDestId)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "safeNavigate: CAUGHT IllegalArgumentException — ${e.message}")
        }
    }

    // ==================================================================================
    // VIEW INIT
    // ==================================================================================

    private fun showToolBar() {
        (activity as? MainActivity)?.handleToolbar(true)
    }

    private fun initView(view: View) {
        tvBikeName = view.findViewById(R.id.tvBikeName)
        tvBattery = view.findViewById(R.id.tvBattery)
        tvMedia = view.findViewById(R.id.tvMedia)
        tvNowPlaying = view.findViewById(R.id.tvNowPlaying)
        tvConnected = view.findViewById(R.id.tvConnected)
        tvConnectedDevice = view.findViewById(R.id.tvConnectedDevice)
        tvControls = view.findViewById(R.id.tvControls)
        tvAbs = view.findViewById(R.id.tvAbs)
        tvTractionControl = view.findViewById(R.id.tvTractionControl)
        tvRegen = view.findViewById(R.id.tvRegen)
        tvHillHold = view.findViewById(R.id.tvHillHold)
        tvTpms = view.findViewById(R.id.tvTpms)
        tvTpmsFront = view.findViewById(R.id.tvTpmsFront)
        tvTpmsRear = view.findViewById(R.id.tvTpmsRear)
        tvNavigate = view.findViewById(R.id.tvNavigate)
        tvBatteryLimit = view.findViewById(R.id.tvBatteryLimit)
        tvNavigating = view.findViewById(R.id.tvNavigating)
        tvBatteryPercent = view.findViewById(R.id.tvBatteryPercent)
        tvTpmsFrontValue = view.findViewById(R.id.tvTpmsFrontValue)
        tvTpmsFrontUnit = view.findViewById(R.id.tvTpmsFrontUnit)
        tvTpmsRearValue = view.findViewById(R.id.tvTpmsRearValue)
        tvTpmsRearUnit = view.findViewById(R.id.tvTpmsRearUnit)
        tvSongName = view.findViewById(R.id.tvSongName)
        tvAbsValue = view.findViewById(R.id.tvAbsValue)
        tvTractionValue = view.findViewById(R.id.tvTractionValue)
        tvHillHoldState = view.findViewById(R.id.tvHillHoldState)
        tvRegenValue = view.findViewById(R.id.tvRegenValue)
        tvPercent = view.findViewById(R.id.tvPercent)
        tvDistance = view.findViewById(R.id.tvDistance)
        ivMusicSelected = view.findViewById(R.id.ivMusicSelect)
        ivTpmsStateFront = view.findViewById(R.id.ivTpmsStateFront)
        ivTpmsStateRear = view.findViewById(R.id.ivTpmsStateRear)
        ivManeuver = view.findViewById(R.id.ivManeuver)
        clBgMyF77 = view.findViewById(R.id.clBgMyF77)
        clBgBattery = view.findViewById(R.id.clBgBattery)
        clBgSetting = view.findViewById(R.id.clBgSetting)
        clBgMedia = view.findViewById(R.id.clBgMedia)
        clBgControls = view.findViewById(R.id.clBgControls)
        clBgTpms = view.findViewById(R.id.clBgTpms)
        clBgNavigate = view.findViewById(R.id.clBgNavigate)
        clNavigationOff = view.findViewById(R.id.clNavigationOff)
        clNavigateStart = view.findViewById(R.id.clNavigationStart)
        clMenu = view.findViewById(R.id.clMenu)
        pbBattery = view.findViewById(R.id.pbBattery)
        laMusicAnim = view.findViewById(R.id.laMusicAnim)
        laSettingsAnim = view.findViewById(R.id.laSettingsAnim)
        laNavigateAnim = view.findViewById(R.id.laNavigateAnim)
        tvBatteryLimit.text = "Limit ${sharedViewModel.batteryLimit} %"
    }

    // ==================================================================================
    // CLICK LISTENERS — touch/tap on menu tiles
    // ==================================================================================

    private fun initClickListener() {
        val menuMap = mapOf(
            clBgMyF77 to MenuPosition.MyF77,
            clBgBattery to MenuPosition.Battery,
            clBgSetting to MenuPosition.Setting,
            clBgMedia to MenuPosition.Music,
            clBgControls to MenuPosition.Controls,
            clBgTpms to MenuPosition.Tpms,
            clBgNavigate to MenuPosition.Navigate
        )
        menuMap.forEach { (view, menuItem) ->
            view.setOnSoundClickListener(requireContext()) {
                val newPosition = menuItem.ordinal
                Log.d(TAG, "tile clicked: ${menuItem.name}, prev=${positionName(viewModel.lastMenuPosition)}")
                refreshLastPosition(viewModel.lastMenuPosition)
                viewModel.handleCurrentMenuPosition(newPosition)
                viewModel.lastMenuPosition = newPosition
                when (menuItem) {
                    MenuPosition.MyF77 -> animateAndNavigateToMyF77()
                    MenuPosition.Battery -> safeNavigate(R.id.action_menuFragment_to_batteryFragment)
                    MenuPosition.Setting -> {
                        sharedViewModel.handleSettingsChildClick(true)
                        animateAndNavigateToSettingMenu()
                    }
                    MenuPosition.Music -> animateAndNavigateToMedia()
                    MenuPosition.Controls -> safeNavigate(R.id.action_menuFragment_to_controlFragment)
                    MenuPosition.Tpms -> safeNavigate(R.id.action_menuFragment_to_tpmsFragment)
                    MenuPosition.Navigate -> animationAndNavigateToNavigation()
                    MenuPosition.Enter -> return@setOnSoundClickListener
                }
            }
        }
    }

    // ==================================================================================
    // SWIPE GESTURE
    // ==================================================================================

    @SuppressLint("ClickableViewAccessibility")
    private fun addSwipeGesture(rootView: View?) {
        if (rootView == null) return
        val swipeThreshold = 50
        val swipeVelocityThreshold = 50
        val detector = GestureDetector(
            requireContext(),
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onDown(e: MotionEvent): Boolean = true
                override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                    Log.d(TAG, "tap on ${runCatching { resources.getResourceEntryName(rootView.id) }.getOrDefault("view")}")
                    onClick(rootView)
                    return true
                }
                override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
                    if (e1 == null) return false
                    val diffY = e2.y - e1.y
                    if (diffY > swipeThreshold && kotlin.math.abs(distanceY) > 0) {
                        onSwipeDown(); return true
                    }
                    return false
                }
                override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                    if (e1 == null) return false
                    val diffY = e2.y - e1.y
                    if (diffY > swipeThreshold && kotlin.math.abs(velocityY) > swipeVelocityThreshold) {
                        onSwipeDown(); return true
                    }
                    return false
                }
            }
        )
        rootView.setOnTouchListener { _, event -> detector.onTouchEvent(event); true }
        rootView.isClickable = true
        rootView.isFocusable = true
    }

    private fun onSwipeDown() {
        Log.d(TAG, "swipe down → dashboard")
        safeNavigate(R.id.dashboardFragment)
    }

    private fun onClick(rootview: View?) {
        when (rootview?.id) {
            R.id.clBgControls -> safeNavigate(R.id.controlFragment)
            R.id.clBgMedia -> safeNavigate(R.id.musicFragment)
            R.id.clBgMyF77 -> safeNavigate(R.id.myF77MenuFragment)
            R.id.clBgTpms -> safeNavigate(R.id.tpmsFragment)
            R.id.clBgBattery -> safeNavigate(R.id.batteryFragment)
            R.id.clMenu -> safeNavigate(R.id.dashboardFragment)
            R.id.clBgNavigate -> safeNavigate(R.id.mapFragment)
            R.id.clBgSetting -> safeNavigate(R.id.settingMenuFragment)
        }
    }

    // ==================================================================================
    // STATE UPDATE
    // ==================================================================================

    private fun updateCurrentState() {
        val hillHold = viewModel.hillHoldState
        val absState = viewModel.absState
        val regenValue = viewModel.regenLevel
        val tractionLevel = viewModel.tractionLevel
        val tractionLevelValue = when (tractionLevel) {
            getString(R.string.tc1) -> "T1"
            getString(R.string.tc2) -> "T2"
            getString(R.string.tc3) -> "T3"
            else -> "Off"
        }
        Log.d(TAG, "updateCurrentState — hillHold=$hillHold, abs=$absState, regen=$regenValue, traction=$tractionLevelValue")
        tvHillHoldState.text = if (!hillHold) getString(R.string.off) else getString(R.string.on)
        tvAbsValue.text = if (absState) getString(R.string.mono) else getString(R.string.dual)
        tvRegenValue.text = "R$regenValue"
        tvTractionValue.text = tractionLevelValue
    }

    private fun updateAbs(telltale: TellTales) {
        val abs = telltale.absMode
        val absWarning = telltale.absWarningLamp
        if (absWarning == 0x00) {
            if (abs == 0x00) tvAbsValue.text = getString(R.string.dual)
            else tvAbsValue.text = getString(R.string.mono)
        } else {
            tvAbsValue.text = getString(R.string.off)
        }
    }

    private fun updateTractionControl(t: TellTales) {
        val (value, _) = when (t.mtcMode) {
            0x01 -> "OFF" to R.string.triple_hypen
            0x02 -> "T1" to R.string.sport
            0x03 -> "T2" to R.string.street
            0x04 -> "T3" to R.string.rain
            else -> "OFF" to R.string.triple_hypen
        }
        tvTractionValue.text = value
    }

    private fun updateBatteryValue(tellTale: TellTales) {
        val batteryLevel = tellTale.batterySoc
        val finalBatteryLevel = batteryLevel.applyMinMax(sharedViewModel.socLimit)
        pbBattery.progress = finalBatteryLevel
        tvBatteryPercent.text = finalBatteryLevel.toString()
    }

    // ==================================================================================
    // UI SELECTION — called when a menu position becomes active
    // ==================================================================================

    private fun selectedPosition(position: Int?) {
        Log.d(TAG, "selectedPosition → ${positionName(position ?: -1)}")
        when (position) {
            MenuPosition.MyF77.ordinal -> selectMyF77()
            MenuPosition.Battery.ordinal -> selectBattery()
            MenuPosition.Setting.ordinal -> selectSetting()
            MenuPosition.Music.ordinal -> selectMusic()
            MenuPosition.Controls.ordinal -> selectControls()
            MenuPosition.Tpms.ordinal -> selectTpms()
            MenuPosition.Navigate.ordinal -> selectNavigate()
        }
    }

    private fun selectMyF77() {
        Log.d(TAG, "selectMyF77")
        clBgMyF77.setBackgroundResource(R.drawable.bg_my_f77_active)
        tvBikeName.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
    }

    private fun selectBattery() {
        Log.d(TAG, "selectBattery")
        val selectedTextColor = ContextCompat.getColor(requireContext(), R.color.white)
        clBgBattery.setBackgroundResource(R.drawable.bg_battery_active)
        tvBattery.setTextColor(selectedTextColor)
        tvBatteryLimit.setTextColor(selectedTextColor)
    }

    private fun selectSetting() {
        Log.d(TAG, "selectSetting")
        clBgSetting.setBackgroundResource(R.drawable.bg_settings_active)
    }

    private fun selectMusic() {
        Log.d(TAG, "selectMusic")
        val selectedTextColor = ContextCompat.getColor(requireContext(), R.color.white)
        clBgMedia.setBackgroundResource(R.drawable.bg_music_active)
        tvNowPlaying.setTextColor(selectedTextColor)
        tvMedia.setTextColor(selectedTextColor)
        tvConnected.setTextColor(selectedTextColor)
        tvConnectedDevice.setTextColor(selectedTextColor)
        val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.bg_music_design)?.mutate()
        drawable?.setTint(ContextCompat.getColor(requireContext(), R.color.white))
        ivMusicSelected.setColorFilter(ContextCompat.getColor(requireContext(), R.color.white))
    }

    private fun selectControls() {
        Log.d(TAG, "selectControls")
        val selectedTextColor = ContextCompat.getColor(requireContext(), R.color.white)
        clBgControls.setBackgroundResource(R.drawable.bg_controls_active)
        tvControls.setTextColor(selectedTextColor)
        tvAbs.setTextColor(selectedTextColor)
        tvTractionControl.setTextColor(selectedTextColor)
        tvRegen.setTextColor(selectedTextColor)
        tvHillHold.setTextColor(selectedTextColor)
    }

    private fun selectTpms() {
        Log.d(TAG, "selectTpms")
        val selectedTextColor = ContextCompat.getColor(requireContext(), R.color.white)
        clBgTpms.setBackgroundResource(R.drawable.bg_tpms_active)
        tvTpms.setTextColor(selectedTextColor)
        tvTpmsFront.setTextColor(selectedTextColor)
        tvTpmsRear.setTextColor(selectedTextColor)
    }

    private fun selectNavigate() {
        Log.d(TAG, "selectNavigate")
        clBgNavigate.setBackgroundResource(R.drawable.bg_navigate_active)
        if (ivManeuver.isVisible) {
            val color = ContextCompat.getColor(requireContext(), R.color.white)
            ImageViewCompat.setImageTintList(ivManeuver, ColorStateList.valueOf(color))
            tvNavigating.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        }
        tvNavigate.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
    }

    // ==================================================================================
    // UI DESELECTION — called when a position is leaving focus
    // ==================================================================================

    fun refreshLastPosition(position: Int) {
        Log.d(TAG, "refreshLastPosition: deselecting ${positionName(position)}")
        when (position) {
            MenuPosition.MyF77.ordinal -> {
                clBgMyF77.setBackgroundResource(R.drawable.bg_my_f77)
                tvBikeName.setTextColor(ContextCompat.getColor(requireContext(), R.color.unSelectedTitle))
                handleMyF77Animation(R.raw.f77_headlight_exit_anim)
            }
            MenuPosition.Battery.ordinal -> {
                tvBattery.setTextColor(ContextCompat.getColor(requireContext(), R.color.unSelectedTitle))
                clBgBattery.setBackgroundResource(R.drawable.bg_battery)
                tvBatteryLimit.setTextColor(ContextCompat.getColor(requireContext(), R.color.unSelected))
            }
            MenuPosition.Setting.ordinal -> {
                handleSettingsAnimation(R.raw.setting_icon_exit_anim)
                clBgSetting.setBackgroundResource(R.drawable.bg_settings)
            }
            MenuPosition.Controls.ordinal -> {
                tvControls.setTextColor(ContextCompat.getColor(requireContext(), R.color.unSelectedTitle))
                clBgControls.setBackgroundResource(R.drawable.bg_controls)
                tvAbs.setTextColor(ContextCompat.getColor(requireContext(), R.color.unSelected))
                tvTractionControl.setTextColor(ContextCompat.getColor(requireContext(), R.color.unSelected))
                tvRegen.setTextColor(ContextCompat.getColor(requireContext(), R.color.unSelected))
                tvHillHold.setTextColor(ContextCompat.getColor(requireContext(), R.color.unSelected))
            }
            MenuPosition.Tpms.ordinal -> {
                clBgTpms.setBackgroundResource(R.drawable.bg_tpms)
                tvTpms.setTextColor(ContextCompat.getColor(requireContext(), R.color.unSelectedTitle))
                tvTpmsFront.setTextColor(ContextCompat.getColor(requireContext(), R.color.unSelected))
                tvTpmsRear.setTextColor(ContextCompat.getColor(requireContext(), R.color.unSelected))
            }
            MenuPosition.Navigate.ordinal -> {
                tvNavigate.setTextColor(ContextCompat.getColor(requireContext(), R.color.unSelected))
                clNavigationOff.isVisible = true
                clNavigateStart.isVisible = false
                clBgNavigate.setBackgroundResource(R.drawable.bg_navigate)
                handleNavigateAnimation(R.raw.navigation_icon_exit_anim)
            }
            MenuPosition.Music.ordinal -> {
                Log.d(TAG, "refreshLastPosition: deselecting Music")
                handleMusicAnimation(R.raw.music_icon_exit_anim)
                val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.bg_music_design)?.mutate()
                drawable?.setTint(ContextCompat.getColor(requireContext(), R.color.mediumRed))
                tvMedia.setTextColor(ContextCompat.getColor(requireContext(), R.color.unSelectedTitle))
                tvNowPlaying.setTextColor(ContextCompat.getColor(requireContext(), R.color.unSelected))
                tvConnected.setTextColor(ContextCompat.getColor(requireContext(), R.color.green))
                clBgMedia.setBackgroundResource(R.drawable.bg_media)
            }
        }
    }

    // ==================================================================================
    // ANIMATIONS
    // ==================================================================================

    fun initAnimation() {
        val value = viewModel.currentMenuPosition.value
        Log.d(TAG, "initAnimation — position=${positionName(value ?: -1)}")
        when (value) {
            MenuPosition.Setting.ordinal -> {
                doSlideDownReverseAnim(clBgMyF77)
                doSlideDownReverseAnim(clBgBattery)
                doSlideDownReverseAnim(clBgSetting)
                doSlideDownReverseAnim(laSettingsAnim)
                doSlideUpReverseAnim(clBgMedia)
                doSlideUpReverseAnim(clBgControls)
                doSlideUpReverseAnim(clBgTpms)
                doSlideUpReverseAnim(clBgNavigate)
                doSlideUpReverseAnim(laNavigateAnim)
                doSlideUpReverseAnim(laMusicAnim, true) {
                    handleSettingsAnimation(R.raw.setting_icon_entry_anim)
                }
            }
            MenuPosition.MyF77.ordinal -> {
                clBgMyF77.startAnimation(animReversePressScale)
                doSlideDownReverseAnim(clBgBattery)
                doSlideDownReverseAnim(clBgSetting)
                doSlideDownReverseAnim(laSettingsAnim)
                doSlideUpReverseAnim(clBgMedia)
                doSlideUpReverseAnim(clBgControls)
                doSlideUpReverseAnim(clBgTpms)
                doSlideUpReverseAnim(clBgNavigate)
                doSlideUpReverseAnim(laNavigateAnim)
                doSlideUpReverseAnim(laMusicAnim, true) {
                    handleMyF77Animation(R.raw.f77_headlight_entry_anim)
                }
            }
            MenuPosition.Music.ordinal -> handleMusicAnimation(R.raw.music_icon_entry_anim)
            MenuPosition.Navigate.ordinal -> handleNavigateAnimation(R.raw.navigation_icon_entry_anim)
        }
    }

    private fun animateAndNavigateToMyF77() {
        Log.d(TAG, "animateAndNavigateToMyF77")
        safeNavigate(R.id.action_menuFragment_to_myF77MenuFragment)
    }

    private fun animateAndNavigateToMedia() {
        Log.d(TAG, "animateAndNavigateToMedia: starting animation")
        handleMusicAnimation(R.raw.music_icon_entry_anim) {
            Log.d(TAG, "animateAndNavigateToMedia: done → musicFragment")
            safeNavigate(R.id.musicFragment)
        }
    }

    private fun animationAndNavigateToNavigation() {
        Log.d(TAG, "animationAndNavigateToNavigation: isBallistic=$isBallistic")
        handleNavigateAnimation(R.raw.navigation_icon_entry_anim) {
            Log.d(TAG, "animationAndNavigateToNavigation: done → mapFragment")
            laNavigateAnim.isVisible = false
            clNavigationOff.isVisible = false
            val bundle = Bundle()
            bundle.putBoolean(ARG_BALLISTIC_PLUS, isBallistic)
            safeNavigate(R.id.mapFragment, bundle)
        }
    }

    private fun animateAndNavigateToSettingMenu() {
        Log.d(TAG, "animateAndNavigateToSettingMenu: starting animation")
        laSettingsAnim.isVisible = true
        handleSettingsAnimation(R.raw.setting_icon_entry_anim) {
            Log.d(TAG, "animateAndNavigateToSettingMenu: done → settingMenuFragment")
            laSettingsAnim.isVisible = false
            doSlideUpAnim(clBgMyF77)
            doSlideUpAnim(clBgBattery)
            doSlideUpAnim(clBgSetting)
            doSlideUpAnim(laSettingsAnim)
            doSlideDownAnim(clBgMedia)
            doSlideDownAnim(clBgControls)
            doSlideDownAnim(clBgTpms)
            doSlideDownAnim(clBgNavigate)
            doSlideDownAnim(laNavigateAnim)
            doSlideDownAnim(laMusicAnim)
            laSettingsAnim.post {
                if (!isAdded) {
                    Log.w(TAG, "animateAndNavigateToSettingMenu: fragment not added, skip")
                    return@post
                }
                safeNavigate(R.id.action_menuFragment_to_settingMenuFragment)
            }
        }
    }

    private fun handleMyF77Animation(animJsonPath: Int, actionOnEnd: (() -> Unit)? = null) {
        Log.d(TAG, "handleMyF77Animation: placeholder (laF77Anim disabled)")
    }

    private fun handleMusicAnimation(animJsonPath: Int, actionOnEnd: (() -> Unit)? = null) {
        if (laMusicAnim.isAnimating) {
            Log.d(TAG, "handleMusicAnimation: cancelling previous")
            laMusicAnim.cancelAnimation()
        }
        laMusicAnim.removeAllAnimatorListeners()
        laMusicAnim.isVisible = true
        laMusicAnim.setAnimation(animJsonPath)
        laMusicAnim.speed = 0.75f
        laMusicAnim.playAnimation()
        laMusicAnim.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator) { Log.d(TAG, "laMusicAnim: start") }
            override fun onAnimationEnd(p0: Animator) { Log.d(TAG, "laMusicAnim: end"); actionOnEnd?.invoke() }
            override fun onAnimationCancel(p0: Animator) { Log.d(TAG, "laMusicAnim: cancel") }
            override fun onAnimationRepeat(p0: Animator) {}
        })
    }

    private fun handleNavigateAnimation(animJsonPath: Int, actionOnEnd: (() -> Unit)? = null) {
        if (laNavigateAnim.isAnimating) {
            Log.d(TAG, "handleNavigateAnimation: cancelling previous")
            laNavigateAnim.cancelAnimation()
        }
        laNavigateAnim.removeAllAnimatorListeners()
        laNavigateAnim.isVisible = true
        laNavigateAnim.setAnimation(animJsonPath)
        laNavigateAnim.speed = 0.75f
        laNavigateAnim.playAnimation()
        laNavigateAnim.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator) { Log.d(TAG, "laNavigateAnim: start") }
            override fun onAnimationEnd(p0: Animator) { Log.d(TAG, "laNavigateAnim: end"); actionOnEnd?.invoke() }
            override fun onAnimationCancel(p0: Animator) { Log.d(TAG, "laNavigateAnim: cancel") }
            override fun onAnimationRepeat(p0: Animator) {}
        })
    }

    private fun handleSettingsAnimation(animJsonPath: Int, actionOnEnd: (() -> Unit)? = null) {
        if (laSettingsAnim.isAnimating) {
            Log.d(TAG, "handleSettingsAnimation: cancelling previous")
            laSettingsAnim.cancelAnimation()
        }
        laSettingsAnim.removeAllAnimatorListeners()
        laSettingsAnim.setAnimation(animJsonPath)
        laSettingsAnim.speed = 1f
        laSettingsAnim.playAnimation()
        laSettingsAnim.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator) { Log.d(TAG, "laSettingsAnim: start") }
            override fun onAnimationEnd(p0: Animator) { Log.d(TAG, "laSettingsAnim: end"); actionOnEnd?.invoke() }
            override fun onAnimationCancel(p0: Animator) { Log.d(TAG, "laSettingsAnim: cancel") }
            override fun onAnimationRepeat(p0: Animator) {}
        })
    }

    private fun doSlideUpAnim(view: View, shouldListenAnim: Boolean = false, actionOnEnd: (() -> Unit)? = null) {
        if (shouldListenAnim) {
            animSlideUp.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {}
                override fun onAnimationEnd(animation: Animation?) { actionOnEnd?.invoke() }
                override fun onAnimationRepeat(animation: Animation?) {}
            })
        }
        view.startAnimation(animSlideUp)
    }

    private fun doSlideDownAnim(view: View, shouldListenAnim: Boolean = false, actionOnEnd: (() -> Unit)? = null) {
        if (shouldListenAnim) {
            animSlideDown.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {}
                override fun onAnimationEnd(animation: Animation?) { actionOnEnd?.invoke() }
                override fun onAnimationRepeat(animation: Animation?) {}
            })
        }
        view.startAnimation(animSlideDown)
    }

    private fun doSlideUpReverseAnim(view: View, shouldListenAnim: Boolean = false, actionOnEnd: (() -> Unit)? = null) {
        if (shouldListenAnim) {
            animSlideUpReverse.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {}
                override fun onAnimationEnd(animation: Animation?) { actionOnEnd?.invoke() }
                override fun onAnimationRepeat(animation: Animation?) {}
            })
        }
        view.startAnimation(animSlideUpReverse)
    }

    private fun doSlideDownReverseAnim(view: View, shouldListenAnim: Boolean = false, actionOnEnd: (() -> Unit)? = null) {
        if (shouldListenAnim) {
            animSlideDownReverse.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {}
                override fun onAnimationEnd(animation: Animation?) { actionOnEnd?.invoke() }
                override fun onAnimationRepeat(animation: Animation?) {}
            })
        }
        view.startAnimation(animSlideDownReverse)
    }

    private fun doPressScaleAnim(view: View, shouldListenAnim: Boolean = false, actionOnEnd: (() -> Unit)? = null) {
        if (shouldListenAnim) {
            animPressScale.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {
                    Log.d(TAG, "pressScaleAnim: start"); actionOnEnd?.invoke()
                }
                override fun onAnimationEnd(animation: Animation?) { Log.d(TAG, "pressScaleAnim: end") }
                override fun onAnimationRepeat(animation: Animation?) {}
            })
        }
        view.startAnimation(animPressScale)
    }

    private fun simulateTpms() {
        var tpmsFront = 0
        var tpmsBack = 0
        tpmsJob = CoroutineScope(Dispatchers.Main).launch {
            tvTpmsFrontValue.text = tpmsFront.toString()
            tvTpmsRearValue.text = tpmsBack.toString()
            ivTpmsStateFront.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_tpms_pff))
            ivTpmsStateRear.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_tpms_pff))
            while (isActive) {
                delay(5000)
                tpmsFront = (1..36).random()
                tpmsBack = (1..36).random()
                tvTpmsFrontValue.text = tpmsFront.toString()
                tvTpmsRearValue.text = tpmsBack.toString()
                ivTpmsStateFront.setImageDrawable(
                    ContextCompat.getDrawable(requireContext(), if (tpmsFront < 20) R.drawable.ic_tpms_error else R.drawable.ic_tpms_normal)
                )
                ivTpmsStateRear.setImageDrawable(
                    ContextCompat.getDrawable(requireContext(), if (tpmsBack < 20) R.drawable.ic_tpms_error else R.drawable.ic_tpms_normal)
                )
            }
        }
    }

    private fun simulateNavigation() {
        navigateJob = CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                val rotation = if (ivManeuver.scaleX == -1f) 1f else -1f
                delay(1000)
                clNavigationOff.isVisible = false
                clNavigateStart.isVisible = true
                ivManeuver.scaleX = rotation
                tvDistance.text = (1..100).random().toString()
            }
        }
    }

    // ==================================================================================
    // ENUM
    // ==================================================================================

    enum class MenuPosition {
        MyF77,
        Battery,
        Setting,
        Music,
        Controls,
        Tpms,
        Navigate,
        Enter
    }
}
