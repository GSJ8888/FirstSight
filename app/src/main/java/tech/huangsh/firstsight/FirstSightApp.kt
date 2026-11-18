package tech.huangsh.firstsight

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import coil.ImageLoader
import coil.ImageLoaderFactory
import tech.huangsh.firstsight.utils.PerformanceUtils

/**
 * 一见应用类
 */
@HiltAndroidApp
class FirstSightApp : Application(), ImageLoaderFactory {
    
    private lateinit var imageLoader: ImageLoader
    
    override fun onCreate() {
        super.onCreate()
        
        // 初始化优化的图片加载器
        imageLoader = PerformanceUtils.getOptimizedImageLoader(this)
        
        // 预加载常用资源
        preloadResources()
    }
    
    override fun newImageLoader(): ImageLoader {
        return imageLoader
    }
    
    /**
     * 预加载常用资源
     */
    private fun preloadResources() {
        // 在这里可以预加载一些常用资源
        // 例如：默认图标、占位图等
    }
}