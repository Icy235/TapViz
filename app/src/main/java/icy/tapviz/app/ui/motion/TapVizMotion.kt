package icy.tapviz.app.ui.motion

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring

/**
 * Централизованный справочник параметров и настроек анимаций интерфейса TapViz.
 */
object TapVizMotion {
    const val ActiveCardGlowDurationMs = 2_400
    const val CounterEnterDurationMs = 150
    const val CounterExitDurationMs = 100
    const val CardDepthScale = 0.075f
    const val CardPerspectiveDegrees = 7f
    const val CardDepthOffsetDp = 9f
    const val FocusedCardScale = 1.012f
    const val CardFocusDamping = 0.52f
    const val CardDetailsEnterDurationMs = 250
    const val CardDetailsExitDurationMs = 150
    const val ActionPressScale = 0.94f
    const val LinkRevealDurationMs = 170
    const val LinkCollapseDurationMs = 100

    val BouncySpring = spring<Float>(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow)
    val GentleSpring = spring<Float>(dampingRatio = 0.8f, stiffness = Spring.StiffnessLow)
}
