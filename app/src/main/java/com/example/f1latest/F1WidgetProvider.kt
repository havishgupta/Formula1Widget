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
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, F1WidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            
            for (appWidgetId in appWidgetIds) {
                val views = RemoteViews(context.packageName, R.layout.f1_widget)
                views.setTextViewText(R.id.tv_widget_title, "Refreshing...")
                appWidgetManager.updateAppWidget(appWidgetId, views)
                
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }
    }

    companion object {
        const val ACTION_REFRESH = "com.example.f1latest.WIDGET_REFRESH"

        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.f1_widget)

            val refreshIntent = Intent(context, F1WidgetProvider::class.java).apply {
                action = ACTION_REFRESH
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context, 0, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_refresh, pendingIntent)

            val apiService = F1ApiService.create()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = apiService.getLatestResults()
                    val races = response.mrData.raceTable?.races
                    
                    if (!races.isNullOrEmpty()) {
                        val race = races.first()
                        val results = race.results ?: emptyList()
                        
                        val displayDrivers = mutableListOf<Result>()
                        displayDrivers.addAll(results.take(5))
                        
                        val maxVerstappen = results.find { it.driver.familyName == "Verstappen" }
                        if (maxVerstappen != null && !displayDrivers.contains(maxVerstappen)) {
                            displayDrivers.add(maxVerstappen)
                        }

                        val tvIds = listOf(
                            R.id.tv_driver_1, R.id.tv_driver_2, R.id.tv_driver_3,
                            R.id.tv_driver_4, R.id.tv_driver_5, R.id.tv_driver_6
                        )

                        // Clear all
                        for (id in tvIds) {
                            views.setTextViewText(id, "")
                        }

                        // Populate available
                        for (i in displayDrivers.indices) {
                            if (i < tvIds.size) {
                                val driver = displayDrivers[i]
                                val text = "${driver.position}. ${driver.driver.givenName} ${driver.driver.familyName} - ${driver.points} pts"
                                views.setTextViewText(tvIds[i], text)
                            }
                        }

                        views.setTextViewText(R.id.tv_widget_title, race.raceName)
                    } else {
                        views.setTextViewText(R.id.tv_widget_title, "No Data Found")
                    }
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                } catch (e: Exception) {
                    views.setTextViewText(R.id.tv_widget_title, "Error Loading")
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            }
        }
    }
}
