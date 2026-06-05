# EasyApex - Apex 英雄数据追踪器

> 一款轻量级 Android 应用，用于追踪 Apex 英雄玩家数据、地图轮换、猎杀门槛和服务器状态。

## 概述

**EasyApex** 是一款使用 Kotlin 和 Jetpack Compose 构建的 Android 应用程序，通过 [Mozambique API](https://github.com/mozibane/apex_legends_api) 提供实时的 Apex 英雄游戏数据。它采用 Material Design 3 风格的清爽界面，支持自定义主题配色。

## 功能特性

- **玩家搜索** — 通过用户名或 UID 查询玩家，查看排名数据、传奇角色表现和封号历史
- **地图轮换** — 查看当前和即将到来的生存竞赛/排名赛地图及倒计时
- **猎杀门槛** — 追踪各平台（PC、Xbox、PlayStation）达到猎杀者排名所需的 RP
- **服务器状态** — 实时监控 Origin 登录服务的健康状况和响应延迟
- **主题自定义** — 在动态取色、紫色、蓝色三种配色方案中选择
- **搜索历史** — 自动保存最近搜索记录，方便快速查询

## 技术栈

| 类别 | 技术 |
|---|---|
| 编程语言 | Kotlin |
| 用户界面 | Jetpack Compose、Material Design 3 |
| 架构模式 | MVVM + StateFlow |
| 网络请求 | Retrofit 2.9.0 + Gson |
| 并发处理 | Kotlin 协程 |
| 依赖注入 | ViewModel + Compose `viewModel()` |
| 构建工具 | Gradle (Kotlin DSL)、AGP 8.5.0 |
| 最低 SDK | 26 (Android 8.0) |
| 目标 SDK | 34 (Android 14) |

## 项目结构

```
app/
├── src/main/java/com/jussicodes/easyapex/
│   ├── MainActivity.kt              # 入口，Compose 组合
│   ├── ApexViewModel.kt             # ViewModel：API 调用、状态管理、偏好设置
│   ├── ApexApi.kt                   # 数据模型、Retrofit 接口、API 客户端
│   ├── ApexStatsScreen.kt           # Compose UI 界面（玩家、地图、猎杀、服务器）
│   └── ui/theme/
│       ├── Color.kt                 # 颜色定义
│       ├── Type.kt                  # 字体排印
│       └── Theme.kt                 # AppTheme 枚举、配色方案
├── src/main/AndroidManifest.xml
└── build.gradle.kts
```

## 快速开始

### 前置要求

- Android Studio Hedgehog（或更高版本）
- JDK 17+
- Android SDK 34
- 模拟器（API 26+）或实体设备

### 设置步骤

1. 在 Android Studio 中打开项目：`File → Open` → 选择 `F:\Apex_tracker`
2. 等待 Gradle 同步完成
3. 在模拟器或已连接的设备上运行

### 构建命令

```powershell
# 调试构建
.\gradlew assembleDebug

# 发布构建（未签名）
.\gradlew assembleRelease
```

## 架构图

```
┌─────────────────────────────────────────┐
│            Compose UI 界面              │
│  (玩家搜索 / 地图轮换 / 猎杀者 / 服务器) │
└──────────────┬──────────────────────────┘
               │ StateFlow 监听
┌──────────────▼──────────────────────────┐
│           ApexViewModel                 │
│  • API 状态管理                          │
│  • 搜索历史（SharedPreferences）        │
│  • 主题偏好持久化                        │
│  • 基于协程的 API 调用                   │
└──────────────┬──────────────────────────┘
               │ Retrofit 挂起函数
┌──────────────▼──────────────────────────┐
│        Retrofit + ApexApi 接口          │
│  基地址: https://api.mozambiquehe.re/   │
└─────────────────────────────────────────┘
```

## API 端点

所有端点均通过 [Apex 英雄 API](https://github.com/mozibane/apex_legends_api) 代理：

| 端点 | 描述 |
|---|---|
| `GET /bridge?player=&platform=` | 按名称查询玩家资料 |
| `GET /bridge?uid=&platform=` | 按 UID 查询玩家资料 |
| `GET /nametouid?player=&platform=` | 解析 EA 名称为 UID |
| `GET /maprotation?version=2` | 当前及下一张地图轮换 |
| `GET /predator` | 各平台猎杀者排名门槛 |
| `GET /servers` | 服务器运行状态 |

> ⚠️ **安全提示**：API 密钥目前硬编码在 `ApexViewModel.kt` 中。公开发布前请将其移至配置文件、环境变量或 BuildConfig。

## 主题系统

设置中提供三种主题选项：

| 主题 | 描述 |
|---|---|
| **动态取色** | 基于系统强调色的配色（Android 12+），回退到自定义紫色 |
| **紫色** | 自定义紫色配色方案（明暗模式） |
| **蓝色** | 自定义深蓝色配色方案（明暗模式） |

## 构建配置

- **编译 SDK**：34
- **最低 SDK**：26
- **AGP**：8.5.0
- **Kotlin**：1.9.0
- **Compose BOM**：2024.04.01

## 许可证

[待定]

## 致谢

- [Apex 英雄 API](https://github.com/mozibane/apex_legends_api) — 数据提供方
- Material Design 3 — UI 框架
- Jetpack Compose — 声明式 UI 工具包