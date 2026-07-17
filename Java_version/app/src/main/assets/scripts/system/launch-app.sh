#!/data/data/com.termux/files/usr/bin/bash
# =============================================================================
# launch-app.sh — Termux.Wayland
# Lance une application Linux graphique via la session X11 active.
#
# Usage :
#   ./launch-app.sh firefox
#   ./launch-app.sh --display 2 gimp
#   ./launch-app.sh --detach vlc        → lance en arrière-plan
# =============================================================================

set -uo pipefail

TERMUX_PREFIX="/data/data/com.termux/files/usr"
DISPLAY_NUM=1
DETACH=0
APP=""

while [[ $# -gt 0 ]]; do
    case "$1" in
        --display) DISPLAY_NUM="$2"; shift 2 ;;
        --detach)  DETACH=1;         shift   ;;
        -h|--help)
            echo "Usage: launch-app.sh [--display N] [--detach] <commande>"
            exit 0 ;;
        *)
            # Tout le reste = commande à lancer
            APP="$*"
            break ;;
    esac
done

if [[ -z "$APP" ]]; then
    echo "ERREUR : aucune application spécifiée."
    echo "Usage: launch-app.sh [--display N] [--detach] <commande>"
    exit 1
fi

# ── Charger l'env X11 sauvegardé par setup-x11.sh ────────────────────────────
ENV_FILE="/data/data/com.termux.wayland/files/scripts/system/.x11_env"
if [[ -f "$ENV_FILE" ]]; then
    source "$ENV_FILE"
else
    # Fallback si setup-x11.sh n'a pas encore tourné
    export DISPLAY=":${DISPLAY_NUM}"
    export PULSE_SERVER="127.0.0.1"
    export XDG_RUNTIME_DIR="${TMPDIR:-/tmp}"
fi

# ── Vérifier que le serveur X est actif ───────────────────────────────────────
if ! "$TERMUX_PREFIX/bin/xdpyinfo" -display "$DISPLAY" > /dev/null 2>&1; then
    echo "ERREUR : aucun serveur X sur DISPLAY=$DISPLAY"
    echo "Lance d'abord : setup-x11.sh"
    exit 1
fi

# ── PATH Termux ───────────────────────────────────────────────────────────────
export PATH="$TERMUX_PREFIX/bin:$PATH"
export LD_LIBRARY_PATH="$TERMUX_PREFIX/lib:${LD_LIBRARY_PATH:-}"

# ── Lancement ─────────────────────────────────────────────────────────────────
echo "Lancement : $APP (DISPLAY=$DISPLAY)"

if [[ "$DETACH" -eq 1 ]]; then
    nohup bash -c "$APP" > "/tmp/launch-$(echo "$APP" | awk '{print $1}').log" 2>&1 &
    echo "PID : $!"
else
    exec bash -c "$APP"
fi
