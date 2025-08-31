package io.github.onreg.feature.release.impl

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.onreg.data.release.api.ReleaseRepository
import javax.inject.Inject

@HiltViewModel
internal class ReleasesViewModel @Inject constructor(
    releaseRepository: ReleaseRepository
) : ViewModel() {
    val releases = releaseRepository.getReleases()

}