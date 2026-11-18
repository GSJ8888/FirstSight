package tech.huangsh.firstsight.data.remote

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.CancellationSignal
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import tech.huangsh.firstsight.data.model.WeatherError
import tech.huangsh.firstsight.data.model.WeatherInfo
import tech.huangsh.firstsight.data.repository.SettingsRepository
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * 天气服务
 *
 * 采用腾讯天气公开 HTTPS 接口（wis.qq.com）：
 * 1. 若用户在天气设置中手动指定了城市，直接查询该城市；
 * 2. 若设为“自动定位”：
 *    - 优先读取设备定位（仅接受中国境内有效坐标），通过系统 Geocoder 或本地 CityCodeTable 换算省市；
 *    - 若未授予位置权限、室内无定位信号或处于模拟器默认海外坐标，自动通过公网 IP 定位接口（r.inews.qq.com）反查所在省市；
 *    - 均失败时退回默认城市或上一次成功的天气缓存。
 */
@Singleton
class WeatherService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository
) {

    private val cnWeatherApi: CnWeatherApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://wis.qq.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(CnWeatherApiService::class.java)
    }

    /** 上一次取到的真实天气，网络失败时用它兜底 */
    @Volatile
    private var lastSuccessful: WeatherInfo? = null

    /** 全局共享的最新天气状态，供首页与天气设置页同步监听 */
    private val _currentWeather = MutableStateFlow<WeatherInfo?>(null)
    val currentWeather: StateFlow<WeatherInfo?> = _currentWeather.asStateFlow()

    /**
     * 获取天气信息并同步更新 [currentWeather]
     */
    suspend fun getWeatherInfo(): WeatherInfo {
        val manualProvince = settingsRepository.weatherProvince.first().trim()
        val manualCity = settingsRepository.weatherCity.first().trim()

        // 1. 用户手动指定了城市
        if (manualProvince.isNotBlank() && manualCity.isNotBlank()) {
            val manualResult = fetchByProvinceCity(
                province = manualProvince,
                city = manualCity,
                isAutoLocated = false
            )
            if (manualResult != null) {
                lastSuccessful = manualResult
                _currentWeather.value = manualResult
                return manualResult
            }
            val fallback = lastSuccessful?.takeIf {
                it.city == CnWeatherMapper.cleanCityName(manualCity)
            } ?: WeatherInfo(
                province = manualProvince,
                city = CnWeatherMapper.cleanCityName(manualCity),
                isAutoLocated = false,
                error = WeatherError.NETWORK
            )
            _currentWeather.value = fallback
            return fallback
        }

        // 2. 自动定位模式：先尝试设备定位
        val location = getLocation()
        if (location != null) {
            // 2a. 优先尝试系统 Geocoder 反查精确省市（覆盖全国所有城市）
            reverseGeocode(location.latitude, location.longitude)?.let { (province, city) ->
                fetchByProvinceCity(province, city, isAutoLocated = true)?.let { info ->
                    lastSuccessful = info
                    _currentWeather.value = info
                    return info
                }
            }

            // 2b. 回退到本地最近邻城市表
            val nearest = CityCodeTable.nearest(location.latitude, location.longitude)
            fetchByProvinceCity(nearest.province, nearest.name, isAutoLocated = true)?.let { info ->
                lastSuccessful = info
                _currentWeather.value = info
                return info
            }
        } else {
            Log.w(TAG, "device location unavailable, falling back to IP location")
        }

        // 3. 设备定位不可用时，使用公网 IP 反查所在省市
        fetchIpProvinceCity()?.let { (province, city) ->
            fetchByProvinceCity(province, city, isAutoLocated = true)?.let { info ->
                lastSuccessful = info
                _currentWeather.value = info
                return info
            }
        }

        // 4. 最终兜底：默认城市或上次成功缓存
        val fallbackCity = CityCodeTable.default
        fetchByProvinceCity(fallbackCity.province, fallbackCity.name, isAutoLocated = true)?.let { info ->
            lastSuccessful = info
            _currentWeather.value = info
            return info
        }

        val finalInfo = lastSuccessful ?: WeatherInfo(error = WeatherError.NETWORK)
        _currentWeather.value = finalInfo
        return finalInfo
    }

    private suspend fun fetchByProvinceCity(
        province: String,
        city: String,
        isAutoLocated: Boolean
    ): WeatherInfo? {
        return try {
            val response = cnWeatherApi.getWeather(province = province, city = city)
            CnWeatherMapper.toWeatherInfo(
                response = response,
                province = province,
                city = city,
                isAutoLocated = isAutoLocated
            )?.takeIf { it.weather.isNotBlank() }
        } catch (e: Exception) {
            Log.e(TAG, "fetch weather failed for $province $city", e)
            null
        }
    }

    private suspend fun fetchIpProvinceCity(): Pair<String, String>? {
        return try {
            val ipResp = cnWeatherApi.getIpLocation()
            val province = ipResp.province?.trim().orEmpty()
            val city = ipResp.city?.trim().orEmpty()
            if (province.isNotBlank() && city.isNotBlank()) {
                province to city
            } else {
                null
            }
        } catch (e: Exception) {
            Log.w(TAG, "IP location lookup failed", e)
            null
        }
    }

    @Suppress("DEPRECATION")
    private suspend fun reverseGeocode(latitude: Double, longitude: Double): Pair<String, String>? {
        if (!Geocoder.isPresent()) return null
        return withTimeoutOrNull(GEOCODE_TIMEOUT_MS) {
            withContext(Dispatchers.IO) {
                try {
                    val geocoder = Geocoder(context, Locale.CHINA)
                    val address = geocoder.getFromLocation(latitude, longitude, 1)?.firstOrNull()
                    val province = address?.adminArea?.trim().orEmpty()
                    val city = (address?.locality ?: address?.subAdminArea)?.trim().orEmpty()
                    if (province.isNotBlank() && city.isNotBlank()) {
                        province to city
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "reverse geocode failed", e)
                    null
                }
            }
        }
    }

    /**
     * 取设备当前中国境内坐标
     */
    @SuppressLint("MissingPermission")
    private suspend fun getLocation(): Location? {
        if (!hasLocationPermission()) return null

        val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return null

        // 快速路径：缓存位置足够新且在国内就直接用
        bestLastKnown(manager, onlyFresh = true)?.let { return it }

        // 缓存过期或为空，主动请求一次
        val fresh = requestSingleLocation(manager)
        if (fresh != null) return fresh

        // 请求失败时退而求其次，用不一定新鲜的国内缓存位置
        return bestLastKnown(manager, onlyFresh = false)
    }

    /**
     * 从所有可用 provider 里挑最新的国内缓存位置
     */
    @SuppressLint("MissingPermission")
    private fun bestLastKnown(manager: LocationManager, onlyFresh: Boolean): Location? {
        val providers = try {
            manager.getProviders(true)
        } catch (e: Exception) {
            Log.e(TAG, "query providers failed", e)
            return null
        }

        val candidates = providers.mapNotNull { provider ->
            try {
                manager.getLastKnownLocation(provider)
            } catch (e: Exception) {
                Log.w(TAG, "last known location from $provider failed", e)
                null
            }
        }

        val now = System.currentTimeMillis()
        return candidates
            .filter { CityCodeTable.isInChina(it.latitude, it.longitude) }
            .filter { !onlyFresh || now - it.time <= FRESH_THRESHOLD_MS }
            .maxByOrNull { it.time }
    }

    /**
     * 主动请求一次定位
     */
    @SuppressLint("MissingPermission")
    private suspend fun requestSingleLocation(manager: LocationManager): Location? {
        val providers = pickProviders(manager)
        for (provider in providers) {
            val location = requestFromProvider(manager, provider)
            if (location != null && CityCodeTable.isInChina(location.latitude, location.longitude)) {
                return location
            }
        }
        return null
    }

    @SuppressLint("MissingPermission")
    private suspend fun requestFromProvider(manager: LocationManager, provider: String): Location? {
        return withTimeoutOrNull(LOCATE_TIMEOUT_MS) {
            suspendCancellableCoroutine { cont ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val signal = CancellationSignal()
                    cont.invokeOnCancellation { signal.cancel() }
                    try {
                        manager.getCurrentLocation(
                            provider,
                            signal,
                            context.mainExecutor
                        ) { location ->
                            if (cont.isActive) cont.resume(location)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "getCurrentLocation from $provider failed", e)
                        if (cont.isActive) cont.resume(null)
                    }
                } else {
                    val listener = object : LocationListener {
                        override fun onLocationChanged(location: Location) {
                            if (cont.isActive) cont.resume(location)
                        }

                        @Deprecated("Deprecated in Java")
                        override fun onStatusChanged(provider: String?, status: Int, extras: android.os.Bundle?) = Unit

                        override fun onProviderEnabled(provider: String) = Unit

                        override fun onProviderDisabled(provider: String) = Unit
                    }
                    try {
                        manager.requestSingleUpdate(provider, listener, Looper.getMainLooper())
                        cont.invokeOnCancellation { manager.removeUpdates(listener) }
                    } catch (e: Exception) {
                        Log.e(TAG, "requestSingleUpdate from $provider failed", e)
                        if (cont.isActive) cont.resume(null)
                    }
                }
            }
        }
    }

    /**
     * 选择定位 provider 优先级列表
     */
    private fun pickProviders(manager: LocationManager): List<String> {
        val enabled = try {
            manager.getProviders(true)
        } catch (e: Exception) {
            return emptyList()
        }

        val preferred = buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(LocationManager.FUSED_PROVIDER)
            }
            add(LocationManager.NETWORK_PROVIDER)
            add(LocationManager.GPS_PROVIDER)
        }

        return preferred.filter { it in enabled }.ifEmpty { enabled }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * 监听天气变化（每小时更新一次）
     */
    fun observeWeatherChanges(): Flow<WeatherInfo> = flow {
        while (true) {
            emit(getWeatherInfo())
            delay(60 * 60 * 1000) // 1小时更新一次
        }
    }

    private companion object {
        const val TAG = "WeatherService"

        /** 缓存位置超过 30 分钟就认为过期，需要重新请求 */
        const val FRESH_THRESHOLD_MS = 30 * 60 * 1000L

        /** 单个定位 provider 的最长等待时间 */
        const val LOCATE_TIMEOUT_MS = 4_000L

        /** Geocoder 逆地理编码的最长等待时间 */
        const val GEOCODE_TIMEOUT_MS = 2_000L
    }
}
