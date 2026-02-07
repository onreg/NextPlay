package io.github.onreg.core.network.rawg.api

import io.github.onreg.core.network.rawg.dto.MovieDto
import io.github.onreg.core.network.rawg.dto.PaginatedResponseDto
import io.github.onreg.core.network.retrofit.NetworkResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

public interface GameMoviesApi {
    @GET("games/{id}/movies")
    public suspend fun getMovies(
        @Path("id") id: Int,
        @Query("page") page: Int,
        @Query("page_size") pageSize: Int,
    ): NetworkResponse<PaginatedResponseDto<MovieDto>>
}
