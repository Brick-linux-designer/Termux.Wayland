#!/data/data/com.termux/files/usr/bin/bash
#
# setup-x11.sh
# Installe et prepare l'environnement X11 minimal dans Termux.
# Idempotent : peut etre relance sans casser une install existante.

set -euo pipefail

LOG_TAG="[setup-x11]"
log() { echo "$LOG_TAG $*"; }

log "Mise a jour des depots..."
pkg update -y

log "Ajout du x11-repo si absent..."
if ! pkg list-installed 2>/dev/null | grep -q '^x11-repo'; then
    pkg install -y x11-repo
fi

log "Installation des paquets X11 de base..."
pkg install -y \
    termux-x11-nightly \
    xorg-server \
    xorg-xrandr \
    xorg-xsetroot \
    pulseaudio \
    termux-api

log "Verification de la variable DISPLAY par defaut..."
PROFILE_FILE="$HOME/.bashrc"
if ! grep -q "export DISPLAY=:0" "$PROFILE_FILE" 2>/dev/null; then
    echo "export DISPLAY=:0" >> "$PROFILE_FILE"
    log "DISPLAY=:0 ajoute a $PROFILE_FILE"
fi

log "Setup X11 termine. Utilise launch-app.sh pour demarrer une app graphique."
