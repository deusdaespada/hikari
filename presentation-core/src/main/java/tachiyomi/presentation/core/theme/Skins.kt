package tachiyomi.presentation.core.theme

import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.Color

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
