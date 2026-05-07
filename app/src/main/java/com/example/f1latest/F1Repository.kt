package com.example.f1latest

class F1Repository {
    private val ergastPrimary = F1ApiService.create(F1ApiService.getPrimaryUrl())
    private val ergastFallback = F1ApiService.create(F1ApiService.getFallbackUrl())
    private val openF1 = OpenF1ApiService.create()

    private suspend fun <T> withFallback(
        primaryCall: suspend () -> T,
        fallbackCall: suspend () -> T
    ): T? {
        return try {
            primaryCall()
        } catch (e: Exception) {
            try {
                fallbackCall()
            } catch (e2: Exception) {
                null
            }
        }
    }

    suspend fun getLatestResults(): Race? {
        return withFallback(
            { ergastPrimary.getLatestResults().mrData.raceTable?.races?.firstOrNull() },
            { ergastFallback.getLatestResults().mrData.raceTable?.races?.firstOrNull() }
        )
    }

    suspend fun getLatestQualifying(): Race? {
        return withFallback(
            { ergastPrimary.getLatestQualifying().mrData.raceTable?.races?.firstOrNull() },
            { ergastFallback.getLatestQualifying().mrData.raceTable?.races?.firstOrNull() }
        )
    }

    suspend fun getDriverStandings(): List<DriverStanding>? {
        return withFallback(
            { ergastPrimary.getDriverStandings().mrData.standingsTable?.standingsLists?.firstOrNull()?.driverStandings },
            { ergastFallback.getDriverStandings().mrData.standingsTable?.standingsLists?.firstOrNull()?.driverStandings }
        )
    }

    suspend fun getConstructorStandings(): List<ConstructorStanding>? {
        return withFallback(
            { ergastPrimary.getConstructorStandings().mrData.standingsTable?.standingsLists?.firstOrNull()?.constructorStandings },
            { ergastFallback.getConstructorStandings().mrData.standingsTable?.standingsLists?.firstOrNull()?.constructorStandings }
        )
    }

    suspend fun getSchedule(): List<Race>? {
        return withFallback(
            { ergastPrimary.getSchedule().mrData.raceTable?.races },
            { ergastFallback.getSchedule().mrData.raceTable?.races }
        )
    }

    suspend fun getRaceResults(round: String): Race? {
        return withFallback(
            { ergastPrimary.getRaceResults(round).mrData.raceTable?.races?.firstOrNull() },
            { ergastFallback.getRaceResults(round).mrData.raceTable?.races?.firstOrNull() }
        )
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
