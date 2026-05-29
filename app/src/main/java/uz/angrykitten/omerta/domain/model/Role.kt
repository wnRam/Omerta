package uz.angrykitten.omerta.domain.model

import kotlinx.serialization.Serializable

/**
 * A role is a triplet of localized strings + a team alignment + an icon key.
 *
 * Built-in roles ship with non-null *En/Ru/Uz fields. Custom roles may be authored
 * in a subset of languages; missing translations fall back to whichever language
 * the user provided first (resolved at presentation time, see RoleLocalization).
 *
 * The [iconRes] field is a string key (not an int) so it serializes cleanly across
 * the WebSocket protocol and DataStore — the icon-key → vector mapping lives in
 * [uz.angrykitten.omerta.ui.icons.RoleIcons].
 */
@Serializable
data class Role(
    val id: String,
    val nameEn: String,
    val nameRu: String,
    val nameUz: String,
    val descriptionEn: String,
    val descriptionRu: String,
    val descriptionUz: String,
    val team: Team,
    val iconRes: String,
    val isCustom: Boolean = false,
)
