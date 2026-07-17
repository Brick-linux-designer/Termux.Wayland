#! /usr/bin/bash
# Termux
# ==========================================================================
# This file is hear to help you to install softwares on Termux & PRoot on Termux.
# Ce fichier est ici pour vous aider à installer des logiciels sur Termux & PRoot sur Termux.
# ==========================================================================

# pour installer un logiciel sur Termux utiliser: pkg install par ex: ⤦
pkg install firefox # ou ⤦
pkg install nano

# pour chercher une mise à jour effectuer
pkg update

# pour mettre à jour
pkg upgrade

# pour installer une disto sur proot distro
proot-distro install [nom-de-la-distro] #ex Debian
proot-distro install debian

# pour lancer la distro en CLI
proot-distro login [nom-de-la-distro]

# pour la lancer sur Termux x11 ou Termux Wayland
export DISPLAY=:1
termux-x11 :1 &
proot-distro login debian --env DISPLAY=:1 --env PULSE_SERVER=127.0.0.1 -- startxfce4
