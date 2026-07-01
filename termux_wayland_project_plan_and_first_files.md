# Termux Wayland Project — Practical Start

## Vision UX importante

Le projet ne cherche PAS à créer un environnement virtuel Linux compliqué.

L’objectif est :

```text
Transformer Android en lui ajoutant des capacités Linux desktop
sans root
sans configuration complexe
et avec une UX Android native.
```

Le projet doit rester :

- simple pour utilisateurs Android
- léger
- intégré au launcher Android
- compatible widgets/raccourcis Android
- proche d’un vrai système hybride Android + Linux.

---

# Fonctionnalités UX prévues

## Docks Android en widgets

L’application pourra créer :

- docks Linux-style sur écran d’accueil
- launchers flottants
- widgets 1×1
- rangées d’applications Linux
- mini taskbars Android
- menus applications Linux.

---

## Raccourcis fichiers Android → Linux

L’utilisateur pourra créer :

- raccourcis vers fichiers
- ouverture directe de documents
- “Open with Linux app”
- associations fichiers Linux.

Exemples :

```text
PDF → Okular Linux
ZIP → File Roller
HTML → Firefox Linux
TXT → Mousepad
```

---

## Philosophie du projet

Le projet doit :

✅ étendre Android
✅ intégrer plus de vrai Linux naturellement
✅ garder l’UX Android
✅ éviter les desktops virtuels lourds
✅ éviter les configurations compliquées
✅ fonctionner sans root

et NON :

❌ remplacer Android
❌ créer une VM complète
❌ imposer un bureau Linux classique.


## Goal

Create a single Android app:

```text
com.termux.wayland
```

that:

- reuses existing Termux packages
- scans Linux desktop apps (.desktop)
- launches Linux apps individually
- uses Termux:X11 rendering backend initially
- later migrates to Wayland
