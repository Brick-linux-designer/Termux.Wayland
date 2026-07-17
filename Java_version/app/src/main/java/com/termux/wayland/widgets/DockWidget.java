package com.termux.wayland.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import com.termux.wayland.R;

public class DockWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
            updateWidgetLayout(context, appWidgetManager, appWidgetId, options);
        }
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        // Appelé dès que l'utilisateur redimensionne le widget sur son launcher
        updateWidgetLayout(context, appWidgetManager, appWidgetId, newOptions);
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }

    private void updateWidgetLayout(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle options) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.dock_widget_layout);

        // Récupérer les dimensions actuelles en dp
        int minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
        int minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);

        // Si la hauteur est supérieure à la largeur, on bascule en mode colonne (Vertical)
        if (minHeight > minWidth) {
            views.setInt(R.id.dock_container, "setOrientation", LinearLayout.VERTICAL);
        } else {
            views.setInt(R.id.dock_container, "setOrientation", LinearLayout.HORIZONTAL);
        }

        // Liaison des clics sur les boutons
        views.setOnClickPendingIntent(R.id.btn_dock_terminal, createPendingAction(context, appWidgetId, "LAUNCH_TERMINAL"));
        views.setOnClickPendingIntent(R.id.btn_dock_wayland, createPendingAction(context, appWidgetId, "START_WAYLAND"));
        views.setOnClickPendingIntent(R.id.btn_dock_app, createPendingAction(context, appWidgetId, "LAUNCH_APP"));
        views.setOnClickPendingIntent(R.id.btn_dock_stop, createPendingAction(context, appWidgetId, "STOP_ALL"));

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private PendingIntent createPendingAction(Context context, int widgetId, String actionName) {
        Intent intent = new Intent(context, WidgetReceiver.class);
        intent.setAction("com.termux.wayland.ACTION_DOCK_CLICK");
        intent.putExtra("WIDGET_ACTION", actionName);

        return PendingIntent.getBroadcast(
                context,
                actionName.hashCode() + widgetId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }
}
