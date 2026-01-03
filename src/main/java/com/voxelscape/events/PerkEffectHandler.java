package com.voxelscape.events;

import com.voxelscape.data.PlayerQuestData;
import com.voxelscape.data.QuestDataManager;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;

public class PerkEffectHandler {
    private static final Random RANDOM = new Random();
    private static int tickCounter = 0;
    
    public static void register() {
        // Apply passive effects every second
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tickCounter++;
            if (tickCounter >= 20) { // Every second
                tickCounter = 0;
                
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    applyPassiveEffects(player);
                }
            }
        });
        
        // Handle mining perks
        PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) -> {
            if (player instanceof ServerPlayer serverPlayer) {
                handleMiningPerks(serverPlayer, state);
            }
        });
        
        // Handle mob head drops
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            // Null check to prevent crash from environmental deaths (fall, fire, etc.)
            if (damageSource.getEntity() != null && damageSource.getEntity() instanceof ServerPlayer player) {
                handleMobHeadDrop(player, entity);
            }
        });
    }
    
    private static void applyPassiveEffects(ServerPlayer player) {
        PlayerQuestData data = QuestDataManager.getPlayerData(player);
        
        // Night Vision
        if (data.hasPerk("night_vision")) {
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 300, 0, true, false));
        }
        
        // Aqua Affinity (water breathing when underwater)
        if (data.hasPerk("aqua_affinity") && player.isUnderWater()) {
            player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 40, 0, true, false));
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 40, 0, true, false));
        }
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
