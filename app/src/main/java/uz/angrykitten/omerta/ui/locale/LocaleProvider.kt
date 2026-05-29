package uz.angrykitten.omerta.ui.locale

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.AssetManager
import android.content.res.Configuration
import android.content.res.Resources
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import uz.angrykitten.omerta.domain.model.AppLanguage
import java.util.Locale

/**
 * Read this in any composable to know the active app language. Use it when
 * picking which Role.name* / Role.description* field to display (built-in
 * strings.xml resolution happens via the wrapped LocalContext automatically).
 */
val LocalAppLanguage = compositionLocalOf<AppLanguage> { AppLanguage.EN }

fun interface AppLanguageSetter {
    operator fun invoke(language: AppLanguage)
}

val LocalAppLanguageSetter = staticCompositionLocalOf<AppLanguageSetter> {
    AppLanguageSetter { /* no-op default — replaced by app shell */ }
}

/**
 * Wraps a [Context] so [Context.getResources] returns resources resolved
 * with [language] as the primary locale, **while keeping the Activity
 * reachable via the baseContext chain**.
 *
 * AUDIT FIX: previous implementation returned the bare
 * `createConfigurationContext(config)` whose baseContext was a `ContextImpl`,
 * not an `Activity`. Libraries that walk `baseContext` to find the Activity
 * (Accompanist permissions, Coil's image cache lookup, system Toast targets,
 * etc.) all broke — crashed with `IllegalStateException: Permissions should
 * be called in the context of an Activity` when the user opened the QR tab.
 *
 * The new wrapper keeps the Activity as the base context so `findActivity()`
 * walks find it, while overriding [getResources]/[getAssets] to apply the
 * locale.
 */
fun Context.withLocale(language: AppLanguage): Context {
    val activity = findActivityInChain() ?: return this
    val locale = Locale.forLanguageTag(language.tag)
    val config = Configuration(activity.resources.configuration).apply {
        setLocale(locale)
        setLayoutDirection(locale)
    }
    val localizedResources = activity.createConfigurationContext(config).resources
    return LocaleAwareContextWrapper(activity, localizedResources)
}

private class LocaleAwareContextWrapper(
    activity: Activity,
    private val localizedResources: Resources,
) : ContextWrapper(activity) {
    override fun getResources(): Resources = localizedResources
    override fun getAssets(): AssetManager = localizedResources.assets
}

private fun Context.findActivityInChain(): Activity? {
    var ctx: Context = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

/**
 * Provider that swaps both [LocalContext] and [LocalConfiguration] so that
 * every [androidx.compose.ui.res.stringResource] lookup re-resolves against
 * the wrapped resources whenever [language] changes. Compose recomposes
 * anything reading these locals — this is what makes "switch language
 * applies instantly without app restart" work.
 *
 * Because the wrapped Context now keeps the Activity reachable via
 * `baseContext`, libraries that rely on `findActivity()` (Accompanist,
 * Coil, etc.) continue to work.
 */
@Composable
fun ProvideAppLanguage(
    language: AppLanguage,
    setter: AppLanguageSetter,
    content: @Composable () -> Unit,
) {
    val baseContext = LocalContext.current
    val localizedContext = remember(baseContext, language) {
        baseContext.withLocale(language)
    }
    val localizedConfig = remember(language, localizedContext) {
        Configuration(localizedContext.resources.configuration)
    }
    CompositionLocalProvider(
        LocalAppLanguage provides language,
        LocalAppLanguageSetter provides setter,
        LocalContext provides localizedContext,
        LocalConfiguration provides localizedConfig,
        content = content,
    )
}
