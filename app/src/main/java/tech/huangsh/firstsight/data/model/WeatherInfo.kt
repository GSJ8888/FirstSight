package tech.huangsh.firstsight.data.model

/**
 * 单日天气预报数据模型
 */
data class DailyForecast(
    val date: String = "",
    val weekday: String = "",
    val weather: String = "",
    val weatherIcon: String = "",
    val high: Int = 0,
    val low: Int = 0
)

/**
 * 天气获取错误类型
 */
enum class WeatherError {
    NETWORK,
    PARSE
}

/**
 * 天气信息数据模型
 */
data class WeatherInfo(
    val province: String = "",
    val city: String = "",
    val isAutoLocated: Boolean = true,
    val temperature: Int = 0,
    val high: Int? = null,
    val low: Int? = null,
    val weather: String = "",
    val weatherIcon: String = "",
    val humidity: Int = 0,
    val windDirection: String = "",
    val windPower: String = "",
    val windSpeed: Float = 0f,
    val forecast: List<DailyForecast> = emptyList(),
    val error: WeatherError? = null,
    val updateTime: Long = System.currentTimeMillis()
)
