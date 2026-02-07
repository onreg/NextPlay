package io.github.onreg.feature.game.details.impl.ui.mapper

import io.github.onreg.data.details.api.model.GameDetails
import io.github.onreg.feature.game.details.impl.ui.model.GameDetailsUi
import io.github.onreg.ui.platform.mapper.PlatformUiMapper
import java.net.URI
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

internal interface GameDetailsUiMapper {
    fun map(model: GameDetails): GameDetailsUi
}

internal class GameDetailsUiMapperImpl
    @Inject
    constructor(
        private val platformUiMapper: PlatformUiMapper,
    ) : GameDetailsUiMapper {
        private val dateFormatter: DateTimeFormatter =
            DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault())

        override fun map(model: GameDetails): GameDetailsUi = GameDetailsUi(
            gameId = model.gameId,
            title = model.title,
            imageUrl = model.imageUrl,
            releaseDate = model.releaseDate
                ?.atZone(ZoneId.systemDefault())
                ?.toLocalDate()
                ?.format(dateFormatter)
                .orEmpty(),
            platforms = platformUiMapper.mapPlatform(model.platforms),
            website = model.website,
            isWebsiteVisible = model.website.isValidHttpUrl(),
            rating = "%.1f".format(Locale.getDefault(), model.rating),
            description = model.description.toPlainText(),
            developers = model.developers,
        )

        private fun String?.isValidHttpUrl(): Boolean {
            val uri = this
                ?.takeUnless(String::isBlank)
                ?.let { runCatching { URI(it) }.getOrNull() }
            return uri?.scheme == "http" || uri?.scheme == "https"
        }

        private fun String.toPlainText(): String = this
            .replace(Regex("<[^>]*>"), " ")
            .replace("&#39;", "'")
            .replace("&quot;", "\"")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&nbsp;", " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
