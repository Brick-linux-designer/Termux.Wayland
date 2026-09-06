package com.termux.wayland.kotlin.beta.scanner

import android.content.Context
import com.termux.wayland.kotlin.beta.data.AppSource
import com.termux.wayland.kotlin.beta.data.LinuxApp
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Lit les deux sources JSON :
 *  1. assets/apps.json      → base curatée bundlée dans l'APK
 *  2. apps_user.json        → fichier éditable par l'utilisateur
 *                             stocké dans le dossier de données de l'app
 *
 * Règle de merge : apps_user.json peut OVERRIDER ou AJOUTER des entrées.
 * Un override est détecté par un "id" identique.
 * Priorité : USER > DATABASE
 */
object JsonAppScanner {

    // Chemin du fichier utilisateur dans le stockage interne de l'app
    private const val USER_FILE_NAME = "apps_user.json"

    /**
     * Retourne la liste brute des apps depuis les deux JSON,
     * mergée mais PAS encore filtrée par dpkg.
     *
     * @param context Android Context pour accéder aux assets
     */
    fun loadApps(context: Context): List<LinuxApp> {
        val dbApps = loadDatabase(context)
        val userApps = loadUserFile(context)
        return merge(dbApps, userApps)
    }

    // ─── Lecture apps.json (assets) ──────────────────────────────────────────

    private fun loadDatabase(context: Context): List<LinuxApp> {
        return try {
            val json = context.assets.open("apps.json")
                .bufferedReader()
                .readText()
            parseJsonArray(json, AppSource.DATABASE)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // ─── Lecture apps_user.json (fichier interne) ─────────────────────────────

    private fun loadUserFile(context: Context): List<LinuxApp> {
        val file = File(context.filesDir, USER_FILE_NAME)
        if (!file.exists()) return emptyList()
        return try {
            val json = file.readText()
            parseJsonArray(json, AppSource.USER)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // ─── Parser JSON → List<LinuxApp> ────────────────────────────────────────

    private fun parseJsonArray(json: String, source: AppSource): List<LinuxApp> {
        val apps = mutableListOf<LinuxApp>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val app = parseApp(obj, source) ?: continue
                apps.add(app)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return apps
    }

    private fun parseApp(obj: JSONObject, source: AppSource): LinuxApp? {
        return try {
            LinuxApp(
                id             = obj.getString("id"),
                name           = obj.getString("name"),
                packageName    = obj.getString("package"),
                exec           = obj.getString("exec"),
                category       = obj.optString("category", "tools"),
                icon           = obj.optString("icon", null),
                requiresDisplay= obj.optBoolean("requires_display", true),
                repo           = obj.optString("repo", "x11-repo"),
                description    = obj.optString("description", ""),
                source         = source
            )
        } catch (e: Exception) {
            null  // entrée malformée → on l'ignore silencieusement
        }
    }

    // ─── Merge : USER écrase DATABASE si même id ─────────────────────────────

    private fun merge(
        dbApps: List<LinuxApp>,
        userApps: List<LinuxApp>
    ): List<LinuxApp> {
        // Index de la base par id
        val merged = dbApps.associateBy { it.id }.toMutableMap()

        for (userApp in userApps) {
            val existing = merged[userApp.id]
            if (existing != null) {
                // Override partiel : on garde les champs non nuls de l'utilisateur
                // et on complète avec les valeurs de la base
                merged[userApp.id] = existing.copy(
                    name           = userApp.name.ifBlank { existing.name },
                    packageName    = userApp.packageName.ifBlank { existing.packageName },
                    exec           = userApp.exec.ifBlank { existing.exec },
                    category       = userApp.category.ifBlank { existing.category },
                    icon           = userApp.icon ?: existing.icon,
                    requiresDisplay= userApp.requiresDisplay,
                    repo           = userApp.repo.ifBlank { existing.repo },
                    description    = userApp.description.ifBlank { existing.description },
                    source         = AppSource.USER
                )
            } else {
                // Nouvelle entrée utilisateur inconnue de la base
                merged[userApp.id] = userApp
            }
        }

        return merged.values.toList()
    }

    /**
     * Retourne le chemin du fichier apps_user.json
     * pour permettre à l'UI de le montrer / l'ouvrir.
     */
    fun getUserFilePath(context: Context): String {
        return File(context.filesDir, USER_FILE_NAME).absolutePath
    }

    /**
     * Crée un apps_user.json vide si inexistant.
     * À appeler au premier lancement.
     */
    fun initUserFileIfNeeded(context: Context) {
        val file = File(context.filesDir, USER_FILE_NAME)
        if (!file.exists()) {
            file.writeText("[]")
        }
    }
}
