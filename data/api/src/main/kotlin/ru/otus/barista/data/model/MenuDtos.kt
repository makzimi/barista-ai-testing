package ru.otus.barista.data.model

import kotlinx.serialization.Serializable

/** Body of `GET /menu`. */
@Serializable
data class MenuDto(
    val drinks: List<DrinkDto>,
)

/** Body of `GET /drinks/{id}` and one element of [MenuDto.drinks]. [category] is the raw backend string. */
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
