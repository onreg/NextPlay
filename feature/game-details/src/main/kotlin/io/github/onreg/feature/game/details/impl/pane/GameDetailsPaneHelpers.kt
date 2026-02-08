package io.github.onreg.feature.game.details.impl.pane

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems

internal const val COLLAPSED_DESCRIPTION_MAX_LINES: Int = 6

internal fun readMoreText(isExpanded: Boolean): String =
    if (isExpanded) "Read less" else "Read more"

internal enum class SectionVisibility {
    ShowContent,
    ShowLoading,
    Hide,
}

internal fun sectionVisibility(items: LazyPagingItems<*>): SectionVisibility =
    resolveSectionVisibility(
        itemCount = items.itemCount,
        refreshLoadState = items.loadState.refresh,
    )

internal fun resolveSectionVisibility(
    itemCount: Int,
    refreshLoadState: LoadState,
): SectionVisibility {
    if (itemCount > 0) {
        return SectionVisibility.ShowContent
    }
    return when (refreshLoadState) {
        is LoadState.Loading -> {
            SectionVisibility.ShowLoading
        }

        is LoadState.Error -> {
            SectionVisibility.Hide
        }

        is LoadState.NotLoading -> {
            if (refreshLoadState.endOfPaginationReached) {
                SectionVisibility.Hide
            } else {
                SectionVisibility.ShowLoading
            }
        }
    }
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
