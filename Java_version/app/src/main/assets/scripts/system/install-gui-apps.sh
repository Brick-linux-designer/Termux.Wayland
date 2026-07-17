#!/data/data/com.termux/files/usr/bin/bash
# =============================================================================
# install-gui-apps.sh — Termux.Wayland
# Installe des paquets GUI Termux depuis x11-repo et tur-repo
#
# Usage :
#   ./install-gui-apps.sh                  → menu interactif
#   ./install-gui-apps.sh firefox gimp     → installe directement
#   ./install-gui-apps.sh --list           → liste tous les paquets disponibles
#   ./install-gui-apps.sh --setup-repos    → configure les repos seulement
# =============================================================================

set -uo pipefail

TERMUX_PREFIX="/data/data/com.termux/files/usr"
PKG="$TERMUX_PREFIX/bin/pkg"
DPKG="$TERMUX_PREFIX/bin/dpkg"

# ── Catalogue : paquet → description ─────────────────────────────────────────
declare -A CATALOG_WEB=(
    [firefox]="Navigateur web Firefox"
    [chromium]="Navigateur web Chromium"
)
declare -A CATALOG_GRAPHICS=(
    [gimp]="Éditeur d'images avancé"
    [inkscape]="Éditeur vectoriel"
    [krita]="Peinture numérique"
    [blender]="Modélisation 3D"
    [feh]="Visionneuse d'images légère"
    [ristretto]="Visionneuse d'images XFCE"
)
declare -A CATALOG_DEV=(
    [geany]="IDE léger"
    [code-oss]="Visual Studio Code"
    [git-cola]="Interface graphique Git"
)
declare -A CATALOG_OFFICE=(
    [libreoffice]="Suite bureautique complète"
    [mousepad]="Éditeur de texte XFCE"
    [evince]="Visionneuse PDF"
    [okular]="Visionneuse PDF KDE"
)
declare -A CATALOG_MEDIA=(
    [vlc]="Lecteur multimédia"
    [mpv]="Lecteur vidéo minimaliste"
    [audacity]="Éditeur audio"
    [audacious]="Lecteur audio"
    [parole]="Lecteur multimédia XFCE"
    [pavucontrol]="Contrôle volume PulseAudio"
)
declare -A CATALOG_SYSTEM=(
    [xfce4-terminal]="Terminal graphique XFCE"
    [xterm]="Terminal X11 classique"
    [thunar]="Gestionnaire de fichiers XFCE"
    [pcmanfm]="Gestionnaire de fichiers LXDE"
    [xfce4-taskmanager]="Moniteur de processus"
)
declare -A CATALOG_DESKTOP=(
    [xfce4]="Bureau XFCE complet"
    [openbox]="Gestionnaire de fenêtres léger"
    [i3]="Gestionnaire de fenêtres en tuiles"
    [picom]="Compositeur X11"
    [rofi]="Lanceur d'applications"
    [tint2]="Barre de tâches"
    [nitrogen]="Gestionnaire fond d'écran"
)
declare -A CATALOG_NETWORK=(
    [wireshark]="Analyseur réseau"
    [remmina]="Bureau à distance"
    [thunderbird]="Client email"
    [telegram-desktop]="Messagerie Telegram"
)

# ── Configurer les repos ───────────────────────────────────────────────────────
setup_repos() {
    echo "Configuration des repos Termux..."
    "$PKG" install -y x11-repo   2>/dev/null || true
    "$PKG" install -y tur-repo   2>/dev/null || true
    "$PKG" update -y
    echo "Repos configurés."
}

# ── Vérifier si installé ──────────────────────────────────────────────────────
is_installed() {
    "$DPKG" -l "$1" 2>/dev/null | grep -q "^ii"
}

# ── Installer un paquet ───────────────────────────────────────────────────────
install_pkg() {
    local pkg="$1"
    if is_installed "$pkg"; then
        echo "  ✓ $pkg déjà installé"
        return 0
    fi
    echo "  → Installation de $pkg..."
    "$PKG" install -y "$pkg" && echo "  ✓ $pkg installé" || echo "  ✗ Échec : $pkg"
}

# ── Liste tous les paquets du catalogue ───────────────────────────────────────
list_all() {
    echo ""
    echo "=== Paquets GUI disponibles ==="
    local categories=("WEB" "GRAPHICS" "DEV" "OFFICE" "MEDIA" "SYSTEM" "DESKTOP" "NETWORK")
    for cat in "${categories[@]}"; do
        echo ""
        echo "[$cat]"
        local -n ref="CATALOG_${cat}"
        for pkg in "${!ref[@]}"; do
            local status="  "
            is_installed "$pkg" && status="✓ " || status="  "
            printf "  %s %-30s %s\n" "$status" "$pkg" "${ref[$pkg]}"
        done
    done
}

# ── Menu interactif ───────────────────────────────────────────────────────────
interactive_menu() {
    echo "=== Termux.Wayland — Installation apps GUI ==="
    echo ""
    echo "Groupes disponibles :"
    echo "  1) Web          (firefox, chromium)"
    echo "  2) Graphisme    (gimp, inkscape, krita...)"
    echo "  3) Dev          (geany, code-oss, git-cola)"
    echo "  4) Bureau       (libreoffice, evince...)"
    echo "  5) Médias       (vlc, audacity, audacious...)"
    echo "  6) Système      (terminaux, gestionnaires de fichiers)"
    echo "  7) Desktop      (xfce4, openbox, i3, rofi...)"
    echo "  8) Réseau       (wireshark, remmina, thunderbird)"
    echo "  9) Tout installer"
    echo "  0) Quitter"
    echo ""
    read -rp "Choix : " choice

    case "$choice" in
        1) install_group CATALOG_WEB ;;
        2) install_group CATALOG_GRAPHICS ;;
        3) install_group CATALOG_DEV ;;
        4) install_group CATALOG_OFFICE ;;
        5) install_group CATALOG_MEDIA ;;
        6) install_group CATALOG_SYSTEM ;;
        7) install_group CATALOG_DESKTOP ;;
        8) install_group CATALOG_NETWORK ;;
        9) install_all ;;
        0) exit 0 ;;
        *) echo "Choix invalide." ;;
    esac
}

install_group() {
    local -n group="$1"
    echo ""
    echo "Installation du groupe..."
    setup_repos
    for pkg in "${!group[@]}"; do
        install_pkg "$pkg"
    done
    echo ""
    echo "Terminé."
}

install_all() {
    setup_repos
    local all_groups=(CATALOG_WEB CATALOG_GRAPHICS CATALOG_DEV CATALOG_OFFICE
                      CATALOG_MEDIA CATALOG_SYSTEM CATALOG_DESKTOP CATALOG_NETWORK)
    for group in "${all_groups[@]}"; do
        local -n ref="$group"
        for pkg in "${!ref[@]}"; do
            install_pkg "$pkg"
        done
    done
    echo ""
    echo "Installation complète terminée."
}

# ── Main ──────────────────────────────────────────────────────────────────────
case "${1:-}" in
    --list)       list_all ;;
    --setup-repos) setup_repos ;;
    --help|-h)
        echo "Usage: install-gui-apps.sh [paquet...] [--list] [--setup-repos]"
        ;;
    "")           interactive_menu ;;
    *)
        # Arguments directs : ./install-gui-apps.sh firefox gimp vlc
        setup_repos
        for pkg in "$@"; do
            install_pkg "$pkg"
        done
        ;;
esac
