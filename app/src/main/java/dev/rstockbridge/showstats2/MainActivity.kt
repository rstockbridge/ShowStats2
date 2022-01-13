package dev.rstockbridge.showstats2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.rstockbridge.showstats2.api.models.*
import dev.rstockbridge.showstats2.ui.theme.ShowStats2Theme
import java.util.*
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp

val testSetlist: List<Setlist> = Collections.nCopies(
    30,
    Setlist(
        "2020-03-20",
        SetlistArtist("Talisk"),
        SetlistVenue("The Ark", City("Ann Arbor", Coordinates(123.0, 456.0))),
        "https://www.setlist.fm/setlist/talisk/2020/the-ark-ann-arbor-mi-1b8a9144.html"
    )
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShowStats2Theme {
                Setlists(testSetlist)
            }
        }
    }
}

@Composable
fun Setlists(setlists: List<Setlist>) {
    Surface(
        color = MaterialTheme.colors.background
    ) {
        LazyColumn(contentPadding = PaddingValues(all = 8.dp)) {
            items(items = setlists) { setlist ->
                Setlist(setlist = setlist)
            }
        }
    }
}

@Composable
fun Setlist(setlist: Setlist) {
    Column(modifier = Modifier
        .padding(8.dp)
        .fillMaxWidth(1f)) {
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(stringResource(R.string.date))
                }
                append(setlist.eventDate)
            },
            fontSize = 16.sp
        )
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(stringResource(R.string.venue))
                }
                append(setlist.venue.name)
            },
            fontSize = 16.sp
        )
        AnnotatedClickableText(setlist.artist.name, setlist.url)
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


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ShowStats2Theme {
        Setlists(testSetlist)
    }
}
