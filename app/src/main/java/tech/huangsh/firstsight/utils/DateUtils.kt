package tech.huangsh.firstsight.utils

import com.nlf.calendar.Lunar
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author huangshihai
 */
object DateUtils {

    /**
     * 获取格式化的时间
     */
    fun formatTime(date: Date): String {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        return timeFormat.format(date)
    }

    /**
     * 获取格式化的日期
     */
    fun formatDate(date: Date): String {
        val dateFormat = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault())
        return dateFormat.format(date)
    }

    /**
     * 获取星期
     */
    fun getWeekDay(date: Date): String {
        val calendar = Calendar.getInstance()
        calendar.time = date

        val weekday = when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> "星期日"
            Calendar.MONDAY -> "星期一"
            Calendar.TUESDAY -> "星期二"
            Calendar.WEDNESDAY -> "星期三"
            Calendar.THURSDAY -> "星期四"
            Calendar.FRIDAY -> "星期五"
            Calendar.SATURDAY -> "星期六"
            else -> ""
        }
        return weekday
    }

    /**
     * 获取农历日期
     *
     * 输出「农历冬月十七」这种口语格式。
     * 不带干支年（丙午年），一是老人日常不说，二是语音播报时更自然。
     */
    fun getLunar(date: Date): String {
        val calendar = Calendar.getInstance()
        calendar.time = date

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1 // Calendar 的月份从 0 开始，所以要 +1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val lunar = Lunar.fromYmd(year, month, day)

        val lunarMonth = lunar.monthInChinese // 农历月（中文，如 "正"、"冬"）
        val lunarDay = lunar.dayInChinese // 农历日（中文，如 "初一"、"十七"）

        return "农历${lunarMonth}月${lunarDay}"
    }

    /**
     * 获取适合朗读的时间
     *
     * 「08:26」直接念会很生硬，转成「上午8点26分」更自然。
     */
    fun formatTimeForSpeech(date: Date): String {
        val calendar = Calendar.getInstance().apply { time = date }
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val period = when {
            hour < 6 -> "凌晨"
            hour < 12 -> "上午"
            hour < 13 -> "中午"
            hour < 18 -> "下午"
            else -> "晚上"
        }
        // 12 小时制，0 点与 12 点都要显示为 12
        val displayHour = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }

        return if (minute == 0) {
            "$period${displayHour}点整"
        } else {
            "$period${displayHour}点${minute}分"
        }
    }

    /**
     * 整点报时用的时间文案
     *
     * 只报小时，不带分钟。
     */
    fun formatHourForSpeech(date: Date): String {
        val hour = Calendar.getInstance().apply { time = date }.get(Calendar.HOUR_OF_DAY)
        val period = when {
            hour < 6 -> "凌晨"
            hour < 12 -> "上午"
            hour < 13 -> "中午"
            hour < 18 -> "下午"
            else -> "晚上"
        }
        val displayHour = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        return "$period${displayHour}点"
    }
}