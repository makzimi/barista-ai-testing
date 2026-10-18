package ru.otus.barista.feature.drinkdetails.presentation

import kotlinx.collections.immutable.toImmutableList
import ru.otus.barista.domain.model.DrinkConfiguration
import ru.otus.barista.domain.model.DrinkDetails
import ru.otus.barista.domain.model.MenuError
import ru.otus.barista.domain.model.PriceBreakdown

/** Domain → [DrinkDetailsState]. All formatting happens here, never in composables. */
class DrinkDetailsStateMapper {

    fun mapContent(
        details: DrinkDetails,
        configuration: DrinkConfiguration,
        price: PriceBreakdown,
        isFavorite: Boolean,
    ): DrinkDetailsState {
        val drink = details.drink
        return DrinkDetailsState(
            status = DrinkDetailsStatus.Content,
            name = drink.name,
            description = drink.description,
            caffeineMg = drink.caffeineMg,
            calories = drink.calories,
            isNew = drink.isNew,
            isSoldOut = drink.isSoldOut,
            selectedSize = configuration.size,
            addOns = drink.allowedAddOns.map { addOn ->
                val quantity = configuration.quantityOf(addOn.id)
                AddOnRowState(
                    id = addOn.id,
                    name = addOn.name,
                    price = "+${addOn.price.format()}",
                    quantity = quantity,
                    limitReached = quantity >= DrinkConfiguration.MAX_ADD_ON_QUANTITY,
                )
            }.toImmutableList(),
            totalPrice = price.total.format(),
            oldTotalPrice = if (price.hasDiscount) price.subtotal.format() else null,
            discountPercent = price.discountPercent.takeIf { price.hasDiscount },
            discountLabel = if (price.hasDiscount) "−${price.discountPercent}%" else null,
            isFavorite = isFavorite,
            canAddToCart = !drink.isSoldOut,
        )
    }

    fun mapError(state: DrinkDetailsState, error: Throwable): DrinkDetailsState = state.copy(
        status = DrinkDetailsStatus.Error(
            when (error) {
                is MenuError.Network -> DrinkDetailsErrorKind.NETWORK
                is MenuError.NotFound -> DrinkDetailsErrorKind.NOT_FOUND
                else -> DrinkDetailsErrorKind.UNKNOWN
            },
        ),
        canAddToCart = false,
    )
}
