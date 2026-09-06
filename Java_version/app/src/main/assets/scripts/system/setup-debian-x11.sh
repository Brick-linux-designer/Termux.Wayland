#!/data/data/com.termux/files/usr/bin/bash
# ============================================================
#  setup-debian-x11.sh
#  Setup complet Debian + XFCE4 via proot-distro + Termux X11
#  Cible : Android x86_64
# ============================================================

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

log()  { echo -e "${GREEN}[✔]${NC} $1"; }
warn() { echo -e "${YELLOW}[!]${NC} $1"; }
err()  { echo -e "${RED}[✘]${NC} $1"; exit 1; }
info() { echo -e "${CYAN}[→]${NC} $1"; }

echo ""
echo -e "${CYAN}╔══════════════════════════════════════════╗${NC}"
echo -e "${CYAN}║   Debian XFCE4 + Termux X11 Setup        ║${NC}"
echo -e "${CYAN}║   Android x86_64                         ║${NC}"
echo -e "${CYAN}╚══════════════════════════════════════════╝${NC}"
echo ""

# ------------------------------------------------------------
# 1. Mise à jour de Termux
# ------------------------------------------------------------
info "Mise à jour des paquets Termux..."
pkg update -y && pkg upgrade -y
log "Termux à jour"

# ------------------------------------------------------------
# 2. Paquets Termux nécessaires
# ------------------------------------------------------------
info "Installation des paquets Termux requis..."
pkg install -y x11-repo
pkg install -y termux-x11-nightly proot-distro pulseaudio
log "Paquets Termux installés"

# ------------------------------------------------------------
# 3. Installation de Debian via proot-distro
# ------------------------------------------------------------
if proot-distro list | grep -q "debian.*INSTALLED"; then
    warn "Debian déjà installé, on passe."
else
    info "Installation de Debian (proot-distro)..."
    proot-distro install debian
    log "Debian installé"
fi

# ------------------------------------------------------------
# 4. Setup intérieur Debian
# ------------------------------------------------------------
info "Configuration de Debian (XFCE4, dbus, outils)..."

proot-distro login debian -- bash -c '
set -e

export DEBIAN_FRONTEND=noninteractive

apt update -y
apt upgrade -y

# Desktop + utilitaires essentiels
apt install -y \
    xfce4 \
    xfce4-terminal \
    xfce4-taskmanager \
    xfce4-whiskermenu-plugin \
    dbus-x11 \
    x11-xserver-utils \
    xterm \
    wget curl git nano vim \
    htop neofetch \
    fonts-noto fonts-liberation \
    gtk2-engines-murrine \
    --no-install-recommends

# Nettoyer
apt autoremove -y
apt clean

echo "[✔] Debian configuré"
'

log "Debian prêt"

# ------------------------------------------------------------
# 5. Script de démarrage du bureau
# ------------------------------------------------------------
info "Création du script start-desktop.sh..."

cat > "$HOME/start-desktop.sh" << 'EOF'
#!/data/data/com.termux/files/usr/bin/bash
# ----------------------------------------
#  Démarre Termux X11 + XFCE4 sous Debian
# ----------------------------------------

SOCKET_DIR="/data/data/com.termux/files/usr/tmp/.X11-unix"
RUNTIME_DIR="/tmp/runtime-root"

echo "[→] Arrêt des instances précédentes..."
pkill -f termux-x11 2>/dev/null
pkill -f xfce4-session 2>/dev/null
sleep 1

echo "[→] Démarrage de Termux X11..."
termux-x11 :1 &
sleep 2

# Vérifier que le socket existe
if [ ! -d "$SOCKET_DIR" ]; then
    echo "[✘] Socket X11 introuvable : $SOCKET_DIR"
    echo "    Ouvre l'app Termux X11 sur Android et réessaie."
    exit 1
fi

echo "[→] Lancement de XFCE4 dans Debian..."
proot-distro login debian \
    --env DISPLAY=:1 \
    --bind "$SOCKET_DIR:/tmp/.X11-unix" \
    --env XDG_RUNTIME_DIR="$RUNTIME_DIR" \
    --env LANG=fr_FR.UTF-8 \
    -- bash -c "
        mkdir -p $RUNTIME_DIR
        chmod 700 $RUNTIME_DIR
        dbus-launch --exit-with-session xfce4-session 2>/dev/null
    "
EOF

chmod +x "$HOME/start-desktop.sh"
log "Script start-desktop.sh créé dans ~/"

# ------------------------------------------------------------
# 6. Script d'arrêt
# ------------------------------------------------------------
cat > "$HOME/stop-desktop.sh" << 'EOF'
#!/data/data/com.termux/files/usr/bin/bash
echo "[→] Arrêt du bureau..."
pkill -f xfce4-session 2>/dev/null
pkill -f termux-x11 2>/dev/null
echo "[✔] Bureau arrêté"
EOF

chmod +x "$HOME/stop-desktop.sh"
log "Script stop-desktop.sh créé dans ~/"

# ------------------------------------------------------------
# 7. Résumé final
# ------------------------------------------------------------
echo ""
echo -e "${CYAN}╔══════════════════════════════════════════╗${NC}"
echo -e "${CYAN}║              Setup terminé !             ║${NC}"
echo -e "${CYAN}╚══════════════════════════════════════════╝${NC}"
echo ""
echo -e "  ${GREEN}Pour démarrer le bureau :${NC}"
echo -e "    1. Ouvre l'app ${YELLOW}Termux X11${NC} sur Android"
echo -e "    2. Lance : ${CYAN}~/start-desktop.sh${NC}"
echo ""
echo -e "  ${GREEN}Pour arrêter :${NC}"
echo -e "    ${CYAN}~/stop-desktop.sh${NC}"
echo ""
warn "Si l'écran est noir : Settings → Window Manager Tweaks → Compositor → désactiver"
echo ""
