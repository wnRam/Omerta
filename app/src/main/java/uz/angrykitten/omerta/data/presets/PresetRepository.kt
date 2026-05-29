package uz.angrykitten.omerta.data.presets

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import uz.angrykitten.omerta.domain.model.Preset

private val Context.presetsDataStore by preferencesDataStore(name = "omerta_presets")

/**
 * Persists [Preset]s as a JSON-encoded list in a single DataStore key.
 *
 * Why one big JSON blob instead of a Room database:
 *  - Preset count is tiny (typical user has <10).
 *  - Schema evolution is easier — we own the kotlinx.serialization classes.
 *  - No build-time annotation processing overhead for a single table.
 */
class PresetRepository(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    private val listSerializer = ListSerializer(Preset.serializer())

    val presets: Flow<List<Preset>> = context.presetsDataStore.data.map { prefs ->
        val raw = prefs[KEY_PRESETS_JSON] ?: return@map emptyList<Preset>()
        runCatching { json.decodeFromString(listSerializer, raw) }.getOrDefault(emptyList())
    }

    suspend fun upsert(preset: Preset) {
        mutate { current ->
            val without = current.filterNot { it.id == preset.id }
            (without + preset).sortedByDescending { it.createdAt }
        }
    }

    suspend fun delete(id: String) {
        mutate { current -> current.filterNot { it.id == id } }
    }

    private suspend fun mutate(transform: (List<Preset>) -> List<Preset>) {
        context.presetsDataStore.edit { prefs ->
            val current = runCatching {
                prefs[KEY_PRESETS_JSON]?.let { json.decodeFromString(listSerializer, it) }
            }.getOrNull() ?: emptyList()
            val next = transform(current)
            prefs[KEY_PRESETS_JSON] = json.encodeToString(listSerializer, next)
        }
    }

    companion object {
        private val KEY_PRESETS_JSON = stringPreferencesKey("presets_json")
    }
}
