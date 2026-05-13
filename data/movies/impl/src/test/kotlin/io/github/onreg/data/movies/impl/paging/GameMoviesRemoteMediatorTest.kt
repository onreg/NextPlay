package io.github.onreg.data.movies.impl.paging

import androidx.paging.LoadType
import androidx.paging.RemoteMediator
import io.github.onreg.core.db.movies.entity.GameMovieRemoteKeysEntity
import io.github.onreg.core.db.movies.entity.MovieEntity
import io.github.onreg.core.network.rawg.dto.MovieDto
import io.github.onreg.core.network.rawg.dto.PaginatedResponseDto
import io.github.onreg.core.network.retrofit.NetworkResponse
import kotlinx.coroutines.test.runTest
import org.mockito.kotlin.check
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class GameMoviesRemoteMediatorTest {
    private val dto = MovieDto(
        id = 1,
        name = "Trailer",
        previewUrl = "https://preview",
        data = mapOf("max" to "https://video"),
    )

    private val mappedEntity = MovieEntity(
        id = 1,
        gameId = 0,
        position = 0,
        name = "Trailer",
        previewUrl = "https://preview",
        videoUrl = "https://video",
    )

    @Test
    fun `load refresh inserts movies and remote keys`() = runTest {
        val expectedEntity = mappedEntity.copy(gameId = 7, position = 0)
        val driver = GameMoviesRemoteMediatorTestDriver
            .Builder()
            .moviesApiGetMovies(
                response = NetworkResponse.Success(
                    PaginatedResponseDto(
                        count = 1,
                        next = "https://example.com?page=2",
                        previous = null,
                        results = listOf(dto),
                    ),
                ),
            ).movieDtoMapperMap(dto, 7, 0, expectedEntity)
            .build()

        val result = driver.load(LoadType.REFRESH, driver.emptyPagingState())

        assertTrue(
            result is RemoteMediator.MediatorResult.Success &&
                !result.endOfPaginationReached,
        )
        verify(driver.moviesApi).getMovies(driver.gameId, 1, driver.pagingConfig.pageSize)
        verify(driver.moviesDao).deleteByGameId(driver.gameId)
        verify(driver.moviesDao).insertMovies(listOf(expectedEntity))
        verify(driver.remoteKeysDao).insertRemoteKeys(
            listOf(
                GameMovieRemoteKeysEntity(
                    gameId = driver.gameId,
                    prevKey = null,
                    nextKey = 2,
                ),
            ),
        )
    }

    @Test
    fun `load refresh drops null mapped items`() = runTest {
        val dtoTwo = dto.copy(id = 2)
        val expectedEntity = mappedEntity.copy(gameId = 7, position = 0)
        val driver = GameMoviesRemoteMediatorTestDriver
            .Builder()
            .moviesApiGetMovies(
                response = NetworkResponse.Success(
                    PaginatedResponseDto(
                        count = 2,
                        next = null,
                        previous = null,
                        results = listOf(dto, dtoTwo),
                    ),
                ),
            ).movieDtoMapperMap(dto, 7, 0, expectedEntity)
            .movieDtoMapperMap(dtoTwo, 7, 1, null)
            .build()

        val result = driver.load(LoadType.REFRESH, driver.emptyPagingState())

        assertTrue(
            result is RemoteMediator.MediatorResult.Success &&
                result.endOfPaginationReached,
        )
        verify(driver.moviesDao).insertMovies(listOf(expectedEntity))
        verify(driver.remoteKeysDao).insertRemoteKeys(
            listOf(
                GameMovieRemoteKeysEntity(
                    gameId = driver.gameId,
                    prevKey = null,
                    nextKey = null,
                ),
            ),
        )
    }

    @Test
    fun `load refresh ends pagination when next link is invalid`() = runTest {
        val expectedEntity = mappedEntity.copy(gameId = 7, position = 0)
        val driver = GameMoviesRemoteMediatorTestDriver
            .Builder()
            .moviesApiGetMovies(
                response = NetworkResponse.Success(
                    PaginatedResponseDto(
                        count = 1,
                        next = "https://example.com",
                        previous = null,
                        results = listOf(dto),
                    ),
                ),
            ).movieDtoMapperMap(dto, 7, 0, expectedEntity)
            .build()

        val result = driver.load(LoadType.REFRESH, driver.emptyPagingState())

        assertTrue(result is RemoteMediator.MediatorResult.Success && result.endOfPaginationReached)
        verify(driver.remoteKeysDao).insertRemoteKeys(
            check { keys ->
                assertEquals(1, keys.size)
                assertEquals(null, keys.first().nextKey)
            },
        )
    }

    @Test
    fun `load prepend returns end of pagination without api call`() = runTest {
        val driver = GameMoviesRemoteMediatorTestDriver
            .Builder()
            .build()

        val result = driver.load(LoadType.PREPEND, driver.emptyPagingState())

        assertTrue(result is RemoteMediator.MediatorResult.Success && result.endOfPaginationReached)
        verify(driver.moviesApi, never()).getMovies(driver.gameId, 1, driver.pagingConfig.pageSize)
    }

    @Test
    fun `load append with empty pages waits for refresh`() = runTest {
        val driver = GameMoviesRemoteMediatorTestDriver
            .Builder()
            .build()

        val result = driver.load(LoadType.APPEND, driver.emptyPagingState())

        assertTrue(
            result is RemoteMediator.MediatorResult.Success &&
                !result.endOfPaginationReached,
        )
        verify(driver.moviesApi, never()).getMovies(driver.gameId, 1, driver.pagingConfig.pageSize)
    }

    @Test
    fun `load append with null next key ends pagination`() = runTest {
        val driver = GameMoviesRemoteMediatorTestDriver
            .Builder()
            .remoteKeysDaoGetByGameId(
                GameMovieRemoteKeysEntity(
                    gameId = 7,
                    prevKey = 1,
                    nextKey = null,
                ),
            ).build()

        val lastItem = mappedEntity.copy(gameId = driver.gameId)
        val result = driver.load(LoadType.APPEND, driver.pagingStateWithLastItem(lastItem))

        assertTrue(result is RemoteMediator.MediatorResult.Success && result.endOfPaginationReached)
        verify(driver.moviesApi, never()).getMovies(driver.gameId, 1, driver.pagingConfig.pageSize)
    }

    @Test
    fun `load append with remote key fetches next page`() = runTest {
        val expectedEntity = mappedEntity.copy(gameId = 7, position = 4)
        val driver = GameMoviesRemoteMediatorTestDriver
            .Builder()
            .remoteKeysDaoGetByGameId(
                GameMovieRemoteKeysEntity(
                    gameId = 7,
                    prevKey = 2,
                    nextKey = 3,
                ),
            ).moviesApiGetMovies(
                page = 3,
                response = NetworkResponse.Success(
                    PaginatedResponseDto(
                        count = 1,
                        next = "https://example.com?page=4",
                        previous = null,
                        results = listOf(dto),
                    ),
                ),
            ).movieDtoMapperMap(dto, 7, 4, expectedEntity)
            .build()

        val lastItem = mappedEntity.copy(gameId = driver.gameId)
        val result = driver.load(LoadType.APPEND, driver.pagingStateWithLastItem(lastItem))

        assertTrue(
            result is RemoteMediator.MediatorResult.Success &&
                !result.endOfPaginationReached,
        )
        verify(driver.moviesApi).getMovies(driver.gameId, 3, driver.pagingConfig.pageSize)
        verify(driver.moviesDao).insertMovies(listOf(expectedEntity))
        verify(driver.remoteKeysDao).insertRemoteKeys(
            listOf(
                GameMovieRemoteKeysEntity(
                    gameId = driver.gameId,
                    prevKey = 2,
                    nextKey = 4,
                ),
            ),
        )
    }

    @Test
    fun `load refresh returns error with fallback exception`() = runTest {
        val driver = GameMoviesRemoteMediatorTestDriver
            .Builder()
            .moviesApiGetMovies(
                response = NetworkResponse.Failure.OtherError(null),
            ).build()

        val result = driver.load(LoadType.REFRESH, driver.emptyPagingState())

        assertTrue(result is RemoteMediator.MediatorResult.Error)
        val throwable = (result as RemoteMediator.MediatorResult.Error).throwable
        assertTrue(throwable is IllegalStateException)
    }
}
