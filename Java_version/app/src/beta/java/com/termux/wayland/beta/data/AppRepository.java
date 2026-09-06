package com.termux.wayland.beta.data;

import android.content.Context;
import android.os.AsyncTask;

import com.termux.wayland.beta.scanner.DesktopFileScanner;
import com.termux.wayland.beta.scanner.DpkgChecker;
import com.termux.wayland.beta.scanner.JsonAppScanner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Point d'entrée unique pour obtenir la liste des apps Linux disponibles.
 *
 * Pipeline complet :
 *   1. Charger dpkg (liste des paquets installés)
 *   2. Lire apps.json (base curatée)
 *   3. Lire apps_user.json (overrides utilisateur)
 *   4. Merger JSON (USER > DATABASE)
 *   5. Scanner .desktop (fallback)
 *   6. Fusionner tout
 *   7. Filtrer : garder seulement les apps installées
 *   8. Dédupliquer par id
 *   9. Trier par nom
 */
public class AppRepository {

    /**
     * Callback appelé quand la liste est prête.
     */
    public interface OnAppsLoadedListener {
        void onAppsLoaded(List<LinuxApp> apps);
        void onError(Exception e);
    }

    // Singleton
    private static AppRepository instance;

    private AppRepository() {}

    public static synchronized AppRepository getInstance() {
        if (instance == null) {
            instance = new AppRepository();
        }
        return instance;
    }

    /**
     * Charge les apps de façon asynchrone.
     * Résultat livré via le listener sur le thread principal.
     *
     * @param context Android Context
     * @param includeDatabaseOnly si true, ignore le scan .desktop (plus rapide)
     * @param listener callback résultat
     */
    public void getInstalledApps(
            Context context,
            boolean includeDatabaseOnly,
            OnAppsLoadedListener listener
    ) {
        new LoadAppsTask(context, includeDatabaseOnly, listener).execute();
    }

    /**
     * Surcharge rapide (inclut .desktop scan).
     */
    public void getInstalledApps(Context context, OnAppsLoadedListener listener) {
        getInstalledApps(context, false, listener);
    }

    /**
     * Force un rechargement complet (utile après installation d'un paquet).
     */
    public void refresh(Context context, OnAppsLoadedListener listener) {
        DpkgChecker.getInstance().refresh();
        getInstalledApps(context, listener);
    }

    // ─── AsyncTask interne ────────────────────────────────────────────────────

    private static class LoadAppsTask extends AsyncTask<Void, Void, List<LinuxApp>> {

        private final Context context;
        private final boolean includeDatabaseOnly;
        private final OnAppsLoadedListener listener;
        private Exception error;

        LoadAppsTask(Context context, boolean includeDatabaseOnly, OnAppsLoadedListener listener) {
            this.context              = context.getApplicationContext();
            this.includeDatabaseOnly  = includeDatabaseOnly;
            this.listener             = listener;
        }

        @Override
        protected List<LinuxApp> doInBackground(Void... voids) {
            try {
                // ── Étape 1 : initialisation ──────────────────────────────────
                JsonAppScanner.getInstance().initUserFileIfNeeded(context);
                DpkgChecker.getInstance().loadInstalledPackages();

                // ── Étapes 2-4 : sources JSON ─────────────────────────────────
                List<LinuxApp> jsonApps = JsonAppScanner.getInstance().loadApps(context);

                // ── Étape 5 : scan .desktop (optionnel) ───────────────────────
                List<LinuxApp> desktopApps = new ArrayList<>();
                if (!includeDatabaseOnly) {
                    desktopApps = DesktopFileScanner.getInstance().scanApps();
                }

                // ── Étape 6 : fusion globale ──────────────────────────────────
                List<LinuxApp> allApps = mergeWithDesktop(jsonApps, desktopApps);

                // ── Étape 7 : filtre dpkg ─────────────────────────────────────
                DpkgChecker dpkg = DpkgChecker.getInstance();
                List<LinuxApp> filtered = new ArrayList<>();
                for (LinuxApp app : allApps) {
                    if (app.getSource() == LinuxApp.AppSource.DESKTOP) {
                        filtered.add(app); // pas de vérif possible
                    } else if (dpkg.isInstalled(app.getPackageName())) {
                        filtered.add(app);
                    }
                }

                // ── Étapes 8-9 : déduplique + tri ────────────────────────────
                return deduplicateAndSort(filtered);

            } catch (Exception e) {
                this.error = e;
                return new ArrayList<>();
            }
        }

        @Override
        protected void onPostExecute(List<LinuxApp> apps) {
            if (error != null) {
                listener.onError(error);
            } else {
                listener.onAppsLoaded(apps);
            }
        }
    }

    // ─── Helpers statiques ────────────────────────────────────────────────────

    /**
     * Fusionne apps JSON + apps .desktop.
     * Les ids JSON ont priorité sur .desktop.
     */
    private static List<LinuxApp> mergeWithDesktop(
            List<LinuxApp> jsonApps,
            List<LinuxApp> desktopApps
    ) {
        Set<String> knownIds = new HashSet<>();
        for (LinuxApp app : jsonApps) {
            knownIds.add(app.getId());
        }

        List<LinuxApp> result = new ArrayList<>(jsonApps);
        for (LinuxApp app : desktopApps) {
            if (!knownIds.contains(app.getId())) {
                result.add(app);
            }
        }
        return result;
    }

    /**
     * Supprime les doublons (même id) et trie par nom alphabétique.
     */
    private static List<LinuxApp> deduplicateAndSort(List<LinuxApp> apps) {
        Set<String> seen = new HashSet<>();
        List<LinuxApp> unique = new ArrayList<>();
        for (LinuxApp app : apps) {
            if (seen.add(app.getId())) {
                unique.add(app);
            }
        }
        Collections.sort(unique, (a, b) ->
            a.getName().toLowerCase().compareTo(b.getName().toLowerCase())
        );
        return unique;
    }
}
