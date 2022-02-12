package dev.rstockbridge.showstats2.ui.composables

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.rstockbridge.showstats2.*
import dev.rstockbridge.showstats2.R
import dev.rstockbridge.showstats2.api.SetlistfmApi
import dev.rstockbridge.showstats2.api.models.Show
import dev.rstockbridge.showstats2.ui.theme.Purple

@Composable
fun FetchDataScreen(
    snackbarHostState: SnackbarHostState,
    onDataLoaded: (List<Show>) -> Unit
) {
    val viewModel: FetchDataViewModel = viewModel(
        factory = UserNameViewModelFactory(ProductionCoroutineContextProvider(), SetlistfmApi)
    )

    val viewState by viewModel.viewState.collectAsState()

    viewState.shows?.let {
        onDataLoaded(it)
    }

    val goButtonOnClick: (String) -> Unit = { setlistfmID ->
        viewModel.onSetlistfmIdSubmitted(setlistfmID)
    }

    if (viewState.displayProgressBar) {
        ProgressBar()
    } else {
        EnterSetlistfmId(goButtonOnClick)
    }

    viewState.userMessages.firstOrNull()?.let { userMessage ->
        LaunchedEffect(userMessage) {
            snackbarHostState.showSnackbar(userMessage.message)
            viewModel.userMessageShown(userMessage.uniqueId)
        }
    }
}

@Composable
fun EnterSetlistfmId(goButtonOnClick: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth(1f)
            .fillMaxHeight(1f),
        contentAlignment = BiasAlignment(0f, -.5f)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            var text by remember { mutableStateOf("") }

            Text(stringResource(id = R.string.enter_user_id), fontSize = 24.sp)

            TextField(
                value = text,
                onValueChange = { text = it },
                singleLine = true,
                colors = TextFieldDefaults.textFieldColors(
                    cursorColor = Purple,
                    focusedIndicatorColor = Purple
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                keyboardActions = KeyboardActions(
                    onGo = {
                        if (text.isNotEmpty()) {
                            goButtonOnClick(text)
                        }
                    })
            )

            Button(
                onClick = {
                    if (text.isNotEmpty()) {
                        goButtonOnClick(text)
                    }
                }
            ) {
                Text(
                    text = stringResource(R.string.go),
                    fontSize = 20.sp
                )
            }
        }
    }
}
