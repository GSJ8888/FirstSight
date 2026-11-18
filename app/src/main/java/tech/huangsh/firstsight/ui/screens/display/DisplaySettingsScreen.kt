package tech.huangsh.firstsight.ui.screens.display

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import tech.huangsh.firstsight.ui.theme.FirstSightIcons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import tech.huangsh.firstsight.R
import tech.huangsh.firstsight.data.model.Settings
import tech.huangsh.firstsight.data.model.ThemeMode
import tech.huangsh.firstsight.ui.screens.components.CommonTopBar
import tech.huangsh.firstsight.ui.theme.FirstSightStyle
import tech.huangsh.firstsight.viewmodel.SettingsViewModel

@Composable
fun DisplaySettingsScreen(
    onBack: () -> Unit,
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val settings by settingsViewModel.settings.collectAsState(initial = Settings())
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FirstSightStyle.pageGradient())
    ) {
        // 顶部栏 - 使用统一的样式
        CommonTopBar(
            title = stringResource(R.string.display_settings),
            onBack = onBack
        )

        // 设置列表
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 外观设置 - 深浅色三档
            DisplaySettingsItem(
                icon = FirstSightIcons.Palette,
                title = stringResource(R.string.theme_mode),
                subtitle = when (settings.themeMode) {
                    ThemeMode.SYSTEM -> stringResource(R.string.theme_system)
                    ThemeMode.LIGHT -> stringResource(R.string.theme_light)
                    ThemeMode.DARK -> stringResource(R.string.theme_dark)
                }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CompactThemeOption(
                        themeMode = ThemeMode.SYSTEM,
                        currentThemeMode = settings.themeMode,
                        title = stringResource(R.string.theme_system),
                        onClick = { settingsViewModel.updateThemeMode(ThemeMode.SYSTEM) }
                    )

                    CompactThemeOption(
                        themeMode = ThemeMode.LIGHT,
                        currentThemeMode = settings.themeMode,
                        title = stringResource(R.string.theme_light),
                        onClick = { settingsViewModel.updateThemeMode(ThemeMode.LIGHT) }
                    )

                    CompactThemeOption(
                        themeMode = ThemeMode.DARK,
                        currentThemeMode = settings.themeMode,
                        title = stringResource(R.string.theme_dark),
                        onClick = { settingsViewModel.updateThemeMode(ThemeMode.DARK) }
                    )
                }
            }
        }
    }
}

@Composable
fun DisplaySettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    content: @Composable () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .let { if (content == {}) it.clickable { onClick() } else it }
            .shadow(
                elevation = 6.dp,
                shape = FirstSightStyle.cardMedium,
                ambientColor = FirstSightStyle.cardShadow(),
                spotColor = FirstSightStyle.cardShadow()
            ),
        shape = FirstSightStyle.cardMedium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 图标背景
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(FirstSightStyle.small)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                // 标题和副标题
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (content == {}) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // 内容区域
            if (content != {}) {
                Spacer(modifier = Modifier.height(16.dp))
                content()
            }
        }
    }
}

@Composable
fun RowScope.CompactThemeOption(
    themeMode: ThemeMode,
    currentThemeMode: ThemeMode,
    title: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        // 固定高度：三个选项字数不同（跟随系统/浅色/深色），
        // 不固定的话长文案换行会把按钮撑高，导致高度参差
        modifier = Modifier.weight(1f).height(48.dp),
        contentPadding = PaddingValues(horizontal = 4.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (themeMode == currentThemeMode)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.surface,
            contentColor = if (themeMode == currentThemeMode)
                MaterialTheme.colorScheme.onPrimary
            else
                MaterialTheme.colorScheme.onSurface
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = title,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
