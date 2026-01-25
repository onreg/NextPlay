package io.github.onreg.core.util.android.resources

import android.content.Context
import androidx.annotation.StringRes
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

public interface ResourcesProvider {
    public fun getString(
        @StringRes resId: Int,
    ): String
}

public class ResourcesProviderImpl
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
    ) : ResourcesProvider {
        override fun getString(
            @StringRes resId: Int,
        ): String = context.getString(resId)
    }
