#!/data/data/com.termux/files/usr/bin/bash
# =============================================================================
# setup-x11.sh — Termux.Wayland
# Configure et démarre une session X11 via Termux:X11
#
# Usage :
#   ./setup-x11.sh              → démarre sur :1 (défaut)
#   ./setup-x11.sh --display 2  → démarre sur :2
#   ./setup-x11.sh --wm openbox → lance un window manager spécifique
#   ./setup-x11.sh --stop       → arrête la session en cours
# =============================================================================

set -uo pipefail

# ── Valeurs par défaut ────────────────────────────────────────────────────────
DISPLAY_NUM=1
WM=""
STOP=0

while [[ $# -gt 0 ]]; do
    case "$1" in
        --display) DISPLAY_NUM="$2"; shift 2 ;;
        --wm)      WM="$2";          shift 2 ;;
        --stop)    STOP=1;           shift   ;;
        -h|--help)
            echo "Usage: setup-x11.sh [--display N] [--wm openbox|i3|xfwm4] [--stop]"
            exit 0 ;;
        *) echo "Option inconnue : $1"; exit 1 ;;
    esac
done

export DISPLAY=":${DISPLAY_NUM}"
TERMUX_PREFIX="/data/data/com.termux/files/usr"
TERMUX_X11_PKG="com.termux.x11.next"

# ── Stop session ──────────────────────────────────────────────────────────────
if [[ "$STOP" -eq 1 ]]; then
    echo "Arrêt de la session X11 :${DISPLAY_NUM}..."
    pkill -f "Xwayland :${DISPLAY_NUM}" 2>/dev/null || true
    pkill -f "termux-x11"               2>/dev/null || true
    pkill -f "$WM"                      2>/dev/null || true
    echo "Session arrêtée."
    exit 0
fi

# ── Vérifications ─────────────────────────────────────────────────────────────
check_package() {
    if ! "$TERMUX_PREFIX/bin/dpkg" -l "$1" &>/dev/null; then
        echo "ERREUR : paquet '$1' non installé."
        echo "         Installe-le avec : pkg install $1"
        exit 1
    fi
}

echo "=== Termux.Wayland — Setup X11 ==="
echo "Display : $DISPLAY"

check_package "termux-x11-nightly"

# ── Lancer Termux:X11 si pas déjà actif ──────────────────────────────────────
if ! pgrep -f "Xwayland :${DISPLAY_NUM}" > /dev/null 2>&1; then
    echo "Démarrage de Termux:X11..."
    # Lancer l'activité Termux:X11 via Android
    am start \
        --user 0 \
        -n "${TERMUX_X11_PKG}/.MainActivity" \
        -e "DISPLAY" ":${DISPLAY_NUM}" \
        > /dev/null 2>&1 || true

    # Attendre que le serveur X soit prêt
    echo -n "Attente du serveur X"
    for i in $(seq 1 20); do
        sleep 0.5
        if "$TERMUX_PREFIX/bin/xdpyinfo" -display ":${DISPLAY_NUM}" > /dev/null 2>&1; then
            echo " OK"
            break
        fi
        echo -n "."
        if [[ "$i" -eq 20 ]]; then
            echo ""
            echo "ERREUR : timeout — le serveur X:${DISPLAY_NUM} ne répond pas."
            exit 1
        fi
    done
else
    echo "Serveur X:${DISPLAY_NUM} déjà actif."
fi

# ── Variables d'environnement X11 ─────────────────────────────────────────────
export DISPLAY=":${DISPLAY_NUM}"
export PULSE_SERVER="127.0.0.1"
export XDG_RUNTIME_DIR="${TMPDIR:-/tmp}"

# Écrire dans un fichier source pour que d'autres scripts puissent les charger
ENV_FILE="/data/data/com.termux.wayland/files/scripts/system/.x11_env"
cat > "$ENV_FILE" << ENV
export DISPLAY=":${DISPLAY_NUM}"
export PULSE_SERVER="127.0.0.1"
export XDG_RUNTIME_DIR="${TMPDIR:-/tmp}"
ENV
echo "Environnement X11 sauvegardé dans .x11_env"

# ── Lancer un window manager si demandé ───────────────────────────────────────
if [[ -n "$WM" ]]; then
    check_package "$WM"
    echo "Lancement du WM : $WM"
    nohup "$TERMUX_PREFIX/bin/$WM" > /tmp/wm.log 2>&1 &
    sleep 1
    echo "WM $WM démarré (PID $!)"
fi

echo ""
echo "Session X11 prête sur DISPLAY=:${DISPLAY_NUM}"
echo "Lance tes apps avec : DISPLAY=:${DISPLAY_NUM} nom-app"
echo "Ou utilise launch-app.sh"
