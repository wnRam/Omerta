package uz.angrykitten.omerta.ui.navigation

/**
 * Static route names. Kept as string constants (not sealed-class objects) so
 * `NavHost.composable(route = …)` and `navController.navigate(…)` use the same
 * single source of truth without ceremony.
 *
 * Routes that take args use Navigation Compose's `/{arg}` syntax; the helper
 * functions on each destination construct the full path.
 */
object Destination {
    const val HOME = "home"
    const val HOW_TO_PLAY = "how_to_play"
    const val SETTINGS = "settings"

    const val CREATE_ROOM_MODE = "create_room/mode"
    const val RULE_SETUP = "create_room/rules"
    const val ROLE_EDITOR = "create_room/role_editor"
    const val ROOM_LOBBY = "room/lobby"

    const val JOIN_ROOM = "join_room"
    const val WAITING_ROOM = "join_room/waiting"

    const val ROLE_REVEAL = "game/role_reveal"
    const val PHASE_TRACKER = "game/phase"
    const val END_GAME = "game/end"
}
