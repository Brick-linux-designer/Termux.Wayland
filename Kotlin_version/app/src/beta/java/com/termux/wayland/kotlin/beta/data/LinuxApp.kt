package com.termux.wayland.kotlin.beta.data

/**
 * Représente une application Linux avec interface graphique.
 * Peut venir de 3 sources : base JSON bundlée, JSON utilisateur, ou scan .desktop.
 */
data class LinuxApp(
    val id: String,                          // identifiant unique (ex: "firefox")
    val name: String,                        // nom affiché (ex: "Firefox")
    val packageName: String,                 // nom paquet dpkg (ex: "firefox")
    val exec: String,                        // commande de lancement (ex: "firefox %u")
    val category: String,                    // catégorie (web, graphics, dev, ...)
    val icon: String? = null,                // référence icône (ex: "builtin:firefox")
    val requiresDisplay: Boolean = true,     // nécessite DISPLAY=:1
    val repo: String = "x11-repo",          // repo Termux source
    val description: String = "",            // description courte
    val source: AppSource = AppSource.DATABASE
)

/**
 * Origine de l'entrée — détermine la priorité lors du merge.
 * USER > DATABASE > DESKTOP
 */
enum class AppSource {
    DATABASE,   // apps.json bundlé dans l'APK
    USER,       // apps_user.json édité par l'utilisateur
    DESKTOP     // scan .desktop fallback
}
