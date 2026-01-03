# Voxel Scape v1.4.0 - Daily Quests & Void Survival

**Latest Update for Minecraft 1.21.11 (Fabric)**

## 🎉 What's New in v1.4.0

### New Perks

**Rare Drop Booster** (40 QP) 🌟
- Increases base rare drop chance by +20%
- Boosts mob head drops from 5% to 25% (when combined with Head Hunter)
- Affects all rare loot drops including saddles, music discs, and more
- Stacks multiplicatively with Head Hunter perk
- One of the most powerful end-game perks for collectors

**Void Walker** (35 QP) 🌌
- Saves you from the void once per day
- Automatically teleports to Y=320 when falling below Y=-60
- 24-hour cooldown system with time tracking
- Perfect for End dimension exploration and city raids
- Shows remaining cooldown time in chat
- Prevents fall damage on teleport

### Daily Quest System Infrastructure

- Complete data structures for rotating daily challenges
- Quest streak tracking (consecutive days completed)
- Timestamp-based expiry system
- Persistent NBT save/load for all daily quest data
- Ready for future daily quest command implementation

### Bug Fixes & Improvements

- Fixed unused variable compilation warnings
- Cleaned up code organization
- Improved perk effect handler
- Enhanced drop rate calculations

## 📊 Current Stats

- **Total Quests:** 150 across 6 chapters
- **Total Quest Points Available:** 450 QP
- **Working Perks:** 14 permanent upgrades
- **Total Perk Cost:** 285 QP (for all perks)
- **Collectibles Tracked:** 31 rare items

---

# Previous Releases

## v1.3.0 - Fire & Forge Update

### New Perks
- **Auto-Smelt** (50 QP) - Automatically smelt ores when mined
- **Fire Resistance** (30 QP) - Permanent fire and lava immunity

---

## v1.2.0 - Perk Shop & Collections

### Features Added
- Perk Shop system with 12 working perks
- Collection Log tracking 31 collectibles
- Enhanced GUI with sound effects

---

## v1.1.0 - Quest Expansion

### Major Update
- Expanded from 60 to 150 quests
- Added Chapters 4, 5, and 6
- Combat tracking system
- XP reward system with bonus multipliers

---

## v1.0.0 - Initial Release

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
