package com.termux.wayland.scanner;

import android.content.Context;

import com.termux.wayland.data.LinuxApp;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class JsonAppScanner {

    public static List<LinuxApp> loadApps(Context context) {

        List<LinuxApp> apps = new ArrayList<>();

        try {

            InputStream is = context.getAssets().open("apps.json");

            int size = is.available();

            byte[] buffer = new byte[size];

            is.read(buffer);

            is.close();

            String json = new String(buffer);

            JSONArray array = new JSONArray(json);

            for (int i = 0; i < array.length(); i++) {

                JSONObject obj = array.getJSONObject(i);

                LinuxApp app = new LinuxApp();

                app.id = obj.optString("id");
                app.name = obj.optString("name");
                app.packageName = obj.optString("package");
                app.exec = obj.optString("exec");
                app.category = obj.optString("category");
                app.icon = obj.optString("icon");
                app.requiresDisplay = obj.optBoolean("requires_display", true);
                app.description = obj.optString("description");

                apps.add(app);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return apps;
    }
}
