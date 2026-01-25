package io.github.onreg.ui.game.presentation.mapper

import androidx.paging.PagingData
import androidx.paging.map
import io.github.onreg.core.ui.components.chip.ChipUI
import io.github.onreg.data.game.api.model.Game
import io.github.onreg.ui.game.presentation.components.card.model.GameCardUI
import io.github.onreg.ui.platform.mapper.PlatformUiMapper
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
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
        private val releaseDateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US)

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
            releaseDate = game.releaseDate
                ?.atZone(ZoneOffset.UTC)
                ?.format(releaseDateFormatter)
                .orEmpty(),
            platforms = platformUiMapper.mapPlatform(game.platforms),
            rating = ChipUI(
                text = game.rating.toString(),
                isSelected = true,
            ),
            isBookmarked = isBookmarked,
        )
    }
