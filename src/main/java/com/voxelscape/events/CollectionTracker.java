package com.voxelscape.events;

import com.voxelscape.data.PlayerQuestData;
import com.voxelscape.data.QuestDataManager;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

public class CollectionTracker {
    private static int tickCounter = 0;
    
    public static void register() {
        // Track item pickups via inventory changes (throttled to once per second)
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server -> {
            tickCounter++;
            if (tickCounter >= 20) { // Only check once per second
                tickCounter = 0;
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    checkInventoryForCollectibles(player);
                }
            }
        });
        
        // Track special blocks broken
        PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) -> {
            if (player instanceof ServerPlayer serverPlayer) {
                // Dragon Egg
                if (state.is(Blocks.DRAGON_EGG)) {
                    trackCollection(serverPlayer, "dragon_egg");
                }
            }
        });
    }
    
    private static void checkInventoryForCollectibles(ServerPlayer player) {
        PlayerQuestData data = QuestDataManager.getPlayerData(player);
        
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty()) {
                String itemId = getCollectibleId(stack.getItem());
                if (itemId != null && !data.hasCollected(itemId)) {
                    trackCollection(player, itemId);
                }
            }
        }
    }
    
    private static void trackCollection(ServerPlayer player, String itemId) {
        PlayerQuestData data = QuestDataManager.getPlayerData(player);
        
        if (!data.hasCollected(itemId)) {
            data.collectItem(itemId);
            QuestDataManager.savePlayerData(player);
            
            String displayName = itemId.replace("music_disc_", "")
                                       .replace("_", " ");
            displayName = displayName.substring(0, 1).toUpperCase() + displayName.substring(1);
            
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(""));
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§6╔═══════════════════════════════════╗"));
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§6║  §d§l✦ COLLECTION LOG UPDATED!     §6║"));
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§6╚═══════════════════════════════════╝"));
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(""));
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§eNew Item: §f" + displayName));
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§7Type §e/collection §7to view your log"));
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(""));
            
            player.playSound(SoundEvents.PLAYER_LEVELUP, 1.0f, 2.0f);
        }
    }
    
    private static String getCollectibleId(Item item) {
        // Music Discs
        if (item == Items.MUSIC_DISC_13) return "music_disc_13";
        if (item == Items.MUSIC_DISC_CAT) return "music_disc_cat";
        if (item == Items.MUSIC_DISC_BLOCKS) return "music_disc_blocks";
        if (item == Items.MUSIC_DISC_CHIRP) return "music_disc_chirp";
        if (item == Items.MUSIC_DISC_FAR) return "music_disc_far";
        if (item == Items.MUSIC_DISC_MALL) return "music_disc_mall";
        if (item == Items.MUSIC_DISC_MELLOHI) return "music_disc_mellohi";
        if (item == Items.MUSIC_DISC_STAL) return "music_disc_stal";
        if (item == Items.MUSIC_DISC_STRAD) return "music_disc_strad";
        if (item == Items.MUSIC_DISC_WARD) return "music_disc_ward";
        if (item == Items.MUSIC_DISC_11) return "music_disc_11";
        if (item == Items.MUSIC_DISC_WAIT) return "music_disc_wait";
        if (item == Items.MUSIC_DISC_PIGSTEP) return "music_disc_pigstep";
        if (item == Items.MUSIC_DISC_OTHERSIDE) return "music_disc_otherside";
        if (item == Items.MUSIC_DISC_5) return "music_disc_5";
        if (item == Items.MUSIC_DISC_RELIC) return "music_disc_relic";
        
        // Rare Drops
        if (item == Items.NETHER_STAR) return "nether_star";
        if (item == Items.DRAGON_EGG) return "dragon_egg";
        if (item == Items.ELYTRA) return "elytra";
        if (item == Items.TRIDENT) return "trident";
        if (item == Items.TOTEM_OF_UNDYING) return "totem_of_undying";
        if (item == Items.ENCHANTED_GOLDEN_APPLE) return "enchanted_golden_apple";
        if (item == Items.HEART_OF_THE_SEA) return "heart_of_the_sea";
        if (item == Items.ECHO_SHARD) return "echo_shard";
        
        // Mob Heads
        if (item == Items.ZOMBIE_HEAD) return "zombie_head";
        if (item == Items.SKELETON_SKULL) return "skeleton_skull";
        if (item == Items.CREEPER_HEAD) return "creeper_head";
        if (item == Items.WITHER_SKELETON_SKULL) return "wither_skeleton_skull";
        if (item == Items.PIGLIN_HEAD) return "piglin_head";
        if (item == Items.DRAGON_HEAD) return "dragon_head";
        if (item == Items.PLAYER_HEAD) return "player_head";
        
        return null;
    }
}
