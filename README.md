# Termux Wayland
# com.termux.wayland
## Linux in your pocket, Wayland in your terminal

 Termux Wayland brings a complete Linux graphical environment to Android using modern Wayland technologies and Android free mode API.
Built around Termux, Wayland and Weston, the project aims to provide a lightweight, fast and flexible desktop experience directly on your phone or tablet, it in Android x86 on compunter.

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
 │    │     ├── Termux
 │    │     ├── Alpine Linux
 │    │     ├── Debian
 │    │     ├── Ubuntu
 │    │     ├── Manjaro
 │    │     └── [...]
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
| Termux Wayland | Widgets, apps, Android PiP windows, Android styled like Ubuntu Touch & Android |
| Weston | Reference Wayland compositor |
| XWayland | Legacy X11 app compatibility |
| PulseAudio / PipeWire | Audio support |
| Mesa | OpenGL rendering |

---

# Goals

The goal of Termux Wayland is to create:

- a modern Linux desktop environment for Android,
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

---

# Planned Features

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

---

# Why Wayland?

| Wayland | X11 |
|---|---|
| Lower latency | Older architecture |
| Better rendering pipeline | More overhead |
| More secure | Legacy design |
| Better mobile integration | Desktop-first design |

---

# Inspired By

- Termux
- Termux:X11
- Ubuntu Touch & Android
- Wayland
- Alpine Linux
- VNC

---

# Vision

A real Linux desktop in your pocket without changing or replacing Android.

Open. Lightweight. Modern. Mobile-first.

---

# Status

The app and source code will be available soon.
