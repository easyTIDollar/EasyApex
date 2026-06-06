# EasyApex - 智能体指引

## 项目概述

**EasyApex** 是一个使用 Kotlin 和 Jetpack Compose 开发的 Android 应用，用于查询和追踪《Apex Legends》相关数据。当前项目已经实现玩家信息查询、地图轮换、猎杀门槛、服务器状态，以及应用内更新检查等能力。

## 仓库结构

```text
EasyApex/
├── app/
│   ├── src/main/java/com/easyapex/
│   │   ├── MainActivity.kt              # Compose 入口
│   │   ├── ApexViewModel.kt             # ViewModel + 状态管理 + 更新逻辑
│   │   ├── ApexApi.kt                   # Retrofit 客户端 + 数据模型 + GitHub Release 接口
│   │   ├── ApexStatsScreen.kt           # Compose UI 界面
│   │   └── ui/theme/
│   │       ├── Color.kt                 # 颜色定义
│   │       ├── Type.kt                  # 字体排印
│   │       └── Theme.kt                 # AppTheme 枚举 + 配色方案
│   ├── src/main/AndroidManifest.xml
│   ├── src/test/
│   └── build.gradle.kts
├── gradle/libs.versions.toml            # 版本目录
├── build.gradle.kts                     # 根构建配置
├── settings.gradle.kts
├── README.md
└── AGENTS.md
```

## 当前功能

### 主要页面
1. **玩家**：按玩家名或 UID 查询资料、排位与部分角色数据
2. **地图**：查看当前与下一张地图轮换和倒计时
3. **猎杀**：查看各平台猎杀者门槛
4. **服务器**：查看服务器状态与延迟
5. **设置/关于**：主题切换、版本展示、检查更新

### 已实现能力
- 玩家搜索与最近搜索历史
- 地图轮换刷新
- 猎杀者分数门槛查询
- 服务器状态查看
- 主题偏好持久化
- GitHub Release 更新检查与 APK 下载

## 关键技术细节

### 架构
- **MVVM 模式**：`ApexViewModel` 通过 `StateFlow` 管理界面状态
- **单 Activity 架构**：`MainActivity` 承载 Compose 页面
- **统一请求状态**：`ApiState<T>` 负责 Idle / Loading / Success / Error
- **独立更新状态**：`UpdateState` 负责版本检查与下载过程展示

### 技术栈
- **Kotlin**
- **Jetpack Compose + Material 3**
- **Retrofit 2.9.0 + Gson**
- **Kotlin 协程**
- **SharedPreferences**
- **GitHub Releases API**：用于检查应用更新

### 重要配置
- **包名 / namespace**：`com.easyapex`
- **应用 ID**：`com.easyapex`
- **最低 SDK**：26
- **目标 / 编译 SDK**：34
- **当前版本**：`versionCode = 8`，`versionName = "1.0.11"`
- **API 基地址**：`https://api.mozambiquehe.re/`

## 代码约定

### 语言与风格
- 仅使用 Kotlin，不使用 Java
- 默认优先使用 `val`
- 优先用 `sealed class` 表达状态
- 修改时保持现有 Compose + ViewModel 风格一致

### 命名约定
- ViewModel 类以 `ViewModel` 结尾
- `@Composable` 函数使用 PascalCase
- 数据模型使用 Kotlin 常规 PascalCase 命名
- 包名统一使用 `com.easyapex`

### API 集成
- 所有 Apex 接口统一通过 `RetrofitClient.api`
- GitHub 更新接口通过独立的 `GitHubApi` / 对应 Retrofit 实例访问
- 响应模型集中定义在 `ApexApi.kt`
- ViewModel 中网络请求统一使用 `try/catch`

### 状态管理
- 对外暴露 `StateFlow`，不直接暴露 `MutableStateFlow`
- 页面请求状态统一使用 `ApiState<T>`
- 更新流程统一使用 `UpdateState`
- UI 端通过 `collectAsState()` 收集状态

### 本地存储
当前使用 `SharedPreferences("apex_prefs")`，包含：

| 键名 | 值类型 | 用途 |
|---|---|---|
| `search_history` | 逗号分隔字符串 | 最近搜索记录 |
| `app_theme_preference` | 字符串 | 当前主题枚举值 |

## 安全与维护提示

- **API 密钥当前硬编码在 `app/src/main/java/com/easyapex/ApexViewModel.kt` 中**，公开发布前应迁移到 `local.properties`、`BuildConfig` 或其他安全配置方案。
- `SharedPreferences` 当前未加密，仅适合保存低敏感偏好数据。
- 更新下载涉及外部 URL，后续可补充签名校验或更严格的安装流程。

## 构建与测试

```powershell
# 调试构建
.\gradlew assembleDebug

# 单元测试
.\gradlew test

# 仪器化测试
.\gradlew connectedAndroidTest
```

## 常见修改路径

### 新增 Apex API 端点
1. 在 `ApexApi.kt` 中新增响应 `data class`
2. 在 `ApexApi` 接口中新增 `suspend` 方法
3. 在 `ApexViewModel.kt` 中补充状态与请求函数
4. 在 `ApexStatsScreen.kt` 中新增 UI 展示
5. 如有必要，在主页标签中接入入口

### 新增主题
1. 在 `app/src/main/java/com/easyapex/ui/theme/Theme.kt` 中扩展 `AppTheme`
2. 增加对应 `lightColorScheme` / `darkColorScheme`
3. 在主题分支中接入新方案

### 调整更新逻辑
1. 检查 `ApexApi.kt` 中 GitHub Release 数据模型和接口
2. 检查 `ApexViewModel.kt` 中版本比较、下载与状态流逻辑
3. 如涉及权限或安装流程，同时检查 `AndroidManifest.xml`






