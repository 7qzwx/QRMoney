package qzwx.app.module_ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.systemuicontroller.rememberSystemUiController

private val lightScheme = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,

    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,

    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = onTertiaryContainerLight,

    error = errorLight,
    onError = onErrorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,

    background = backgroundLight,
    onBackground = onBackgroundLight,

    surface = surfaceLight,
    onSurface = onSurfaceLight,

    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,

    outline = outlineLight,
    outlineVariant = outlineVariantLight,

    scrim = Color(0xFF000000),

    inverseSurface = inverseSurfaceLight,
    inverseOnSurface = inverseOnSurfaceLight,
    inversePrimary = inversePrimaryLight,

    // ===== 关键补齐（统一层级）=====
    surfaceDim = Color(0xFFE6EBF1),
    surfaceBright = Color(0xFFFFFFFF),

    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF8FAFD),
    surfaceContainer = Color(0xFFF1F5F9),
    surfaceContainerHigh = Color(0xFFEAF0F6),
    surfaceContainerHighest = Color(0xFFE3E9EF),
)

private val darkScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,

    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,

    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,

    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,

    background = backgroundDark,
    onBackground = onBackgroundDark,

    surface = surfaceDark,
    onSurface = onSurfaceDark,

    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,

    outline = outlineDark,
    outlineVariant = outlineVariantDark,

    scrim = Color(0xFF000000),

    inverseSurface = inverseSurfaceDark,
    inverseOnSurface = inverseOnSurfaceDark,
    inversePrimary = inversePrimaryDark,

    // ===== 关键补齐（核心层级）=====
    surfaceDim = Color(0xFF101720),
    surfaceBright = Color(0xFF2C3540),

    surfaceContainerLowest = Color(0xFF0F141B),
    surfaceContainerLow = Color(0xFF1B232D),
    surfaceContainer = Color(0xFF222A34), // = surface
    surfaceContainerHigh = Color(0xFF2A333E),
    surfaceContainerHighest = Color(0xFF313B47),
)


@Composable
fun QAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // 防止 Android 12+ 默认使用系统壁纸动态取色
    content: @Composable() () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

//        darkTheme -> darkScheme   亮色暗色模式自动切换
//        else -> lightScheme   //亮色暗色模式自动切换
        darkTheme -> darkScheme
        else -> darkScheme
    }

    // 设置系统 状态栏颜色跟背景一致
    val systemUiController = rememberSystemUiController()
    val syscolor = colorScheme.background // 【重点修改】直接从计算好的 colorScheme 中直接取背景色，而不是写死
//    val darkiconscolor = if (isSystemInDarkTheme()) { false } else true
    val darkiconscolor = false
    SideEffect {
        systemUiController.setStatusBarColor(
            color = syscolor, // 状态栏
            darkIcons = darkiconscolor, // 根据主题设置图标颜色
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = QAppTypography,
        content = content
    )
}

