package io.github.onreg.feature.game.details.impl.mapper

import io.github.onreg.core.ui.components.chip.ChipUI
import io.github.onreg.core.util.android.resources.ResourcesProvider
import io.github.onreg.core.util.format.InstantTextFormatter
import io.github.onreg.core.util.format.NumberTextFormatter
import io.github.onreg.data.details.api.model.GameCompanyRole
import io.github.onreg.data.details.api.model.GameDetails
import io.github.onreg.feature.game.details.impl.R
import io.github.onreg.feature.game.details.impl.model.DescriptionToggleUi
import io.github.onreg.feature.game.details.impl.model.GameCompanyUi
import io.github.onreg.feature.game.details.impl.model.GameDescriptionUi
import io.github.onreg.feature.game.details.impl.model.GameDetailsInternalState
import io.github.onreg.feature.game.details.impl.model.GameDetailsUi
import io.github.onreg.ui.platform.mapper.PlatformUiMapper
import java.net.URI
import javax.inject.Inject

internal interface GameDetailsUiMapper {
    fun map(
        model: GameDetails,
        localState: GameDetailsInternalState,
    ): GameDetailsUi
}

internal class GameDetailsUiMapperImpl
@Inject
constructor(
    private val platformUiMapper: PlatformUiMapper,
    private val resourcesProvider: ResourcesProvider,
    private val instantTextFormatter: InstantTextFormatter,
    private val numberTextFormatter: NumberTextFormatter,
) : GameDetailsUiMapper {
    override fun map(
        model: GameDetails,
        localState: GameDetailsInternalState,
    ): GameDetailsUi = GameDetailsUi(
        image = model.imageUrl,
        releaseDate = model.releaseDate
            ?.let { instantTextFormatter.format(instant = it) }
            .orEmpty(),
        rating = ChipUI(
            text = numberTextFormatter.format(value = model.rating),
            isSelected = true,
        ),
        platforms = platformUiMapper.mapPlatform(model.platforms),
        website = model.website?.takeIf { it.isValidHttpUrl() },
        gameDescriptionUi = GameDescriptionUi(
            description = model.description.toPlainText(),
            isExpanded = localState.isDescriptionExpanded,
            descriptionToggleUi = DescriptionToggleUi(
                text = if (localState.isDescriptionExpanded) {
                    resourcesProvider.getString(R.string.read_less)
                } else {
                    resourcesProvider.getString(R.string.read_more)
                },
                isVisible = localState.isReadMoreVisible || localState.isDescriptionExpanded,
            ),
        ),
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
        isBookmarked = localState.isBookmarked,
    )

    private fun mapRole(role: GameCompanyRole): String = when (role) {
        GameCompanyRole.Developer -> resourcesProvider.getString(R.string.role_developer)
        GameCompanyRole.Publisher -> resourcesProvider.getString(R.string.role_publisher)
    }

    private fun String?.isValidHttpUrl(): Boolean {
        val uri = this
            ?.takeIf { it.isNotBlank() }
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
