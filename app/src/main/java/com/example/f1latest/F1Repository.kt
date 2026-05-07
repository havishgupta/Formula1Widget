package com.example.f1latest

import java.net.UnknownHostException

class F1Repository {
    private val ergastPrimary = F1ApiService.create(F1ApiService.getPrimaryUrl())
    private val ergastFallback = F1ApiService.create(F1ApiService.getFallbackUrl())
    private val openF1 = OpenF1ApiService.create()

    suspend fun getLatestResults(): Race? {
        return try {
            ergastPrimary.getLatestResults().mrData.raceTable?.races?.firstOrNull()
        } catch (e: UnknownHostException) {
            ergastFallback.getLatestResults().mrData.raceTable?.races?.firstOrNull()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getLatestQualifying(): Race? {
        return try {
            ergastPrimary.getLatestQualifying().mrData.raceTable?.races?.firstOrNull()
        } catch (e: UnknownHostException) {
            ergastFallback.getLatestQualifying().mrData.raceTable?.races?.firstOrNull()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getDriverStandings(): List<DriverStanding>? {
        return try {
            ergastPrimary.getDriverStandings().mrData.standingsTable?.standingsLists?.firstOrNull()?.driverStandings
        } catch (e: UnknownHostException) {
            ergastFallback.getDriverStandings().mrData.standingsTable?.standingsLists?.firstOrNull()?.driverStandings
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getConstructorStandings(): List<ConstructorStanding>? {
        return try {
            ergastPrimary.getConstructorStandings().mrData.standingsTable?.standingsLists?.firstOrNull()?.constructorStandings
        } catch (e: UnknownHostException) {
            ergastFallback.getConstructorStandings().mrData.standingsTable?.standingsLists?.firstOrNull()?.constructorStandings
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getSchedule(): List<Race>? {
        return try {
            ergastPrimary.getSchedule().mrData.raceTable?.races
        } catch (e: UnknownHostException) {
            ergastFallback.getSchedule().mrData.raceTable?.races
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getRaceResults(round: String): Race? {
        return try {
            ergastPrimary.getRaceResults(round).mrData.raceTable?.races?.firstOrNull()
        } catch (e: UnknownHostException) {
            ergastFallback.getRaceResults(round).mrData.raceTable?.races?.firstOrNull()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getLatestSession(): Session? {
        return try {
            openF1.getSessions("latest").firstOrNull()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getLaps(sessionKey: Int): List<Lap> {
        return try {
            openF1.getLaps(sessionKey)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getDrivers(sessionKey: Int): List<OpenF1Driver> {
        return try {
            openF1.getDrivers(sessionKey)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
