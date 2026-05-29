package uz.angrykitten.omerta.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import uz.angrykitten.omerta.data.settings.SettingsRepository
import uz.angrykitten.omerta.domain.model.AppLanguage

class SettingsViewModel(
    private val repository: SettingsRepository,
) : ViewModel() {

    val state: StateFlow<SettingsRepository.Settings> = repository.settings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsRepository.Settings(
                language = null,
                themeMode = SettingsRepository.ThemeMode.SYSTEM,
            ),
        )

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch { repository.setLanguage(language) }
    }

    fun setThemeMode(mode: SettingsRepository.ThemeMode) {
        viewModelScope.launch { repository.setThemeMode(mode) }
    }
}
