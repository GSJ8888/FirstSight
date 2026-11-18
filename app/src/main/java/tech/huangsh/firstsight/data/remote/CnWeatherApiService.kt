package tech.huangsh.firstsight.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 国内天气与 IP 定位接口（腾讯天气公开 HTTPS 接口）
 */
interface CnWeatherApiService {

    /**
     * 按省市名称查询实时天气与 24 小时（含未来 7 天）预报
     */
    @GET("weather/common")
    suspend fun getWeather(
        @Query("source") source: String = "pc",
        @Query("weather_type") weatherType: String = "observe|forecast_24h",
        @Query("province") province: String,
        @Query("city") city: String
    ): CnWeatherResponse

    /**
     * 当设备无定位权限、室内无法定位或处于模拟器海外坐标时，按公网 IP 反查所在省市
     */
    @GET("https://r.inews.qq.com/api/ip2city")
    suspend fun getIpLocation(): CnIpLocationResponse
}

data class CnWeatherResponse(
    val status: Int? = null,
    val message: String? = null,
    val data: CnWeatherData? = null
)

data class CnWeatherData(
    val observe: CnObserve? = null,
    val forecast_24h: Map<String, CnForecastDay>? = null
)

data class CnObserve(
    val degree: String? = null,
    val humidity: String? = null,
    val weather: String? = null,
    val weather_short: String? = null,
    val wind_direction_name: String? = null,
    val wind_power: String? = null,
    val update_time: String? = null
)

data class CnForecastDay(
    val day_weather: String? = null,
    val day_weather_short: String? = null,
    val night_weather: String? = null,
    val night_weather_short: String? = null,
    val day_wind_direction: String? = null,
    val day_wind_power: String? = null,
    val max_degree: String? = null,
    val min_degree: String? = null,
    val time: String? = null
)

data class CnIpLocationResponse(
    val ret: Int? = null,
    val country: String? = null,
    val province: String? = null,
    val city: String? = null,
    val district: String? = null
)
