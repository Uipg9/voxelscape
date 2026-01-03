# 🎮 Voxel Scape

**An immersive quest adventure mod for Minecraft 1.21.11 (Fabric)**

Inspired by Old School RuneScape, Voxel Scape brings a comprehensive quest system to Minecraft with 150 progressive quests, a currency system, upgradeable perks, and collectible tracking.

## ✨ Features

### 📜 Quest System
- **150 Progressive Quests** organized into 6 chapters
- **Quest Points Currency** - Earn 1-6 QP per quest (450 total)
- **Hero's Journal** - A mystical quest book given to all new players
- **Auto-tracking** - All quests activate automatically
- **Smart Dependencies** - Quests unlock as you progress

### 🛍️ Perk Shop
- **14 Unique Perks** across 5 categories
  - **Movement**: Speed Boost, Magnetism I/II
  - **Utility**: Night Vision, Auto Replant, Aqua Affinity
  - **Combat**: Strength, Resistance
  - **Mining**: Lucky Miner, Efficiency, Fortune
  - **Special**: XP Multiplier, Soul Keeper, Head Hunter
- **Spend Quest Points** to unlock permanent upgrades
- **Prerequisite System** - Some perks require others first

### 🏛️ Collection Log
- **31 Rare Collectibles** to discover
  - 16 Music Discs
  - 8 Rare Mob Drops
  - 7 Mob Heads
- **Auto-tracking** - Automatically detected when you obtain items
- **Progress Tracking** - See what you've collected at a glance
- **Discovery Notifications** - Fancy messages when you find new items

### 🎵 Sound Effects
- Page turn sounds when opening GUIs
- Ka-ching sound effect for perk purchases
- High-pitched chime for collection discoveries

## 📋 Commands

- `/quests` - Open the Hero's Journal quest list
- `/quest <id>` - View details of a specific quest
- `/quest start <id>` - Start a quest
- `/quest complete <id>` - Complete a quest (if requirements met)
- `/quest claim` - Claim all pending rewards
- `/perks` - Open the Perk Shop
- `/collection` - Open the Collection Log

## 🔧 Installation

1. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft 1.21.11
2. Download [Fabric API](https://modrinth.com/mod/fabric-api)
3. Download the latest Voxel Scape release
4. Place both `.jar` files in your `mods` folder
5. Launch Minecraft with the Fabric profile

## 🎯 Quest Chapters

1. **Genesis** (1-30) - Early game basics - 1 QP each
2. **Exploration** (31-60) - Mid-game adventures - 2 QP each
3. **Mastery** (61-90) - Advanced skills - 3 QP each
4. **Conquest** (91-120) - Combat challenges - 4 QP each
5. **Legends** (121-140) - Legendary tasks - 5 QP each
6. **Ascension** (141-150) - Endgame bosses - 6 QP each

## 🐛 Known Issues

- Some combat perks (Speed, Strength, Haste) temporarily disabled due to Mojang mapping compatibility
- XP Multiplier and Soul Keeper perks need implementation

## 🛠️ Development

Built with:
- Minecraft 1.21.11
- Fabric Loader 0.18.1+
- Fabric API
- Simple GUI (sgui) library

## 📝 License

MIT License - Feel free to use, modify, and distribute

## 👤 Author

**Crafted with passion and dedication by Uipg**

Every quest, every feature, every line of code was written with care to bring you the best adventure experience in Minecraft.

---

*For bug reports and feature requests, please open an issue on GitHub.*
