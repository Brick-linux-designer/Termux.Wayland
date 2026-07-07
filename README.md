# Termux Wayland
# com.termux.wayland
## Linux in your pocket, Wayland in your terminal

Termux Wayland brings a complete Linux graphical environment to Android using modern Wayland technologies and Android free mode API.
Built around Termux, Wayland and Weston, the project aims to provide a lightweight, fast and flexible desktop experience directly on your phone or tablet, it in Android x86 on compunter.
#### ⚠️ Warning the code is séparated in two versions Java that is the same language for Ternux & Termux x11, actualy just Kitlin version work.

---

# Features

- Native Wayland graphical session
- Lightweight and optimized for mobile devices
- Real Linux environment with APT
- Sandboxed
- No root required, but its better with root
- Keyboard & mouse support
- Touchscreen friendly
- Dynamic window resizing
- Hardware accelerated rendering support
- Simple app launcher system
- Modern dark UI inspired by Linux desktops

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
| Termux Wayland | Widgets, apps, Android Free Mode windows, Android styled like Ubuntu Touch & Android |
| Weston | Reference Wayland compositor |
| XWayland | Legacy X11 app compatibility |
| PulseAudio / PipeWire | Audio support |
| Mesa | OpenGL rendering |

---

# Goals

The goal of Termux Wayland is to create:

- a modern Linux desktop environment within Android
- with native support,
- fully touch compatible,
- lightweight enough for mobile hardware,
- easy to install and maintain,
- developer friendly,
- open-source and customizable.

---

# Installation

Downoload the free app from Github.
On Termux (downoload on: https://github.com/termux/termux-app/releases or  in F-droid https://f-droid.org/en/packages/com.termux or in Google PlayStore for Android 11+):
```bash
pkg update && pkg upgrade #chec and install updates

pkg install x11-repo #later wayland-repo

pkg install root-repo #if you are rooted

pkg install git

git clone https://github.com/Brick-linux-designer/Termux.Wayland.git

cd Termux.Wayland
```
And install the last version of APK for your device. In the folder or go to https://github.com/Brick-linux-designer/Termux.Wayland/relases .

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
- [ ] XFCE / KDE support
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

MIT License

Copyright (c) 2026 Brick-linux-designer

Copiright (c) 2026 Termux (for the logos & some pictures in /.github/static and the source codes off termux & Termux x11 that I (Brick-linux-designer) will use soon

---

# Why Wayland?

| Wayland | X11 |
|---|---|
| Lower latency | Older architecture |
| Better rendering pipeline | More overhead |
| More secure | Legacy design |
| Better mobile integration | Desktop-first design |
| Termux Wayland (now no x11 & no wayland after x11 and after wayland if i have 10 stars & 5contributors | Termux x11 |

---

# Inspired By

- Termux
- Termux:X11
- Ubuntu Touch & Android
- Wayland
- Linux Phone
- Alpine Linux

---

# Vision

A real Linux desktop in your pocket without changing or replacing Android.

Open. Lightweight. Modern. Mobile-first.

---

# Status

The app and 100% compilable code will be coming soon.
