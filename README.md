# Termux XWayland (Bêta)
### com.termux.wayland(.kotlin/.beta/.kotlin.beta)
### Linux in your pocket, Wayland in your terminal

#### Disclamer: this project is not supported aproved or made by Termux devlopers.
Termux Wayland brings a complete Linux graphical environment to Android using modern Wayland technologies and Android free mode API.
Built around Termux, Wayland and Weston, the project aims to provide a lightweight, fast and flexible desktop experience directly on your phone or tablet, it in Android x86 on compunter.
##### ⚠️ Warning the original code at https://github.com/Brick-linux-designer/Termux.Wayland/ is séparated in two versions Java that is up to date & the same language for Ternux & Termux x11 & Kotlin that is not updated & not supported. And only the README.md & the LICENCE files are 100% in English; other files are only in French or in English & in french, and https://github.com/cakroni1580/Termux.Wayland is a fork it' s not him maitain this original repo at https://github.com/Brick-linux-designer/Termux.Wayland/ .

[<img alt="Get it on GitHub" src="https://raw.githubusercontent.com/Kunzisoft/Github-badge/main/get-it-on-github.png" width="240">](https://github.com/Brick-linux-designer/Termux.Wayland/releases)

---

# Features

- Native Wayland or X11 graphical session
- Lightweight and very optimized for mobile devices
- Real Linux environment with APT
- No root required, but it can be better with root
- Touchscreen friendly
- Keyboard & mouse support if your Android support
- Dynamic window resizing
- Hardware accelerated rendering support
- Simple app launcher system
- Modern UI inspired by Linux Phone & actual Linux desktops

---

# Architecture

```text
Android
 ├── Termux
 │    ├── Linux Environment
 │    │     └── Termux App (directly but limited or in PRoot ⤦)
 |    |          └── PRoot
 │    │                ├── Alpine
 │    │                ├── Debian
 │    │                ├── Ubuntu
 │    │                ├── Manjaro
 │    │                └── [...]
 │    │
 │    └── Legacy x11-repo
 │          └── (later Wayland-repo)
 │
 └── Termux Wayland
      ├── Android Widgets & Linux apps
      ├── Android FreeMode windows
      ├── Android styled like Ubuntu Touch & Android
      │
      ├── Legacy Termux:X11 compositor
      └── Weston (Wayland compositor)
           └── Linux GUI Applications
```

---

# Included Components

| Component | Description |
|---|---|
| Termux | Android terminal environment |
| Termux PRoot | Debian, Alpine Linux, Manjaro, Ubuntu... Linux userspace |
| Termux Wayland | Widgets, apps, Android Free Form windows, Android styled like Pure Linux Phone |
| Weston | Reference Wayland compositor |
| XWayland | Legacy X11 compatibility |
| PulseAudio / PipeWire | Audio support |
| Mesa | OpenGL rendering |

---

# Goals

The goal of Termux Wayland is to create:

- a modern Linux desktop environment within Android
- native support,
- fully touch compatible,
- lightweight enough for mobile hardware,
- easy to install and maintain,
- developer friendly,
- open-source and very customizable.

---

# Installation

Downoload the free Termux app from Github.
(downoload on: https://github.com/termux/termux-app/releases or  in F-droid https://f-droid.org/en/packages/com.termux or in Google PlayStore for Android 11+):
```bash
pkg update && pkg upgrade # chec and install updates

pkg install x11-repo #later wayland-repo or just a custom package (by Brick-linux-designer)
pkg install root-repo #if you are rooted
pkg install git #install git to clone the repo

mkdir /data/data/com.termux/files/home/Downolods || echo The directory Downoloads olready exists
cd /data/data/com.termux/files/home/Downolods

git clone https://github.com/Brick-linux-designer/Termux.Wayland.git #You maybe have to authentificate if it's the first time you use git comand
cd /data/data/com.termux/files/home/Downolods/Termux.Wayland
```
And install the last version of APK for your device. In the folder or go to https://github.com/Brick-linux-designer/Termux.Wayland/releases .

---

# Planned Features (becoming soon)

- [ ] Full launcher UI
- [ ] Session manager
- [ ] Multi-window support
- [ ] GPU acceleration improvements
- [ ] Integrated app store
- [ ] Wayland gestures
- [ ] Containerized environments
- [ ] XFCE4 / KDE Plasma / MATE / LXDE / LXQt / Cinnamon support 
- [ ] Automatic setup scripts
- [ ] Backup & restore

---

# Contributing

Contributions are welcome!

You can help by:

- reporting bugs,
- improving documentation,
- testing on devices,
- contributing code,
- suggesting features.

---

# Support The Project

If you like the project:

- Give a star to the repository
- Share it with other Termux/Linux/Android/Linux phone enthusiasts/fans
- Contribute to development

---

# License

## GNU GENERAL PUBLIC LICENSE v3

Version 3, 29 June 2007
Copyright © 2007 Free Software Foundation, Inc.
https://www.fsf.org/

Everyone is permitted to copy and distribute verbatim copies of this license document, but changing it is not allowed.

[The complete GPLv3 license text is available from the official GNU website.]
https://www.gnu.org/licenses/gpl-3.0.txt

## GPL v3
Copiright (c) 2026 Termux developers (for the logos & some pictures (and the source codes off termux & Termux x11 that I (Brick-linux-designer) will use soon).

Copiright (c) 2026 Termux developers

---

# Wayland vs x11 

| Wayland | X11 |
|---|---|
| Architecture moderne | Architecture historique |
| Latence potentiellement plus faible | Latence variable |
| Pipeline de rendu plus direct | Architecture avec davantage de couches |
| Meilleure isolation des applications | Isolation plus limitée par défaut |
| Conçu pour remplacer X11 | Ancien système d'affichage |
| Meilleure gestion du HiDPI selon l'environnement | HiDPI parfois moins uniforme |
| Gestion moderne du tactile selon le compositeur | Tactile dépendant des outils |
| Utilise un compositeur Wayland | Utilise un serveur X11 |
| Compatible avec XWayland | Compatible avec de nombreuses applications anciennes |
| Compatibilité variable selon les applications | Large compatibilité logicielle |
| Termux : nécessite un compositeur Wayland adapté | Termux:X11 fournit un serveur X11 |
| Configuration variable | Configuration souvent plus simple |
| Performances variables selon le GPU et le rendu | Performances variables selon le GPU et le rendu |
| Adapté aux environnements modernes | Adapté aux bureaux Linux classiques |

---

| Termux:X11 | Termux:Wayland |
|---|---|
| Utilise le protocole X11 | Utilise le protocole Wayland |
| Serveur X11 pour Android | Compositeur Wayland compatible avec Android |
| Compatible avec XFCE et LXQt | Compatible avec les environnements Wayland adaptés |
| Large compatibilité avec les applications X11 | Nécessite des applications Wayland ou XWayland |
| Configuration généralement simple | Configuration variable selon le compositeur |
| Bonne compatibilité avec les bureaux Linux classiques | Adapté aux bureaux Linux modernes |
| Peut avoir davantage de surcharge selon le rendu | Peut avoir moins de surcharge selon le rendu |
| Performances dépendantes du GPU et du rendu | Performances dépendantes du GPU et du rendu |
| Prise en charge des applications X11 | Prise en charge des applications Wayland |
| Idéal pour faire tourner XFCE dans Termux | Idéal pour expérimenter avec Wayland dans Termux |
| Application Android : Termux:X11 | Nécessite une solution Wayland adaptée |
| Maturité et documentation importantes | Configuration plus dépendante de la solution utilisée |
| Recommandé pour un bureau Linux stable et pratique | Recommandé pour tester un environnement Wayland |

---

# Inspired By

- Termux x11
- Termux
- Wayland
- Ubuntu Touch & Android
- Linux Phone
- Alpine Linux
- PostmarketOS

---

# Vision

A real Linux desktop on your phone without changing or replacing Android or other distro in Android branch
Open. Lightweight. Modern. Mobile & Linux-first.

Doesn't exist officialy by Brick-linux-designer on iOS or Windows phone but is compilable on Chrome OS.

---

# Status

The Kotlin version is at end of support & the Java version will work & integrate Termux:x11 source code soon.
