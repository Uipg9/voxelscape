package com.voxelscape.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.voxelscape.data.PlayerQuestData;
import com.voxelscape.data.QuestDataManager;
import com.voxelscape.gui.QuestListGui;
import com.voxelscape.quest.Quest;
import com.voxelscape.quest.QuestManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class QuestsCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("quests")
            .executes(QuestsCommand::openQuestGui));
    }

    private static int openQuestGui(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
            context.getSource().sendFailure(Component.literal("Only players can use this command"));
            return 0;
        }

        PlayerQuestData data = QuestDataManager.getPlayerData(player);
        
        // Quest 1: Awakening - Complete when opening quest menu for the first time
        if (!data.isQuestCompleted(1)) {
            data.completeQuest(1);
            QuestDataManager.savePlayerData(player);
            
            Quest quest1 = QuestManager.getQuest(1);
            
            player.sendSystemMessage(Component.literal("§6========================================"));
            player.sendSystemMessage(Component.literal("§a✔ Quest Complete! §f" + quest1.getName()));
            player.sendSystemMessage(Component.literal("§e" + quest1.getDescription()));
            player.sendSystemMessage(Component.literal(""));
            player.sendSystemMessage(Component.literal("§6Starter gift available!"));
            player.sendSystemMessage(Component.literal("§eUse §6/quest claim 1 §eto claim your reward"));
            player.sendSystemMessage(Component.literal("§6========================================"));
            player.playSound(net.minecraft.sounds.SoundEvents.PLAYER_LEVELUP, 1.0f, 1.0f);
            player.sendSystemMessage(Component.literal(""));
        }
        
        // Open the GUI
        new QuestListGui(player).open();
        
        return 1;
    }
}
