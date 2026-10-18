package ru.otus.barista.feature.drinkdetails.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import ru.otus.barista.core.designsystem.BaristaIcons
import ru.otus.barista.core.designsystem.ThemePreviews
import ru.otus.barista.core.designsystem.theme.BaristaSpacing
import ru.otus.barista.core.designsystem.theme.BaristaTheme
import ru.otus.barista.feature.drinkdetails.R
import ru.otus.barista.feature.drinkdetails.presentation.AddOnRowState

@Composable
fun AddOnRow(
    state: AddOnRowState,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag(DrinkDetailsTestTags.addOnRow(state.id)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = state.name, style = MaterialTheme.typography.bodyLarge)
            val colors = MaterialTheme.colorScheme
            Text(
                text = if (state.limitReached) stringResource(R.string.details_add_on_limit) else state.price,
                style = MaterialTheme.typography.bodySmall,
                color = if (state.limitReached) colors.error else colors.onSurfaceVariant,
            )
        }
        IconButton(
            onClick = onDecrement,
            enabled = state.canDecrement,
            modifier = Modifier.testTag(DrinkDetailsTestTags.addOnDecrement(state.id)),
        ) {
            Icon(
                painter = painterResource(BaristaIcons.Remove),
                contentDescription = stringResource(R.string.details_add_on_decrement, state.name),
            )
        }
        Text(
            text = state.quantity.toString(),
            modifier = Modifier
                .testTag(DrinkDetailsTestTags.addOnQuantity(state.id))
                .padding(horizontal = BaristaSpacing.XSmall),
            style = MaterialTheme.typography.titleMedium,
        )
        IconButton(
            onClick = onIncrement,
            enabled = !state.limitReached,
            modifier = Modifier.testTag(DrinkDetailsTestTags.addOnIncrement(state.id)),
        ) {
            Icon(
                painter = painterResource(BaristaIcons.Add),
                contentDescription = stringResource(R.string.details_add_on_increment, state.name),
            )
        }
    }
}

@ThemePreviews
@Composable
private fun AddOnRowPreview() {
    BaristaTheme {
        Surface {
            AddOnRow(state = DrinkDetailsPreviewData.extraShot, onIncrement = {}, onDecrement = {})
        }
    }
}

@ThemePreviews
@Composable
private fun AddOnRowLimitReachedPreview() {
    BaristaTheme {
        Surface {
            AddOnRow(state = DrinkDetailsPreviewData.syrup, onIncrement = {}, onDecrement = {})
        }
    }
}
