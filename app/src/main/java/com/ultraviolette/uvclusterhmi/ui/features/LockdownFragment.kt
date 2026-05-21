package com.ultraviolette.uvclusterhmi.ui.features

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.ultraviolette.uvclusterhmi.R
import com.ultraviolette.uvclusterhmi.utils.Utilities.setOnSoundClickListener
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LockdownFragment : Fragment() {
    private lateinit var tvBack: TextView
    private lateinit var clLockdownPark: ConstraintLayout
    private lateinit var clVehicleLocked: ConstraintLayout
    private var simulationJob: Job? = null
    private var isEnteringLockdown = true


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_lockdown, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        isEnteringLockdown = arguments?.getBoolean("isEnteringLockdown") ?: true
        clLockdownPark.isVisible = isEnteringLockdown
        clVehicleLocked.isVisible = !isEnteringLockdown
        tvBack.setOnSoundClickListener(requireContext()) {
            findNavController().navigateUp()
        }
    }

    /**
     * Binds UI components from the provided root view using their IDs.
     *
     * @param view The root view containing the layout elements.
     *
     * Initializes:
     * - tvBack (TextView)
     * - clLockdownPark,clVehicleLocked (ConstraintLayout)
     */
    private fun initViews(view: View) {
        tvBack = view.findViewById(R.id.tvBack)
        clLockdownPark = view.findViewById(R.id.clLockdownPark)
        clVehicleLocked = view.findViewById(R.id.clVehicleLocked)
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).handleToolbar(true)
        //simulateLockdown()
    }

    /**
     * simulate the lockdown UI
     */
    private fun simulateLockdown() {
        simulationJob?.cancel()
        simulationJob = lifecycleScope.launch {
            delay(10000)
            clLockdownPark.visibility = View.VISIBLE
            clVehicleLocked.visibility = View.GONE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        simulationJob?.cancel()
    }
}