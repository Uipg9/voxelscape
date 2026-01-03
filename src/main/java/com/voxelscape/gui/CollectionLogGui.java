package com.voxelscape.gui;

import com.voxelscape.data.PlayerQuestData;
import com.voxelscape.data.QuestDataManager;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

public class CollectionLogGui extends SimpleGui {
    private static final String[] MUSIC_DISCS = {
        "music_disc_13", "music_disc_cat", "music_disc_blocks", "music_disc_chirp",
        "music_disc_far", "music_disc_mall", "music_disc_mellohi", "music_disc_stal",
        "music_disc_strad", "music_disc_ward", "music_disc_11", "music_disc_wait",
        "music_disc_pigstep", "music_disc_otherside", "music_disc_5", "music_disc_relic"
    };
    
    private static final String[] RARE_DROPS = {
        "nether_star", "dragon_egg", "elytra", "trident", "totem_of_undying",
        "enchanted_golden_apple", "heart_of_the_sea", "echo_shard"
    };
    
    private static final String[] MOB_HEADS = {
        "zombie_head", "skeleton_skull", "creeper_head", "wither_skeleton_skull",
        "piglin_head", "dragon_head", "player_head"
    };
    
    public CollectionLogGui(ServerPlayer player) {
        super(MenuType.GENERIC_9x6, player, false);
        
        PlayerQuestData data = QuestDataManager.getPlayerData(player);
        int total = MUSIC_DISCS.length + RARE_DROPS.length + MOB_HEADS.length;
        int collected = countCollected(data);
        
        this.setTitle(Component.literal("§6§lCollection Log §8(" + collected + "/" + total + ")"));
        
        setupCategories(player);
        setupBackButton();
    }
    
    private void setupCategories(ServerPlayer player) {
        PlayerQuestData data = QuestDataManager.getPlayerData(player);
        
        // Music Discs Category
        int musicCount = 0;
        for (String disc : MUSIC_DISCS) {
            if (data.hasCollected(disc)) musicCount++;
        }
        addCategory(player, 11, "§dMusic Discs", Items.MUSIC_DISC_CAT, musicCount, MUSIC_DISCS.length,
            "§7Collect all music discs!",
            "§8Found in dungeons and structures");
        
        // Rare Drops Category
        int rareCount = 0;
        for (String item : RARE_DROPS) {
            if (data.hasCollected(item)) rareCount++;
        }
        addCategory(player, 13, "§6Rare Drops", Items.NETHER_STAR, rareCount, RARE_DROPS.length,
            "§7Collect rare boss drops!",
            "§8From bosses and special mobs");
        
        // Mob Heads Category
        int headCount = 0;
        for (String head : MOB_HEADS) {
            if (data.hasCollected(head)) headCount++;
        }
        addCategory(player, 15, "§eMob Heads", Items.ZOMBIE_HEAD, headCount, MOB_HEADS.length,
            "§7Collect all mob heads!",
            "§8From killing mobs (rare drop)");
        
        // Show items in respective rows
        showMusicDiscs(player, 27);
        showRareDrops(player, 36);
        showMobHeads(player, 45);
    }
    
    private void addCategory(ServerPlayer player, int slot, String name, net.minecraft.world.item.Item icon,
                            int collected, int total, String... lore) {
        GuiElementBuilder builder = new GuiElementBuilder(icon);
        builder.setName(Component.literal(name));
        builder.addLoreLine(Component.literal("§7Progress: §e" + collected + "§7/§e" + total));
        builder.addLoreLine(Component.literal(""));
        for (String line : lore) {
            builder.addLoreLine(Component.literal(line));
        }
        
        if (collected == total) {
            builder.addLoreLine(Component.literal(""));
            builder.addLoreLine(Component.literal("§a§l✔ COMPLETED!"));
        }
        
        this.setSlot(slot, builder);
    }
    
    private void showMusicDiscs(ServerPlayer player, int startSlot) {
        PlayerQuestData data = QuestDataManager.getPlayerData(player);
        int slot = startSlot;
        
        for (String discId : MUSIC_DISCS) {
            if (slot >= startSlot + 9) break;
            
            boolean collected = data.hasCollected(discId);
            GuiElementBuilder builder = new GuiElementBuilder(
                collected ? getItemFromId(discId) : Items.GRAY_DYE
            );
            
            String displayName = discId.replace("music_disc_", "").replace("_", " ");
            displayName = displayName.substring(0, 1).toUpperCase() + displayName.substring(1);
            
            builder.setName(Component.literal(collected ? "§a✔ " + displayName : "§7§m" + displayName));
            
            if (!collected) {
                builder.addLoreLine(Component.literal("§c✖ Not collected"));
            } else {
                builder.addLoreLine(Component.literal("§a✔ Collected!"));
            }
            
            this.setSlot(slot++, builder);
        }
    }
    
    private void showRareDrops(ServerPlayer player, int startSlot) {
        PlayerQuestData data = QuestDataManager.getPlayerData(player);
        int slot = startSlot;
        
        for (String itemId : RARE_DROPS) {
            if (slot >= startSlot + 9) break;
            
            boolean collected = data.hasCollected(itemId);
            GuiElementBuilder builder = new GuiElementBuilder(
                collected ? getItemFromId(itemId) : Items.GRAY_DYE
            );
            
            String displayName = itemId.replace("_", " ");
            displayName = displayName.substring(0, 1).toUpperCase() + displayName.substring(1);
            
            builder.setName(Component.literal(collected ? "§a✔ " + displayName : "§7§m" + displayName));
            
            if (!collected) {
                builder.addLoreLine(Component.literal("§c✖ Not collected"));
            } else {
                builder.addLoreLine(Component.literal("§a✔ Collected!"));
            }
            
            this.setSlot(slot++, builder);
        }
    }
    
    private void showMobHeads(ServerPlayer player, int startSlot) {
        PlayerQuestData data = QuestDataManager.getPlayerData(player);
        int slot = startSlot;
        
        for (String headId : MOB_HEADS) {
            if (slot >= startSlot + 9) break;
            
            boolean collected = data.hasCollected(headId);
            GuiElementBuilder builder = new GuiElementBuilder(
                collected ? getItemFromId(headId) : Items.GRAY_DYE
            );
            
            String displayName = headId.replace("_", " ");
            displayName = displayName.substring(0, 1).toUpperCase() + displayName.substring(1);
            
            builder.setName(Component.literal(collected ? "§a✔ " + displayName : "§7§m" + displayName));
            
            if (!collected) {
                builder.addLoreLine(Component.literal("§c✖ Not collected"));
            } else {
                builder.addLoreLine(Component.literal("§a✔ Collected!"));
            }
            
            this.setSlot(slot++, builder);
        }
    }
    
    private net.minecraft.world.item.Item getItemFromId(String itemId) {
        return switch (itemId) {
            // Music Discs
            case "music_disc_13" -> Items.MUSIC_DISC_13;
            case "music_disc_cat" -> Items.MUSIC_DISC_CAT;
            case "music_disc_blocks" -> Items.MUSIC_DISC_BLOCKS;
            case "music_disc_chirp" -> Items.MUSIC_DISC_CHIRP;
            case "music_disc_far" -> Items.MUSIC_DISC_FAR;
            case "music_disc_mall" -> Items.MUSIC_DISC_MALL;
            case "music_disc_mellohi" -> Items.MUSIC_DISC_MELLOHI;
            case "music_disc_stal" -> Items.MUSIC_DISC_STAL;
            case "music_disc_strad" -> Items.MUSIC_DISC_STRAD;
            case "music_disc_ward" -> Items.MUSIC_DISC_WARD;
            case "music_disc_11" -> Items.MUSIC_DISC_11;
            case "music_disc_wait" -> Items.MUSIC_DISC_WAIT;
            case "music_disc_pigstep" -> Items.MUSIC_DISC_PIGSTEP;
            case "music_disc_otherside" -> Items.MUSIC_DISC_OTHERSIDE;
            case "music_disc_5" -> Items.MUSIC_DISC_5;
            case "music_disc_relic" -> Items.MUSIC_DISC_RELIC;
            
            // Rare Drops
            case "nether_star" -> Items.NETHER_STAR;
            case "dragon_egg" -> Items.DRAGON_EGG;
            case "elytra" -> Items.ELYTRA;
            case "trident" -> Items.TRIDENT;
            case "totem_of_undying" -> Items.TOTEM_OF_UNDYING;
            case "enchanted_golden_apple" -> Items.ENCHANTED_GOLDEN_APPLE;
            case "heart_of_the_sea" -> Items.HEART_OF_THE_SEA;
            case "echo_shard" -> Items.ECHO_SHARD;
            
            // Mob Heads
            case "zombie_head" -> Items.ZOMBIE_HEAD;
            case "skeleton_skull" -> Items.SKELETON_SKULL;
            case "creeper_head" -> Items.CREEPER_HEAD;
            case "wither_skeleton_skull" -> Items.WITHER_SKELETON_SKULL;
            case "piglin_head" -> Items.PIGLIN_HEAD;
            case "dragon_head" -> Items.DRAGON_HEAD;
            case "player_head" -> Items.PLAYER_HEAD;
            
            default -> Items.BARRIER;
        };
    }
    
    private int countCollected(PlayerQuestData data) {
        int count = 0;
        
        for (String disc : MUSIC_DISCS) {
            if (data.hasCollected(disc)) count++;
        }
        for (String item : RARE_DROPS) {
            if (data.hasCollected(item)) count++;
        }
        for (String head : MOB_HEADS) {
            if (data.hasCollected(head)) count++;
        }
        
        return count;
    }
    
    private void setupBackButton() {
        GuiElementBuilder backButton = new GuiElementBuilder(Items.BARRIER);
        backButton.setName(Component.literal("§cClose"));
        backButton.setCallback((index, type, action) -> {
            this.close();
        });
        this.setSlot(53, backButton);
    }
}
