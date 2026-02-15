package io.github.onreg.data.movies.impl.mapper

import io.github.onreg.core.db.movies.entity.MovieEntity
import io.github.onreg.data.movies.api.model.Movie
import io.github.onreg.data.movies.impl.mapper.impl.MovieEntityMapperImpl
import kotlin.test.Test
import kotlin.test.assertEquals

internal class MovieEntityMapperTest {
    private val mapper = MovieEntityMapperImpl()

    @Test
    fun `should map movie entity to model`() {
        val entity = MovieEntity(
            id = 1,
            gameId = 14,
            position = 3,
            name = "Trailer",
            previewUrl = "https://preview",
            videoUrl = "https://video",
        )

        val result = mapper.map(entity)

        assertEquals(
            Movie(
                id = 1,
                name = "Trailer",
                previewUrl = "https://preview",
                videoUrl = "https://video",
            ),
            result,
        )
    }
}
