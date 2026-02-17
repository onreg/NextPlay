package io.github.onreg.feature.game.details.impl.mapper

import io.github.onreg.core.ui.components.chip.ChipUI
import io.github.onreg.core.ui.format.ReleaseDateFormatter
import io.github.onreg.core.util.android.resources.ResourcesProvider
import io.github.onreg.data.details.api.model.GameCompanyRole
import io.github.onreg.data.details.api.model.GameDetails
import io.github.onreg.feature.game.details.impl.R
import io.github.onreg.feature.game.details.impl.model.GameCompanyUi
import io.github.onreg.feature.game.details.impl.model.GameDetailsUi
import io.github.onreg.ui.platform.mapper.PlatformUiMapper
import java.net.URI
import java.util.Locale
import javax.inject.Inject

internal interface GameDetailsUiMapper {
    fun map(model: GameDetails): GameDetailsUi
}

internal class GameDetailsUiMapperImpl
    @Inject
    constructor(
        private val platformUiMapper: PlatformUiMapper,
        private val resourcesProvider: ResourcesProvider,
    ) : GameDetailsUiMapper {
        override fun map(model: GameDetails): GameDetailsUi = GameDetailsUi(
            title = model.title,
            imageUrl = model.imageUrl,
            releaseDate = ReleaseDateFormatter.format(model.releaseDate),
            ratingChip = ChipUI(
                text = model.rating.toString(),
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
                        role = mapRole(company.role),
                    )
                },
            isBookmarked = false,
        )

        private fun mapRole(role: GameCompanyRole): String = when (role) {
            GameCompanyRole.Developer -> resourcesProvider.getString(R.string.role_developer)
            GameCompanyRole.Publisher -> resourcesProvider.getString(R.string.role_publisher)
        }

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
