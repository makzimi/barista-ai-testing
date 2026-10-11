package ru.otus.barista.app.debug

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.otus.barista.app.R
import ru.otus.barista.app.fakebackend.FakeBackendConfig
import ru.otus.barista.core.designsystem.BaristaIcons

/** Overflow menu with the "Fail next request" switch of the fake backend (demo of the stale-cache path). */
@Composable
fun DebugMenu(
    config: FakeBackendConfig,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val failNextRequest by config.failNextRequest.collectAsStateWithLifecycle()
    Box(modifier = modifier) {
        IconButton(onClick = { expanded = true }, modifier = Modifier.testTag(AppTestTags.DEBUG_MENU)) {
            Icon(
                painter = painterResource(BaristaIcons.MoreVert),
                contentDescription = stringResource(R.string.debug_menu)
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.debug_fail_next_request)) },
                onClick = {
                    config.setFailNextRequest(!failNextRequest)
                    expanded = false
                },
                modifier = Modifier.testTag(AppTestTags.FAIL_NEXT_REQUEST),
                trailingIcon = {
                    if (failNextRequest) {
                        Icon(painter = painterResource(BaristaIcons.Check), contentDescription = null)
                    }
                },
            )
        }
    }
}
