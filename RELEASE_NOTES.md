# Voxel Scape v1.0.0 - Initial Release

**An immersive quest adventure mod for Minecraft 1.21.11 (Fabric)**

## ✨ Features

### 📜 Quest System
- **150 Progressive Quests** across 6 chapters (Genesis, Exploration, Mastery, Conquest, Legends, Ascension)
- **Quest Points Currency** - Earn 1-6 QP per quest based on difficulty (450 total available)
- **Hero's Journal** - Mystical quest book automatically given to new players
- **Auto-tracking** - All quests activate automatically on join
- **Smart Dependencies** - Quests unlock as you progress through chapters

### 🛍️ Perk Shop (14 Perks)
Spend your hard-earned Quest Points on permanent upgrades:

**Working Perks:**
- 🌙 **Night Vision** (12 QP) - See in the dark permanently
- 🌊 **Aqua Affinity** (15 QP) - Breathe and see underwater
- 🛡️ **Resistance I** (20 QP) - Extra hearts + regeneration
- 💎 **Lucky Miner** (25 QP) - 10% chance for double ore drops
- 💀 **Head Hunter** (30 QP) - 5% chance for mob head drops
- ✨ **XP Multiplier** (35 QP) - 50% bonus XP from all sources
- 👻 **Soul Keeper** (50 QP) - Keep inventory on death (5min cooldown)

**Coming Soon:**
- Speed Boost, Strength, Haste (awaiting Mojang mapping fixes)

### 🏛️ Collection Log
Track 31 rare collectibles:
- 16 Music Discs
- 8 Rare Mob Drops (Nether Star, Dragon Egg, Elytra, etc.)
- 7 Mob Heads

Features auto-tracking with fancy discovery messages and sound effects!

### 🎵 Polish
- Page turn sounds when opening GUIs
- Ka-ching effect on perk purchases
- High-pitched chime for collection discoveries
- Particle effects on quest completion

## 📋 Commands

- `/quests` - Open quest list
- `/quest <id>` - View specific quest
- `/quest claim` - Claim quest rewards
- `/perks` - Open Perk Shop
- `/collection` - View Collection Log

## 🔧 Installation

1. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft 1.21.11
2. Download [Fabric API](https://modrinth.com/mod/fabric-api) (required)
3. Download `voxelscape-1.0.0.jar` from this release
4. Place both JAR files in your `.minecraft/mods` folder
5. Launch Minecraft with Fabric profile

## ⚙️ Requirements

- Minecraft 1.21.11
- Fabric Loader 0.18.1+
- Fabric API 0.140.0+1.21.11
- Java 21+

## 📝 Technical Details

- **NBT Data Persistence** - All progress saved per-player
- **Memory Optimized** - Auto-cleanup on player disconnect
- **Performance Tuned** - Throttled tracking systems (1Hz)
- **Server-Side** - Works on dedicated servers

## 🐛 Known Issues

- Some combat perks (Speed, Strength, Haste) temporarily disabled due to Mojang mapping compatibility
- These will be re-enabled in a future update

## 👤 Credits

**Crafted with passion and dedication by Uipg**

Every quest, every feature, every line of code was written with care to bring you the best adventure experience in Minecraft.

---

For bug reports and suggestions, please [open an issue](https://github.com/Uipg9/voxelscape/issues)!
