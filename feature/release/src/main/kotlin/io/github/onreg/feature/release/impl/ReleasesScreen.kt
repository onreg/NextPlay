package io.github.onreg.feature.release.impl

import android.R.id.input
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.onreg.data.release.api.Release


@Composable
public fun ReleasesScreen() {
    val vm = hiltViewModel<ReleasesViewModel>()
    val releases = vm.releases.collectAsState(emptyList())
    ReleasesComponent(releases.value)
}

@Composable
internal fun ReleasesComponent(releases: List<Release>) {
    val state by remember {
        mutableStateOf(false)
    }
    LazyColumn {
        items(releases) {
            Text(text = it.title)
        }
    }
}

@Preview
@Composable
private fun Preview() {
    ReleasesComponent(
        releases = listOf(
            Release(
                image = "https://example.com/image1.jpg",
                title = "Test Release 1",
                releaseDate = "2024-06-01",
                genres = setOf("Action", "Adventure"),
                platforms = setOf("PC", "PS5"),
                rating = "9.0"
            ),
            Release(
                image = "https://example.com/image2.jpg",
                title = "Test Release 2",
                releaseDate = "2024-07-15",
                genres = setOf("RPG"),
                platforms = setOf("Xbox", "Switch"),
                rating = "8.5"
            )
        )
    )
}