package com.voxelscape.events;

import com.voxelscape.VoxelScape;
import com.voxelscape.data.PlayerQuestData;
import com.voxelscape.data.QuestDataManager;
import com.voxelscape.quest.Quest;
import com.voxelscape.quest.QuestManager;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class QuestTracker {
    // Track progress for each player
    private static final Map<UUID, Map<Integer, Integer>> QUEST_PROGRESS = new HashMap<>();
    // Track inventory to detect item pickups
    private static final Map<UUID, Map<net.minecraft.world.item.Item, Integer>> LAST_INVENTORY = new HashMap<>();
    // Track kill counts for each player
    private static final Map<UUID, Map<String, Integer>> KILL_COUNTS = new HashMap<>();
    
    public static void register() {
        // Track block breaking - now just to trigger inventory checks
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (player instanceof ServerPlayer serverPlayer) {
                // Check inventory after a short delay to catch all items
                world.getServer().execute(() -> checkInventoryChanges(serverPlayer));
            }
        });
        
        // Track mob kills
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity.level().isClientSide()) return;
            
            // Check if killed by a player
            Entity attacker = damageSource.getEntity();
            if (attacker instanceof ServerPlayer player) {
                trackMobKill(player, entity);
            }
        });
    }
    
    private static void checkInventoryChanges(ServerPlayer player) {
        UUID uuid = player.getUUID();
        Map<net.minecraft.world.item.Item, Integer> currentInventory = new HashMap<>();
        
        // Count all items in player inventory using the correct API
        var inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty()) {
                currentInventory.merge(stack.getItem(), stack.getCount(), Integer::sum);
            }
        }
        
        // Get last known inventory
        Map<net.minecraft.world.item.Item, Integer> lastInventory = LAST_INVENTORY.getOrDefault(uuid, new HashMap<>());
        
        // Check for new items
        for (Map.Entry<net.minecraft.world.item.Item, Integer> entry : currentInventory.entrySet()) {
            net.minecraft.world.item.Item item = entry.getKey();
            int currentCount = entry.getValue();
            int lastCount = lastInventory.getOrDefault(item, 0);
            
            if (currentCount > lastCount) {
                int gained = currentCount - lastCount;
                trackItemGained(player, item, gained);
            }
        }
        
        // Update last inventory
        LAST_INVENTORY.put(uuid, currentInventory);
    }
    
    private static void trackItemGained(ServerPlayer player, net.minecraft.world.item.Item item, int count) {
        // Quest 2: Knock on Wood - Collect 4 Oak Logs
        if (item == Items.OAK_LOG) {
            trackQuestProgress(player, 2, 4, "Oak Log", count);
        }
        
        // Quest 7: Stone Age - Mine 3 Cobblestone
        if (item == Items.COBBLESTONE) {
            trackQuestProgress(player, 7, 3, "Cobblestone", count);
        }
        
        // Quest 9: Lumberjack I - Collect 32 Logs (any type)
        if (isLog(item)) {
            trackQuestProgress(player, 9, 32, "Log", count);
        }
    }
    
    private static void trackMobKill(ServerPlayer player, LivingEntity entity) {
        UUID uuid = player.getUUID();
        KILL_COUNTS.putIfAbsent(uuid, new HashMap<>());
        Map<String, Integer> kills = KILL_COUNTS.get(uuid);
        
        // Determine mob type by EntityType
        String mobType = null;
        EntityType<?> type = entity.getType();
        
        if (type == EntityType.ZOMBIE) {
            mobType = "zombie";
            trackQuestProgress(player, 57, 10, "Zombie", 1);
        } else if (type == EntityType.SKELETON) {
            mobType = "skeleton";
            trackQuestProgress(player, 58, 10, "Skeleton", 1);
        } else if (type == EntityType.CREEPER) {
            mobType = "creeper";
            trackQuestProgress(player, 59, 5, "Creeper", 1);
        } else if (entity instanceof Monster) {
            mobType = "hostile";
        }
        
        // Track for "Monster Hunter" quest (Quest 60)
        if (mobType != null) {
            kills.merge("hostile", 1, Integer::sum);
            
            // Update Quest 60 progress
            trackQuestProgress(player, 60, 50, "Hostile Mob", 1);
        }
    }
    
    private static boolean isLog(net.minecraft.world.item.Item item) {
        return item == Items.OAK_LOG || item == Items.BIRCH_LOG || 
               item == Items.SPRUCE_LOG || item == Items.JUNGLE_LOG ||
               item == Items.ACACIA_LOG || item == Items.DARK_OAK_LOG ||
               item == Items.CHERRY_LOG || item == Items.MANGROVE_LOG;
    }
    
    private static void trackQuestProgress(ServerPlayer player, int questId, int required, String itemName, int count) {
        PlayerQuestData data = QuestDataManager.getPlayerData(player);
        
        // Skip if already completed
        if (data.isQuestCompleted(questId)) {
            return;
        }
        
        // Initialize progress tracking
        UUID uuid = player.getUUID();
        QUEST_PROGRESS.putIfAbsent(uuid, new HashMap<>());
        Map<Integer, Integer> playerProgress = QUEST_PROGRESS.get(uuid);
        
        int current = playerProgress.getOrDefault(questId, 0) + count;
        playerProgress.put(questId, current);
        
        Quest quest = QuestManager.getQuest(questId);
        
        // Show progress for low counts, or every 10 for high counts
        boolean showProgress = required <= 10 || current % 10 == 0 || current == required;
        
        if (showProgress) {
            player.sendSystemMessage(Component.literal("§e[Quest] §f" + current + "/" + required + " " + itemName + " - " + quest.getName()));
        }
        
        // Complete quest when done
        if (current >= required) {
            completeQuest(player, questId);
            playerProgress.put(questId, 0); // Reset progress
        }
        
        QuestDataManager.savePlayerData(player);
    }
    
    private static void completeQuest(ServerPlayer player, int questId) {
        PlayerQuestData data = QuestDataManager.getPlayerData(player);
        data.completeQuest(questId);
        
        Quest quest = QuestManager.getQuest(questId);
        
        // Award Quest Points based on chapter
        int questPoints = quest.getChapter(); // Chapter 1 = 1 QP, Chapter 2 = 2 QP, etc.
        data.addQuestPoints(questPoints);
        
        // Celebration effects
        player.playSound(net.minecraft.sounds.SoundEvents.PLAYER_LEVELUP, 1.0f, 1.0f);
        
        // Spawn particles around player
        var level = player.level();
        for (int i = 0; i < 20; i++) {
            double offsetX = (Math.random() - 0.5) * 2;
            double offsetY = Math.random() * 2;
            double offsetZ = (Math.random() - 0.5) * 2;
            level.sendParticles(
                net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER,
                player.getX() + offsetX,
                player.getY() + offsetY,
                player.getZ() + offsetZ,
                1, 0, 0, 0, 0
            );
        }
        
        // Celebration message with sound
        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal("§6╔══════════════════════════════════════╗"));
        player.sendSystemMessage(Component.literal("§6║  §a§l✔ QUEST COMPLETE!              §6║"));
        player.sendSystemMessage(Component.literal("§6╚══════════════════════════════════════╝"));
        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal("§e§l" + quest.getName()));
        player.sendSystemMessage(Component.literal("§7" + quest.getDescription()));
        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal("§6✦ Earned: §e+" + questPoints + " Quest Point" + (questPoints > 1 ? "s" : "")));
        player.sendSystemMessage(Component.literal("§7Total: §e" + data.getQuestPoints() + " QP"));
        player.sendSystemMessage(Component.literal(""));
        
        // Show rewards preview
        if (!quest.getRewards().isEmpty()) {
            player.sendSystemMessage(Component.literal("§6§lRewards Available:"));
            for (String reward : quest.getRewards()) {
                if (reward.toLowerCase().startsWith("xp:")) {
                    String[] parts = reward.split(":");
                    player.sendSystemMessage(Component.literal("  §b✦ " + parts[1] + " XP"));
                } else {
                    String[] parts = reward.split(":");
                    if (parts.length == 2) {
                        String itemDisplay = parts[0].replace("_", " ");
                        itemDisplay = itemDisplay.substring(0, 1).toUpperCase() + itemDisplay.substring(1);
                        player.sendSystemMessage(Component.literal("  §a• " + parts[1] + "x " + itemDisplay));
                    }
                }
            }
            int bonusXP = quest.getChapter() * 50;
            player.sendSystemMessage(Component.literal("  §b✦ " + bonusXP + " XP (bonus)"));
            player.sendSystemMessage(Component.literal(""));
        }
        
        player.sendSystemMessage(Component.literal("§6Click quest in §e/quests §6or use §e/quest claim " + questId));
        player.sendSystemMessage(Component.literal("§6══════════════════════════════════════"));
        player.sendSystemMessage(Component.literal(""));
    }
    
    public static void giveReward(ServerPlayer player, String reward) {
        try {
            // Skip if reward is corrupted/invalid format
            if (reward == null || reward.trim().isEmpty()) {
                return;
            }
            
            // Check if reward contains "x " which means it's corrupted old format
            if (reward.contains("x ")) {
                VoxelScape.LOGGER.warn("Corrupted reward format detected: {}. Please reset quests with /quest reset", reward);
                player.sendSystemMessage(Component.literal("§c⚠ Corrupted reward data! Use §6/quest reset §cto fix."));
                return;
            }
            
            // Parse reward format: "item:count" or just "item"
            String[] parts = reward.split(":");
            if (parts.length == 0) {
                return;
            }
            
            String itemName = parts[0].toLowerCase().trim();
            int count = parts.length > 1 ? Integer.parseInt(parts[1].trim()) : 1;
            
            ItemStack stack = getItemStack(itemName, count);
            if (stack != null) {
                player.addItem(stack);
                player.sendSystemMessage(Component.literal("§a+ " + count + "x " + itemName.replace("_", " ")));
            }
        } catch (Exception e) {
            VoxelScape.LOGGER.error("Error processing reward: " + reward, e);
            player.sendSystemMessage(Component.literal("§cReward error: " + reward));
        }
    }
    
    public static ItemStack getItemStack(String itemName, int count) {
        itemName = itemName.toLowerCase().trim();
        return switch (itemName) {
            // Food
            case "apple" -> new ItemStack(Items.APPLE, count);
            case "bread" -> new ItemStack(Items.BREAD, count);
            case "cooked_beef", "steak" -> new ItemStack(Items.COOKED_BEEF, count);
            case "cooked_porkchop" -> new ItemStack(Items.COOKED_PORKCHOP, count);
            case "golden_apple" -> new ItemStack(Items.GOLDEN_APPLE, count);
            case "carrot" -> new ItemStack(Items.CARROT, count);
            case "potato" -> new ItemStack(Items.POTATO, count);
            case "baked_potato" -> new ItemStack(Items.BAKED_POTATO, count);
            case "melon_slice" -> new ItemStack(Items.MELON_SLICE, count);
            case "cookie" -> new ItemStack(Items.COOKIE, count);
            
            // Materials - Wood
            case "stick" -> new ItemStack(Items.STICK, count);
            case "oak_log" -> new ItemStack(Items.OAK_LOG, count);
            case "oak_planks" -> new ItemStack(Items.OAK_PLANKS, count);
            case "spruce_log" -> new ItemStack(Items.SPRUCE_LOG, count);
            case "birch_log" -> new ItemStack(Items.BIRCH_LOG, count);
            
            // Materials - Stone
            case "cobblestone" -> new ItemStack(Items.COBBLESTONE, count);
            case "stone" -> new ItemStack(Items.STONE, count);
            case "granite" -> new ItemStack(Items.GRANITE, count);
            case "diorite" -> new ItemStack(Items.DIORITE, count);
            case "andesite" -> new ItemStack(Items.ANDESITE, count);
            
            // Materials - Ores & Ingots
            case "coal" -> new ItemStack(Items.COAL, count);
            case "charcoal" -> new ItemStack(Items.CHARCOAL, count);
            case "iron_ore" -> new ItemStack(Items.IRON_ORE, count);
            case "iron_ingot" -> new ItemStack(Items.IRON_INGOT, count);
            case "gold_ore" -> new ItemStack(Items.GOLD_ORE, count);
            case "gold_ingot" -> new ItemStack(Items.GOLD_INGOT, count);
            case "copper_ore" -> new ItemStack(Items.COPPER_ORE, count);
            case "copper_ingot" -> new ItemStack(Items.COPPER_INGOT, count);
            case "diamond" -> new ItemStack(Items.DIAMOND, count);
            case "emerald" -> new ItemStack(Items.EMERALD, count);
            case "lapis_lazuli" -> new ItemStack(Items.LAPIS_LAZULI, count);
            case "redstone" -> new ItemStack(Items.REDSTONE, count);
            case "quartz" -> new ItemStack(Items.QUARTZ, count);
            
            // Materials - Other
            case "leather" -> new ItemStack(Items.LEATHER, count);
            case "string" -> new ItemStack(Items.STRING, count);
            case "feather" -> new ItemStack(Items.FEATHER, count);
            case "bone" -> new ItemStack(Items.BONE, count);
            case "gunpowder" -> new ItemStack(Items.GUNPOWDER, count);
            case "ender_pearl" -> new ItemStack(Items.ENDER_PEARL, count);
            case "blaze_rod" -> new ItemStack(Items.BLAZE_ROD, count);
            
            // Tools & Weapons - Wooden
            case "wooden_pickaxe" -> new ItemStack(Items.WOODEN_PICKAXE, count);
            case "wooden_axe" -> new ItemStack(Items.WOODEN_AXE, count);
            case "wooden_sword" -> new ItemStack(Items.WOODEN_SWORD, count);
            case "wooden_shovel" -> new ItemStack(Items.WOODEN_SHOVEL, count);
            case "wooden_hoe" -> new ItemStack(Items.WOODEN_HOE, count);
            
            // Tools & Weapons - Stone
            case "stone_pickaxe" -> new ItemStack(Items.STONE_PICKAXE, count);
            case "stone_axe" -> new ItemStack(Items.STONE_AXE, count);
            case "stone_sword" -> new ItemStack(Items.STONE_SWORD, count);
            case "stone_shovel" -> new ItemStack(Items.STONE_SHOVEL, count);
            case "stone_hoe" -> new ItemStack(Items.STONE_HOE, count);
            
            // Tools & Weapons - Iron
            case "iron_pickaxe" -> new ItemStack(Items.IRON_PICKAXE, count);
            case "iron_axe" -> new ItemStack(Items.IRON_AXE, count);
            case "iron_sword" -> new ItemStack(Items.IRON_SWORD, count);
            case "iron_shovel" -> new ItemStack(Items.IRON_SHOVEL, count);
            case "iron_hoe" -> new ItemStack(Items.IRON_HOE, count);
            
            // Tools & Weapons - Diamond
            case "diamond_pickaxe" -> new ItemStack(Items.DIAMOND_PICKAXE, count);
            case "diamond_axe" -> new ItemStack(Items.DIAMOND_AXE, count);
            case "diamond_sword" -> new ItemStack(Items.DIAMOND_SWORD, count);
            case "diamond_shovel" -> new ItemStack(Items.DIAMOND_SHOVEL, count);
            
            // Armor - Leather
            case "leather_helmet" -> new ItemStack(Items.LEATHER_HELMET, count);
            case "leather_chestplate" -> new ItemStack(Items.LEATHER_CHESTPLATE, count);
            case "leather_leggings" -> new ItemStack(Items.LEATHER_LEGGINGS, count);
            case "leather_boots" -> new ItemStack(Items.LEATHER_BOOTS, count);
            
            // Armor - Iron
            case "iron_helmet" -> new ItemStack(Items.IRON_HELMET, count);
            case "iron_chestplate" -> new ItemStack(Items.IRON_CHESTPLATE, count);
            case "iron_leggings" -> new ItemStack(Items.IRON_LEGGINGS, count);
            case "iron_boots" -> new ItemStack(Items.IRON_BOOTS, count);
            
            // Armor - Diamond
            case "diamond_helmet" -> new ItemStack(Items.DIAMOND_HELMET, count);
            case "diamond_chestplate" -> new ItemStack(Items.DIAMOND_CHESTPLATE, count);
            case "diamond_leggings" -> new ItemStack(Items.DIAMOND_LEGGINGS, count);
            case "diamond_boots" -> new ItemStack(Items.DIAMOND_BOOTS, count);
            
            // Blocks & Utilities
            case "torch" -> new ItemStack(Items.TORCH, count);
            case "crafting_table" -> new ItemStack(Items.CRAFTING_TABLE, count);
            case "furnace" -> new ItemStack(Items.FURNACE, count);
            case "chest" -> new ItemStack(Items.CHEST, count);
            case "barrel" -> new ItemStack(Items.BARREL, count);
            case "bed" -> new ItemStack(Items.RED_BED, count);
            case "door" -> new ItemStack(Items.OAK_DOOR, count);
            case "ladder" -> new ItemStack(Items.LADDER, count);
            case "fence" -> new ItemStack(Items.OAK_FENCE, count);
            case "bucket" -> new ItemStack(Items.BUCKET, count);
            case "water_bucket" -> new ItemStack(Items.WATER_BUCKET, count);
            case "lava_bucket" -> new ItemStack(Items.LAVA_BUCKET, count);
            case "anvil" -> new ItemStack(Items.ANVIL, count);
            case "enchanting_table" -> new ItemStack(Items.ENCHANTING_TABLE, count);
            case "bookshelf" -> new ItemStack(Items.BOOKSHELF, count);
            
            // Common blocks
            case "dirt" -> new ItemStack(Items.DIRT, count);
            case "sand" -> new ItemStack(Items.SAND, count);
            case "gravel" -> new ItemStack(Items.GRAVEL, count);
            case "glass" -> new ItemStack(Items.GLASS, count);
            case "wool" -> new ItemStack(Items.WHITE_WOOL, count);
            case "brick" -> new ItemStack(Items.BRICKS, count);
            case "clay" -> new ItemStack(Items.CLAY, count);
            
            // Special items
            case "arrow" -> new ItemStack(Items.ARROW, count);
            case "bow" -> new ItemStack(Items.BOW, count);
            case "fishing_rod" -> new ItemStack(Items.FISHING_ROD, count);
            case "shears" -> new ItemStack(Items.SHEARS, count);
            case "compass" -> new ItemStack(Items.COMPASS, count);
            case "clock" -> new ItemStack(Items.CLOCK, count);
            case "map" -> new ItemStack(Items.MAP, count);
            case "book" -> new ItemStack(Items.BOOK, count);
            case "paper" -> new ItemStack(Items.PAPER, count);
            case "shield" -> new ItemStack(Items.SHIELD, count);
            case "flint_and_steel" -> new ItemStack(Items.FLINT_AND_STEEL, count);
            case "obsidian" -> new ItemStack(Items.OBSIDIAN, count);
            
            // Nether items
            case "netherrack" -> new ItemStack(Items.NETHERRACK, count);
            case "soul_sand" -> new ItemStack(Items.SOUL_SAND, count);
            case "nether_wart" -> new ItemStack(Items.NETHER_WART, count);
            case "glowstone" -> new ItemStack(Items.GLOWSTONE, count);
            case "magma_cream" -> new ItemStack(Items.MAGMA_CREAM, count);
            
            // More tools
            case "golden_pickaxe" -> new ItemStack(Items.GOLDEN_PICKAXE, count);
            case "golden_axe" -> new ItemStack(Items.GOLDEN_AXE, count);
            case "golden_sword" -> new ItemStack(Items.GOLDEN_SWORD, count);
            case "golden_shovel" -> new ItemStack(Items.GOLDEN_SHOVEL, count);
            case "golden_hoe" -> new ItemStack(Items.GOLDEN_HOE, count);
            
            // More armor
            case "golden_helmet" -> new ItemStack(Items.GOLDEN_HELMET, count);
            case "golden_chestplate" -> new ItemStack(Items.GOLDEN_CHESTPLATE, count);
            case "golden_leggings" -> new ItemStack(Items.GOLDEN_LEGGINGS, count);
            case "golden_boots" -> new ItemStack(Items.GOLDEN_BOOTS, count);
            case "chainmail_helmet" -> new ItemStack(Items.CHAINMAIL_HELMET, count);
            case "chainmail_chestplate" -> new ItemStack(Items.CHAINMAIL_CHESTPLATE, count);
            case "chainmail_leggings" -> new ItemStack(Items.CHAINMAIL_LEGGINGS, count);
            case "chainmail_boots" -> new ItemStack(Items.CHAINMAIL_BOOTS, count);
            
            // Food extras
            case "cooked_chicken" -> new ItemStack(Items.COOKED_CHICKEN, count);
            case "cooked_mutton" -> new ItemStack(Items.COOKED_MUTTON, count);
            case "cooked_rabbit" -> new ItemStack(Items.COOKED_RABBIT, count);
            case "cooked_cod" -> new ItemStack(Items.COOKED_COD, count);
            case "cooked_salmon" -> new ItemStack(Items.COOKED_SALMON, count);
            case "pumpkin_pie" -> new ItemStack(Items.PUMPKIN_PIE, count);
            
            // More materials
            case "flint" -> new ItemStack(Items.FLINT, count);
            case "clay_ball" -> new ItemStack(Items.CLAY_BALL, count);
            case "brick_item", "brick_single" -> new ItemStack(Items.BRICK, count);
            case "netherite_scrap" -> new ItemStack(Items.NETHERITE_SCRAP, count);
            case "netherite_ingot" -> new ItemStack(Items.NETHERITE_INGOT, count);
            case "amethyst_shard" -> new ItemStack(Items.AMETHYST_SHARD, count);
            case "prismarine_shard" -> new ItemStack(Items.PRISMARINE_SHARD, count);
            case "prismarine_crystals" -> new ItemStack(Items.PRISMARINE_CRYSTALS, count);
            case "nautilus_shell" -> new ItemStack(Items.NAUTILUS_SHELL, count);
            case "heart_of_the_sea" -> new ItemStack(Items.HEART_OF_THE_SEA, count);
            case "echo_shard" -> new ItemStack(Items.ECHO_SHARD, count);
            
            // More blocks
            case "white_wool" -> new ItemStack(Items.WHITE_WOOL, count);
            case "red_bed" -> new ItemStack(Items.RED_BED, count);
            case "oak_door" -> new ItemStack(Items.OAK_DOOR, count);
            case "oak_fence" -> new ItemStack(Items.OAK_FENCE, count);
            case "oak_stairs" -> new ItemStack(Items.OAK_STAIRS, count);
            case "oak_slab" -> new ItemStack(Items.OAK_SLAB, count);
            case "stone_bricks" -> new ItemStack(Items.STONE_BRICKS, count);
            case "mossy_cobblestone" -> new ItemStack(Items.MOSSY_COBBLESTONE, count);
            case "cracked_stone_bricks" -> new ItemStack(Items.CRACKED_STONE_BRICKS, count);
            case "end_stone" -> new ItemStack(Items.END_STONE, count);
            case "purpur_block" -> new ItemStack(Items.PURPUR_BLOCK, count);
            
            // Nether items
            case "fire_charge" -> new ItemStack(Items.FIRE_CHARGE, count);
            case "warped_fungus" -> new ItemStack(Items.WARPED_FUNGUS, count);
            case "saddle" -> new ItemStack(Items.SADDLE, count);
            case "fire_resistance_potion" -> new ItemStack(Items.POTION, count); // Placeholder
            case "regeneration_potion" -> new ItemStack(Items.POTION, count); // Placeholder
            case "slow_falling_potion" -> new ItemStack(Items.POTION, count); // Placeholder
            
            // End items
            case "dragon_egg" -> new ItemStack(Items.DRAGON_EGG, count);
            case "chorus_fruit" -> new ItemStack(Items.CHORUS_FRUIT, count);
            case "shulker_shell" -> new ItemStack(Items.SHULKER_SHELL, count);
            case "phantom_membrane" -> new ItemStack(Items.PHANTOM_MEMBRANE, count);
            case "firework_rocket" -> new ItemStack(Items.FIREWORK_ROCKET, count);
            case "dragon_head" -> new ItemStack(Items.DRAGON_HEAD, count);
            case "elytra" -> new ItemStack(Items.ELYTRA, count);
            case "end_crystal" -> new ItemStack(Items.END_CRYSTAL, count);
            
            // Special/rare items
            case "nether_star" -> new ItemStack(Items.NETHER_STAR, count);
            case "totem_of_undying" -> new ItemStack(Items.TOTEM_OF_UNDYING, count);
            case "trident" -> new ItemStack(Items.TRIDENT, count);
            case "sponge" -> new ItemStack(Items.SPONGE, count);
            case "name_tag" -> new ItemStack(Items.NAME_TAG, count);
            case "music_disc_cat" -> new ItemStack(Items.MUSIC_DISC_CAT, count);
            case "music_disc_13" -> new ItemStack(Items.MUSIC_DISC_13, count);
            case "jukebox" -> new ItemStack(Items.JUKEBOX, count);
            case "snowball" -> new ItemStack(Items.SNOWBALL, count);
            
            // Blocks
            case "diamond_block" -> new ItemStack(Items.DIAMOND_BLOCK, count);
            case "emerald_block" -> new ItemStack(Items.EMERALD_BLOCK, count);
            case "iron_block" -> new ItemStack(Items.IRON_BLOCK, count);
            case "gold_block" -> new ItemStack(Items.GOLD_BLOCK, count);
            case "glass_bottle" -> new ItemStack(Items.GLASS_BOTTLE, count);
            
            // Default - try to be helpful
            default -> {
                VoxelScape.LOGGER.warn("Unknown item name for reward: {}", itemName);
                yield new ItemStack(Items.BARRIER, count); // Use barrier as fallback so at least something is given
            }
        };
    }
}
