package com.easyapex

import android.app.DownloadManager
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.easyapex.ui.theme.AppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

sealed class ApiState<out T> {
    object Idle : ApiState<Nothing>()
    object Loading : ApiState<Nothing>()
    data class Success<T>(val data: T) : ApiState<T>()
    data class Error(val message: String) : ApiState<Nothing>()
}

sealed class UpdateState {
    object Idle : UpdateState()
    object Checking : UpdateState()
    data class NewUpdateAvailable(
        val version: String,
        val releaseName: String,
        val releaseNotes: String,
        val downloadUrl: String,
        val fileSize: Long
    ) : UpdateState()
    data class DownloadInProgress(val progress: Int, val downloadedBytes: Long, val totalBytes: Long) : UpdateState()
    object UpToDate : UpdateState()
    data class Error(val message: String) : UpdateState()
}

class ApexViewModel(application: Application) : AndroidViewModel(application) {

    private val apiKey = "018fb4bd2975c737286d02b2ed3f450a"

    private val prefs = application.getSharedPreferences("apex_prefs", Context.MODE_PRIVATE)
    private val historyKey = "search_history"
    private val themeKey = "app_theme_preference"

    private val _currentTheme = MutableStateFlow(AppTheme.DYNAMIC)
    val currentTheme: StateFlow<AppTheme> = _currentTheme.asStateFlow()

    private val _searchHistory = MutableStateFlow<List<String>>(emptyList())
    val searchHistory: StateFlow<List<String>> = _searchHistory.asStateFlow()

    private val _playerState = MutableStateFlow<ApiState<PlayerResponse>>(ApiState.Idle)
    val playerState: StateFlow<ApiState<PlayerResponse>> = _playerState.asStateFlow()

    private val _mapState = MutableStateFlow<ApiState<MapRotationResponse>>(ApiState.Idle)
    val mapState: StateFlow<ApiState<MapRotationResponse>> = _mapState.asStateFlow()

    private val _predatorState = MutableStateFlow<ApiState<PredatorResponse>>(ApiState.Idle)
    val predatorState: StateFlow<ApiState<PredatorResponse>> = _predatorState.asStateFlow()

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    val versionName: StateFlow<String> = MutableStateFlow(getCurrentVersionName())

    init {
        loadHistory()
        loadTheme()
    }

    private fun getCurrentVersionName(): String {
        val context = getApplication<Application>()
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        return packageInfo.versionName ?: "1.0.6"
    }

    private fun loadTheme() {
        val savedThemeName = prefs.getString(themeKey, AppTheme.DYNAMIC.name)
        _currentTheme.value = AppTheme.valueOf(savedThemeName ?: AppTheme.DYNAMIC.name)
    }

    fun setTheme(theme: AppTheme) {
        _currentTheme.value = theme
        prefs.edit().putString(themeKey, theme.name).apply()
    }

    private fun loadHistory() {
        val historyStr = prefs.getString(historyKey, "") ?: ""
        _searchHistory.value = if (historyStr.isBlank()) emptyList() else historyStr.split(",")
    }

    private fun saveToHistory(playerName: String) {
        val currentList = _searchHistory.value.toMutableList()
        currentList.remove(playerName)
        currentList.add(0, playerName)
        if (currentList.size > 10) {
            currentList.removeAt(currentList.lastIndex)
        }
        _searchHistory.value = currentList
        prefs.edit().putString(historyKey, currentList.joinToString(",")).apply()
    }

    fun removeHistoryItem(playerName: String) {
        val updatedList = _searchHistory.value.filterNot { it == playerName }
        _searchHistory.value = updatedList
        prefs.edit().putString(historyKey, updatedList.joinToString(",")).apply()
    }

    fun searchPlayer(playerInput: String, platform: String = "PC") {
        fetchPlayer(playerInput, platform, forceRefresh = true)
    }

    fun fetchPlayer(playerInput: String, platform: String = "PC", forceRefresh: Boolean = false) {
        if (!forceRefresh && _playerState.value is ApiState.Success) return
        _playerState.value = ApiState.Loading
        viewModelScope.launch {
            try {
                val input = playerInput.trim()
                val isUid = input.all { it.isDigit() }
                val response = if (isUid) {
                    RetrofitClient.api.getPlayerProfileByUid(apiKey, input, platform)
                } else {
                    val uidResponse = RetrofitClient.api.nameToUid(apiKey, input, platform)
                    val uid = uidResponse.uid
                    if (uid.isNullOrBlank()) {
                        RetrofitClient.api.getPlayerProfileByName(apiKey, input, platform)
                    } else {
                        RetrofitClient.api.getPlayerProfileByUid(apiKey, uid, platform)
                    }
                }
                _playerState.value = ApiState.Success(response)
                saveToHistory(input)
            } catch (e: Exception) {
                _playerState.value = ApiState.Error(e.localizedMessage ?: "玩家查询失败")
            }
        }
    }

    fun fetchMapRotation(forceRefresh: Boolean = false) {
        if (!forceRefresh && _mapState.value is ApiState.Success) return
        _mapState.value = ApiState.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.getMapRotation(apiKey)
                _mapState.value = ApiState.Success(response)
            } catch (e: Exception) {
                _mapState.value = ApiState.Error(e.localizedMessage ?: "地图数据加载失败")
            }
        }
    }

    fun fetchPredator(forceRefresh: Boolean = false) {
        if (!forceRefresh && _predatorState.value is ApiState.Success) return
        _predatorState.value = ApiState.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.getPredator(apiKey)
                _predatorState.value = ApiState.Success(response)
            } catch (e: Exception) {
                _predatorState.value = ApiState.Error(e.localizedMessage ?: "门槛数据加载失败")
            }
        }
    }

    fun checkForUpdate() {
        viewModelScope.launch {
            _updateState.value = UpdateState.Checking
            try {
                val latestRelease = GitHubApiClient.api.getLatestRelease()
                val latestVersion = latestRelease.tag_name.removePrefix("v")
                val currentVersion = getCurrentVersionName().removePrefix("v")
                if (isVersionNewer(latestVersion, currentVersion)) {
                    val apkAsset = latestRelease.assets.find { it.name.endsWith(".apk", ignoreCase = true) }
                    if (apkAsset != null) {
                        _updateState.value = UpdateState.NewUpdateAvailable(
                            version = latestVersion,
                            releaseName = latestRelease.name.ifBlank { latestRelease.tag_name },
                            releaseNotes = latestRelease.body.ifBlank { "暂无更新说明" },
                            downloadUrl = apkAsset.browser_download_url,
                            fileSize = apkAsset.size.toLong()
                        )
                    } else {
                        _updateState.value = UpdateState.Error("发现新版本，但当前 Release 未附带 APK 安装包")
                    }
                } else {
                    _updateState.value = UpdateState.UpToDate
                }
            } catch (e: Exception) {
                _updateState.value = UpdateState.Error(e.localizedMessage ?: "检查更新失败，请检查网络连接")
            }
        }
    }

    private fun isVersionNewer(latest: String, current: String): Boolean {
        val latestParts = latest.split(".").map { it.toIntOrNull() ?: 0 }
        val currentParts = current.split(".").map { it.toIntOrNull() ?: 0 }
        val maxSize = maxOf(latestParts.size, currentParts.size)
        for (index in 0 until maxSize) {
            val latestPart = latestParts.getOrElse(index) { 0 }
            val currentPart = currentParts.getOrElse(index) { 0 }
            if (latestPart > currentPart) return true
            if (latestPart < currentPart) return false
        }
        return false
    }

    fun downloadAndInstallApk(downloadUrl: String) {
        val context = getApplication<Application>().applicationContext
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !context.packageManager.canRequestPackageInstalls()) {
            _updateState.value = UpdateState.Error("请先允许安装未知来源应用后再重试")
            val settingsIntent = Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${context.packageName}")
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(settingsIntent)
            return
        }

        _updateState.value = UpdateState.DownloadInProgress(0, 0, 0)
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(Uri.parse(downloadUrl)).apply {
            setTitle("EasyApex 更新中")
            setDescription("正在下载新版本...")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setAllowedOverMetered(true)
            setAllowedOverRoaming(true)
            setMimeType("application/vnd.android.package-archive")
            setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                "EasyApex_v${getCurrentVersionName()}.apk"
            )
        }
        val downloadId = downloadManager.enqueue(request)

        viewModelScope.launch(Dispatchers.IO) {
            var keepPolling = true
            while (keepPolling) {
                val cursor = downloadManager.query(DownloadManager.Query().setFilterById(downloadId)) ?: break
                cursor.use {
                    if (!it.moveToFirst()) {
                        _updateState.value = UpdateState.Error("下载状态读取失败")
                        keepPolling = false
                        return@use
                    }

                    val bytesDownloaded = it.getLong(it.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                    val bytesTotal = it.getLong(it.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                    val progress = if (bytesTotal > 0) (bytesDownloaded * 100 / bytesTotal).toInt() else 0
                    _updateState.value = UpdateState.DownloadInProgress(progress, bytesDownloaded, bytesTotal)

                    when (it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))) {
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            installApk(context, downloadManager, downloadId)
                            keepPolling = false
                        }
                        DownloadManager.STATUS_FAILED -> {
                            _updateState.value = UpdateState.Error("下载失败，请检查存储空间和网络连接")
                            keepPolling = false
                        }
                    }
                }
                if (keepPolling) {
                    delay(500)
                }
            }
        }
    }

    private fun installApk(context: Context, downloadManager: DownloadManager, downloadId: Long) {
        try {
            val apkUri = downloadManager.getUriForDownloadedFile(downloadId) ?: resolveDownloadedApkUri(context, downloadManager, downloadId)
            if (apkUri == null) {
                _updateState.value = UpdateState.Error("安装失败：未找到已下载的安装包")
                return
            }

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            _updateState.value = UpdateState.Error("安装失败：${e.localizedMessage ?: "未知错误"}")
        }
    }

    private fun resolveDownloadedApkUri(
        context: Context,
        downloadManager: DownloadManager,
        downloadId: Long
    ): Uri? {
        val cursor = downloadManager.query(DownloadManager.Query().setFilterById(downloadId)) ?: return null
        cursor.use {
            if (!it.moveToFirst()) return null
            val localUri = it.getString(it.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI)) ?: return null
            val filePath = Uri.parse(localUri).path ?: return null
            return FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                File(filePath)
            )
        }
    }
}
