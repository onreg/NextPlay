package io.github.onreg.ui.game.presentation.mapper

import androidx.paging.PagingData
import androidx.paging.map
import io.github.onreg.core.ui.components.chip.ChipUI
import io.github.onreg.data.game.api.model.Game
import io.github.onreg.ui.game.presentation.components.card.model.GameCardUI
import io.github.onreg.ui.platform.mapper.PlatformUiMapper
import javax.inject.Inject

public interface GameUiMapper {
    public fun map(game: Game, isBookmarked: Boolean): GameCardUI
    public fun map(games: PagingData<Game>, bookMarks: Set<String>): PagingData<GameCardUI>
}

public class GameUiMapperImpl @Inject constructor(
    private val platformUiMapper: PlatformUiMapper
) : GameUiMapper {
    override fun map(game: Game, isBookmarked: Boolean): GameCardUI {
        return GameCardUI(
            id = game.id.toString(),
            title = game.title,
            imageUrl = game.imageUrl,
            releaseDate = game.releaseDate?.toString().orEmpty(),
            platforms = platformUiMapper.mapPlatform(game.platforms),
            rating = ChipUI(
                text = game.rating.toString(),
            ),
            isBookmarked = isBookmarked
        )
    }

    override fun map(games: PagingData<Game>, bookMarks: Set<String>): PagingData<GameCardUI> =
        games.map { game ->
            map(
                game = game,
                isBookmarked = bookMarks.contains(game.id.toString())
            )
        }
}
