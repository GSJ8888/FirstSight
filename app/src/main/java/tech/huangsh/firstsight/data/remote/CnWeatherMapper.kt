package tech.huangsh.firstsight.data.remote

import tech.huangsh.firstsight.data.model.DailyForecast
import tech.huangsh.firstsight.data.model.WeatherInfo
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * 腾讯天气响应转换为应用内的 WeatherInfo（包含当天实时天气与未来一周预报）
 */
object CnWeatherMapper {

    fun toWeatherInfo(
        response: CnWeatherResponse,
        province: String = "",
        city: String = "",
        isAutoLocated: Boolean = true
    ): WeatherInfo? {
        if (response.status != null && response.status != 200) return null
        val data = response.data ?: return null
        val observe = data.observe
        val today = data.forecast_24h?.get("1") ?: data.forecast_24h?.values?.firstOrNull()

        val weather = observe?.weather?.trim()?.takeIf { it.isNotBlank() }
            ?: observe?.weather_short?.trim()?.takeIf { it.isNotBlank() }
            ?: today?.day_weather?.trim()?.takeIf { it.isNotBlank() }
            ?: today?.day_weather_short?.trim().orEmpty()

        if (weather.isBlank()) return null

        val todayHigh = today?.max_degree?.trim()?.toIntOrNull()
        val todayLow = today?.min_degree?.trim()?.toIntOrNull()

        // 当前温度优先取实时 observe.degree，缺失时用今日最高最低的平均值
        val temperature = observe?.degree?.trim()?.toFloatOrNull()?.toInt()
            ?: averageOf(today?.max_degree, today?.min_degree)
            ?: return null

        val forecastList = parseForecastList(data.forecast_24h)

        val windDir = observe?.wind_direction_name?.trim()?.takeIf { it.isNotBlank() }
            ?: today?.day_wind_direction?.trim().orEmpty()
        val windPow = observe?.wind_power?.trim()?.takeIf { it.isNotBlank() }
            ?: today?.day_wind_power?.trim().orEmpty()

        return WeatherInfo(
            province = cleanAdminName(province),
            city = cleanCityName(city),
            isAutoLocated = isAutoLocated,
            temperature = temperature,
            high = todayHigh ?: forecastList.firstOrNull()?.high,
            low = todayLow ?: forecastList.firstOrNull()?.low,
            weather = weather,
            weatherIcon = iconOf(weather),
            humidity = observe?.humidity?.replace("%", "")?.trim()?.toIntOrNull() ?: 0,
            windDirection = windDir,
            windPower = windPow,
            windSpeed = 0f,
            forecast = forecastList,
            error = null,
            updateTime = System.currentTimeMillis()
        )
    }

    /**
     * 解析未来一周（从今天起共 7 天，对应 key "1".."7"）的天气预报
     */
    private fun parseForecastList(forecastMap: Map<String, CnForecastDay>?): List<DailyForecast> {
        if (forecastMap.isNullOrEmpty()) return emptyList()

        // forecast_24h 中 "0" 是昨天，"1" 是今天，"2".."7" 是未来 6 天
        val sortedEntries = forecastMap.entries
            .mapNotNull { (k, v) -> k.toIntOrNull()?.let { idx -> idx to v } }
            .filter { (idx, _) -> idx >= 1 }
            .sortedBy { it.first }
            .ifEmpty {
                forecastMap.entries
                    .mapIndexed { idx, entry -> (idx + 1) to entry.value }
            }

        return sortedEntries.mapIndexedNotNull { listIndex, (_, day) ->
            val dayW = day.day_weather?.trim()?.takeIf { it.isNotBlank() }
                ?: day.day_weather_short?.trim().orEmpty()
            val nightW = day.night_weather?.trim()?.takeIf { it.isNotBlank() }
                ?: day.night_weather_short?.trim().orEmpty()
            val primaryWeather = dayW.ifBlank { nightW }
            if (primaryWeather.isBlank()) return@mapIndexedNotNull null

            val combinedWeather = if (dayW.isNotBlank() && nightW.isNotBlank() && dayW != nightW) {
                "${dayW}转${nightW}"
            } else {
                primaryWeather
            }

            val high = day.max_degree?.trim()?.toIntOrNull() ?: 0
            val low = day.min_degree?.trim()?.toIntOrNull() ?: 0
            val rawDate = day.time?.trim().orEmpty()

            DailyForecast(
                date = formatMonthDay(rawDate),
                weekday = formatWeekday(rawDate, listIndex),
                weather = combinedWeather,
                weatherIcon = iconOf(primaryWeather),
                high = high,
                low = low
            )
        }
    }

    private fun formatMonthDay(ymd: String): String {
        // "2026-11-18" -> "11月18日"
        val parts = ymd.split("-")
        if (parts.size == 3) {
            val m = parts[1].toIntOrNull()
            val d = parts[2].toIntOrNull()
            if (m != null && d != null) {
                return "${m}月${d}日"
            }
        }
        return ymd
    }

    private fun formatWeekday(ymd: String, indexFromToday: Int): String {
        when (indexFromToday) {
            0 -> return "今天"
            1 -> return "明天"
        }
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
            val date = sdf.parse(ymd) ?: return ""
            val cal = Calendar.getInstance().apply { time = date }
            when (cal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.SUNDAY -> "周日"
                Calendar.MONDAY -> "周一"
                Calendar.TUESDAY -> "周二"
                Calendar.WEDNESDAY -> "周三"
                Calendar.THURSDAY -> "周四"
                Calendar.FRIDAY -> "周五"
                Calendar.SATURDAY -> "周六"
                else -> ""
            }
        } catch (e: Exception) {
            ""
        }
    }

    /** 清理城市名后缀，方便界面显示与自然语音播报（如 "南京市" -> "南京"） */
    fun cleanCityName(city: String): String {
        val trimmed = city.trim()
        return if (trimmed.length > 2 && trimmed.endsWith("市")) {
            trimmed.removeSuffix("市")
        } else {
            trimmed
        }
    }

    fun cleanAdminName(province: String): String {
        return province.trim()
    }

    /** 最高最低温取平均，作为当前温度的回退值 */
    private fun averageOf(high: String?, low: String?): Int? {
        val h = high?.filter { it.isDigit() || it == '-' }?.toIntOrNull()
        val l = low?.filter { it.isDigit() || it == '-' }?.toIntOrNull()
        return when {
            h != null && l != null -> (h + l) / 2
            h != null -> h
            l != null -> l
            else -> null
        }
    }

    /**
     * 天气文字转图标
     */
    fun iconOf(weather: String): String = when {
        weather.contains("雷") -> "⛈️"
        weather.contains("雪") -> "❄️"
        weather.contains("雨") -> "🌧️"
        weather.contains("雾") || weather.contains("霾") || weather.contains("尘") || weather.contains("沙") -> "🌫️"
        weather.contains("阴") -> "☁️"
        weather.contains("多云") -> "⛅"
        weather.contains("晴") -> "☀️"
        else -> "🌤️"
    }
}
