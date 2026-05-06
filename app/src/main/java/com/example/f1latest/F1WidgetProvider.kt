package com.example.f1latest

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
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
                // Set to loading state immediately
                val views = RemoteViews(context.packageName, R.layout.f1_widget)
                views.setTextViewText(R.id.tv_race_control_msg, "Refreshing...")
                appWidgetManager.updateAppWidget(appWidgetId, views)
                
                // Fetch new data
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }
    }

    companion object {
        const val ACTION_REFRESH = "com.example.f1latest.WIDGET_REFRESH"

        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.f1_widget)

            // Setup refresh button intent
            val refreshIntent = Intent(context, F1WidgetProvider::class.java).apply {
                action = ACTION_REFRESH
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context, 0, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_refresh, pendingIntent)

            // Fetch Live Data
            val apiService = OpenF1ApiService.create()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Fetch race control messages
                    val raceControlResponse = apiService.getRaceControl()
                    val latestMessage = raceControlResponse.lastOrNull()
                    
                    var bgColor = Color.parseColor("#1E1E1E") // Default dark
                    var msgText = "No recent messages"
                    
                    if (latestMessage != null) {
                        msgText = latestMessage.message ?: "Unknown Status"
                        
                        // Map flag to background color
                        bgColor = when (latestMessage.flag) {
                            "GREEN" -> Color.parseColor("#1B5E20") // Green
                            "RED" -> Color.parseColor("#B71C1C") // Red
                            "YELLOW", "DOUBLE YELLOW", "VSC" -> Color.parseColor("#F57F17") // Yellow
                            "SAFETY CAR" -> Color.parseColor("#E65100") // Orange
                            else -> {
                                // Check if sector yellow
                                if (latestMessage.scope == "Sector" && latestMessage.flag?.contains("YELLOW") == true) {
                                    Color.parseColor("#827717") // Mixed yellow
                                } else {
                                    Color.parseColor("#1E1E1E") // Default
                                }
                            }
                        }
                    }
                    
                    // Fetch intervals for Max (1), Charles (16), Kimi (12)
                    val intervals = apiService.getIntervals()
                    
                    // Default values
                    var maxGap = "--"
                    var lecGap = "--"
                    var antGap = "--"
                    
                    // Map intervals
                    intervals.forEach { interval ->
                        val gapStr = if (interval.gapToLeader == 0.0) "Leader" else "+${interval.gapToLeader}s"
                        when (interval.driverNumber) {
                            1 -> maxGap = gapStr
                            16 -> lecGap = gapStr
                            12 -> antGap = gapStr
                        }
                    }

                    // Update UI on main thread (AppWidgetManager handles this internally but good practice to be mindful)
                    views.setInt(R.id.widget_container, "setBackgroundColor", bgColor)
                    views.setTextViewText(R.id.tv_race_control_msg, msgText)
                    views.setTextViewText(R.id.tv_driver_1_gap, maxGap)
                    views.setTextViewText(R.id.tv_driver_16_gap, lecGap)
                    views.setTextViewText(R.id.tv_driver_12_gap, antGap)

                    appWidgetManager.updateAppWidget(appWidgetId, views)
                } catch (e: Exception) {
                    views.setTextViewText(R.id.tv_race_control_msg, "Error: ${e.localizedMessage}")
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            }
        }
    }
}
