package moe.fuqiuluo.mamu.ui.screen

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import moe.fuqiuluo.mamu.data.model.DriverStatus
import moe.fuqiuluo.mamu.data.model.SeLinuxMode
import moe.fuqiuluo.mamu.data.model.SystemInfo
import moe.fuqiuluo.mamu.service.FloatingWindowService
import moe.fuqiuluo.mamu.ui.tutorial.components.TutorialDialog
import moe.fuqiuluo.mamu.ui.theme.MXTheme
import moe.fuqiuluo.mamu.ui.theme.AdaptiveLayoutInfo
import moe.fuqiuluo.mamu.ui.theme.Dimens
import moe.fuqiuluo.mamu.ui.theme.rememberAdaptiveLayoutInfo
import moe.fuqiuluo.mamu.ui.tutorial.TutorialManager
import moe.fuqiuluo.mamu.ui.viewmodel.MainViewModel

@Composable
fun HomeScreen(
    windowSizeClass: WindowSizeClass,
    viewModel: MainViewModel = viewModel(),
    onStartPractice: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val adaptiveLayout = rememberAdaptiveLayoutInfo(windowSizeClass)

    // 教程弹窗状态
    val showTutorial by TutorialManager.shouldShowTutorial.collectAsStateWithLifecycle()

    // 新手教程弹窗
    if (showTutorial) {
        TutorialDialog(
            adaptiveLayout = adaptiveLayout,
            onDismiss = { TutorialManager.dismissTutorial() },
            onComplete = { TutorialManager.completeTutorial() },
            onStartPractice = if (onStartPractice != null) {
                {
                    // 启动悬浮窗
                    if (!uiState.isFloatingWindowActive) {
                        val intent = Intent(context, FloatingWindowService::class.java)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            context.startForegroundService(intent)
                        } else {
                            context.startService(intent)
                        }
                    }
                    onStartPractice()
                }
            } else null
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            when (adaptiveLayout.windowSizeClass.widthSizeClass) {
                WindowWidthSizeClass.Compact -> {
                    // 竖屏布局：垂直Column with 居中对齐
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Column(
                            modifier = Modifier
                                .widthIn(max = adaptiveLayout.contentMaxWidth)
                                .fillMaxWidth()
                                .padding(Dimens.paddingLg(adaptiveLayout)),
                            verticalArrangement = Arrangement.spacedBy(
                                Dimens.spacingMd(
                                    adaptiveLayout
                                )
                            )
                        ) {
                            StatusOverviewCard(
                                adaptiveLayout = adaptiveLayout,
                                driverStatus = uiState.dashboardDriverInfo?.status,
                                isProcessBound = uiState.dashboardDriverInfo?.isProcessBound
                                    ?: false,
                                boundPid = uiState.dashboardDriverInfo?.boundPid ?: -1,
                                hasRoot = uiState.hasRootAccess,
                                seLinuxMode = uiState.seLinuxStatus?.mode,
                                seLinuxModeString = uiState.seLinuxStatus?.modeString
                            )

                            ReadmeCard(adaptiveLayout = adaptiveLayout)

                            SystemInfoCard(
                                adaptiveLayout = adaptiveLayout,
                                systemInfo = uiState.systemInfo
                            )

                            ScreenInfoCard(adaptiveLayout = adaptiveLayout)

                            // 错误信息
                            uiState.error?.let { error ->
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(Dimens.paddingLg(adaptiveLayout))
                                    ) {
                                        Text(
                                            text = "错误",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                        Spacer(
                                            modifier = Modifier.height(
                                                Dimens.spacingSm(
                                                    adaptiveLayout
                                                )
                                            )
                                        )
                                        Text(
                                            text = error,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                else -> {
                    // 横屏布局：2列FlowRow网格，填充整个宽度
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Dimens.paddingLg(adaptiveLayout)),
                            horizontalArrangement = Arrangement.spacedBy(
                                Dimens.spacingMd(
                                    adaptiveLayout
                                )
                            ),
                            verticalArrangement = Arrangement.spacedBy(
                                Dimens.spacingMd(
                                    adaptiveLayout
                                )
                            ),
                            maxItemsInEachRow = 2
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                StatusOverviewCard(
                                    adaptiveLayout = adaptiveLayout,
                                    driverStatus = uiState.dashboardDriverInfo?.status,
                                    isProcessBound = uiState.dashboardDriverInfo?.isProcessBound
                                        ?: false,
                                    boundPid = uiState.dashboardDriverInfo?.boundPid ?: -1,
                                    hasRoot = uiState.hasRootAccess,
                                    seLinuxMode = uiState.seLinuxStatus?.mode,
                                    seLinuxModeString = uiState.seLinuxStatus?.modeString
                                )
                            }

                            Box(modifier = Modifier.weight(1f)) {
                                ReadmeCard(adaptiveLayout = adaptiveLayout)
                            }

                            Box(modifier = Modifier.weight(1f)) {
                                SystemInfoCard(
                                    adaptiveLayout = adaptiveLayout,
                                    systemInfo = uiState.systemInfo
                                )
                            }

                            Box(modifier = Modifier.weight(1f)) {
                                ScreenInfoCard(adaptiveLayout = adaptiveLayout)
                            }

                            // 错误信息（横跨两列）
                            uiState.error?.let { error ->
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.errorContainer
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(
                                                Dimens.paddingLg(
                                                    adaptiveLayout
                                                )
                                            )
                                        ) {
                                            Text(
                                                text = "错误",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onErrorContainer
                                            )
                                            Spacer(
                                                modifier = Modifier.height(
                                                    Dimens.spacingSm(
                                                        adaptiveLayout
                                                    )
                                                )
                                            )
                                            Text(
                                                text = error,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onErrorContainer
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 悬浮操作按钮
        FloatingActionButton(
            onClick = { toggleFloatingWindow(context, uiState.isFloatingWindowActive) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(Dimens.paddingLg(adaptiveLayout))
        ) {
            Icon(
                imageVector = if (uiState.isFloatingWindowActive) {
                    Icons.Default.Close
                } else {
                    Icons.Default.Window
                },
                contentDescription = if (uiState.isFloatingWindowActive) {
                    "关闭悬浮窗"
                } else {
                    "启动悬浮窗"
                }
            )
        }
    }
}

private fun toggleFloatingWindow(context: Context, isActive: Boolean) {
    if (isActive) {
        val intent = Intent(context, FloatingWindowService::class.java)
        context.stopService(intent)
    } else {
        val intent = Intent(context, FloatingWindowService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }
}

@Composable
fun StatusOverviewCard(
    adaptiveLayout: AdaptiveLayoutInfo,
    driverStatus: DriverStatus?,
    isProcessBound: Boolean,
    boundPid: Int,
    hasRoot: Boolean,
    seLinuxMode: SeLinuxMode?,
    seLinuxModeString: String?
) {
    StatusCard(
        adaptiveLayout = adaptiveLayout,
        title = "状态概览",
        icon = Icons.Default.Dashboard
    ) {
        val driverStatusText = when (driverStatus) {
            DriverStatus.LOADED -> if (isProcessBound && boundPid > 0) "已加载 (PID: $boundPid)" else "已加载"
            DriverStatus.NOT_LOADED -> "未加载"
            DriverStatus.ERROR -> "错误"
            null -> "未知"
        }
        val driverColor = when (driverStatus) {
            DriverStatus.LOADED -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.error
        }
        StatusItem(
            adaptiveLayout = adaptiveLayout,
            label = "驱动",
            value = driverStatusText,
            color = driverColor
        )

        StatusItem(
            adaptiveLayout = adaptiveLayout,
            label = "Root",
            value = if (hasRoot) "已获取" else "未获取",
            color = if (hasRoot) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )

        val (selinuxText, selinuxColor) = when (seLinuxMode) {
            SeLinuxMode.ENFORCING -> "强制模式" to MaterialTheme.colorScheme.error
            SeLinuxMode.PERMISSIVE -> "宽容模式" to MaterialTheme.colorScheme.tertiary
            SeLinuxMode.DISABLED -> "已禁用" to MaterialTheme.colorScheme.primary
            SeLinuxMode.UNKNOWN, null -> "未知" to MaterialTheme.colorScheme.onSurfaceVariant
        }
        StatusItem(
            adaptiveLayout = adaptiveLayout,
            label = "SELinux",
            value = seLinuxModeString ?: selinuxText,
            color = selinuxColor
        )
    }
}

@Composable
fun ReadmeCard(adaptiveLayout: AdaptiveLayoutInfo) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(Dimens.paddingLg(adaptiveLayout))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacingSm(adaptiveLayout))
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "关于 Mamu",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(Dimens.spacingMd(adaptiveLayout)))
            Text(
                text = "Mamu 是一个需要 Root 权限的 Android 内存操作和调试工具。" +
                        "通过悬浮窗界面，可以在运行时搜索、监控和修改进程内存。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(Dimens.spacingSm(adaptiveLayout)))
            Text(
                text = "点击右下角按钮启动悬浮窗",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(Dimens.spacingMd(adaptiveLayout)))

            HorizontalDivider(
                modifier = Modifier.padding(vertical = Dimens.spacingSm(adaptiveLayout)),
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacingSm(adaptiveLayout))
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Text(
                    text = "安全警告",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Spacer(modifier = Modifier.height(Dimens.spacingSm(adaptiveLayout)))
            Text(
                text = "FloatingWindowService 可能被目标应用检测到（通过 Service 查询）" +
                        "请自行实现应用隐藏（修改包名、进程名、使用 Xposed/LSPosed 隐藏等），" +
                        "否则可能被特定系统检测并导致封号，影响日常进程使用，尽管Mamu只是一个调试工具。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun SystemInfoCard(
    adaptiveLayout: AdaptiveLayoutInfo,
    systemInfo: SystemInfo
) {
    StatusCard(
        adaptiveLayout = adaptiveLayout,
        title = "设备信息",
        icon = Icons.Default.PhoneAndroid
    ) {
        StatusItem(
            adaptiveLayout = adaptiveLayout,
            label = "设备",
            value = "${systemInfo.deviceBrand} ${systemInfo.deviceModel}"
        )
        StatusItem(
            adaptiveLayout = adaptiveLayout,
            label = "系统",
            value = "Android ${systemInfo.androidVersion} (API ${systemInfo.sdkVersion})"
        )
        StatusItem(
            adaptiveLayout = adaptiveLayout,
            label = "架构",
            value = systemInfo.cpuAbi
        )
    }
}

@Composable
fun ScreenInfoCard(
    adaptiveLayout: AdaptiveLayoutInfo
) {
    val context = LocalContext.current
    val displayMetrics = context.resources.displayMetrics

    // 获取屏幕信息
    val screenWidth = displayMetrics.widthPixels
    val screenHeight = displayMetrics.heightPixels
    val density = displayMetrics.density
    val densityDpi = displayMetrics.densityDpi
    val scaledDensity = displayMetrics.scaledDensity

    // 计算屏幕尺寸（英寸）
    val widthInches = screenWidth / densityDpi.toFloat()
    val heightInches = screenHeight / densityDpi.toFloat()
    val diagonalInches = kotlin.math.sqrt(
        widthInches * widthInches + heightInches * heightInches
    )

    // 密度类型
    val densityType = when {
        densityDpi <= 120 -> "LDPI"
        densityDpi <= 160 -> "MDPI"
        densityDpi <= 240 -> "HDPI"
        densityDpi <= 320 -> "XHDPI"
        densityDpi <= 480 -> "XXHDPI"
        densityDpi <= 640 -> "XXXHDPI"
        else -> "ULTRA"
    }

    StatusCard(
        adaptiveLayout = adaptiveLayout,
        title = "屏幕信息",
        icon = Icons.Default.Smartphone
    ) {
        StatusItem(
            adaptiveLayout = adaptiveLayout,
            label = "分辨率",
            value = "${screenWidth} × ${screenHeight}"
        )
        StatusItem(
            adaptiveLayout = adaptiveLayout,
            label = "屏幕尺寸",
            value = "${"%.2f".format(diagonalInches)}\" (${"%.2f".format(widthInches)} × ${
                "%.2f".format(
                    heightInches
                )
            })"
        )
        StatusItem(
            adaptiveLayout = adaptiveLayout,
            label = "密度 (DPI)",
            value = "$densityDpi ($densityType)"
        )
        StatusItem(
            adaptiveLayout = adaptiveLayout,
            label = "密度系数",
            value = "${"%.2f".format(density)}x"
        )
        StatusItem(
            adaptiveLayout = adaptiveLayout,
            label = "字体缩放",
            value = "${"%.2f".format(scaledDensity)}x"
        )
    }
}

@Composable
fun StatusCard(
    adaptiveLayout: AdaptiveLayoutInfo,
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(Dimens.paddingLg(adaptiveLayout))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacingSm(adaptiveLayout))
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(Dimens.spacingMd(adaptiveLayout)))
            content()
        }
    }
}

@Composable
fun StatusItem(
    adaptiveLayout: AdaptiveLayoutInfo,
    label: String,
    value: String,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.spacingXs(adaptiveLayout)),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

// ============ Previews ============
// Note: Previews use fixed dp values as they don't have WindowSizeClass

@Preview(
    name = "Light Mode",
    showBackground = true
)
@Preview(
    name = "Dark Mode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun StatusOverviewCardPreviewLegacy() {
    MXTheme {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("状态概览", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("驱动: 已加载")
                Text("Root: 已获取")
                Text("SELinux: 宽容模式")
            }
        }
    }
}

@Preview(
    name = "Light Mode",
    showBackground = true
)
@Preview(
    name = "Dark Mode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun SystemInfoCardPreviewLegacy() {
    MXTheme {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("设备信息", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("设备: Google Pixel 8 Pro")
                Text("系统: Android 15 (API 35)")
                Text("架构: arm64-v8a")
            }
        }
    }
}
