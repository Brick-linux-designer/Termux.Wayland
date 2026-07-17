#!/bin/bash

# Script complet pour supprimer l'historique Git
# Supprime les fichiers gitignorés et garde uniquement les fichiers actuels
# Utilisation: bash clean-history-complete.sh

set -e

echo "=== Suppression complète de l'historique Git ==="
echo "⚠️  Cette action est IRRÉVERSIBLE!"
echo ""

# Vérifier qu'on est dans un dépôt git
if [ ! -d .git ]; then
    echo "❌ Erreur: Ce n'est pas un dépôt Git"
    exit 1
fi

CURRENT_BRANCH=$(git rev-parse --abbrev-ref HEAD)
echo "📍 Branche actuelle: $CURRENT_BRANCH"
echo ""

# Afficher ce qui sera supprimé
echo "📋 Fichiers qui seront supprimés (ignorés par .gitignore):"
git clean -fdnX | head -20
if [ $(git clean -fdnX | wc -l) -gt 20 ]; then
    echo "   ... et d'autres fichiers"
fi
echo ""

# Demander confirmation finale
read -p "⚠️  Confirmez-vous la suppression COMPLÈTE? (tapez 'OUI' pour continuer): " confirmation
if [ "$confirmation" != "OUI" ]; then
    echo "❌ Opération annulée"
    exit 1
fi

echo ""
echo "🔄 Opération en cours..."
echo ""

# 1. Supprimer les fichiers ignorés
echo "🗑️  Suppression des fichiers gitignorés..."
git clean -fdX

# 2. Créer un commit orphelin (sans parent)
echo "📌 Création d'un nouveau commit orphelin..."
git checkout --orphan nouveau_historique

# 3. Ajouter tous les fichiers actuels (sauf gitignorés)
echo "📦 Ajout des fichiers actuels..."
git add -A

# 4. Créer le premier commit
echo "✍️  Création du commit initial..."
git commit -m "Initial commit - Historique nettoyé"

# 5. Supprimer l'ancienne branche
git branch -D $CURRENT_BRANCH

# 6. Renommer la nouvelle branche avec l'ancien nom
git branch -m $CURRENT_BRANCH

echo ""
echo "✅ Historique supprimé avec succès!"
echo ""
echo "📊 Vérification:"
echo "---"
git log --oneline -5
echo "---"
echo ""
echo "⏭️  Prochaines étapes:"
echo "  1. Vérifier les fichiers: git status"
echo "  2. Pousser les changements: git push origin $CURRENT_BRANCH --force"
echo ""
echo "⚠️  IMPORTANT: Utilisez --force car vous changez l'historique!"
