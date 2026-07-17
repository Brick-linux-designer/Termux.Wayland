package com.termux.wayland.scanner;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

/**
 * Vérifie quels paquets Termux sont réellement installés via dpkg.
 *
 * On exécute `dpkg -l` une seule fois puis on cache le résultat.
 * Toutes les vérifications suivantes se font en mémoire (O(1)).
 *
 * Singleton via instance statique.
 */
public class DpkgChecker {

    private static DpkgChecker instance;
    private Set<String> installedPackages = null;

    private DpkgChecker() {}

    public static synchronized DpkgChecker getInstance() {
        if (instance == null) {
            instance = new DpkgChecker();
        }
        return instance;
    }

    /**
     * Charge la liste des paquets installés depuis dpkg.
     * À appeler une fois au démarrage (dans un thread/AsyncTask).
     */
    public void loadInstalledPackages() {
        if (installedPackages != null) return; // déjà chargé

        Set<String> packages = new HashSet<>();
        try {
            // dpkg -l retourne des lignes comme :
            // ii  firefox  123.0  aarch64  Mozilla Firefox
            // "ii" = installé correctement
            Process process = Runtime.getRuntime().exec(
                new String[]{"/data/data/com.termux/files/usr/bin/dpkg", "-l"}
            );

            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            );

            String line;
            while ((line = reader.readLine()) != null) {
                // Garder uniquement les lignes "ii" (installé)
                if (line.startsWith("ii")) {
                    String[] parts = line.trim().split("\\s+");
                    if (parts.length >= 2) {
                        // Le nom peut avoir un suffixe d'archi (ex: firefox:aarch64)
                        String pkgName = parts[1].split(":")[0];
                        packages.add(pkgName);
                    }
                }
            }
            reader.close();
            process.waitFor();

        } catch (Exception e) {
            e.printStackTrace();
            // dpkg inaccessible → liste vide
            // L'app affichera zéro résultat, comportement attendu
        }

        installedPackages = packages;
    }

    /**
     * Retourne true si le paquet est installé.
     * Appeler loadInstalledPackages() avant.
     */
    public boolean isInstalled(String packageName) {
        if (installedPackages == null) return false;
        return installedPackages.contains(packageName);
    }

    /**
     * Force un rechargement (utile après une installation en live).
     */
    public void refresh() {
        installedPackages = null;
        loadInstalledPackages();
    }

    /**
     * Retourne tous les paquets installés (utile pour debug).
     */
    public Set<String> getAllInstalled() {
        return installedPackages != null ? installedPackages : new HashSet<>();
    }
}
