# Termux XWayland
### com.termux.wayland(.kotlin/.beta/.kotlin.beta)
### Linux in your pocket, Wayland in your terminal

#### Disclamer: this project is not supported aproved or made by Termux devlopers.
Termux Wayland brings a complete Linux graphical environment to Android using modern Wayland technologies and Android free mode API.
Built around Termux, Wayland and Weston, the project aims to provide a lightweight, fast and flexible desktop experience directly on your phone or tablet, it in Android x86 on compunter.
##### ⚠️ Warning the code is séparated in two versions Java that is up to date & the same language for Ternux & Termux x11 & Kotlin that is not updated & no longer supported. And only the README.md & the LICENCE files are 100% in English; other files are only in French or in English & in french.

---

# Features

- Native Wayland or X11 graphical session
- Lightweight and very optimized for mobile devices
- Real Linux environment with APT
- Sandboxed
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
- [ ] Backup & restore
- [ ] Wayland gestures
- [ ] Containerized environments
- [ ] XFCE4 / KDE Plasma / MATE / LXDE / LXQt / Cinnamon support 
- [ ] Automatic setup scripts

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

Copiright (c) 2026 Termux developers (
---

# Why Wayland?

| Wayland | X11 |
|---|---|
| Lower latency | Older architecture |
| Better rendering pipeline | More overhead |
| More secure | Legacy design |
| Better mobile integration | Desktop-first design |
| Termux Wayland (now no x11 & no wayland after x11 and after wayland if i have 7 stars | Termux x11 |

---

# Inspired By

- Termux
- Termux x11
- Wayland
- Ubuntu Touch & Android
- Linux Phone
- Alpine Linux

---

# Vision

A real Linux desktop on your phone without changing or replacing Android or other distro in Android branch
Open. Lightweight. Modern. Mobile & Linux-first.

Doesn't exist officialy by Brick-linux-designer on iOS or Windows phone but is compilable on Chrome OS.

---

# Status

The Kotlin version is at end of support & the Java version will work & integrate Termux:x11 source code soon.
