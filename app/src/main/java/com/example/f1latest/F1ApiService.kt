package com.example.f1latest

import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET

data class F1Response(
    @Json(name = "MRData") val mrData: MRData
)

data class MRData(
    @Json(name = "RaceTable") val raceTable: RaceTable
)

data class RaceTable(
    val season: String,
    val round: String,
    @Json(name = "Races") val races: List<Race>
)

data class Race(
    val season: String,
    val round: String,
    val raceName: String,
    @Json(name = "Circuit") val circuit: Circuit,
    val date: String,
    val time: String?,
    @Json(name = "Results") val results: List<Result>
)

data class Circuit(
    val circuitId: String,
    val circuitName: String
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

data class Driver(
    val driverId: String,
    val givenName: String,
    val familyName: String,
    val nationality: String
)

data class Constructor(
    val constructorId: String,
    val name: String,
    val nationality: String
)

data class TimeInfo(
    val millis: String?,
    val time: String?
)

interface F1ApiService {
    @GET("current/last/results.json")
    suspend fun getLatestResults(): F1Response

    companion object {
        private const val BASE_URL = "https://api.jolpi.ca/ergast/f1/"

        fun create(): F1ApiService {
            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(F1ApiService::class.java)
        }
    }
}
