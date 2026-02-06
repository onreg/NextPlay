package io.github.onreg.data.movies.impl.paging

import androidx.paging.LoadType
import androidx.paging.PagingConfig
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import io.github.onreg.core.db.TransactionProvider
import io.github.onreg.core.db.movies.dao.MovieDao
import io.github.onreg.core.db.movies.dao.MovieRemoteKeysDao
import io.github.onreg.core.db.movies.entity.MovieEntity
import io.github.onreg.core.db.movies.entity.MovieRemoteKeysEntity
import io.github.onreg.core.network.rawg.api.GameMoviesApi
import io.github.onreg.core.network.rawg.dto.MovieDto
import io.github.onreg.core.network.rawg.dto.PaginatedResponseDto
import io.github.onreg.core.network.retrofit.NetworkResponse
import io.github.onreg.data.movies.api.model.Movie
import io.github.onreg.data.movies.impl.mapper.MovieDtoMapper
import io.github.onreg.data.movies.impl.mapper.MovieEntityMapper
import kotlinx.coroutines.test.runTest
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify
import kotlin.test.Test
import kotlin.test.assertTrue

internal class MoviesRemoteMediatorTest {
    private val api: GameMoviesApi = mock()
    private val dao: MovieDao = mock()
    private val remoteKeysDao: MovieRemoteKeysDao = mock()
    private val dtoMapper: MovieDtoMapper = mock()
    private val entityMapper: MovieEntityMapper = mock()
    private val transactionProvider = object : TransactionProvider {
        override suspend fun <T> run(block: suspend () -> T): T = block()
    }

    private val pagingConfig = PagingConfig(
        pageSize = 2,
        prefetchDistance = 1,
        initialLoadSize = 2,
        maxSize = 10,
    )

    @Test
    fun `load refresh inserts movies and remote keys`() = runTest {
        val dto = movieDto()
        val model = movieModel()
        val entity = movieEntity()

        stubApi(dto)
        stubMappers(dto, model, entity)

        val result = buildMediator().load(
            LoadType.REFRESH,
            pagingState(),
        )

        assertTrue(result is RemoteMediator.MediatorResult.Success)
        verify(dao).clearForGame(10)
        verify(remoteKeysDao).clearForGame(10)
        verify(dao).upsertAll(listOf(entity))
        verify(remoteKeysDao).insertRemoteKeys(
            listOf(
                MovieRemoteKeysEntity(
                    gameId = 10,
                    movieId = 1,
                    prevKey = null,
                    nextKey = 2,
                ),
            ),
        )
    }

    private fun movieDto(): MovieDto = MovieDto(
        id = 1,
        name = "Movie",
        previewUrl = "preview",
        data = mapOf("max" to "video"),
    )

    private fun movieModel(): Movie = Movie(
        id = 1,
        name = "Movie",
        previewUrl = "preview",
        videoUrl = "video",
    )

    private fun movieEntity(): MovieEntity = MovieEntity(
        id = 1,
        gameId = 10,
        name = "Movie",
        previewUrl = "preview",
        bestVideoUrl = "video",
        insertionOrder = 0,
    )

    private fun stubApi(dto: MovieDto) {
        api.stub {
            onBlocking { getMovies(10, 1, pagingConfig.pageSize) } doReturn
                NetworkResponse.Success(
                    PaginatedResponseDto(
                        count = 1,
                        next = "https://example.com?page=2",
                        previous = null,
                        results = listOf(dto),
                    ),
                )
        }
    }

    private fun stubMappers(
        dto: MovieDto,
        model: Movie,
        entity: MovieEntity,
    ) {
        dtoMapper.stub { on { map(dto) } doReturn model }
        entityMapper.stub { on { mapToEntity(model, 10, 0) } doReturn entity }
    }

    private fun buildMediator(): MoviesRemoteMediator = MoviesRemoteMediator(
        gameId = 10,
        moviesApi = api,
        movieDao = dao,
        remoteKeysDao = remoteKeysDao,
        dtoMapper = dtoMapper,
        entityMapper = entityMapper,
        transactionProvider = transactionProvider,
    )

    private fun pagingState(): PagingState<Int, MovieEntity> =
        PagingState(emptyList(), null, pagingConfig, 0)
}
