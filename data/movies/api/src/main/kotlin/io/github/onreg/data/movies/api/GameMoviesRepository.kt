package io.github.onreg.data.movies.api

import androidx.paging.PagingData
import io.github.onreg.data.movies.api.model.Movie
import kotlinx.coroutines.flow.Flow

public interface GameMoviesRepository {
    public fun getMovies(gameId: Int): Flow<PagingData<Movie>>
}
