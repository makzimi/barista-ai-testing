package ru.otus.barista.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import ru.otus.barista.core.designsystem.DesignSystemTestTags
import ru.otus.barista.core.designsystem.ThemePreviews
import ru.otus.barista.core.designsystem.theme.BaristaSpacing
import ru.otus.barista.core.designsystem.theme.BaristaTheme

/** An already formatted [price]; when [oldPrice] is set it is shown crossed out before the price. */
@Composable
fun PriceText(
    price: String,
    modifier: Modifier = Modifier,
    oldPrice: String? = null,
    style: TextStyle = MaterialTheme.typography.titleMedium,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(BaristaSpacing.XSmall),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (oldPrice != null) {
            Text(
                text = oldPrice,
                modifier = Modifier.testTag(DesignSystemTestTags.OLD_PRICE),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textDecoration = TextDecoration.LineThrough,
            )
        }
        Text(
            text = price,
            modifier = Modifier.testTag(DesignSystemTestTags.PRICE),
            style = style,
            color = if (oldPrice != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
        )
    }
}

@ThemePreviews
@Composable
private fun PriceTextPreview() {
    BaristaTheme {
        Surface {
            PriceText(price = "219.90 ₽")
        }
    }
}

@ThemePreviews
@Composable
private fun PriceTextDiscountPreview() {
    BaristaTheme {
        Surface {
            PriceText(price = "186.92 ₽", oldPrice = "219.90 ₽")
        }
    }
}
