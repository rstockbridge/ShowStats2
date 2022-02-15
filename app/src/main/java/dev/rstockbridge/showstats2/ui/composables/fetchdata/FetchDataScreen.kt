package dev.rstockbridge.showstats2.ui.composables.fetchdata

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.rstockbridge.showstats2.FetchDataViewModel
import dev.rstockbridge.showstats2.R
import dev.rstockbridge.showstats2.api.models.Setlist
import dev.rstockbridge.showstats2.ui.theme.Purple

@Composable
fun FetchDataScreen(
    snackbarHostState: SnackbarHostState,
    onDataLoaded: (List<Setlist>) -> Unit
) {
    val viewModel = hiltViewModel<FetchDataViewModel>()

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

    Box(
        modifier = Modifier
            .fillMaxWidth(1f)
            .fillMaxHeight(1f), contentAlignment = Alignment.BottomCenter
    ) {
        AnnotatedSetlistfmClickableText(Modifier.padding(0.dp, 0.dp, 0.dp, 16.dp))
    }
}

@Composable
fun AnnotatedSetlistfmClickableText(
    modifier: Modifier = Modifier
) {
    val annotatedText = buildAnnotatedString {
        withStyle(
            style = SpanStyle(
                fontSize = 20.sp,
                color = if (isSystemInDarkTheme()) {
                    Color.White
                } else {
                    Color.Black
                }
            )
        ) {
            append(stringResource(R.string.powered_by))
        }

        pushStringAnnotation(
            tag = "URL",
            annotation = "https://www.setlist.fm/"
        )
        withStyle(
            style = SpanStyle(
                fontSize = 20.sp,
                color = Purple,
                textDecoration = TextDecoration.Underline
            )
        ) {
            append("setlist.fm")
        }

        pop()
    }

    val uriHandler = LocalUriHandler.current

    ClickableText(
        modifier = modifier,
        text = annotatedText,
        onClick = { offset ->
            annotatedText.getStringAnnotations(
                tag = "URL", start = offset,
                end = offset
            )
                .firstOrNull()?.let { annotation ->
                    uriHandler.openUri(annotation.item)
                }
        }
    )
}
