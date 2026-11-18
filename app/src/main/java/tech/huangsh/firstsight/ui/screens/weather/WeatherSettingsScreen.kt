package tech.huangsh.firstsight.ui.screens.weather

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import tech.huangsh.firstsight.ui.theme.FirstSightIcons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tech.huangsh.firstsight.R
import tech.huangsh.firstsight.data.model.DailyForecast
import tech.huangsh.firstsight.data.model.WeatherError
import tech.huangsh.firstsight.data.model.WeatherInfo
import tech.huangsh.firstsight.data.remote.CityCodeTable
import tech.huangsh.firstsight.ui.screens.components.CommonTopBar
import tech.huangsh.firstsight.ui.theme.FirstSightStyle
import tech.huangsh.firstsight.viewmodel.WeatherSettingsViewModel

/**
 * 天气设置页
 *
 * 包含三块内容：
 * 1. 城市选择与自动定位切换（支持全国各省市搜索与选择，或根据定位自动选择）；
 * 2. 当天实时天气详情卡片（点击可连同当前所选城市一起语音播报）；
 * 3. 未来一周（7天）天气预报列表。
 */
@Composable
fun WeatherSettingsScreen(
    onBack: () -> Unit,
    viewModel: WeatherSettingsViewModel
) {
    val weather by viewModel.weather.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val cityCode by viewModel.cityCode.collectAsState()

    var showCityPicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FirstSightStyle.pageGradient())
    ) {
        CommonTopBar(
            title = stringResource(R.string.weather_settings),
            onBack = onBack,
            actions = {
                IconButton(
                    onClick = { viewModel.refresh() },
                    enabled = !loading
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CitySelectorCard(
                cityName = weather?.city.orEmpty(),
                isAutoLocated = cityCode.isBlank(),
                loading = loading,
                onOpenPicker = { showCityPicker = true },
                onAutoLocate = { viewModel.selectCity("", "") }
            )

            CurrentWeatherCard(
                weatherInfo = weather,
                loading = loading,
                onClick = { viewModel.speakCurrentWeather() }
            )

            ForecastCard(
                forecast = weather?.forecast.orEmpty()
            )
        }
    }

    if (showCityPicker) {
        CityPickerDialog(
            currentCode = cityCode,
            onDismiss = { showCityPicker = false },
            onSelect = { province, city ->
                viewModel.selectCity(province, city)
                showCityPicker = false
            }
        )
    }
}

@Composable
private fun CitySelectorCard(
    cityName: String,
    isAutoLocated: Boolean,
    loading: Boolean,
    onOpenPicker: () -> Unit,
    onAutoLocate: () -> Unit
) {
    SettingsCard {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !loading) { onOpenPicker() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(FirstSightStyle.small)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isAutoLocated) FirstSightIcons.MyLocation else Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(26.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.weather_city),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(4.dp))
                    val modeText = if (isAutoLocated) {
                        if (cityName.isNotBlank()) {
                            stringResource(R.string.weather_city_auto_current, cityName)
                        } else {
                            stringResource(R.string.weather_city_auto_desc)
                        }
                    } else {
                        stringResource(R.string.weather_city_manual_current, cityName)
                    }
                    Text(
                        text = modeText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onOpenPicker,
                    enabled = !loading,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = stringResource(R.string.weather_select_city),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            if (!isAutoLocated) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onAutoLocate,
                    enabled = !loading,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = FirstSightIcons.MyLocation,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.weather_use_auto_location),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun CurrentWeatherCard(
    weatherInfo: WeatherInfo?,
    loading: Boolean,
    onClick: () -> Unit
) {
    val cityName = weatherInfo?.city.orEmpty()
    val weatherText = weatherInfo?.weather.orEmpty()
    val weatherIcon = weatherInfo?.weatherIcon.orEmpty()
    val temperature = weatherInfo?.temperature ?: 0
    val error = weatherInfo?.error

    SettingsCard(
        modifier = Modifier.clickable(enabled = weatherText.isNotBlank()) { onClick() }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(FirstSightStyle.small)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (weatherIcon.isNotBlank()) {
                        Text(text = weatherIcon, fontSize = 34.sp)
                    } else {
                        Icon(
                            imageVector = FirstSightIcons.WbSunny,
                            contentDescription = null,
                            modifier = Modifier.size(34.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = when {
                            loading && cityName.isBlank() -> stringResource(R.string.weather_loading)
                            cityName.isNotBlank() -> stringResource(R.string.weather_today_title, cityName)
                            else -> stringResource(R.string.weather_city_auto)
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = when {
                            weatherText.isNotBlank() -> "$weatherText  ${temperature}°C"
                            loading -> stringResource(R.string.weather_loading)
                            error == WeatherError.PARSE -> stringResource(R.string.weather_error_parse)
                            else -> stringResource(R.string.weather_error_network)
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (weatherText.isBlank() && !loading) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                }
            }

            if (weatherInfo != null && weatherText.isNotBlank()) {
                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (weatherInfo.high != null && weatherInfo.low != null) {
                        Text(
                            text = "最高 ${weatherInfo.high}°C / 最低 ${weatherInfo.low}°C",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (weatherInfo.humidity > 0) {
                        Text(
                            text = "湿度 ${weatherInfo.humidity}%",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (weatherInfo.windDirection.isNotBlank()) {
                        val windPowerSuffix = weatherInfo.windPower.takeIf { it.isNotBlank() }?.let { " ${it}级" }.orEmpty()
                        Text(
                            text = "${weatherInfo.windDirection}$windPowerSuffix",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ForecastCard(forecast: List<DailyForecast>) {
    SettingsCard {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.weather_forecast),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(12.dp))

            if (forecast.isEmpty()) {
                Text(
                    text = stringResource(R.string.weather_forecast_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                forecast.forEachIndexed { index, day ->
                    if (index > 0) {
                        Spacer(Modifier.height(10.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                        Spacer(Modifier.height(10.dp))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1.1f)) {
                            Text(
                                text = day.weekday.ifBlank { day.date },
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (day.weekday.isNotBlank() && day.date.isNotBlank()) {
                                Text(
                                    text = day.date,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Text(
                            text = "${day.weatherIcon} ${day.weather}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1.2f)
                        )

                        Text(
                            text = "${day.low}°C ~ ${day.high}°C",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CityPickerDialog(
    currentCode: String,
    onDismiss: () -> Unit,
    onSelect: (province: String, city: String) -> Unit
) {
    var keyword by remember { mutableStateOf("") }

    // 有关键词时跨省份做全量筛选，否则按省份分组展示
    val matched = remember(keyword) {
        val query = keyword.trim()
        if (query.isEmpty()) {
            emptyList()
        } else {
            CityCodeTable.selectable.filter {
                it.name.contains(query, ignoreCase = true) ||
                    it.province.contains(query, ignoreCase = true)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.weather_select_city)) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = keyword,
                    onValueChange = { keyword = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = stringResource(R.string.weather_search_hint),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium
                )

                Spacer(Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                ) {
                    // 自动定位始终排在最前，清空编码即恢复跟随定位
                    item {
                        CityRow(
                            name = stringResource(R.string.weather_city_auto),
                            subtitle = stringResource(R.string.weather_city_auto_desc),
                            selected = currentCode.isBlank(),
                            onClick = { onSelect("", "") }
                        )
                        HorizontalDivider()
                    }

                    if (keyword.trim().isEmpty()) {
                        CityCodeTable.groupedByProvince.forEach { (province, list) ->
                            item(key = "prov_$province") {
                                Text(
                                    text = province,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(
                                        start = 12.dp,
                                        top = 14.dp,
                                        bottom = 4.dp
                                    )
                                )
                            }
                            items(list, key = { it.code }) { city ->
                                CityRow(
                                    name = city.name,
                                    selected = currentCode == city.code,
                                    onClick = { onSelect(city.province, city.name) }
                                )
                            }
                        }
                    } else if (matched.isEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.weather_search_empty),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    } else {
                        items(matched, key = { it.code }) { city ->
                            CityRow(
                                name = city.name,
                                subtitle = city.province,
                                selected = currentCode == city.code,
                                onClick = { onSelect(city.province, city.name) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun CityRow(
    name: String,
    selected: Boolean,
    onClick: () -> Unit,
    subtitle: String = ""
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
            if (subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (selected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun SettingsCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
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
        Column(modifier = Modifier.padding(18.dp), content = content)
    }
}
