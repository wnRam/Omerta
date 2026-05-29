package uz.angrykitten.omerta.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.getValue
import uz.angrykitten.omerta.OmertaApp
import uz.angrykitten.omerta.ui.createroom.CreateRoomViewModel
import uz.angrykitten.omerta.ui.createroom.LobbyScreen
import uz.angrykitten.omerta.ui.createroom.LobbyViewModel
import uz.angrykitten.omerta.ui.createroom.ModeSelectionScreen
import uz.angrykitten.omerta.ui.createroom.RoleEditorScreen
import uz.angrykitten.omerta.ui.createroom.RuleSetupScreen
import uz.angrykitten.omerta.ui.game.EndGameScreen
import uz.angrykitten.omerta.ui.game.PhaseTrackerScreen
import uz.angrykitten.omerta.ui.game.RoleRevealScreen
import uz.angrykitten.omerta.ui.home.HomeScreen
import uz.angrykitten.omerta.ui.howtoplay.HowToPlayScreen
import uz.angrykitten.omerta.ui.joinroom.JoinRoomScreen
import uz.angrykitten.omerta.ui.locale.LocalAppLanguage
import uz.angrykitten.omerta.ui.settings.SettingsScreen

/**
 * Top-level navigation graph. Forward transitions: slide-in from end +
 * fade. Back transitions: slide-out to end + fade.
 *
 * `CreateRoomViewModel` is scoped to the NavHost's NavBackStackEntry for
 * the "create_room/rules" route via [createRoomViewModelStore]. This means
 * Mode → Rules → Role Editor → Lobby all share one VM, and it's cleared
 * when the user backs out of the Create-Room subgraph.
 */
@Composable
fun OmertaNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = Destination.HOME,
        enterTransition = NavTransitions.push,
        exitTransition = NavTransitions.pushExit,
        popEnterTransition = NavTransitions.popEnter,
        popExitTransition = NavTransitions.popExit,
    ) {
        composable(Destination.HOME) {
            HomeScreen(
                onCreateRoom = { navController.navigate(Destination.CREATE_ROOM_MODE) },
                onJoinRoom = { navController.navigate(Destination.JOIN_ROOM) },
                onHowToPlay = { navController.navigate(Destination.HOW_TO_PLAY) },
                onSettings = { navController.navigate(Destination.SETTINGS) },
            )
        }

        composable(Destination.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }

        composable(Destination.HOW_TO_PLAY) {
            HowToPlayScreen(onBack = { navController.popBackStack() })
        }

        composable(Destination.CREATE_ROOM_MODE) { entry ->
            val createRoomVm = createRoomVm(entry, navController)
            ModeSelectionScreen(
                onBack = { navController.popBackStack() },
                onModeChosen = { mode ->
                    createRoomVm.setMode(mode)
                    navController.navigate(Destination.RULE_SETUP)
                },
            )
        }

        composable(Destination.RULE_SETUP) { entry ->
            val createRoomVm = createRoomVm(entry, navController)
            RuleSetupScreen(
                viewModel = createRoomVm,
                onBack = { navController.popBackStack() },
                onOpenRoleEditor = { navController.navigate(Destination.ROLE_EDITOR) },
                onStart = { navController.navigate(Destination.ROOM_LOBBY) },
            )
        }

        composable(Destination.ROLE_EDITOR) { entry ->
            val createRoomVm = createRoomVm(entry, navController)
            RoleEditorScreen(
                onBack = { navController.popBackStack() },
                onSave = { role ->
                    createRoomVm.addCustomRole(role)
                    navController.popBackStack()
                },
            )
        }

        composable(Destination.ROOM_LOBBY) { entry ->
            val createRoomVm = createRoomVm(entry, navController)
            val state by createRoomVm.state.collectAsStateWithLifecycle()
            val language = LocalAppLanguage.current
            val lobbyVm: LobbyViewModel = viewModel(
                key = "lobby:${language.tag}:${state.totalSeats}",
                factory = viewModelFactory {
                    initializer {
                        LobbyViewModel(
                            gameSession = OmertaApp.get().gameSession,
                            createRoomState = state,
                            language = language,
                        )
                    }
                },
            )
            LobbyScreen(
                viewModel = lobbyVm,
                onBack = { navController.popBackStack() },
                onStartReveal = {
                    lobbyVm.startGame()
                    navController.navigate(Destination.ROLE_REVEAL) {
                        // Clear the create-room stack so back from reveal goes home.
                        popUpTo(Destination.HOME) { inclusive = false }
                    }
                },
            )
        }

        composable(Destination.JOIN_ROOM) {
            JoinRoomScreen(
                onBack = { navController.popBackStack() },
                onJoinByCode = {
                    // LAN client wiring lands in a follow-up; for now we just
                    // close the screen on successful code+QR — keeps the UX flow
                    // intact without pretending we already joined a server.
                    navController.popBackStack()
                },
            )
        }

        // FIXED: removed duplicate if/else branch — both LAN and Local
        // navigate to PHASE_TRACKER identically. The dead branch was confusing.
        composable(Destination.ROLE_REVEAL) {
            RoleRevealScreen(
                onAllRevealed = {
                    navController.navigate(Destination.PHASE_TRACKER) {
                        popUpTo(Destination.HOME) { inclusive = false }
                    }
                },
            )
        }

        composable(Destination.PHASE_TRACKER) {
            PhaseTrackerScreen(
                onBack = { navController.popBackStack() },
                onGameEnd = {
                    navController.navigate(Destination.END_GAME) {
                        popUpTo(Destination.HOME) { inclusive = false }
                    }
                },
            )
        }

        composable(Destination.END_GAME) {
            EndGameScreen(
                // FIXED: navigate to CREATE_ROOM_MODE (not RULE_SETUP)
                // so a new CreateRoomViewModel is properly scoped. Navigating
                // directly to RULE_SETUP after popping left the VM ownerless.
                onPlayAgain = {
                    navController.navigate(Destination.CREATE_ROOM_MODE) {
                        popUpTo(Destination.HOME) { inclusive = false }
                    }
                },
                onHome = {
                    OmertaApp.get().gameSession.resetToLobby()
                    navController.navigate(Destination.HOME) {
                        popUpTo(Destination.HOME) { inclusive = true }
                    }
                },
            )
        }
    }
}

/**
 * Shares a single [CreateRoomViewModel] across the whole Create-Room flow.
 * We grab the back stack entry for `CREATE_ROOM_MODE` (the root of the
 * subgraph) — every subsequent entry in the flow re-resolves the same VM.
 */
@Composable
private fun createRoomVm(entry: NavBackStackEntry, navController: NavHostController): CreateRoomViewModel {
    val owner = remember(navController) {
        runCatching { navController.getBackStackEntry(Destination.CREATE_ROOM_MODE) }.getOrNull() ?: entry
    }
    return viewModel(
        viewModelStoreOwner = owner,
        factory = viewModelFactory { initializer { CreateRoomViewModel() } },
    )
}

// Slide helpers superseded by [NavTransitions] — kept only as a memory of the
// previous design language. Removed.
