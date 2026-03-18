package com.jussicodes.easyapex

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// ================= 1. 数据模型 =================

// --- 玩家数据 ---
data class PlayerResponse(
    val global: GlobalStats?,
    val realtime: RealtimeStats?,
    val legends: LegendsStats?
)

data class GlobalStats(
    val name: String, val uid: Long, val level: Int, val levelPrestige: Int?,
    val platform: String, val rank: RankStats?, val bans: BanStats?
)
data class RankStats(val rankScore: Int, val rankName: String, val rankDiv: Int)
data class BanStats(val isActive: Boolean, val last_banReason: String)
data class RealtimeStats(val lobbyState: String, val isOnline: Int, val isInGame: Int, val selectedLegend: String)
data class LegendsStats(val selected: SelectedLegend?)
data class SelectedLegend(val LegendName: String, val data: List<TrackerData>?)
data class TrackerData(val name: String, val value: Float)

// --- 🌟 新增：名字转 UID 响应数据 ---
data class UidResponse(
    val name: String?,
    val uid: String?
)

// --- 地图轮换数据 ---
data class MapRotationResponse(
    val battle_royale: MapMode?,
    val ranked: MapMode?
)
data class MapMode(
    val current: MapInfo?,
    val next: MapInfo?
)
data class MapInfo(
    val map: String,
    val remainingTimer: String?
)

// --- 猎杀者门槛数据 ---
data class PredatorResponse(
    val RP: Map<String, PredatorDetails>?
)
data class PredatorDetails(
    val foundRank: Int,
    val `val`: Int,
    val totalMastersAndPreds: Int
)

// --- 服务器状态数据 ---
data class ServerRegion(
    val Status: String,
    val ResponseTime: Int
)

// ================= 2. API 接口定义 =================
interface ApexApi {
    // 🌟 1. 通过名字查询 (兜底备用)
    @GET("bridge")
    suspend fun getPlayerProfileByName(
        @Query("auth") auth: String,
        @Query("player") player: String,
        @Query("platform") platform: String
    ): PlayerResponse

    // 🌟 2. 通过 UID 查询 (最稳定)
    @GET("bridge")
    suspend fun getPlayerProfileByUid(
        @Query("auth") auth: String,
        @Query("uid") uid: String,
        @Query("platform") platform: String
    ): PlayerResponse

    // 🌟 3. 将 EA 名字转换为底层 UID
    @GET("nametouid")
    suspend fun nameToUid(
        @Query("auth") auth: String,
        @Query("player") player: String,
        @Query("platform") platform: String
    ): UidResponse

    @GET("maprotation?version=2")
    suspend fun getMapRotation(@Query("auth") auth: String): MapRotationResponse

    @GET("predator")
    suspend fun getPredator(@Query("auth") auth: String): PredatorResponse

    @GET("servers")
    suspend fun getServerStatus(@Query("auth") auth: String): Map<String, Map<String, ServerRegion>>
}

// ================= 3. Retrofit 客户端 =================
object RetrofitClient {
    private const val BASE_URL = "https://api.mozambiquehe.re/"

    val api: ApexApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApexApi::class.java)
    }
}