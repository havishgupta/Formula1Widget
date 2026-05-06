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
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class F1WidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            setInitialViews(context, appWidgetManager, appWidgetId)
        }
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

                        if (resultData.error != null) {
                            views.setTextViewText(R.id.tv_widget_title, "Error: ${resultData.error}")
                        } else {
                            views.setTextViewText(R.id.tv_widget_title, resultData.raceName)
                            val tvIds = listOf(
                                R.id.tv_driver_1, R.id.tv_driver_2, R.id.tv_driver_3,
                                R.id.tv_driver_4, R.id.tv_driver_5, R.id.tv_driver_6
                            )
                            for (id in tvIds) views.setTextViewText(id, "")
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

    private fun fetchF1Data(): WidgetData {
        try {
            val url = URL("https://api.jolpi.ca/ergast/f1/current/last/results.json")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            if (connection.responseCode != 200) {
                return WidgetData(error = "HTTP ${connection.responseCode}")
            }

            val responseStr = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(responseStr)
            
            val mrData = json.getJSONObject("MRData")
            val raceTable = mrData.getJSONObject("RaceTable")
            val races = raceTable.getJSONArray("Races")
            if (races.length() == 0) return WidgetData(error = "No race data")
            
            val race = races.getJSONObject(0)
            val raceName = race.getString("raceName")
            val results = race.getJSONArray("Results")
            
            val drivers = mutableListOf<String>()
            var maxFound = false
            var maxDriverStr = ""
            
            for (i in 0 until results.length()) {
                val result = results.getJSONObject(i)
                val position = result.getString("position")
                val points = result.getString("points")
                val driverObj = result.getJSONObject("Driver")
                val familyName = driverObj.getString("familyName")
                val givenName = driverObj.getString("givenName")
                
                val driverStr = "$position. $givenName $familyName - $points pts"
                
                if (i < 5) {
                    drivers.add(driverStr)
                }
                
                if (familyName == "Verstappen") {
                    maxFound = true
                    maxDriverStr = driverStr
                }
            }
            
            if (!maxFound && maxDriverStr.isNotEmpty() && !drivers.contains(maxDriverStr)) {
                drivers.add(maxDriverStr)
            }
            
            return WidgetData(raceName = raceName, drivers = drivers)
        } catch (e: Exception) {
            return WidgetData(error = e.localizedMessage ?: "Unknown Error")
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
