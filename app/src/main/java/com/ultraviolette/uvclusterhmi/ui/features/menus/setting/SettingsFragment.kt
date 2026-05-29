package com.ultraviolette.uvclusterhmi.ui.features.menus.setting

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Log.d
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.ultraviolette.uvclusterhmi.R
import com.ultraviolette.uvclusterhmi.domain.dataModel.ChildItem
import com.ultraviolette.uvclusterhmi.domain.dataModel.SettingMenuItem
import com.ultraviolette.uvclusterhmi.domain.dataModel.TimeZoneItem
import com.ultraviolette.uvclusterhmi.domain.ennumerate.ButtonNavigation
import com.ultraviolette.uvclusterhmi.ui.adapter.LanguageAdapter
import com.ultraviolette.uvclusterhmi.ui.adapter.TimeZoneAdapter
import com.ultraviolette.uvclusterhmi.ui.adapter.VerticalMenuAdapter
import com.ultraviolette.uvclusterhmi.ui.features.settings.bluetooth.BluetoothFragment
import com.ultraviolette.uvclusterhmi.ui.features.settings.data.DataFragment
import com.ultraviolette.uvclusterhmi.ui.features.settings.display.DisplayFragment
import com.ultraviolette.uvclusterhmi.ui.features.settings.factoryReset.FactoryResetFragment
import com.ultraviolette.uvclusterhmi.ui.features.settings.general.GeneralFragment
import com.ultraviolette.uvclusterhmi.ui.features.settings.incognito.IncognitoFragment
import com.ultraviolette.uvclusterhmi.ui.features.settings.sound.SoundFragment
import com.ultraviolette.uvclusterhmi.ui.features.settings.systemUpdates.SystemUpdatesFragment
import com.ultraviolette.uvclusterhmi.ui.features.settings.wifi.WifiFragment
import com.ultraviolette.uvclusterhmi.ClusterApplication
import com.ultraviolette.uvclusterhmi.ui.viewModel.ClusterViewModel
import com.ultraviolette.uvclusterhmi.ui.viewModel.SharedViewModel
import com.ultraviolette.uvclusterhmi.utils.Utilities
import com.ultraviolette.uvclusterhmi.utils.Utilities.setOnSoundClickListener
import com.ultraviolette.uvclusterhmi.utils.ViewModelFactory
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class SettingsFragment : Fragment() {
    private lateinit var ivBack: ImageView
    private lateinit var tvGeneralTitle: TextView
    private lateinit var tvSettings: TextView
    private lateinit var ivBgTopBar: ImageView
    private lateinit var rvSettingMenu: RecyclerView
    private lateinit var rvGeneral: RecyclerView
    private lateinit var clRecyclerView: ConstraintLayout
    private lateinit var vBlocker: View
    private lateinit var clButtonNavigationDetails: ConstraintLayout
    private var verticalMenuAdapter: VerticalMenuAdapter? = null
    private val sharedViewModel by activityViewModels<SharedViewModel> { ViewModelFactory(context = requireContext()) }
    private val clusterViewModel: ClusterViewModel by activityViewModels {
        ClusterViewModel.Factory(requireActivity().application as ClusterApplication)
    }
    private lateinit var navHostFragment: NavHostFragment
    private var adapterPosition = 0
    private var isChildEnter = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_setting_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        initObserver()
        initClickListener()
        if (!sharedViewModel.hasThemeConfigChanged) {
            d("Faizulla", "theme not changed")
            initRecyclerView(0)
        } else {
            d("Faizulla", "theme changed")
            //sharedViewModel.hasThemeConfigChanged = false
	    sharedViewModel.resetThemeChangeFlag()
            initRecyclerView(4)
        }
        val animationSlideUp =
            AnimationUtils.loadAnimation(requireContext(), R.anim.slide_down_fade_reverse)
        rvSettingMenu.startAnimation(animationSlideUp)
    }

    /**
     * Binds UI components from the provided root view using their IDs.
     * @param view The root view containing the layout elements.
     * Initializes:
     * - tvBack,tvGeneralTitle,tvSettings (TextViews)
     * - bgTopBar
     * - rvSettingMenu,rvGeneral (RecyclerViews)
     * - clRecyclerView (ConstraintLayout)
     * - clickableBlockerView (View)
     */
    private fun initViews(view: View) {
        ivBack = view.findViewById(R.id.ivBack)
        tvGeneralTitle = view.findViewById(R.id.tvGeneralTitle)
        tvSettings = view.findViewById(R.id.tvSettings)
        clButtonNavigationDetails = view.findViewById(R.id.clButtonNavigationDetails)
        ivBgTopBar = view.findViewById(R.id.bgTopBar)

        rvSettingMenu = view.findViewById(R.id.rvSettingMenu)
        rvGeneral = view.findViewById(R.id.rvGeneral)

        clRecyclerView = view.findViewById(R.id.clRecyclerView)

        vBlocker = view.findViewById(R.id.vBlocker)
        navHostFragment =
            (childFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment)!!
    }

    /**
     * Initializes click listeners for UI components.
     */
    private fun initClickListener() {
        ivBack.setOnSoundClickListener(requireContext()) {
            findNavController().navigateUp()
        }
    }

    fun handleButtonNavigation(button: Int) {
        when (button) {
            ButtonNavigation.Top.ordinal -> {
                sharedViewModel.handleSettingsChildClick(false)
                if (isChildEnter) {
                    sharedViewModel.handleSettingsChildClick(true)
                    val childNavHost = childFragmentManager.findFragmentById(R.id.nav_host_fragment)
                    val currentChildFragment = childNavHost
                        ?.childFragmentManager
                        ?.primaryNavigationFragment
                    when (currentChildFragment) {
                        is DisplayFragment -> currentChildFragment.handleButtonNavigation(button)
                        is GeneralFragment -> currentChildFragment.handleButtonNavigation(button)
                    }
                    return
                }
                if (adapterPosition <= 0) return
                adapterPosition--
                verticalMenuAdapter?.updateSelectedPosition(adapterPosition)
                rvSettingMenu.scrollToPosition(adapterPosition)
                //for bug no 39 - navigating the respective screen when button press but text and button background not changed.
                // navigateToSelectedScreen(adapterPosition)
            }

            ButtonNavigation.Bottom.ordinal -> {
                sharedViewModel.handleSettingsChildClick(false)
                if (isChildEnter) {
                    sharedViewModel.handleSettingsChildClick(true)
                    val childNavHost = childFragmentManager.findFragmentById(R.id.nav_host_fragment)
                    val currentChildFragment = childNavHost
                        ?.childFragmentManager
                        ?.primaryNavigationFragment
                    when (currentChildFragment) {
                        is DisplayFragment -> currentChildFragment.handleButtonNavigation(button)
                        is GeneralFragment -> currentChildFragment.handleButtonNavigation(button)
                    }
                    return
                }
                if (adapterPosition >= verticalMenuAdapter!!.itemCount - 1) return
                adapterPosition++
                verticalMenuAdapter?.updateSelectedPosition(adapterPosition)
                rvSettingMenu.scrollToPosition(adapterPosition)
                //for bug no 39 - navigating the respective screen when button press but text and button background not changed.
                // navigateToSelectedScreen(adapterPosition)
            }

            ButtonNavigation.Enter.ordinal -> {
                sharedViewModel.handleSettingsChildClick(true)
                if (!isChildEnter) {
                    isChildEnter = true
                    rvSettingMenu.scrollToPosition(adapterPosition)
                    rvSettingMenu.post {
                        rvSettingMenu.findViewHolderForAdapterPosition(adapterPosition)
                            ?.itemView
                            ?.performClick()
                    }
                } else {
                    val childNavHost = childFragmentManager.findFragmentById(R.id.nav_host_fragment)
                    val currentChildFragment = childNavHost
                        ?.childFragmentManager
                        ?.primaryNavigationFragment
                    when (currentChildFragment) {
                        is FactoryResetFragment -> currentChildFragment.handleButtonNavigation(
                            button
                        )

                        is GeneralFragment -> currentChildFragment.handleButtonNavigation(button)
                        is SystemUpdatesFragment -> currentChildFragment.handleButtonNavigation(
                            button
                        )
                    }
                }
            }

            ButtonNavigation.Back.ordinal -> {
                sharedViewModel.handleSettingsChildClick(false)
                if (isChildEnter) {
                    verticalMenuAdapter?.updateSelectedPosition(adapterPosition)
                    val childNavHost = childFragmentManager.findFragmentById(R.id.nav_host_fragment)
                    val currentChildFragment = childNavHost
                        ?.childFragmentManager
                        ?.primaryNavigationFragment
                    when (currentChildFragment) {
                        is BluetoothFragment -> currentChildFragment.handleButtonNavigation(button)
                        is WifiFragment -> currentChildFragment.handleButtonNavigation(button)
                        is DataFragment -> currentChildFragment.handleButtonNavigation(button)
                        is IncognitoFragment -> currentChildFragment.handleButtonNavigation(button)
                        is SoundFragment -> currentChildFragment.handleButtonNavigation(button)
                        is DisplayFragment -> currentChildFragment.handleButtonNavigation(button)
                        is GeneralFragment -> currentChildFragment.handleButtonNavigation(button)
                        is SystemUpdatesFragment -> currentChildFragment.handleButtonNavigation(
                            button
                        )

                        is FactoryResetFragment -> currentChildFragment.handleButtonNavigation(
                            button
                        )
                    }
                    isChildEnter = false
                    return
                }
                findNavController().navigateUp()
            }

            ButtonNavigation.Left.ordinal -> {
                if (!isChildEnter) return
                val childNavHost = childFragmentManager.findFragmentById(R.id.nav_host_fragment)
                val currentChildFragment = childNavHost
                    ?.childFragmentManager
                    ?.primaryNavigationFragment
                when (currentChildFragment) {
                    is BluetoothFragment -> currentChildFragment.handleButtonNavigation(button)
                    is WifiFragment -> currentChildFragment.handleButtonNavigation(button)
                    is DataFragment -> currentChildFragment.handleButtonNavigation(button)
                    is IncognitoFragment -> currentChildFragment.handleButtonNavigation(button)
                    is SoundFragment -> currentChildFragment.handleButtonNavigation(button)
                    is DisplayFragment -> currentChildFragment.handleButtonNavigation(button)
                    is GeneralFragment -> currentChildFragment.handleButtonNavigation(button)
                }
            }

            ButtonNavigation.Right.ordinal -> {
                if (!isChildEnter) return
                val childNavHost = childFragmentManager.findFragmentById(R.id.nav_host_fragment)
                val currentChildFragment = childNavHost
                    ?.childFragmentManager
                    ?.primaryNavigationFragment
                when (currentChildFragment) {
                    is BluetoothFragment -> currentChildFragment.handleButtonNavigation(button)
                    is WifiFragment -> currentChildFragment.handleButtonNavigation(button)
                    is DataFragment -> currentChildFragment.handleButtonNavigation(button)
                    is IncognitoFragment -> currentChildFragment.handleButtonNavigation(button)
                    is SoundFragment -> currentChildFragment.handleButtonNavigation(button)
                    is DisplayFragment -> currentChildFragment.handleButtonNavigation(button)
                    is GeneralFragment -> currentChildFragment.handleButtonNavigation(button)
                }
            }
        }
    }

    //for bug no 39 - navigating the respective screen when button press but text and button background not changed.
    private fun navigateToSelectedScreen(position: Int) {
        val selectedItem = verticalMenuAdapter?.currentList?.get(position)
        selectedItem?.let {
            val navHostFragment =
                childFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
            navHostFragment?.navController?.navigate(it.destination)
        }
    }

    /**
     * Observes LiveData from the [SharedViewModel] to update the settings UI accordingly.
     *
     * - Listens for `childClick` events to inform the `settingsMenuAdapter` whether a child item was clicked.
     * - Listens for `recyclerViewClick` events and updates the UI based on the clicked item's type.
     *   If the click type is `"Time Zone"` or `"Language"`, it:
     *   - Resets the adapter's click state
     *   - Updates the title text
     *   - Displays the content RecyclerView
     *   - Initializes the appropriate adapter (time zone or language)
     */
    private fun initObserver() {
        sharedViewModel.settingsChildClick.observe(viewLifecycleOwner) { isClicked ->
            verticalMenuAdapter?.handleChildClick(isClicked)
        }
        sharedViewModel.rvChildClick.observe(viewLifecycleOwner) { action ->
            if (action?.first != null) {
                return@observe
            }
            when (action?.second) {
                getString(R.string.time_zone) -> {
                    verticalMenuAdapter?.isShowingChildAdapter = true
                    tvGeneralTitle.text = getString(R.string.time_zone)
                    clRecyclerView.isVisible = true
                    initTimeZoneAdapter()
                }

                getString(R.string.language) -> {
                    verticalMenuAdapter?.isShowingChildAdapter = true
                    tvGeneralTitle.text = getString(R.string.language)
                    clRecyclerView.isVisible = true
                    initLanguageAdapter()
                }
            }
        }

        sharedViewModel.settingsBlurStatus.observe(viewLifecycleOwner) { shouldBlur ->
            vBlocker.isVisible = shouldBlur
            if (shouldBlur && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Log.d("BlurEffect", "Blur effect added")
                viewBlur()
            } else {
                Log.d("BlurEffect", "Blur effect removed")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    removeBlur()
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    clusterViewModel.handlebarButton.collect { swiftButton ->
                        val button = Utilities.getButtonState(swiftButton)
                        if (button == ButtonNavigation.None) return@collect
                        handleButtonNavigation(button.ordinal)
                    }
                }
            }
        }
    }

    /**
     * Initialize the RecyclerView with the setting menu items.
     */
    private fun initRecyclerView(position: Int) {
        val menuList = listOf(
            SettingMenuItem(getString(R.string.bluetooth), R.id.bluetoothFragment),
            SettingMenuItem(getString(R.string.wifi), R.id.wifiFragment),
            SettingMenuItem(getString(R.string.data), R.id.dataFragment),
            SettingMenuItem(getString(R.string.incognito), R.id.incognitoFragment),
            SettingMenuItem(getString(R.string.display), R.id.displayFragment),
            SettingMenuItem(getString(R.string.sound), R.id.soundFragment),
            SettingMenuItem(getString(R.string.general), R.id.generalFragment),
            SettingMenuItem(getString(R.string.system_updates), R.id.systemUpdatesFragment),
        )

        verticalMenuAdapter = VerticalMenuAdapter(position, { destinationId, adapterPosition ->
            this.adapterPosition = adapterPosition
            val navHostFragment =
                childFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
            val navController = navHostFragment?.navController
            navController?.navigate(destinationId)
        })
        rvSettingMenu.adapter = verticalMenuAdapter
        verticalMenuAdapter?.submitList(menuList)
    }

    /**
     * Initializes the language selection adapter and displays it in the RecyclerView.
     *
     * This function:
     * - Applies a blur effect to the root view (for Android S and above) to indicate modal interaction.
     * - Creates a list of supported languages from string resources.
     * - Sets up a [com.ultraviolette.uvclusterhmi.ui.adapter.LanguageAdapter] with a click listener:
     *   - Updates the ViewModel with the selected language.
     *   - Hides the RecyclerView after selection.
     *   - Removes the blur effect once a language is selected.
     * - Binds the adapter to the `rvGeneral` RecyclerView and submits the language list.
     */
    private fun initLanguageAdapter() {
        vBlocker.isVisible = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            viewBlur()
        }
        val languageList = listOf(
            getString(R.string.english),
            getString(R.string.german),
            getString(R.string.french),
            getString(R.string.italian),
        )
        val languageAdapter = LanguageAdapter {
            sharedViewModel.saveLanguage(it)
            verticalMenuAdapter?.isShowingChildAdapter = false
            clRecyclerView.isVisible = false
            sharedViewModel.handleRvChildClick(
                ChildItem(
                    type = getString(R.string.language),
                    title = it
                )
            )
            clButtonNavigationDetails.setRenderEffect(null)
            rvSettingMenu.setRenderEffect(null)
            tvSettings.setRenderEffect(null)
            ivBgTopBar.setRenderEffect(null)
            navHostFragment.view?.setRenderEffect(null)
            vBlocker.isVisible = false
        }
        rvGeneral.adapter = languageAdapter
        languageAdapter.submitList(languageList.toList())
    }


    /**
     * Initializes the time zone selection adapter and displays it in the RecyclerView.
     *
     * This function:
     * - Applies a blur effect to the root view (only on Android S and above) to give a modal appearance.
     * - Retrieves the list of available time zones via [getTimeZone].
     * - Sets up a [com.ultraviolette.uvclusterhmi.ui.adapter.TimeZoneAdapter] with a click listener that:
     *   - Updates the ViewModel with the selected time zone and corresponding time.
     *   - Hides the time zone RecyclerView (`clRecyclerView`) once an item is selected.
     *   - Removes the blur effect after selection.
     * - Binds the adapter to the `rvGeneral` RecyclerView and submits the list.
     */
    private fun initTimeZoneAdapter() {
        vBlocker.isVisible = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            viewBlur()
            rvSettingMenu.isVerticalScrollBarEnabled = false
        }
        val timeZone = getTimeZone()
        val timeZoneAdapter = TimeZoneAdapter {
            verticalMenuAdapter?.isShowingChildAdapter = false
            clRecyclerView.isVisible = false
            sharedViewModel.handleRvChildClick(
                ChildItem(
                    type = getString(R.string.time_zone),
                    title = it.timeZone,
                    content = it.time
                )
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                removeBlur()
                rvSettingMenu.isVerticalScrollBarEnabled = true
            }
            vBlocker.isVisible = false

        }
        rvGeneral.adapter = timeZoneAdapter
        timeZoneAdapter.submitList(timeZone)
    }

    /**
     * Removes the blur effect from the root view.
     */
    private fun removeBlur() {
        vBlocker.setRenderEffect(null)
        clButtonNavigationDetails.setRenderEffect(null)
        rvSettingMenu.setRenderEffect(null)
        tvSettings.setRenderEffect(null)
        ivBgTopBar.setRenderEffect(null)
        navHostFragment.view?.setRenderEffect(null)
    }

    /**
     * Applies a blur effect to the root view.
     */
    private fun viewBlur() {
        val blur = RenderEffect.createBlurEffect(4f, 4f, Shader.TileMode.CLAMP)
        clButtonNavigationDetails.setRenderEffect(blur)
        rvSettingMenu.setRenderEffect(blur)
        tvSettings.setRenderEffect(blur)
        ivBgTopBar.setRenderEffect(blur)
        navHostFragment.view?.setRenderEffect(blur)
    }

    /**
     * Generates a list of supported time zones with their current local times.
     *
     * This function:
     * - Defines a list of supported zone IDs.
     * - For each zone, it fetches the current time in that zone.
     * - Maps each zone ID to a user-friendly country label using localized string resources.
     * - Returns a list of [com.ultraviolette.uvclusterhmi.domain.dataModel.TimeZoneItem] containing the display name and time for each zone.
     *
     * @return A list of [com.ultraviolette.uvclusterhmi.domain.dataModel.TimeZoneItem] representing supported time zones and their current time.
     */
    private fun getTimeZone(): ArrayList<TimeZoneItem> {
        val timeZoneList = ArrayList<TimeZoneItem>()
        val supportedZones = listOf(
            "Asia/Kolkata",         // India
            "Europe/Berlin",        // Germany
            "Australia/Sydney",     // Australia
            "America/Sao_Paulo",    // Brazil
            "Asia/Tokyo"            // Japan
        )
        supportedZones.map { zoneId ->
            val zone = ZoneId.of(zoneId)
            val time = ZonedDateTime.now(zone).format(DateTimeFormatter.ofPattern("h:mm a"))
            val countryName = when (zoneId) {
                getString(R.string.asia_kolkata) -> getString(R.string.india_ist)
                getString(R.string.europe_berlin) -> getString(R.string.germany_utc)
                getString(R.string.australia_sydney) -> getString(R.string.australia_aedt)
                getString(R.string.america_sao_paulo) -> getString(R.string.brazil_brt)
                getString(R.string.asia_tokyo) -> getString(R.string.japan_jst)
                else -> zoneId
            }
            TimeZoneItem(countryName, time)
            timeZoneList.add(
                TimeZoneItem(
                    countryName, time
                )
            )
        }
        return timeZoneList
    }

}

