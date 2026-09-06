#!/data/data/com.termux/files/usr/bin/bash
#
# launch-app.sh
# Demarre (si besoin) la session X11 termux-x11 puis lance la commande
# passee en argument avec DISPLAY correctement positionne.
#
# Usage: launch-app.sh <commande_exec> [args...]
# Exemple: launch-app.sh firefox
#          launch-app.sh xterm

set -euo pipefail

DISPLAY_NUM=":0"
PIDFILE="$PREFIX/var/run/termux-x11.pid"

log() { echo "[launch-app] $*"; }

if [[ $# -eq 0 ]]; then
    echo "Usage: $0 <commande> [args...]"
    exit 1
fi

mkdir -p "$(dirname "$PIDFILE")"

is_x11_running() {
    pgrep -f "termux-x11 $DISPLAY_NUM" >/dev/null 2>&1
}

if ! is_x11_running; then
    log "Demarrage de la session termux-x11 sur $DISPLAY_NUM..."
    termux-x11 "$DISPLAY_NUM" &
    echo $! > "$PIDFILE"
    # Attente que le serveur X soit pret
    for i in $(seq 1 20); do
        if is_x11_running; then
            break
        fi
        sleep 0.5
    done
fi

export DISPLAY="$DISPLAY_NUM"
export PULSE_SERVER=127.0.0.1

log "Lancement de: $* (DISPLAY=$DISPLAY)"
exec "$@"
