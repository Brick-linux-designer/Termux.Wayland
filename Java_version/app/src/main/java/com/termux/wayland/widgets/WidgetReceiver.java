package com.termux.wayland.widgets;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class WidgetReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("TermuxWaylandWidget", "Intent reçu: " + intent.getAction());

        if ("com.termux.wayland.ACTION_DOCK_CLICK".equals(intent.getAction())) {
            String actionName = intent.getStringExtra("WIDGET_ACTION");
            Log.d("TermuxWaylandWidget", "Action détectée: " + actionName);

            if (actionName != null) {
                switch (actionName) {
                    case "LAUNCH_TERMINAL":
                        Toast.makeText(context, "Ouverture de Termux...", Toast.LENGTH_SHORT).show();
                        envoyerCommandeTermux(context, "termux-wake-lock; exec bash");
                        break;
                    case "START_WAYLAND":
                        Toast.makeText(context, "Démarrage Wayland...", Toast.LENGTH_SHORT).show();
                        envoyerCommandeTermux(context, "start-wayland.sh");
                        break;
                    case "LAUNCH_APP":
                        Toast.makeText(context, "Lancement application...", Toast.LENGTH_SHORT).show();
                        envoyerCommandeTermux(context, "DISPLAY=:1 gimp");
                        break;
                    case "STOP_ALL":
                        Toast.makeText(context, "Arrêt des environnements...", Toast.LENGTH_SHORT).show();
                        envoyerCommandeTermux(context, "pkill -f wayland; pkill -f Xwayland");
                        break;
                }
            }
        }
    }

    private void envoyerCommandeTermux(Context context, String commande) {
        try {
            Intent termuxIntent = new Intent();
            termuxIntent.setClassName("com.termux", "com.termux.app.TermuxService");
            termuxIntent.setAction("com.termux.service.RUN_COMMAND");
            termuxIntent.putExtra("com.termux.RUN_COMMAND_ARGUMENTS", new String[]{"-c", commande});
            termuxIntent.putExtra("com.termux.RUN_COMMAND_BACKGROUND", true);
            context.startService(termuxIntent);
        } catch (Exception e) {
            Log.e("TermuxWaylandWidget", "Erreur Termux: " + e.getMessage());
        }
    }
}
