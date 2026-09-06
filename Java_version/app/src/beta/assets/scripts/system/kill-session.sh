#!/data/data/com.termux/files/usr/bin/bash
# =============================================================================
# kill-session.sh — Termux.Wayland
# Arrête proprement la session X11 et tous les processus associés.
#
# Usage :
#   ./kill-session.sh              → arrête DISPLAY=:1 (défaut)
#   ./kill-session.sh --display 2  → arrête DISPLAY=:2
#   ./kill-session.sh --all        → arrête toutes les sessions
#   ./kill-session.sh --app firefox → arrête seulement firefox
# =============================================================================

set -uo pipefail

DISPLAY_NUM=1
KILL_ALL=0
APP=""

while [[ $# -gt 0 ]]; do
    case "$1" in
        --display) DISPLAY_NUM="$2"; shift 2 ;;
        --all)     KILL_ALL=1;       shift   ;;
        --app)     APP="$2";         shift 2 ;;
        -h|--help)
            echo "Usage: kill-session.sh [--display N] [--all] [--app nom]"
            exit 0 ;;
        *) echo "Option inconnue : $1"; exit 1 ;;
    esac
done

echo "=== Termux.Wayland — Arrêt session ==="

# ── Tuer une seule app ────────────────────────────────────────────────────────
if [[ -n "$APP" ]]; then
    echo "Arrêt de : $APP"
    pkill -f "$APP" && echo "  ✓ $APP arrêté" || echo "  ✗ $APP introuvable"
    exit 0
fi

# ── Tuer toutes les sessions X11 ─────────────────────────────────────────────
if [[ "$KILL_ALL" -eq 1 ]]; then
    echo "Arrêt de toutes les sessions X11..."
    pkill -f "Xwayland"        2>/dev/null && echo "  ✓ Xwayland arrêté"      || true
    pkill -f "termux-x11"      2>/dev/null && echo "  ✓ termux-x11 arrêté"    || true
    pkill -f "xfce4-session"   2>/dev/null && echo "  ✓ xfce4-session arrêté" || true
    pkill -f "openbox"         2>/dev/null && echo "  ✓ openbox arrêté"       || true
    pkill -f "i3"              2>/dev/null && echo "  ✓ i3 arrêté"            || true
    pkill -f "fluxbox"         2>/dev/null && echo "  ✓ fluxbox arrêté"       || true
    # Nettoyer les fichiers de lock X11
    rm -f /tmp/.X*-lock /tmp/.X11-unix/X* 2>/dev/null || true
    echo "Toutes les sessions arrêtées."
    exit 0
fi

# ── Tuer la session sur DISPLAY=:N ───────────────────────────────────────────
echo "Arrêt de la session DISPLAY=:${DISPLAY_NUM}..."

# Tuer les apps X11 tournant sur ce display
DISPLAY=":${DISPLAY_NUM}" xdotool search --onlyvisible "" 2>/dev/null \
    | xargs -I{} xdotool windowclose {} 2>/dev/null || true

# Tuer le serveur X
pkill -f "Xwayland :${DISPLAY_NUM}" 2>/dev/null && \
    echo "  ✓ Xwayland :${DISPLAY_NUM} arrêté" || \
    echo "  ✗ Xwayland :${DISPLAY_NUM} introuvable"

# Nettoyer le lock
rm -f "/tmp/.X${DISPLAY_NUM}-lock" 2>/dev/null || true
rm -f "/tmp/.X11-unix/X${DISPLAY_NUM}" 2>/dev/null || true

# Supprimer le fichier d'env
ENV_FILE="/data/data/com.termux.wayland/files/scripts/system/.x11_env"
rm -f "$ENV_FILE" 2>/dev/null || true

echo "Session :${DISPLAY_NUM} arrêtée."
