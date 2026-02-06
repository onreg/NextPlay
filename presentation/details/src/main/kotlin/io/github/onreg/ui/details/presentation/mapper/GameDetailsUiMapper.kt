package io.github.onreg.ui.details.presentation.mapper

import io.github.onreg.data.details.api.model.GameDetails
import io.github.onreg.ui.details.presentation.model.GameDetailsUi
import java.net.URI
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

public interface GameDetailsUiMapper {
    public fun map(details: GameDetails): GameDetailsUi
}

public class GameDetailsUiMapperImpl
    @Inject
    constructor() : GameDetailsUiMapper {
        private val releaseDateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US)

        override fun map(details: GameDetails): GameDetailsUi {
            val websiteUrl = details.websiteUrl
            return GameDetailsUi(
                id = details.id.toString(),
                title = details.name,
                bannerImageUrl = details.bannerImageUrl,
                releaseDate = details.releaseDate
                    ?.atZone(ZoneOffset.UTC)
                    ?.format(releaseDateFormatter)
                    .orEmpty(),
                rating = details.rating?.let { rating -> String.format(Locale.US, "%.1f", rating) },
                websiteUrl = websiteUrl,
                isWebsiteVisible = isValidWebsiteUrl(websiteUrl),
                descriptionHtml = details.descriptionHtml,
                developers = details.developers.map { developer -> developer.name },
            )
        }

        private fun isValidWebsiteUrl(url: String?): Boolean {
            val uri = url
                ?.takeIf(String::isNotBlank)
                ?.let { value -> runCatching { URI(value) }.getOrNull() }
            val scheme = uri?.scheme?.lowercase(Locale.US)
            return scheme == "http" || scheme == "https"
        }
    }
