package com.easyapex

import android.app.DownloadManager
import android.app.Application
import android.content.Context
import android.os.Environment
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.easyapex.ui.theme.AppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.content.Intent
import android.net.Uri

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

    private val API_KEY = "018fb4bd2975c737286d02b2ed3f450a"

    private val prefs = application.getSharedPreferences("apex_prefs", Context.MODE_PRIVATE)
    private val HISTORY_KEY = "search_history"
    private val THEME_KEY = "app_theme_preference"

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
    val versionName: StateFlow<String> = MutableStateFlow(currentVersionName)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    private val currentVersionName: String
        get() = try {
            getApplication<Application>().packageManager
                .getPackageInfo(getApplication<Application>().packageName, 0).versionName ?: "unknown"
        } catch (e: Exception) {
            "unknown"
        }

    init {
        loadHistory()
        loadTheme()
    }

    private fun loadTheme() {
        val savedThemeName = prefs.getString(THEME_KEY, AppTheme.DYNAMIC.name)
        _currentTheme.value = AppTheme.valueOf(savedThemeName ?: "DYNAMIC")
    }

    fun setTheme(theme: AppTheme) {
        _currentTheme.value = theme
        prefs.edit().putString(THEME_KEY, theme.name).apply()
    }

    private fun loadHistory() {
        val historyStr = prefs.getString(HISTORY_KEY, "") ?: ""
        if (historyStr.isNotEmpty()) {
            _searchHistory.value = historyStr.split(",")
        }
    }

    private fun saveToHistory(playerName: String) {
        val currentList = _searchHistory.value.toMutableList()
        if (currentList.contains(playerName)) currentList.remove(playerName)
        currentList.add(0, playerName)
        val trimmedList = currentList.take(10)
        _searchHistory.value = trimmedList
        prefs.edit().putString(HISTORY_KEY, trimmedList.joinToString(",")).apply()
    }

    fun removeHistoryItem(playerName: String) {
        val currentList = _searchHistory.value.toMutableList()
        currentList.remove(playerName)
        _searchHistory.value = currentList
        prefs.edit().putString(HISTORY_KEY, currentList.joinToString(",")).apply()
    }

    fun searchPlayer(searchInput: String) {
        if (searchInput.isBlank()) return

        _playerState.value = ApiState.Loading
        viewModelScope.launch {
            try {
                val isUid = searchInput.all { it.isDigit() }
                var finalUid = if (isUid) searchInput else ""

                if (!isUid) {
                    try {
                        val uidResponse = RetrofitClient.api.nameToUid(API_KEY, searchInput, "PC")
                        if (!uidResponse.uid.isNullOrEmpty()) {
                            finalUid = uidResponse.uid
                        }
                    } catch (e: Exception) {}
                }

                val response = if (finalUid.isNotEmpty()) {
                    RetrofitClient.api.getPlayerProfileByUid(API_KEY, finalUid, "PC")
                } else {
                    RetrofitClient.api.getPlayerProfileByName(API_KEY, searchInput, "PC")
                }

                _playerState.value = ApiState.Success(response)
                saveToHistory(searchInput)

            } catch (e: Exception) {
                _playerState.value = ApiState.Error(e.localizedMessage ?: "查询失败，请检查 ID 或网络")
            }
        }
    }

    fun fetchMapRotation(forceRefresh: Boolean = false) {
        if (!forceRefresh && _mapState.value is ApiState.Success) return
        _mapState.value = ApiState.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.getMapRotation(API_KEY)
                _mapState.value = ApiState.Success(response)
            } catch (e: Exception) {
                _mapState.value = ApiState.Error(e.localizedMessage ?: "地图加载失败")
            }
        }
    }

    fun fetchPredator(forceRefresh: Boolean = false) {
        if (!forceRefresh && _predatorState.value is ApiState.Success) return
        _predatorState.value = ApiState.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.getPredator(API_KEY)
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
                val currentVersion = currentVersionName

                if (isVersionNewer(latestVersion, currentVersion)) {
                    val apkAsset = latestRelease.assets.find { it.name.endsWith(".apk", ignoreCase = true) }
                    val downloadUrl = apkAsset?.browser_download_url ?: latestRelease.html_url
                    val fileSize = apkAsset?.size?.toLong() ?: 0L

                    _updateState.value = UpdateState.NewUpdateAvailable(
                        version = latestVersion,
                        releaseName = latestRelease.name,
                        releaseNotes = latestRelease.body.ifBlank { "暂无更新说明" },
                        downloadUrl = downloadUrl,
                        fileSize = fileSize
                    )
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

        for (i in 0 until maxOf(latestParts.size, currentParts.size)) {
            val l = if (i < latestParts.size) latestParts[i] else 0
            val c = if (i < currentParts.size) currentParts[i] else 0
            if (l > c) return true
            if (l < c) return false
        }
        return false
    }

    fun downloadAndInstallApk(downloadUrl: String) {
        _updateState.value = UpdateState.DownloadInProgress(0, 0, 0)
        val context = getApplication<Application>().applicationContext
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        val request = DownloadManager.Request(Uri.parse(downloadUrl)).apply {
            setTitle("EasyApex 更新中")
            setDescription("正在下载新版本...")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setAllowedOverMetered(true)
            setAllowedOverRoaming(true)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "EasyApex_latest.apk")
        }

        val downloadId = downloadManager.enqueue(request)

        viewModelScope.launch {
            var lastProgress = -1
            while (true) {
                val query = DownloadManager.Query().setFilterById(downloadId)
                val cursor = downloadManager.query(query) ?: break

                if (cursor.moveToFirst()) {
                    val bytesDownloaded = cursor.getInt(
                        cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                    )
                    val bytesTotal = cursor.getInt(
                        cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                    )
                    val progress = if (bytesTotal > 0) bytesDownloaded * 100 / bytesTotal else 0

                    _updateState.value = UpdateState.DownloadInProgress(progress, bytesDownloaded.toLong(), bytesTotal.toLong())

                    val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                    cursor.close()

                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        installApk(context, downloadManager, downloadId)
                        break
                    } else if (status == DownloadManager.STATUS_FAILED) {
                        _updateState.value = UpdateState.Error("下载失败，请检查存储空间和网络连接")
                        break
                    }

                    if (progress != lastProgress) lastProgress = progress
                } else {
                    break
                }

                delay(500)
            }
        }
    }

    private fun installApk(context: Context, downloadManager: DownloadManager, downloadId: Long) {
        try {
            val query = DownloadManager.Query().setFilterById(downloadId)
            val cursor = downloadManager.query(query)
            if (cursor != null && cursor.moveToFirst()) {
                val uriString = cursor.getString(
                    cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
                )
                cursor.close()
                val uri = Uri.parse(uriString)
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/vnd.android.package-archive")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            _updateState.value = UpdateState.Error("安装失败: ${e.localizedMessage ?: "未知错误"}")
        }
    }
}



