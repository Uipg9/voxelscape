package com.voxelscape.data;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
                    CompoundTag tag = NbtIo.readCompressed(playerFile.toPath(), NbtAccounter.unlimitedHeap());
                    CompoundTag questData = tag.getCompound("QuestData").orElse(new CompoundTag());
                    data.fromNbt(questData);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void saveAllPlayerData() {
        if (server != null) {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                savePlayerData(player);
            }
        }
    }

    public static void removePlayerData(UUID uuid) {
        PLAYER_DATA.remove(uuid);
    }
}
