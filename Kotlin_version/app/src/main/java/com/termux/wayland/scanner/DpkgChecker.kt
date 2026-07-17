package com.termux.wayland.scanner

/**
 * Vérifie quels paquets Termux sont réellement installés via dpkg.
 *
 * On exécute `dpkg -l` une seule fois puis on cache le résultat.
 * Toutes les vérifications suivantes se font en mémoire (O(1)).
 */
object DpkgChecker {

    // Cache : nom de paquet → installé (true/false)
    private var installedPackages: Set<String>? = null

    /**
     * Charge la liste des paquets installés depuis dpkg.
     * À appeler une fois au démarrage (dans un coroutine/thread).
     */
    fun loadInstalledPackages() {
        if (installedPackages != null) return  // déjà chargé

        val packages = mutableSetOf<String>()
        try {
            // dpkg -l retourne des lignes comme :
            // ii  firefox  123.0  aarch64  Mozilla Firefox
            // ^^ status : "ii" = installé correctement
            val process = Runtime.getRuntime().exec(
                arrayOf("/data/data/com.termux/files/usr/bin/dpkg", "-l")
            )
            process.inputStream.bufferedReader().forEachLine { line ->
                // On garde uniquement les lignes commençant par "ii" (installé)
                if (line.startsWith("ii")) {
                    val parts = line.trim().split("\\s+".toRegex())
                    if (parts.size >= 2) {
                        // Le nom du paquet peut avoir un suffixe d'archi (ex: firefox:aarch64)
                        val pkgName = parts[1].substringBefore(":")
                        packages.add(pkgName)
                    }
                }
            }
            process.waitFor()
        } catch (e: Exception) {
            // dpkg inaccessible ou Termux non installé → liste vide
            // L'app affichera zéro résultat, c'est le comportement attendu
            e.printStackTrace()
        }

        installedPackages = packages
    }

    /**
     * Retourne true si le paquet est installé.
     * Appeler loadInstalledPackages() avant.
     */
    fun isInstalled(packageName: String): Boolean {
        return installedPackages?.contains(packageName) ?: false
    }

    /**
     * Force un rechargement (utile après une installation en live).
     */
    fun refresh() {
        installedPackages = null
        loadInstalledPackages()
    }

    /**
     * Retourne tous les paquets installés (utile pour debug).
     */
    fun getAllInstalled(): Set<String> {
        return installedPackages ?: emptySet()
    }
}
