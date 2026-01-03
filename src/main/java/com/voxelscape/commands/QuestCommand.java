package com.voxelscape.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.voxelscape.VoxelScape;
import com.voxelscape.data.PlayerQuestData;
import com.voxelscape.data.QuestDataManager;
import com.voxelscape.events.QuestTracker;
import com.voxelscape.quest.Quest;
import com.voxelscape.quest.QuestManager;
import com.voxelscape.quest.QuestStatus;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class QuestCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("quest")
            .then(Commands.argument("id", IntegerArgumentType.integer(1))
                .executes(QuestCommand::viewQuest))
            .then(Commands.literal("start")
                .then(Commands.argument("id", IntegerArgumentType.integer(1))
                    .executes(QuestCommand::startQuest)))
            .then(Commands.literal("complete")
                .then(Commands.argument("id", IntegerArgumentType.integer(1))
                    .executes(QuestCommand::completeQuest)))
            .then(Commands.literal("claim")
                .executes(QuestCommand::claimAllRewards)
                .then(Commands.argument("id", IntegerArgumentType.integer(1))
                    .executes(QuestCommand::claimReward)))
            .then(Commands.literal("reset")
                .executes(QuestCommand::resetAllQuests)
                .then(Commands.argument("id", IntegerArgumentType.integer(1))
                    .executes(QuestCommand::resetQuest))));
    }

    private static int viewQuest(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
            context.getSource().sendFailure(Component.literal("Only players can use this command"));
            return 0;
        }

        int questId = IntegerArgumentType.getInteger(context, "id");
        Quest quest = QuestManager.getQuest(questId);

        if (quest == null) {
            player.sendSystemMessage(Component.literal("§cQuest #" + questId + " not found!"));
            return 0;
        }

        PlayerQuestData data = QuestDataManager.getPlayerData(player);
        QuestStatus status = data.getQuestStatus(questId, quest.getDependencies());

        String color = switch (status) {
            case COMPLETED -> "§a";
            case IN_PROGRESS -> "§e";
            case LOCKED -> "§8";
            case AVAILABLE -> "§c";
        };

        player.sendSystemMessage(Component.literal("§6=== Quest #" + questId + " ==="));
        player.sendSystemMessage(Component.literal(color + quest.getName()));
        player.sendSystemMessage(Component.literal("§7" + quest.getDescription()));
        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal("§eRequirements:"));
        for (String req : quest.getRequirements()) {
            player.sendSystemMessage(Component.literal("  §7• " + req));
        }
        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal("§aRewards:"));
        for (String reward : quest.getRewards()) {
            player.sendSystemMessage(Component.literal("  §7• " + reward));
        }
        player.sendSystemMessage(Component.literal(""));
        
        String statusText = switch (status) {
            case COMPLETED -> "§aCompleted!";
            case IN_PROGRESS -> "§eIn Progress";
            case LOCKED -> "§cLocked (complete dependencies first)";
            case AVAILABLE -> "§eAvailable - Use §f/quest start " + questId;
        };
        player.sendSystemMessage(Component.literal("Status: " + statusText));

        return 1;
    }

    private static int startQuest(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
            context.getSource().sendFailure(Component.literal("Only players can use this command"));
            return 0;
        }

        int questId = IntegerArgumentType.getInteger(context, "id");
        Quest quest = QuestManager.getQuest(questId);

        if (quest == null) {
            player.sendSystemMessage(Component.literal("§cQuest #" + questId + " not found!"));
            return 0;
        }

        PlayerQuestData data = QuestDataManager.getPlayerData(player);
        QuestStatus status = data.getQuestStatus(questId, quest.getDependencies());

        if (status == QuestStatus.LOCKED) {
            player.sendSystemMessage(Component.literal("§cThis quest is locked! Complete dependencies first."));
            return 0;
        }

        if (status == QuestStatus.COMPLETED) {
            player.sendSystemMessage(Component.literal("§aYou've already completed this quest!"));
            return 0;
        }

        if (status == QuestStatus.IN_PROGRESS) {
            player.sendSystemMessage(Component.literal("§eThis quest is already in progress!"));
            return 0;
        }

        data.startQuest(questId);
        QuestDataManager.savePlayerData(player);
        
        player.sendSystemMessage(Component.literal("§aStarted quest: §e" + quest.getName()));
        return 1;
    }

    private static int completeQuest(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
            context.getSource().sendFailure(Component.literal("Only players can use this command"));
            return 0;
        }

        int questId = IntegerArgumentType.getInteger(context, "id");
        Quest quest = QuestManager.getQuest(questId);

        if (quest == null) {
            player.sendSystemMessage(Component.literal("§cQuest #" + questId + " not found!"));
            return 0;
        }

        PlayerQuestData data = QuestDataManager.getPlayerData(player);
        
        if (data.isQuestCompleted(questId)) {
            player.sendSystemMessage(Component.literal("§aYou've already completed this quest!"));
            return 0;
        }

        data.completeQuest(questId);
        QuestDataManager.savePlayerData(player);
        
        player.sendSystemMessage(Component.literal("§6✦ Quest Complete! ✦"));
        player.sendSystemMessage(Component.literal("§e" + quest.getName()));
        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal("§aRewards:"));
        for (String reward : quest.getRewards()) {
            player.sendSystemMessage(Component.literal("  §7• " + reward));
        }
        
        return 1;
    }
    
    private static int claimReward(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
            context.getSource().sendFailure(Component.literal("Only players can use this command"));
            return 0;
        }

        int questId = IntegerArgumentType.getInteger(context, "id");
        Quest quest = QuestManager.getQuest(questId);

        if (quest == null) {
            player.sendSystemMessage(Component.literal("§cQuest #" + questId + " not found!"));
            return 0;
        }

        PlayerQuestData data = QuestDataManager.getPlayerData(player);
        
        if (!data.isQuestCompleted(questId)) {
            player.sendSystemMessage(Component.literal("§cYou haven't completed this quest yet!"));
            return 0;
        }
        
        if (!data.hasUnclaimedReward(questId)) {
            player.sendSystemMessage(Component.literal("§cYou've already claimed this reward!"));
            return 0;
        }
        
        // Give rewards
        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal("§6========================================"));
        player.sendSystemMessage(Component.literal("§a§lCLAIMING REWARDS"));
        player.sendSystemMessage(Component.literal("§eQuest: §f" + quest.getName()));
        player.sendSystemMessage(Component.literal("§7Rewards: " + quest.getRewards().size()));
        player.sendSystemMessage(Component.literal(""));
        
        boolean success = true;
        for (String reward : quest.getRewards()) {
            try {
                player.sendSystemMessage(Component.literal("§7Processing: " + reward));
                
                // Check for XP reward format: "xp:amount"
                if (reward.toLowerCase().startsWith("xp:")) {
                    String[] parts = reward.split(":");
                    try {
                        int xpAmount = Integer.parseInt(parts[1].trim());
                        player.giveExperiencePoints(xpAmount);
                        player.sendSystemMessage(Component.literal("§a✓ Received: §f" + xpAmount + " XP"));
                        continue;
                    } catch (Exception e) {
                        player.sendSystemMessage(Component.literal("§c✗ Invalid XP amount: " + reward));
                        success = false;
                        continue;
                    }
                }
                
                // Parse reward format: "item:count"
                String[] parts = reward.split(":");
                if (parts.length != 2) {
                    player.sendSystemMessage(Component.literal("§c✗ Invalid reward format: " + reward));
                    player.sendSystemMessage(Component.literal("§7Expected format: item:count or xp:amount"));
                    success = false;
                    continue;
                }
                
                String itemName = parts[0].toLowerCase().trim();
                int count;
                try {
                    count = Integer.parseInt(parts[1].trim());
                } catch (NumberFormatException e) {
                    player.sendSystemMessage(Component.literal("§c✗ Invalid count in reward: " + parts[1]));
                    success = false;
                    continue;
                }
                
                player.sendSystemMessage(Component.literal("§7Item: " + itemName + ", Count: " + count));
                var stack = QuestTracker.getItemStack(itemName, count);
                
                if (stack == null || stack.isEmpty()) {
                    player.sendSystemMessage(Component.literal("§c✗ Failed to create item: " + itemName));
                    player.sendSystemMessage(Component.literal("§7This item may not be supported yet"));
                    success = false;
                    continue;
                }
                
                // Check if it's a barrier (fallback item)
                if (stack.getItem() == net.minecraft.world.item.Items.BARRIER) {
                    player.sendSystemMessage(Component.literal("§e⚠ Warning: Unknown item '" + itemName + "' - giving placeholder"));
                }
                
                // Use the proper inventory method to give items
                boolean added = player.getInventory().add(stack);
                if (added || stack.isEmpty()) {
                    String displayName = itemName.replace("_", " ");
                    displayName = displayName.substring(0, 1).toUpperCase() + displayName.substring(1);
                    player.sendSystemMessage(Component.literal("§a✓ Received: §f" + count + "x " + displayName));
                } else {
                    // If inventory full, drop at player's feet
                    player.drop(stack, false);
                    String displayName = itemName.replace("_", " ");
                    displayName = displayName.substring(0, 1).toUpperCase() + displayName.substring(1);
                    player.sendSystemMessage(Component.literal("§a✓ Dropped: §f" + count + "x " + displayName + " §7(inventory full)"));
                }
            } catch (Exception e) {
                player.sendSystemMessage(Component.literal("§c✗ Error processing reward: " + reward));
                player.sendSystemMessage(Component.literal("§7Error: " + e.getMessage()));
                VoxelScape.LOGGER.error("Error giving quest reward: " + reward, e);
                success = false;
            }
        }
        
        // Always give bonus XP based on quest chapter
        int bonusXP = quest.getChapter() * 50;
        player.giveExperiencePoints(bonusXP);
        player.sendSystemMessage(Component.literal("§b✦ Bonus: §f" + bonusXP + " XP"));
        
        if (success) {
            data.claimReward(questId);
            QuestDataManager.savePlayerData(player);
            player.sendSystemMessage(Component.literal(""));
            player.sendSystemMessage(Component.literal("§aRewards claimed successfully!"));
        }
        
        player.sendSystemMessage(Component.literal("§6========================================"));
        player.playSound(net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        
        return 1;
    }
    
    private static int claimAllRewards(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
            context.getSource().sendFailure(Component.literal("Only players can use this command"));
            return 0;
        }

        PlayerQuestData data = QuestDataManager.getPlayerData(player);
        var unclaimed = data.getUnclaimedRewards();
        
        if (unclaimed.isEmpty()) {
            player.sendSystemMessage(Component.literal("§eYou have no rewards to claim!"));
            return 0;
        }
        
        player.sendSystemMessage(Component.literal("§6========================================"));
        player.sendSystemMessage(Component.literal("§aClaiming all pending rewards..."));
        player.sendSystemMessage(Component.literal(""));
        
        int claimed = 0;
        for (int questId : unclaimed) {
            Quest quest = QuestManager.getQuest(questId);
            if (quest != null) {
                player.sendSystemMessage(Component.literal("§e" + quest.getName() + ":"));
                
                for (String reward : quest.getRewards()) {
                    try {
                        // Handle XP rewards
                        if (reward.toLowerCase().startsWith("xp:")) {
                            String[] xpParts = reward.split(":");
                            int xpAmount = Integer.parseInt(xpParts[1].trim());
                            player.giveExperiencePoints(xpAmount);
                            player.sendSystemMessage(Component.literal("  §b+ " + xpAmount + " XP"));
                            continue;
                        }
                        
                        String[] parts = reward.split(":");
                        String itemName = parts[0].toLowerCase().trim();
                        int count = parts.length > 1 ? Integer.parseInt(parts[1].trim()) : 1;
                        
                        var stack = QuestTracker.getItemStack(itemName, count);
                        if (stack != null) {
                            boolean added = player.getInventory().add(stack);
                            if (added || stack.isEmpty()) {
                                player.sendSystemMessage(Component.literal("  §a+ " + count + "x " + itemName.replace("_", " ")));
                            } else {
                                player.drop(stack, false);
                                player.sendSystemMessage(Component.literal("  §a+ " + count + "x " + itemName.replace("_", " ") + " §7(dropped)"));
                            }
                        }
                    } catch (Exception e) {
                        player.sendSystemMessage(Component.literal("  §cError: " + reward));
                    }
                }
                
                // Give bonus XP per quest
                int bonusXP = quest.getChapter() * 50;
                player.giveExperiencePoints(bonusXP);
                player.sendSystemMessage(Component.literal("  §b+ " + bonusXP + " XP (bonus)"));
                
                data.claimReward(questId);
                claimed++;
            }
        }
        
        QuestDataManager.savePlayerData(player);
        
        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal("§aClaimed rewards from " + claimed + " quest(s)!"));
        player.sendSystemMessage(Component.literal("§6========================================"));
        player.playSound(net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        
        return 1;
    }
    
    private static int resetQuest(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
            context.getSource().sendFailure(Component.literal("Only players can use this command"));
            return 0;
        }

        int questId = IntegerArgumentType.getInteger(context, "id");
        Quest quest = QuestManager.getQuest(questId);

        if (quest == null) {
            player.sendSystemMessage(Component.literal("§cQuest #" + questId + " not found!"));
            return 0;
        }

        PlayerQuestData data = QuestDataManager.getPlayerData(player);
        data.resetQuest(questId);
        data.startQuest(questId); // Auto-start it again
        QuestDataManager.savePlayerData(player);
        
        player.sendSystemMessage(Component.literal("§e⟲ Quest Reset: §f" + quest.getName()));
        player.sendSystemMessage(Component.literal("§7You can now complete this quest again!"));
        
        return 1;
    }
    
    private static int resetAllQuests(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
            context.getSource().sendFailure(Component.literal("Only players can use this command"));
            return 0;
        }

        PlayerQuestData data = QuestDataManager.getPlayerData(player);
        int totalQuests = QuestManager.getQuestCount();
        
        data.resetAllQuests();
        
        // Auto-start all quests again
        for (int questId : QuestManager.getAllQuestIds()) {
            data.startQuest(questId);
        }
        
        QuestDataManager.savePlayerData(player);
        
        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal("§6========================================"));
        player.sendSystemMessage(Component.literal("§e⟲ ALL QUESTS RESET!"));
        player.sendSystemMessage(Component.literal("§7Reset " + totalQuests + " quests"));
        player.sendSystemMessage(Component.literal("§7All quests are now ready to complete again!"));
        player.sendSystemMessage(Component.literal("§6========================================"));
        player.sendSystemMessage(Component.literal(""));
        
        return 1;
    }
}
