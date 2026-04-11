package tachiyomi.presentation.core.theme

import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.Color
import org.intellij.lang.annotations.Language

/**
 * Defines a set of colors used to drive the AGSL materials.
 */
data class SkinColors(
    val main: Color,
    val accent: Color,
    val background: Color,
    val isDark: Boolean,
)

/**
 * Defines a visual "Material" or "Skin" that can be applied via AGSL shaders.
 */
interface Skin {
    /**
     * The AGSL shader code for this skin.
     */
    val shaderCode: String

    /**
     * Updates the uniforms for the given [RuntimeShader].
     */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun updateUniforms(shader: RuntimeShader, time: Float, colors: SkinColors)
}

/**
 * A "Glass" material with multi-color refraction and subtle chromatic aberration.
 */
object GlassSkin : Skin {
    override val shaderCode: String = """
        uniform shader content;
        uniform float time;
        uniform vec4 mainColor;
        uniform vec4 accentColor;

        vec4 main(vec2 coords) {
            vec2 uv = coords.xy / 1000.0;

            // Multi-layered distortion for a more refractive look
            float d1 = sin(uv.x * 12.0 + time * 0.5) * 0.003;
            float d2 = cos(uv.y * 8.0 - time * 0.3) * 0.002;

            vec4 cR = content.eval(coords + d1);
            vec4 cG = content.eval(coords + d2);
            vec4 cB = content.eval(coords - d1);

            vec4 c = vec4(cR.r, cG.g, cB.b, (cR.a + cG.a + cB.a) / 3.0);

            // Blend with a gradient of main and accent colors
            float blend = clamp(sin(uv.x * 5.0 + uv.y * 3.0) * 0.5 + 0.5, 0.0, 1.0);
            vec4 tint = mix(mainColor, accentColor, blend);

            return mix(c, tint, 0.15);
        }
    """.trimIndent()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun updateUniforms(shader: RuntimeShader, time: Float, colors: SkinColors) {
        shader.setFloatUniform("time", time)
        shader.setFloatUniform("mainColor", colors.main.targetColor())
        shader.setFloatUniform("accentColor", colors.accent.targetColor())
    }
}

/**
 * A "Liquid" material with melting/metaball effects and color blending.
 */
object LiquidSkin : Skin {
    override val shaderCode: String = """
        uniform shader content;
        uniform float time;
        uniform vec4 mainColor;
        uniform vec4 accentColor;

        vec4 main(vec2 coords) {
            vec2 uv = coords.xy / 1000.0;
            float s1 = sin(uv.x * 25.0 + time * 1.5) * 0.004;
            float s2 = cos(uv.y * 18.0 + time * 2.0) * 0.003;

            vec4 c = content.eval(coords + vec2(s1, s2));
            float pulse = sin(time * 0.8) * 0.5 + 0.5;
            vec4 colorMix = mix(mainColor, accentColor, pulse * uv.y);

            return mix(c, colorMix, 0.1);
        }
    """.trimIndent()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun updateUniforms(shader: RuntimeShader, time: Float, colors: SkinColors) {
        shader.setFloatUniform("time", time)
        shader.setFloatUniform("mainColor", colors.main.targetColor())
        shader.setFloatUniform("accentColor", colors.accent.targetColor())
    }
}

/**
 * A "Frosted" material that simulates a blurred, tinted glass surface.
 */
object FrostedSkin : Skin {
    override val shaderCode: String = """
        uniform shader content;
        uniform float time;
        uniform vec4 mainColor;

        float hash(vec2 p) {
            return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453123);
        }

        vec4 main(vec2 coords) {
            float noise = hash(coords + time) * 0.01;
            vec4 c = content.eval(coords + noise);
            return mix(c, mainColor, 0.3);
        }
    """.trimIndent()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun updateUniforms(shader: RuntimeShader, time: Float, colors: SkinColors) {
        shader.setFloatUniform("time", time)
        shader.setFloatUniform("mainColor", colors.main.targetColor())
    }
}

/**
 * A simple "Solid" skin that acts as a fallback or base.
 */
object DefaultSkin : Skin {
    override val shaderCode: String = """
        uniform shader content;
        vec4 main(vec2 coords) {
            return content.eval(coords);
        }
    """.trimIndent()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun updateUniforms(shader: RuntimeShader, time: Float, colors: SkinColors) {
    }
}


/**
 * Internal helper to convert Compose color to AGSL uniform array.
 */
private fun Color.targetColor(): FloatArray {
    return floatArrayOf(red, green, blue, alpha)
}

/**
 * Shader for the flowing liquid progress bar.
 */
@Language("AGSL")
const val LIQUID_PROGRESS_SHADER = """
    uniform float progress;
    uniform float time;
    uniform vec4 mainColor;
    uniform vec4 accentColor;
    uniform vec2 resolution;

    float hash(vec2 p) {
        return fract(sin(dot(p, vec2(12.71, 31.17))) * 43758.5453);
    }

    half4 main(float2 coords) {
        float2 uv = coords / resolution;

        float w1 = sin(uv.y * 10.0 + time * 4.0) * 0.02;
        float w2 = sin(uv.y * 25.0 - time * 6.0) * 0.01;
        float w3 = sin(uv.y * 5.0 + time * 2.0) * 0.03;
        float threshold = progress + w1 + w2 + w3;

        if (uv.x < threshold) {
            float depth = uv.y;
            vec4 col = mix(mainColor, accentColor, uv.x / max(progress, 0.1));

            col.rgb *= (1.0 - depth * 0.2); // Darker at bottom
            float topHighlight = smoothstep(0.15, 0.0, depth) * 0.2;
            col.rgb += topHighlight;

            float distToEdge = threshold - uv.x;
            float edgeGlow = smoothstep(0.12, 0.0, distToEdge);
            vec4 glowCol = mix(col, vec4(1.0, 1.0, 1.0, col.a), edgeGlow * 0.4);

            float2 bubbleUv = uv * vec2(20.0, 5.0);
            float n = hash(floor(bubbleUv + vec2(time * 2.0, 0.0)));
            float bubble = smoothstep(0.1, 0.0, length(fract(bubbleUv + vec2(time * 2.0, 0.0)) - 0.5)) * step(0.98, n);
            glowCol.rgb += bubble * 0.3;

            float shimmer = sin(uv.x * 40.0 - time * 12.0) * 0.03;
            glowCol.rgb += shimmer;

            return half4(glowCol);
        }

        float bgGlow = smoothstep(0.05, 0.0, uv.x - threshold) * 0.1;
        return half4(vec4(mainColor.rgb, 0.1 + bgGlow));
    }
"""
