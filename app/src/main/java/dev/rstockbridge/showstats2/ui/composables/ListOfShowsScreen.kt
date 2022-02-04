package dev.rstockbridge.showstats2.ui.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.rstockbridge.showstats2.ListOfShowsViewModel
import dev.rstockbridge.showstats2.ListOfShowsViewModelFactory
import dev.rstockbridge.showstats2.ProductionCoroutineContextProvider
import dev.rstockbridge.showstats2.R
import dev.rstockbridge.showstats2.api.SetlistfmApi
import dev.rstockbridge.showstats2.api.models.Show

@Composable
fun ListOfShowsScreen(snackbarHostState: SnackbarHostState) {
    val viewModel: ListOfShowsViewModel = viewModel(
        factory = ListOfShowsViewModelFactory(ProductionCoroutineContextProvider(), SetlistfmApi)
    )
    val viewState by viewModel.viewState.collectAsState()

    val setlistfmId = "rstockbridge"

    LaunchedEffect(setlistfmId) {
        viewModel.fetchData(setlistfmId)
    }

    viewState.shows?.let {
        Shows(listOfShows = it)
    }

    if (viewState.networkCallInProgress) {
        ProgressBar()
    }

    viewState.userMessages.firstOrNull()?.let { userMessage ->
        val stringResource = stringResource(userMessage.message)
        LaunchedEffect(userMessage) {
            snackbarHostState.showSnackbar(stringResource)
            viewModel.userMessageShown(userMessage.uniqueId)
        }
    }
}

@Composable
fun Shows(listOfShows: List<Show>) {
    LazyColumn(contentPadding = PaddingValues(all = 8.dp)) {
        items(items = listOfShows) { show ->
            Show(show = show)
        }
    }
}

@Composable
fun Show(show: Show) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(1f)
    ) {
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(stringResource(R.string.date))
                }
                append(show.eventDate)
            },
            fontSize = 16.sp
        )
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(stringResource(R.string.venue))
                }
                append(show.venue.name)
            },
            fontSize = 16.sp
        )
        AnnotatedClickableText(show.artist.name, show.url)
    }
}

@Composable
fun AnnotatedClickableText(artistName: String, url: String) {
    val annotatedText = buildAnnotatedString {
        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp)) {
            append(stringResource(R.string.artist))
        }

        pushStringAnnotation(
            tag = "URL",
            annotation = url
        )
        withStyle(
            style = SpanStyle(
                fontSize = 16.sp,
                color = Color.Magenta,
                textDecoration = TextDecoration.Underline
            )
        ) {
            append(artistName)
        }

        pop()
    }

    val uriHandler = LocalUriHandler.current

    ClickableText(
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
