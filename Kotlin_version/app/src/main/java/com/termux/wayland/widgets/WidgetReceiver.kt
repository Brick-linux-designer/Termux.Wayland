package com.termux.wayland.widgets

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast

class WidgetReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("TermuxWaylandWidget", "Intent reçu: ${intent.action}")

        if (intent.action == "com.termux.wayland.ACTION_DOCK_CLICK") {
            val actionName = intent.getStringExtra("WIDGET_ACTION")
            Log.d("TermuxWaylandWidget", "Action détectée: $actionName")

            when (actionName) {
                "LAUNCH_TERMINAL" -> {
                    Toast.makeText(context, "Ouverture de Termux...", Toast.LENGTH_SHORT).show()
                    envoyerCommandeTermux(context, "termux-wake-lock; exec bash")
                }
                "START_WAYLAND" -> {
                    Toast.makeText(context, "Démarrage Wayland...", Toast.LENGTH_SHORT).show()
                    envoyerCommandeTermux(context, "start-wayland.sh") 
                }
                "LAUNCH_APP" -> {
                    Toast.makeText(context, "Lancement application...", Toast.LENGTH_SHORT).show()
                    envoyerCommandeTermux(context, "DISPLAY=:1 gimp") 
                }
                "STOP_ALL" -> {
                    Toast.makeText(context, "Arrêt des environnements...", Toast.LENGTH_SHORT).show()
                    envoyerCommandeTermux(context, "pkill -f wayland; pkill -f Xwayland")
                }
            }
        }
    }

    private fun envoyerCommandeTermux(context: Context, commande: String) {
        try {
            val termuxIntent = Intent().apply {
                setClassName("com.termux", "com.termux.app.TermuxService")
                action = "com.termux.service.RUN_COMMAND"
                putExtra("com.termux.RUN_COMMAND_ARGUMENTS", arrayOf("-c", commande))
                putExtra("com.termux.RUN_COMMAND_BACKGROUND", true)
            }
            context.startService(termuxIntent)
        } catch (e: Exception) {
            Log.e("TermuxWaylandWidget", "Erreur Termux: ${e.message}")
        }
    }
}
