package io.github.onreg.core.network.rawg.api

import io.github.onreg.core.network.rawg.dto.GameDto
import io.github.onreg.core.network.rawg.dto.PaginatedResponseDto
import io.github.onreg.core.network.retrofit.NetworkResponse
import retrofit2.http.GET
import retrofit2.http.Query

public interface GameApi {
    @GET("games")
    public suspend fun getGames(
        @Query("page") page: Int,
        @Query("page_size") pageSize: Int,
    ): NetworkResponse<PaginatedResponseDto<GameDto>>
}
