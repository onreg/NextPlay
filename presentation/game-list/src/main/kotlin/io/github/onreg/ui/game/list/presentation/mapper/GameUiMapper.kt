package io.github.onreg.ui.game.list.presentation.mapper

import androidx.paging.PagingData
import androidx.paging.map
import io.github.onreg.core.ui.components.chip.ChipUI
import io.github.onreg.core.ui.format.ReleaseDateFormatter
import io.github.onreg.data.game.list.api.model.Game
import io.github.onreg.ui.game.list.presentation.components.card.model.GameCardUI
import io.github.onreg.ui.platform.mapper.PlatformUiMapper
import javax.inject.Inject

public interface GameUiMapper {
    public fun map(
        games: PagingData<Game>,
        bookmarks: Set<String>,
    ): PagingData<GameCardUI>
}

public class GameUiMapperImpl
    @Inject
    constructor(
        private val platformUiMapper: PlatformUiMapper,
    ) : GameUiMapper {
        override fun map(
            games: PagingData<Game>,
            bookmarks: Set<String>,
        ): PagingData<GameCardUI> = games.map { game ->
            map(
                game = game,
                isBookmarked = bookmarks.contains(game.id.toString()),
            )
        }

        private fun map(
            game: Game,
            isBookmarked: Boolean,
        ): GameCardUI = GameCardUI(
            id = game.id.toString(),
            title = game.title,
            imageUrl = game.imageUrl,
            releaseDate = ReleaseDateFormatter.format(game.releaseDate),
            platforms = platformUiMapper.mapPlatform(game.platforms),
            rating = ChipUI(
                text = game.rating.toString(),
                isSelected = true,
            ),
            isBookmarked = isBookmarked,
        )
    }
