package com.voxelscape.gui;

import com.voxelscape.data.PlayerQuestData;
import com.voxelscape.data.QuestDataManager;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

public class PerkShopGui extends SimpleGui {
    private static final int[] PERK_SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25};
    
    public PerkShopGui(ServerPlayer player) {
        super(MenuType.GENERIC_9x3, player, false);
        this.setTitle(Component.literal("§6§lPerk Shop §8(QP: §e" + QuestDataManager.getPlayerData(player).getQuestPoints() + "§8)"));
        
        setupPerks(player);
        setupBackButton();
    }
    
    private void setupPerks(ServerPlayer player) {
        PlayerQuestData data = QuestDataManager.getPlayerData(player);
        
        // Movement Perks - Speed Boost disabled (MobEffects not available)
        
        // Utility Perks
        addPerk(player, 12, "night_vision", "§eNight Vision", 12, Items.GOLDEN_CARROT,
            "§7Permanent night vision",
            "§7See in the dark!");
        
        addPerk(player, 13, "auto_replant", "§eAuto-Replant", 10, Items.WHEAT_SEEDS,
            "§7Auto-replant crops when broken",
            "§7Never replant manually again!");
        
        addPerk(player, 14, "magnetism_1", "§eMagnetism I", 5, Items.IRON_INGOT,
            "§7XP & Items: +2 block range",
            "§7Collect loot easier!");
        
        addPerk(player, 15, "magnetism_2", "§eMagnetism II", 12, Items.GOLD_INGOT,
            "§7XP & Items: +4 block range",
            "§8Requires: Magnetism I",
            "magnetism_1"); // Prerequisite perk ID
        
        // Combat Perks
        addPerk(player, 16, "strength_boost", "§cStrength I", 20, Items.BLAZE_POWDER,
            "§7Permanent Strength I effect",
            "§7Deal 3 more damage!");
        
        addPerk(player, 19, "resistance", "§cResistance I", 20, Items.IRON_CHESTPLATE,
            "§7Permanent Resistance I effect",
            "§7Take 20% less damage!");
        
        // Mining Perks
        addPerk(player, 20, "aqua_affinity", "§3Aqua Affinity", 15, Items.PRISMARINE_SHARD,
            "§7Mine faster underwater",
            "§7Ocean mining made easy!");
        
        addPerk(player, 21, "haste_1", "§3Haste I", 18, Items.DIAMOND_PICKAXE,
            "§7Permanent Haste I effect",
            "§7Mine 20% faster!");
        
        // Special Perks
        addPerk(player, 22, "fortune_luck", "§dLucky Miner", 25, Items.EMERALD,
            "§710% chance for double drops",
            "§7When mining ores!");
        
        addPerk(player, 23, "mob_heads", "§dHead Hunter", 30, Items.ZOMBIE_HEAD,
            "§75% chance for mob heads",
            "§7From any mob kill!");
        
        addPerk(player, 24, "xp_boost", "§dXP Multiplier", 35, Items.EXPERIENCE_BOTTLE,
            "§7+50% XP from all sources",
            "§7Level up faster!");
        
        addPerk(player, 25, "keep_inventory", "§5Soul Keeper", 50, Items.TOTEM_OF_UNDYING,
            "§7Keep items on death",
            "§c§lONE TIME USE PER DEATH!");
    }
    
    private void addPerk(ServerPlayer player, int slot, String perkId, String name, int cost, 
                         net.minecraft.world.item.Item displayItem, String... loreAndPrereq) {
        PlayerQuestData data = QuestDataManager.getPlayerData(player);
        boolean unlocked = data.hasPerk(perkId);
        boolean canAfford = data.getQuestPoints() >= cost;
        
        // Check for prerequisite (last parameter if it's a perk ID)
        String prerequisite = null;
        String[] lore = loreAndPrereq;
        if (loreAndPrereq.length > 0) {
            String lastParam = loreAndPrereq[loreAndPrereq.length - 1];
            // If last param contains underscore, it's likely a perk ID prerequisite
            if (lastParam.contains("_") && !lastParam.contains("§")) {
                prerequisite = lastParam;
                lore = new String[loreAndPrereq.length - 1];
                System.arraycopy(loreAndPrereq, 0, lore, 0, lore.length);
            }
        }
        
        boolean hasPrerequisite = prerequisite == null || data.hasPerk(prerequisite);
        
        GuiElementBuilder builder = new GuiElementBuilder(unlocked ? Items.ENCHANTED_BOOK : displayItem);
        builder.setName(Component.literal((unlocked ? "§a✔ " : "§7") + name));
        
        if (unlocked) {
            builder.addLoreLine(Component.literal("§a§lUNLOCKED!"));
            builder.addLoreLine(Component.literal(""));
            for (String line : lore) {
                builder.addLoreLine(Component.literal(line));
            }
        } else {
            builder.addLoreLine(Component.literal("§6Cost: §e" + cost + " Quest Points"));
            builder.addLoreLine(Component.literal(""));
            for (String line : lore) {
                builder.addLoreLine(Component.literal(line));
            }
            builder.addLoreLine(Component.literal(""));
            if (!hasPrerequisite) {
                builder.addLoreLine(Component.literal("§c§lPREREQUISITE REQUIRED!"));
            } else if (canAfford) {
                builder.addLoreLine(Component.literal("§a§l➤ CLICK TO PURCHASE"));
            } else {
                builder.addLoreLine(Component.literal("§c§lNOT ENOUGH QP!"));
            }
        }
        
        builder.setCallback((index, type, action) -> {
            if (!unlocked && !hasPrerequisite) {
                player.sendSystemMessage(Component.literal("§c✖ You must unlock the prerequisite perk first!"));
                player.playSound(SoundEvents.VILLAGER_NO, 1.0f, 1.0f);
            } else if (!unlocked && canAfford) {
                purchasePerk(player, perkId, name, cost);
            } else if (!unlocked) {
                player.sendSystemMessage(Component.literal("§c✖ You need " + cost + " Quest Points!"));
                player.playSound(SoundEvents.VILLAGER_NO, 1.0f, 1.0f);
            } else {
                player.sendSystemMessage(Component.literal("§a✔ You already own this perk!"));
                player.playSound(SoundEvents.VILLAGER_NO, 1.0f, 1.0f);
            }
        });
        
        this.setSlot(slot, builder);
    }
    
    private void purchasePerk(ServerPlayer player, String perkId, String name, int cost) {
        PlayerQuestData data = QuestDataManager.getPlayerData(player);
        
        if (data.spendQuestPoints(cost)) {
            data.unlockPerk(perkId);
            QuestDataManager.savePlayerData(player);
            
            // Success message
            player.sendSystemMessage(Component.literal(""));
            player.sendSystemMessage(Component.literal("§6╔══════════════════════════════════════╗"));
            player.sendSystemMessage(Component.literal("§6║     §a§l✔ PERK UNLOCKED!              §6║"));
            player.sendSystemMessage(Component.literal("§6╚══════════════════════════════════════╝"));
            player.sendSystemMessage(Component.literal(""));
            player.sendSystemMessage(Component.literal(name));
            player.sendSystemMessage(Component.literal("§7Spent: §e-" + cost + " QP"));
            player.sendSystemMessage(Component.literal("§7Balance: §e" + data.getQuestPoints() + " QP"));
            player.sendSystemMessage(Component.literal(""));
            
            // Play success sounds - ka-ching!
            player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            player.playSound(SoundEvents.PLAYER_LEVELUP, 1.0f, 1.5f);
            
            // Refresh GUI
            this.close();
            new PerkShopGui(player).open();
        }
    }
    
    private void setupBackButton() {
        GuiElementBuilder backButton = new GuiElementBuilder(Items.BARRIER);
        backButton.setName(Component.literal("§cClose"));
        backButton.setCallback((index, type, action) -> {
            this.close();
        });
        this.setSlot(26, backButton);
    }
}
