package com.voxelscape.events;

import com.voxelscape.data.PlayerQuestData;
import com.voxelscape.data.QuestDataManager;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
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
    
    public static void register() {
        // Apply passive effects every second
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tickCounter++;
            if (tickCounter >= 20) { // Every second
                tickCounter = 0;
                
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    applyPassiveEffects(player);
                    applyXPBoost(player);
                }
            }
        });
        
        // Handle mining perks
        PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) -> {
            if (player instanceof ServerPlayer serverPlayer) {
                handleMiningPerks(serverPlayer, state);
            }
        });
        
        // Handle mob head drops and XP boost
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            // Null check to prevent crash from environmental deaths (fall, fire, etc.)
            if (damageSource.getEntity() != null && damageSource.getEntity() instanceof ServerPlayer player) {
                handleMobHeadDrop(player, entity);
            }
        });
        
        // Handle XP pickup boost
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            // XP boost is handled via ExperienceOrb tracking in applyXPBoost
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
        
        // Speed Boost - Using MOVEMENT_SLOWNESS inverse wouldn't work, so disable for now
        // if (data.hasPerk("speed_boost")) {
        //     player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 40, 0, true, false));
        // }
        
        // Strength - Using WEAKNESS inverse wouldn't work, so disable for now  
        // if (data.hasPerk("strength_1")) {
        //     player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 40, 0, true, false));
        // }
        
        // Resistance - Using absorption as alternative
        if (data.hasPerk("resistance_1")) {
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 40, 0, true, false));
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 0, true, false));
        }
        
        // Haste - Using MINING_FATIGUE inverse wouldn't work, so disable for now
        // if (data.hasPerk("haste_1")) {
        //     player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 40, 0, true, false));
        // }
        
        // Aqua Affinity (water breathing when underwater)
        if (data.hasPerk("aqua_affinity") && player.isUnderWater()) {
            player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 40, 0, true, false));
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 40, 0, true, false));
        }
        
        // Magnetism - Increase XP pickup range
        if (data.hasPerk("magnetism_2")) {
            attractXPOrbs(player, 6.0); // +4 block range
        } else if (data.hasPerk("magnetism_1")) {
            attractXPOrbs(player, 4.0); // +2 block range
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
        
        // Head Hunter - 5% chance for mob heads
        if (data.hasPerk("mob_heads") && RANDOM.nextFloat() < 0.05f) {
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
}
