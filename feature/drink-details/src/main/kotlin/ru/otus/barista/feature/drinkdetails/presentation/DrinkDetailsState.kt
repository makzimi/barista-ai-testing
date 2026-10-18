package ru.otus.barista.feature.drinkdetails.presentation

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import ru.otus.barista.core.designsystem.BaristaImages
import ru.otus.barista.core.mvi.MviState
import ru.otus.barista.domain.model.AddOnId
import ru.otus.barista.domain.model.Size

/** [imageRes] is the hero illustration; the mapper picks it by drink id, the default is only seen while loading. */
@Immutable
data class DrinkDetailsState(
    val status: DrinkDetailsStatus = DrinkDetailsStatus.Loading,
    @DrawableRes val imageRes: Int = BaristaImages.drinkFallback,
    val name: String = "",
    val description: String = "",
    val caffeineMg: Int = 0,
    val calories: Int = 0,
    val isNew: Boolean = false,
    val isSoldOut: Boolean = false,
    val selectedSize: Size = Size.S,
    val addOns: ImmutableList<AddOnRowState> = persistentListOf(),
    val totalPrice: String = "",
    val oldTotalPrice: String? = null,
    val discountPercent: Int? = null,
    val discountLabel: String? = null,
    val isFavorite: Boolean = false,
    val canAddToCart: Boolean = false,
) : MviState

/**
 * One add-on line. [limitReached] = the quantity is at the maximum, the "+" button is disabled (rule 6).
 * [imageRes] is the add-on icon.
 */
@Immutable
data class AddOnRowState(
    val id: AddOnId,
    @DrawableRes val imageRes: Int,
    val name: String,
    val price: String,
    val quantity: Int,
    val limitReached: Boolean,
) {
    val canDecrement: Boolean
        get() = quantity > 0
}

@Immutable
sealed interface DrinkDetailsStatus {
    data object Loading : DrinkDetailsStatus

    data object Content : DrinkDetailsStatus

    data class Error(val kind: DrinkDetailsErrorKind, @DrawableRes val imageRes: Int) : DrinkDetailsStatus
}

enum class DrinkDetailsErrorKind {
    NETWORK,
    NOT_FOUND,
    UNKNOWN,
}
