package ru.otus.barista.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ru.otus.barista.core.designsystem.ThemePreviews
import ru.otus.barista.core.designsystem.theme.BaristaSpacing
import ru.otus.barista.core.designsystem.theme.BaristaTheme

/**
 * Small label next to a drink: "NEW", "−15%", "Нет в наличии".
 * Named `BaristaBadge` to avoid a clash with Material 3 `Badge`.
 */
@Composable
fun BaristaBadge(
    text: String,
    kind: BadgeKind,
    modifier: Modifier = Modifier,
) {
    val (container, content) = badgeColors(kind)
    Text(
        text = text,
        modifier = modifier
            .background(color = container, shape = RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        style = MaterialTheme.typography.labelSmall,
        color = content,
    )
}

@Composable
private fun badgeColors(kind: BadgeKind): Pair<Color, Color> {
    val colors = MaterialTheme.colorScheme
    return when (kind) {
        BadgeKind.New -> colors.tertiaryContainer to colors.onTertiaryContainer
        BadgeKind.Discount -> colors.errorContainer to colors.onErrorContainer
        BadgeKind.SoldOut -> colors.surfaceVariant to colors.onSurfaceVariant
    }
}

@ThemePreviews
@Composable
private fun BaristaBadgePreview() {
    BaristaTheme {
        Surface {
            Row(
                modifier = Modifier.padding(BaristaSpacing.Small),
                horizontalArrangement = Arrangement.spacedBy(BaristaSpacing.Small),
            ) {
                BaristaBadge(text = "NEW", kind = BadgeKind.New)
                BaristaBadge(text = "−15%", kind = BadgeKind.Discount)
                BaristaBadge(text = "Нет в наличии", kind = BadgeKind.SoldOut)
            }
        }
    }
}
