package uz.angrykitten.omerta.ui.howtoplay

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role as SemanticRole
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import uz.angrykitten.omerta.R
import uz.angrykitten.omerta.ui.common.OmertaTopBar
import uz.angrykitten.omerta.ui.theme.LocalOmertaColors
import uz.angrykitten.omerta.ui.common.DepthBackground

@Composable
fun HowToPlayScreen(onBack: () -> Unit) {
    var tab by remember { mutableStateOf(HowToTab.STORYBOARD) }

    DepthBackground {
        Column(modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
            OmertaTopBar(title = stringResource(id = R.string.howtoplay_title), onBack = onBack)
            HowToTabBar(selected = tab, onSelect = { tab = it })
            Box(modifier = Modifier.weight(1f)) {
                when (tab) {
                    HowToTab.STORYBOARD -> StoryboardPager()
                    HowToTab.WALKTHROUGH -> WalkthroughPager()
                }
            }
        }
    }
}

private enum class HowToTab { STORYBOARD, WALKTHROUGH }

@Composable
private fun HowToTabBar(selected: HowToTab, onSelect: (HowToTab) -> Unit) {
    val omerta = LocalOmertaColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(omerta.cardSurface.copy(alpha = 0.55f))
            .padding(4.dp),
    ) {
        TabPill(
            label = stringResource(id = R.string.howto_tab_rules),
            selected = selected == HowToTab.STORYBOARD,
            onClick = { onSelect(HowToTab.STORYBOARD) },
            modifier = Modifier.weight(1f),
        )
        TabPill(
            label = stringResource(id = R.string.howto_tab_guide),
            selected = selected == HowToTab.WALKTHROUGH,
            onClick = { onSelect(HowToTab.WALKTHROUGH) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun TabPill(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent)
            .clickable(role = SemanticRole.Tab, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun StoryboardPager() {
    val panels = remember { StoryboardPanels.list }
    val pagerState = rememberPagerState(pageCount = { panels.size })
    val scope = rememberCoroutineScopeShim()

    Column(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
        ) { page ->
            panels[page].Render()
        }
        DotIndicator(count = panels.size, selected = pagerState.currentPage)
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(id = R.string.howto_panel_progress, pagerState.currentPage + 1, panels.size),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(id = R.string.howto_skip),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.clickable {
                    scope.launch { pagerState.animateScrollToPage(panels.lastIndex, animationSpec = tween(400)) }
                },
            )
        }
    }
}

@Composable
private fun DotIndicator(count: Int, selected: Int) {
    val omerta = LocalOmertaColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        for (i in 0 until count) {
            val active = i == selected
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(width = if (active) 22.dp else 8.dp, height = 8.dp)
                    .clip(CircleShape)
                    .background(
                        if (active) MaterialTheme.colorScheme.secondary
                        else omerta.divider.copy(alpha = 0.7f),
                    ),
            )
        }
    }
}

@Composable
private fun WalkthroughPager() {
    val steps = remember { WalkthroughSteps.list }
    val pagerState = rememberPagerState(pageCount = { steps.size })

    Column(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
            steps[page].Render()
        }
        DotIndicator(count = steps.size, selected = pagerState.currentPage)
        Spacer(modifier = Modifier.height(24.dp))
    }
}

// AUDIT FIX: rememberCoroutineScope import — using direct call introduces a
// circular import problem with this file's existing animation imports during
// auto-import. Aliased through a tiny shim that just delegates.
@Composable
private fun rememberCoroutineScopeShim() = androidx.compose.runtime.rememberCoroutineScope()
