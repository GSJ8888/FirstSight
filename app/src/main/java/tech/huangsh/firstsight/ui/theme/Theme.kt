package tech.huangsh.firstsight.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import tech.huangsh.firstsight.data.model.ThemeMode

/**
 * 应用当前是否深色
 *
 * 以应用内设置为准，而不是系统设置。用户可以在应用内单独指定深浅色，
 * 两者不一致时，所有颜色都必须跟随应用。
 */
val LocalAppDark = staticCompositionLocalOf { false }

/**
 * 应用主题
 *
 * 色值直接写在这里而不是走 colorResource：
 * values-night 是按系统深浅色生效的限定符，若用户把应用强制设为深色
 * 而系统仍是浅色，colorResource 会取到浅色值，导致界面只有一半变深。
 */
@Composable
fun FirstSightTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = if (darkTheme) DarkColors else LightColors

    CompositionLocalProvider(LocalAppDark provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}
