package ru.otus.barista.feature.drinkdetails.presentation

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import ru.otus.barista.core.mvi.MviState
import ru.otus.barista.domain.model.AddOnId
import ru.otus.barista.domain.model.Size

@Immutable
data class DrinkDetailsState(
    val status: DrinkDetailsStatus = DrinkDetailsStatus.Loading,
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

/** One add-on line. [limitReached] = the quantity is at the maximum, the "+" button is disabled (rule 6). */
@Immutable
data class AddOnRowState(
    val id: AddOnId,
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

    data class Error(val kind: DrinkDetailsErrorKind) : DrinkDetailsStatus
}

enum class DrinkDetailsErrorKind {
    NETWORK,
    NOT_FOUND,
    UNKNOWN,
}
