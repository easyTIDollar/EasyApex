package com.jussicodes.easyapex

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jussicodes.easyapex.ui.theme.AppTheme // 🌟 导入你的主题

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApexMainScreen(viewModel: ApexViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("玩家", "地图", "猎杀", "服务器")
    val icons = listOf(Icons.Default.Person, Icons.Default.Place, Icons.Default.Star, Icons.Default.Info)

    var showSettingsDialog by remember { mutableStateOf(false) }
    val currentTheme by viewModel.currentTheme.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("EasyApex - ${tabs[selectedTab]}") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "设置")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        icon = { Icon(icons[index], contentDescription = title) },
                        label = { Text(title) },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (selectedTab) {
                0 -> PlayerSearchScreen(viewModel)
                1 -> MapRotationScreen(viewModel)
                2 -> PredatorScreen(viewModel)
                3 -> ServerStatusScreen(viewModel)
            }
        }

        if (showSettingsDialog) {
            SettingsDialog(
                currentTheme = currentTheme,
                onThemeChange = { newTheme -> viewModel.setTheme(newTheme) },
                onDismiss = { showSettingsDialog = false }
            )
        }
    }
}

// ================= 设置弹窗 =================
@Composable
fun SettingsDialog(
    currentTheme: AppTheme,
    onThemeChange: (AppTheme) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("设置与关于", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("主题配色", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    AppTheme.values().forEach { themeOption ->
                        FilterChip(
                            selected = currentTheme == themeOption,
                            onClick = { onThemeChange(themeOption) },
                            label = { Text(themeOption.displayName) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                Text("关于 EasyApex", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "一款轻量、便捷的《Apex 英雄》非官方数据查询工具。支持 Origin ID 及底层 UID 智能查询，提供最新地图轮换与猎杀者门槛数据。\n\n数据来源: apexlegendsstatus.com",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val githubUrl = "https://github.com/你的用户名/EasyApex/releases"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(githubUrl))
                context.startActivity(intent)
            }) {
                Text("检查更新")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("关闭") }
        }
    )
}

// ================= 1. 玩家查询页面 =================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerSearchScreen(viewModel: ApexViewModel) {
    var playerName by remember { mutableStateOf("") }
    val uiState by viewModel.playerState.collectAsState()
    val searchHistory by viewModel.searchHistory.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        OutlinedTextField(
            value = playerName, onValueChange = { playerName = it },
            label = { Text("输入 EA 名字 或 UID") },
            modifier = Modifier.fillMaxWidth(), singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { viewModel.searchPlayer(playerName) }, modifier = Modifier.fillMaxWidth()) {
            Text("查询战绩")
        }

        if (searchHistory.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                Text("搜索历史", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.outline)
            }
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(searchHistory) { historyName ->
                    InputChip(
                        selected = false,
                        onClick = { playerName = historyName; viewModel.searchPlayer(historyName) },
                        label = { Text(historyName) },
                        trailingIcon = {
                            Icon(Icons.Default.Close, contentDescription = "删除", modifier = Modifier.size(16.dp).clickable { viewModel.removeHistoryItem(historyName) })
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        when (val state = uiState) {
            is ApiState.Idle -> Text("支持 Origin ID 或 底层数字 UID 查询", color = MaterialTheme.colorScheme.outline)
            is ApiState.Loading -> CircularProgressIndicator()
            is ApiState.Error -> Text("错误: ${state.message}", color = MaterialTheme.colorScheme.error)
            is ApiState.Success -> PlayerResultCard(state.data)
        }
    }
}

@Composable
fun PlayerResultCard(playerData: PlayerResponse) {
    val global = playerData.global
    val realtime = playerData.realtime
    val legends = playerData.legends

    if (global == null) { Text("未获取到玩家详细数据"); return }

    ElevatedCard(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.elevatedCardElevation(6.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = global.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                if (realtime != null) {
                    val isOnline = realtime.isOnline == 1
                    val statusText = if (isOnline) { if (realtime.isInGame == 1) "游戏中" else "大厅发呆" } else "离线"
                    AssistChip(onClick = { }, label = { Text(statusText) }, colors = AssistChipDefaults.assistChipColors(labelColor = if (isOnline) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    val prestige = global.levelPrestige ?: 0
                    val totalLevel = prestige * 500 + global.level
                    val levelText = if (prestige > 0) "${global.level} (转生 $prestige，总 $totalLevel 级)" else "${global.level} 级"
                    InfoItem(icon = Icons.Default.Person, title = "等级", value = levelText)
                    Spacer(modifier = Modifier.height(8.dp))
                    InfoItem(icon = Icons.Default.Info, title = "平台", value = global.platform)
                }
                if (global.rank != null) {
                    Column(horizontalAlignment = Alignment.End) {
                        InfoItem(icon = Icons.Default.Star, title = "当前排位", value = "${global.rank.rankName} ${global.rank.rankDiv}", valueColor = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "排位分: ${global.rank.rankScore}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(end = 4.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (global.bans?.isActive == true) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer), modifier = Modifier.fillMaxWidth()) {
                    Text(text = "⚠️ 该账号已被封禁 (原因: ${global.bans.last_banReason})", color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.padding(8.dp), style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (legends?.selected != null) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "当前选用: ${legends.selected.LegendName}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        val trackers = legends.selected.data
                        if (trackers.isNullOrEmpty()) {
                            Text("未装备任何数据追踪器", style = MaterialTheme.typography.bodySmall)
                        } else {
                            trackers.forEach { tracker ->
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = tracker.name, style = MaterialTheme.typography.bodyMedium)
                                    Text(text = tracker.value.toInt().toString(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoItem(icon: ImageVector, title: String, value: String, valueColor: Color = MaterialTheme.colorScheme.onSurface) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = valueColor)
        }
    }
}

// ================= 2. 地图轮换页面 =================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapRotationScreen(viewModel: ApexViewModel) {
    LaunchedEffect(Unit) { viewModel.fetchMapRotation() }
    val uiState by viewModel.mapState.collectAsState()
    val pullToRefreshState = rememberPullToRefreshState()

    if (pullToRefreshState.isRefreshing) {
        LaunchedEffect(true) { viewModel.fetchMapRotation(forceRefresh = true) }
    }
    LaunchedEffect(uiState) { if (uiState !is ApiState.Loading) pullToRefreshState.endRefresh() }

    Box(modifier = Modifier.fillMaxSize().nestedScroll(pullToRefreshState.nestedScrollConnection)) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            when (val state = uiState) {
                is ApiState.Loading -> if (!pullToRefreshState.isRefreshing) CircularProgressIndicator()
                is ApiState.Error -> Text(state.message, color = MaterialTheme.colorScheme.error)
                is ApiState.Success -> {
                    MapCard("匹配地图轮换", state.data.battle_royale)
                    Spacer(modifier = Modifier.height(16.dp))
                    MapCard("排位地图轮换", state.data.ranked)
                }
                is ApiState.Idle -> {}
            }
        }
        PullToRefreshContainer(state = pullToRefreshState, modifier = Modifier.align(Alignment.TopCenter))
    }
}

@Composable
fun MapCard(title: String, mapMode: MapMode?) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Text("当前地图: ${mapMode?.current?.map ?: "未知"}", fontWeight = FontWeight.Bold)
            Text("剩余时间: ${mapMode?.current?.remainingTimer ?: "--:--"}", color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(8.dp))
            Text("下一张图: ${mapMode?.next?.map ?: "未知"}")
        }
    }
}

// ================= 3. 猎杀者门槛页面 =================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PredatorScreen(viewModel: ApexViewModel) {
    LaunchedEffect(Unit) { viewModel.fetchPredator() }
    val uiState by viewModel.predatorState.collectAsState()
    val pullToRefreshState = rememberPullToRefreshState()

    if (pullToRefreshState.isRefreshing) {
        LaunchedEffect(true) { viewModel.fetchPredator(forceRefresh = true) }
    }
    LaunchedEffect(uiState) { if (uiState !is ApiState.Loading) pullToRefreshState.endRefresh() }

    Box(modifier = Modifier.fillMaxSize().nestedScroll(pullToRefreshState.nestedScrollConnection)) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            when (val state = uiState) {
                is ApiState.Loading -> if (!pullToRefreshState.isRefreshing) CircularProgressIndicator()
                is ApiState.Error -> Text(state.message, color = MaterialTheme.colorScheme.error)
                is ApiState.Success -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        state.data.RP?.forEach { (platform, details) ->
                            item {
                                val displayPlatform = when (platform.uppercase()) {
                                    "X1" -> "Xbox"
                                    "PS4" -> "PlayStation"
                                    "SWITCH" -> "Nintendo Switch"
                                    else -> platform
                                }
                                ElevatedCard(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(displayPlatform, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("猎杀者门槛: ${details.`val`} RP", fontWeight = FontWeight.Bold)
                                        Text("大师/猎杀总数: ${details.totalMastersAndPreds}")
                                    }
                                }
                            }
                        }
                    }
                }
                is ApiState.Idle -> {}
            }
        }
        PullToRefreshContainer(state = pullToRefreshState, modifier = Modifier.align(Alignment.TopCenter))
    }
}

// ================= 4. 服务器状态页面 =================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerStatusScreen(viewModel: ApexViewModel) {
    LaunchedEffect(Unit) { viewModel.fetchServers() }
    val uiState by viewModel.serverState.collectAsState()
    val pullToRefreshState = rememberPullToRefreshState()

    if (pullToRefreshState.isRefreshing) {
        LaunchedEffect(true) { viewModel.fetchServers(forceRefresh = true) }
    }
    LaunchedEffect(uiState) { if (uiState !is ApiState.Loading) pullToRefreshState.endRefresh() }

    Box(modifier = Modifier.fillMaxSize().nestedScroll(pullToRefreshState.nestedScrollConnection)) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            when (val state = uiState) {
                is ApiState.Loading -> if (!pullToRefreshState.isRefreshing) CircularProgressIndicator()
                is ApiState.Error -> Text(state.message, color = MaterialTheme.colorScheme.error)
                is ApiState.Success -> {
                    val originServers = state.data["Origin_login"] ?: emptyMap()
                    Text("Origin 登录服务", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.Start))
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(originServers.entries.toList()) { entry ->
                            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(entry.key, fontWeight = FontWeight.Bold)
                                    Text(entry.value.Status, color = if (entry.value.Status == "UP") Color(0xFF4CAF50) else Color.Red)
                                }
                            }
                        }
                    }
                }
                is ApiState.Idle -> {}
            }
        }
        PullToRefreshContainer(state = pullToRefreshState, modifier = Modifier.align(Alignment.TopCenter))
    }
}