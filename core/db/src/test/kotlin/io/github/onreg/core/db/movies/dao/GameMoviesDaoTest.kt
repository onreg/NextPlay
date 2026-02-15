package io.github.onreg.core.db.movies.dao

import androidx.paging.PagingSource
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.onreg.core.db.NextPlayDatabase
import io.github.onreg.core.db.game.entity.GameEntity
import io.github.onreg.core.db.movies.entity.MovieEntity
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import java.time.Instant
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
internal class GameMoviesDaoTest {
    private val database = Room
        .inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            NextPlayDatabase::class.java,
        ).allowMainThreadQueries()
        .build()

    private val gameDao = database.gameDao()
    private val gameMoviesDao = database.gameMoviesDao()

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun `should return movies ordered by position`() = runTest {
        val firstGameId = 4001
        val secondGameId = 4002
        gameDao.insertGames(listOf(game(firstGameId, "First"), game(secondGameId, "Second")))

        val firstGameMovies = listOf(
            MovieEntity(
                id = 1,
                gameId = firstGameId,
                position = 2,
                name = "Trailer 2",
                previewUrl = "preview-2",
                videoUrl = "video-2",
            ),
            MovieEntity(
                id = 2,
                gameId = firstGameId,
                position = 1,
                name = "Trailer 1",
                previewUrl = "preview-1",
                videoUrl = "video-1",
            ),
        )
        val secondGameMovie = MovieEntity(
            id = 3,
            gameId = secondGameId,
            position = 0,
            name = "Second Trailer",
            previewUrl = null,
            videoUrl = "video-3",
        )
        gameMoviesDao.insertMovies(firstGameMovies + secondGameMovie)
        val result = loadMovies(firstGameId)
        assertEquals(
            listOf(
                firstGameMovies[1],
                firstGameMovies[0],
            ),
            result,
        )
    }

    @Test
    fun `should delete movies only for requested game id`() = runTest {
        val firstGameId = 5001
        val secondGameId = 5002
        gameDao.insertGames(listOf(game(firstGameId, "First"), game(secondGameId, "Second")))

        val firstGameMovie = MovieEntity(
            id = 10,
            gameId = firstGameId,
            position = 0,
            name = "First",
            previewUrl = "preview-10",
            videoUrl = "video-10",
        )
        val secondGameMovie = MovieEntity(
            id = 11,
            gameId = secondGameId,
            position = 0,
            name = "Second",
            previewUrl = "preview-11",
            videoUrl = "video-11",
        )
        gameMoviesDao.insertMovies(listOf(firstGameMovie, secondGameMovie))

        gameMoviesDao.deleteByGameId(firstGameId)

        val firstGameResult = loadMovies(firstGameId)
        val secondGameResult = loadMovies(secondGameId)
        assertTrue(firstGameResult.isEmpty())
        assertEquals(listOf(secondGameMovie), secondGameResult)
        assertEquals(2, countRows(GameEntity.TABLE_NAME))
    }

    @Test
    fun `should return empty list when game has no movies`() = runTest {
        val gameId = 6001
        gameDao.insertGames(listOf(game(gameId, "No Movies")))

        val result = loadMovies(gameId)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `should cascade delete movies for deleted game only`() = runTest {
        val firstGameId = 6002
        val secondGameId = 6003
        gameDao.insertGames(listOf(game(firstGameId, "First"), game(secondGameId, "Second")))
        val firstGameMovies = listOf(
            MovieEntity(
                id = 20,
                gameId = firstGameId,
                position = 0,
                name = "First 0",
                previewUrl = "preview-20",
                videoUrl = "video-20",
            ),
            MovieEntity(
                id = 21,
                gameId = firstGameId,
                position = 1,
                name = "First 1",
                previewUrl = "preview-21",
                videoUrl = "video-21",
            ),
        )
        val secondGameMovies = listOf(
            MovieEntity(
                id = 22,
                gameId = secondGameId,
                position = 0,
                name = "Second 0",
                previewUrl = "preview-22",
                videoUrl = "video-22",
            ),
        )
        gameMoviesDao.insertMovies(firstGameMovies + secondGameMovies)

        database.openHelper.writableDatabase.execSQL(
            "DELETE FROM ${GameEntity.TABLE_NAME} WHERE ${GameEntity.ID} = ?",
            arrayOf(firstGameId),
        )

        assertTrue(loadMovies(firstGameId).isEmpty())
        assertEquals(secondGameMovies, loadMovies(secondGameId))
        assertEquals(1, countRows(GameEntity.TABLE_NAME))
        assertEquals(secondGameMovies.size, countRows(MovieEntity.TABLE_NAME))
    }

    private fun game(
        id: Int,
        title: String,
    ): GameEntity = GameEntity(
        id = id,
        title = title,
        imageUrl = title.lowercase(),
        releaseDate = Instant.parse("2024-01-01T00:00:00Z"),
        rating = 4.0,
    )

    private suspend fun loadMovies(gameId: Int): List<MovieEntity> {
        val result = gameMoviesDao.pagingSource(gameId).load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 10,
                placeholdersEnabled = false,
            ),
        )
        assertTrue(result is PagingSource.LoadResult.Page)
        return result.data
    }

    private fun countRows(table: String): Int =
        database.query("SELECT COUNT(*) FROM $table", null).use { cursor ->
            cursor.moveToFirst()
            cursor.getInt(0)
        }
}
