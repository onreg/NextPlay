package io.github.onreg.feature.release.impl

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel


@Composable
fun ReleasesScreen(
    vm: ReleasesViewModel = hiltViewModel()
) {

    val releases = vm.releases.collectAsState(emptyList())
    LazyColumn {
        items(releases.value) {
            Text(text = it.title)
        }
    }
}