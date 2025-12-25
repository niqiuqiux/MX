# Mamu

**一个类似于GameGuardion的内存修改器**，使用Kotlin+Rust编写，基于[android-wuwa](https://github.com/fuqiuluo/android-wuwa)实现！

[![许可证](https://img.shields.io/badge/license-GPLv3-green.svg)](https://github.com/Shirasuki/MX/blob/master/LICENSE)
[![平台](https://img.shields.io/badge/platform-Android%207.0%2B-orange.svg)](https://developer.android.com)
[![语言](https://img.shields.io/badge/language-Kotlin%20%2B%20Rust-purple.svg)](https://kotlinlang.org)

**中文** | [English](./README.md)

---

> **⚠️ 重要安全声明**
>
> - **需要 ROOT 权限**：此工具需要特权系统访问权限
> - **仅限教育用途**：仅用于研究、学习和授权的安全测试
> - **法律责任**：用户承担使用本软件的所有法律责任
> - **检测风险**：可能被反作弊系统和安全机制检测到
> - **无担保**：按"原样"提供，不提供任何担保或保证
> - **负责任使用**：尊重软件许可证、服务条款和适用法律

> **使用本软件即表示您确认：**
>
> - 您对目标应用和设备拥有**授权访问权**
> - 您理解并接受**所有法律风险**和责任
> - 您**不会将**此工具用于非法目的
> - 开发者对滥用或损害**不承担任何责任**
> - 您将遵守所有**适用的法律和法规**

---

## 核心功能

- **实时内存搜索与修改**
  - 精确值搜索（整数、浮点数、双精度、文本、十六进制）
  - 未知值的模糊搜索
  - 精炼搜索以缩小结果范围
  - 批量修改和数值冻结

- **浮窗界面**
  - **搜索标签页**：精确搜索/联合搜索/模糊搜索，实时进度显示
  - **设置标签页**：进程绑定、内存范围配置
  - **保存地址标签页**：收藏和管理内存地址
  - **内存预览标签页**：十六进制转储可视化及 ASCII 表示
  - **断点标签页**：调试断点和监视点（未实现）

- **高级内存访问**
  - 多种访问模式：_NORMAL_、_WRITETHROUGH_、_NOCACHE_、_PGFAULT_
  - 基于权限的内存区域过滤
  - 访问失败时自动重试
  - 全新的内存区域映射分析

## 系统要求

### 设备要求

| 要求          | 规格                            |
|-------------|-------------------------------|
| **操作系统**    | Android 7.0+ (API 24+)        |
| **架构**      | 仅限 ARM64-v8a (aarch64)        |
| **Root 权限** | 必需（Magisk、KernelSU 或 SuperSU） |

### 构建要求

| 要求                 | 版本                          |
|--------------------|-----------------------------|
| **Android SDK**    | compileSdk 36, targetSdk 35 |
| **Android NDK**    | 最新稳定版，带 LLVM 工具链            |
| **Rust**           | 最新稳定版 (1.70+)               |
| **Gradle**         | 8.13+                       |

## 安装

### 选项 A：下载预构建 APK

> **即将推出**：预构建 APK 版本将在 [Releases](https://github.com/Shirasuki/MX/releases) 页面提供

1. 从发布页面下载最新 APK
2. 在 Android 设置中启用"未知来源安装"
3. 在已 root 的设备上安装 APK
4. 启动应用并在提示时授予 root 权限
5. 允许所有必需权限（大多数通过 root 自动授予）

### 选项 B：从源码构建

请参阅下面的[从源码构建](#从源码构建)部分获取详细说明。

## 从源码构建

### 前提条件

#### 安装 Rust 工具链

```bash
# Linux/macOS/WSL
curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh

# Windows（从 https://rustup.rs 获取并使用 rustup-init.exe）
```

#### 添加 Android 目标

```bash
rustup target add aarch64-linux-android
```

#### 设置环境变量

**Linux/macOS:**
```bash
export ANDROID_SDK_ROOT=$HOME/Android/Sdk
export ANDROID_NDK_HOME=$ANDROID_SDK_ROOT/ndk/<version>

# 示例:
# export ANDROID_NDK_HOME=$ANDROID_SDK_ROOT/ndk/26.1.10909125
```

**Windows (PowerShell):**
```powershell
$env:ANDROID_SDK_ROOT = "C:\Users\<YourUsername>\AppData\Local\Android\Sdk"
$env:ANDROID_NDK_HOME = "$env:ANDROID_SDK_ROOT\ndk\<version>"
```

**Windows (命令提示符):**
```cmd
set ANDROID_SDK_ROOT=C:\Users\<YourUsername>\AppData\Local\Android\Sdk
set ANDROID_NDK_HOME=%ANDROID_SDK_ROOT%\ndk\<version>
```

### 构建步骤

#### 克隆仓库

```bash
git clone https://github.com/Shirasuki/MX.git
cd MX
```

#### 构建调试 APK

```bash
# 自动编译 Rust 库并构建 APK
./gradlew assembleDebug

# 输出: app/build/outputs/apk/debug/app-debug.apk
```

#### 构建发布 APK

```bash
./gradlew assembleRelease

# 输出: app/build/outputs/apk/release/app-release.apk
# 注意: 需要在 build.gradle.kts 中配置签名
```

#### 安装到设备

```bash
# 通过 ADB 安装调试版本
./gradlew installDebug

# 或手动安装
adb install app/build/outputs/apk/debug/app-debug.apk
```

### 手动构建 Rust（可选）

Gradle 构建系统通过 `buildRustAndroid` 任务自动编译 Rust 库。很少需要手动编译，但可以这样做：

```bash
cd app/src/main/rust

# 调试构建
cargo build --target aarch64-linux-android

# 发布构建（带优化）
cargo build --target aarch64-linux-android --release
```

**构建流程**：Gradle 构建自动触发 `buildRustAndroid → copyRustLibs → preBuild`，确保在 Android 构建开始之前将 Rust 库编译并复制到 `app/src/main/jniLibs/arm64-v8a/libmamu_core.so`。

### 构建问题排查

| 问题                | 解决方案                                             |
|-------------------|--------------------------------------------------|
| **找不到 NDK**       | 设置 `ANDROID_NDK_HOME` 环境变量或通过 SDK Manager 安装 NDK |
| **缺少 Rust 目标**    | 运行 `rustup target add aarch64-linux-android`     |
| **链接器错误**         | 确保 NDK 版本具有 LLVM 工具链（r21+）                       |
| **gradlew 权限被拒绝** | 运行 `chmod +x gradlew`（Linux/macOS）               |

## 架构概览

Mamu 采用分层混合架构，将 Kotlin 用于 UI/展示层，Rust 用于性能关键操作。

### 架构层次图

```
┌─────────────────────────────────────────────────────────┐
│          UI 层 (Jetpack Compose + ViewBinding)          │
│   - Compose 屏幕（HomeScreen、对话框）                     │
│   - XML 布局（悬浮窗、自定义键盘）                           │
├─────────────────────────────────────────────────────────┤
│      展示层（Controllers + ViewModels）                  │
│   - MainViewModel（主屏幕的 StateFlow）                   │
│   - FloatingControllers（搜索、设置等）                   │
│   - FloatingWindowStateManager（服务状态同步）            │
├─────────────────────────────────────────────────────────┤
│        数据/仓库层（DataSources）                         │
│   - MMKV（配置持久化）                                   │
├─────────────────────────────────────────────────────────┤
│         JNI 桥接层（Kotlin Facades）                     │
│   - WuwaDriver（进程管理、内存区域、内存读写）               │
│   - SearchEngine（内存搜索）                             │
└─────────────────────────────────────────────────────────┘
```

## 计划功能

### 计划功能

- [x] **组搜索**：搜索内存中的相关值
- [x] **偏移计算器**：计算相关地址之间的偏移
- [ ] **搜索历史**：保存和重放以前的搜索会话
- [ ] **进制转换工具**：十六进制/十进制/二进制/八进制转换器
- [ ] **脚本自动化**：Lua 脚本支持自动化操作
- [ ] **多架构支持**：支持 arm、x86、x86_64（除 ARM64 外）
- [ ] **CSV 导入/导出**：批量导入/导出保存的地址
- [ ] **内存差异**：比较随时间变化的内存快照
- [ ] **变速器**：修改游戏速度（时间操控）
- [ ] **断点调试**：断点调试进程
- [ ] **Imgui**：支持使用imgui渲染脚本界面
- [ ] **指针链扫描**
- [ ] **扩展插件系统**

## 贡献

欢迎贡献！无论是错误修复、新功能、文档改进还是翻译，您的帮助都值得!

### 特别感谢

- **GameGuardian** - 流行的 Android 内存操控工具
- **Cheat Engine** - PC 内存扫描器和调试器
- Magisk/KernelSu提供的 root 访问基础设施
- niqiuqiux的[PointerScan](https://github.com/niqiuqiux/PointerScan)提供C++链式指针扫描实现！

---

## 免责声明

**本软件按"原样"提供，不提供任何明示或暗示的担保**，包括但不限于适销性、特定用途适用性和非侵权性的担保。在任何情况下，作者或版权持有人均不对任何索赔、损害或其他责任负责，无论是在合同诉讼、侵权行为还是其他方面，由软件或软件的使用或其他交易引起、产生或与之相关。

**使用 Mamu 即表示您确认：**

- 您已阅读并理解本 README 和 LICENSE
- 您接受与内存操控相关的所有风险
- 您将负责任且合法地使用此工具
- 您理解开发者对任何损害或法律后果不承担任何责任

**仅限教育和研究目的。使用风险自负。**

---

本项目基于 **GNU 通用公共许可证 v3.0（GPLv3）** 授权，有关完整许可证文本，请参阅 [LICENSE](https://github.com/Shirasuki/MX/blob/master/LICENSE) 文件。

**[⬆ 返回顶部](#mamu)**
