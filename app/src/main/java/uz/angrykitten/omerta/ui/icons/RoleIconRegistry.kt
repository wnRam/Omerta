package uz.angrykitten.omerta.ui.icons

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Resolves [uz.angrykitten.omerta.domain.model.Role.iconRes] strings to
 * [ImageVector]s. Keeping the role data plain-string keyed (not @DrawableRes)
 * is what lets a `Role` survive WebSocket transit and DataStore round-trips
 * without leaking Android resource IDs.
 *
 * Unknown keys fall through to [RoleIcons.CustomRole] — better to show an
 * ornate "?" frame than to crash if a custom role lands with a typo'd key.
 */
object RoleIconRegistry {

    private val builtIns: Map<String, ImageVector> = mapOf(
        "citizen" to RoleIcons.Citizen,
        "mafia" to RoleIcons.Mafia,
        "sheriff" to RoleIcons.Sheriff,
        "doctor" to RoleIcons.Doctor,
        "don" to RoleIcons.Don,
        "maniac" to RoleIcons.Maniac,
        "detective" to RoleIcons.Detective,
        "bodyguard" to RoleIcons.Bodyguard,
        "prostitute" to RoleIcons.Prostitute,
        "mayor" to RoleIcons.Mayor,
        "custom" to RoleIcons.CustomRole,
    )

    /** All built-in role-icon keys, in the order they appear on the role-picker grid. */
    val allKeys: List<String> get() = builtIns.keys.toList()

    fun forKey(key: String): ImageVector = builtIns[key] ?: RoleIcons.CustomRole
}
