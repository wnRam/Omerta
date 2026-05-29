package uz.angrykitten.omerta.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class AppLanguage(val tag: String) {
    EN("en"),
    RU("ru"),
    UZ("uz");

    companion object {
        fun fromTag(tag: String?): AppLanguage? =
            entries.firstOrNull { it.tag.equals(tag, ignoreCase = true) }
    }
}
