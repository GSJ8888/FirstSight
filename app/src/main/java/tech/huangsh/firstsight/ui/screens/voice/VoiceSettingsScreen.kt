package tech.huangsh.firstsight.ui.screens.voice

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import tech.huangsh.firstsight.ui.theme.FirstSightIcons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlin.math.roundToInt
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import tech.huangsh.firstsight.R
import tech.huangsh.firstsight.data.model.Settings
import tech.huangsh.firstsight.ui.screens.components.CommonTopBar
import tech.huangsh.firstsight.ui.theme.FirstSightStyle
import tech.huangsh.firstsight.viewmodel.SettingsViewModel

@Composable
fun VoiceSettingsScreen(
    onBack: () -> Unit,
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val loadedSettings by settingsViewModel.settings.collectAsState(initial = null)
    val settings = loadedSettings ?: Settings()
    val languageAvailable by settingsViewModel.voiceLanguageAvailable.collectAsState()

    var volumeSlider by remember { mutableFloatStateOf(0.8f) }
    var isDragging by remember { mutableStateOf(false) }
    var initializedFromStore by remember { mutableStateOf(false) }

    var thresholdSlider by remember { mutableIntStateOf(20) }
    var isDraggingThreshold by remember { mutableStateOf(false) }
    var thresholdInitialized by remember { mutableStateOf(false) }

    // 等 DataStore 从磁盘读取出真实配置后再同步到滑块，避免 initial 默认值盖住真实设置
    LaunchedEffect(loadedSettings) {
        val realSettings = loadedSettings ?: return@LaunchedEffect
        if (!initializedFromStore && !isDragging) {
            volumeSlider = realSettings.voiceVolume
            initializedFromStore = true
        }
        if (!thresholdInitialized && !isDraggingThreshold) {
            thresholdSlider = realSettings.lowBatteryThreshold
            thresholdInitialized = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FirstSightStyle.pageGradient())
    ) {
        CommonTopBar(
            title = stringResource(R.string.voice_settings),
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 语音引擎不可用时先说明原因，避免用户以为功能坏了
            if (!languageAvailable) {
                VoiceWarningCard(stringResource(R.string.voice_unavailable))
            }

            VoiceSettingsItem(
                icon = FirstSightIcons.VolumeUp,
                title = stringResource(R.string.voice_assistant)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.voice_feedback_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 27.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = settings.voiceEnabled,
                        onCheckedChange = { settingsViewModel.updateVoiceEnabled(it) }
                    )
                }
            }

            // 语音关闭时隐藏子项，避免用户误以为设置无效
            if (settings.voiceEnabled) {
                VoiceSettingsItem(
                    icon = FirstSightIcons.Schedule,
                    title = stringResource(R.string.voice_hourly_chime)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.voice_hourly_chime_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = settings.hourlyChimeEnabled,
                            onCheckedChange = { settingsViewModel.updateHourlyChimeEnabled(it) }
                        )
                    }
                }

                VoiceSettingsItem(
                    icon = FirstSightIcons.PhoneInTalk,
                    title = stringResource(R.string.voice_call_prompt)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.voice_call_prompt_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = settings.callPromptEnabled,
                            onCheckedChange = { settingsViewModel.updateCallPromptEnabled(it) }
                        )
                    }
                }

                VoiceSettingsItem(
                    icon = FirstSightIcons.VolumeUp,
                    title = stringResource(R.string.voice_volume)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(
                                R.string.voice_volume_value,
                                (volumeSlider * 100).toInt()
                            ),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Slider(
                            value = volumeSlider,
                            onValueChange = {
                                isDragging = true
                                volumeSlider = it
                            },
                            valueRange = 0.1f..1.0f,
                            steps = 8,
                            onValueChangeFinished = {
                                isDragging = false
                                initializedFromStore = true
                                settingsViewModel.updateVoiceVolume(volumeSlider)
                                settingsViewModel.previewVoice(volume = volumeSlider)
                            }
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.voice_low),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = stringResource(R.string.voice_loud),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                VoiceSettingsItem(
                    icon = FirstSightIcons.BatterySaver,
                    title = stringResource(R.string.voice_low_battery_alert)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.voice_low_battery_alert_desc),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                checked = settings.lowBatteryAlertEnabled,
                                onCheckedChange = { settingsViewModel.updateLowBatteryAlertEnabled(it) }
                            )
                        }

                        if (settings.lowBatteryAlertEnabled) {
                            Spacer(Modifier.height(14.dp))
                            Text(
                                text = stringResource(R.string.voice_low_battery_threshold, thresholdSlider),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Slider(
                                value = thresholdSlider.toFloat(),
                                onValueChange = { raw ->
                                    isDraggingThreshold = true
                                    val snapped = ((raw / 5f).roundToInt() * 5).coerceIn(10, 50)
                                    thresholdSlider = snapped
                                },
                                valueRange = 10f..50f,
                                steps = 7,
                                onValueChangeFinished = {
                                    isDraggingThreshold = false
                                    thresholdInitialized = true
                                    settingsViewModel.updateLowBatteryThreshold(thresholdSlider)
                                }
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "10%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "50%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VoiceSettingsItem(
    icon: ImageVector,
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
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
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
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
                Spacer(Modifier.width(20.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
private fun VoiceWarningCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = FirstSightStyle.cardMedium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.padding(16.dp)
        )
    }
}
