new_dialog = '''@Composable
fun SettingsDialog(
    currentTheme: AppTheme,
    onThemeChange: (AppTheme) -> Unit,
    onDismiss: () -> Unit,
    viewModel: ApexViewModel
) {
    val context = LocalContext.current
    val updateState by viewModel.updateState.collectAsState()

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
                    text = "一款轻量、便捷的《Apex 英雄》非官方数据查询工具。支持 Origin ID 及底层 UID 智能查询，提供最新地图轮换与猎杀者门槛数据。\\\\n\\\\n数据来源: apexlegendsstatus.com",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                Text("版本更新", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))

                when (val state = updateState) {
                    is UpdateState.Idle -> {
                        Text("点击检查更新按钮查看是否有新版本", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    is UpdateState.Checking -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("正在检查更新...", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    is UpdateState.NewUpdateAvailable -> {
                        Column {
                            Text(
                                text = "发现新版本: v",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = state.releaseName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = state.releaseNotes,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 4
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            val sizeStr = if (state.fileSize > 0) {
                                val mb = state.fileSize / (1024.0 * 1024.0)
                                String.format("大小: %.1f MB", mb)
                            } else "未知大小"
                            Text(text = sizeStr, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                    is UpdateState.DownloadInProgress -> {
                        Column {
                            Text(
                                text = "下载中: %",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = state.progress / 100f,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            val downloadedMB = state.downloadedBytes / (1024.0 * 1024.0)
                            val totalMB = if (state.totalBytes > 0) state.totalBytes / (1024.0 * 1024.0) else 0.0
                            Text(
                                text = String.format("%.1f MB / %.1f MB", downloadedMB, totalMB),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                    is UpdateState.UpToDate -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("您已是最新版本", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    is UpdateState.Error -> {
                        Text(state.message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                    }
                    else -> {}
                }
            }
        },
        confirmButton = {
            when (updateState) {
                is UpdateState.NewUpdateAvailable -> {
                    Button(onClick = {
                        viewModel.downloadAndInstallApk((updateState as UpdateState.NewUpdateAvailable).downloadUrl)
                    }) {
                        Text("立即更新")
                    }
                }
                is UpdateState.DownloadInProgress -> {
                    Button(enabled = false) {
                        Text("下载中...")
                    }
                }
                else -> {
                    Button(onClick = { viewModel.checkForUpdate() }) {
                        Text("检查更新")
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("关闭") }
        }
    )
}

'''

with open(r'F:\EasyApex\app\src\main\java\com\easyapex\ApexStatsScreen.kt', 'r', encoding='utf-8') as f:
    lines = f.readlines()

result = lines[:77] + [new_dialog] + lines[77:]

with open(r'F:\EasyApex\app\src\main\java\com\easyapex\ApexStatsScreen.kt', 'w', encoding='utf-8') as f:
    f.writelines(result)

content = new_dialog + ''.join(lines[77:])
o = content.count('{')
c = content.count('}')
print('Braces: open=' + str(o) + ' close=' + str(c) + ' diff=' + str(o-c))
print('Done')