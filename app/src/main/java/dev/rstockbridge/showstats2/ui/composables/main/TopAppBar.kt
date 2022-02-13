package dev.rstockbridge.showstats2.ui.composables.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import dev.rstockbridge.showstats2.R
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun TopAppBar(
    switchUserIdOnClick: () -> Unit,
    licensesOnClick: () -> Unit,
    topAppBarState: MutableState<Boolean>
) {
    var showMenu by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current

    AnimatedVisibility(
        visible = topAppBarState.value,
        enter = slideInVertically(initialOffsetY = { it }),
        content = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.app_name)) },
                actions = {
                    IconButton(
                        onClick = { showMenu = !showMenu }
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.more)
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            onClick = {
                                showMenu = false
                                switchUserIdOnClick()
                            }
                        ) {
                            Text(stringResource(R.string.switch_user_id))
                        }
                        DropdownMenuItem(
                            onClick = licensesOnClick
                        ) {
                            Text(stringResource(R.string.licenses))
                        }
                        DropdownMenuItem(
                            onClick = {
                                uriHandler.openUri("https://github.com/rstockbridge/ShowStats2/blob/main/privacy_policy.md")
                            }
                        ) {
                            Text(stringResource(R.string.privacy_policy))
                        }
                    }
                }
            )
        }
    )
}
