package moe.fuqiuluo.mamu.ui.tutorial.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import moe.fuqiuluo.mamu.ui.theme.AdaptiveLayoutInfo
import moe.fuqiuluo.mamu.ui.theme.Dimens

/**
 * Tutorial step data class
 */
data class TutorialStep(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val tips: List<String> = emptyList()
)

/**
 * Default tutorial steps for Mamu
 */
val defaultTutorialSteps = listOf(
    TutorialStep(
        icon = Icons.Default.Celebration,
        title = "欢迎使用 Mamu",
        description = "Mamu 是一个强大的 Android 内存调试工具，可以帮助你搜索、监控和修改进程内存。",
        tips = listOf(
            "需要 Root 权限才能正常使用",
            "仅支持 arm64-v8a 架构设备"
        )
    ),
    TutorialStep(
        icon = Icons.Default.Window,
        title = "启动悬浮窗",
        description = "点击主界面右下角的悬浮按钮即可启动悬浮窗。悬浮窗是你进行所有内存操作的主要界面。",
        tips = listOf(
            "悬浮窗可以自由拖动位置",
            "点击悬浮窗可以展开/收起菜单"
        )
    ),
    TutorialStep(
        icon = Icons.Default.AppShortcut,
        title = "选择目标进程",
        description = "在悬浮窗菜单中选择「进程」，然后从列表中选择你要调试的目标应用进程。",
        tips = listOf(
            "只显示正在运行的进程",
            "可以通过包名或进程名搜索"
        )
    ),
    TutorialStep(
        icon = Icons.Default.Search,
        title = "搜索内存",
        description = "绑定进程后，选择「搜索」功能。输入要搜索的数值，选择数据类型（如 int、float），然后点击搜索。",
        tips = listOf(
            "首次搜索会扫描全部内存",
            "后续搜索在结果中筛选",
            "支持精确搜索和模糊搜索"
        )
    ),
    TutorialStep(
        icon = Icons.Default.FilterList,
        title = "筛选结果",
        description = "搜索结果可能很多，需要多次筛选。改变游戏中的数值后，输入新值再次搜索，逐步缩小范围。",
        tips = listOf(
            "数值变化后立即搜索效果最好",
            "可以使用「增加」「减少」等条件",
            "结果少于 100 个时可以尝试修改"
        )
    ),
    TutorialStep(
        icon = Icons.Default.Edit,
        title = "修改数值",
        description = "找到目标地址后，点击该地址可以修改其数值。修改会立即生效，可以在游戏中验证结果。",
        tips = listOf(
            "修改前建议保存地址",
            "某些数值可能有校验保护",
            "可以锁定数值防止变化"
        )
    ),
    TutorialStep(
        icon = Icons.Default.Warning,
        title = "注意事项",
        description = "使用内存修改工具存在风险，请仅在单机游戏或学习研究中使用。",
        tips = listOf(
            "在线游戏使用可能导致封号",
            "建议先在模拟器上练习",
            "保持应用隐藏以避免检测"
        )
    )
)

/**
 * Tutorial dialog component
 */
@Composable
fun TutorialDialog(
    adaptiveLayout: AdaptiveLayoutInfo,
    onDismiss: () -> Unit,
    onComplete: () -> Unit,
    onStartPractice: (() -> Unit)? = null,
    steps: List<TutorialStep> = defaultTutorialSteps
) {
    val pagerState = rememberPagerState(pageCount = { steps.size })
    val coroutineScope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side: Previous button
                if (pagerState.currentPage > 0) {
                    TextButton(
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        }
                    ) {
                        Text("上一步")
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                // Right side: action buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Dimens.spacingSm(adaptiveLayout))
                ) {
                    if (pagerState.currentPage < steps.size - 1) {
                        // Skip button
                        TextButton(onClick = onComplete) {
                            Text("跳过")
                        }
                        // Next button
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            }
                        ) {
                            Text("下一步")
                            Spacer(modifier = Modifier.width(Dimens.spacingXs(adaptiveLayout)))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(Dimens.iconXs(adaptiveLayout))
                            )
                        }
                    } else {
                        Button(onClick = onComplete) {
                            Text("开始使用")
                        }
                    }
                }
            }
        },
        title = null,
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Pager for tutorial steps
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimens.scaled(adaptiveLayout, 320f))
                ) { page ->
                    TutorialStepContent(adaptiveLayout = adaptiveLayout, step = steps[page])
                }

                // Practice button on last page
                if (onStartPractice != null && pagerState.currentPage == steps.size - 1) {
                    Spacer(modifier = Modifier.height(Dimens.spacingMd(adaptiveLayout)))
                    OutlinedButton(
                        onClick = onStartPractice,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            modifier = Modifier.size(Dimens.iconSm(adaptiveLayout))
                        )
                        Spacer(modifier = Modifier.width(Dimens.spacingSm(adaptiveLayout)))
                        Text("进入练习模式")
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.spacingLg(adaptiveLayout)))

                // Page indicators
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    repeat(steps.size) { index ->
                        val isSelected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .padding(horizontal = Dimens.spacingXs(adaptiveLayout))
                                .size(if (isSelected) Dimens.scaled(adaptiveLayout, 10f) else Dimens.spacingSm(adaptiveLayout))
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                    }
                                )
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun TutorialStepContent(adaptiveLayout: AdaptiveLayoutInfo, step: TutorialStep) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(Dimens.paddingSm(adaptiveLayout)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Icon
        Icon(
            imageVector = step.icon,
            contentDescription = null,
            modifier = Modifier.size(Dimens.iconXxl(adaptiveLayout)),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(Dimens.spacingLg(adaptiveLayout)))

        // Title
        Text(
            text = step.title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(Dimens.spacingMd(adaptiveLayout)))

        // Description
        Text(
            text = step.description,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Tips
        if (step.tips.isNotEmpty()) {
            Spacer(modifier = Modifier.height(Dimens.spacingLg(adaptiveLayout)))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(Dimens.paddingMd(adaptiveLayout))
                ) {
                    step.tips.forEach { tip ->
                        Row(
                            modifier = Modifier.padding(vertical = Dimens.spacingXxs(adaptiveLayout)),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(Dimens.iconXs(adaptiveLayout))
                                    .padding(top = Dimens.spacingXxs(adaptiveLayout)),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.width(Dimens.spacingSm(adaptiveLayout)))
                            Text(
                                text = tip,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}
