# Minecraft 1.21.11 Fabric - Persistent Player Data API

## The Problem
In Minecraft 1.21.11 with Mojang mappings, several commonly-used methods for storing player data **no longer exist**:
- ❌ `player.getData()` - removed
- ❌ `player.getPersistentData()` - removed  
- ❌ `tag.getIntArray("key")` - now returns `Optional<int[]>`
- ❌ `tag.getCompound("key")` - now returns `Optional<CompoundTag>`

## The Solution: File-Based Storage

Since Minecraft 1.21+ removed direct NBT access on players, the recommended approach is **file-based persistent storage** in the world directory.

### Implementation Pattern

#### 1. QuestDataManager - File-Based Storage
```java
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;

public class QuestDataManager {
    private static final Map<UUID, PlayerQuestData> PLAYER_DATA = new HashMap<>();
    private static final String DATA_DIR = "voxelscape";
    private static MinecraftServer server;

    public static void initialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(s -> server = s);
        ServerLifecycleEvents.SERVER_STOPPING.register(s -> saveAllPlayerData());
    }

    public static PlayerQuestData getPlayerData(ServerPlayer player) {
        UUID uuid = player.getUUID();
        
        if (!PLAYER_DATA.containsKey(uuid)) {
            PlayerQuestData data = new PlayerQuestData();
            loadPlayerData(uuid, data);
            PLAYER_DATA.put(uuid, data);
        }
        
        return PLAYER_DATA.get(uuid);
    }

    public static void savePlayerData(ServerPlayer player) {
        UUID uuid = player.getUUID();
        PlayerQuestData data = PLAYER_DATA.get(uuid);
        
        if (data != null && server != null) {
            try {
                File dataDir = new File(server.getWorldPath(LevelResource.ROOT).toFile(), DATA_DIR);
                if (!dataDir.exists()) {
                    dataDir.mkdirs();
                }
                
                File playerFile = new File(dataDir, uuid.toString() + ".dat");
                CompoundTag tag = new CompoundTag();
                tag.put("QuestData", data.toNbt());
                NbtIo.writeCompressed(tag, playerFile.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void loadPlayerData(UUID uuid, PlayerQuestData data) {
        if (server != null) {
            try {
                File dataDir = new File(server.getWorldPath(LevelResource.ROOT).toFile(), DATA_DIR);
                File playerFile = new File(dataDir, uuid.toString() + ".dat");
                
                if (playerFile.exists()) {
                    // IMPORTANT: readCompressed requires NbtAccounter parameter
                    CompoundTag tag = NbtIo.readCompressed(playerFile.toPath(), NbtAccounter.unlimitedHeap());
                    
                    // IMPORTANT: getCompound returns Optional<CompoundTag>
                    CompoundTag questData = tag.getCompound("QuestData").orElse(new CompoundTag());
                    data.fromNbt(questData);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
```

#### 2. Handling Optional NBT Returns

In Minecraft 1.21.11, NBT read methods return `Optional` types:

```java
// ❌ OLD (doesn't work):
int[] completed = tag.getIntArray("completed");
CompoundTag questData = tag.getCompound("QuestData");

// ✅ NEW (1.21.11):
int[] completed = tag.getIntArray("completed").orElse(new int[0]);
CompoundTag questData = tag.getCompound("QuestData").orElse(new CompoundTag());
```

#### 3. Initialize Data Manager

In your mod initializer:
```java
@Override
public void onInitialize() {
    // Initialize data manager FIRST
    QuestDataManager.initialize();
    
    // Then register commands, etc.
}
```

## Key API Changes Summary

| Old API | New API (1.21.11) |
|---------|-------------------|
| `player.getData()` | ❌ Removed - use file-based storage |
| `player.getPersistentData()` | ❌ Removed - use file-based storage |
| `tag.getIntArray("key")` | `tag.getIntArray("key").orElse(new int[0])` |
| `tag.getCompound("key")` | `tag.getCompound("key").orElse(new CompoundTag())` |
| `NbtIo.readCompressed(path)` | `NbtIo.readCompressed(path, NbtAccounter.unlimitedHeap())` |

## Data Storage Location

Player data will be saved to:
```
<world_directory>/voxelscape/<player_uuid>.dat
```

For example:
```
saves/My World/voxelscape/a1b2c3d4-e5f6-7890-abcd-ef1234567890.dat
```

## Alternative: Cardinal Components API

For more advanced use cases, consider using Fabric's Cardinal Components API:
```gradle
// build.gradle
dependencies {
    modImplementation "dev.onyxstudios.cardinal-components-api:cardinal-components-entity:5.3.0"
}
```

This provides a cleaner component-based attachment system but requires an additional dependency.

## Working Implementation

Your Voxel Scape mod now uses this pattern successfully:
- ✅ File-based persistent storage per player
- ✅ Auto-save on server shutdown
- ✅ Lazy loading on player data access
- ✅ Proper Optional handling for NBT reads
