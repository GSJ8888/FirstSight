package tech.huangsh.firstsight.viewmodel

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tech.huangsh.firstsight.R
import tech.huangsh.firstsight.data.model.ThemeMode
import tech.huangsh.firstsight.data.repository.SettingsRepository
import tech.huangsh.firstsight.utils.LauncherUtils
import tech.huangsh.firstsight.utils.VoiceAssistant
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val voiceAssistant: VoiceAssistant,
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    val settings = settingsRepository.settings

    /**
     * 中文语音是否可用
     *
     * 语音引擎异步初始化，因此用单独的状态承载，避免暴露 VoiceAssistant 实例
     */
    private val _voiceLanguageAvailable = MutableStateFlow(true)
    val voiceLanguageAvailable: StateFlow<Boolean> = _voiceLanguageAvailable
    
    private val _isLauncherMode = MutableStateFlow(false)
    val isLauncherMode: StateFlow<Boolean> = _isLauncherMode
    
    private val _floatingBallEnabled = MutableStateFlow(false)
    val floatingBallEnabled: StateFlow<Boolean> = _floatingBallEnabled
    
    private val _isDefaultLauncher = MutableStateFlow(false)
    val isDefaultLauncher: StateFlow<Boolean> = _isDefaultLauncher
    
    init {
        checkLauncherMode()
        loadFloatingBallSetting()
        observeVoiceState()
        checkDefaultLauncherStatus()
    }
    
    /**
     * 检查是否是默认桌面
     */
    private fun checkLauncherMode() {
        viewModelScope.launch {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            _isLauncherMode.value = am.isLowRamDevice // Simplified check
        }
    }
    
    /**
     * 检查默认启动器状态
     */
    private fun checkDefaultLauncherStatus() {
        viewModelScope.launch {
            val isDefault = LauncherUtils.isDefaultLauncher(context)
            _isDefaultLauncher.value = isDefault
            settingsRepository.updateIsDefaultLauncher(isDefault)
        }
    }
    
    /**
     * 加载悬浮球设置
     */
    private fun loadFloatingBallSetting() {
        viewModelScope.launch {
            val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            _floatingBallEnabled.value = prefs.getBoolean("floating_ball_enabled", false)
        }
    }
    
    /**
     * 把语音引擎状态同步到界面
     *
     * 引擎是应用级单例，这里只做状态订阅，不负责创建和释放。
     */
    private fun observeVoiceState() {
        viewModelScope.launch {
            voiceAssistant.languageAvailable.collect { _voiceLanguageAvailable.value = it }
        }
    }
    
    /**
     * 更新语音反馈开关
     */
    fun updateVoiceEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateVoiceEnabled(enabled)
        }
        if (!enabled) {
            voiceAssistant.stop()
        }
    }
    
    /**
     * 更新整点报时开关
     */
    fun updateHourlyChimeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateHourlyChimeEnabled(enabled)
        }
    }

    /**
     * 更新低电量语音提醒开关
     */
    fun updateLowBatteryAlertEnabled(enabled: Boolean) {
        viewModelScope.launch {
            withContext(NonCancellable) {
                settingsRepository.updateLowBatteryAlertEnabled(enabled)
            }
        }
    }

    /**
     * 更新低电量提醒阈值
     */
    fun updateLowBatteryThreshold(threshold: Int) {
        viewModelScope.launch {
            withContext(NonCancellable) {
                settingsRepository.updateLowBatteryThreshold(threshold)
            }
        }
    }

    /**
     * 更新微信来电自动接听开关
     */
    fun updateWechatAutoAnswerEnabled(enabled: Boolean) {
        viewModelScope.launch {
            withContext(NonCancellable) {
                settingsRepository.updateWechatAutoAnswerEnabled(enabled)
            }
        }
    }

    /**
     * 更新是否仅自动接听已添加的联系人
     */
    fun updateWechatAutoAnswerContactsOnly(contactsOnly: Boolean) {
        viewModelScope.launch {
            withContext(NonCancellable) {
                settingsRepository.updateWechatAutoAnswerContactsOnly(contactsOnly)
            }
        }
    }

    /**
     * 更新微信自动接听后自动开启免提开关
     */
    fun updateWechatAutoSpeakerphone(enabled: Boolean) {
        viewModelScope.launch {
            withContext(NonCancellable) {
                settingsRepository.updateWechatAutoSpeakerphone(enabled)
            }
        }
    }

    /**
     * 更新微信语音/视频挂断后自动返回首页开关
     */
    fun updateWechatReturnHomeAfterCall(enabled: Boolean) {
        viewModelScope.launch {
            withContext(NonCancellable) {
                settingsRepository.updateWechatReturnHomeAfterCall(enabled)
            }
        }
    }

    /**
     * 更新电话挂断后自动返回首页开关
     */
    fun updatePhoneReturnHomeAfterCall(enabled: Boolean) {
        viewModelScope.launch {
            withContext(NonCancellable) {
                settingsRepository.updatePhoneReturnHomeAfterCall(enabled)
            }
        }
    }

    /**
     * 更新语音音量（使用 NonCancellable 确保退出页面时也能完整落盘）
     */
    fun updateVoiceVolume(volume: Float) {
        viewModelScope.launch {
            withContext(NonCancellable) {
                settingsRepository.updateVoiceVolume(volume)
            }
        }
    }

    /**
     * 更新呼叫语音提示开关
     */
    fun updateCallPromptEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateCallPromptEnabled(enabled)
        }
    }
    
    /**
     * 更新主题模式
     */
    fun updateThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.updateThemeMode(themeMode)
        }
    }
    
    /**
     * 更新开机自启动
     */
    fun updateAutoStartEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateAutoStartEnabled(enabled)
        }
    }
    
    /**
     * 用指定音量试听
     *
     * 拖动音量滑块松手时调用。直接传参而不是回读 DataStore，
     * 是因为刚写入的值不一定立刻从 Flow 里读得到。
     */
    fun previewVoice(volume: Float) {
        voiceAssistant.speak(
            text = context.getString(R.string.voice_test_text),
            volume = volume
        )
    }

    /**
     * 打开系统「默认桌面」设置页
     */
    fun openDefaultAppSettings() {
        LauncherUtils.openDefaultAppSettings(context)
    }
    
    /**
     * 刷新默认启动器状态
     */
    fun refreshDefaultLauncherStatus() {
        checkDefaultLauncherStatus()
    }

    /**
     * 首次启动引导是否已完成
     */
    suspend fun isOnboardingCompleted(): Boolean = settingsRepository.isOnboardingCompleted()

    /**
     * 引导完成标记的实时状态
     *
     * null 表示尚未读取，UI 据此显示占位而不闪桌面
     */
    private val _onboardingCompleted = MutableStateFlow<Boolean?>(null)
    val onboardingCompleted: StateFlow<Boolean?> = _onboardingCompleted

    fun refreshOnboardingCompleted() {
        viewModelScope.launch {
            _onboardingCompleted.value = settingsRepository.isOnboardingCompleted()
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        // 语音引擎是应用级单例，这里只停止当前播报，不做 shutdown
        voiceAssistant.stop()
    }
}