package com.voxelscape.gui;

import com.voxelscape.data.PlayerQuestData;
import com.voxelscape.data.QuestDataManager;
import com.voxelscape.events.QuestTracker;
import com.voxelscape.quest.Quest;
import com.voxelscape.quest.QuestManager;
import com.voxelscape.quest.QuestStatus;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

import java.util.Comparator;
import java.util.List;

public class QuestListGui extends SimpleGui {
    private final PlayerQuestData questData;
    private int page = 0;
    private final int questsPerPage = 45; // 5 rows for quests

    public QuestListGui(ServerPlayer player) {
        super(MenuType.GENERIC_9x6, player, false);
        this.questData = QuestDataManager.getPlayerData(player);
        this.setTitle(Component.literal("§6§lVoxel Scape Quests"));
        updateDisplay();
    }

    private void updateDisplay() {
        // Clear all slots first
        for (int i = 0; i < 54; i++) {
            this.clearSlot(i);
        }

        List<Quest> allQuests = QuestManager.getAllQuests().stream()
            .sorted(Comparator.comparingInt(Quest::getId))
            .toList();

        int totalPages = (int) Math.ceil((double) allQuests.size() / questsPerPage);
        int startIndex = page * questsPerPage;
        int endIndex = Math.min(startIndex + questsPerPage, allQuests.size());

        // Display quests
        int slot = 0;
        for (int i = startIndex; i < endIndex; i++) {
            Quest quest = allQuests.get(i);
            QuestStatus status = questData.getQuestStatus(quest.getId(), quest.getDependencies());
            
            GuiElementBuilder builder = new GuiElementBuilder();
            
            // Choose item and color based on status
            switch (status) {
                case COMPLETED -> {
                    builder.setItem(Items.LIME_DYE);
                    builder.setName(Component.literal("§a✓ " + quest.getName()));
                }
                case IN_PROGRESS -> {
                    builder.setItem(Items.YELLOW_DYE);
                    builder.setName(Component.literal("§e⚬ " + quest.getName()));
                }
                case AVAILABLE -> {
                    builder.setItem(Items.RED_DYE);
                    builder.setName(Component.literal("§c○ " + quest.getName()));
                }
                case LOCKED -> {
                    builder.setItem(Items.GRAY_DYE);
                    builder.setName(Component.literal("§8✗ " + quest.getName()));
                }
            }

            // Add lore (description)
            builder.addLoreLine(Component.literal("§7" + quest.getDescription()));
            builder.addLoreLine(Component.literal(""));
            
            // Show requirements
            if (!quest.getRequirements().isEmpty()) {
                builder.addLoreLine(Component.literal("§6Requirements:"));
                for (String req : quest.getRequirements()) {
                    builder.addLoreLine(Component.literal("§7- " + req));
                }
                builder.addLoreLine(Component.literal(""));
            }
            
            // Show rewards
            if (!quest.getRewards().isEmpty()) {
                builder.addLoreLine(Component.literal("§6Rewards:"));
                for (String reward : quest.getRewards()) {
                    String[] parts = reward.split(":");
                    if (parts.length == 2) {
                        builder.addLoreLine(Component.literal("§7- " + parts[1] + "x " + parts[0]));
                    }
                }
                builder.addLoreLine(Component.literal(""));
            }
            
            // Show if reward is unclaimed
            if (questData.hasUnclaimedReward(quest.getId())) {
                builder.addLoreLine(Component.literal("§6§l⚠ REWARD READY TO CLAIM!"));
                builder.addLoreLine(Component.literal("§eUse /quest claim " + quest.getId()));
            }
            
            // Add dependencies info
            if (!quest.getDependencies().isEmpty()) {
                builder.addLoreLine(Component.literal("§7Requires:"));
                for (int depId : quest.getDependencies()) {
                    Quest dep = QuestManager.getQuest(depId);
                    if (dep != null) {
                        builder.addLoreLine(Component.literal("§7- Quest #" + depId + ": " + dep.getName()));
                    }
                }
            }

            // Click handler
            final int questId = quest.getId();
            builder.setCallback((index, type, action) -> {
                // If quest has unclaimed reward, claim it directly!
                if (questData.hasUnclaimedReward(questId)) {
                    player.closeContainer(); // Close GUI first
                    
                    // Claim reward directly
                    for (String reward : quest.getRewards()) {
                        try {
                            // Handle XP rewards
                            if (reward.toLowerCase().startsWith("xp:")) {
                                String[] parts = reward.split(":");
                                int xpAmount = Integer.parseInt(parts[1].trim());
                                player.giveExperiencePoints(xpAmount);
                                continue;
                            }
                            
                            String[] parts = reward.split(":");
                            if (parts.length == 2) {
                                String itemName = parts[0].toLowerCase().trim();
                                int count = Integer.parseInt(parts[1].trim());
                                
                                var stack = QuestTracker.getItemStack(itemName, count);
                                if (stack != null && !stack.isEmpty()) {
                                    boolean added = player.getInventory().add(stack);
                                    if (!added && !stack.isEmpty()) {
                                        player.drop(stack, false);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            // Silent fail, will be logged elsewhere if needed
                        }
                    }
                    
                    // Give bonus XP
                    int bonusXP = quest.getChapter() * 50;
                    player.giveExperiencePoints(bonusXP);
                    
                    // Mark as claimed
                    questData.claimReward(questId);
                    QuestDataManager.savePlayerData(player);
                    
                    player.sendSystemMessage(Component.literal("§a✓ Claimed rewards for: §e" + quest.getName()));
                    player.playSound(net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                    
                    // Refresh GUI
                    updateDisplay();
                } else {
                    player.sendSystemMessage(Component.literal("§7Use §6/quest " + questId + " §7for details"));
                    if (status == QuestStatus.COMPLETED) {
                        player.sendSystemMessage(Component.literal("§aReward already claimed!"));
                    }
                }
            });

            this.setSlot(slot++, builder);
        }

        // Navigation buttons in bottom row
        if (page > 0) {
            // Previous page button
            this.setSlot(45, new GuiElementBuilder()
                .setItem(Items.ARROW)
                .setName(Component.literal("§e← Previous Page"))
                .setCallback((index, type, action) -> {
                    page--;
                    updateDisplay();
                })
            );
        }

        // Progress indicator
        int completed = questData.getCompletedQuests().size();
        int total = QuestManager.getQuestCount();
        this.setSlot(49, new GuiElementBuilder()
            .setItem(Items.BOOK)
            .setName(Component.literal("§6§lQuest Progress"))
            .addLoreLine(Component.literal("§7Completed: §a" + completed + "§7/§a" + total))
            .addLoreLine(Component.literal("§7Page: §e" + (page + 1) + "§7/§e" + totalPages))
        );

        // Unclaimed rewards indicator
        int unclaimed = questData.getUnclaimedRewards().size();
        if (unclaimed > 0) {
            this.setSlot(50, new GuiElementBuilder()
                .setItem(Items.CHEST)
                .setName(Component.literal("§6§l⚠ Unclaimed Rewards"))
                .addLoreLine(Component.literal("§7You have §e" + unclaimed + " §7rewards to claim!"))
                .addLoreLine(Component.literal("§eUse /quest claim to claim all"))
            );
        }

        if (page < totalPages - 1) {
            // Next page button
            this.setSlot(53, new GuiElementBuilder()
                .setItem(Items.ARROW)
                .setName(Component.literal("§eNext Page →"))
                .setCallback((index, type, action) -> {
                    page++;
                    updateDisplay();
                })
            );
        }

        // Close button
        this.setSlot(48, new GuiElementBuilder()
            .setItem(Items.BARRIER)
            .setName(Component.literal("§cClose"))
            .setCallback((index, type, action) -> this.close())
        );
    }
}
