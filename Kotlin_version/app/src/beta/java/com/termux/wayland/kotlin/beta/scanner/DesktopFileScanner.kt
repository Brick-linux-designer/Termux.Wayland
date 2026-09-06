package com.termux.wayland.kotlin.beta.scanner

import com.termux.wayland.kotlin.beta.data.AppSource
import com.termux.wayland.kotlin.beta.data.LinuxApp
import java.io.File

/**
 * Fallback : scanne les fichiers .desktop pour découvrir
 * des apps GUI non répertoriées dans apps.json.
 *
 * Note : les paquets Termux natifs n'ont PAS de .desktop,
 * donc ce scanner ne sert que pour les apps installées
 * depuis des proot-distros ou des sources tierces.
 *
 * Les résultats sont toujours de priorité DESKTOP (la plus basse).
 */
object DesktopFileScanner {

    // Chemins à scanner (ordre de priorité)
    private val SCAN_PATHS = listOf(
        "/data/data/com.termux/files/usr/share/applications",
        "/data/data/com.termux/files/home/.local/share/applications"
    )

    /**
     * Scanne tous les chemins connus et retourne les apps trouvées.
     * Les apps sans Exec= ou marquées NoDisplay=true sont ignorées.
     */
    fun scanApps(): List<LinuxApp> {
        val apps = mutableListOf<LinuxApp>()

        for (path in SCAN_PATHS) {
            val dir = File(path)
            if (!dir.exists() || !dir.isDirectory) continue

            dir.listFiles { f -> f.extension == "desktop" }
                ?.forEach { file ->
                    parseDesktopFile(file)?.let { apps.add(it) }
                }
        }

        return apps
    }

    // ─── Parseur .desktop (format INI simplifié) ──────────────────────────────

    private fun parseDesktopFile(file: File): LinuxApp? {
        val props = mutableMapOf<String, String>()

        try {
            file.bufferedReader().forEachLine { line ->
                val trimmed = line.trim()
                // Ignorer commentaires et sections
                if (trimmed.startsWith("#") || trimmed.startsWith("[")) return@forEachLine
                val eq = trimmed.indexOf('=')
                if (eq > 0) {
                    val key   = trimmed.substring(0, eq).trim()
                    val value = trimmed.substring(eq + 1).trim()
                    // On garde seulement la première occurrence (locale par défaut)
                    if (!props.containsKey(key)) {
                        props[key] = value
                    }
                }
            }
        } catch (e: Exception) {
            return null
        }

        // Filtres d'exclusion
        if (props["NoDisplay"] == "true") return null
        if (props["Type"] != "Application") return null

        val name = props["Name"] ?: return null
        val exec = props["Exec"]?.cleanExec() ?: return null

        // Détecter si l'app est graphique
        // On exclut les apps Terminal=true qui ne font qu'ouvrir un terminal texte
        // (sauf si elles ont une icône = elles ont probablement une vraie UI)
        val isTerminalApp = props["Terminal"] == "true"
        val hasIcon = props["Icon"] != null

        // Si Terminal=true et pas d'icône → probablement pas une vraie GUI
        if (isTerminalApp && !hasIcon) return null

        // Construire un id à partir du nom de fichier
        val id = file.nameWithoutExtension.lowercase().replace(" ", "-")

        // Déterminer la catégorie depuis Categories=
        val category = guessCategory(props["Categories"] ?: "")

        return LinuxApp(
            id             = id,
            name           = name,
            packageName    = id,   // on ne connaît pas le paquet exact
            exec           = exec,
            category       = category,
            icon           = props["Icon"],
            requiresDisplay= true,
            repo           = "unknown",
            description    = props["Comment"] ?: "",
            source         = AppSource.DESKTOP
        )
    }

    /**
     * Nettoie la valeur Exec= en retirant les placeholders %f %u %F %U etc.
     */
    private fun String.cleanExec(): String {
        return this.replace(Regex("%[fFuUdDnNickvm]"), "").trim()
    }

    /**
     * Déduit une catégorie depuis la chaîne Categories= du .desktop.
     */
    private fun guessCategory(categories: String): String {
        val cats = categories.lowercase()
        return when {
            "webbrowser" in cats || "network" in cats  -> "web"
            "graphics" in cats || "photography" in cats-> "graphics"
            "development" in cats || "ide" in cats     -> "dev"
            "office" in cats || "wordprocessor" in cats-> "office"
            "video" in cats || "audio" in cats ||
                "music" in cats || "player" in cats    -> "media"
            "system" in cats || "filemanager" in cats  -> "system"
            "utility" in cats || "calculator" in cats  -> "tools"
            else                                        -> "tools"
        }
    }
}
