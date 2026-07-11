package com.termux.wayland.scanner;

import android.content.Context;

import com.termux.wayland.data.LinuxApp;
import com.termux.wayland.data.LinuxApp.AppSource;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Lit les deux sources JSON :
 *  1. assets/apps.json      → base curatée bundlée dans l'APK
 *  2. apps_user.json        → fichier éditable par l'utilisateur
 *                             stocké dans filesDir de l'app
 *
 * Règle de merge : apps_user.json peut OVERRIDER ou AJOUTER des entrées.
 * Priorité : USER > DATABASE
 */
public class JsonAppScanner {

    private static final String USER_FILE_NAME = "apps_user.json";

    // Singleton
    private static JsonAppScanner instance;

    private JsonAppScanner() {}

    public static synchronized JsonAppScanner getInstance() {
        if (instance == null) {
            instance = new JsonAppScanner();
        }
        return instance;
    }

    /**
     * Retourne la liste brute mergée des deux JSON.
     * Non filtrée par dpkg.
     */
    public List<LinuxApp> loadApps(Context context) {
        List<LinuxApp> dbApps   = loadDatabase(context);
        List<LinuxApp> userApps = loadUserFile(context);
        return merge(dbApps, userApps);
    }

    // ─── Lecture assets/apps.json ─────────────────────────────────────────────

    private List<LinuxApp> loadDatabase(Context context) {
        try {
            InputStream is = context.getAssets().open("apps.json");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);
            return parseJsonArray(json, AppSource.DATABASE);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // ─── Lecture apps_user.json (filesDir) ───────────────────────────────────

    private List<LinuxApp> loadUserFile(Context context) {
        File file = new File(context.getFilesDir(), USER_FILE_NAME);
        if (!file.exists()) return new ArrayList<>();
        try {
            byte[] bytes = new byte[(int) file.length()];
            java.io.FileInputStream fis = new java.io.FileInputStream(file);
            fis.read(bytes);
            fis.close();
            String json = new String(bytes, StandardCharsets.UTF_8);
            return parseJsonArray(json, AppSource.USER);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // ─── Parser JSON → List<LinuxApp> ────────────────────────────────────────

    private List<LinuxApp> parseJsonArray(String json, AppSource source) {
        List<LinuxApp> apps = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                LinuxApp app = parseApp(obj, source);
                if (app != null) apps.add(app);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return apps;
    }

    private LinuxApp parseApp(JSONObject obj, AppSource source) {
        try {
            return new LinuxApp(
                obj.getString("id"),
                obj.getString("name"),
                obj.getString("package"),
                obj.getString("exec"),
                obj.optString("category", "tools"),
                obj.has("icon") ? obj.getString("icon") : null,
                obj.optBoolean("requires_display", true),
                obj.optString("repo", "x11-repo"),
                obj.optString("description", ""),
                source
            );
        } catch (Exception e) {
            return null; // entrée malformée → ignorée
        }
    }

    // ─── Merge : USER écrase DATABASE si même id ─────────────────────────────

    private List<LinuxApp> merge(List<LinuxApp> dbApps, List<LinuxApp> userApps) {
        // Index de la base par id (LinkedHashMap garde l'ordre d'insertion)
        Map<String, LinuxApp> merged = new LinkedHashMap<>();
        for (LinuxApp app : dbApps) {
            merged.put(app.getId(), app);
        }

        for (LinuxApp userApp : userApps) {
            LinuxApp existing = merged.get(userApp.getId());
            if (existing != null) {
                // Override partiel : on complète avec les valeurs de la base
                merged.put(userApp.getId(), new LinuxApp.Builder()
                    .from(existing)
                    .name(userApp.getName().isEmpty()        ? existing.getName()        : userApp.getName())
                    .packageName(userApp.getPackageName().isEmpty() ? existing.getPackageName() : userApp.getPackageName())
                    .exec(userApp.getExec().isEmpty()        ? existing.getExec()        : userApp.getExec())
                    .category(userApp.getCategory().isEmpty()? existing.getCategory()    : userApp.getCategory())
                    .icon(userApp.getIcon() != null          ? userApp.getIcon()         : existing.getIcon())
                    .requiresDisplay(userApp.isRequiresDisplay())
                    .repo(userApp.getRepo().isEmpty()        ? existing.getRepo()        : userApp.getRepo())
                    .description(userApp.getDescription().isEmpty() ? existing.getDescription() : userApp.getDescription())
                    .source(AppSource.USER)
                    .build()
                );
            } else {
                // Nouvelle entrée inconnue de la base
                merged.put(userApp.getId(), userApp);
            }
        }

        return new ArrayList<>(merged.values());
    }

    /**
     * Retourne le chemin du fichier apps_user.json.
     */
    public String getUserFilePath(Context context) {
        return new File(context.getFilesDir(), USER_FILE_NAME).getAbsolutePath();
    }

    /**
     * Crée un apps_user.json vide si inexistant.
     * À appeler au premier lancement.
     */
    public void initUserFileIfNeeded(Context context) {
        File file = new File(context.getFilesDir(), USER_FILE_NAME);
        if (!file.exists()) {
            try {
                java.io.FileWriter fw = new java.io.FileWriter(file);
                fw.write("[]");
                fw.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
