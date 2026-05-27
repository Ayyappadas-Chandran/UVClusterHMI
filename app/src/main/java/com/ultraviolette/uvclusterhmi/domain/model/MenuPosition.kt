package com.ultraviolette.uvclusterhmi.domain.model

/**
 * Each top-level menu tile on the HMI menu screen.
 *
 * Order matches the old [MenuFragment.MenuPosition] ordinals so that
 * integer positions stored in [MenuUiState.selectedPosition] and the
 * [ClusterViewModel] button-navigation maps remain stable.
 */
enum class MenuPosition {
    MyF77,
    Battery,
    Setting,
    Music,
    Controls,
    Tpms,
    Navigate,
    Enter,  // sentinel — not a real tile; indicates "confirm selected tile"
}
