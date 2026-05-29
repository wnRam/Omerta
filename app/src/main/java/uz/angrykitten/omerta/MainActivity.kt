package uz.angrykitten.omerta

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import uz.angrykitten.omerta.data.settings.SettingsRepository
import uz.angrykitten.omerta.domain.model.AppLanguage
import uz.angrykitten.omerta.ui.locale.AppLanguageSetter
import uz.angrykitten.omerta.ui.locale.ProvideAppLanguage
import uz.angrykitten.omerta.ui.navigation.OmertaNavHost
import uz.angrykitten.omerta.ui.settings.SettingsViewModel
import uz.angrykitten.omerta.ui.theme.OmertaTheme

class MainActivity : ComponentActivity() {

    private val settingsViewModel: SettingsViewModel by viewModels {
        viewModelFactory {
            initializer { SettingsViewModel(OmertaApp.get().settingsRepository) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by settingsViewModel.state.collectAsStateWithLifecycle()
            val deviceLang = remember { resolveDeviceLanguage() }
            // AUDIT FIX: persisted language wins, otherwise device locale, then EN.
            val activeLanguage = settings.language ?: deviceLang

            val setter = remember(settingsViewModel) {
                AppLanguageSetter { settingsViewModel.setLanguage(it) }
            }

            val darkTheme = when (settings.themeMode) {
                SettingsRepository.ThemeMode.SYSTEM -> isSystemInDarkTheme()
                SettingsRepository.ThemeMode.DARK -> true
                SettingsRepository.ThemeMode.LIGHT -> false
            }

            ProvideAppLanguage(language = activeLanguage, setter = setter) {
                OmertaTheme(darkTheme = darkTheme) {
                    OmertaNavHost()
                }
            }
        }
    }

    private fun resolveDeviceLanguage(): AppLanguage {
        val tag = resources.configuration.locales.get(0)?.language
        return AppLanguage.fromTag(tag) ?: AppLanguage.EN
    }
}
