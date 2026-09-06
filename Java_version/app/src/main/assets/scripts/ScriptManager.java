package com.termux.wayland.scripts;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * ScriptManager
 *
 * Gere le dossier de scripts de l'app :
 *
 *   files/scripts/system/   <- bundles dans assets/scripts/system, copies
 *                               au premier lancement puis JAMAIS ecrases
 *                               si le fichier existe deja (l'utilisateur
 *                               peut les modifier librement).
 *   files/scripts/user/     <- dossier libre, jamais touche par l'app,
 *                               cree vide s'il n'existe pas.
 *
 * Les scripts sont ensuite partageables via FileProvider (voir
 * res/xml/file_paths.xml et la declaration <provider> dans le manifest).
 */
public class ScriptManager {

    private static final String TAG = "ScriptManager";

    private static final String ASSETS_SCRIPTS_DIR = "scripts/system";
    private static final String SCRIPTS_ROOT = "scripts";
    private static final String SYSTEM_DIR = "system";
    private static final String USER_DIR = "user";

    private final Context context;

    public ScriptManager(Context context) {
        this.context = context.getApplicationContext();
    }

    /** A appeler une fois au demarrage de l'app (ex: Application.onCreate). */
    public void ensureScriptsInstalled() {
        File systemDir = getSystemScriptsDir();
        File userDir = getUserScriptsDir();

        if (!systemDir.exists() && !systemDir.mkdirs()) {
            Log.e(TAG, "Impossible de creer " + systemDir);
        }
        if (!userDir.exists() && !userDir.mkdirs()) {
            Log.e(TAG, "Impossible de creer " + userDir);
        }

        copyBundledScriptsIfMissing(systemDir);
    }

    public File getScriptsRoot() {
        return new File(context.getFilesDir(), SCRIPTS_ROOT);
    }

    public File getSystemScriptsDir() {
        return new File(getScriptsRoot(), SYSTEM_DIR);
    }

    public File getUserScriptsDir() {
        return new File(getScriptsRoot(), USER_DIR);
    }

    /** Liste les scripts systeme presents sur le disque. */
    public List<File> listSystemScripts() {
        return listScriptsIn(getSystemScriptsDir());
    }

    /** Liste les scripts utilisateur presents sur le disque. */
    public List<File> listUserScripts() {
        return listScriptsIn(getUserScriptsDir());
    }

    private List<File> listScriptsIn(File dir) {
        List<File> result = new ArrayList<>();
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isFile()) {
                    result.add(f);
                }
            }
        }
        return result;
    }

    /**
     * Retourne une Uri partageable (content://) pour un script donne,
     * utilisable directement dans un Intent (ACTION_SEND, RUN_COMMAND, etc.).
     */
    public Uri getShareableUri(File script) {
        String authority = context.getPackageName() + ".fileprovider";
        return FileProvider.getUriForFile(context, authority, script);
    }

    // --- Installation depuis assets -----------------------------------

    private void copyBundledScriptsIfMissing(File systemDir) {
        String[] assetFiles;
        try {
            assetFiles = context.getAssets().list(ASSETS_SCRIPTS_DIR);
        } catch (IOException e) {
            Log.e(TAG, "Impossible de lister les assets " + ASSETS_SCRIPTS_DIR, e);
            return;
        }

        if (assetFiles == null) {
            Log.w(TAG, "Aucun asset trouve dans " + ASSETS_SCRIPTS_DIR);
            return;
        }

        for (String fileName : assetFiles) {
            File dest = new File(systemDir, fileName);
            if (dest.exists()) {
                // Ne jamais ecraser un script deja present (potentiellement
                // modifie par l'utilisateur).
                continue;
            }
            String assetPath = ASSETS_SCRIPTS_DIR + "/" + fileName;
            if (copyAssetToFile(assetPath, dest)) {
                makeExecutable(dest);
                Log.i(TAG, "Script installe: " + dest.getAbsolutePath());
            }
        }
    }

    private boolean copyAssetToFile(String assetPath, File dest) {
        try (InputStream in = context.getAssets().open(assetPath);
             OutputStream out = new FileOutputStream(dest)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            out.flush();
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Echec copie asset " + assetPath + " -> " + dest, e);
            return false;
        }
    }

    private void makeExecutable(File file) {
        boolean ok = file.setExecutable(true, false)
                && file.setReadable(true, false);
        if (!ok) {
            Log.w(TAG, "Impossible de rendre executable: " + file.getAbsolutePath());
        }
    }
}
