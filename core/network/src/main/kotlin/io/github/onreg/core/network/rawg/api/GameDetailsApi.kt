package io.github.onreg.core.network.rawg.api

import io.github.onreg.core.network.rawg.dto.GameDetailsDto
import io.github.onreg.core.network.retrofit.NetworkResponse
import retrofit2.http.GET
import retrofit2.http.Path

public interface GameDetailsApi {
    @GET("games/{id}")
    public suspend fun getGameDetails(
        @Path("id") id: Int,
    ): NetworkResponse<GameDetailsDto>
}
