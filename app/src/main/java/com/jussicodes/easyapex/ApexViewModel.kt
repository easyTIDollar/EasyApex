package com.jussicodes.easyapex

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jussicodes.easyapex.ui.theme.AppTheme // 🌟 修复 AppTheme 报错
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ApiState<out T> {
    object Idle : ApiState<Nothing>()
    object Loading : ApiState<Nothing>()
    data class Success<T>(val data: T) : ApiState<T>()
    data class Error(val message: String) : ApiState<Nothing>()
}

class ApexViewModel(application: Application) : AndroidViewModel(application) {

    // 记得在公开源码前把这个 Key 拿掉或者弄成配置文件！
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

    private val _serverState = MutableStateFlow<ApiState<Map<String, Map<String, ServerRegion>>>>(ApiState.Idle)
    val serverState: StateFlow<ApiState<Map<String, Map<String, ServerRegion>>>> = _serverState.asStateFlow()

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

    fun fetchServers(forceRefresh: Boolean = false) {
        if (!forceRefresh && _serverState.value is ApiState.Success) return
        _serverState.value = ApiState.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.getServerStatus(API_KEY)
                _serverState.value = ApiState.Success(response)
            } catch (e: Exception) {
                _serverState.value = ApiState.Error(e.localizedMessage ?: "服务器状态加载失败")
            }
        }
    }
}