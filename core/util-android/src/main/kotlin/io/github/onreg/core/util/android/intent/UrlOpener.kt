package io.github.onreg.core.util.android.intent

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

public interface UrlOpener {
    public fun open(url: String)
}

public class UrlOpenerImpl
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
) : UrlOpener {
    override fun open(url: String) {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, url.toUri()).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }
}
