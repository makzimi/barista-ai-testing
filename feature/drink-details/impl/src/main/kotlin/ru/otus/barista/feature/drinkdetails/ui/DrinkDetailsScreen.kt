package ru.otus.barista.feature.drinkdetails.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.otus.barista.core.designsystem.BaristaIcons
import ru.otus.barista.core.designsystem.ThemePreviews
import ru.otus.barista.core.designsystem.component.BadgeKind
import ru.otus.barista.core.designsystem.component.BaristaBadge
import ru.otus.barista.core.designsystem.component.ErrorContent
import ru.otus.barista.core.designsystem.component.FavoriteIcon
import ru.otus.barista.core.designsystem.component.LoadingContent
import ru.otus.barista.core.designsystem.component.PriceText
import ru.otus.barista.core.designsystem.theme.BaristaSpacing
import ru.otus.barista.core.designsystem.theme.BaristaTheme
import ru.otus.barista.feature.drinkdetails.R
import ru.otus.barista.feature.drinkdetails.presentation.DrinkDetailsAction
import ru.otus.barista.feature.drinkdetails.presentation.DrinkDetailsErrorKind
import ru.otus.barista.feature.drinkdetails.presentation.DrinkDetailsState
import ru.otus.barista.feature.drinkdetails.presentation.DrinkDetailsStatus

/** Stateless drink details screen: renders [state], reports every user intent through [onAction]. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrinkDetailsScreen(
    state: DrinkDetailsState,
    onAction: (DrinkDetailsAction) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        modifier = modifier.testTag(DrinkDetailsTestTags.SCREEN),
        topBar = {
            TopAppBar(
                title = { Text(state.name) },
                navigationIcon = {
                    IconButton(
                        onClick = { onAction(DrinkDetailsAction.Back) },
                        modifier = Modifier.testTag(DrinkDetailsTestTags.BACK),
                    ) {
                        Icon(
                            painter = painterResource(BaristaIcons.ArrowBack),
                            contentDescription = stringResource(R.string.details_back),
                        )
                    }
                },
                actions = {
                    if (state.status == DrinkDetailsStatus.Content) {
                        FavoriteIcon(
                            isFavorite = state.isFavorite,
                            onClick = { onAction(DrinkDetailsAction.ToggleFavorite) },
                            modifier = Modifier.testTag(DrinkDetailsTestTags.FAVORITE),
                        )
                    }
                },
            )
        },
        bottomBar = {
            if (state.status == DrinkDetailsStatus.Content) {
                CartBar(state = state, onAddToCart = { onAction(DrinkDetailsAction.AddToCart) })
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        val contentModifier = Modifier
            .padding(padding)
            .fillMaxSize()
        when (val status = state.status) {
            DrinkDetailsStatus.Loading -> LoadingContent(
                modifier = contentModifier.testTag(DrinkDetailsTestTags.LOADING)
            )
            DrinkDetailsStatus.Content -> DrinkDetailsContent(
                state = state,
                onAction = onAction,
                modifier = contentModifier.testTag(DrinkDetailsTestTags.CONTENT),
            )
            is DrinkDetailsStatus.Error -> ErrorContent(
                title = stringResource(status.kind.titleRes()),
                onRetry = { onAction(DrinkDetailsAction.Retry) },
                modifier = contentModifier.testTag(DrinkDetailsTestTags.ERROR),
            )
        }
    }
}

@Composable
private fun DrinkDetailsContent(
    state: DrinkDetailsState,
    onAction: (DrinkDetailsAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(BaristaSpacing.Large),
        verticalArrangement = Arrangement.spacedBy(BaristaSpacing.Large),
    ) {
        ImagePlaceholder(modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(BaristaSpacing.Small)) {
            if (state.isNew) {
                BaristaBadge(text = stringResource(R.string.details_badge_new), kind = BadgeKind.New)
            }
            if (state.discountLabel != null) {
                BaristaBadge(text = state.discountLabel, kind = BadgeKind.Discount)
            }
            if (state.isSoldOut) {
                BaristaBadge(text = stringResource(R.string.details_sold_out), kind = BadgeKind.SoldOut)
            }
        }
        Text(text = state.description, style = MaterialTheme.typography.bodyLarge)
        Text(
            text = stringResource(R.string.details_caffeine, state.caffeineMg) + " · " +
                stringResource(R.string.details_calories, state.calories),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(text = stringResource(R.string.details_size_title), style = MaterialTheme.typography.titleMedium)
        SizeSelector(
            selected = state.selectedSize,
            onSelect = { onAction(DrinkDetailsAction.SelectSize(it)) },
            modifier = Modifier.fillMaxWidth(),
        )
        if (state.addOns.isNotEmpty()) {
            Text(text = stringResource(R.string.details_add_ons_title), style = MaterialTheme.typography.titleMedium)
            state.addOns.forEach { addOn ->
                AddOnRow(
                    state = addOn,
                    onIncrement = { onAction(DrinkDetailsAction.IncrementAddOn(addOn.id)) },
                    onDecrement = { onAction(DrinkDetailsAction.DecrementAddOn(addOn.id)) },
                )
            }
        }
    }
}

@Composable
private fun ImagePlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(180.dp)
            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = "☕", style = MaterialTheme.typography.displayLarge)
    }
}

@Composable
private fun CartBar(
    state: DrinkDetailsState,
    onAddToCart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier, tonalElevation = 3.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = BaristaSpacing.Large, vertical = BaristaSpacing.Medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PriceText(
                price = state.totalPrice,
                oldPrice = state.oldTotalPrice,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .weight(1f)
                    .testTag(DrinkDetailsTestTags.TOTAL_PRICE),
            )
            Button(
                onClick = onAddToCart,
                enabled = state.canAddToCart,
                modifier = Modifier.testTag(DrinkDetailsTestTags.ADD_TO_CART),
            ) {
                Text(
                    text = stringResource(
                        if (state.isSoldOut) R.string.details_sold_out else R.string.details_add_to_cart,
                    ),
                )
            }
        }
    }
}

private fun DrinkDetailsErrorKind.titleRes(): Int = when (this) {
    DrinkDetailsErrorKind.NETWORK -> R.string.details_error_network
    DrinkDetailsErrorKind.NOT_FOUND -> R.string.details_error_not_found
    DrinkDetailsErrorKind.UNKNOWN -> R.string.details_error_unknown
}

@ThemePreviews
@Composable
private fun DrinkDetailsScreenPreview() {
    BaristaTheme {
        DrinkDetailsScreen(state = DrinkDetailsPreviewData.cappuccino, onAction = {})
    }
}

@ThemePreviews
@Composable
private fun DrinkDetailsScreenSoldOutPreview() {
    BaristaTheme {
        DrinkDetailsScreen(state = DrinkDetailsPreviewData.icedLatteSoldOut, onAction = {})
    }
}

@ThemePreviews
@Composable
private fun DrinkDetailsScreenLoadingPreview() {
    BaristaTheme {
        DrinkDetailsScreen(state = DrinkDetailsState(status = DrinkDetailsStatus.Loading), onAction = {})
    }
}

@ThemePreviews
@Composable
private fun DrinkDetailsScreenErrorPreview() {
    BaristaTheme {
        DrinkDetailsScreen(
            state = DrinkDetailsState(status = DrinkDetailsStatus.Error(DrinkDetailsErrorKind.NETWORK)),
            onAction = {},
        )
    }
}
