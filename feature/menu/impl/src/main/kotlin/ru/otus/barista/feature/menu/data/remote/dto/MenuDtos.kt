package ru.otus.barista.feature.menu.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class MenuResponseDto(
    val drinks: List<DrinkDto>,
)

@Serializable
data class DrinkDto(
    val id: String,
    val name: String,
    val description: String,
    val category: String,
    val price: Double,
    val caffeineMg: Int,
    val calories: Int,
    val isNew: Boolean = false,
    val isSoldOut: Boolean = false,
    val addOns: List<AddOnDto> = emptyList(),
    val happyHour: HappyHourDto? = null,
)

@Serializable
data class AddOnDto(
    val id: String,
    val name: String,
    val price: Double,
)

/** [from] / [to] are local times in `HH:mm` format. */
@Serializable
data class HappyHourDto(
    val from: String,
    val to: String,
    val percent: Int,
)
