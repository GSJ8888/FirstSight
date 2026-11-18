package tech.huangsh.firstsight.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import tech.huangsh.firstsight.data.model.Settings
import tech.huangsh.firstsight.ui.screens.home.HomeScreen
import tech.huangsh.firstsight.ui.theme.FirstSightTheme
import tech.huangsh.firstsight.utils.LauncherUtils
import tech.huangsh.firstsight.viewmodel.MainViewModel
import tech.huangsh.firstsight.viewmodel.SettingsViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemBars()

        // 检查并更新默认启动器状态
        updateLauncherStatus()

        setContent {
            val settings by settingsViewModel.settings.collectAsState(initial = Settings())
            val onboardingCompleted by settingsViewModel.onboardingCompleted.collectAsState()

            FirstSightTheme(
                themeMode = settings.themeMode
            ) {
                // 引导未完成时占位等待引导页覆盖，避免先闪一下桌面
                if (onboardingCompleted == true) {
                    HomeScreen(viewModel)
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        // 从设置页或系统界面返回后，状态栏可能被重新显示，再次隐藏
        hideSystemBars()
        // 每次恢复时检查启动器状态
        updateLauncherStatus()
        // 用户可能刚从无障碍设置页或引导页返回，刷新状态
        viewModel.refreshAccessibilityStatus()
        viewModel.refreshWeatherIfNeeded()
        // 若引导尚未完成（例如上次在引导页按返回键退出后再回来），确保重新拉起引导页
        lifecycleScope.launch {
            settingsViewModel.refreshOnboardingCompleted()
            if (!settingsViewModel.isOnboardingCompleted()) {
                val intent = Intent(this@MainActivity, OnboardingActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                }
                startActivity(intent)
            }
        }
    }

    /**
     * 隐藏系统状态栏和导航栏
     *
     * 桌面场景下不需要系统栏，隐藏后由应用自己显示电量和时间。
     * 用 immersiveSticky 让用户从边缘滑动时临时唤出，随后自动隐藏。
     */
    private fun hideSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
    
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // 处理Home键和返回键，确保启动器行为正确
        return when (keyCode) {
            KeyEvent.KEYCODE_HOME -> {
                // 如果是默认启动器，不做任何处理，保持在当前界面
                if (LauncherUtils.isDefaultLauncher(this)) {
                    true
                } else {
                    super.onKeyDown(keyCode, event)
                }
            }
            KeyEvent.KEYCODE_BACK -> {
                // 如果是默认启动器，返回键也不退出应用
                if (LauncherUtils.isDefaultLauncher(this)) {
                    true
                } else {
                    super.onKeyDown(keyCode, event)
                }
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }
    
    /**
     * 更新启动器状态
     */
    private fun updateLauncherStatus() {
        lifecycleScope.launch {
            settingsViewModel.refreshDefaultLauncherStatus()
        }
    }
}
