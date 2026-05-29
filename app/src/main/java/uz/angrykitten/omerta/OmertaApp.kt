package uz.angrykitten.omerta

import android.app.Application
import uz.angrykitten.omerta.data.presets.PresetRepository
import uz.angrykitten.omerta.data.settings.SettingsRepository
import uz.angrykitten.omerta.domain.game.GameSession

/**
 * Tiny service locator. We deliberately avoid Hilt here — the project has only
 * a handful of singletons (settings, preset store, network manager) and a
 * lightweight Application-hosted DI is simpler to reason about for now.
 */
class OmertaApp : Application() {

    val settingsRepository: SettingsRepository by lazy { SettingsRepository(this) }
    val gameSession: GameSession by lazy { GameSession() }
    val presetRepository: PresetRepository by lazy { PresetRepository(this) }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        @Volatile
        private var instance: OmertaApp? = null

        // AUDIT FIX: never returns null — Application is created before any
        // composable can run, so the !! is sound but we still error explicitly
        // if someone calls this from a content provider that runs before onCreate.
        fun get(): OmertaApp = instance
            ?: error("OmertaApp.get() called before Application.onCreate()")
    }
}
