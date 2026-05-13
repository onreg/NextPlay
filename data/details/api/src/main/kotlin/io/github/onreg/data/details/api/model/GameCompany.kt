package io.github.onreg.data.details.api.model

public enum class GameCompanyRole {
    Developer,
    Publisher,
}

public data class GameCompany(
    val name: String,
    val logoUrl: String?,
    val role: GameCompanyRole,
)
