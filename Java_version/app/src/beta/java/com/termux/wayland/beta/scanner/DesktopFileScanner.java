package com.termux.wayland.beta.scanner;

import com.termux.wayland.beta.data.LinuxApp;
import com.termux.wayland.beta.data.LinuxApp.AppSource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fallback : scanne les fichiers .desktop pour découvrir
 * des apps GUI non répertoriées dans apps.json.
 *
 * Note : les paquets Termux natifs n'ont PAS de .desktop,
 * ce scanner sert pour les apps depuis proot-distros ou sources tierces.
 *
 * Priorité DESKTOP = la plus basse dans le merge final.
 */
public class DesktopFileScanner {

    // Chemins à scanner
    private static final String[] SCAN_PATHS = {
        "/data/data/com.termux/files/usr/share/applications",
        "/data/data/com.termux/files/home/.local/share/applications"
    };

    // Singleton
    private static DesktopFileScanner instance;

    private DesktopFileScanner() {}

    public static synchronized DesktopFileScanner getInstance() {
        if (instance == null) {
            instance = new DesktopFileScanner();
        }
        return instance;
    }

    /**
     * Scanne tous les chemins connus et retourne les apps trouvées.
     */
    public List<LinuxApp> scanApps() {
        List<LinuxApp> apps = new ArrayList<>();

        for (String path : SCAN_PATHS) {
            File dir = new File(path);
            if (!dir.exists() || !dir.isDirectory()) continue;

            File[] files = dir.listFiles(f -> f.getName().endsWith(".desktop"));
            if (files == null) continue;

            for (File file : files) {
                LinuxApp app = parseDesktopFile(file);
                if (app != null) apps.add(app);
            }
        }

        return apps;
    }

    // ─── Parseur .desktop (format INI simplifié) ──────────────────────────────

    private LinuxApp parseDesktopFile(File file) {
        Map<String, String> props = new HashMap<>();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                // Ignorer commentaires et sections
                if (trimmed.startsWith("#") || trimmed.startsWith("[")) continue;
                int eq = trimmed.indexOf('=');
                if (eq > 0) {
                    String key   = trimmed.substring(0, eq).trim();
                    String value = trimmed.substring(eq + 1).trim();
                    // Garder seulement la première occurrence
                    if (!props.containsKey(key)) {
                        props.put(key, value);
                    }
                }
            }
            reader.close();
        } catch (Exception e) {
            return null;
        }

        // Filtres d'exclusion
        if ("true".equals(props.get("NoDisplay"))) return null;
        if (!"Application".equals(props.get("Type"))) return null;

        String name = props.get("Name");
        String execRaw = props.get("Exec");
        if (name == null || execRaw == null) return null;

        String exec = cleanExec(execRaw);
        if (exec.isEmpty()) return null;

        // Exclure les apps terminal-only sans icône
        boolean isTerminalApp = "true".equals(props.get("Terminal"));
        boolean hasIcon       = props.containsKey("Icon");
        if (isTerminalApp && !hasIcon) return null;

        // Id depuis le nom de fichier
        String id = file.getName()
                .replace(".desktop", "")
                .toLowerCase()
                .replace(" ", "-");

        String category = guessCategory(props.getOrDefault("Categories", ""));

        return new LinuxApp(
            id,
            name,
            id,     // packageName inconnu → on utilise l'id
            exec,
            category,
            props.get("Icon"),
            true,
            "unknown",
            props.getOrDefault("Comment", ""),
            AppSource.DESKTOP
        );
    }

    /**
     * Nettoie la valeur Exec= en retirant les placeholders %f %u %F %U etc.
     */
    private String cleanExec(String exec) {
        return exec.replaceAll("%[fFuUdDnNickvm]", "").trim();
    }

    /**
     * Déduit une catégorie depuis la chaîne Categories= du .desktop.
     */
    private String guessCategory(String categories) {
        String cats = categories.toLowerCase();
        if (cats.contains("webbrowser") || cats.contains("network")) return "web";
        if (cats.contains("graphics")   || cats.contains("photography")) return "graphics";
        if (cats.contains("development")|| cats.contains("ide"))         return "dev";
        if (cats.contains("office")     || cats.contains("wordprocessor"))return "office";
        if (cats.contains("video")      || cats.contains("audio") ||
            cats.contains("music")      || cats.contains("player"))       return "media";
        if (cats.contains("system")     || cats.contains("filemanager"))  return "system";
        if (cats.contains("utility")    || cats.contains("calculator"))   return "tools";
        return "tools";
    }
}
