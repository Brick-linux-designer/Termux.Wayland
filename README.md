# Termux Wayland (Linux in your pocket, Wayland in your terminal)

# com.termux.wayland

 Termux Wayland brings a complete Linux graphical environment to Android using modern Wayland technologies.
Built around Termux, Wayland and Weston, the project aims to provide a lightweight, fast and flexible desktop experience directly on your phone or tablet.

---

# ✨ Features

- Native Wayland graphical session
- Lightweight and optimized for mobile devices
- Real Linux environment with APT
- Sandboxed
- No root required
- Keyboard & mouse support
- Touchscreen friendly
- Dynamic window resizing
- Hardware accelerated rendering support
- Simple app launcher system
- Modern dark UI inspired by Linux desktops

---

# 🏗️ Architecture

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
      ├── Android PiP windows
      ├── Android styled like Ubuntu Touch & Android
      │
      ├── Legacy Termux:X11 compositor
      └── Weston (Wayland compositor)
           └── Linux GUI Applications
```

---

# 📦 Included Components

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

# 🚀 Goals

The goal of Termux Wayland is to create:

- a modern Linux desktop environment for Android,
- fully touch compatible,
- lightweight enough for mobile hardware,
- easy to install and maintain,
- developer friendly,
- open-source and customizable.

---

# ⚡ Why Wayland?

| Wayland | X11 |
|---|---|
| Lower latency | Older architecture |
| Better rendering pipeline | More overhead |
| More secure | Legacy design |
| Better mobile integration | Desktop-first design |

---

# 🔧 Installation (WIP)

```bash
pkg update && pkg upgrade

pkg install root-repo x11-repo

pkg install git

git clone https://github.com/Brick-linux-designer/Termux.Wayland.git

cd Termux.Wayland
```

---

# 🧩 Planned Features

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

# 🤝 Contributing

Contributions are welcome!

You can help by:

- reporting bugs,
- improving documentation,
- testing on devices,
- contributing code,
- suggesting features.

---

# ⭐ Support The Project

If you like the project:

- Give the repository a star ⭐
- Share it with other Linux / Android enthusiasts
- Contribute to development

---

# 📜 License

MIT License

Copyright (c) 2026 Brick-linux-designer

---

# ❤️ Inspired By

- Termux
- Termux:X11
- Ubuntu Touch & Android
- Ubuntu
- Wayland
- Weston
- Debian
- Alpine Linux

---

# 🌍 Vision

A real Linux desktop in your pocket without changing or replacing Android.

Open. Lightweight. Modern. Mobile-first.

---

# 📌 Status

The app and source code will be available soon.
