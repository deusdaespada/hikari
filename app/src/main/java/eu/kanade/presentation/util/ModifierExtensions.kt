package eu.kanade.presentation.util

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

private enum class InteractionState {
    Pressed,
    Idle,
}

fun Modifier.bounceClick(
    enabled: Boolean = true,
) = if (enabled) {
    composed {
        val haptic = LocalHapticFeedback.current
        val scale = remember { Animatable(1f) }

        Modifier
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
            .pointerInput(Unit) {
                coroutineScope {
                    while (true) {
                        val pointerId = awaitPointerEventScope {
                            awaitFirstDown(requireUnconsumed = false).id
                        }

                        launch {
                            scale.animateTo(
                                0.95f,
                                spring(
                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                    stiffness = Spring.StiffnessLow,
                                ),
                            )
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }

                        val up = awaitPointerEventScope {
                            waitForUpOrCancellation()
                        }

                        if (up != null) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }

                        launch {
                            scale.animateTo(
                                1f,
                                spring(
                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                    stiffness = Spring.StiffnessLow,
                                ),
                            )
                        }
                    }
                }
            }
    }
} else {
    this
}
