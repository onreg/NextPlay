package io.github.onreg.feature.game.details.impl.pane

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import java.io.IOException

internal fun readMoreText(isExpanded: Boolean): String =
    if (isExpanded) "Read Less" else "Read More"

internal fun shouldShowSection(items: LazyPagingItems<*>): Boolean {
    if (items.itemCount > 0) {
        return true
    }
    val refreshState = items.loadState.refresh
    return refreshState !is LoadState.Error || refreshState.error !is IOException
}

internal fun openUrl(
    context: Context,
    url: String,
) {
    runCatching {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, url.toUri()).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }
}
