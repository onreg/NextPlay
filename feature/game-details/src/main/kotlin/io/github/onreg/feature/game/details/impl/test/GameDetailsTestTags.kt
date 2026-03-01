package io.github.onreg.feature.game.details.impl.test

internal object GameDetailsTestTags {
    const val LOADING = "details_loading"
    const val ERROR = "details_error"
    const val CONTENT = "details_content"
    const val SCREENSHOTS_SECTION = "details_screenshots_section"
    const val SCREENSHOT_ITEM = "details_screenshot_item"
    const val MOVIES_SECTION = "details_movies_section"
    const val MOVIE_ITEM = "details_movie_item"
    const val SERIES_SECTION = "details_series_section"
    const val SERIES_ITEM = "details_series_item"

    fun screenshotItemTag(id: Int): String = "${SCREENSHOT_ITEM}_$id"

    fun movieItemTag(id: Int): String = "${MOVIE_ITEM}_$id"

    fun seriesItemTag(id: Int): String = "${SERIES_ITEM}_$id"
}
