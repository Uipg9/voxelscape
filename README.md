# 🎮 Voxel Scape

**An immersive quest adventure mod for Minecraft 1.21.11 (Fabric)**
**Current Version: v1.4.0**

Inspired by Old School RuneScape, Voxel Scape brings a comprehensive quest system to Minecraft with 150 progressive quests, a currency system, upgradeable perks, and collectible tracking.

## ✨ Features

### 📜 Quest System
- **150 Progressive Quests** organized into 6 chapters
- **Quest Points Currency** - Earn 1-6 QP per quest (450 total)
- **Hero's Journal** - A mystical quest book given to all new players
- **Auto-tracking** - All quests activate automatically
- **Smart Dependencies** - Quests unlock as you progress
- **Daily Quest System** - Infrastructure ready for rotating daily challenges

### 🛍️ Perk Shop
- **14 Working Perks** across 6 categories
  - **Movement**: Magnetism I/II, Speed Boost
  - **Utility**: Night Vision, Auto Replant, Aqua Affinity
  - **Combat**: Strength, Resistance, Fire Resistance
  - **Mining**: Lucky Miner, Efficiency, Fortune, Auto-Smelt
  - **Special**: Head Hunter, Rare Drop Booster
  - **Survival**: Void Walker
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
- `/perks` - Open the Perk Shop (14 unique perks)
- `/collection` - Open the Collection Log (31 collectibles)

## 🎁 Latest in v1.4.0

### New Perks
- **Rare Drop Booster** (40 QP) - Increases rare drop chances by +20%
  - Boosts mob head drops from 5% to 25%
  - Stacks with Head Hunter perk for even better rates
  - Affects all rare loot drops
- **Void Walker** (35 QP) - Survive the void once per day
  - Teleports you to surface (Y=320) when falling below Y=-60
  - 24-hour cooldown system
  - Perfect for End dimension exploration

### Daily Quest System
- Complete infrastructure for rotating daily challenges
- Quest streak tracking for consecutive completions
- Persistent data storage ready for future updates

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

- XP Multiplier and Soul Keeper perks need full implementation
- Daily quest command interface not yet added (data structures ready)
- Speed Boost perk needs effect reapplication system

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
