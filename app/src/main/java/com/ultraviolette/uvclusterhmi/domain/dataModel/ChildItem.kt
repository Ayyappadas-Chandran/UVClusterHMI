package com.ultraviolette.uvclusterhmi.domain.dataModel


/**
 * Represents a general item for use in a RecyclerView.
 *
 * @property type The type or category of the item.
 * @property title The title or main label of the item.
 * @property content Additional descriptive content of the item.
 */
data class ChildItem(
    val type: String = "",
    val title: String = "",
    val content: String = ""
)