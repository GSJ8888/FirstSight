package tech.huangsh.firstsight.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import tech.huangsh.firstsight.R

/**
 * 桌面启动器工具类
 * 处理默认桌面设置
 */
object LauncherUtils {

    /**
     * 检查当前应用是否为默认桌面
     */
    fun isDefaultLauncher(context: Context): Boolean {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
        }
        
        val resolveInfo = context.packageManager.resolveActivity(
            intent, 
            PackageManager.MATCH_DEFAULT_ONLY
        )
        
        return resolveInfo?.activityInfo?.packageName == context.packageName
    }

    /**
     * 打开系统「默认桌面」设置页，由用户在那里选择桌面应用
     *
     * 不能用 CATEGORY_HOME 的 Intent：本应用自身就是桌面，Home Intent 会被自己接收，
     * 表现为点击后什么都没发生、直接留在桌面。必须跳系统设置页。
     */
    fun openDefaultAppSettings(context: Context) {
        // 标准入口，Android 5.0+ 专门用于切换默认桌面
        val candidates = listOf(
            Intent(Settings.ACTION_HOME_SETTINGS),
            Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS),
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
            }
        )

        for (intent in candidates) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (startSafely(context, intent)) return
        }

        // 部分国产 ROM 的入口比较特殊，最后再试一次
        if (isMiuiSystem() && tryOpenMiuiDefaultAppSettings(context)) return

        ToastUtils.show(context, R.string.launcher_utils_error, Toast.LENGTH_LONG)
    }

    private fun startSafely(context: Context, intent: Intent): Boolean {
        return try {
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 尝试打开小米系统的默认应用设置
     */
    private fun tryOpenMiuiDefaultAppSettings(context: Context): Boolean {
        return try {
            // 方法1：尝试打开小米的默认应用设置
            val miuiIntent = Intent().apply {
                action = "miui.intent.action.APP_PERM"
                putExtra("extra_pkgname", context.packageName)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            
            if (isIntentAvailable(context, miuiIntent)) {
                context.startActivity(miuiIntent)
                ToastUtils.show(context, "请在\"默认启动\"中取消设置", Toast.LENGTH_LONG)
                return true
            }
            
            // 方法2：尝试打开小米的应用管理
            val miuiAppManageIntent = Intent().apply {
                action = "miui.intent.action.OP_AUTO_START"
                putExtra("package_name", context.packageName)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            
            if (isIntentAvailable(context, miuiAppManageIntent)) {
                context.startActivity(miuiAppManageIntent)
                return true
            }
            
            false
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 检查是否是小米MIUI系统
     */
    private fun isMiuiSystem(): Boolean {
        return try {
            val prop = Class.forName("android.os.SystemProperties")
            val method = prop.getMethod("get", String::class.java)
            val miui = method.invoke(null, "ro.miui.ui.version.name") as String?
            !miui.isNullOrEmpty()
        } catch (e: Exception) {
            // 备用检查方法
            android.os.Build.MANUFACTURER.equals("xiaomi", ignoreCase = true) ||
            android.os.Build.BRAND.equals("xiaomi", ignoreCase = true)
        }
    }

    /**
     * 检查Intent是否可用
     */
    private fun isIntentAvailable(context: Context, intent: Intent): Boolean {
        return try {
            val resolveInfo = context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
            resolveInfo != null
        } catch (e: Exception) {
            false
        }
    }
}
