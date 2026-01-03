package com.voxelscape.data;

import com.voxelscape.quest.QuestStatus;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;

import java.util.*;

public class PlayerQuestData {
    private final Set<Integer> completedQuests = new HashSet<>();
    private final Set<Integer> inProgressQuests = new HashSet<>();
    private final Set<Integer> unclaimedRewards = new HashSet<>(); // Quests with pending rewards
    private int questPoints = 0; // Quest Points for perk shop
    private final Set<String> unlockedPerks = new HashSet<>(); // Purchased perks
    private final Set<String> collectedItems = new HashSet<>(); // Collection log
    
    // NEW: Daily quest system
    private int dailyQuestId = -1;
    private long dailyQuestExpiry = 0; // Timestamp when daily quest expires
    private int questStreak = 0; // Consecutive days with completed quests
    private long lastQuestCompletionDay = 0; // Day timestamp for streak tracking

    public void completeQuest(int questId) {
        inProgressQuests.remove(questId);
        completedQuests.add(questId);
        unclaimedRewards.add(questId); // Mark reward as unclaimed
    }
    
    public void claimReward(int questId) {
        unclaimedRewards.remove(questId);
    }
    
    public boolean hasUnclaimedReward(int questId) {
        return unclaimedRewards.contains(questId);
    }
    
    public Set<Integer> getUnclaimedRewards() {
        return new HashSet<>(unclaimedRewards);
    }
    
    public void resetQuest(int questId) {
        completedQuests.remove(questId);
        inProgressQuests.remove(questId);
        unclaimedRewards.remove(questId);
    }
    
    public void resetAllQuests() {
        completedQuests.clear();
        inProgressQuests.clear();
        unclaimedRewards.clear();
    }

    public void startQuest(int questId) {
        if (!completedQuests.contains(questId)) {
            inProgressQuests.add(questId);
        }
    }

    public QuestStatus getQuestStatus(int questId, List<Integer> dependencies) {
        if (completedQuests.contains(questId)) {
            return QuestStatus.COMPLETED;
        }
        if (inProgressQuests.contains(questId)) {
            return QuestStatus.IN_PROGRESS;
        }
        
        // Check dependencies
        for (int dep : dependencies) {
            if (!completedQuests.contains(dep)) {
                return QuestStatus.LOCKED;
            }
        }
        
        return QuestStatus.AVAILABLE;
    }

    public boolean isQuestCompleted(int questId) {
        return completedQuests.contains(questId);
    }

    public boolean isQuestInProgress(int questId) {
        return inProgressQuests.contains(questId);
    }

    public Set<Integer> getCompletedQuests() {
        return new HashSet<>(completedQuests);
    }

    public Set<Integer> getInProgressQuests() {
        return new HashSet<>(inProgressQuests);
    }
    
    // Quest Points management
    public int getQuestPoints() {
        return questPoints;
    }
    
    public void addQuestPoints(int amount) {
        questPoints += amount;
    }
    
    public boolean spendQuestPoints(int amount) {
        if (questPoints >= amount) {
            questPoints -= amount;
            return true;
        }
        return false;
    }
    
    // Perk management
    public boolean hasPerk(String perkId) {
        return unlockedPerks.contains(perkId);
    }
    
    public void unlockPerk(String perkId) {
        unlockedPerks.add(perkId);
    }
    
    public Set<String> getUnlockedPerks() {
        return new HashSet<>(unlockedPerks);
    }
    
    // Collection log management
    public void collectItem(String itemId) {
        collectedItems.add(itemId);
    }
    
    public boolean hasCollected(String itemId) {
        return collectedItems.contains(itemId);
    }
    
    public Set<String> getCollectedItems() {
        return new HashSet<>(collectedItems);
    }
    
    // NEW: Daily quest management
    public int getDailyQuestId() {
        return dailyQuestId;
    }
    
    public void setDailyQuest(int questId, long expiryTime) {
        this.dailyQuestId = questId;
        this.dailyQuestExpiry = expiryTime;
    }
    
    public boolean hasDailyQuest() {
        return dailyQuestId != -1 && System.currentTimeMillis() < dailyQuestExpiry;
    }
    
    public long getDailyQuestExpiry() {
        return dailyQuestExpiry;
    }
    
    public void clearDailyQuest() {
        this.dailyQuestId = -1;
        this.dailyQuestExpiry = 0;
    }
    
    // NEW: Quest streak management
    public int getQuestStreak() {
        return questStreak;
    }
    
    public void incrementStreak() {
        questStreak++;
    }
    
    public void resetStreak() {
        questStreak = 0;
    }
    
    public void updateLastQuestDay() {
        // Store day number (not exact timestamp)
        lastQuestCompletionDay = System.currentTimeMillis() / (24 * 60 * 60 * 1000);
    }
    
    public boolean shouldResetStreak() {
        long currentDay = System.currentTimeMillis() / (24 * 60 * 60 * 1000);
        return currentDay - lastQuestCompletionDay > 1; // More than 1 day gap
    }

    // NBT Serialization
    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        
        tag.put("completed", new IntArrayTag(completedQuests.stream().mapToInt(i -> i).toArray()));
        tag.put("inProgress", new IntArrayTag(inProgressQuests.stream().mapToInt(i -> i).toArray()));
        tag.put("unclaimed", new IntArrayTag(unclaimedRewards.stream().mapToInt(i -> i).toArray()));
        
        // Save quest points
        tag.putInt("questPoints", questPoints);
        
        // Save unlocked perks
        ListTag perksTag = new ListTag();
        for (String perk : unlockedPerks) {
            perksTag.add(StringTag.valueOf(perk));
        }
        tag.put("unlockedPerks", perksTag);
        
        // Save collected items
        ListTag collectionTag = new ListTag();
        for (String item : collectedItems) {
            collectionTag.add(StringTag.valueOf(item));
        }
        tag.put("collectedItems", collectionTag);
        
        // Save daily quest data
        tag.putInt("dailyQuestId", dailyQuestId);
        tag.putLong("dailyQuestExpiry", dailyQuestExpiry);
        tag.putInt("questStreak", questStreak);
        tag.putLong("lastQuestDay", lastQuestCompletionDay);
        
        return tag;
    }

    public void fromNbt(CompoundTag tag) {
        completedQuests.clear();
        inProgressQuests.clear();
        unclaimedRewards.clear();
        
        if (tag.contains("completed")) {
            int[] completed = tag.getIntArray("completed").orElse(new int[0]);
            for (int id : completed) {
                completedQuests.add(id);
            }
        }
        
        if (tag.contains("inProgress")) {
            int[] inProgress = tag.getIntArray("inProgress").orElse(new int[0]);
            for (int id : inProgress) {
                inProgressQuests.add(id);
            }
        }
        
        if (tag.contains("unclaimed")) {
            int[] unclaimed = tag.getIntArray("unclaimed").orElse(new int[0]);
            for (int id : unclaimed) {
                unclaimedRewards.add(id);
            }
        }
        
        // Load quest points
        if (tag.contains("questPoints")) {
            questPoints = tag.getInt("questPoints").orElse(0);
        }
        
        // Load unlocked perks
        unlockedPerks.clear();
        if (tag.contains("unlockedPerks")) {
            ListTag perksTag = tag.getList("unlockedPerks").orElse(new ListTag());
            for (int i = 0; i < perksTag.size(); i++) {
                String perk = perksTag.getString(i).orElse("");
                if (!perk.isEmpty()) {
                    unlockedPerks.add(perk);
                }
            }
        }
        
        // Load collected items
        collectedItems.clear();
        if (tag.contains("collectedItems")) {
            ListTag collectionTag = tag.getList("collectedItems").orElse(new ListTag());
            for (int i = 0; i < collectionTag.size(); i++) {
                String item = collectionTag.getString(i).orElse("");
                if (!item.isEmpty()) {
                    collectedItems.add(item);
                }
            }
        }
        
        // Load daily quest data
        if (tag.contains("dailyQuestId")) {
            dailyQuestId = tag.getInt("dailyQuestId").orElse(-1);
        }
        if (tag.contains("dailyQuestExpiry")) {
            dailyQuestExpiry = tag.getLong("dailyQuestExpiry").orElse(0L);
        }
        if (tag.contains("questStreak")) {
            questStreak = tag.getInt("questStreak").orElse(0);
        }
        if (tag.contains("lastQuestDay")) {
            lastQuestCompletionDay = tag.getLong("lastQuestDay").orElse(0L);
        }
    }
    
    // For client-side data
    public void fromClientData(Set<Integer> completed, Set<Integer> inProgress, Set<Integer> unclaimed) {
        this.completedQuests.clear();
        this.completedQuests.addAll(completed);
        this.inProgressQuests.clear();
        this.inProgressQuests.addAll(inProgress);
        this.unclaimedRewards.clear();
        this.unclaimedRewards.addAll(unclaimed);
    }
}
