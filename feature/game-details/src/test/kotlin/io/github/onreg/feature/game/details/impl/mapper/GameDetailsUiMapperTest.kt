package io.github.onreg.feature.game.details.impl.mapper

import io.github.onreg.core.ui.components.chip.ChipUI
import io.github.onreg.core.ui.format.InstantTextFormatterImpl
import io.github.onreg.core.ui.format.NumberTextFormatterImpl
import io.github.onreg.core.util.android.resources.ResourcesProvider
import io.github.onreg.data.details.api.model.GameCompany
import io.github.onreg.data.details.api.model.GameCompanyRole
import io.github.onreg.data.details.api.model.GameDetails
import io.github.onreg.data.game.api.model.GamePlatform
import io.github.onreg.feature.game.details.impl.model.ContentState
import io.github.onreg.feature.game.details.impl.model.GameDetailsInternalState
import io.github.onreg.ui.platform.mapper.PlatformUiMapper
import io.github.onreg.ui.platform.model.PlatformUI
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import java.time.Instant

class GameDetailsUiMapperTest {
    private val platformUiMapper: PlatformUiMapper = mock {
        on { mapPlatform(setOf(GamePlatform.PC)) } doReturn
            setOf(PlatformUI(name = "PC", iconRes = 1))
    }

    private val resourcesProvider: ResourcesProvider = mock {
        on { getString(io.github.onreg.feature.game.details.impl.R.string.read_more) } doReturn "Read more"
        on { getString(io.github.onreg.feature.game.details.impl.R.string.read_less) } doReturn "Read less"
        on { getString(io.github.onreg.feature.game.details.impl.R.string.role_developer) } doReturn "Developer"
        on { getString(io.github.onreg.feature.game.details.impl.R.string.role_publisher) } doReturn "Publisher"
    }

    private val mapper = GameDetailsUiMapperImpl(
        platformUiMapper = platformUiMapper,
        resourcesProvider = resourcesProvider,
        instantTextFormatter = InstantTextFormatterImpl(),
        numberTextFormatter = NumberTextFormatterImpl(),
    )

    @Test
    fun `should map core fields and formatters`() {
        val model = baseModel.copy(
            releaseDate = Instant.parse("2023-07-19T00:00:00Z"),
            rating = 8.0,
            platforms = setOf(GamePlatform.PC),
            website = "https://example.com",
            description = "Hello",
            companies = emptyList(),
        )
        val localState = baseLocalState.copy(
            isBookmarked = true,
            isDescriptionExpanded = false,
            isReadMoreVisible = true,
        )

        val result = mapper.map(
            model = model,
            localState = localState,
        )

        assertEquals(model.imageUrl, result.image)
        assertEquals("Jul 19, 2023", result.releaseDate)
        assertEquals(ChipUI(text = "8", isSelected = true), result.rating)
        assertEquals(setOf(PlatformUI(name = "PC", iconRes = 1)), result.platforms)
        assertEquals("https://example.com", result.website)
        assertEquals(true, result.isBookmarked)
    }

    @Test
    fun `should drop website when scheme is not http(s) or blank`() {
        val ftpWebsiteResult = mapper.map(
            model = baseModel.copy(website = "ftp://example.com"),
            localState = baseLocalState,
        )
        val noSchemeResult = mapper.map(
            model = baseModel.copy(website = "www.example.com"),
            localState = baseLocalState,
        )
        val blankWebsiteResult = mapper.map(
            model = baseModel.copy(website = "   "),
            localState = baseLocalState,
        )

        assertNull(ftpWebsiteResult.website)
        assertNull(noSchemeResult.website)
        assertNull(blankWebsiteResult.website)
    }

    @Test
    fun `should convert description html to plain text`() {
        val result = mapper.map(
            model = baseModel.copy(description = "Hello <b>world</b> &amp; friends&#39; &quot;ok&quot;"),
            localState = baseLocalState,
        )

        assertEquals("Hello world & friends' \"ok\"", result.gameDescriptionUi.description)
    }

    @Test
    fun `should set description toggle text and visibility from local state`() {
        val collapsedResult = mapper.map(
            model = baseModel,
            localState = baseLocalState.copy(
                isDescriptionExpanded = false,
                isReadMoreVisible = false,
            ),
        )

        assertEquals("Read more", collapsedResult.gameDescriptionUi.descriptionToggleUi.text)
        assertEquals(false, collapsedResult.gameDescriptionUi.descriptionToggleUi.isVisible)

        val expandedResult = mapper.map(
            model = baseModel,
            localState = baseLocalState.copy(
                isDescriptionExpanded = true,
                isReadMoreVisible = false,
            ),
        )

        assertEquals("Read less", expandedResult.gameDescriptionUi.descriptionToggleUi.text)
        assertEquals(true, expandedResult.gameDescriptionUi.descriptionToggleUi.isVisible)
    }

    @Test
    fun `should sort companies developer first and map role strings`() {
        val model = baseModel.copy(
            companies = listOf(
                GameCompany(name = "Pub1", logoUrl = null, role = GameCompanyRole.Publisher),
                GameCompany(name = "Dev1", logoUrl = null, role = GameCompanyRole.Developer),
                GameCompany(name = "Pub2", logoUrl = null, role = GameCompanyRole.Publisher),
                GameCompany(name = "Dev2", logoUrl = null, role = GameCompanyRole.Developer),
            ),
        )

        val result = mapper.map(
            model = model,
            localState = baseLocalState,
        )

        assertEquals(listOf("Dev1", "Dev2", "Pub1", "Pub2"), result.companies.map { it.name })
        assertEquals(listOf("Developer", "Developer", "Publisher", "Publisher"), result.companies.map { it.role })
    }

    private companion object {
        val baseModel = GameDetails(
            gameId = 1,
            title = "Some title",
            imageUrl = "img",
            releaseDate = null,
            platforms = emptySet(),
            website = null,
            rating = 0.0,
            description = "",
            companies = emptyList(),
        )

        val baseLocalState = GameDetailsInternalState(
            isBookmarked = false,
            contentState = ContentState.Idle,
            isDescriptionExpanded = false,
            isReadMoreVisible = false,
        )
    }
}
