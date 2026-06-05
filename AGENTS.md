# EasyApex - 智能体指引

## 项目概述

**EasyApex** 是一个使用 Kotlin 和 Jetpack Compose 开发的 Android 应用，用于追踪 Apex 英雄联盟游戏数据。本文件为在本仓库中工作的 AI 智能体提供指导。

## 仓库结构

```
Apex_tracker/
├── app/
│   ├── src/main/java/com/jussicodes/easyapex/
│   │   ├── MainActivity.kt              # Compose 入口
│   │   ├── ApexViewModel.kt             # ViewModel + 状态管理
│   │   ├── ApexApi.kt                   # Retrofit 客户端 + 数据模型
│   │   ├── ApexStatsScreen.kt           # 所有 Compose UI 界面
│   │   └── ui/theme/
│   │       ├── Color.kt                 # 颜色定义
│   │       ├── Type.kt                  # 字体排印
│   │       └── Theme.kt                 # AppTheme 枚举 + 配色方案
│   ├── src/main/AndroidManifest.xml
│   └── build.gradle.kts
├── gradle/libs.versions.toml             # 版本目录
├── build.gradle.kts                       # 根构建配置
└── gradle.properties / settings.gradle.kts
```

## 关键技术细节

### 架构
- **MVVM 模式**：`ApexViewModel` 通过 `StateFlow` 管理所有状态。
- **单 Activity 架构**：`MainActivity` 通过 Compose 标签导航承载所有界面。
- **状态密封类**：`ApiState<out T>`（Idle/Loading/Success/Error）在所有界面中统一使用。

### UI 界面（底部导航栏标签页）
1. **玩家** — 按名称或 UID 搜索，显示排名/传奇统计数据
2. **地图** — 当前及即将到来的地图轮换，附带倒计时
3. **猎杀** — 各平台的猎杀者排名门槛 RP 需求
4. **服务器** — Origin 登录服务器健康状况与响应时间

### 依赖项
- **Retrofit 2.9.0** + Gson 转换器用于 HTTP
- **API 基地址**：`https://api.mozambiquehe.re/`
- **协程**用于 `viewModelScope` 中的异步 API 调用
- **SharedPreferences** 用于搜索历史和主题偏好存储（最多 10 条历史记录）

### 主题系统
`AppTheme` 枚举，共 3 个选项：
- `DYNAMIC` — 系统动态取色（API 31+），回退到自定义紫色
- `PURPLE` — 自定义紫色明暗方案
- `BLUE` — 自定义深蓝色明暗方案

存储在 SharedPreferences 中，键名为 `"app_theme_preference"`。

## 编码规范

### 语言
- 仅使用 Kotlin，不使用 Java
- 默认使用 `val`，仅在需要修改时使用 `var`
- 优先使用 `sealed class` / `sealed interface` 进行状态建模

### 命名约定
- `ViewModel` 类以 `ViewModel` 结尾
- 数据类使用 PascalCase 属性名
- Compose `@Composable` 函数使用 PascalCase
- 包名：`com.jussicodes.easyapex`

### API 集成
- 所有 API 调用均通过 `RetrofitClient.api`（单个对象）
- 响应类型在 `ApexApi.kt` 中定义为 `data class`
- ViewModel 中所有网络调用必须用 try/catch 包裹
- 使用 `forceRefresh: Boolean = false` 模式避免重复请求

### 状态管理
- 以 `StateFlow` 暴露状态（不对外暴露 `MutableStateFlow`）
- 所有 API 状态使用 `ApiState<T>` 密封类
- UI 通过 `collectAsState()` 收集状态

### UI 模式
- 列表界面使用 `PullToRefreshContainer` 支持下拉刷新
- 布局使用 `Scaffold` + `TopAppBar` + `NavigationBar`
- 列表项使用 `ElevatedCard` / `Card`
- 设置界面使用 `AlertDialog`

### SharedPreferences 键名
| 键名 | 值类型 | 用途 |
|---|---|---|
| `search_history` | 逗号分隔字符串 | 最近 10 条玩家搜索记录 |
| `app_theme_preference` | 枚举名称 | 选中的主题 |

## 安全提示

- **API 密钥硬编码在 `ApexViewModel.kt` 中**。在公开发布前，请将其移至 `local.properties` 或 BuildConfig。
- SharedPreferences 数据未加密。

## 构建与测试

```powershell
# 同步并构建
.\gradlew assembleDebug

# 运行单元测试
.\gradlew test

# 运行仪器化测试
.\gradlew connectedAndroidTest
```

## 常见操作

### 添加新的 API 端点
1. 在 `ApexApi.kt` 中为响应模型添加 `data class`
2. 在 `ApexApi` 接口中添加 `suspend fun` 方法
3. 在 `ApexViewModel` 中添加 `_state` / `state` StateFlow 配对
4. 在 `ApexViewModel` 中添加带缓存逻辑的获取函数
5. 在 `ApexStatsScreen.kt` 中添加新的 Compose 界面
6. 在 `ApexMainScreen` 的标签列表中注册新标签页

### 添加新主题
1. 在 `Theme.kt` 的 `AppTheme` 枚举中添加枚举值
2. 添加对应的 `darkColorScheme` / `lightColorScheme`
3. 在 `EasyApexTheme` 的 `when` 表达式中添加分支