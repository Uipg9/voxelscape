package com.voxelscape.events;

import com.voxelscape.data.PlayerQuestData;
import com.voxelscape.data.QuestDataManager;
import com.voxelscape.items.HeroJournal;
import com.voxelscape.quest.Quest;
import com.voxelscape.quest.QuestManager;
import com.voxelscape.quest.QuestStatus;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class PlayerJoinHandler {
    public static void register() {
        // Handle player join
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.getPlayer();
            
            // Initialize quest data
            PlayerQuestData data = QuestDataManager.getPlayerData(player);
            
            // Auto-activate all quests
            for (int questId : QuestManager.getAllQuestIds()) {
                if (!data.isQuestCompleted(questId) && !data.isQuestInProgress(questId)) {
                    data.startQuest(questId);
                }
            }
            QuestDataManager.savePlayerData(player);
            
            // Count progress
            int completed = data.getCompletedQuests().size();
            int total = QuestManager.getQuestCount();
            int unclaimed = data.getUnclaimedRewards().size();
            
            // Welcome message with progress
            player.sendSystemMessage(Component.literal(""));
            player.sendSystemMessage(Component.literal("§6╔══════════════════════════════════════╗"));
            player.sendSystemMessage(Component.literal("§6║      §e§lVOXEL SCAPE QUEST SYSTEM      §6║"));
            player.sendSystemMessage(Component.literal("§6╚══════════════════════════════════════╝"));
            player.sendSystemMessage(Component.literal(""));
            
            if (completed == 0) {
                // First time player - give Hero's Journal
                player.sendSystemMessage(Component.literal("§aWelcome, Adventurer!"));
                player.sendSystemMessage(Component.literal("§7Your journey begins with §e" + total + " quests§7!"));
                player.sendSystemMessage(Component.literal(""));
                
                // Give the Hero's Journal
                HeroJournal.giveJournal(player);
                
                player.sendSystemMessage(Component.literal("§6➤ §eRight-click your Hero's Journal"));
                player.sendSystemMessage(Component.literal("§7  Or type §6/quests §7to see all quests"));
            } else {
                // Returning player
                player.sendSystemMessage(Component.literal("§eWelcome back!"));
                player.sendSystemMessage(Component.literal("§7Progress: §a" + completed + "§7/§a" + total + " §7quests completed"));
                player.sendSystemMessage(Component.literal("§7Quest Points: §e" + data.getQuestPoints() + " QP"));
                
                if (unclaimed > 0) {
                    player.sendSystemMessage(Component.literal("§6⚠ §eYou have §6" + unclaimed + " unclaimed rewards§e!"));
                    player.sendSystemMessage(Component.literal("§7  Use §e/quest claim §7to collect them"));
                }
                
                // Show next available quest hint
                Quest nextQuest = findNextAvailableQuest(data);
                if (nextQuest != null) {
                    player.sendSystemMessage(Component.literal(""));
                    player.sendSystemMessage(Component.literal("§6➤ §eNext Quest: §f" + nextQuest.getName()));
                    if (!nextQuest.getRequirements().isEmpty()) {
                        player.sendSystemMessage(Component.literal("§7  " + nextQuest.getRequirements().get(0)));
                    }
                }
            }
            
            player.sendSystemMessage(Component.literal(""));
            player.sendSystemMessage(Component.literal("§6═══════════════════════════════════════"));
            player.sendSystemMessage(Component.literal(""));
        });
        
        // Handle player disconnect - save data and clean up memory
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayer player = handler.getPlayer();
            QuestDataManager.savePlayerData(player);
            QuestDataManager.removePlayerData(player.getUUID());
        });
    }
    
    private static Quest findNextAvailableQuest(PlayerQuestData data) {
        // Find the first non-completed quest with no dependencies or all dependencies met
        for (int i = 1; i <= QuestManager.getQuestCount(); i++) {
            Quest quest = QuestManager.getQuest(i);
            if (quest != null && !data.isQuestCompleted(i)) {
                QuestStatus status = data.getQuestStatus(i, quest.getDependencies());
                if (status == QuestStatus.AVAILABLE || status == QuestStatus.IN_PROGRESS) {
                    return quest;
                }
            }
        }
        return null;
    }
}
