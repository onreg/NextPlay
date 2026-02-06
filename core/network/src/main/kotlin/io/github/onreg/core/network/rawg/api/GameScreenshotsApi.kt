package io.github.onreg.core.network.rawg.api

import io.github.onreg.core.network.rawg.dto.PaginatedResponseDto
import io.github.onreg.core.network.rawg.dto.ScreenshotDto
import io.github.onreg.core.network.retrofit.NetworkResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

public interface GameScreenshotsApi {
    @GET("games/{id}/screenshots")
    public suspend fun getScreenshots(
        @Path("id") id: Int,
        @Query("page") page: Int,
        @Query("page_size") pageSize: Int,
    ): NetworkResponse<PaginatedResponseDto<ScreenshotDto>>
}
