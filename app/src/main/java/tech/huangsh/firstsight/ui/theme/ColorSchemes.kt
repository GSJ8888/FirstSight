package tech.huangsh.firstsight.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * 浅色色板
 */
val LightColors = lightColorScheme(
    primary = Color(0xFF2E90E8),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0EFFF),
    onPrimaryContainer = Color(0xFF1264AB),

    secondary = Color(0xFF77574A),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDDD1),
    onSecondaryContainer = Color(0xFF2C160C),

    tertiary = Color(0xFF6C5D2F),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF5E1A6),
    onTertiaryContainer = Color(0xFF231B00),

    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),

    background = Color(0xFFF2F7FD),
    onBackground = Color(0xFF16202B),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF16202B),
    surfaceVariant = Color(0xFFE0EFFF),
    onSurfaceVariant = Color(0xFF4A5A6A),
    outline = Color(0xFF85736B)
)

/**
 * 深色色板
 */
val DarkColors = darkColorScheme(
    primary = Color(0xFF4FA6F2),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1A3A5C),
    onPrimaryContainer = Color(0xFFE8F4FD),

    secondary = Color(0xFFA8918A),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF3A2A24),
    onSecondaryContainer = Color(0xFFFFDDD1),

    tertiary = Color(0xFFD4C87A),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF3A3320),
    onTertiaryContainer = Color(0xFFF5E1A6),

    error = Color(0xFFFF8A80),
    onError = Color.White,
    errorContainer = Color(0xFF4E1512),
    onErrorContainer = Color(0xFFFFDAD6),

    background = Color(0xFF0E141C),
    onBackground = Color(0xFFEAF0F6),
    surface = Color(0xFF1A222C),
    onSurface = Color(0xFFEAF0F6),
    surfaceVariant = Color(0xFF26303C),
    onSurfaceVariant = Color(0xFFB4C0CC),
    outline = Color(0xFF5A6675)
)

/**
 * 两套色板共用的视觉常量
 *
 * 这些颜色此前放在 colors.xml，但 values-night 是按系统深浅色生效的，
 * 应用强制深色时会取错值，因此改为代码内按 LocalAppDark 选择。
 */
object FirstSightPalette {

    // 主色渐变
    val LightGradientStart = Color(0xFF4FA6F2)
    val LightGradientMid = Color(0xFF2E90E8)
    val LightGradientEnd = Color(0xFF1E7FD8)

    val DarkGradientStart = Color(0xFF3D8FD6)
    val DarkGradientMid = Color(0xFF2E90E8)
    val DarkGradientEnd = Color(0xFF1A6BB8)

    // 整页背景渐变
    val LightPageTop = Color(0xFFE8F2FC)
    val LightPageMid = Color(0xFFF5F9FE)
    val LightPageBottom = Color(0xFFEDF4FB)

    val DarkPageTop = Color(0xFF121A24)
    val DarkPageMid = Color(0xFF0E141C)
    val DarkPageBottom = Color(0xFF101824)

    // 呼叫面板按钮渐变
    val LightVideoStart = Color(0xFF5FD394)
    val LightVideoEnd = Color(0xFF35B574)
    val DarkVideoStart = Color(0xFF2E8B5F)
    val DarkVideoEnd = Color(0xFF1E7349)

    val LightVoiceStart = Color(0xFFFFB05C)
    val LightVoiceEnd = Color(0xFFF58A2E)
    val DarkVoiceStart = Color(0xFFC4762E)
    val DarkVoiceEnd = Color(0xFFA15C1C)

    val LightCallStart = Color(0xFF5AAAF5)
    val LightCallEnd = Color(0xFF1E7FD8)
    val DarkCallStart = Color(0xFF2E7BC4)
    val DarkCallEnd = Color(0xFF1A5F9E)

    // 常用应用图标底
    val LightAppIconStart = Color(0xFFF0EEFF)
    val LightAppIconEnd = Color(0xFFE2DEFC)
    val DarkAppIconStart = Color(0xFF242038)
    val DarkAppIconEnd = Color(0xFF1C1830)

    // 呼叫提醒卡
    val LightAlertStart = Color(0xFFFFF4E8)
    val LightAlertEnd = Color(0xFFFFEBD6)
    val LightAlertText = Color(0xFF94530C)
    val LightAlertTextSub = Color(0xFFB0762F)

    val DarkAlertStart = Color(0xFF3A2A18)
    val DarkAlertEnd = Color(0xFF2E2114)
    val DarkAlertText = Color(0xFFFFB865)
    val DarkAlertTextSub = Color(0xFFC99455)

    // 提醒卡的强调橙
    val AlertAccent = Color(0xFFFF9A3C)

    /** 联系人头像渐变，按索引循环 */
    val AvatarGradients: List<Pair<Color, Color>> = listOf(
        Color(0xFF6FBBF8) to Color(0xFF2E90E8),
        Color(0xFF7DD3A8) to Color(0xFF35A870),
        Color(0xFFFFB865) to Color(0xFFF58A2E),
        Color(0xFFA79BF5) to Color(0xFF7C6BEA),
        Color(0xFFF58FA6) to Color(0xFFE4637F),
        Color(0xFF64C8D4) to Color(0xFF2FA0AE)
    )

    val DarkAvatarGradients: List<Pair<Color, Color>> = listOf(
        Color(0xFF4A8FC0) to Color(0xFF2A6E9E),
        Color(0xFF4FA377) to Color(0xFF357C55),
        Color(0xFFC4843F) to Color(0xFF9A6329),
        Color(0xFF7A6EB8) to Color(0xFF5A4E96),
        Color(0xFFB86A7C) to Color(0xFF944E5E),
        Color(0xFF4A9AA4) to Color(0xFF337680)
    )
}
