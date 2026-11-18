package tech.huangsh.firstsight.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * 电量状态
 */
data class BatteryStatus(
    val level: Int = 0,
    val isCharging: Boolean = false
)

/**
 * 读取当前电量与充电状态
 */
fun readBatteryStatus(context: Context): BatteryStatus {
    val manager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        ?: return BatteryStatus()

    val level = manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        .coerceIn(0, 100)
    val isCharging = manager.isCharging
    return BatteryStatus(level = level, isCharging = isCharging)
}

/**
 * 从 ACTION_BATTERY_CHANGED 广播解析电量与充电状态
 */
fun parseBatteryIntent(intent: Intent, fallback: BatteryStatus = BatteryStatus()): BatteryStatus {
    val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
    val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
    val percent = if (level >= 0 && scale > 0) level * 100 / scale else -1
    val charging = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1).let {
        it == BatteryManager.BATTERY_STATUS_CHARGING ||
            it == BatteryManager.BATTERY_STATUS_FULL
    }
    return BatteryStatus(
        level = if (percent >= 0) percent else fallback.level,
        isCharging = charging
    )
}

/**
 * 订阅电量变化
 *
 * 用 ACTION_BATTERY_CHANGED 粘性广播，注册时立即拿到当前值，
 * 之后电量或充电状态变化会自动更新。
 */
@Composable
fun rememberBatteryStatus(): State<BatteryStatus> {
    val context = LocalContext.current
    val status = remember { mutableStateOf(readBatteryStatus(context)) }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                intent ?: return
                status.value = parseBatteryIntent(intent, status.value)
            }
        }
        context.registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        onDispose { context.unregisterReceiver(receiver) }
    }

    return status
}
