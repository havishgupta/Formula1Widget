package com.example.f1latest

import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

data class RaceControlMessage(
    @Json(name = "category") val category: String?,
    @Json(name = "flag") val flag: String?,
    @Json(name = "message") val message: String?,
    @Json(name = "scope") val scope: String?,
    @Json(name = "sector") val sector: Int?
)

data class IntervalData(
    @Json(name = "driver_number") val driverNumber: Int,
    @Json(name = "gap_to_leader") val gapToLeader: Double?,
    @Json(name = "interval") val interval: Double?
)

interface OpenF1ApiService {
    @GET("race_control")
    suspend fun getRaceControl(
        @Query("session_key") sessionKey: String = "latest"
    ): List<RaceControlMessage>

    @GET("intervals")
    suspend fun getIntervals(
        @Query("session_key") sessionKey: String = "latest",
        @Query("driver_number") drivers: List<Int> = listOf(1, 16, 12)
    ): List<IntervalData>

    companion object {
        private const val BASE_URL = "https://api.openf1.org/v1/"

        fun create(): OpenF1ApiService {
            val client = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .header("User-Agent", "F1LatestApp/1.0")
                        .build()
                    chain.proceed(request)
                }
                .build()

            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(OpenF1ApiService::class.java)
        }
    }
}
