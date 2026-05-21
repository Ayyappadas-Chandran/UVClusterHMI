package com.ultraviolette.uvclusterhmi.ui.features.settings.versions

import android.content.Context
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.Log.d
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresPermission
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ultraviolette.uvclusterhmi.R
import com.ultraviolette.uvclusterhmi.domain.dataModel.vcuData.ChargerCtxObc
import com.ultraviolette.uvclusterhmi.domain.dataModel.vcuData.ImxDbgMsg
import com.ultraviolette.uvclusterhmi.domain.dataModel.vcuData.ImxFwVersionMsg
import com.ultraviolette.uvclusterhmi.domain.dataModel.vcuData.TellTales
import com.ultraviolette.uvclusterhmi.ui.viewModel.CarViewModel
import com.ultraviolette.uvclusterhmi.ui.viewModel.SharedViewModel
import com.ultraviolette.uvclusterhmi.utils.ViewModelFactory
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Button
import android.widget.EditText
import androidx.navigation.fragment.findNavController
import com.ultraviolette.uvclusterhmi.BugReportHelper
import com.ultraviolette.uvclusterhmi.data.repository.SharedPreferenceRepoImpl
import com.ultraviolette.uvclusterhmi.domain.ennumerate.ButtonNavigation
import com.ultraviolette.uvclusterhmi.domain.manager.PreferenceManager
import com.ultraviolette.uvclusterhmi.utils.Utilities
import java.io.File


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class fragment_versions : Fragment() {

    private var param1: String? = null
    private var param2: String? = null

    private lateinit var tvBatteryPercent: TextView

    private lateinit var tvMcSwValue: TextView
    private lateinit var tvBmsFwValue: TextView
    private lateinit var tvMcPrdValue: TextView
    private lateinit var tvMcCfgValue: TextView
    private lateinit var tvVcuFwValue: TextView
    private lateinit var tvDispFwVersion: TextView
    private lateinit var tvChargerTypeValue: TextView
    private lateinit var tvChargerVersion: TextView
    private lateinit var tvDcpOrObc: TextView
    private lateinit var tvChargerFwObcValue: TextView
    private lateinit var tvChargerFwExtValue: TextView
    private lateinit var textViewImei: TextView
    private lateinit var textViewTime: TextView
    private lateinit var textViewDate: TextView
    private lateinit var tvRh850Value: TextView

    private lateinit var enterTime: EditText
    private lateinit var setTime: Button
    private val TAG = "TimeSync"
    private lateinit var tvTitle: TextView
    private lateinit var topBar: View
    private lateinit var preferenceManager: PreferenceManager
    private var isHover=false
    private var isCHarging = false

    /*private val carViewModel by viewModels<CarViewModel> {
        ViewModelFactory(requireContext())
    }*/
    private val carViewModel by activityViewModels<CarViewModel> {
        ViewModelFactory(context = requireContext())
    }

    private val viewModel by viewModels<SharedViewModel> {
        ViewModelFactory(requireContext())
    }

    private val sharedViewModel by activityViewModels<SharedViewModel> {
        ViewModelFactory(context = requireContext())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_versions, container, false)
    }

    @RequiresPermission("android.permission.READ_PRIVILEGED_PHONE_STATE")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvTitle = view.findViewById(R.id.tvTitle)

        tvBatteryPercent = view.findViewById(R.id.batteryPercent)
        tvMcSwValue = view.findViewById(R.id.tvMcSwValue)
        tvBmsFwValue = view.findViewById(R.id.tvBmsFwValue)
        tvMcPrdValue = view.findViewById(R.id.tvMcPrdValue)
        tvMcCfgValue = view.findViewById(R.id.tvMcCfgValue)
        tvVcuFwValue = view.findViewById(R.id.tvVcuFwValue)
        tvDispFwVersion = view.findViewById(R.id.tvDispFwVersion)

        tvChargerTypeValue = view.findViewById(R.id.tvChargerTypeValue)
        tvChargerVersion = view.findViewById(R.id.tvChargerVersion)
        tvDcpOrObc = view.findViewById(R.id.tvDcpOrObc)
        tvChargerFwObcValue = view.findViewById(R.id.tvChargerFwObcValue)
        tvChargerFwExtValue = view.findViewById(R.id.tvChargerFwExtValue)
        textViewTime = view.findViewById(R.id.textViewTime)
        textViewDate = view.findViewById(R.id.textViewDate)
        textViewImei = view.findViewById(R.id.textViewImei)
        tvRh850Value=view.findViewById(R.id.tvRh850Value)
        topBar = view.findViewById(R.id.topBar)
        textViewImei.text = "IMEI : " + getImei()
	saveImeiNumber()	

        enterTime = view.findViewById(R.id.enterTime)
        setTime = view.findViewById(R.id.setTime)

        setTime.setOnClickListener {
            val time = enterTime.text.toString()
            Log.d(TAG, "Button clicked. Time value = $time")

            if (time.isNotEmpty()) {
                writeTimeToSystemFile(time)
            } else {
                Log.w(TAG, "Time is empty. Skipping file write.")
            }
        }

        tvTitle.setOnClickListener {
            val helper = BugReportHelper()
            helper.startBugReport(requireContext())
        }

	preferenceManager = PreferenceManager((SharedPreferenceRepoImpl(requireContext()))
        )

        d("ChargerPrefs", "=== RESTORED ON BOOT ===")
        d("ChargerPrefs", "chargerFwExt     = ${preferenceManager.chargerFwExt.ifBlank { "EMPTY" }}")
        d("ChargerPrefs", "chargerFwObc     = ${preferenceManager.chargerFwObc.ifBlank { "EMPTY" }}")
        d("ChargerPrefs", "chargerVersion   = ${preferenceManager.chargerVersion.ifBlank { "EMPTY" }}")
        d("ChargerPrefs", "chargerTypeValue = ${preferenceManager.chargerTypeValue.ifBlank { "EMPTY" }}")

        initObserver()
        initClickListioner()
        //setTimeAndDate()
    }


    private fun writeTimeToSystemFile(time: String) {
        val path = "/data/system/time_sync.txt"
        Log.d(TAG, "Attempting to write to $path")

        try {
            val file = File(path)
            file.writeText(time)   // overwrite
            Log.i(TAG, "Successfully wrote time to $path")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to write file: $path", e)
        }
    }

    /*private fun setTimeAndDate() {
        val date = LocalDate.now()
        val formattedDate = date.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
        val time = LocalTime.now()
        val formattedTime = time.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        textViewTime.text =  formattedTime
        textViewDate.text =  formattedDate
    }*/

     private fun initClickListioner(){

        val gestureDetector = GestureDetector(
            requireContext(),
            object : GestureDetector.SimpleOnGestureListener() {

                override fun onDown(e: MotionEvent): Boolean {
                    return true
                }

                override fun onDoubleTap(e: MotionEvent): Boolean {
                    d("EmergencyFragment", "Double tap detected")
                    findNavController().navigate(R.id.emergencyFragment)
                    return true
                }
            }
        )

        topBar.apply {
            isClickable = true
            isFocusable = true

            setOnTouchListener { _, event ->
                gestureDetector.onTouchEvent(event)
                false 
            }
        }
    }

    private fun initObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    carViewModel.imxDbgMsg.collect { imxDbgMsg ->
                        d("VersionFragment", "imxDbgMsg:$imxDbgMsg")
                        handleImxDbgMsg(imxDbgMsg)
                    }
                }

                launch {
                    carViewModel.imxFwVersionMsg.collect { imxFwVersionMsg ->
                        d("VersionFragment", "imxFwVersionMsg:$imxFwVersionMsg")
                        handleImxFwVersions(imxFwVersionMsg)
                    }
                }
                launch {
                    carViewModel.chargerCtxObc.collect { chargerCtxObc ->
                        d("VersionFragment", "chargerCtxObc:$chargerCtxObc")
                        handleChargeCtx(chargerCtxObc)
                    }
                }
                launch {
                    carViewModel.tellTales.collect { tellTales ->
                        d("VersionFragment", "tellTales:$tellTales")
                        handleTellTales(tellTales)
                    }
                }
                launch {
                    carViewModel.swiftButton.collect { swiftButton ->
                        val button = Utilities.getButtonState(swiftButton)
                        if (ButtonNavigation.Enter == button) {
                            val destination = when {
                                isHover -> R.id.hoverModeFragment
                                isCHarging -> R.id.chargingFragment
                                else -> R.id.dashboardFragment
                            }
                            findNavController().navigate(destination)
                        }
                        if (ButtonNavigation.Left == button) {
                            findNavController().navigate(R.id.action_versionFragment_to_debugFragment)
                        }
                    }

                }

            }
        }
    }

    private fun handleImxDbgMsg(imxDbgMsg: ImxDbgMsg) {
        val soc = imxDbgMsg.soc
        d("UI Update", "soc: $soc, ${viewModel.socLimit}")

        //tvBatteryPercent.text = "$soc%"
    }

    private fun handleTellTales(tellTales: TellTales) {
        val soc = tellTales.batterySoc
	isHover= tellTales.modeHover==1
        isCHarging = tellTales.charger == 1 || tellTales.charger == 2
        d("UI Update handleTellTales", "batterySoc soc: $soc, ${viewModel.socLimit}")
        tvBatteryPercent.text = "$soc%"
    }

    private fun handleImxFwVersions(imxFwVersionMsg: ImxFwVersionMsg) {
        /*        val mcVersion = String(imxFwVersionMsg.mcSwVersion, Charsets.UTF_8)
                val bmsVersion = String(imxFwVersionMsg.bmsFw, Charsets.UTF_8)
                val mcProCode = imxFwVersionMsg.mcProductCode
                val vcuVersion = String(imxFwVersionMsg.vcuFw, Charsets.UTF_8)
                val dispVersion = String(imxFwVersionMsg.displayFw, Charsets.UTF_8)*/

        //dummy
        /*val imxFwVersionMsg = ImxFwVersionMsg(
            bmsFw = byteArrayOf(
                49, 46, 48, 46, 51, 50, 44, 78, 111, 118, 32, 50, 54, 32, 50, 48,
                50, 53, 44, 49, 53, 58, 51, 54, 58, 52, 51, 32, 124, 32, 50, 46,
                52, 32, 124, 32, 48, 120, 48, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
            ),
            vcuFw = byteArrayOf(
                50, 46, 52, 55, 46, 49, 32, 88, 52, 55, 32, 72, 86, 32, 73, 72,
                77, 32, 120, 52, 55, 32, 74, 97, 110, 32, 49, 51, 32, 50, 48, 50,
                54, 32, 50, 48, 58, 48, 48, 58, 53, 48, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
            ),
            mcSwVersion  = byteArrayOf(5, 12, 124, 2, 1, 0, -1, -1, 0, 0),
            mcHwVersion  = byteArrayOf(-60, -82, -77, -28, 0, 0, 0, 0, 0, 0, 0),
            padding      = byteArrayOf(0, 0, 0),
            mcProductCode    = 1296248882u,
            mcDcfChecksum    = 0u,
            displayFw    = byteArrayOf(1, 5),
            chgFw        = byteArrayOf(1, 50, -114, 0),
            chargerType  = 205u,
            extChgFwVersion = 106u
        )*/
        val mcVersion = imxFwVersionMsg.mcSwVersion
        val version = mcVersion
            .takeWhile { it != 0.toByte() }
            .toByteArray()
            .toString(Charsets.US_ASCII)
        Log.d("Firmware", "Size: ${version.length}")
        Log.d("Firmware", "ContentToString: $version")
        val bmsVersion = imxFwVersionMsg.bmsFw
        val mcProCode = imxFwVersionMsg.mcProductCode
        val vcuVersion = imxFwVersionMsg.vcuFw
        val dispVersion = imxFwVersionMsg.displayFw
        Log.d("Firmware", "Size: ${dispVersion.size}")
        Log.d("Firmware", "ContentToString: ${dispVersion.contentToString()}")
        Log.d("Firmware", "Hex: ${dispVersion.joinToString(" ") { "%02X".format(it) }}")
        val firmwareVersion = if (dispVersion.isEmpty()) {
            "-"
        } else {
            dispVersion.decodeToString()               // best attempt
                .substringBefore(' ')     // remove trailing nulls
                .trim()
                .ifBlank { "-" }
        }
        tvRh850Value.text=imxFwVersionMsg.displayFw.toRH850VersionString()

        d("UI Update", "version: $mcVersion, ${viewModel.socLimit}")

        tvMcSwValue.text =
            imxFwVersionMsg.mcSwVersion.toMcVersionString()
        tvBmsFwValue.text = imxFwVersionMsg.bmsFw.toNullTerminatedString().ifBlank { "--" }
        /*tvMcPrdValue.text     = imxFwVersionMsg.displayFw.toNullTerminatedString().ifBlank { "--" }*/
        tvVcuFwValue.text = imxFwVersionMsg.vcuFw.toTrimmedAscii()
        val extFwRaw = imxFwVersionMsg.extChgFwVersion.toInt()
        if (extFwRaw != 0) {
            val extFwStr = (extFwRaw.toDouble() / 100).toString()
            tvChargerFwExtValue.text = extFwStr
            preferenceManager.saveChargerFwExt(extFwStr)
            d("ChargerPrefs", "SAVED chargerFwExt = $extFwStr")
        }
        else {
            Log.d("ChargerPrefs", "SKIPPED chargerFwExt -- extFwRaw is 0")
        }


        val hw = imxFwVersionMsg.mcHwVersion
        val sw = imxFwVersionMsg.mcSwVersion

        val swChanged = sw.isNotEmpty()   // dummy logic since we don't keep old value

        if (hw.isNotEmpty() && swChanged) {

            // take first 4 bytes like memcpy
            val hashBytes = hw.take(4).toByteArray()

            val mcuConfigHashHex = hashBytes.joinToString("") {
                "%02X".format(it.toUByte().toInt())
            }

            Log.d("MC_CFG_HASH", mcuConfigHashHex)

            // If you want to show it in UI (optional)
            tvMcCfgValue.text = mcuConfigHashHex
        }

        val code = imxFwVersionMsg.mcProductCode.toInt()

        val mcProductString = when (code) {

            0x0705302D -> "SEV4"
            0x07053041 -> "SEV6"
            0x07055037 -> "HVS6"

            else -> {
                val asciiBytes = byteArrayOf(
                    (code and 0xFF).toByte(),
                    ((code shr 8) and 0xFF).toByte(),
                    ((code shr 16) and 0xFF).toByte(),
                    ((code shr 24) and 0xFF).toByte()
                )

                asciiBytes
                    .toString(Charsets.UTF_8)
                    .reversed()
                    .trim { it <= ' ' || it.code == 0 }
            }
        }

        Log.d("MC_PRD_CODE", mcProductString)

        tvMcPrdValue.text = mcProductString

	val chargerType = imxFwVersionMsg.chargerType.toInt()
        val fw = imxFwVersionMsg.chgFw

        val chargerName = when (chargerType) {
            202 -> "OBC"
            203 -> "BOOST"
            204 -> "X-OBC"
            205 -> "STATION"
            206 -> "STANDARD"
            else -> null
        }

        if (chargerName != null) {
            // Live data is valid — check if charger name actually changed
            val savedChargerName = preferenceManager.chargerTypeValue

            if (chargerName != savedChargerName) {
                // Charger type changed, update name
                tvChargerTypeValue.text = chargerName
                tvDcpOrObc.text = "DCP_VER" //Dcp version only we are showing
                preferenceManager.saveChargerTypeValue(chargerName)
                d("ChargerPrefs", "SAVED chargerTypeValue = $chargerName (was: $savedChargerName)")
            } else {
                // Same charger type, still restore name from prefs (in case of reboot)
                tvChargerTypeValue.text = preferenceManager.chargerTypeValue.ifBlank { "—" }
                d("ChargerPrefs", "SAME charger type ($chargerName) — restored from prefs")
            }

            // FW version — only update if non-zero bytes
            var obcFw: String? = null
            var dcpFw: String? = null

            when (chargerType) {
                202 -> {
                    if (fw.size >= 2) {
                        val b0 = fw[0].toUByte().toInt()
                        val b1 = fw[1].toUByte().toInt()
                        if (b0 != 0 || b1 != 0) {
                            obcFw = "$b0.$b1"
                        }
                    }
                }
                203, 204, 205, 206 -> {
                    if (fw.size >= 4) {
                        val b0 = fw[0].toUByte().toInt()
                        val b1 = fw[1].toUByte().toInt()
                        val b2 = fw[3].toUByte().toInt()
                        val b3 = fw[2].toUByte().toInt()
                        if (b0 != 0 || b1 != 0) {
                            dcpFw = if (b0 <= 1 && b1 <= 42) "$b0.$b1" else "$b0.$b1.$b3.$b2"
                        }
                    }
                }
            }

            if (obcFw != null) {
                // Valid fw version received — save and show
                tvChargerFwObcValue.text = obcFw
                preferenceManager.saveChargerFwObc(obcFw)
                d("ChargerPrefs", "SAVED chargerFwObc/chargerVersion = $obcFw")
            } else {
                // fw bytes are zero even though charger is connected — show last saved
                tvChargerFwObcValue.text = preferenceManager.chargerFwObc.ifBlank { "—" }
                d("ChargerPrefs", "SKIPPED fw save — bytes are zero, restored: ${preferenceManager.chargerFwObc.ifBlank { "EMPTY" }}")
            }
            if (dcpFw != null)
            {
                tvChargerVersion.text = dcpFw
                preferenceManager.saveChargerVersion(dcpFw)
            }
            else
            {
                tvChargerVersion.text = preferenceManager.chargerVersion.ifBlank { "—" }
                d("ChargerPrefs", "SKIPPED fw save — bytes are zero, chargerVersion restored: ${preferenceManager.chargerVersion.ifBlank { "EMPTY" }}")
            }

        } else {
            // chargerType = 0 → not connected, restore all from prefs
            tvChargerFwExtValue.text = preferenceManager.chargerFwExt.ifBlank { "—" }
            tvChargerFwObcValue.text = preferenceManager.chargerFwObc.ifBlank { "—" }
            tvChargerVersion.text = preferenceManager.chargerVersion.ifBlank { "—" }
            tvChargerTypeValue.text = preferenceManager.chargerTypeValue.ifBlank { "—" }
            d("ChargerPrefs", "  chargerFwExt     = ${preferenceManager.chargerFwExt.ifBlank { "EMPTY" }}")
            d("ChargerPrefs", "  chargerFwObc     = ${preferenceManager.chargerFwObc.ifBlank { "EMPTY" }}")
            d("ChargerPrefs", "  chargerVersion   = ${preferenceManager.chargerVersion.ifBlank { "EMPTY" }}")
            d("ChargerPrefs", "  chargerTypeValue = ${preferenceManager.chargerTypeValue.ifBlank { "EMPTY" }}")
        }
    }
    private fun handleChargeCtx(chargerCtxObc: ChargerCtxObc) {

        /*val chargerCtxObc = ChargerCtxObc(
            chargerLogBoundary = 0u,
            obcStatus = 0uL,
            temperature01 = 0.0f,
            temperature02 = 0.0f,
            temperature03 = 0.0f,
            temperature04 = 0.0f,
            ipAcRmsVoltage = 0.0f,
            ipAcRmsCurrent = 0.0f,
            fanFrequency = 0.0f,
            opFbVoltage = 0.0f,
            opFbCurrent = 0.0f,
            ipAcSignalFreq = 0.0f,
            dcFbVoltage = 0.0f,
            llcFreq = 0.0f,
            opRippleCurrent = 0.0f,
            acVoltageThd = 0.0f,
            chargerConnectionState = 0u,
            chargerType = 0u,
            chargerFwMajorNum = 0u,
            chargerFwMinorNum = 0u,
            chargerViReq = 0u,
            chargerRangeValue = 0u
        )*/
        val chargeType = chargerCtxObc.chargerType
        val chargeVersionMajor = chargerCtxObc.chargerFwMajorNum.toInt()
        val chargeVersionMinor = chargerCtxObc.chargerFwMinorNum.toInt()

        d("ChargerPrefs", "handleChargeCtx — chargeType=$chargeType, major=$chargeVersionMajor, minor=$chargeVersionMinor")
        if (chargerCtxObc.chargerConnectionState.toInt() == 1) {
            if (chargeVersionMajor != 0 || chargeVersionMinor != 0) {
                val fwStr = "$chargeVersionMajor.$chargeVersionMinor"
                tvChargerFwObcValue.text = fwStr
                preferenceManager.saveChargerFwObc(fwStr)
                tvChargerTypeValue.text = "OBC"
                preferenceManager.saveChargerTypeValue("OBC")
                d("ChargerPrefs", "handleChargeCtx SAVED chargerFwObc = $fwStr")
            } else {
                // Zero values — charger disconnected, restore from prefs
                val saved = preferenceManager.chargerFwObc.ifBlank { "—" }
                tvChargerFwObcValue.text = saved
                d(
                    "ChargerPrefs",
                    "handleChargeCtx SKIPPED — zero values, restored chargerFwObc = $saved"
                )
            }
        }

        d("UI Update", "version: $chargeType, ${viewModel.socLimit}")
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            fragment_versions().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
    
       @RequiresPermission("android.permission.READ_PRIVILEGED_PHONE_STATE")
    private fun saveImeiNumber(){
        var temp = getImei()
        viewModel.saveIemiNumber(temp)
    }
    @RequiresPermission("android.permission.READ_PRIVILEGED_PHONE_STATE")
    private fun getImei(): String {
        return try {
            val tm =
                requireContext().getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            tm.getImei(0) ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    // Option A: Most common – null-terminated string
    fun ByteArray.toNullTerminatedString(): String {
        return takeWhile { it != 0.toByte() }
            .toByteArray()
            .decodeToString(throwOnInvalidSequence = false)
            .trim()
    }

    // Option B: Fixed length → trim spaces and nulls
    fun ByteArray.toTrimmedAscii(): String {
        return decodeToString(throwOnInvalidSequence = false)
            .trim { it <= ' ' || it.code == 0 }
            .ifBlank { "-" }
    }

    fun ByteArray.toMcVersionString(): String {
        if (size < 6) return "-"

        val major = this[0].toUByte().toInt()
        val minor = this[1].toUByte().toInt()
        val patch = this[2].toUByte().toInt()

        val subMajor = this[3].toUByte().toInt()
        val subMinor = this[4].toUByte().toInt()
        val subPatch = this[5].toUByte().toInt()

        return "$major.$minor.$patch" +
                "_$subMajor.$subMinor.$subPatch"
    }
    fun ByteArray.toRH850VersionString(): String {
        if (size < 2) return "-"
        val major = this[0].toUByte().toInt()
        val minor = this[1].toUByte().toInt()
        return "$major.$minor"
    }

    // Option C: Major.Minor style (most likely for your 2-byte displayFw)
    fun ByteArray.toMajorMinor(): String {
        if (size < 2) return "-"
        val major = this[0].toUByte().toInt()
        val minor = this[1].toUByte().toInt()
        return "$major.$minor"
    }

    // Option D: BCD style (1 byte = two digits)
    fun Byte.toBcdString(): String {
        val high = (this.toInt() shr 4) and 0x0F
        val low = this.toInt() and 0x0F
        return "$high$low"
    }

    // Option E: Hex debug
    fun ByteArray.toHexString(): String =
        joinToString("") { "%02x".format(it.toUByte()) }
}



