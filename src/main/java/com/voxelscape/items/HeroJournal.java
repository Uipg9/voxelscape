package com.voxelscape.items;

import com.voxelscape.VoxelScape;
import com.voxelscape.gui.QuestListGui;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class HeroJournal extends Item {
    public HeroJournal(Properties properties) {
        super(properties);
    }
    
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            // Play book open sound
            level.playSound(null, player.blockPosition(), 
                SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 
                1.0f, 1.0f);
            
            // Open quest GUI
            new QuestListGui(serverPlayer).open();
            
            return InteractionResult.SUCCESS;
        }
        
        return InteractionResult.PASS;
    }
    
    // Tooltip is shown automatically via custom name
    
    @Override
    public boolean isFoil(ItemStack stack) {
        return true; // Make it shimmer like enchanted items
    }
    
    /**
     * Give a Hero's Journal to a player if they don't have one
     */
    public static void giveJournal(ServerPlayer player) {
        // Check if player already has a journal
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() == VoxelScape.HERO_JOURNAL) {
                return; // Already has one
            }
        }
        
        // Create the journal
        ItemStack journal = new ItemStack(VoxelScape.HERO_JOURNAL);
        journal.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, Component.literal("§6§lHero's Journal"));
        
        // Try to add to inventory
        boolean added = player.getInventory().add(journal);
        if (!added) {
            // Drop at feet if inventory full
            player.drop(journal, false);
        }
        
        // Notify player
        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal("§6✦ You received the §l§6Hero's Journal§r§6!"));
        player.sendSystemMessage(Component.literal("§7Right-click it anytime to view your quests"));
        player.sendSystemMessage(Component.literal(""));
    }
}
