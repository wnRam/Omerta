package uz.angrykitten.omerta.data.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import uz.angrykitten.omerta.domain.model.AppLanguage

private val Context.settingsDataStore by preferencesDataStore(name = "omerta_settings")

/**
 * Single source of truth for app-wide preferences (language + theme override).
 *
 * Theme is tri-state: SYSTEM (follow device), DARK, LIGHT.
 */
class SettingsRepository(private val context: Context) {

    enum class ThemeMode { SYSTEM, DARK, LIGHT }

    data class Settings(
        val language: AppLanguage?,
        val themeMode: ThemeMode,
    )

    val settings: Flow<Settings> = context.settingsDataStore.data.map { prefs ->
        Settings(
            language = AppLanguage.fromTag(prefs[KEY_LANGUAGE]),
            themeMode = prefs[KEY_THEME]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                ?: ThemeMode.SYSTEM,
        )
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.settingsDataStore.edit { it[KEY_LANGUAGE] = language.tag }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.settingsDataStore.edit { it[KEY_THEME] = mode.name }
    }

    companion object {
        private val KEY_LANGUAGE = stringPreferencesKey("language")
        private val KEY_THEME = stringPreferencesKey("theme_mode")
    }
}
