package io.github.onreg.feature.game.details.impl.ui.mapper

import io.github.onreg.core.ui.components.chip.ChipUI
import io.github.onreg.data.details.api.model.GameCompanyRole
import io.github.onreg.data.details.api.model.GameDetails
import io.github.onreg.feature.game.details.impl.ui.model.GameCompanyRoleUi
import io.github.onreg.feature.game.details.impl.ui.model.GameCompanyUi
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
                ?.format(dateFormatter),
            ratingChip = ChipUI(
                text = "%.1f".format(Locale.getDefault(), model.rating),
                isSelected = true,
            ),
            platforms = platformUiMapper.mapPlatform(model.platforms),
            website = model.website,
            isWebsiteVisible = model.website.isValidHttpUrl(),
            description = model.description.toPlainText(),
            companies = model.companies
                .sortedBy { company ->
                    when (company.role) {
                        GameCompanyRole.Developer -> 0
                        GameCompanyRole.Publisher -> 1
                    }
                }.map { company ->
                    GameCompanyUi(
                        name = company.name,
                        logoUrl = company.logoUrl,
                        role = when (company.role) {
                            GameCompanyRole.Developer -> GameCompanyRoleUi.Developer
                            GameCompanyRole.Publisher -> GameCompanyRoleUi.Publisher
                        },
                    )
                },
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
