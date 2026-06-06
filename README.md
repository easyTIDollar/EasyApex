# EasyApex

EasyApex 是一个基于 Kotlin 与 Jetpack Compose 的 Android 应用，用于查询《Apex Legends》玩家与游戏运行状态数据。当前版本支持玩家搜索、地图轮换、猎杀门槛、服务器状态、主题切换，以及基于 GitHub Release 的应用内更新检查。

## 功能特性

- **玩家搜索**：支持按玩家名或 UID 查询玩家资料、排位信息与部分角色数据
- **地图轮换**：查看当前和下一张地图，以及剩余时间
- **猎杀门槛**：展示不同平台的猎杀者 RP 门槛
- **服务器状态**：查看相关服务器健康状态与响应延迟
- **主题切换**：支持动态、紫色、蓝色三种主题
- **搜索历史**：自动保存近期查询记录
- **应用更新**：从 GitHub Release 检查最新版本并下载 APK

## 技术栈

| 类别 | 技术 |
|---|---|
| 语言 | Kotlin |
| UI | Jetpack Compose、Material 3 |
| 架构 | MVVM + StateFlow |
| 网络 | Retrofit 2.9.0 + Gson |
| 异步 | Kotlin 协程 |
| 存储 | SharedPreferences |
| 构建 | Gradle Kotlin DSL |

## 项目结构

```text
app/
├── src/main/java/com/easyapex/
│   ├── MainActivity.kt
│   ├── ApexViewModel.kt
│   ├── ApexApi.kt
│   ├── ApexStatsScreen.kt
│   └── ui/theme/
│       ├── Color.kt
│       ├── Type.kt
│       └── Theme.kt
├── src/main/AndroidManifest.xml
└── build.gradle.kts
```

## 当前配置

- **包名**：`com.easyapex`
- **applicationId**：`com.easyapex`
- **minSdk**：26
- **targetSdk**：34
- **compileSdk**：34
- **versionCode**：2
- **versionName**：`1.0.4`
- **Apex API 基地址**：`https://api.mozambiquehe.re/`

## 快速开始

### 环境要求

- Android Studio Hedgehog 或更高版本
- JDK 17+
- Android SDK 34
- 一台 Android 模拟器或真机设备

### 运行项目

1. 用 Android Studio 打开项目目录：`F:\EasyApex`
2. 等待 Gradle 同步完成
3. 选择设备并运行 `app`

### 常用命令

```powershell
# 调试构建
.\gradlew assembleDebug

# 运行单元测试
.\gradlew test

# 运行仪器化测试
.\gradlew connectedAndroidTest
```

## 架构说明

应用采用单 Activity + MVVM 结构：

- `MainActivity.kt`：Compose 入口与主题注入
- `ApexViewModel.kt`：负责 API 请求、搜索历史、主题偏好、更新状态
- `ApexApi.kt`：定义 Apex API / GitHub Release API 接口和响应模型
- `ApexStatsScreen.kt`：承载主要页面 UI

核心状态流：

- `ApiState<T>`：统一表示普通接口请求状态
- `UpdateState`：表示更新检查、发现新版、下载进度等状态

## 数据与接口

### Apex 数据接口

| 端点 | 说明 |
|---|---|
| `GET /bridge?player=&platform=` | 按玩家名查询 |
| `GET /bridge?uid=&platform=` | 按 UID 查询 |
| `GET /nametouid?player=&platform=` | 名称转 UID |
| `GET /maprotation?version=2` | 地图轮换 |
| `GET /predator` | 猎杀门槛 |
| `GET /servers` | 服务器状态 |

### 更新接口

应用还会调用 GitHub Releases 最新版本接口，用于检查新版本与获取 APK 下载地址。

## 主题系统

当前支持以下主题：

- `DYNAMIC`：Android 12+ 动态取色，不支持时回退默认方案
- `PURPLE`：紫色主题
- `BLUE`：蓝色主题

主题偏好通过 `SharedPreferences` 持久化保存。

## 安全说明

- 当前 API Key 仍硬编码在 `app/src/main/java/com/easyapex/ApexViewModel.kt`。
- 若项目准备公开发布，建议迁移到 `local.properties`、`BuildConfig` 或安全配置方案。
- 当前本地存储未加密，不应保存高敏感数据。

## 后续可优化项

- 将 API Key 改为构建时注入
- 为更新下载增加更完整的签名或来源校验
- 补充更多单元测试和 UI 测试
- 清理重复依赖声明并统一版本管理

## 致谢

- [Apex Legends API](https://github.com/mozambiquehe/re-acting-apex) 或相关社区接口项目
- Jetpack Compose
- Material 3
