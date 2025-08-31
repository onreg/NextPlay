package io.github.onreg.data.release.api

import kotlinx.coroutines.flow.Flow

public interface ReleaseRepository {
    public fun getReleases(): Flow<List<Release>>
}