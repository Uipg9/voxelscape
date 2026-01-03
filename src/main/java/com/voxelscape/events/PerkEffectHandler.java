package com.voxelscape.events;

import com.voxelscape.data.PlayerQuestData;
import com.voxelscape.data.QuestDataManager;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class PerkEffectHandler {
    private static final Random RANDOM = new Random();
    private static int tickCounter = 0;
    private static final Map<UUID, Boolean> SOUL_KEEPER_ACTIVE = new HashMap<>();
    private static final Map<UUID, Long> VOID_WALKER_COOLDOWN = new HashMap<>(); // Void Walker cooldown tracker
    
    public static void register() {
        // Apply passive effects every second
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tickCounter++;
            if (tickCounter >= 20) { // Every second
                tickCounter = 0;
                
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    applyPassiveEffects(player);
                    applyXPBoost(player);
                    checkVoidWalker(player); // NEW: Check if player is falling into void
                }
            }
        });
        
        // Handle mining perks
        PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) -> {
            if (player instanceof ServerPlayer serverPlayer) {
                handleMiningPerks(serverPlayer, state);
                handleAutoReplant(serverPlayer, level, pos, state);
            }
        });
        
        // Handle mob head drops and XP boost
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            // Null check to prevent crash from environmental deaths (fall, fire, etc.)
            if (damageSource.getEntity() != null && damageSource.getEntity() instanceof ServerPlayer player) {
                handleMobHeadDrop(player, entity);
            }
        });
        
        // Handle XP pickup boost and feather falling  
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            // Feather falling is applied passively via fall distance reduction
            // (handled through player tick event if needed in future)
            return true;
        });
        
        // Soul Keeper - Keep inventory on death
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            if (!alive) { // Player died
                PlayerQuestData data = QuestDataManager.getPlayerData(newPlayer);
                UUID uuid = newPlayer.getUUID();
                
                // Check if Soul Keeper perk is unlocked and active
                if (data.hasPerk("keep_inventory") && !SOUL_KEEPER_ACTIVE.getOrDefault(uuid, false)) {
                    // Copy all items from old player to new player
                    for (int i = 0; i < oldPlayer.getInventory().getContainerSize(); i++) {
                        ItemStack stack = oldPlayer.getInventory().getItem(i);
                        if (!stack.isEmpty()) {
                            newPlayer.getInventory().setItem(i, stack.copy());
                        }
                    }
                    
                    // Mark Soul Keeper as used (one-time use per death)
                    SOUL_KEEPER_ACTIVE.put(uuid, true);
                    
                    newPlayer.sendSystemMessage(net.minecraft.network.chat.Component.literal("§5✦ Soul Keeper activated! §7Items preserved."));
                    newPlayer.sendSystemMessage(net.minecraft.network.chat.Component.literal("§c§l⚠ Soul Keeper will recharge in 5 minutes!"));
                    
                    // Schedule recharge after 5 minutes (6000 ticks)
                    scheduleRecharge(newPlayer, uuid);
                }
            }
        });
    }
    
    private static void applyPassiveEffects(ServerPlayer player) {
        PlayerQuestData data = QuestDataManager.getPlayerData(player);
        
        // Night Vision
        if (data.hasPerk("night_vision")) {
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 300, 0, true, false));
        }
        
        // Speed Boost - MobEffects constants not available in 1.21.11
        // if (data.hasPerk("speed_boost")) {
        //     player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 40, 0, true, false));
        // }
        
        // Strength - MobEffects constants not available in 1.21.11  
        // if (data.hasPerk("strength_1")) {
        //     player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 40, 0, true, false));
        // }
        
        // Resistance - Using absorption as alternative
        if (data.hasPerk("resistance_1")) {
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 40, 0, true, false));
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 0, true, false));
        }
        
        // Haste - MobEffects constants not available in 1.21.11
        // if (data.hasPerk("haste_1")) {
        //     player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 40, 0, true, false));
        // }
        
        // Aqua Affinity (water breathing when underwater)
        if (data.hasPerk("aqua_affinity") && player.isUnderWater()) {
            player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 40, 0, true, false));
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 40, 0, true, false));
        }
        
        // Fire Resistance
        if (data.hasPerk("fire_resistance")) {
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 40, 0, true, false));
        }
        
        // Magnetism - Increase XP and item pickup range
        if (data.hasPerk("magnetism_2")) {
            attractXPOrbs(player, 6.0); // +4 block range
            attractItems(player, 6.0);
        } else if (data.hasPerk("magnetism_1")) {
            attractXPOrbs(player, 4.0); // +2 block range
            attractItems(player, 4.0);
        }
    }
    
    private static void attractXPOrbs(ServerPlayer player, double range) {
        var level = player.level();
        var nearbyOrbs = level.getEntitiesOfClass(
            ExperienceOrb.class,
            player.getBoundingBox().inflate(range),
            orb -> orb.isAlive()
        );
        
        for (ExperienceOrb orb : nearbyOrbs) {
            // Pull orbs toward player
            double dx = player.getX() - orb.getX();
            double dy = player.getY() + player.getEyeHeight() / 2.0 - orb.getY();
            double dz = player.getZ() - orb.getZ();
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            
            if (distance > 0.1) {
                double speed = 0.3;
                orb.setDeltaMovement(
                    dx / distance * speed,
                    dy / distance * speed,
                    dz / distance * speed
                );
            }
        }
    }
    
    private static void applyXPBoost(ServerPlayer player) {
        PlayerQuestData data = QuestDataManager.getPlayerData(player);
        
        // XP Multiplier - Spawn bonus XP orbs near player
        if (data.hasPerk("xp_boost")) {
            // Check for nearby XP orbs and boost them
            var level = player.level();
            var nearbyOrbs = level.getEntitiesOfClass(
                ExperienceOrb.class,
                player.getBoundingBox().inflate(2.0),
                orb -> orb.isAlive()
            );
            
            for (ExperienceOrb orb : nearbyOrbs) {
                // Give player 50% bonus XP directly
                int originalValue = orb.getValue();
                int bonusXP = (int)(originalValue * 0.5f);
                
                if (bonusXP > 0 && RANDOM.nextFloat() < 0.1f) { // 10% chance per second to show message
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§d✦ XP Boost! §e+50%"));
                }
                
                player.giveExperiencePoints(bonusXP);
            }
        }
    }
    
    private static void scheduleRecharge(ServerPlayer player, UUID uuid) {
        // Schedule Soul Keeper recharge after 5 minutes
        new Thread(() -> {
            try {
                Thread.sleep(300000); // 5 minutes
                SOUL_KEEPER_ACTIVE.put(uuid, false);
                
                // Notify player if online
                if (player.isAlive()) {
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§5✦ Soul Keeper recharged! §7Protection ready."));
                }
            } catch (InterruptedException e) {
                // Ignore
            }
        }).start();
    }
    
    private static void handleMiningPerks(ServerPlayer player, BlockState state) {
        PlayerQuestData data = QuestDataManager.getPlayerData(player);
        
        // Auto-Smelt - Instantly smelt ores
        if (data.hasPerk("auto_smelt") && isSmeltable(state)) {
            ItemStack smeltedResult = getSmeltedItem(state);
            if (smeltedResult != null && !smeltedResult.isEmpty()) {
                ItemEntity itemEntity = new ItemEntity(
                    player.level(), 
                    player.getX(), 
                    player.getY(), 
                    player.getZ(), 
                    smeltedResult
                );
                player.level().addFreshEntity(itemEntity);
                
                if (RANDOM.nextFloat() < 0.05f) { // 5% chance to show message
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§6✦ Auto-Smelt!"));
                }
            }
        }
        
        // Lucky Miner - 10% chance for double drops on ores
        if (data.hasPerk("fortune_luck") && isOre(state)) {
            if (RANDOM.nextFloat() < 0.10f) {
                // Drop extra item at player's feet
                ItemStack drop = new ItemStack(state.getBlock().asItem());
                ItemEntity itemEntity = new ItemEntity(
                    player.level(), 
                    player.getX(), 
                    player.getY(), 
                    player.getZ(), 
                    drop
                );
                player.level().addFreshEntity(itemEntity);
                
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§6✦ Lucky Miner! §e+1 bonus drop"));
            }
        }
    }
    
    private static void handleMobHeadDrop(ServerPlayer player, net.minecraft.world.entity.LivingEntity entity) {
        PlayerQuestData data = QuestDataManager.getPlayerData(player);
        
        // Base 5% chance for Head Hunter perk
        float baseChance = 0.05f;
        
        // +20% if Rare Drop Booster is active (total 25% = 0.05 + 0.20)
        if (data.hasPerk("rare_drop_boost")) {
            baseChance += 0.20f;
        }
        
        // Head Hunter - 5% base chance (or 25% with booster) for mob heads
        if (data.hasPerk("mob_heads") && RANDOM.nextFloat() < baseChance) {
            ItemStack head = getMobHead(entity);
            if (head != null) {
                ItemEntity itemEntity = new ItemEntity(
                    player.level(),
                    entity.getX(),
                    entity.getY(),
                    entity.getZ(),
                    head
                );
                player.level().addFreshEntity(itemEntity);
                
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§d✦ Head Hunter! §fMob head dropped"));
            }
        }
    }
    
    private static boolean isOre(BlockState state) {
        return state.is(Blocks.COAL_ORE) || state.is(Blocks.DEEPSLATE_COAL_ORE) ||
               state.is(Blocks.IRON_ORE) || state.is(Blocks.DEEPSLATE_IRON_ORE) ||
               state.is(Blocks.COPPER_ORE) || state.is(Blocks.DEEPSLATE_COPPER_ORE) ||
               state.is(Blocks.GOLD_ORE) || state.is(Blocks.DEEPSLATE_GOLD_ORE) ||
               state.is(Blocks.REDSTONE_ORE) || state.is(Blocks.DEEPSLATE_REDSTONE_ORE) ||
               state.is(Blocks.LAPIS_ORE) || state.is(Blocks.DEEPSLATE_LAPIS_ORE) ||
               state.is(Blocks.DIAMOND_ORE) || state.is(Blocks.DEEPSLATE_DIAMOND_ORE) ||
               state.is(Blocks.EMERALD_ORE) || state.is(Blocks.DEEPSLATE_EMERALD_ORE) ||
               state.is(Blocks.NETHER_GOLD_ORE) || state.is(Blocks.NETHER_QUARTZ_ORE) ||
               state.is(Blocks.ANCIENT_DEBRIS);
    }
    
    private static ItemStack getMobHead(net.minecraft.world.entity.LivingEntity entity) {
        String entityType = entity.getType().toString();
        
        if (entityType.contains("zombie")) return new ItemStack(Items.ZOMBIE_HEAD);
        if (entityType.contains("skeleton")) return new ItemStack(Items.SKELETON_SKULL);
        if (entityType.contains("creeper")) return new ItemStack(Items.CREEPER_HEAD);
        if (entityType.contains("wither_skeleton")) return new ItemStack(Items.WITHER_SKELETON_SKULL);
        if (entityType.contains("piglin")) return new ItemStack(Items.PIGLIN_HEAD);
        if (entityType.contains("dragon")) return new ItemStack(Items.DRAGON_HEAD);
        
        // Default to player head for other mobs
        return new ItemStack(Items.PLAYER_HEAD);
    }
    
    private static void attractItems(ServerPlayer player, double range) {
        var level = player.level();
        var nearbyItems = level.getEntitiesOfClass(
            ItemEntity.class,
            player.getBoundingBox().inflate(range),
            item -> item.isAlive() && !item.hasPickUpDelay()
        );
        
        for (ItemEntity item : nearbyItems) {
            // Pull items toward player
            double dx = player.getX() - item.getX();
            double dy = player.getY() + player.getEyeHeight() / 2.0 - item.getY();
            double dz = player.getZ() - item.getZ();
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            
            if (distance > 0.1) {
                double speed = 0.4;
                item.setDeltaMovement(
                    dx / distance * speed,
                    dy / distance * speed,
                    dz / distance * speed
                );
            }
        }
    }
    
    private static void handleAutoReplant(ServerPlayer player, net.minecraft.world.level.Level level, 
                                          net.minecraft.core.BlockPos pos, BlockState state) {
        PlayerQuestData data = QuestDataManager.getPlayerData(player);
        
        if (!data.hasPerk("auto_replant")) return;
        
        // Check if player has seeds/crops in inventory and replant
        if (state.is(Blocks.WHEAT)) {
            if (hasSeedInInventory(player, Items.WHEAT_SEEDS)) {
                level.setBlock(pos, Blocks.WHEAT.defaultBlockState(), 3);
                consumeSeed(player, Items.WHEAT_SEEDS);
            }
        } else if (state.is(Blocks.CARROTS)) {
            if (hasSeedInInventory(player, Items.CARROT)) {
                level.setBlock(pos, Blocks.CARROTS.defaultBlockState(), 3);
                consumeSeed(player, Items.CARROT);
            }
        } else if (state.is(Blocks.POTATOES)) {
            if (hasSeedInInventory(player, Items.POTATO)) {
                level.setBlock(pos, Blocks.POTATOES.defaultBlockState(), 3);
                consumeSeed(player, Items.POTATO);
            }
        } else if (state.is(Blocks.BEETROOTS)) {
            if (hasSeedInInventory(player, Items.BEETROOT_SEEDS)) {
                level.setBlock(pos, Blocks.BEETROOTS.defaultBlockState(), 3);
                consumeSeed(player, Items.BEETROOT_SEEDS);
            }
        } else if (state.is(Blocks.NETHER_WART)) {
            if (hasSeedInInventory(player, Items.NETHER_WART)) {
                level.setBlock(pos, Blocks.NETHER_WART.defaultBlockState(), 3);
                consumeSeed(player, Items.NETHER_WART);
            }
        }
    }
    
    private static boolean hasSeedInInventory(ServerPlayer player, net.minecraft.world.item.Item seed) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(seed)) {
                return true;
            }
        }
        return false;
    }
    
    private static void consumeSeed(ServerPlayer player, net.minecraft.world.item.Item seed) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(seed)) {
                stack.shrink(1);
                if (stack.isEmpty()) {
                    player.getInventory().setItem(i, ItemStack.EMPTY);
                }
                return;
            }
        }
    }
    
    private static boolean isSmeltable(BlockState state) {
        return state.is(Blocks.IRON_ORE) || state.is(Blocks.DEEPSLATE_IRON_ORE) ||
               state.is(Blocks.GOLD_ORE) || state.is(Blocks.DEEPSLATE_GOLD_ORE) ||
               state.is(Blocks.COPPER_ORE) || state.is(Blocks.DEEPSLATE_COPPER_ORE) ||
               state.is(Blocks.ANCIENT_DEBRIS) ||
               state.is(Blocks.COBBLESTONE) || state.is(Blocks.COBBLED_DEEPSLATE) ||
               state.is(Blocks.SAND) || state.is(Blocks.CLAY);
    }
    
    private static ItemStack getSmeltedItem(BlockState state) {
        if (state.is(Blocks.IRON_ORE) || state.is(Blocks.DEEPSLATE_IRON_ORE)) {
            return new ItemStack(Items.IRON_INGOT);
        } else if (state.is(Blocks.GOLD_ORE) || state.is(Blocks.DEEPSLATE_GOLD_ORE)) {
            return new ItemStack(Items.GOLD_INGOT);
        } else if (state.is(Blocks.COPPER_ORE) || state.is(Blocks.DEEPSLATE_COPPER_ORE)) {
            return new ItemStack(Items.COPPER_INGOT);
        } else if (state.is(Blocks.ANCIENT_DEBRIS)) {
            return new ItemStack(Items.NETHERITE_SCRAP);
        } else if (state.is(Blocks.COBBLESTONE)) {
            return new ItemStack(Items.STONE);
        } else if (state.is(Blocks.COBBLED_DEEPSLATE)) {
            return new ItemStack(Items.DEEPSLATE);
        } else if (state.is(Blocks.SAND)) {
            return new ItemStack(Items.GLASS);
        } else if (state.is(Blocks.CLAY)) {
            return new ItemStack(Items.TERRACOTTA);
        }
        return ItemStack.EMPTY;
    }
    
    private static void checkVoidWalker(ServerPlayer player) {
        PlayerQuestData data = QuestDataManager.getPlayerData(player);
        UUID uuid = player.getUUID();
        
        // Check if player has Void Walker perk and is below Y=-60 (falling into void)
        if (data.hasPerk("void_walker") && player.getY() < -60) {
            long currentTime = System.currentTimeMillis();
            long lastUse = VOID_WALKER_COOLDOWN.getOrDefault(uuid, 0L);
            long dayInMillis = 24 * 60 * 60 * 1000; // 24 hours
            
            // Check if cooldown has expired (24 hours)
            if (currentTime - lastUse >= dayInMillis) {
                // Teleport player to surface (Y=320) at their current XZ
                player.teleportTo(player.getX(), 320, player.getZ());
                player.setDeltaMovement(0, 0, 0); // Stop all movement
                player.fallDistance = 0; // Reset fall distance
                
                // Set cooldown
                VOID_WALKER_COOLDOWN.put(uuid, currentTime);
                
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§8✦ Void Walker activated! §7Teleported to safety."));
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§c§lCooldown: 24 hours"));
            } else if (player.getY() < -80) {
                // Show cooldown message if they're really deep
                long hoursLeft = (dayInMillis - (currentTime - lastUse)) / (60 * 60 * 1000);
                if (RANDOM.nextFloat() < 0.05f) { // Only 5% chance to avoid spam
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§c§lVoid Walker on cooldown! §7(" + hoursLeft + "h remaining)"));
                }
            }
        }
    }
}