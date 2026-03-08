package io.github.onreg.core.db.movies.dao

import androidx.paging.PagingSource
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import io.github.onreg.core.db.NextPlayDatabase
import io.github.onreg.core.db.game.dao.GameDao
import io.github.onreg.core.db.game.entity.GameEntity
import io.github.onreg.core.db.movies.entity.MovieEntity
import io.github.onreg.core.db.test.loadDaoRefreshPage
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Instant
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
internal class GameMoviesDaoTest {
    private val firstGame = GameEntity(
        id = 101,
        title = "First Game",
        imageUrl = "https://example.com/first.png",
        releaseDate = Instant.parse("2024-01-01T00:00:00Z"),
        rating = 4.2,
    )
    private val secondGame = GameEntity(
        id = 102,
        title = "Second Game",
        imageUrl = "https://example.com/second.png",
        releaseDate = Instant.parse("2024-02-02T00:00:00Z"),
        rating = 4.5,
    )
    private val firstGameFirstMovie = MovieEntity(
        id = 201,
        gameId = firstGame.id,
        position = 1,
        name = "Gameplay Trailer",
        previewUrl = "https://example.com/movie-201-preview.jpg",
        videoUrl = "https://example.com/movie-201.mp4",
    )
    private val firstGameSecondMovie = MovieEntity(
        id = 202,
        gameId = firstGame.id,
        position = 0,
        name = "Announcement Trailer",
        previewUrl = null,
        videoUrl = "https://example.com/movie-202.mp4",
    )
    private val secondGameMovie = MovieEntity(
        id = 301,
        gameId = secondGame.id,
        position = 0,
        name = "Second Game Trailer",
        previewUrl = "https://example.com/movie-301-preview.jpg",
        videoUrl = "https://example.com/movie-301.mp4",
    )

    private val database = Room
        .inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            NextPlayDatabase::class.java,
        ).allowMainThreadQueries()
        .build()

    private val gameDao: GameDao = database.gameDao()
    private val moviesDao: GameMoviesDao = database.gameMoviesDao()

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun `pagingSource should return only movies for the requested game ordered by position`() =
        runTest {
            gameDao.insertGames(listOf(firstGame, secondGame))
            moviesDao.insertMovies(
                listOf(
                    firstGameFirstMovie,
                    secondGameMovie,
                    firstGameSecondMovie,
                ),
            )

            val page = loadMoviePage(firstGame.id)

            assertEquals(
                listOf(firstGameSecondMovie, firstGameFirstMovie),
                page.data,
            )
        }

    @Test
    fun `pagingSource should return an empty page when the game has no movies`() = runTest {
        gameDao.insertGames(listOf(firstGame))

        val page = loadMoviePage(firstGame.id)

        assertEquals(emptyList(), page.data)
    }

    @Test
    fun `insertMovies should persist inserted rows`() = runTest {
        gameDao.insertGames(listOf(firstGame))
        moviesDao.insertMovies(listOf(firstGameFirstMovie, firstGameSecondMovie))

        assertEquals(
            listOf(firstGameSecondMovie, firstGameFirstMovie),
            readMovies(firstGame.id),
        )
    }

    @Test
    fun `insertMovies should replace an existing movie with the same id`() = runTest {
        val updatedMovie = firstGameFirstMovie.copy(
            name = "Updated Gameplay Trailer",
            previewUrl = "https://example.com/movie-201-updated-preview.jpg",
            videoUrl = "https://example.com/movie-201-updated.mp4",
        )

        gameDao.insertGames(listOf(firstGame))
        moviesDao.insertMovies(listOf(firstGameFirstMovie, firstGameSecondMovie))
        moviesDao.insertMovies(listOf(updatedMovie))

        assertEquals(
            listOf(firstGameSecondMovie, updatedMovie),
            readMovies(firstGame.id),
        )
    }

    @Test
    fun `deleteByGameId should remove only movies for the requested game`() = runTest {
        gameDao.insertGames(listOf(firstGame, secondGame))
        moviesDao.insertMovies(
            listOf(
                firstGameFirstMovie,
                firstGameSecondMovie,
                secondGameMovie,
            ),
        )

        moviesDao.deleteByGameId(firstGame.id)

        assertEquals(emptyList(), readMovies(firstGame.id))
        assertEquals(listOf(secondGameMovie), readMovies(secondGame.id))
    }

    @Test
    fun `should cascade delete movies when the parent game is removed`() = runTest {
        gameDao.insertGames(listOf(firstGame, secondGame))
        moviesDao.insertMovies(
            listOf(
                firstGameFirstMovie,
                firstGameSecondMovie,
                secondGameMovie,
            ),
        )

        database.openHelper.writableDatabase.execSQL(
            "DELETE FROM ${GameEntity.TABLE_NAME} WHERE ${GameEntity.ID} = ?",
            arrayOf(firstGame.id),
        )

        assertEquals(emptyList(), readMovies(firstGame.id))
        assertEquals(listOf(secondGameMovie), readMovies(secondGame.id))
    }

    @Test
    fun `insertMovies should fail when the parent game does not exist`() = runTest {
        val orphanMovie = MovieEntity(
            id = 401,
            gameId = 999,
            position = 0,
            name = "Orphan Trailer",
            previewUrl = null,
            videoUrl = "https://example.com/movie-401.mp4",
        )

        val error = try {
            moviesDao.insertMovies(listOf(orphanMovie))
            null
        } catch (throwable: Throwable) {
            throwable
        }

        assertTrue(error != null)
        assertEquals(emptyList(), readMovies(orphanMovie.gameId))
    }

    private suspend fun loadMoviePage(gameId: Int): PagingSource.LoadResult.Page<Int, MovieEntity> {
        return moviesDao.pagingSource(gameId).loadDaoRefreshPage()
    }

    private suspend fun readMovies(gameId: Int): List<MovieEntity> = loadMoviePage(gameId).data
}
