package tech.huangsh.firstsight.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * 统一的视觉修饰
 *
 * 圆角、渐变、阴影集中在这里，避免各页面各写一套导致风格不统一。
 *
 * 所有深浅色的判断都走 [LocalAppDark]，即应用内的设置，
 * 而不是系统的 isSystemInDarkTheme——两者不一致时必须以应用为准。
 */
object FirstSightStyle {

    /** 大卡片圆角 */
    val cardLarge = RoundedCornerShape(24.dp)

    /** 中等卡片圆角 */
    val cardMedium = RoundedCornerShape(18.dp)

    /** 小元素圆角，用于图标底、按钮 */
    val small = RoundedCornerShape(12.dp)

    /** 按钮圆角 */
    val button = RoundedCornerShape(18.dp)

    /** 头像占位圆角 */
    val avatar = RoundedCornerShape(18.dp)

    /** 当前是否为深色（以应用设置为准） */
    @Composable
    fun isDark(): Boolean = LocalAppDark.current

    /** 按深浅色在当前两个色值间取一个 */
    @Composable
    fun profileColors(light: Color, dark: Color): Color = if (isDark()) dark else light

    /** 主色渐变，用于头图和主要按钮 */
    @Composable
    fun primaryGradient(): Brush {
        val p = FirstSightPalette
        return Brush.linearGradient(
            if (isDark()) {
                listOf(p.DarkGradientStart, p.DarkGradientMid, p.DarkGradientEnd)
            } else {
                listOf(p.LightGradientStart, p.LightGradientMid, p.LightGradientEnd)
            }
        )
    }

    /** 整页背景渐变 */
    @Composable
    fun pageGradient(): Brush {
        val p = FirstSightPalette
        return Brush.linearGradient(
            if (isDark()) {
                listOf(p.DarkPageTop, p.DarkPageMid, p.DarkPageBottom)
            } else {
                listOf(p.LightPageTop, p.LightPageMid, p.LightPageBottom)
            }
        )
    }

    /** 呼叫面板按钮渐变 */
    @Composable
    fun videoButtonGradient(): Brush {
        val p = FirstSightPalette
        return Brush.linearGradient(
            if (isDark()) {
                listOf(p.DarkVideoStart, p.DarkVideoEnd)
            } else {
                listOf(p.LightVideoStart, p.LightVideoEnd)
            }
        )
    }

    @Composable
    fun voiceButtonGradient(): Brush {
        val p = FirstSightPalette
        return Brush.linearGradient(
            if (isDark()) {
                listOf(p.DarkVoiceStart, p.DarkVoiceEnd)
            } else {
                listOf(p.LightVoiceStart, p.LightVoiceEnd)
            }
        )
    }

    @Composable
    fun callButtonGradient(): Brush {
        val p = FirstSightPalette
        return Brush.linearGradient(
            if (isDark()) {
                listOf(p.DarkCallStart, p.DarkCallEnd)
            } else {
                listOf(p.LightCallStart, p.LightCallEnd)
            }
        )
    }

    /** 常用应用图标底色渐变 */
    @Composable
    fun appIconGradient(): Brush {
        val p = FirstSightPalette
        return Brush.linearGradient(
            if (isDark()) {
                listOf(p.DarkAppIconStart, p.DarkAppIconEnd)
            } else {
                listOf(p.LightAppIconStart, p.LightAppIconEnd)
            }
        )
    }

    /** 呼叫提醒卡背景渐变 */
    @Composable
    fun alertGradient(): Brush {
        val p = FirstSightPalette
        return Brush.linearGradient(
            if (isDark()) {
                listOf(p.DarkAlertStart, p.DarkAlertEnd)
            } else {
                listOf(p.LightAlertStart, p.LightAlertEnd)
            }
        )
    }

    /** 提醒卡上的强调橙 */
    val alertAccent: Color get() = FirstSightPalette.AlertAccent

    /** 提醒卡主文字色 */
    @Composable
    fun alertTextColor(): Color =
        if (isDark()) FirstSightPalette.DarkAlertText else FirstSightPalette.LightAlertText

    /** 提醒卡次要文字色 */
    @Composable
    fun alertTextSubColor(): Color =
        if (isDark()) FirstSightPalette.DarkAlertTextSub else FirstSightPalette.LightAlertTextSub

    /**
     * 卡片阴影色
     *
     * 浅色下用淡蓝投影提升浮起感；深色下背景本就深，
     * 再加投影只会糊成一团，改为极淡的黑以保留层次边界。
     */
    @Composable
    fun cardShadow(): Color = if (isDark()) {
        Color.Black.copy(alpha = 0.45f)
    } else {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
    }

    /** 主色阴影，用于头图和强调按钮 */
    @Composable
    fun primaryShadow(): Color = if (isDark()) {
        Color.Black.copy(alpha = 0.55f)
    } else {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)
    }

    /**
     * 头图光晕颜色
     *
     * 深色下降低不透明度，避免在压暗的渐变上糊出一块白斑。
     */
    @Composable
    fun heroGlowColor(strong: Boolean): Color = if (isDark()) {
        Color.White.copy(alpha = if (strong) 0.10f else 0.06f)
    } else {
        Color.White.copy(alpha = if (strong) 0.30f else 0.16f)
    }

    /**
     * 联系人头像渐变色对
     *
     * 按索引循环取色，让相邻联系人视觉上有区分。
     */
    @Composable
    fun avatarGradientColors(index: Int): Pair<Color, Color> {
        val list = if (isDark()) FirstSightPalette.DarkAvatarGradients else FirstSightPalette.AvatarGradients
        val i = (index % list.size + list.size) % list.size
        return list[i]
    }

    /**
     * 取名字首字做头像文字
     *
     * 中文取第一个字，英文取首字母大写。
     */
    fun initialOf(name: String): String {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return ""
        val first = trimmed.first()
        return if (first.isLetter()) first.uppercase() else first.toString()
    }
}
