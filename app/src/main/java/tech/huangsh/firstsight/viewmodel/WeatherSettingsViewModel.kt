package tech.huangsh.firstsight.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import tech.huangsh.firstsight.data.model.WeatherInfo
import tech.huangsh.firstsight.data.remote.WeatherService
import tech.huangsh.firstsight.data.repository.SettingsRepository
import tech.huangsh.firstsight.utils.VoiceAssistant
import javax.inject.Inject

/**
 * 天气设置页 ViewModel
 *
 * 与首页 MainViewModel 共用 WeatherService 单例的 currentWeather 状态流，
 * 切换城市或刷新后首页会自动同步最新天气。
 */
@HiltViewModel
class WeatherSettingsViewModel @Inject constructor(
    private val weatherService: WeatherService,
    private val settingsRepository: SettingsRepository,
    private val voiceAssistant: VoiceAssistant
) : ViewModel() {

    val weather: StateFlow<WeatherInfo?> = weatherService.currentWeather

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    /** 当前手动选择的城市唯一标识（province_city），空串表示根据定位自动选择 */
    private val _cityCode = MutableStateFlow("")
    val cityCode: StateFlow<String> = _cityCode.asStateFlow()

    init {
        viewModelScope.launch {
            val prov = settingsRepository.weatherProvince.first().trim()
            val city = settingsRepository.weatherCity.first().trim()
            _cityCode.value = if (prov.isNotBlank() && city.isNotBlank()) {
                "${prov}_$city"
            } else {
                ""
            }
        }
        refresh()
    }

    /**
     * 重新获取天气
     */
    fun refresh() {
        viewModelScope.launch {
            _loading.value = true
            weatherService.getWeatherInfo()
            _loading.value = false
        }
    }

    /**
     * 切换城市（传入空串表示恢复根据定位自动选择）
     */
    fun selectCity(province: String, city: String) {
        viewModelScope.launch {
            val cleanProv = province.trim()
            val cleanCity = city.trim()
            settingsRepository.updateWeatherCity(cleanProv, cleanCity)
            _cityCode.value = if (cleanProv.isNotBlank() && cleanCity.isNotBlank()) {
                "${cleanProv}_$cleanCity"
            } else {
                ""
            }
            _loading.value = true
            val info = weatherService.getWeatherInfo()
            _loading.value = false
            speakWeather(info)
        }
    }

    /**
     * 朗读当前所选城市的天气
     */
    fun speakCurrentWeather() {
        val info = weather.value ?: return
        viewModelScope.launch {
            speakWeather(info)
        }
    }

    private suspend fun speakWeather(info: WeatherInfo) {
        if (info.weather.isBlank()) return
        val currentSettings = settingsRepository.settings.first()
        if (!currentSettings.voiceEnabled) return

        val parts = mutableListOf<String>()
        val cityPrefix = info.city.takeIf { it.isNotBlank() } ?: ""
        parts.add("${cityPrefix}天气${info.weather}，当前${info.temperature}度")
        if (info.high != null && info.low != null) {
            parts.add("最高${info.high}度，最低${info.low}度")
        }
        voiceAssistant.speak(
            text = parts.joinToString("，"),
            volume = currentSettings.voiceVolume
        )
    }
}
