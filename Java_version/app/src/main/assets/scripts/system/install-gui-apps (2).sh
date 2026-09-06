#!/data/data/com.termux/files/usr/bin/bash
#
# install-gui-apps.sh
# Installe une ou plusieurs apps GUI depuis la base curatee.
# Usage: install-gui-apps.sh <package1> [package2] ...
#        install-gui-apps.sh --list   (affiche la liste suggeree)

set -euo pipefail

SUGGESTED_APPS=(
    firefox
    xterm
    xfce4
    xfce4-terminal
    thunar
    mousepad
    ristretto
    vlc
)

if [[ "${1:-}" == "--list" ]]; then
    echo "Apps GUI suggerees :"
    printf '  - %s\n' "${SUGGESTED_APPS[@]}"
    exit 0
fi

if [[ $# -eq 0 ]]; then
    echo "Usage: $0 <package1> [package2] ..."
    echo "       $0 --list"
    exit 1
fi

pkg update -y

for app in "$@"; do
    echo "[install-gui-apps] Installation de: $app"
    pkg install -y "$app" || echo "[install-gui-apps] ECHEC pour $app, on continue."
done

echo "[install-gui-apps] Termine."
