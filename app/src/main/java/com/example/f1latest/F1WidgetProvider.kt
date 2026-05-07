package com.example.f1latest

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class F1WidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            setInitialViews(context, appWidgetManager, appWidgetId)
        }
        val refreshIntent = Intent(context, F1WidgetProvider::class.java).apply { action = ACTION_REFRESH }
        context.sendBroadcast(refreshIntent)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent) // Always call this on the main thread
        
        if (intent.action == ACTION_REFRESH || intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val appWidgetManager = AppWidgetManager.getInstance(context)
                    val componentName = ComponentName(context, F1WidgetProvider::class.java)
                    val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
                    
                    val resultData = fetchF1Data()
                    
                    for (appWidgetId in appWidgetIds) {
                        val views = RemoteViews(context.packageName, R.layout.f1_widget)
                        
                        val refreshIntent = Intent(context, F1WidgetProvider::class.java).apply { action = ACTION_REFRESH }
                        val pendingIntent = PendingIntent.getBroadcast(
                            context, 0, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        views.setOnClickPendingIntent(R.id.btn_refresh, pendingIntent)

                        val tvIds = listOf(
                            R.id.tv_driver_1, R.id.tv_driver_2, R.id.tv_driver_3,
                            R.id.tv_driver_4, R.id.tv_driver_5, R.id.tv_driver_6
                        )
                        for (id in tvIds) views.setTextViewText(id, "")

                        if (resultData.error != null) {
                            views.setTextViewText(R.id.tv_widget_title, "Error: ${resultData.error}")
                        } else {
                            views.setTextViewText(R.id.tv_widget_title, resultData.raceName)
                            for (i in resultData.drivers.indices) {
                                if (i < tvIds.size) {
                                    views.setTextViewText(tvIds[i], resultData.drivers[i])
                                }
                            }
                        }
                        appWidgetManager.updateAppWidget(appWidgetId, views)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

    private fun setInitialViews(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.f1_widget)
        views.setTextViewText(R.id.tv_widget_title, "F1 Latest...")
        
        val refreshIntent = Intent(context, F1WidgetProvider::class.java).apply {
            action = ACTION_REFRESH
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.btn_refresh, pendingIntent)
        
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private suspend fun fetchF1Data(): WidgetData {
        return try {
            val openF1 = OpenF1ApiService.create()
            val ergast = F1ApiService.create()
            
            val latestSession = openF1.getSessions("latest").firstOrNull()
            
            // If it's a practice session, we use OpenF1 for real-time practice results
            if (latestSession != null && latestSession.sessionType.contains("Practice", ignoreCase = true)) {
                val laps = openF1.getLaps(latestSession.sessionKey)
                val driversInfo = openF1.getDrivers(latestSession.sessionKey).associateBy { it.driverNumber }
                
                val fastestLaps = laps
                    .filter { it.lapDuration != null && it.lapDuration > 0 && !it.isPitOutLap }
                    .groupBy { it.driverNumber }
                    .mapValues { it.value.minOf { lap -> lap.lapDuration!! } }
                    .toList()
                    .sortedBy { it.second }
                    .take(5)
                
                if (fastestLaps.isNotEmpty()) {
                    val drivers = fastestLaps.mapIndexed { i, pair ->
                        val info = driversInfo[pair.first]
                        val time = String.format("%.3f", pair.second)
                        "${i + 1}. ${info?.fullName ?: "Driver ${pair.first}"} - $time"
                    }
                    return WidgetData(raceName = "${latestSession.circuitName} - ${latestSession.sessionName}", drivers = drivers)
                }
            }

            // Fallback to Ergast for Qualifying and Race results
            val raceResponse = ergast.getLatestResults()
            val qualResponse = ergast.getLatestQualifying()
            
            val latestRace = raceResponse.mrData.raceTable?.races?.firstOrNull()
            val latestQual = qualResponse.mrData.raceTable?.races?.firstOrNull()
            
            val showQual = if (latestRace != null && latestQual != null) {
                val raceRound = latestRace.round.toIntOrNull() ?: 0
                val qualRound = latestQual.round.toIntOrNull() ?: 0
                qualRound > raceRound
            } else {
                latestRace == null
            }

            if (showQual && latestQual != null) {
                val drivers = latestQual.qualifyingResults?.take(5)?.map {
                    "${it.position}. ${it.driver.givenName} ${it.driver.familyName}"
                } ?: emptyList()
                WidgetData(raceName = "${latestQual.raceName} (Quali)", drivers = drivers)
            } else if (latestRace != null) {
                val drivers = latestRace.results?.take(5)?.map {
                    "${it.position}. ${it.driver.givenName} ${it.driver.familyName} - ${it.points} pts"
                } ?: emptyList()
                WidgetData(raceName = latestRace.raceName, drivers = drivers)
            } else {
                WidgetData(error = "No session data")
            }
        } catch (e: Exception) {
            WidgetData(error = e.localizedMessage ?: "Unknown Error")
        }
    }

    companion object {
        const val ACTION_REFRESH = "com.example.f1latest.WIDGET_REFRESH"
    }
}

data class WidgetData(
    val raceName: String = "",
    val drivers: List<String> = emptyList(),
    val error: String? = null
)
