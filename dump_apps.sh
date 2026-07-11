#!/bin/bash
# =============================================================================
# dump_apps.sh — Générateur universel d'apps_user.json pour Termux.Wayland
#
# Scanne tuos les .desktop de la machine, filtre intelligemment,
# et produit un apps_user.json compatible avec le format AppRepository.
#
# Usage :
#   ./dump_apps.sh                        → ~/apps_user.json
#   ./dump_apps.sh -o /tmp/apps_user.json → chemin personnalisé
#   ./dump_apps.sh -v                     → mode verbose
#   ./dump_apps.sh --dry-run              → aperçu sans écrire
# =============================================================================

set -uo pipefail

# ── Paramètres ────────────────────────────────────────────────────────────────
OUTPUT="$HOME/apps_user.json"
VERBOSE=0
DRY_RUN=0

while [[ $# -gt 0 ]]; do
    case "$1" in
        -o|--output)  OUTPUT="$2"; shift 2 ;;
        -v|--verbose) VERBOSE=1;   shift   ;;
        --dry-run)    DRY_RUN=1;   shift   ;;
        -h|--help)
            echo "Usage: $0 [-o OUTPUT] [-v] [--dry-run]"
            exit 0 ;;
        *) echo "Option inconnue : $1"; exit 1 ;;
    esac
done

# ── Dossiers à scanner (ordre de priorité) ────────────────────────────────────
SCAN_DIRS=(
    "/usr/share/applications"
    "/usr/local/share/applications"
    "$HOME/.local/share/applications"
    "/var/lib/snapd/desktop/applications"       # snap
    "/var/lib/flatpak/exports/share/applications" # flatpak system
    "$HOME/.local/share/flatpak/exports/share/applications" # flatpak user
    "/opt/share/applications"                   # appimages installées dans /opt
    "/home" # Dossier personnels + Bureaux
    "/root" # Dossier personnel de root
    "/data/data/com.termux/files" # Pour Termux
    "/storage/emulated/" # Éspaces de stockage partagé sous Android
    "/" # Pour tout le reste
)

# ── Catégories .desktop → catégories Termux.Wayland ─────────────────────────
map_category() {
    local cats="${1,,}"   # lowercase
    if   [[ "$cats" =~ webbrowser|browser ]];               then echo "web"
    elif [[ "$cats" =~ graphics|photography|2dgraphics|3dgraphics|raster|vector ]]; then echo "graphics"
    elif [[ "$cats" =~ ide|development|revisioncontrol|debugger ]]; then echo "dev"
    elif [[ "$cats" =~ office|wordprocessor|spreadsheet|presentation|viewer ]]; then echo "office"
    elif [[ "$cats" =~ audiovideo|audio|video|music|player|recorder ]]; then echo "media"
    elif [[ "$cats" =~ filemanager|filesystem|disk|system|monitor|settings|terminal|emulator ]]; then echo "system"
    elif [[ "$cats" =~ network|chat|email|instantmessaging|vnc|remote|ssh ]]; then echo "network"
    elif [[ "$cats" =~ game|arcade|board|strategy|simulation ]]; then echo "games"
    elif [[ "$cats" =~ utility|calculator|clock|text|dictionary ]]; then echo "tools"
    else echo "tools"
    fi
}

# ── Déduire le repo (best-effort) ─────────────────────────────────────────────
map_repo() {
    local exec_path="$1"
    if   [[ "$exec_path" =~ flatpak ]];  then echo "flatpak"
    elif [[ "$exec_path" =~ snap ]];     then echo "snap"
    elif [[ "$exec_path" =~ AppImage ]]; then echo "appimage"
    else echo "system"
    fi
}

# ── Nettoyage de la valeur Exec= ──────────────────────────────────────────────
clean_exec() {
    # Retire %f %u %F %U %d %D %n %N %i %c %k %v %m
    echo "$1" | sed 's/%[fFuUdDnNickvmh]//g' | sed 's/^ *//;s/ *$//'
}

# ── Escape JSON ───────────────────────────────────────────────────────────────
json_escape() {
    # Échappe guillemets, backslash, et caractères de contrôle
    printf '%s' "$1" | python3 -c \
        'import sys,json; print(json.dumps(sys.stdin.read())[1:-1])'
}

# ── Filtres d'exclusion ───────────────────────────────────────────────────────
should_skip() {
    local nodisplay="$1" type="$2" cats="$3" exec_val="$4" name="$5"

    # Masqués ou non-Application
    [[ "$nodisplay" == "true" ]]  && return 0
    [[ "$type" != "Application" && -n "$type" ]] && return 0

    # Pas d'exec
    [[ -z "$exec_val" ]] && return 0

    # Execs inutilisables ou daemons
    local bin
    bin=$(echo "$exec_val" | awk '{print $1}' | xargs basename 2>/dev/null)
    local daemon_bins=(
        "at-spi-bus-launcher" "baloo_file" "xdg-desktop-portal"
        "xdg-user-dirs-update" "xbrlapi" "update-notifier"
        "gsd-" "ibus-daemon" "fcitx" "zeitgeist" "tracker-store"
        "gnome-session" "ksmserver" "xfce4-session" "mate-session"
        "speech-dispatcher" "pulseaudio" "pipewire" "wireplumber"
        "nm-applet" "xscreensaver" "light-locker" "xfce4-power-manager"
        "canberra-gtk-play" "pk-command-not-found"
    )
    for d in "${daemon_bins[@]}"; do
        [[ "$bin" == "$d"* ]] && return 0
    done

    # Catégories sans GUI réelle
    local cats_lower="${cats,,}"
    [[ "$cats_lower" =~ consoleonly ]] && return 0

    # Execs pointant vers des paths de daemons système
    [[ "$exec_val" =~ /usr/lib(exec)?/ && ! "$exec_val" =~ (gimp|inkscape|blender|krita|godot) ]] && return 0

    return 1
}

# ── Parsing d'un fichier .desktop ─────────────────────────────────────────────
parse_desktop() {
    local file="$1"
    local name="" exec_val="" icon="" categories="" comment=""
    local nodisplay="false" type="" terminal="false"
    local in_desktop_entry=0

    while IFS='=' read -r key rest || [[ -n "$key" ]]; do
        # Détecter la section [Desktop Entry]
        if [[ "$key" =~ ^\[.*\]$ ]]; then
            [[ "$key" == "[Desktop Entry]" ]] && in_desktop_entry=1 || in_desktop_entry=0
            continue
        fi
        [[ "$in_desktop_entry" -eq 0 ]] && continue

        # Ignorer les clés localisées (Name[fr]=...) sauf si pas encore de valeur
        key="${key%% *}"  # trim trailing spaces
        [[ "$key" =~ \[ ]] && continue  # clé localisée → skip

        case "$key" in
            Name)       [[ -z "$name" ]]       && name="$rest" ;;
            Exec)       [[ -z "$exec_val" ]]   && exec_val="$rest" ;;
            Icon)       [[ -z "$icon" ]]       && icon="$rest" ;;
            Categories) [[ -z "$categories" ]] && categories="$rest" ;;
            Comment)    [[ -z "$comment" ]]    && comment="$rest" ;;
            NoDisplay)  nodisplay="${rest,,}" ;;
            Type)       type="$rest" ;;
            Terminal)   terminal="${rest,,}" ;;
        esac
    done < "$file"

    # Filtrage terminal : on accepte quand même si l'exec contient un vrai binaire GUI connu
    if [[ "$terminal" == "true" ]]; then
        return
    fi

    # Vérification via should_skip
    if should_skip "$nodisplay" "$type" "$categories" "$exec_val" "$name"; then
        [[ "$VERBOSE" -eq 1 ]] && echo "  SKIP : $name ($file)" >&2
        return
    fi

    local exec_clean
    exec_clean=$(clean_exec "$exec_val")
    [[ -z "$exec_clean" ]] && return

    # Générer l'id depuis le nom de fichier .desktop
    local id
    id=$(basename "$file" .desktop | tr '[:upper:]' '[:lower:]' | tr ' ' '-' | tr -cd '[:alnum:]-_')

    # Déduire package (best-effort : souvent == id)
    local package="$id"

    local category
    category=$(map_category "$categories")

    local repo
    repo=$(map_repo "$exec_clean")

    # Échapper pour JSON
    local name_esc exec_esc icon_esc desc_esc
    name_esc=$(json_escape "$name")
    exec_esc=$(json_escape "$exec_clean")
    icon_esc=$(json_escape "$icon")
    desc_esc=$(json_escape "$comment")

    # Émettre l'entrée JSON (sans virgule finale — gérée par l'appelant)
    printf '  {\n'
    printf '    "id": "%s",\n'            "$id"
    printf '    "name": "%s",\n'          "$name_esc"
    printf '    "package": "%s",\n'       "$package"
    printf '    "exec": "%s",\n'          "$exec_esc"
    printf '    "category": "%s",\n'      "$category"
    printf '    "icon": "%s",\n'          "$icon_esc"
    printf '    "requires_display": true,\n'
    printf '    "repo": "%s",\n'          "$repo"
    printf '    "description": "%s",\n'   "$desc_esc"
    printf '    "source": "USER"\n'
    printf '  }'
}

# ── Résoudre le vrai binaire d'une commande Exec ──────────────────────────────
resolve_binary() {
    # Extrait le premier mot de la commande, puis résout le chemin réel.
    # Pour snap/flatpak, on garde le chemin complet comme clé de dédup
    # car /snap/bin/supertuxkart et /usr/bin/supertuxkart sont deux instances.
    local exec_val="$1"
    local bin
    bin=$(echo "$exec_val" | awk '{print $1}')

    local repo
    repo=$(map_repo "$exec_val")

    # Pour snap et flatpak : clé = repo + binaire (pas de dédup inter-sources)
    if [[ "$repo" == "snap" || "$repo" == "flatpak" ]]; then
        echo "${repo}:${bin}"
        return
    fi

    # Pour les binaires système : résoudre le chemin réel pour détecter les vrais doublons
    local resolved
    resolved=$(command -v "$bin" 2>/dev/null || realpath "$bin" 2>/dev/null || echo "$bin")
    echo "system:${resolved}"
}

# ── Programme principal ───────────────────────────────────────────────────────
echo "=== dump_apps.sh — Termux.Wayland ===" >&2
echo "Scan des dossiers .desktop..." >&2

# Deux tables de déduplication distinctes :
#   seen_binaries : clé = "repo:binaire_résolu" → évite les vrais doublons
#                   (même binaire deb installé deux fois, même flatpak en double)
#   seen_ids      : clé = id JSON final → évite les collisions de noms dans le JSON
declare -A seen_binaries
declare -A seen_ids
entries=()

for dir in "${SCAN_DIRS[@]}"; do
    [[ ! -d "$dir" ]] && continue
    [[ "$VERBOSE" -eq 1 ]] && echo "→ Scan : $dir" >&2

    while IFS= read -r -d '' file; do

        # Parser le fichier en premier — on a besoin de l'exec pour dédupliquer
        entry=$(parse_desktop "$file")
        [[ -z "$entry" ]] && continue

        # Extraire exec et repo depuis l'entrée déjà générée
        exec_parsed=$(echo "$entry" | grep '"exec"' | sed 's/.*"exec": "\(.*\)",/\1/')
        repo_parsed=$(echo "$entry" | grep '"repo"' | sed 's/.*"repo": "\(.*\)",/\1/')
        id_parsed=$(echo "$entry"   | grep '"id"'   | sed 's/.*"id": "\(.*\)",/\1/')

        # ── Déduplication par binaire réel ────────────────────────────────────
        # Snap et flatpak : on accepte même si le binaire existe aussi en deb.
        # Deux snaps identiques ou deux flatpaks identiques → doublon réel → skip.
        bin_key=$(resolve_binary "$exec_parsed")
        if [[ -n "${seen_binaries[$bin_key]+_}" ]]; then
            [[ "$VERBOSE" -eq 1 ]] && echo "  DEDUP binaire : $id_parsed ($bin_key)" >&2
            continue
        fi
        seen_binaries["$bin_key"]=1

        # ── Déduplication par id JSON (évite collisions de noms) ─────────────
        # Si l'id existe déjà (ex: "supertuxkart" deb + snap),
        # on suffixe avec le repo pour les distinguer dans le JSON.
        final_id="$id_parsed"
        if [[ -n "${seen_ids[$id_parsed]+_}" ]]; then
            final_id="${id_parsed}-${repo_parsed}"
            # Patch l'id et le name dans l'entrée déjà générée
            name_parsed=$(echo "$entry" | grep '"name"' | sed 's/.*"name": "\(.*\)",/\1/')
            entry=$(echo "$entry" \
                | sed "s/\"id\": \"${id_parsed}\"/\"id\": \"${final_id}\"/" \
                | sed "s/\"name\": \"${name_parsed}\"/\"name\": \"${name_parsed} (${repo_parsed})\"/" \
            )
            [[ "$VERBOSE" -eq 1 ]] && echo "  RENAME id : $id_parsed → $final_id" >&2
        fi
        seen_ids["$final_id"]=1

        entries+=("$entry")

    done < <(find "$dir" -maxdepth 2 -name "*.desktop" -type f -print0 2>/dev/null)
done

total=${#entries[@]}
echo "→ $total entrées valides trouvées" >&2

if [[ "$DRY_RUN" -eq 1 ]]; then
    echo "--- DRY RUN (aucun fichier écrit) ---" >&2
    for e in "${entries[@]}"; do
        name=$(echo "$e" | grep '"name"' | head -1 | sed 's/.*"name": "\(.*\)",/\1/')
        cat=$(echo "$e" | grep '"category"' | head -1 | sed 's/.*"category": "\(.*\)",/\1/')
        echo "  [$cat] $name"
    done
    exit 0
fi

# Écrire le JSON
{
    printf '[\n'
    for i in "${!entries[@]}"; do
        printf '%s' "${entries[$i]}"
        # Virgule après chaque entrée sauf la dernière
        [[ $i -lt $((total - 1)) ]] && printf ','
        printf '\n'
    done
    printf ']\n'
} > "$OUTPUT"

echo "→ Fichier écrit : $OUTPUT ($total entrées)" >&2
echo "Terminé." >&2
