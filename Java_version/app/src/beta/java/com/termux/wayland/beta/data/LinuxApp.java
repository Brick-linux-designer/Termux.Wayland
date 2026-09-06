package com.termux.wayland.beta.data;

/**
 * Représente une application Linux avec interface graphique.
 * Peut venir de 3 sources : base JSON bundlée, JSON utilisateur, ou scan .desktop.
 */
public class LinuxApp {

    public enum AppSource {
        DATABASE,   // apps.json bundlé dans l'APK
        USER,       // apps_user.json édité par l'utilisateur
        DESKTOP     // scan .desktop fallback
    }

    private final String id;
    private final String name;
    private final String packageName;
    private final String exec;
    private final String category;
    private final String icon;
    private final boolean requiresDisplay;
    private final String repo;
    private final String description;
    private final AppSource source;

    public LinuxApp(
            String id,
            String name,
            String packageName,
            String exec,
            String category,
            String icon,
            boolean requiresDisplay,
            String repo,
            String description,
            AppSource source
    ) {
        this.id             = id;
        this.name           = name;
        this.packageName    = packageName;
        this.exec           = exec;
        this.category       = category;
        this.icon           = icon;
        this.requiresDisplay= requiresDisplay;
        this.repo           = repo;
        this.description    = description;
        this.source         = source;
    }

    // ─── Getters ──────────────────────────────────────────────────────────────

    public String getId()            { return id; }
    public String getName()          { return name; }
    public String getPackageName()   { return packageName; }
    public String getExec()          { return exec; }
    public String getCategory()      { return category; }
    public String getIcon()          { return icon; }
    public boolean isRequiresDisplay(){ return requiresDisplay; }
    public String getRepo()          { return repo; }
    public String getDescription()   { return description; }
    public AppSource getSource()     { return source; }

    // ─── Builder pour créer des copies modifiées (remplace Kotlin copy()) ─────

    public static class Builder {
        private String id;
        private String name;
        private String packageName;
        private String exec;
        private String category;
        private String icon;
        private boolean requiresDisplay = true;
        private String repo             = "x11-repo";
        private String description      = "";
        private AppSource source        = AppSource.DATABASE;

        public Builder from(LinuxApp app) {
            this.id             = app.id;
            this.name           = app.name;
            this.packageName    = app.packageName;
            this.exec           = app.exec;
            this.category       = app.category;
            this.icon           = app.icon;
            this.requiresDisplay= app.requiresDisplay;
            this.repo           = app.repo;
            this.description    = app.description;
            this.source         = app.source;
            return this;
        }

        public Builder id(String v)             { this.id = v; return this; }
        public Builder name(String v)           { this.name = v; return this; }
        public Builder packageName(String v)    { this.packageName = v; return this; }
        public Builder exec(String v)           { this.exec = v; return this; }
        public Builder category(String v)       { this.category = v; return this; }
        public Builder icon(String v)           { this.icon = v; return this; }
        public Builder requiresDisplay(boolean v){ this.requiresDisplay = v; return this; }
        public Builder repo(String v)           { this.repo = v; return this; }
        public Builder description(String v)    { this.description = v; return this; }
        public Builder source(AppSource v)      { this.source = v; return this; }

        public LinuxApp build() {
            return new LinuxApp(id, name, packageName, exec, category,
                    icon, requiresDisplay, repo, description, source);
        }
    }

    @Override
    public String toString() {
        return "LinuxApp{id='" + id + "', name='" + name + "', package='" + packageName + "'}";
    }
}
