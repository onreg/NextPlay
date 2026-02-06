package io.github.onreg.core.network.rawg.api

import io.github.onreg.core.network.rawg.dto.GameDto
import io.github.onreg.core.network.rawg.dto.PaginatedResponseDto
import io.github.onreg.core.network.retrofit.NetworkResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

public interface GameSeriesApi {
    @GET("games/{id}/game-series")
    public suspend fun getSeries(
        @Path("id") id: Int,
        @Query("page") page: Int,
        @Query("page_size") pageSize: Int,
    ): NetworkResponse<PaginatedResponseDto<GameDto>>
}
