package com.termux.wayland.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.RemoteViews
import com.termux.wayland.R

class DockWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
            updateWidgetLayout(context, appWidgetManager, appWidgetId, options)
        }
    }

    override fun onAppWidgetOptionsChanged(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, newOptions: Bundle) {
        // Appelé dès que l'utilisateur redimensionne le widget sur son launcher
        updateWidgetLayout(context, appWidgetManager, appWidgetId, newOptions)
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
    }

    private fun updateWidgetLayout(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, options: Bundle) {
        val views = RemoteViews(context.packageName, R.layout.dock_widget_layout)

        // Récupérer les dimensions actuelles en dp
        val minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
        val minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)

        // Si la hauteur est supérieure à la largeur, on bascule en mode colonne (Vertical)
        if (minHeight > minWidth) {
            views.setInt(R.id.dock_container, "setOrientation", LinearLayout.VERTICAL)
        } else {
            views.setInt(R.id.dock_container, "setOrientation", LinearLayout.HORIZONTAL)
        }

        // Liaison des clics sur les boutons
        views.setOnClickPendingIntent(R.id.btn_dock_terminal, createPendingAction(context, appWidgetId, "LAUNCH_TERMINAL"))
        views.setOnClickPendingIntent(R.id.btn_dock_wayland, createPendingAction(context, appWidgetId, "START_WAYLAND"))
        views.setOnClickPendingIntent(R.id.btn_dock_app, createPendingAction(context, appWidgetId, "LAUNCH_APP"))
        views.setOnClickPendingIntent(R.id.btn_dock_stop, createPendingAction(context, appWidgetId, "STOP_ALL"))

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun createPendingAction(context: Context, widgetId: Int, actionName: String): PendingIntent {
        val intent = Intent(context, WidgetReceiver::class.java).apply {
            action = "com.termux.wayland.ACTION_DOCK_CLICK"
            putExtra("WIDGET_ACTION", actionName)
        }
        return PendingIntent.getBroadcast(
            context,
            actionName.hashCode() + widgetId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
