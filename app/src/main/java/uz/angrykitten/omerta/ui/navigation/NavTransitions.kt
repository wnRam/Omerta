package uz.angrykitten.omerta.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.navigation.NavBackStackEntry

/**
 * Cinematic nav transitions.
 *
 * Forward (push): the new screen rises in from below with a tiny scale-up
 *                 (think: a card being dealt onto the table). Outgoing
 *                 screen fades down quickly.
 * Backward (pop): the current screen drops back down. Underlying screen
 *                 scales back to identity from a 0.96f resting state.
 *
 * Times are tuned to ~280–340ms — fast enough to never feel laggy, slow
 * enough to read as deliberate cinema.
 */
object NavTransitions {

    private const val PUSH_MS = 320
    private const val POP_MS = 280

    val push: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        fadeIn(animationSpec = tween(PUSH_MS, easing = LinearOutSlowInEasing)) +
            scaleIn(
                initialScale = 0.94f,
                animationSpec = tween(PUSH_MS, easing = LinearOutSlowInEasing),
            ) +
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Up,
                animationSpec = tween(PUSH_MS, easing = LinearOutSlowInEasing),
                initialOffset = { it / 12 },
            )
    }

    val pushExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        fadeOut(animationSpec = tween(160, easing = FastOutSlowInEasing)) +
            scaleOut(
                targetScale = 1.04f,
                animationSpec = tween(220, easing = FastOutSlowInEasing),
            )
    }

    val popEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        fadeIn(animationSpec = tween(220, easing = LinearOutSlowInEasing)) +
            scaleIn(
                initialScale = 0.96f,
                animationSpec = tween(POP_MS, easing = LinearOutSlowInEasing),
            )
    }

    val popExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        fadeOut(animationSpec = tween(POP_MS, easing = EaseOutCubic)) +
            scaleOut(
                targetScale = 0.92f,
                animationSpec = tween(POP_MS, easing = EaseOutCubic),
            ) +
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Down,
                animationSpec = tween(POP_MS, easing = EaseOutCubic),
                targetOffset = { it / 10 },
            )
    }
}
