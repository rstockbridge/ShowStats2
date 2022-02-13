package dev.rstockbridge.showstats2.ui.composables.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import dev.rstockbridge.showstats2.TabScreen
import dev.rstockbridge.showstats2.ui.theme.Black
import dev.rstockbridge.showstats2.ui.theme.TranslucentBlack
import dev.rstockbridge.showstats2.ui.theme.TranslucentWhite
import dev.rstockbridge.showstats2.ui.theme.White

@Composable
fun BottomNavBar(
    tabScreens: List<TabScreen>,
    screenSelected: (TabScreen) -> Boolean,
    bottomNavOnClick: (TabScreen) -> Unit,
    bottomNavBarState: MutableState<Boolean>
) {
    AnimatedVisibility(
        visible = bottomNavBarState.value,
        enter = slideInVertically(initialOffsetY = { it }),
        content = {
            BottomNavigation {
                tabScreens.forEach { screen ->
                    BottomNavigationItem(
                        icon = {
                            Icon(
                                painterResource(id = screen.iconResourceId),
                                contentDescription = null
                            )
                        },
                        label = { Text(stringResource(screen.resourceId)) },
                        selected = screenSelected(screen),
                        selectedContentColor = if (isSystemInDarkTheme()) {
                            Black
                        } else {
                            White
                        },
                        unselectedContentColor = if (isSystemInDarkTheme()) {
                            TranslucentBlack
                        } else {
                            TranslucentWhite
                        },
                        onClick = { bottomNavOnClick(screen) }
                    )
                }
            }
        }
    )
}
