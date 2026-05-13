package io.github.onreg.data.movies.impl

import androidx.paging.testing.asSnapshot
import io.github.onreg.core.db.movies.entity.MovieEntity
import io.github.onreg.data.movies.api.model.Movie
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.mockito.kotlin.verify
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
internal class GameMoviesRepositoryTest {
    private val movieEntity = MovieEntity(
        id = 1,
        gameId = 7,
        position = 0,
        name = "Trailer",
        previewUrl = "https://preview",
        videoUrl = "https://video",
    )

    private val movie = Movie(
        id = 1,
        name = "Trailer",
        previewUrl = "https://preview",
        videoUrl = "https://video",
    )

    private val driver = GameMoviesRepositoryTestDriver
        .Builder()
        .moviesDaoPagingSource(gameId = 7, pagingSource = listOf(movieEntity))
        .movieEntityMapperMap(movieEntity, movie)
        .build()

    @Test
    fun `should get movies`() = runTest {
        val items = driver.getMovies(7).asSnapshot()

        verify(driver.remoteMediatorFactory).create(7)
        verify(driver.moviesDao).pagingSource(7)
        verify(driver.entityMapper).map(movieEntity)
        assertEquals(listOf(movie), items)
    }
}
