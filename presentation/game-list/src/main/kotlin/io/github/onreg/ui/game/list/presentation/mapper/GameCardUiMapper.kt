package io.github.onreg.ui.game.list.presentation.mapper

import io.github.onreg.core.ui.components.chip.ChipUI
import io.github.onreg.core.ui.format.InstantTextFormatter
import io.github.onreg.core.ui.format.NumberTextFormatter
import io.github.onreg.data.game.api.model.Game
import io.github.onreg.ui.game.list.presentation.components.card.model.GameCardUI
import io.github.onreg.ui.platform.mapper.PlatformUiMapper
import javax.inject.Inject

public interface GameCardUiMapper {
    public fun map(
        game: Game,
        isBookmarked: Boolean,
    ): GameCardUI
}

public class GameCardUiMapperImpl
    @Inject
    constructor(
        private val platformUiMapper: PlatformUiMapper,
        private val instantTextFormatter: InstantTextFormatter,
        private val numberTextFormatter: NumberTextFormatter,
    ) : GameCardUiMapper {
        override fun map(
            game: Game,
            isBookmarked: Boolean,
        ): GameCardUI = GameCardUI(
            id = game.id.toString(),
            title = game.title,
            imageUrl = game.imageUrl,
            releaseDate = instantTextFormatter.format(instant = game.releaseDate),
            platforms = platformUiMapper.mapPlatform(game.platforms),
            rating = ChipUI(
                text = numberTextFormatter.format(value = game.rating),
                isSelected = true,
            ),
            isBookmarked = isBookmarked,
        )
    }
