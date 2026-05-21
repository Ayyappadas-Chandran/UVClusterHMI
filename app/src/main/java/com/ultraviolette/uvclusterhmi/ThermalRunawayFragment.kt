package com.ultraviolette.uvclusterhmi

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.ultraviolette.uvclusterhmi.domain.ennumerate.ButtonNavigation
import com.ultraviolette.uvclusterhmi.ui.viewModel.CarViewModel
import com.ultraviolette.uvclusterhmi.utils.Utilities
import com.ultraviolette.uvclusterhmi.utils.ViewModelFactory
import kotlinx.coroutines.launch
import kotlin.getValue

class ThermalRunawayFragment : Fragment() {

    private lateinit var tvPark: TextView
    private val carViewModel: CarViewModel by activityViewModels { ViewModelFactory(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_thermal_runaway, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
      //  initClickListener()
        initObserver()
    }

    private fun initViews(view: View) {
        tvPark = view.findViewById(R.id.tvPark)
    }

    private fun initClickListener() {
        tvPark.setOnClickListener {
            findNavController().navigate(R.id.dashboardFragment)
        }
    }
    private fun initObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {


                launch {
                    carViewModel.tellTales.collect { tellTales ->
                        val isThermalRunaway = tellTales.thermalRunway == 1
                        val isHover=tellTales.modeHover==1
                        /*if (!isThermalRunaway) {
                            if(!isHover)
                            findNavController().navigate(R.id.dashboardFragment)
                            else
                                findNavController().navigate(R.id.debugFragment)

                        }*/

                    }
                }
            }
        }
    }

}



