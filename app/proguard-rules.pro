# FirstSight Release ProGuard / R8 规则

# 保持 Retrofit 与 Gson 模型（网络响应解析依赖反射）
-keep class tech.huangsh.firstsight.data.remote.** { *; }
-keep class tech.huangsh.firstsight.data.model.** { *; }
-keepattributes Signature
-keepattributes *Annotation*

# 保持无障碍服务类（包名需固定为 com.google.android.accessibility.selecttospeak）
-keep class com.google.android.accessibility.selecttospeak.SelectToSpeakService { *; }

# 保持微信自动化枚举与状态
-keep class tech.huangsh.firstsight.service.wechat.** { *; }

# 保持小米系统属性反射调用
-keepclassmembers class android.os.SystemProperties {
    public static java.lang.String get(java.lang.String);
}

# 农历库
-keep class com.nlf.calendar.** { *; }
-dontwarn com.nlf.calendar.**

# XXPermissions
-keep class com.hjq.permissions.** { *; }
-dontwarn com.hjq.permissions.**
