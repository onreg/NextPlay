package io.github.onreg.core.ui.components.image

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import coil3.compose.rememberConstraintsSizeResolver
import coil3.request.ImageRequest
import coil3.request.crossfade
import io.github.onreg.core.ui.R
import io.github.onreg.core.ui.theme.IconsSize

@Composable
public fun DynamicAsyncImage(
    modifier: Modifier = Modifier,
    imageUrl: String,
    contentDescription: String? = null,
) {
    val sizeResolver = rememberConstraintsSizeResolver()
    val asyncImagePainter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .size(sizeResolver)
            .crossfade(true)
            .build()
    )
    val state by asyncImagePainter.state.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .then(sizeResolver)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        contentAlignment = Alignment.Center
    ) {
        Image(
            modifier = Modifier.matchParentSize(),
            painter = asyncImagePainter,
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop
        )

        when (state) {
            is AsyncImagePainter.State.Error -> Icon(
                modifier = Modifier.size(IconsSize.xl),
                painter = painterResource(R.drawable.ic_controller_off_24),
                contentDescription = null
            )

            AsyncImagePainter.State.Empty, is AsyncImagePainter.State.Loading -> Icon(
                modifier = Modifier.size(IconsSize.xl),
                painter = painterResource(R.drawable.ic_controller_24),
                contentDescription = null
            )

            is AsyncImagePainter.State.Success -> Unit
        }
    }
}

@Immutable
private sealed interface LoadingState {
    object Loading : LoadingState
    object Success : LoadingState
    object Error : LoadingState
}