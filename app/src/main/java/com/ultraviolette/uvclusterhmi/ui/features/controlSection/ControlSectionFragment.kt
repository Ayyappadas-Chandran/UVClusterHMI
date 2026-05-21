package com.ultraviolette.uvclusterhmi.ui.features.controlSection

import android.content.res.Configuration
import android.graphics.PorterDuff
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.ultraviolette.uvclusterhmi.R
import com.ultraviolette.uvclusterhmi.domain.dataModel.Notification
import com.ultraviolette.uvclusterhmi.ui.adapter.NotificationAdapter
import com.ultraviolette.uvclusterhmi.ui.adapter.ThemePagerAdapter
import com.ultraviolette.uvclusterhmi.ui.features.MainActivity
import com.ultraviolette.uvclusterhmi.ui.features.settings.bluetooth.BluetoothViewModel
import com.ultraviolette.uvclusterhmi.ui.features.settings.data.DataViewModel
import com.ultraviolette.uvclusterhmi.ui.features.settings.wifi.WifiViewModel
import com.ultraviolette.uvclusterhmi.ui.viewModel.SharedViewModel
import com.ultraviolette.uvclusterhmi.utils.Utilities.setOnSoundClickListener
import com.ultraviolette.uvclusterhmi.utils.ViewModelFactory
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator
import kotlinx.coroutines.launch
import kotlin.math.abs

class ControlSectionFragment : Fragment() {
    private lateinit var tvAutoBrightness: TextView
    private lateinit var ivDay: ImageView
    private lateinit var ivNight: ImageView
    private lateinit var ivAuto: ImageView
    private lateinit var ivHillHold: ImageView
    private lateinit var ivAbs: ImageView
    private lateinit var ivData: ImageView
    private lateinit var ivIncognito: ImageView
    private lateinit var ivWifi: ImageView
    private lateinit var ivBluetooth: ImageView
    private lateinit var ivCloseAll: ImageView
    private lateinit var llDayModeUnselect: LinearLayout
    private lateinit var llNightModeUnselect: LinearLayout
    private lateinit var llAutoModeUnselect: LinearLayout
    private lateinit var llBgHillHold: LinearLayout
    private lateinit var llBgAbs: LinearLayout
    private lateinit var llBgData: LinearLayout
    private lateinit var llBgIncognito: LinearLayout
    private lateinit var llBgWifi: LinearLayout
    private lateinit var llBgBluetooth: LinearLayout
    private lateinit var flBgAutoBrightness: FrameLayout
    private lateinit var sbBrightness: SeekBar
    private lateinit var vpTheme: ViewPager2
    private lateinit var dotsIndicator: DotsIndicator
    private lateinit var rvNotification: RecyclerView
    private lateinit var vpAdapter: ThemePagerAdapter
    private val sharedViewModel by activityViewModels<SharedViewModel> { ViewModelFactory(context = requireContext()) }
    private val wifiViewModel by activityViewModels<WifiViewModel> { ViewModelFactory(context = requireContext()) }
    private val bluetoothViewModel by activityViewModels<BluetoothViewModel> {
        ViewModelFactory(
            context = requireContext()
        )
    }
    private val dataViewModel by activityViewModels<DataViewModel> { ViewModelFactory(context = requireContext()) }
    private lateinit var gestureDetector: GestureDetector
    private var notificationAdapter: NotificationAdapter? = null
    private val viewModel by viewModels<ControlSectionViewModel> { ViewModelFactory(context = requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_control_section, container, false)
        addSwipeGesture(rootView)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        initRecyclerView()
        initViewPager()
        (activity as? MainActivity)?.handleToolbar(true)
        initClickListener()
    }

    override fun onResume() {
        super.onResume()
        initUi()
    }

    /**
     * Binds UI components from the provided root view using their IDs.
     * @param view The root view containing the layout elements.
     * Initializes:
     * - tvAutoBrightness(TextView)
     * - btnCloseAll, ivBluetooth, ivWifi, ivIncognito,(ImageView)
     * - ivDay, ivNight, ivAuto, ivHillHold, ivAbs, ivData,(ImageView)
     * - sbBrightness(Seekbar)
     * - rvNotification,(RecyclerView)
     * - bgAutoBrightness(FrameLayout)
     * - bgData, bgBluetooth, bgIncognito, bgWifi, bgHillHold, bgAbs(LinearLayout)
     * - llDayModeUnselect, llNightModeUnselect, llAutoModeUnselect(LinearLayout)
     * - vpTheme (ViewPager)
     * */
    private fun initViews(view: View) {
        tvAutoBrightness = view.findViewById(R.id.tvAutoBrightness)

        ivCloseAll = view.findViewById(R.id.ivCloseAll)
        ivBluetooth = view.findViewById(R.id.ivBluetooth)
        ivWifi = view.findViewById(R.id.ivWifi)
        ivIncognito = view.findViewById(R.id.ivIncognito)
        ivDay = view.findViewById(R.id.ivDay)
        ivNight = view.findViewById(R.id.ivNight)
        ivAuto = view.findViewById(R.id.ivAuto)
        ivHillHold = view.findViewById(R.id.ivHillHold)
        ivAbs = view.findViewById(R.id.ivAbs)
        ivData = view.findViewById(R.id.ivData)

        sbBrightness = view.findViewById(R.id.sbBrightness)

        rvNotification = view.findViewById(R.id.rvNotification)

        flBgAutoBrightness = view.findViewById(R.id.flBgAutoBrightness)

        llBgData = view.findViewById(R.id.llBgData)
        llBgBluetooth = view.findViewById(R.id.llBgBluetooth)
        llBgIncognito = view.findViewById(R.id.llBgIncognito)
        llBgWifi = view.findViewById(R.id.llBgWifi)
        llBgHillHold = view.findViewById(R.id.llBgHillHold)
        llBgAbs = view.findViewById(R.id.llBgAbs)
        llDayModeUnselect = view.findViewById(R.id.llDayModeUnselect)
        llNightModeUnselect = view.findViewById(R.id.llNightModeUnselect)
        llAutoModeUnselect = view.findViewById(R.id.llAutoModeUnselect)

        vpTheme = view.findViewById(R.id.vpTheme)
        dotsIndicator = view.findViewById(R.id.dotsIndicator)

    }

    /**
     * Initializes the UI components with their respective states.
     */
    private fun initUi() {
        handleBrightnessUi()
        handleModeUi()
        handleHillHoldUi()
        handleAbsUi()
        handleIncognito()
        handleBluetooth()
        handleWifi()
        initObserver()
    }

    private fun initObserver() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    bluetoothViewModel.onBluetoothStateChange.collect {
                        handleBluetooth()
                    }
                }
                launch {
                    wifiViewModel.onWifiStateChange.collect {
                        handleWifi()
                    }
                }
                launch {
                    dataViewModel.onDataStateChange.collect { (state) ->
                        handleDataUi(state)
                    }
                }
            }
        }
    }

    /**
     * Initializes the ViewPager for displaying themes.
     */
    private fun initViewPager() {
        vpAdapter =
            ThemePagerAdapter(listOf(getString(R.string.parallax), getString(R.string.radar)))
        vpTheme.apply {
            adapter = vpAdapter
        }
        dotsIndicator.attachTo(vpTheme)
        val position = if (viewModel.isParallaxEnabled) 0 else 1
        vpTheme.currentItem = position
        vpTheme.setCurrentItem(position, false)

        vpTheme.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                viewModel.saveTheme(position == 0)
            }
        })
    }

    /**
     * Adds swipe gesture detection to navigate back on swipe up.
     */
    private fun addSwipeGesture(rootView: View?) {
        gestureDetector = GestureDetector(
            requireContext(),
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onFling(
                    e1: MotionEvent?,
                    e2: MotionEvent,
                    velocityX: Float,
                    velocityY: Float
                ): Boolean {
                    if (e1 == null) return false
                    val diffY = e2.y - e1.y
                    if (abs(diffY) > 100 && abs(velocityY) > 100 && diffY < 0) {
                        onSwipeUp()
                        return true
                    }
                    return false
                }
            }
        )

        rootView?.setOnTouchListener { v, event ->
            val handled = gestureDetector.onTouchEvent(event)
            if (!handled && event.action == MotionEvent.ACTION_UP) {
                v.performClick()
            }
            true
        }
    }

    /**
     * Navigates back when swiped up.
     */
    private fun onSwipeUp() {
        findNavController().navigateUp()
    }

    /**
     * Initializes the RecyclerView for displaying notifications.
     */
    private fun initRecyclerView() {
        notificationAdapter = NotificationAdapter()
        val notificationList = listOf(
            Notification(
                getString(R.string.confirm_remove),
                getString(R.string.samsung_galaxy_s24_ultra), getString(R.string._30_mins_ago)
            ),
            Notification(
                getString(R.string.confirm_remove),
                getString(R.string.samsung_galaxy_s23_ultra), getString(R.string._40_mins_ago)
            ),
            Notification(
                getString(R.string.confirm_remove), getString(R.string.iphone_13_pro_max),
                getString(R.string._50_mins_ago)
            )
        )
        rvNotification.adapter = notificationAdapter
        notificationAdapter?.submitList(notificationList.toList())
    }


    /**
     * Initializes click listeners for UI components.
     */
    private fun initClickListener() {
        flBgAutoBrightness.setOnSoundClickListener(requireContext()) {
            viewModel.saveBrightnessState(!viewModel.isAutoBrightnessEnabled)
            handleBrightnessUi()
        }
        llBgHillHold.setOnSoundClickListener(requireContext()) {
            viewModel.saveHillHold(!viewModel.isHillHold)
            handleHillHoldUi()
        }
        llBgAbs.setOnSoundClickListener(requireContext()) {
            viewModel.saveCustomModeAbs(!viewModel.isMonoAbs)
            handleAbsUi()
        }
        llBgData.setOnSoundClickListener(requireContext()) {
            //viewModel.saveData(!viewModel.isDataEnabled)
            dataViewModel.setDataState(!viewModel.isDataEnabled)
            //handleDataUi()
        }
        llBgIncognito.setOnSoundClickListener(requireContext()) {
            viewModel.saveIncognito(!viewModel.isIncognitoEnabled)
            handleIncognito()
        }
        ivCloseAll.setOnSoundClickListener(requireContext()) {
            notificationAdapter?.submitList(emptyList<Notification>().toList())
        }
        llDayModeUnselect.setOnSoundClickListener(requireContext()) {
            if (AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_NO) {
                sharedViewModel.hasThemeConfigChanged = true
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            viewModel.saveMode(getString(R.string.day))
            handleModeUi()
        }
        llNightModeUnselect.setOnSoundClickListener(requireContext()) {
            if (AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_YES) {
                sharedViewModel.hasThemeConfigChanged = true
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            viewModel.saveMode(getString(R.string.night))
            handleModeUi()
        }
        llAutoModeUnselect.setOnSoundClickListener(requireContext()) {
            val currentNightMode = resources.configuration.uiMode
            if(currentNightMode == Configuration.UI_MODE_NIGHT_NO) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }else{
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            viewModel.saveMode(getString(R.string.auto))
            handleModeUi()
        }
        sbBrightness.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                if (!viewModel.isAutoBrightnessEnabled) {
                    viewModel.saveBrightness(progress)
                    val brightnessArray = ByteArray(16)
                    val length = 8
                    val msgId = 0x30001B00
                    val value = 0x00
                    brightnessArray[0] = (msgId shr 0).toByte()
                    brightnessArray[1] = (msgId shr 8).toByte()
                    brightnessArray[2] = (msgId shr 16).toByte()
                    brightnessArray[3] = (msgId shr 24).toByte()
                    brightnessArray[4] = (length shr 0).toByte()
                    brightnessArray[5] = (length shr 8).toByte()
                    brightnessArray[6] = (length shr 16).toByte()
                    brightnessArray[7] = (length shr 24).toByte()
                    brightnessArray[8] = (progress shr 0).toByte()
                    brightnessArray[9] = (progress shr 8).toByte()
                    brightnessArray[10] = (progress shr 16).toByte()
                    brightnessArray[11] = (progress shr 24).toByte()
                    brightnessArray[12] = (value shr 0).toByte()
                    brightnessArray[13] = (value shr 8).toByte()
                    brightnessArray[14] = (value shr 16).toByte()
                    brightnessArray[15] = (value shr 24).toByte()
                    Log.d(
                        "SendData",
                        "Brightness Array: ${brightnessArray.joinToString(" ") { "0x%02X".format(it) }}"
                    )
                    // (activity as? MainActivity)?.sendByteArray(0x21700FFF, brightnessArray)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Ignore
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Ignore
            }
        })
    }

    /**
     * Handles the UI state for Hill Hold.
     */
    private fun handleHillHoldUi() {
        llBgHillHold.setBackgroundResource(
            if (viewModel.isHillHold) R.drawable.bg_hill_hold_select
            else R.drawable.bg_hill_hold
        )
        ivHillHold.drawable.setTint(
            ContextCompat.getColor(
                requireContext(),
                if (viewModel.isHillHold) R.color.black else R.color.unSelected
            )
        )
    }

    /**
     * Handles the UI state for Absolute Steering.
     */
    private fun handleAbsUi() {
        llBgAbs.setBackgroundResource(if (viewModel.isMonoAbs) R.drawable.bg_abs else R.drawable.bg_abs_select)
        val tintColor = if (viewModel.isMonoAbs) {
            ContextCompat.getColor(requireContext(), R.color.unSelected)
        } else {
            ContextCompat.getColor(requireContext(), R.color.black)
        }
        ivAbs.drawable.setTint(tintColor)
    }

    /**
     * Handles the UI state for Mode.
     */
    private fun handleModeUi() {
        val isAuto = viewModel.mode == getString(R.string.auto)
        val isDay = viewModel.mode == getString(R.string.day)
        val isNight = viewModel.mode == getString(R.string.night)

        ivNight.isVisible = isNight
        ivDay.isVisible = isDay
        ivAuto.isVisible = isAuto

        llNightModeUnselect.isVisible = !isNight
        llDayModeUnselect.isVisible = !isDay
        llAutoModeUnselect.isVisible = !isAuto
    }

    /**
     * Handles the UI state for Data.
     */
    private fun handleDataUi(state: Boolean) {
        llBgData.setBackgroundResource(
            if (state) R.drawable.bg_data_on
            else R.drawable.bg_control_data
        )
        ivData.drawable.setTint(
            if (state) ContextCompat.getColor(
                requireContext(),
                R.color.black
            ) else ContextCompat.getColor(requireContext(), R.color.unSelected)
        )
    }

    /**
     * Handles the UI state for Brightness.
     */
    private fun handleBrightnessUi() {
        sbBrightness.progress = viewModel.brightnessLevel
        sbBrightness.isEnabled = !viewModel.isAutoBrightnessEnabled
        flBgAutoBrightness.setBackgroundResource(
            if (viewModel.isAutoBrightnessEnabled) R.drawable.bg_auto_brightness
            else R.drawable.bg_manual_brightness
        )
        tvAutoBrightness.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                if (viewModel.isAutoBrightnessEnabled) R.color.black else R.color.white
            )
        )
        handleSeek(if (viewModel.isAutoBrightnessEnabled) R.color.unSelected else R.color.white)
    }

    /**
     * Handles the UI state for Incognito.
     */
    private fun handleIncognito() {
        llBgIncognito.setBackgroundResource(
            if (viewModel.isIncognitoEnabled) R.drawable.bg_incognito_on
            else R.drawable.bg_control_incognito
        )
        ivIncognito.drawable.setTint(
            if (viewModel.isIncognitoEnabled) ContextCompat.getColor(
                requireContext(),
                R.color.black
            ) else ContextCompat.getColor(requireContext(), R.color.unSelected)
        )
    }

    /**
     * Handles the UI state for Wifi.
     */
    private fun handleWifi() {
        llBgWifi.setBackgroundResource(
            if (wifiViewModel.isWifiEnabled()) R.drawable.bg_wifi_on
            else R.drawable.bg_control_wifi
        )
        ivWifi.drawable.setTint(
            if (wifiViewModel.isWifiEnabled()) ContextCompat.getColor(
                requireContext(),
                R.color.black
            ) else ContextCompat.getColor(requireContext(), R.color.unSelected)
        )
    }

    /**
     * Handles the UI state for Bluetooth.
     */
    private fun handleBluetooth() {
        llBgBluetooth.setBackgroundResource(
            if (bluetoothViewModel.isBluetoothEnabled()) R.drawable.bg_bluetooth_on
            else R.drawable.bg_control_bluetooth
        )
        ivBluetooth.drawable.setTint(
            if (bluetoothViewModel.isBluetoothEnabled()) ContextCompat.getColor(
                requireContext(),
                R.color.black
            ) else ContextCompat.getColor(requireContext(), R.color.unSelected)
        )
    }

    /**
     * Handles the UI state for SeekBar.
     */
    private fun handleSeek(drawableTint: Int) {
        val layerDrawable =
            sbBrightness.progressDrawable as? LayerDrawable
        layerDrawable?.let {
            val progressLayer = it.findDrawableByLayerId(android.R.id.progress)
            progressLayer?.setColorFilter(
                ContextCompat.getColor(requireContext(), drawableTint),
                PorterDuff.Mode.SRC_IN
            )
        }
    }
}