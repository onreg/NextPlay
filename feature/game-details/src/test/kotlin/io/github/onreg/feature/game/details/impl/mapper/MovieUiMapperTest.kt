package io.github.onreg.feature.game.details.impl.mapper

import io.github.onreg.data.movies.api.model.Movie
import io.github.onreg.feature.game.details.impl.model.MovieUI
import org.junit.Assert.assertEquals
import org.junit.Test

class MovieUiMapperTest {
    private val mapper = MovieUiMapperImpl()

    @Test
    fun `should map movie and default null fields to empty`() {
        val model = Movie(
            id = 1,
            name = null,
            previewUrl = null,
            videoUrl = "video",
        )

        val result = mapper.map(model = model)

        assertEquals(
            MovieUI(
                id = 1,
                videoUrl = "video",
                previewUrl = "",
                name = "",
            ),
            result,
        )
    }
}
