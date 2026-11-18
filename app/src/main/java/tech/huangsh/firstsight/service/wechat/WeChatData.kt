package tech.huangsh.firstsight.service.wechat

object WeChatData {
    var value: String = ""
    fun updateValue(newValue: String) {
        value = newValue
    }

    var index: Int = 0
    fun updateIndex(newValue: Int) {
        index = newValue
    }

    var video: Boolean = true
    fun updateVideo(newValue: Boolean) {
        video = newValue
    }

    /**
     * 流程启动回调
     *
     * 由无障碍服务注册。设置 index=1 后必须主动通知服务开始推进，
     * 否则只能等微信窗口事件，而窗口事件在流程中途不会再来。
     */
    var onFlowStarted: (() -> Unit)? = null

    /**
     * 流程中途结束的回调
     *
     * 无障碍服务无法直接调用应用层的语音播报，由上层注册回调后按原因播报。
     */
    var onFlowAborted: ((FlowAbortReason) -> Unit)? = null

    /**
     * 自动接听开关与白名单（由 MainViewModel / SettingsRepository 实时同步）
     */
    @Volatile
    var autoAnswerEnabled: Boolean = false

    @Volatile
    var autoAnswerContactsOnly: Boolean = true

    @Volatile
    var autoSpeakerphone: Boolean = false

    @Volatile
    var returnHomeAfterWechatCall: Boolean = false

    @Volatile
    var returnHomeAfterPhoneCall: Boolean = false

    @Volatile
    var allowedCallers: Set<String> = emptySet()

    /**
     * 自动接听成功后的回调（参数为匹配到的联系人姓名，未匹配时为 null）
     */
    var onAutoAnswered: ((String?) -> Unit)? = null

    fun startFlow() {
        index = 1
        onFlowStarted?.invoke()
    }

    fun findText(options: Boolean): String {
        return if (video || !options) {
            "视频通话"
        } else {
            "语音通话"
        }
    }
}

/**
 * 自动流程中途结束的原因
 */
enum class FlowAbortReason {
    /** 用户碰了屏幕，主动接管 */
    USER_TOUCH,

    /** 某一步迟迟等不到目标界面 */
    TIMEOUT,

    /** 执行步骤时抛出异常 */
    ERROR,
}
