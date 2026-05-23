# Termux Wayland (Linux in your pocket, Wayland in your terminal)
# com.termux.wayland

 Termux Wayland brings a complete Linux graphical environment to Android using modern Wayland technologies.
Built around Termux, Wayland and Weston, the project aims to provide a lightweight, fast and flexible desktop experience directly on your phone or tablet.

# Features
Native Wayland graphical session
Lightweight and optimized for mobile devices
Real Linux environment with APT
Sandboxed
No root required
Keyboard & mouse support
Touchscreen friendly
Dynamic window resizing
Hardware accelerated rendering support
Simple app launcher system
Modern dark UI inspired by Linux desktops
Preview

# Architecture

Android
   └── Termux
   |     └── Linux Environment (Termux,Alpine Linux,Debian,Ubuntu,Termux,Manjaro, [...] )
   |     └── Legacy X11-repo (later Wayland-repo)
   |          |
   └── Termux Wayland
         └── Android Wigets and Linux apps
         └── Legacy Termux X11 compositor (later Weston (Wayland compositor))
                └── Linux GUI Applications

# 📦 Included Components

Component	Description
Termux => Android terminal environment
Termux PRoot => Debian,Alpine Linux,Manjaro,Ubuntu,...: Linux userspace
Termux Wayland => widgets,apps,Android PIP windows,Android styled like Ubuntu touch & Android
Termux Wayland => Weston:	Reference Wayland compositor
Termux Wayland & Termux X11 => XWayland:	Legacy X11 app compatibility
Termux & Termux Wayland & Termux X11 =>PulseAudio / PipeWire	Audio support Mesa	OpenGL rendering

# 🚀 Goals

The goal of Termux Wayland is to create:

a modern Linux desktop environment for Android,
fully touch compatible,
lightweight enough for mobile hardware,
easy to install and maintain,
developer friendly,
open-source and customizable.

# Why Wayland?

Compared to traditional X11 environments:

Wayland	X11
Lower latency	Older architecture
Better rendering pipeline	More overhead
More secure	Legacy design
Better mobile integration	Desktop-first design

# Installation (WIP)

pkg update && pkg upgrade
pkg install root-repo x11-repo
pkg install git
git clone https://github.com/Brick-linux-designer/Termux.Wayland/
cd /[chemin]/Termux.Wayland

# 🧩 Planned Features

 Full launcher UI
 Session manager
 Multi-window support
 GPU acceleration improvements
 Integrated app store
 Backup & restore
 Wayland gestures
 Containerized environments
 XFCE / KDE support
 Automatic setup scripts

# 🤝 Contributing

Contributions are welcome!
You can help by:

reporting bugs,
improving documentation,
testing on devices,
contributing code,
suggesting features.

# ⭐ Support The Project

If you like the project:

Give the repository a star ⭐
Share it with other Linux / Android enthusiasts
Contribute to development

# 📜 License

MIT License

# ❤️ Inspired By
Termux
Termux:X11
Ubuntu Touch & Android
Ubuntu
Wayland
Weston
Debian
Alpine Linux

# 🌍 Vision

A real Linux desktop in your pocket with out change or move Android.
Open. Lightweight. Modern. Mobile-first.

The app and source code will be availlable soon.
