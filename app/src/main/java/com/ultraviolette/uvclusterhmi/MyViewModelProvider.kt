package com.ultraviolette.uvclusterhmi

import com.ultraviolette.uvclusterhmi.ui.viewModel.CarViewModel

object MyViewModelProvider {
    // Backing field for your ViewModel
    lateinit var instance: CarViewModel

    fun init(viewModel: CarViewModel) {
        instance = viewModel
    }
}

