# Voxel Scape - Fixes Needed for Build

## Problem
The build is failing because Minecraft 1.21.11 class names in Yarn mappings don't match what we used.

## Solution
We need to use Mojang mappings instead of Yarn. Here's what to change:

### 1. Update gradle.properties
Current version (DO NOT CHANGE):
```
yarn_mappings=1.21.11+build.1
```

**Note: We are using Minecraft 1.21.11, NOT 1.21.1 - these are very different versions!**

If you needed to revert to 1.21.1:
```
yarn_mappings=1.21.1+build.3
```

OR use Mojang mappings by changing build.gradle line 13 from:
```gradle
mappings "net.fabricmc:yarn:${project.yarn_mappings}:v2"
```
To:
```gradle
mappings loom.officialMojangMappings()
```

### 2. After fixing mappings, the imports should work:
- `net.minecraft.nbt.CompoundTag`
- `net.minecraft.nbt.IntArrayTag`  
- `net.minecraft.commands.CommandSourceStack`
- `net.minecraft.commands.Commands`
- `net.minecraft.network.chat.Component`
- `net.minecraft.server.level.ServerPlayer`

These are all standard Mojang-mapped names.

### 3. Build command:
```powershell
cd "C:\Users\baesp\Desktop\iujhwerfoiuwhb iouwb\voxelscape"
.\gradlew.bat build --no-daemon
```

### 4. After successful build:
Copy the JAR from `build\libs\voxelscape-1.0.0.jar` to your mods folder.

## What the mod does
- `/quests` - Lists all 200 quests with color-coded status
- `/quest <id>` - View quest details
- `/quest start <id>` - Start a quest
- `/quest complete <id>` - Complete a quest (manual for now)

Quest progress is saved in player NBT data automatically!
