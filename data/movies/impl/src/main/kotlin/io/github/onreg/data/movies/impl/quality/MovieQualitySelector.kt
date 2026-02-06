package io.github.onreg.data.movies.impl.quality

import javax.inject.Inject

public interface MovieQualitySelector {
    public fun bestUrl(qualityUrls: Map<String, String>): String?
}

public class MovieQualitySelectorImpl
    @Inject
    constructor() : MovieQualitySelector {
        override fun bestUrl(qualityUrls: Map<String, String>): String? {
            val maxUrl = qualityUrls["max"]
            val numericUrl = qualityUrls
                .mapNotNull { (key, value) -> key.toIntOrNull()?.let { it to value } }
                .maxByOrNull { it.first }
                ?.second
            return maxUrl ?: numericUrl
        }
    }
