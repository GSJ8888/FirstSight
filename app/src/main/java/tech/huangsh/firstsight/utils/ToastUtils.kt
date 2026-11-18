package tech.huangsh.firstsight.utils

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.annotation.StringRes

/**
 * 全局 Toast 工具类
 *
 * 每次弹出新 Toast 时先取消上一条正在显示的 Toast，避免连续点击时系统排队等待、
 * 从而立即展示最新提示并重新计算显示时长。
 */
object ToastUtils {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var currentToast: Toast? = null
    private var pendingShow: Runnable? = null

    fun show(context: Context, @StringRes resId: Int, duration: Int = Toast.LENGTH_SHORT) {
        show(context, context.getString(resId), duration)
    }

    fun show(context: Context, message: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
        val appContext = context.applicationContext
        val showAction = Runnable {
            currentToast?.cancel()
            currentToast = Toast.makeText(appContext, message, duration).also {
                it.show()
            }
        }

        if (Looper.myLooper() == Looper.getMainLooper()) {
            pendingShow?.let { mainHandler.removeCallbacks(it) }
            pendingShow = null
            showAction.run()
        } else {
            pendingShow?.let { mainHandler.removeCallbacks(it) }
            pendingShow = showAction
            mainHandler.post(showAction)
        }
    }
}
