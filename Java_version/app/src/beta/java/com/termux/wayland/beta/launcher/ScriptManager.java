package com.termux.wayland.beta.launcher;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Gère le dossier de scripts exposé aux autres apps via FileProvider.
 *
 * Structure créée dans filesDir :
 *   scripts/
 *   ├── system/          ← scripts bundlés (jamais écrasés si déjà présents)
 *   │   ├── setup-x11.sh
 *   │   ├── install-gui-apps.sh
 *   │   ├── launch-app.sh
 *   │   └── kill-session.sh
 *   └── user/            ← dossier libre pour l'utilisateur
 *
 * FileProvider authority : com.termux.wayland.beta.fileprovider
 * URI exemple : content://com.termux.wayland.beta.fileprovider/scripts/system/setup-x11.sh
 */
public class ScriptManager {

    private static final String TAG           = "ScriptManager";
    private static final String AUTHORITY     = "com.termux.wayland.beta.fileprovider";
    private static final String SCRIPTS_DIR   = "scripts";
    private static final String SYSTEM_DIR    = "scripts/system";
    private static final String USER_DIR      = "scripts/user";

    // Scripts bundlés dans assets/scripts/system/
    private static final String[] SYSTEM_SCRIPTS = {
        "setup-x11.sh",
        "install-gui-apps.sh",
        "launch-app.sh",
        "kill-session.sh"
    };

    public interface OnScriptsReadyListener {
        void onReady(File scriptsDir);
        void onError(Exception e);
    }

    // ── Point d'entrée principal ──────────────────────────────────────────────

    /**
     * Initialise le dossier scripts au premier lancement.
     * Copie les scripts system depuis les assets si absents.
     * Crée le dossier user/ s'il n'existe pas.
     * Résultat livré via listener sur le thread principal.
     */
    public static void initScripts(Context context, OnScriptsReadyListener listener) {
        new InitTask(context.getApplicationContext(), listener).execute();
    }

    // ── Accès aux fichiers ────────────────────────────────────────────────────

    /**
     * Retourne le File du dossier scripts/system/
     */
    public static File getSystemDir(Context context) {
        return new File(context.getFilesDir(), SYSTEM_DIR);
    }

    /**
     * Retourne le File du dossier scripts/user/
     */
    public static File getUserDir(Context context) {
        return new File(context.getFilesDir(), USER_DIR);
    }

    /**
     * Retourne le File d'un script system par son nom.
     */
    public static File getSystemScript(Context context, String scriptName) {
        return new File(getSystemDir(context), scriptName);
    }

    /**
     * Retourne l'URI FileProvider d'un script system.
     * Cette URI peut être partagée avec n'importe quelle app.
     *
     * Exemple d'usage :
     *   Uri uri = ScriptManager.getScriptUri(context, "setup-x11.sh");
     *   // → content://com.termux.wayland.beta.fileprovider/scripts/system/setup-x11.sh
     */
    public static Uri getScriptUri(Context context, String scriptName) {
        File script = getSystemScript(context, scriptName);
        return FileProvider.getUriForFile(context, AUTHORITY, script);
    }

    /**
     * Construit un Intent pour ouvrir un script dans une app tierce (éditeur, Termux...).
     * L'app cible reçoit l'URI avec READ permission.
     */
    public static Intent buildOpenScriptIntent(Context context, String scriptName) {
        Uri uri = getScriptUri(context, scriptName);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "text/x-shellscript");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        return intent;
    }

    /**
     * Construit un Intent pour exécuter un script via Termux RUN_COMMAND.
     * Termux doit être installé avec la permission RUN_COMMAND accordée.
     *
     * @param scriptName  nom du script dans system/ (ex: "setup-x11.sh")
     * @param args        arguments passés au script (peut être null)
     */
    public static Intent buildRunScriptIntent(Context context, String scriptName, String[] args) {
        File script = getSystemScript(context, scriptName);

        Intent intent = new Intent();
        intent.setClassName("com.termux", "com.termux.app.RunCommandService");
        intent.setAction("com.termux.RUN_COMMAND");
        intent.putExtra("com.termux.RUN_COMMAND_PATH", script.getAbsolutePath());
        if (args != null) {
            intent.putExtra("com.termux.RUN_COMMAND_ARGUMENTS", args);
        }
        intent.putExtra("com.termux.RUN_COMMAND_WORKDIR",
                "/data/data/com.termux/files/home");
        intent.putExtra("com.termux.RUN_COMMAND_TERMINAL", true);
        return intent;
    }

    // ── AsyncTask d'initialisation ────────────────────────────────────────────

    private static class InitTask extends AsyncTask<Void, Void, File> {

        private final Context context;
        private final OnScriptsReadyListener listener;
        private Exception error;

        InitTask(Context context, OnScriptsReadyListener listener) {
            this.context  = context;
            this.listener = listener;
        }

        @Override
        protected File doInBackground(Void... voids) {
            try {
                File scriptsDir = new File(context.getFilesDir(), SCRIPTS_DIR);
                File systemDir  = new File(context.getFilesDir(), SYSTEM_DIR);
                File userDir    = new File(context.getFilesDir(), USER_DIR);

                // Créer les dossiers si absents
                scriptsDir.mkdirs();
                systemDir.mkdirs();
                userDir.mkdirs();

                // Copier les scripts system depuis les assets
                // Règle : on ne copie QUE si le fichier n'existe pas encore
                // → permet à l'utilisateur de modifier ses scripts sans les perdre
                for (String scriptName : SYSTEM_SCRIPTS) {
                    File dest = new File(systemDir, scriptName);
                    if (!dest.exists()) {
                        copyAsset(context, "scripts/system/" + scriptName, dest);
                        // Rendre le script exécutable
                        dest.setExecutable(true, false);
                        Log.d(TAG, "Script copié : " + scriptName);
                    } else {
                        Log.d(TAG, "Script déjà présent (conservé) : " + scriptName);
                    }
                }

                // Créer un README dans user/ si absent
                File readme = new File(userDir, "README.txt");
                if (!readme.exists()) {
                    writeText(readme,
                        "Dossier scripts utilisateur — Termux.Wayland\n" +
                        "=============================================\n\n" +
                        "Place ici tes scripts bash personnalisés.\n" +
                        "Ils ne seront jamais écrasés lors des mises à jour.\n\n" +
                        "Les scripts system/ sont dans le dossier parent.\n" +
                        "Tu peux les copier ici pour les modifier.\n"
                    );
                }

                return scriptsDir;

            } catch (Exception e) {
                this.error = e;
                return null;
            }
        }

        @Override
        protected void onPostExecute(File scriptsDir) {
            if (error != null) {
                Log.e(TAG, "Erreur init scripts", error);
                listener.onError(error);
            } else {
                listener.onReady(scriptsDir);
            }
        }
    }

    // ── Helpers I/O ──────────────────────────────────────────────────────────

    private static void copyAsset(Context context, String assetPath, File dest) throws IOException {
        try (InputStream in = context.getAssets().open(assetPath);
             OutputStream out = new FileOutputStream(dest)) {
            byte[] buf = new byte[4096];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        }
    }

    private static void writeText(File dest, String text) throws IOException {
        try (FileOutputStream out = new FileOutputStream(dest)) {
            out.write(text.getBytes("UTF-8"));
        }
    }
}
