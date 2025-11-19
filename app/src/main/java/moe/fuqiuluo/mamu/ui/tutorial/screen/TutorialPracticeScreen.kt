package moe.fuqiuluo.mamu.ui.tutorial.screen

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import moe.fuqiuluo.mamu.driver.MemoryOps
import moe.fuqiuluo.mamu.ui.theme.MXTheme

/**
 * 练习界面 - 单值搜索练习
 * 目标：让用户通过悬浮窗搜索内存并将值修改为 114514
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorialPracticeScreen(
    onBack: () -> Unit
) {
    // 分配内存并存储初始值 42
    var memoryAddress by remember { mutableStateOf(0UL) }
    var currentValue by remember { mutableIntStateOf(42) }
    var isSuccess by remember { mutableStateOf(false) }

    // 初始化内存
    DisposableEffect(Unit) {
        val address = MemoryOps.alloc(4) // 4 bytes for Int
        if (address != 0UL) {
            MemoryOps.writeInt(address, 42)
            memoryAddress = address
        }

        onDispose {
            if (memoryAddress != 0UL) {
                MemoryOps.free(memoryAddress, 4)
            }
        }
    }

    // 定期从内存读取值（检测用户是否通过悬浮窗修改了值）
    LaunchedEffect(memoryAddress) {
        if (memoryAddress != 0UL) {
            while (true) {
                val value = MemoryOps.readInt(memoryAddress)
                if (value != currentValue) {
                    currentValue = value
                    if (value == 114514) {
                        isSuccess = true
                    }
                }
                delay(100) // 每 100ms 检查一次
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("单值搜索练习") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 说明卡片
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "练习目标",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "使用悬浮窗搜索下方显示的数值，找到内存地址后将其修改为 114514",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 步骤提示
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "操作步骤",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val steps = listOf(
                        "启动悬浮窗",
                        "选择进程：moe.fuqiuluo.mamu (PID: ${MemoryOps.getPid()})",
                        "搜索当前值：$currentValue",
                        "使用 +1/-1 按钮改变值",
                        "再次搜索新值筛选结果",
                        "重复直到找到唯一地址",
                        "修改该地址的值为 114514"
                    )

                    steps.forEachIndexed { index, step ->
                        Row(
                            modifier = Modifier.padding(vertical = 2.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "${index + 1}.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.width(20.dp)
                            )
                            Text(
                                text = step,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 数值显示卡片
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isSuccess) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 内存地址
                    Text(
                        text = "内存地址",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (memoryAddress != 0UL) {
                            // 屏蔽 MTE/TBI 标签，只显示低 48 位有效地址
                            val cleanAddress = memoryAddress and 0x0000FFFFFFFFFFFFUL
                            "0x${cleanAddress.toString(16).uppercase()}"
                        } else {
                            "分配中..."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 当前值
                    Text(
                        text = "当前值",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = currentValue.toString(),
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isSuccess) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // +1 / -1 按钮
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        FilledTonalButton(
                            onClick = {
                                if (memoryAddress != 0UL) {
                                    val newValue = currentValue - 1
                                    MemoryOps.writeInt(memoryAddress, newValue)
                                    currentValue = newValue
                                }
                            }
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = null)
                        }

                        FilledTonalButton(
                            onClick = {
                                if (memoryAddress != 0UL) {
                                    val newValue = currentValue + 1
                                    MemoryOps.writeInt(memoryAddress, newValue)
                                    currentValue = newValue
                                }
                            }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                        }
                    }
                }
            }

            // 成功提示
            if (isSuccess) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Column {
                            Text(
                                text = "恭喜完成！",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "你已经学会了基本的单值搜索和修改",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 目标提示
            Text(
                text = "目标值：114514",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ============ Previews ============

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
private fun TutorialPracticeScreenPreview() {
    MXTheme {
        // Note: Preview won't work with actual memory operations
        TutorialPracticeScreen(onBack = {})
    }
}