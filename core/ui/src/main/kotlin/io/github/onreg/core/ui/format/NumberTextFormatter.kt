package io.github.onreg.core.ui.format

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import javax.inject.Inject

public interface NumberTextFormatter {
    public fun format(
        value: Double,
        fractionDigits: Int = 1,
        trimZeroFraction: Boolean = true,
        locale: Locale = Locale.US,
    ): String
}

public class NumberTextFormatterImpl
@Inject
constructor() : NumberTextFormatter {
    override fun format(
        value: Double,
        fractionDigits: Int,
        trimZeroFraction: Boolean,
        locale: Locale,
    ): String {
        val roundedValue = BigDecimal.valueOf(value)
            .setScale(fractionDigits, RoundingMode.DOWN)
        return DecimalFormat().apply {
            decimalFormatSymbols = DecimalFormatSymbols(locale)
            roundingMode = RoundingMode.UNNECESSARY
            isGroupingUsed = false
            maximumFractionDigits = fractionDigits
            minimumFractionDigits = if (trimZeroFraction) 0 else fractionDigits
        }.format(roundedValue)
    }
}
