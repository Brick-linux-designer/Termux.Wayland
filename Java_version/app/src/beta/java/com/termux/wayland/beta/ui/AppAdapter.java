package com.termux.wayland.beta.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.termux.wayland.beta.R;
import com.termux.wayland.beta.data.LinuxApp;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter pour afficher la liste des apps Linux installées.
 * Supporte filtrage par catégorie + recherche texte en temps réel.
 */
public class AppAdapter extends RecyclerView.Adapter<AppAdapter.AppViewHolder> {

    public interface OnAppClickListener {
        void onLaunchClick(LinuxApp app);
    }

    // Liste complète (source de vérité)
    private List<LinuxApp> allApps = new ArrayList<>();
    // Liste filtrée (affichée)
    private List<LinuxApp> filteredApps = new ArrayList<>();

    private String currentCategory = "all";
    private String currentQuery    = "";
    private final OnAppClickListener listener;

    public AppAdapter(OnAppClickListener listener) {
        this.listener = listener;
    }

    // ── Données ───────────────────────────────────────────────────────────────

    public void setApps(List<LinuxApp> apps) {
        this.allApps = apps != null ? apps : new ArrayList<>();
        applyFilters();
    }

    public void filterByCategory(String category) {
        this.currentCategory = category;
        applyFilters();
    }

    public void filterByQuery(String query) {
        this.currentQuery = query != null ? query.toLowerCase().trim() : "";
        applyFilters();
    }

    private void applyFilters() {
        filteredApps = new ArrayList<>();
        for (LinuxApp app : allApps) {
            if (matchesCategory(app) && matchesQuery(app)) {
                filteredApps.add(app);
            }
        }
        notifyDataSetChanged();
    }

    private boolean matchesCategory(LinuxApp app) {
        return "all".equals(currentCategory) || currentCategory.equals(app.getCategory());
    }

    private boolean matchesQuery(LinuxApp app) {
        if (currentQuery.isEmpty()) return true;
        return app.getName().toLowerCase().contains(currentQuery)
            || app.getPackageName().toLowerCase().contains(currentQuery)
            || app.getDescription().toLowerCase().contains(currentQuery);
    }

    public int getTotalCount() { return allApps.size(); }

    // ── RecyclerView ──────────────────────────────────────────────────────────

    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_app, parent, false);
        return new AppViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
        holder.bind(filteredApps.get(position));
    }

    @Override
    public int getItemCount() { return filteredApps.size(); }

    // ── ViewHolder ────────────────────────────────────────────────────────────

    class AppViewHolder extends RecyclerView.ViewHolder {

        private final ImageView  appIcon;
        private final TextView   appName;
        private final TextView   appDescription;
        private final TextView   appCategory;
        private final TextView   appRepo;
        private final ImageButton btnLaunch;

        AppViewHolder(@NonNull View itemView) {
            super(itemView);
            appIcon        = itemView.findViewById(R.id.app_icon);
            appName        = itemView.findViewById(R.id.app_name);
            appDescription = itemView.findViewById(R.id.app_description);
            appCategory    = itemView.findViewById(R.id.app_category);
            appRepo        = itemView.findViewById(R.id.app_repo);
            btnLaunch      = itemView.findViewById(R.id.btn_launch);
        }

        void bind(LinuxApp app) {
            appName.setText(app.getName());
            appDescription.setText(
                app.getDescription().isEmpty() ? app.getPackageName() : app.getDescription()
            );
            appCategory.setText(app.getCategory());
            appRepo.setText(app.getRepo());

            // Icône : pour l'instant on utilise l'icône par défaut
            // TODO : charger depuis le système de fichiers quand les icônes seront intégrées
            appIcon.setImageResource(R.mipmap.ic_launcher);

            // Couleur repo
            int repoColor;
            switch (app.getRepo()) {
                case "x11-repo":  repoColor = 0xFF185FA5; break;
                case "tur-repo":  repoColor = 0xFF854F0B; break;
                case "flatpak":   repoColor = 0xFF4A235A; break;
                case "snap":      repoColor = 0xFFE95420; break;
                default:          repoColor = 0xFF3B6D11; break;
            }
            appRepo.setTextColor(repoColor);

            // Clic sur le bouton lancer
            btnLaunch.setOnClickListener(v -> {
                if (listener != null) listener.onLaunchClick(app);
            });

            // Clic sur toute la carte = même action
            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onLaunchClick(app);
            });
        }
    }
}
