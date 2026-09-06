#!/data/data/com.termux/files/usr/bin/bash
#
# kill-session.sh
# Ferme proprement la session termux-x11 et, en option, les process GUI
# lances via launch-app.sh.
#
# Usage: kill-session.sh [--all]
#   --all : tue aussi tous les process GUI courants (best-effort)

set -uo pipefail

PIDFILE="$PREFIX/var/run/termux-x11.pid"

log() { echo "[kill-session] $*"; }

if [[ -f "$PIDFILE" ]]; then
    X11_PID="$(cat "$PIDFILE")"
    if kill -0 "$X11_PID" 2>/dev/null; then
        log "Arret du serveur termux-x11 (pid $X11_PID)..."
        kill "$X11_PID" 2>/dev/null
    fi
    rm -f "$PIDFILE"
else
    log "Aucun pidfile trouve, tentative via pgrep..."
    pkill -f "termux-x11 :0" 2>/dev/null || true
fi

if [[ "${1:-}" == "--all" ]]; then
    log "Fermeture des apps GUI en cours (best-effort)..."
    for proc in firefox xterm xfce4-session xfce4-terminal mousepad ristretto vlc; do
        pkill -f "$proc" 2>/dev/null || true
    done
fi

log "Session terminee."
