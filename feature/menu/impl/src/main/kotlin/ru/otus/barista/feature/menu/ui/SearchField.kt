package ru.otus.barista.feature.menu.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import ru.otus.barista.core.designsystem.BaristaIcons
import ru.otus.barista.core.designsystem.ThemePreviews
import ru.otus.barista.core.designsystem.theme.BaristaSpacing
import ru.otus.barista.core.designsystem.theme.BaristaTheme
import ru.otus.barista.feature.menu.R

@Composable
fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.testTag(MenuTestTags.SEARCH),
        placeholder = { Text(stringResource(R.string.menu_search_hint)) },
        leadingIcon = { Icon(painter = painterResource(BaristaIcons.Search), contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        painter = painterResource(BaristaIcons.Close),
                        contentDescription = stringResource(R.string.menu_search_clear),
                    )
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
    )
}

@ThemePreviews
@Composable
private fun SearchFieldPreview() {
    BaristaTheme {
        Surface {
            SearchField(
                query = "",
                onQueryChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(BaristaSpacing.Small),
            )
        }
    }
}

@ThemePreviews
@Composable
private fun SearchFieldFilledPreview() {
    BaristaTheme {
        Surface {
            SearchField(
                query = "латте",
                onQueryChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(BaristaSpacing.Small),
            )
        }
    }
}
