package com.termux.wayland.data

import android.content.Context
import com.termux.wayland.scanner.DesktopFileScanner
import com.termux.wayland.scanner.DpkgChecker
import com.termux.wayland.scanner.JsonAppScanner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Point d'entrée unique pour obtenir la liste des apps Linux disponibles.
 *
 * Pipeline complet :
 *   1. Charger dpkg (liste des paquets installés)
 *   2. Lire apps.json (base curatée)
 *   3. Lire apps_user.json (overrides utilisateur)
 *   4. Merger JSON (USER > DATABASE)
 *   5. Scanner .desktop (fallback pour apps inconnues)
 *   6. Fusionner tout
 *   7. Filtrer : ne garder que les apps installées (via dpkg)
 *   8. Dédupliquer par id
 *   9. Trier par nom
 */
object AppRepository {

    /**
     * Charge et retourne toutes les apps installées.
     * Suspendu — à appeler depuis une coroutine.
     *
     * @param context Android Context
     * @param includeDatabaseOnly si true, ignore le scan .desktop (plus rapide)
     */
    suspend fun getInstalledApps(
        context: Context,
        includeDatabaseOnly: Boolean = false
    ): List<LinuxApp> = withContext(Dispatchers.IO) {

        // ── Étape 1 : initialisation ──────────────────────────────────────────
        JsonAppScanner.initUserFileIfNeeded(context)
        DpkgChecker.loadInstalledPackages()

        // ── Étape 2-4 : sources JSON ──────────────────────────────────────────
        val jsonApps = JsonAppScanner.loadApps(context)

        // ── Étape 5 : scan .desktop (optionnel) ───────────────────────────────
        val desktopApps = if (!includeDatabaseOnly) {
            DesktopFileScanner.scanApps()
        } else {
            emptyList()
        }

        // ── Étape 6 : fusion globale ──────────────────────────────────────────
        // Les entrées JSON (DATABASE/USER) ont priorité sur DESKTOP
        // Si un .desktop a le même id qu'une entrée JSON, on garde le JSON
        val allApps = mergeWithDesktop(jsonApps, desktopApps)

        // ── Étape 7 : filtre dpkg ─────────────────────────────────────────────
        // Pour les apps DESKTOP, on ne connaît pas le vrai package → on les garde
        // si leur exec est dans le PATH (on ne peut pas vérifier avec dpkg)
        val filtered = allApps.filter { app ->
            when (app.source) {
                AppSource.DESKTOP  -> true          // pas de paquet connu → on garde
                AppSource.DATABASE,
                AppSource.USER     -> DpkgChecker.isInstalled(app.packageName)
            }
        }

        // ── Étapes 8-9 : déduplique + tri ────────────────────────────────────
        filtered
            .distinctBy { it.id }
            .sortedBy { it.name.lowercase() }
    }

    /**
     * Fusionne les apps JSON avec les apps .desktop.
     * Les ids JSON ont priorité : si un .desktop a le même id, il est ignoré.
     */
    private fun mergeWithDesktop(
        jsonApps: List<LinuxApp>,
        desktopApps: List<LinuxApp>
    ): List<LinuxApp> {
        val knownIds = jsonApps.map { it.id }.toSet()
        val newDesktopApps = desktopApps.filter { it.id !in knownIds }
        return jsonApps + newDesktopApps
    }

    /**
     * Force un rechargement complet (utile après installation d'un nouveau paquet).
     */
    suspend fun refresh(context: Context): List<LinuxApp> {
        DpkgChecker.refresh()
        return getInstalledApps(context)
    }

    /**
     * Retourne uniquement les apps d'une catégorie donnée.
     */
    suspend fun getAppsByCategory(
        context: Context,
        category: String
    ): List<LinuxApp> {
        return getInstalledApps(context).filter { it.category == category }
    }

    /**
     * Retourne les catégories distinctes ayant au moins une app installée.
     */
    suspend fun getAvailableCategories(context: Context): List<String> {
        return getInstalledApps(context)
            .map { it.category }
            .distinct()
            .sorted()
    }
}
