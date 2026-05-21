package com.ultraviolette.uvclusterhmi

import android.content.Context
import android.os.Bundle
import android.service.games.GameSession
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ultraviolette.uvclusterhmi.utils.Utilities.setOnSoundClickListener
import com.ultraviolette.uvclusterhmi.utils.Utilities.visible
import com.ultraviolette.uvclusterhmi.utils.ViewModelFactory
import kotlinx.coroutines.launch


class HapticFragment : Fragment() {

    private lateinit var tvHapticOn: TextView
    private lateinit var tvHapticOff: TextView
    private lateinit var ivHapticOnSelected: ImageView
    private lateinit var ivHapticOffSelected: ImageView
    private val viewModel: HapticViewModel by viewModels { ViewModelFactory(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_haptic, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
        initObserver()
        initClickListener()
    }

    private fun initView(view: View) {
        tvHapticOn = view.findViewById(R.id.tvHapticOn)
        tvHapticOff = view.findViewById(R.id.tvHapticOff)
        ivHapticOnSelected = view.findViewById(R.id.ivHapticOnSelected)
        ivHapticOffSelected = view.findViewById(R.id.ivHapticOffSelected)
    }

    private fun initClickListener() {
        tvHapticOn.setOnSoundClickListener(requireContext()) {
            viewModel.saveHaptic(true)
        }

        tvHapticOff.setOnSoundClickListener(requireContext()) {
            viewModel.saveHaptic(false)
        }
    }

    private fun initObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->
                    handleHaptic(uiState.isHapticEnabled)
                }
            }
        }
    }

    private fun handleHaptic(isEnable: Boolean) {
        ivHapticOnSelected.visibility = if (isEnable) View.VISIBLE else View.INVISIBLE
        ivHapticOffSelected.visibility = if (!isEnable) View.VISIBLE else View.INVISIBLE
        val selectedTxtColor = ContextCompat.getColor(requireContext(), R.color.white)
        val unselectedTxtColor = ContextCompat.getColor(requireContext(), R.color.unSelected)
        val selectedBgColor = ContextCompat.getColor(requireContext(), R.color.activeSelectionRed)
        val unselectedBgColor = ContextCompat.getColor(requireContext(), R.color.transparent)

        tvHapticOn.apply {
            setTextColor(if (isEnable) selectedTxtColor else unselectedTxtColor)
            setBackgroundColor(if (isEnable) selectedBgColor else unselectedBgColor)
        }

        tvHapticOff.apply {
            setTextColor(if (!isEnable) selectedTxtColor else unselectedTxtColor)
            setBackgroundColor(if (!isEnable) selectedBgColor else unselectedBgColor)
        }
    }


}

