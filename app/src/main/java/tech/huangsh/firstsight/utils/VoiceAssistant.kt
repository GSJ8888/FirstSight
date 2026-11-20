package tech.huangsh.firstsight.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * 语音播报，基于系统 TTS
 *
 * 语速和音量在每次 speak 时按设置传入，而不是初始化时设一次，
 * 这样用户在设置页拖动滑块后立即生效，不需要重建实例。
 *
 * 播报时申请瞬时音频焦点并允许压低其他音频（DUCK），
 * 避免打断用户正在听的音乐或视频，也避免被系统判定为抢占音频。
 */
class VoiceAssistant(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null

    private val audioManager: AudioManager?
        get() = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    /** API 26+ 的焦点请求对象，复用同一个实例避免重复分配 */
    private var focusRequest: AudioFocusRequest? = null

    private var hasAudioFocus = false

    private val mainHandler = Handler(Looper.getMainLooper())
    private val lock = Any()

    private data class PendingUtterance(
        val text: String,
        val volume: Float
    )

    @Volatile
    private var pendingUtterance: PendingUtterance? = null

    @Volatile
    private var lastSpokenUtterance: PendingUtterance? = null

    @Volatile
    private var isInitializing = false

    private var rebindAttempts = 0

    /**
     * 焦点变化回调
     *
     * 播报是短提示，被其他应用抢占时直接停止即可，不做延迟恢复。
     */
    private val focusChangeListener = AudioManager.OnAudioFocusChangeListener { change ->
        when (change) {
            AudioManager.AUDIOFOCUS_LOSS,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                Log.d(TAG, "audio focus lost: $change")
                hasAudioFocus = false
                stopInternal()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                // 允许压低，保持播报继续，TTS 音量已由调用方传入
                Log.d(TAG, "audio focus ducked")
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                hasAudioFocus = true
            }
        }
    }

    private val ttsAudioAttributes: AudioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
        .build()

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    /** 中文语音是否可用，不可用时上层提示用户安装语音数据 */
    private val _languageAvailable = MutableStateFlow(true)
    val languageAvailable: StateFlow<Boolean> = _languageAvailable.asStateFlow()

    init {
        initTts()
    }

    private fun initTts() {
        synchronized(lock) {
            if (isInitializing) return
            isInitializing = true
            _isReady.value = false
        }
        val createAction = {
            synchronized(lock) {
                runCatching {
                    tts?.shutdown()
                }
                tts = TextToSpeech(context.applicationContext, this)
            }
        }
        if (Looper.myLooper() == Looper.getMainLooper()) {
            createAction()
        } else {
            mainHandler.post(createAction)
        }
    }

    private fun rebindTts(replayUtterance: PendingUtterance? = null) {
        synchronized(lock) {
            if (replayUtterance != null) {
                pendingUtterance = replayUtterance
            }
            if (isInitializing) return
            _isReady.value = false
        }
        initTts()
    }

    override fun onInit(status: Int) {
        val toReplay: PendingUtterance?
        synchronized(lock) {
            isInitializing = false
            if (status != TextToSpeech.SUCCESS) {
                Log.e(TAG, "TTS init failed, status=$status")
                _isReady.value = false
                pendingUtterance = null
                return
            }

            // 让系统把播报识别为无障碍语音，走辅助音频通道
            tts?.setAudioAttributes(ttsAudioAttributes)

            val result = tts?.setLanguage(Locale.CHINA)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "Chinese language not supported, result=$result")
                _languageAvailable.value = false
            } else {
                _languageAvailable.value = true
            }

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    rebindAttempts = 0
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                    lastSpokenUtterance = null
                    abandonFocus()
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    handleTtsError(errorCode = TextToSpeech.ERROR)
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    handleTtsError(errorCode = errorCode)
                }
            })

            _isReady.value = true
            toReplay = pendingUtterance
            pendingUtterance = null
        }

        if (toReplay != null) {
            speak(toReplay.text, toReplay.volume)
        }
    }

    private fun handleTtsError(errorCode: Int) {
        Log.e(TAG, "speak error, code=$errorCode")
        _isSpeaking.value = false
        abandonFocus()

        val shouldRebind = errorCode == TextToSpeech.ERROR_SERVICE ||
            errorCode == TextToSpeech.ERROR_NOT_INSTALLED_YET ||
            errorCode == TextToSpeech.ERROR
        if (shouldRebind && rebindAttempts < MAX_REBIND_RETRIES) {
            rebindAttempts++
            val replay = lastSpokenUtterance
            lastSpokenUtterance = null
            rebindTts(replay)
        }
    }

    /**
     * 朗读文本
     *
     * @param text 要朗读的内容
     * @param volume 音量，0.0~1.0
     */
    fun speak(text: String, volume: Float = 1.0f) {
        if (text.isBlank()) return

        val engine: TextToSpeech = synchronized(lock) {
            val currentEngine = tts
            if (currentEngine == null || !_isReady.value) {
                pendingUtterance = PendingUtterance(text, volume)
                if (currentEngine == null && !isInitializing) {
                    initTts()
                }
                return
            }
            currentEngine
        }

        // 先拿焦点再打断上一条：中断会触发 onStop 回调，反过来释放刚申请的焦点
        requestFocus()

        // 新的播报打断旧的，避免连续点击时排队念一长串
        engine.stop()

        val params = Bundle().apply {
            putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, volume.coerceIn(0f, 1f))
        }
        val utterance = PendingUtterance(text, volume)
        lastSpokenUtterance = utterance

        val result = engine.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            params,
            System.currentTimeMillis().toString()
        )
        if (result != TextToSpeech.SUCCESS) {
            Log.w(TAG, "engine.speak returned $result, attempting TTS rebind")
            _isSpeaking.value = false
            abandonFocus()
            if (rebindAttempts < MAX_REBIND_RETRIES) {
                rebindAttempts++
                rebindTts(utterance)
            }
        }
    }

    fun stop() {
        synchronized(lock) {
            pendingUtterance = null
            lastSpokenUtterance = null
        }
        stopInternal()
        abandonFocus()
    }

    fun shutdown() {
        stop()
        synchronized(lock) {
            isInitializing = false
            tts?.shutdown()
            tts = null
            _isReady.value = false
        }
    }

    private fun stopInternal() {
        tts?.stop()
        _isSpeaking.value = false
    }

    /**
     * 申请瞬时焦点，允许系统压低其他音频
     *
     * 焦点被拒绝时仍然播报：呼叫提示属于无障碍信息，不应因其他应用占用音频而静默丢失。
     */
    private fun requestFocus() {
        if (hasAudioFocus) return
        val manager = audioManager ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val request = focusRequest ?: AudioFocusRequest
                .Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                .setAudioAttributes(ttsAudioAttributes)
                .setOnAudioFocusChangeListener(focusChangeListener, mainHandler)
                .setWillPauseWhenDucked(false)
                .build()
                .also { focusRequest = it }

            val result = manager.requestAudioFocus(request)
            hasAudioFocus = result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            @Suppress("DEPRECATION")
            val result = manager.requestAudioFocus(
                focusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
            )
            hasAudioFocus = result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }

        if (!hasAudioFocus) {
            Log.d(TAG, "audio focus denied, speaking anyway")
        }
    }

    private fun abandonFocus() {
        if (!hasAudioFocus) return
        hasAudioFocus = false
        val manager = audioManager ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest?.let { manager.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            manager.abandonAudioFocus(focusChangeListener)
        }
    }

    private companion object {
        const val TAG = "VoiceAssistant"
        const val MAX_REBIND_RETRIES = 2
    }
}
