# Changelog

All notable changes to Voxel Scape will be documented in this file.

## [1.3.0] - 2026-01-03

### Added
- **Auto-Smelt Perk** (28 QP) - Instantly smelt ores when mined
  - Works with iron, gold, copper ore (including deepslate variants)
  - Also smelts cobblestone→stone, sand→glass, clay→terracotta
  - Get ingots directly without smelting!
- **Fire Resistance Perk** (22 QP) - Permanent fire immunity
  - Swim in lava safely
  - Never burn from fire or lava again
- **Feather Falling Perk** (18 QP) - Reduce fall damage (planned)
  - Note: Currently disabled pending API updates

### Fixed
- Cleaned up unused imports for better code quality
- Fixed compilation warnings

## [1.2.0] - 2026-01-03

### Added
- **Auto-Replant Perk** (10 QP) - Automatically replants crops when harvested
  - Supports wheat, carrots, potatoes, beetroot, and nether wart
  - Uses seeds/crops from your inventory
- **Item Magnetism** - Magnetism I & II perks now attract dropped items (not just XP)
  - Both tiers pull items toward you automatically
  - Makes looting and farming much more convenient

### Changed
- Magnetism perks now affect both XP orbs and dropped items
- Auto-Replant replaces the previously planned Auto-Feeder perk

## [1.1.0] - 2026-01-03

### Added
- **Magnetism I Perk** (5 QP) - Attracts XP orbs from 4 blocks away
- **Magnetism II Perk** (12 QP) - Attracts XP orbs from 6 blocks away (requires Magnetism I)
- XP orbs now physically move toward players with Magnetism perks
- Prerequisite perk system - some perks now require others first

### Changed
- Removed Step Assist perk (vanilla auto-jump already exists)
- Improved perk shop UI organization

### Fixed
- Memory leak from player disconnect - data now properly cleared
- Unused variable warnings cleaned up
- Soul Keeper now has proper 5-minute cooldown tracking

## [1.0.0] - 2026-01-03

### Added
- **150 Progressive Quests** across 6 chapters
- **Quest Points Currency System** - Earn 1-6 QP per quest (450 total)
- **Hero's Journal Item** - Mystical quest book given to new players
- **Perk Shop** with 14 permanent upgrades:
  - Night Vision (12 QP)
  - Aqua Affinity (15 QP)
  - Resistance I (20 QP) - Absorption + Regeneration
  - Lucky Miner (25 QP) - 10% double ore drops
  - Head Hunter (30 QP) - 5% mob head drops
  - XP Multiplier (35 QP) - 50% bonus XP
  - Soul Keeper (50 QP) - Keep inventory on death
- **Collection Log** tracking 31 rare items:
  - 16 Music Discs
  - 8 Rare Mob Drops
  - 7 Mob Heads
- Auto-tracking for collections with fancy messages
- Sound effects for GUIs, purchases, and discoveries
- Particle effects on quest completion
- NBT data persistence per-player
- Server-side implementation

### Commands
- `/quests` - Open quest list
- `/quest <id>` - View specific quest
- `/quest claim` - Claim rewards
- `/perks` - Open Perk Shop
- `/collection` - View Collection Log

### Known Issues
- Speed Boost, Strength, and Haste perks disabled (Mojang mapping compatibility)
- Auto-Feeder perk listed but not functional yet
