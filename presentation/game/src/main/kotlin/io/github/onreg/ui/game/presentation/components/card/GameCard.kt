package io.github.onreg.ui.game.presentation.components.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.onreg.core.ui.components.chip.Chip
import io.github.onreg.core.ui.components.chip.ChipUI
import io.github.onreg.core.ui.components.image.DynamicAsyncImage
import io.github.onreg.core.ui.preview.ThemePreview
import io.github.onreg.core.ui.theme.ControlsSize
import io.github.onreg.core.ui.theme.IconsSize
import io.github.onreg.core.ui.theme.NextPlayTheme
import io.github.onreg.core.ui.theme.Spacing
import io.github.onreg.ui.game.presentation.R
import io.github.onreg.ui.game.presentation.components.card.model.GameCardUI
import io.github.onreg.ui.game.presentation.components.card.test.GameCardTestTags.GAME_CARD_ADD_BOOKMARK_BUTTON
import io.github.onreg.ui.platform.model.PlatformUI
import io.github.onreg.core.ui.R as CoreUiR

@Composable
public fun GameCard(
    modifier: Modifier = Modifier,
    gameData: GameCardUI,
    onBookmarkClick: () -> Unit = {},
    onCardClicked: () -> Unit = {}
) {
    ElevatedCard(
        modifier = modifier,
        onClick = onCardClicked,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box {
                DynamicAsyncImage(
                    modifier = Modifier
                        .aspectRatio(2f)
                        .clip(MaterialTheme.shapes.small),
                    imageUrl = gameData.imageUrl
                )

                Chip(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = Spacing.sm),
                    chipUI = gameData.rating
                )
            }

            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .requiredSize(ControlsSize.IconButton)
                        .testTag(GAME_CARD_ADD_BOOKMARK_BUTTON),
                    onClick = onBookmarkClick
                ) {
                    Icon(
                        painter = painterResource(
                            when (gameData.isBookmarked) {
                                true -> R.drawable.ic_bookmark_filled_24
                                false -> R.drawable.ic_bookmark_24
                            }
                        ),
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = stringResource(
                            if (gameData.isBookmarked) {
                                R.string.remove_bookmark
                            } else {
                                R.string.add_bookmark
                            }
                        )
                    )
                }

                Column(modifier = Modifier.padding(Spacing.lg)) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                end = (ControlsSize.IconButton - Spacing.sm)
                                    .coerceAtLeast(0.dp)
                            ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        text = gameData.title,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        modifier = Modifier.padding(top = Spacing.sm),
                        text = gameData.releaseDate,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Platforms(
                        modifier = Modifier.padding(top = Spacing.sm),
                        platforms = gameData.platforms
                    )
                }
            }
        }
    }
}

@Composable
private fun Platforms(
    modifier: Modifier = Modifier,
    platforms: Set<PlatformUI>
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        platforms.forEach { platform ->
            Icon(
                modifier = Modifier.size(IconsSize.sm),
                painter = painterResource(platform.iconRes),
                contentDescription = platform.name,
            )
        }
    }
}

@Composable
@ThemePreview
private fun GameListCardPreview() {
    NextPlayTheme {
        GameCard(
            modifier = Modifier,
            gameData = GameCardUI(
                id = "1",
                title = "Cyberpunk 2077",
                releaseDate = "December 10, 2020",
                imageUrl = "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD/2wCEAAkGBxASEhUSEBIWFhUXFxUXGBgXFxcYGBgYFhYXGBcVFxcYHSggGB8lHRcYITEhJSkrLjAuGB8zODMtNygtLisBCgoKDg0OGxAQGy8lHyUtLS0vLS0tLS0tLS0tLy0tLTYtLS0tLS0tLS8vLS0tNS0tLS0tLS8tLS0tLS0tLS0tLf/AABEIAPMAzwMBIgACEQEDEQH/xAAcAAAABwEBAAAAAAAAAAAAAAAAAQMEBQYHAgj/xABCEAACAQIEAwUGBAUDAgUFAAABAhEAAwQSITEFQVEGImFxgQcTMpGhsRRCwdEjUnLw8WKS4VOCJDNDotIVNIOTsv/EABsBAAIDAQEBAAAAAAAAAAAAAAABAgMEBQYH/8QANREAAgECBQIDBwIFBQAAAAAAAAECAxEEBRIhMUFRBiJxExQyYYGRwaHxFSOx0eEzNEKCwv/aAAwDAQACEQMRAD8A0Rf3+9HRL+/3roCvkbOiCKEUcUcVEDmKEV3FCKLjOYo4rqKOKVwOIo4rqKOKLgcEUYFdRQpXAKKKK7oUXA4ArqKOhQBzFGBR0dAHJFEVruhSuAnlroCuqFO4BUKOhFIDnLQArqioAbL+/wB67miUfrXQFWMQBRzRgUcVEZzR0cUcUrgEKOjimvE8cli0119hHhJJgCp04SqSUIq7Ym7DkUdUnHe0TDKyhWIOYo9v3TM2bwcsqiNeR3q18Jxwv2bd4KVDjMFMSBJiY8NfWteLy2vhoKdRbMjGafA7oUKFYCYKFHQoAKhR0KACo6OhQAVCjihSAKhRxQoAKhR0IpgFQo6FACKj9fvXcUF/f70dNsQIoUdCkMKhR0dABVWfaCP/AAoaA2W7bbKSRmgyNRtBE+MRVnqC7ZuowzZhIPXyrp5NFyxtNLuV1X5WYVgELNLbgvcPmWKgeWteiOGWPd2bafyoi/JQKwns5Yz4hEH5zbB8JuAf/KvQJFdjxPU3hD1ZCijmjo4oV5MvCihFHUfxPjViwP4jif5RBPyqdOnOo9MFdiH8UcVSMT7Q0XVbJI/qg+e2lHhfaNZY9+yyjrmDemg3rd/CcXa+j+gtSLtQqO4bxzD39EcZv5ToflUlWGpTnTdpqzGFQo6FVjCoUdCKACoUdCgAqFHQigBNf3+9HRL+/wB66qTAKjoUdIAqOhR0AFWee1bi72gLSqCDbkmSCCWYADly6VolZP7VSrXADvO/grGB5afWvQeG4asXq7JlNbixG+zfD5sXbMbOzeiKY9J+9bVWR+x61mxLvyW1cI/73QD6Bq12o+Ip3xWnskSp8BUKOK5uNAJ6A1wYrU0iwq/a7jjWwbdpsp5trPkIrOOIXiYJEzmnvGWnr9KsPaIsWLHr9RqN/I1XLmZRIEggkzrrJJ8t699gMFCjTSSK27iK2M8MFEgRy08oEz/elc4iVnaZ3/sGlYynVSJ1iRv5kT8/ChiEkTJ156j9N66qgiDIscSe02ZO6RroTOgP+a1nsD2wTFp7q438VRz/ADD7TWP8QsDpofPT1pHhmIexdVkeCCNuWsgg/rWLMMtp4qk116MUZtM9L0U1F9m+KjFYdLvM6N4MN/39alK+eVKcqc3CXKLwTQmhRVWM6mhXNAGgDqhQBrqKQCK/v966ol/f70dTYgUdChFRGCjoRQPjTjFydktxCGLxItiTHUyYAA+Ik8tKxL2k9rMLiiGw6MrIWVswENBYAghj56ias3tEe7iEuXMNdue7tubToMxR3Sc0qdssDUDUt/prJ7NuFBG5Ln6gD9a+gZJl6w1LVL4nz/YzTbkzYvY5gwtu8/P+EnyXOf8A+q0Wq52Dw5XDsT+a4flbVLX3Q1ZIrx+b1faYycvmaIKyCpHG/wDlt5GnFJYpCUYDmD9qw0XapH1QzMMaxzEHUePSuFw9uBAjWfkdPLaakL+Cu2zmuWjlg7zr60lZwauM1ppj8p+L/keVfTKKtHcgw73ABimRw2Uk6zGh0LR9x4GmPaLs1esKWsn3ijpIYA8zyIH71M8Oum2wJ57j9fPenWN4lH+am5NMNJlOOuMveI0jYaa6gg1X8ZiJIiQZ2157c60Ljao8nKJO9Uy+qhu6BIO4rRCasQlBmz+ykRgcp3VzJ6yFP61cqqvs1QDCebmf9q1a6+Z5m74uo/mXJWCoUdCsNmMKhQoTTsxBrSgrhaUqLAbKo6dfvXWUdKC/v96OpNgFlHSjyjpR034jifdW2cCSo0ExJ5CalCMpyUY8sBW4yL8UCo7iF+2RlLlJBggZT6Ft+u1NcTeKSfeokmWyAlpgaZie6NPCo/FXmFstdC3rG5PNI5kfljqD8q9zl+UU8Nact5/09Cq7kVa9ZxOEvKMQRcwhe44vID3XuGT75Z089v0g73AVu45bWEy3FeHgGAoJZ7gPQD7EVOdoOOC2DasXHa0yvIMHUL8Abcjzp37MEwzXxets2c2ihVoOsqSVMaaLtXRxdaVHDzqRW6RK3Qv/AAXAe5sqjQW7xPMSzFiAemtPsg6V1UB2h48to+7U97nqJE7AV87pU6uLrWXL3JNpIk8XjrNv4yJ6ASarfFO2VlT7pYVn7isxAgt3VMeZFQyYa5iZZna3Z6rAdweeZ+7aXoTqeQp1a4bgsMMwt2k/1sFZj4+9xJAP/ahr3GX+FYRSnVbuc6pmMYytHcZtiDau/h7Nu8/dXMWeVfTvOVIJZmMiZFL/AP0spfKWzBBBGvLff6elTOC4thntMyXJKHVlYuBIMEsqAAmDMCKi/wAYRe95IIbIdDIysARBjWurVg4NxZ0FVVWKmiWXAiY0B6feKb47BWlEllnpSt26lxDlaCeexrOe03ALwBZHutMztH11NVRV3uHBJ8Ra3B1EeBqkM1sM3MyIpD8M8iCw06kz86c3uFsoQgHU89CZ2rQoqPUjdtXsbJ7NsK64Uu4H8RpUeC92T6/arZkHSsw7F9tltL+GdJW2SAV0bUmYBMNr5etaTgcZbvIHtMGU/Q8wRyPhXzzN8NXp4iU5qyb2/BOMkxR7SndQfMA1z+Gt/wAi/wC0ftS1CuUpPuOwh+Ft/wDTT/av7UPwlr/pp/tX9qWoU9cu4WObdpRsAPSlMg6US12ag2wEV/f70dEv7/ejqTAOontPgve2MsxluW30Ek5GmI8alqr/AG2x5tYfTdjFb8qhqxcPW/2B8GV9qePtmK3Hc2p+G3kRvASVOXnPOuF48ly0pw1+7BJW5YuBSwOkd9AMwP6+Bqv9oXLDNJOuuxjQxsKZcEDZSE3Lb9ANzX0uMVoMzk1MlWvM/wDDtySrNDflykEa+P7Vp3sr7Mi0pxLTrKoNfJmj6D1qudiOyzYg6aWlI95cPM/yr1J+n31bF4+xhlVNAAvdUcgNB/fnXm87xzcfdaG8nzboi2Pdg49xQYe0W0znuoDGrHz5Df8AzWc28NmctdMn43JGuupBMxJPTx2pbtD2ttPiCHJAQAKACd9SR46j5Cmb4wQWuGFHffz3VNN4EDx9a6XhnJfZeeqt+X+DBjsQ4wtHlljsq9wA5ii/lA+M9TMEr/2jN1KjSo/EY3C2STb921yYYhkLz1Z/4jf+8eVUTivH7+KbIrG3a2yg7jq5HxeW3hzphib5t2IT4nIVfXn/AH1r1latFXsc+jg27avsap2NxT3bV69dJZbhCoCSTkSROvUk03vX0zmDIJgeGUQPtFPuC2haweUbKgA9BVI4tjtcyHXmJ+org1fPO53aUdMbFsN8D4YB1g/p/wA1XuI4685IZu6OQmPU6fKmfDOMZiCW/vnUriuIWShJExyj4j+1V6WmWqRH8CyFme+y20A0Z4AJ8zUrxRLM25uIRnBBzaFdz3gDAjnBqt43iFxlJe2YOwzIB5RNc9j79m1eQ3GygkhSSQAT/qG3mQR1EVopUtUlcqr1dMG7dDQOL8Bs4m37xYW6ACl0ZQfD3mTu3UO3vF2nUVDdn+L3cPiUBkBmCXFJ3k5ZPiDrPgaseNYWEz24yjvMoAgz+ZQDCtEyF7ridjpWeYvGM90BPjdxljcEtpr4E083w9OpTtJc3+xy8vqSba6G60KBoV8jezO2FQo6KgA1rpq5WjekISX9/vXVJWWmf6mH1pWpyVmCDqoe01l/DCd8wj1IHOrfWce1jEtNpPy7luQOsKem8g+B6V2MgpOeMT7JsjN2RnuBwaMzC4SFymRlcZug1Eb6zyirH7Peyy3MTDgGzbGd03BJ0UMeYnl4GozAJOvI8xqPpVt7L9psLggy3g5e84jIubugQukgnUn517DMZ1Vh5+xTcntsV2V9xv7Qe1uWMLgu4iE5ioyiV5KBEKNfM/WuWOMXXX+I5YiFkknQf5Pzpn2iv2Xxb+4ByM7ksx1OYfDHIAz86jbdphaJk6MRA05Dc7/Kng8HThRikt+d+fqQk22OuNYm4dQgyEgZ8oktA0zxOnSY18aPjnEpAtLsDJAOhPITzA/aoLHXPhjT/M6ec00vYlnJYmu5Qqezg4rqZZ09Uk30JfAX194AfEesGT+lI4rETibazKpH7/tUVac5hG9TmMNo3wiIQRaXMTvnjM0+GtQbvsTW25rNu/ODMdD9qzG6rMx1qy8A44nuGw905WiFJ2I6E8j96YYbDiSxrKk03c1qSa2K8cOVOjEE8jOtTJJtoGunKNO9O58ae2sAHMgbayaq3aUszwWLAbDkB5VNJS2ISenckOI+7KyGLR8hpUDfxckBdhTEWyTAp3ZwTGBG/wBauilEplJyLnwDieIbDsrv/DkZZmZ3IU8l+Ex1HnQ7O/xManII6szcgAZkk+IpivFAqpYS2ZygAyACx56+NC3jVwahSZuOQX8I6eA2HrWTEa6l78tWQQjGHB6KoVnXZvt0VtgX7TFOTKBmUdSnMeWtX3A423eQPabMp8x9DrXzXG5fWwsvOtu/Q2xmmOKKjoVgJAWg9GtE+9Ahvh9j/U33pWkMN+b+tqXq2p8QIBMa1kXb3HZsSYOwj67eNadxrFi3aJ66fOsQ41i/e33b/UfppXrvDeG0wlVfUrnu7DrBDYgAfl0AEho105g/rTbjtxlugosnJE66AlpgjST9qdYIaL/Uv6n9Kje01wC4PiByjvA+J0ivTJXISI7F3wMpjUENy1jypT8coVoaZ/VRp6belM8Lw/FYl8mHtveYfyiY8zsvqaacT4fiMNdNrEWmtvM5WG/ipGjDxBNTWi+m6v26ldxvi3zN5aUrZsSs9T9v80iF1qZTAuy27dpWe42yqJYkyYA8qtlJRRC1yO4dgy11RmCjN8R2EczT/h9xrl67dY5iWIzdQNvoBU9xPgNzB2yly2C2S2HeZy3MRoqjrkHTcz4V3jOF20h7AAWBIHKNM3rpPjNRhUU1qXDE0RuItTUd7y6hhXIHnp8jUu5pjiRFXEEdYbG4p1dReZUAlioHPYT4+fI1IDgOfW4xICqNTEQIIganXxFHw3CxYSQc164T4G3b9RILdRrVvxmC/hoo3O55nqT61S5W4LOeSq2uAJCZBuTvpAAktHLzppYsi5cL2yPcpPeOgyqCXaTpG9WDtJK+7wto5bl0QxG6Whq7eZ2ri3bDocPhrIYBCoJ5SIzHlz/velcZTcfi2S8HLZ1QyvemQTpDaiNOXjUVcxbPcNxzJJnwHQDwFSfHeDXLJKswL7sqmcnRSep3jl61AxVsUmRLPgeMlNSasvZ3t7etMVsqCp3DmFkneBt9KzYk0vhb+Uif7ms9fCU6sXGauiSk0eoeA8YGISSuVwBmWZ35g8xUnNYl7Ou0DW7y23fT8snrPdPUdOkDrW2KZAPUT86+d5vl/ulXy/C+DVTnqR0DRE0KFcksGuH/ADeLMfr/AMV1icTbtjNcdVHViB96ie02IvW8HfuYcgXEtuy6A/DqdOsA1gtnF4jG4hEvXnbORmJY/Dz15afeu7l+UvGKVRyslz3K5T07GwduOJhraFDKsucEcxAINZIG7x860rtHhGv3vw1gpmRFUKzheUACd4HSlOB+zREIfFOLh3yLmCep3b6V6TD4nD4OhGEpW22XUi78lW4Pgbt4qtlCxDCY2Hdbc7D1qW4t2MwdphiuK4nIgUAWkMFiCTGYd5t9lHrWiPhLi2/d4f3drySQB4DTXxqj8Z9mP4q8b1/GOSYGw0gcpmKrp5gqz809EeyTcn9bWX03Kpy7IrPE/aebQFrhdi3YsrsWUFm8coMDzMmqp2gxuPxWTGYzMbbFktsRlTTvFUHTx+taQnskwCgi5i7kkHL3raweRIy6+VTHAuGYLE2rNi4wvjh5a2TEWWdVEMSdDA/LOhmeU3rGYWgtdGDduXZ33v1fN3sRSvyzHMLwbElVf3NzIxXK2RspzEBYMQZJAHWa1vhHC14XY9/cTPi7vdVQMwQfyyNgAAWPParEMX7yMUylrS//AG9tfiuNEC7HjqFmAASx3EIWOLX7EfjLiNdvNFvD2o7snQZoloG7GAINY8RjK2JtHSrLmN+flfsuvToSTjEz58BdxV5WdLxaWxF0wxBk5bEKOUBjr/LpUhiOG3YzLh7oUAkyjS0A6BACSTtrFX7G8RCwotXndxLNZTTu6a3GgeXhNQt63eu4qxbC4i0CTduF72Ym3bju5VYhczFR46+NaaeY1bNygopLvfj6r6bEduhn9ns1j8gLYa7MD8vhTTGcBxYGuGvf/rY/YVt9zDZ2Li8+UxAQwPnTK9xY2iEWzd1MZrjb+Wp+elQpZzXq+WEU323Vvu/wN00tzPbOCKXbVshh7q1aWNzmcZyYDGSSBsBy0q1W8LcZRmtPIBjun5U74JZu38S2JvWygQsElcpf8qsRJmBm1n8wpXjOKe2jPiMYmGUPMpDEIdFU5x8R8jVs8ym6vs4pXSV+Xv2VluPSkrsoeF7P4649/EXMPc94592gIgi0oBnwk1O8O4Vdwlss6fxCBkXSWuNougO4JA85NWPDcVRbD3LZuXPdwua6dXcgaACB+YchvSlzEOls4i+suIFtBpmdu6igciWaNes1X/EcQm9aiktkurfT/IrJrYybhvB2e9jbV9pKu4LwYNxdWifE1VuI8JuBgBbc5mCg5TBJMATtqYHrW+vgAbDWM6tiJS7dM/nusczeAgMAOiiuAVuYgW1ZfcYIBn10a+ynKD/QhLnxZelTjnPluo7q9/To/wDs7WG4GDcW7L4zDsiX7RRrhIUEqSY3PdJq1YP2fOloXMS1qzaGr3L3TeFTcnfmNudani7a3FbF4JbN3EwAjXGMAD8qn8m5MaanWsR7Wdo8ReuNbxgLXLbEQTARhoYCmK0YTGV8WtO0Wvi7/RdF83f0ItJcHfGcRgbRS3gA7KNWvvOdmnQINMiDyk6dKsvsu7Y3zjEw9+6zpcDKMxJhlBKxO3wketZsLpdhOmmkfar77OOxN+/iLeKLBbFu4GzT33Zdcqjpm0JPQ71bmFOhHDSVZ7We75v/AHCF77G7UKKaOa+YG0Z5AQQRIMgjqDoRWC4Tg/4fHYqzyspcA/pYqUP+0it8t/vVI7dcNS274kfFdtrbb/8AHm1+TKP+0V6HJcX7KpOm/wDkv1/a5XON7MyrHcdZmPvZYz8XM7amlF7Y3wAPf3iBsPeN+9QfEh3z/fSmcV7uFKDiroyuTJ7HdrcU4yrcdB4O0/OdKiHxt1viu3D5ux+5puaWsYdnZUQSzGAPE/YeNWqEY8IiyS7L9nruPxC2LfPV3IkIg3Y/YDmTV87YdqrGEFvhmARWs2iovye7cIPetMw3kjvn06iq/wAD45fsj8BwlFN28cty/EvcMH4J0RF1g+Z51D9q+CNgsU9gktCoQx/OWUZm8s+celYJwVbEJVXsleMf/T/A1xsajjvaUj2VOFRluMDmzhctuNOnf8Nh9qp2C45ct4hsUWd2tozswAaTBjMDoF5aRvpFVT8QQAJ0GkeVH74hDv3iFkGOeoI5iAaso4OlRi4wjs/1AuuJ7YY+8O/e92COUWhPPKB3qsPY7jlpcNfU3y+KZgO8e9kIEFZJJAltepFZuuJnuuoJjQnYjrNL9nr6q966TAAjTz2FRrYSnOnotZXT2+RJbcGhDi1206LbuFM066GY/LlOhmas2A7RXXhTbVmO0HKJ6wdBWbYVbt5wzHx1MC2oIMzNWvhfFLFsPeANxLQJEDW5cCkhV6Abz4r1rLi8NSkruGqXTvf1JXtwyd7R49rNlLPvP4jDM7bGJmBr3QTIA6LWc8cj3DgABff2bjnqMwBJNHc7UHEXnuXu77zQCZCgaKoPh1rjtJjVOFe3mJlROmxkaDXb/NacBhfYUkn8XL9WVTlvwW/hXa60LQsrbi4rMcxgrJYw4nnJjWontN2/dXtNYtBjaDEi5JHvCMobTeBMf1Gqnh8Z7trdwye6M3USsZo59d6bcQxiXXZlEg8jo2wBO+u0+tNZdh09Wne7/XkWpt2Jzsb2xvjEYjE3Cpa9GaZCjL8IUcgBpUff7auLN/CqkG7iHd7wOrqWkqQeZgCemlRnDcaLIuiSA4gAfD0ObxAkR/qpPBth1/iG2l1zJCu2S0mu7yZc+G3nWj3ainqS7fpx9hbvlD/gPFMTaf8AFfiGsWhudCbwH/p27baXD47DrU1cxGE4xne7ZbC3vhF8DNaYD4VcaaxEkdNxVd47jxetLnvWnZQTlSTDMYhPyqqqF28amOP8WRMJatYQBQFgZd5nU+e59alOnHVqXPcEtS32K/xrs1isEwa4ua2fhu2+9bbp3ht5GtK9lPaHD27a4e7cCtlUKG01Lux1/wC4b1nPCe0+Lw6mLmdSRKXe+jdQVP3EU04zj7Vy6LuGsiwcozIrEoH11tzqoOmnIzWXHYT3uk6U/uiVOTi9z1Hl8KGUUhw2/wC8tW7n86I3+5QacV8tnFxk4vobxqhH3qJ7W4VbuGZTuNV84OnrUqB+tJ43Ci7ba2dMwiRuDyI8jrWijP2dWMuzBrY80cUtw5Daa+dR5UdfpVt7bcLuW7pFyA0wTsG1+KqwbYG5k+G3zr6hhqqqU1JGKS3Ewo5b0thMDcu3UtWgS7mABuZmfpJ8qW4fgbl5xbtIWY7KoJP02HjWy9g+xH4Im/fIa8y5QBqEB1bXmx0HQARWbMMyp4Sm235ui7jjByF/Z72IXAJ7y5lbEuveO4QH/wBNP1PM+FVL2z3FN+0uUBwmp5wSYH6+tbFGtYT7V8UGx9xdwoVfXKJFeZyavVxWPdWo7uzL5pRjZFLRzOgml7YJdTMES2YCT/tFc2WGyg/Ku8JlLks5WBoV3nevamcdXsSADALEScziIP8ApX/NdcDRCrZgSSVgDnqJmuMfirnuyr3A3IamYMbry9aLht++iEIYDazAn0PSlbYGyw4vGMdLhFu3vkGmb+rm1IvxT3iG2qkoqORBAgmATAB5fpUHiLbkS5Lb7611hpJOZB8AA1C6SNYPxUlTSI3G926Z+f3p2MYWXK3SKb37Z0PWkgakMIY9h3Z0Gn7R0rjEXlPw6Hw0H/Hp8qQxSwZpEGamkI7uXWO5nz3+dWDs9h8EFLYySSRkUSTEc461XACSABJOwGp9KtXDGtWFBuXlW6QBpDNbHPfQNy8KU+BoW7QPh2a3h8PhmkAnKoCN3hOukkxrrUNdwDRoyp/pa4hgc5I28qmbN+wub3DjM85nZ81xhzEnb0phgyux5ZeUgDSdJpRVwexDPaMwzhvJv1NaR7NuyWCxRzYlWJVdLeYZGgnvGNTodp5eNRPDEtuGygEZ+k/lkABjMEgj1rSuxCoCCFUGAZAH5hB1A8dq5Ob1qkaElBtO3KJwSvcuyKAIAgDQAbDwo6AoV80Ng0WlRXAFKxVjEVrtr2XtY22AxyuPhcCY8xzFULD+yd83fxSBeeW2xY+WYwPrWuYoaetNgK6mFzTE0KeiEtvRMhKCb3Irs52cw2CQrYUyfidjLt5nkPAaVOONqTFLPyrFWqzqz1zd2ySVhUDUV5t7VXTdxV682zXHIHhmNej75hWMxCsZ6QN681cfvLdvOVPdzHKB0k6+tei8MR885ehXV4GKnTTpy386LCsQNhqeZInwkbUV0wpA56aHbz670FbSPefQR9ta9mZznH3B3VCheZAM/WpHA2rZAV3I8pP0qHutL/2PtT63c8CPrUrEWLX8PaEBLzt17rD6E/rSeGRc505D45Ea8tdfWu7t5SBGmmu49aPDuJBJG/PXmNRGtAHVmCCpETz8eVNLyEb8t6X2kgjc+e9C6wbz+9IY1uDMtMYp5cUjrFIXLR1PSJ9djUkI5RjPd32HWTpVqThS2lBSyLhgAliDrGoynQzvVa4fHvFLbAg1bL3EcXEWbS5f5ipM8gCGA+VRm9xojLzWQZuYU2iPzLIHyGlRrYlJ+K4B4Kn71KHG4kmLoAXnlskn7D70yu2Ea5mZgQd8wZI9OXoaFsDE14lk/wDKa4OpJAn0WtP9lhxd8q4LG2rd5i6kaDUFZzA7aRWbDh2HJAF6J5CDFaH7KeCZMSL1m6xUZluSQFYZfhyj4iJB8K5ebzisNN8O3VE6a3NgmioNtQr5mbBIV1XIo6kAniDp60gopxdeKbjy+9WR4EGTSjOOtcBvKlXaIihgR/ai9/4a4qyWdTbAEzL6H6TXnnjLFLht6KV0IXWCOXnW9dv8c1nB3XQgMBpprqYIHQxXnm/aOYmfiMzz1617Hw1D+S5fMpqsRvHVR610rDlXKsSxI/5o7rnKdW9YivVGcaqZb1p6lwjpTPDkA609zA9KbEdlwRQwb98b+gnmKRJorFzUfoYpAO7xIZiI+JvPeuLhnWiY95j4nnREdNKQwzfj4hNdWbtksFGYBpViY0DbRHQ60hcPUU2La07XESeAwLLcbMpJthiQv+kHXy0nyp2CpAPvyk94kMFGvKRqfICprsrxFbOKwt66O5ci2X5K69wZj4rFOfadwS3bx5d27t1EZVVQMoEpkAG/wT61h96tiFRkuVdP05RO21ytp+FJ0a5cPm5+8CuruGskT7pwOoKn5gNUlh8OoUZbSgcveHX/AGLp9aQv5x/0vSR+9a7isRYsYcnRo8yyn5NWm+zDBAXM9p7mWe8rOCubKYIGSf8A3VmWObfTpsZ/v/mrR7KQv4pd5LDSSARruAdeuvSufmkHLCzd+jJQfmN3bahSdd18yNgmGHWjJprNDNVmgQoxnejRKTVx1roGm0wCKawK7cbeApO5dikfxDdaai2BVfajeLLbsLHe1M7b6T/tNZFxNl942U5lVSAepAjN5SasftO4vcOMKnQDceGgH0k+tVDGjKD0IWPIif2r6BlGGdLDwXdX+5nqS3GltecH0/eucUdAP80EJgCDSd46121yUh4dZNOCkbUlhgN/0pRmH8wpMQCRXCJrP9/8UZdes+hoK3gT56AeMUAOI1/aujcXrrXJAbmfT/FIvhj0NAxW7dXmPlTNhrpXbyN6TB6UxF+7I4RL+Gu2bq51MEDmGA5dDtT32iFvxFhH+K3hbKsZ7xMtIJGx8t5qM9mWGvG8TOWwoz3WkbJrAHU7T0qO45xY379y8xEuxI12H5V9BFctQcsX8opv6v8AYnfyjZ7ngPl+u/WkFcGZYjXZYGn9xXF+6Nh/f9j70xe4QzHr9v7ArpJELji+xHwsTV39leCF7GW7y6FM5uAaDRdNP6itUTDksw1rRvZVg7q4246qBbFuGPLM4GUAeMT4AeVc7NZ6cLPezsydPk2Ku5pEuKTuYnkvzr5ppbNg3sneknY660KFaFyI6w573oacihQpT5GI4rlSFChTjwIx/wBsNhVxVtgNWt94ydcpgeWlVDHH+GnkPsooUK+i5d/taXoZZ8sRcxEU0behQroorYvYOlC5cI50KFHUDhrzdaCk0dCgBwhrt3I2oUKQ0NrjE70VrehQp9BF97MsVwGOK6H8O+o8wP1q9YPgGDVEUYe1GUbopJ0BkkiT60KFc6n/AKk/X8FhQO3XCbFpptWwmv5ZA+W1UpBMzyE0KFbqfBGQvgx/EHpW/dk0AwluABMk+J2k9dAPlR0K854jf8qPqW0SVFEp3oUK8eXn/9k=",
                platforms = setOf(
                    PlatformUI("PC", CoreUiR.drawable.ic_controller_24),
                    PlatformUI("PlayStation", CoreUiR.drawable.ic_controller_24),
                    PlatformUI("Xbox", CoreUiR.drawable.ic_controller_24),
                    PlatformUI("Nintendo", CoreUiR.drawable.ic_controller_24),
                    PlatformUI("Linux", CoreUiR.drawable.ic_controller_24),
                    PlatformUI("Apple", CoreUiR.drawable.ic_controller_24),
                    PlatformUI("Android", CoreUiR.drawable.ic_controller_24),
                ),
                rating = ChipUI(
                    text = "#100",
                    isSelected = true
                ),
                isBookmarked = false
            )
        )
    }
}
