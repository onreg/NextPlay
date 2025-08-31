package io.github.onreg.data.release.api

import kotlinx.coroutines.flow.Flow

interface ReleaseRepository {
    fun getReleases(): Flow<List<Release>>
}