package ru.otus.barista.core.testing

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import ru.otus.barista.core.common.Money
import ru.otus.barista.domain.model.AddOnId
import ru.otus.barista.domain.model.Drink
import ru.otus.barista.domain.model.DrinkConfiguration
import ru.otus.barista.domain.model.DrinkDetails
import ru.otus.barista.domain.model.DrinkId
import ru.otus.barista.domain.model.MenuError
import ru.otus.barista.domain.model.MenuFilter
import ru.otus.barista.domain.model.MenuItem
import ru.otus.barista.domain.model.MenuSnapshot
import ru.otus.barista.domain.model.PriceBreakdown
import ru.otus.barista.domain.usecase.ApplyHappyHourUseCase
import ru.otus.barista.domain.usecase.CalculatePriceUseCase
import ru.otus.barista.domain.usecase.ChangeAddOnQuantityUseCase
import ru.otus.barista.domain.usecase.FilterAndSortMenuUseCase
import ru.otus.barista.domain.usecase.GetDrinkDetailsUseCase
import ru.otus.barista.domain.usecase.GetMenuUseCase
import ru.otus.barista.domain.usecase.ObserveFavoritesUseCase
import ru.otus.barista.domain.usecase.ObserveIsFavoriteUseCase
import ru.otus.barista.domain.usecase.ToggleFavoriteUseCase

/*
 * One fake per use case interface of `:domain:api`, for ViewModel tests: a feature sees only these interfaces.
 * Every fake records its calls and has an answer the test can change; the defaults are deliberately dumb
 * (no discount, no filtering, no clamping) — a ViewModel test arranges exactly the answer its scenario needs.
 * The favorites fakes share one `MutableStateFlow`, so a toggle shows up in the observed flow like in the app.
 */

/** Serves [snapshot]; [failNext] fails the next call once. `forceRefresh` of every call is in [calls]. */
class FakeGetMenuUseCase(
    var snapshot: MenuSnapshot = MenuSnapshot(drinks = Create.sampleMenu(), isStale = false),
) : GetMenuUseCase {
    var failNext: Throwable? = null
    val calls: MutableList<Boolean> = mutableListOf()

    override suspend fun invoke(forceRefresh: Boolean): Result<MenuSnapshot> {
        calls += forceRefresh
        val failure = failNext?.also { failNext = null } ?: return Result.success(snapshot)
        return Result.failure(failure)
    }
}

/** Emits [favorites]; pass the same flow to [FakeToggleFavoriteUseCase] / [FakeObserveIsFavoriteUseCase]. */
class FakeObserveFavoritesUseCase(
    val favorites: MutableStateFlow<Set<DrinkId>> = MutableStateFlow(emptySet()),
) : ObserveFavoritesUseCase {
    override fun invoke(): Flow<Set<DrinkId>> = favorites
}

/** One recorded [ToggleFavoriteUseCase] call. */
data class ToggleFavoriteCall(
    val id: DrinkId,
    val isFavoriteNow: Boolean,
)

/** Flips the id in [favorites] and returns the new value; [failure] makes every call fail. */
class FakeToggleFavoriteUseCase(
    private val favorites: MutableStateFlow<Set<DrinkId>> = MutableStateFlow(emptySet()),
) : ToggleFavoriteUseCase {
    var failure: Throwable? = null
    val calls: MutableList<ToggleFavoriteCall> = mutableListOf()

    override suspend fun invoke(id: DrinkId, isFavoriteNow: Boolean): Result<Boolean> {
        calls += ToggleFavoriteCall(id, isFavoriteNow)
        failure?.let { return Result.failure(it) }
        val newValue = !isFavoriteNow
        favorites.update { current -> if (newValue) current + id else current - id }
        return Result.success(newValue)
    }
}

/** No discount unless [setDiscount] arranged one for a drink. */
class FakeApplyHappyHourUseCase : ApplyHappyHourUseCase {
    private val discounts = mutableMapOf<DrinkId, Pair<Money, Int>>()

    fun setDiscount(id: DrinkId, price: Money, percent: Int) {
        discounts[id] = price to percent
    }

    override fun invoke(drinks: List<Drink>): List<MenuItem> = drinks.map { drink ->
        val discount = discounts[drink.id]
        MenuItem(drink = drink, price = discount?.first ?: drink.basePrice, discountPercent = discount?.second)
    }
}

/** Returns what [answer] returns (default: the items unchanged); every filter it was asked for is in [filters]. */
class FakeFilterAndSortMenuUseCase : FilterAndSortMenuUseCase {
    var answer: (items: List<MenuItem>, filter: MenuFilter) -> List<MenuItem> = { items, _ -> items }
    val filters: MutableList<MenuFilter> = mutableListOf()

    override fun invoke(items: List<MenuItem>, filter: MenuFilter): List<MenuItem> {
        filters += filter
        return answer(items, filter)
    }
}

/**
 * Serves the [details] arranged per drink (default: every drink of [Create.sampleMenu], no discount);
 * an unknown id fails with [MenuError.NotFound]; [failNext] fails the next call once.
 */
class FakeGetDrinkDetailsUseCase(drinks: List<Drink> = Create.sampleMenu()) : GetDrinkDetailsUseCase {
    private val details = drinks.associate { it.id to DrinkDetails(drink = it, discountPercent = null) }.toMutableMap()
    var failNext: Throwable? = null
    val calls: MutableList<DrinkId> = mutableListOf()

    fun setDetails(value: DrinkDetails) {
        details[value.drink.id] = value
    }

    override suspend fun invoke(id: DrinkId): Result<DrinkDetails> {
        calls += id
        failNext?.let { failure ->
            failNext = null
            return Result.failure(failure)
        }
        return details[id]?.let { Result.success(it) } ?: Result.failure(MenuError.NotFound(id))
    }
}

/** `id in favorites`, distinct; share [favorites] with [FakeToggleFavoriteUseCase]. */
class FakeObserveIsFavoriteUseCase(
    private val favorites: MutableStateFlow<Set<DrinkId>> = MutableStateFlow(emptySet()),
) : ObserveIsFavoriteUseCase {
    override fun invoke(id: DrinkId): Flow<Boolean> = favorites.map { id in it }.distinctUntilChanged()
}

/** Returns what [answer] returns (default: base price, no discount); every configuration is in [calls]. */
class FakeCalculatePriceUseCase : CalculatePriceUseCase {
    var answer: (details: DrinkDetails, configuration: DrinkConfiguration) -> PriceBreakdown = { details, _ ->
        PriceBreakdown(subtotal = details.drink.basePrice, discountPercent = 0, total = details.drink.basePrice)
    }
    val calls: MutableList<DrinkConfiguration> = mutableListOf()

    override fun invoke(details: DrinkDetails, configuration: DrinkConfiguration): PriceBreakdown {
        calls += configuration
        return answer(details, configuration)
    }
}

/** Returns what [answer] returns (default: quantity + delta, no limits, no checks); every call is in [calls]. */
class FakeChangeAddOnQuantityUseCase : ChangeAddOnQuantityUseCase {
    var answer: (configuration: DrinkConfiguration, addOnId: AddOnId, delta: Int) -> DrinkConfiguration =
        { configuration, addOnId, delta ->
            configuration.copy(addOns = configuration.addOns + (addOnId to configuration.quantityOf(addOnId) + delta))
        }
    val calls: MutableList<Pair<AddOnId, Int>> = mutableListOf()

    override fun invoke(
        details: DrinkDetails,
        configuration: DrinkConfiguration,
        addOnId: AddOnId,
        delta: Int,
    ): DrinkConfiguration {
        calls += addOnId to delta
        return answer(configuration, addOnId, delta)
    }
}
