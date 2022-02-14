package dev.rstockbridge.showstats2.ui.composables.shows

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
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
import dev.rstockbridge.showstats2.R
import dev.rstockbridge.showstats2.api.models.Setlist
import dev.rstockbridge.showstats2.appmodels.Show
import dev.rstockbridge.showstats2.ui.theme.Purple

@Composable
fun ShowsScreen(shows: List<Show>) {
    val savedShows = rememberSaveable {
        mutableStateOf(shows)
    }

    Shows(savedShows.value)
}

@Composable
fun Shows(shows: List<Show>) {
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(all = 8.dp)
    ) {
        items(items = shows) { show ->
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
        AnnotatedArtistClickableText(show.getArtistNames(), show.getUrls())
    }
}

@Composable
fun AnnotatedArtistClickableText(artistNames: List<String>, urls: List<String>) {
    val resources = LocalContext.current.resources

    val annotatedText = buildAnnotatedString {
        withStyle(
            style = SpanStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = if (isSystemInDarkTheme()) {
                    Color.White
                } else {
                    Color.Black
                }
            )
        ) {
            append(
                resources.getQuantityString(
                    R.plurals.artist_plural,
                    artistNames.size
                )
            )
        }

        for (i in artistNames.indices) {
            val artistName = artistNames[i]
            val url = urls[i]

            pushStringAnnotation(
                tag = "URL",
                annotation = url
            )
            withStyle(
                style = SpanStyle(
                    fontSize = 16.sp,
                    color = Purple,
                    textDecoration = TextDecoration.Underline
                )
            ) {
                append(artistName)
                if (i < artistNames.size - 1) {
                    append("\n")
                }
            }

            pop()
        }
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
