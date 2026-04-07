package tachiyomi.presentation.core.components.material

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

/**
 * A custom NavigationBarItem that supports haptic-like scale animations and smooth color transitions.
 */
@Composable
fun RowScope.AnimatedNavigationBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: @Composable (() -> Unit)? = null,
    alwaysShowLabel: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val styledIcon = @Composable {
        val iconColor by animateColorAsState(
            targetValue = if (selected) {
                MaterialTheme.colorScheme.onSecondaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            label = "iconColor",
        )
        val iconScale by animateFloatAsState(
            targetValue = if (selected) 1.1f else 1.0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow,
            ),
            label = "iconScale",
        )
        CompositionLocalProvider(LocalContentColor provides iconColor) {
            Box(Modifier.scale(iconScale)) {
                icon()
            }
        }
    }

    val styledLabel = label?.let {
        @Composable {
            val labelColor by animateColorAsState(
                targetValue = if (selected) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                label = "labelColor",
            )
            val labelScale by animateFloatAsState(
                targetValue = if (selected) 1.05f else 1.0f,
                animationSpec = spring(
                    stiffness = Spring.StiffnessLow,
                ),
                label = "labelScale",
            )
            CompositionLocalProvider(LocalContentColor provides labelColor) {
                Box(Modifier.scale(labelScale)) {
                    it()
                }
            }
        }
    }

    Box(
        modifier = modifier
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.Tab,
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
            )
            .weight(1f)
            .fillMaxHeight(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            styledIcon()
            if (styledLabel != null && alwaysShowLabel) {
                Box(modifier = Modifier.padding(top = 4.dp)) {
                    styledLabel()
                }
            }
        }
    }
}
