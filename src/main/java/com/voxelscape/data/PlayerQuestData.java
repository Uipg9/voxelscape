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
    private final Map<Integer, Map<String, Integer>> questProgress = new HashMap<>();
    private final Set<Integer> unclaimedRewards = new HashSet<>(); // Quests with pending rewards
    private int questPoints = 0; // Quest Points for perk shop
    private final Set<String> unlockedPerks = new HashSet<>(); // Purchased perks
    private final Set<String> collectedItems = new HashSet<>(); // Collection log

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
