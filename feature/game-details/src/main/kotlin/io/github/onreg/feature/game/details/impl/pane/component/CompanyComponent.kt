package io.github.onreg.feature.game.details.impl.pane.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import io.github.onreg.core.ui.components.image.DynamicAsyncImage
import io.github.onreg.core.ui.preview.ThemePreview
import io.github.onreg.core.ui.theme.IconsSize
import io.github.onreg.core.ui.theme.NextPlayTheme
import io.github.onreg.core.ui.theme.Spacing
import io.github.onreg.feature.game.details.impl.R
import io.github.onreg.feature.game.details.impl.model.GameCompanyUi
import io.github.onreg.feature.game.details.impl.test.GameDetailsPaneTestTags
import io.github.onreg.feature.game.details.impl.test.GameDetailsTestData

@Composable
internal fun CompanyComponent(
    modifier: Modifier = Modifier,
    companies: List<GameCompanyUi>,
) {
    Column(modifier = modifier.testTag(GameDetailsPaneTestTags.GAME_DETAILS_COMPANIES_SECTION)) {
        Text(
            text = stringResource(R.string.developers_and_publishers_section_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Column(
            modifier = Modifier.padding(top = Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            companies.forEach { company ->
                CompanyRow(company = company)
            }
        }
    }
}

@Composable
private fun CompanyRow(company: GameCompanyUi) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        DynamicAsyncImage(
            modifier = Modifier
                .size(IconsSize.xl)
                .clip(MaterialTheme.shapes.large),
            imageUrl = company.logoUrl.orEmpty(),
        )
        Column(
            modifier = Modifier.padding(start = Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            Text(
                text = company.name,
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = company.role,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@ThemePreview
@Composable
private fun CompanyComponentPreview() {
    val companies = GameDetailsTestData.readyState.details.companies
    NextPlayTheme {
        Surface {
            CompanyComponent(companies = companies)
        }
    }
}
