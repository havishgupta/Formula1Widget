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

    private suspend fun fetchF1Data(): WidgetData {
        return try {
            val response = F1ApiService.create().getLatestResults()
            val races = response.mrData.raceTable?.races
            if (races.isNullOrEmpty()) return WidgetData(error = "No race data")
            
            val race = races[0]
            val raceName = race.raceName
            val results = race.results ?: emptyList()
            
            val drivers = results.take(5).map { result ->
                "${result.position}. ${result.driver.givenName} ${result.driver.familyName} - ${result.points} pts"
            }
            
            WidgetData(raceName = raceName, drivers = drivers)
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
