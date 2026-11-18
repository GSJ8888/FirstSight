package tech.huangsh.firstsight.utils

import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import android.telephony.TelephonyManager

/**
 * 网络状态检测工具
 */
object NetworkUtils {

    /**
     * 判断当前设备是否已连接可用网络
     */
    fun isNetworkAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    /**
     * 注册默认网络变化回调
     */
    fun registerNetworkCallback(
        context: Context,
        onAvailableChange: (Boolean) -> Unit
    ): ConnectivityManager.NetworkCallback? {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return null

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                onAvailableChange(isNetworkAvailable(context))
            }

            override fun onLost(network: Network) {
                onAvailableChange(isNetworkAvailable(context))
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                val available = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                onAvailableChange(available)
            }
        }

        return try {
            cm.registerDefaultNetworkCallback(callback)
            callback
        } catch (e: Exception) {
            null
        }
    }

    fun unregisterNetworkCallback(
        context: Context,
        callback: ConnectivityManager.NetworkCallback?
    ) {
        if (callback == null) return
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return
        runCatching { cm.unregisterNetworkCallback(callback) }
    }

    /**
     * 判断设备是否已插入可用 SIM 卡（兼容双卡槽）
     */
    @Suppress("DEPRECATION")
    fun isSimReady(context: Context): Boolean {
        if (!context.packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)) {
            return false
        }
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            ?: return false
        if (tm.simState == TelephonyManager.SIM_STATE_READY) {
            return true
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val slotCount = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                tm.activeModemCount
            } else {
                tm.phoneCount
            }
            for (slot in 0 until slotCount) {
                if (runCatching { tm.getSimState(slot) }.getOrNull() == TelephonyManager.SIM_STATE_READY) {
                    return true
                }
            }
        }
        return false
    }
}
