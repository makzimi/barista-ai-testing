package ru.otus.barista.feature.drinkdetails.ui

import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import ru.otus.barista.core.designsystem.ThemePreviews
import ru.otus.barista.core.designsystem.theme.BaristaTheme
import ru.otus.barista.feature.drinkdetails.R
import ru.otus.barista.feature.drinkdetails.api.Size

@Composable
fun SizeSelector(
    selected: Size,
    onSelect: (Size) -> Unit,
    modifier: Modifier = Modifier,
) {
    val sizes = Size.entries
    SingleChoiceSegmentedButtonRow(modifier = modifier) {
        sizes.forEachIndexed { index, size ->
            SegmentedButton(
                selected = size == selected,
                onClick = { onSelect(size) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = sizes.size),
                modifier = Modifier.testTag(DrinkDetailsTestTags.size(size)),
                label = { Text(stringResource(R.string.details_size_option, size.name, size.volumeMl)) },
            )
        }
    }
}

@ThemePreviews
@Composable
private fun SizeSelectorPreview() {
    BaristaTheme {
        Surface {
            SizeSelector(selected = Size.M, onSelect = {})
        }
    }
}
