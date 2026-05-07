package com.example.f1latest

import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

// --- Common ---
data class Driver(
    val driverId: String,
    val givenName: String,
    val familyName: String,
    val nationality: String,
    val permanentNumber: String? = null
)

data class Constructor(
    val constructorId: String,
    val name: String,
    val nationality: String
)

data class Circuit(
    val circuitId: String,
    val circuitName: String
)

data class TimeInfo(
    val millis: String?,
    val time: String?
)

// --- Races & Results ---
data class F1Response(
    @Json(name = "MRData") val mrData: MRData
)

data class MRData(
    @Json(name = "RaceTable") val raceTable: RaceTable?,
    @Json(name = "StandingsTable") val standingsTable: StandingsTable?
)

data class RaceTable(
    val season: String,
    val round: String?,
    @Json(name = "Races") val races: List<Race>
)

data class Race(
    val season: String,
    val round: String,
    val raceName: String,
    @Json(name = "Circuit") val circuit: Circuit,
    val date: String,
    val time: String?,
    @Json(name = "Results") val results: List<Result>? = null
)

data class Result(
    val number: String,
    val position: String,
    val positionText: String,
    val points: String,
    @Json(name = "Driver") val driver: Driver,
    @Json(name = "Constructor") val constructor: Constructor,
    val status: String,
    @Json(name = "Time") val time: TimeInfo? = null
)

// --- Standings ---
data class StandingsTable(
    val season: String,
    @Json(name = "StandingsLists") val standingsLists: List<StandingsList>
)

data class StandingsList(
    val season: String,
    val round: String,
    @Json(name = "DriverStandings") val driverStandings: List<DriverStanding>? = null,
    @Json(name = "ConstructorStandings") val constructorStandings: List<ConstructorStanding>? = null
)

data class DriverStanding(
    val position: String,
    val positionText: String,
    val points: String,
    val wins: String,
    @Json(name = "Driver") val driver: Driver,
    @Json(name = "Constructors") val constructors: List<Constructor>
)

data class ConstructorStanding(
    val position: String,
    val positionText: String,
    val points: String,
    val wins: String,
    @Json(name = "Constructor") val constructor: Constructor
)

// --- API Service ---
interface F1ApiService {
    @GET("current/last/results.json")
    suspend fun getLatestResults(): F1Response

    @GET("current/driverStandings.json")
    suspend fun getDriverStandings(): F1Response

    @GET("current/constructorStandings.json")
    suspend fun getConstructorStandings(): F1Response

    @GET("current.json")
    suspend fun getSchedule(): F1Response
    
    @GET("current/{round}/results.json")
    suspend fun getRaceResults(@Path("round") round: String): F1Response

    companion object {
        private const val BASE_URL = "https://api.jolpi.ca/ergast/f1/"

        fun create(): F1ApiService {
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
                .create(F1ApiService::class.java)
        }
    }
}
