package com.voxelscape.quest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.voxelscape.VoxelScape;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class QuestManager {
    private static final Map<Integer, Quest> QUESTS = new HashMap<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void loadQuests() {
        Path questFile = Paths.get("config", "voxelscape", "quests.json");
        
        // Create default quests if file doesn't exist
        if (!Files.exists(questFile)) {
            VoxelScape.LOGGER.info("Creating default quest data...");
            createDefaultQuests();
            saveQuests(questFile);
        } else {
            try (Reader reader = Files.newBufferedReader(questFile)) {
                Type type = new TypeToken<List<Quest>>(){}.getType();
                List<Quest> quests = GSON.fromJson(reader, type);
                for (Quest quest : quests) {
                    QUESTS.put(quest.getId(), quest);
                }
                VoxelScape.LOGGER.info("Loaded {} quests from file", QUESTS.size());
            } catch (Exception e) {
                VoxelScape.LOGGER.error("Failed to load quests", e);
            }
        }
    }

    private static void createDefaultQuests() {
        // Chapter 1: Genesis (Quests 1-20) - Early Game Basics
        QUESTS.put(1, new Quest(1, "Awakening", "Open the quest menu to receive your starter gift", 1,
            List.of("Type /quests"), List.of("apple:3"), new ArrayList<>()));
        
        QUESTS.put(2, new Quest(2, "Knock on Wood", "Collect 4 Oak Logs", 1,
            List.of("Collect 4 Oak Logs"), List.of("stick:4"), List.of(1)));
        
        QUESTS.put(3, new Quest(3, "Workbench", "Craft a Crafting Table", 1,
            List.of("Craft 1 Crafting Table"), List.of("leather:1", "bread:2"), List.of(2)));
        
        QUESTS.put(4, new Quest(4, "Stick Figure", "Craft 8 Sticks", 1,
            List.of("Craft 8 Sticks"), List.of("torch:2"), List.of(3)));
        
        QUESTS.put(5, new Quest(5, "First Strike", "Craft a Wooden Sword", 1,
            List.of("Craft 1 Wooden Sword"), List.of("bread:2", "apple:1"), List.of(4)));
        
        QUESTS.put(6, new Quest(6, "Dig In", "Craft a Wooden Pickaxe", 1,
            List.of("Craft 1 Wooden Pickaxe"), List.of("bread:3", "torch:4"), List.of(4)));
        
        QUESTS.put(7, new Quest(7, "Stone Age", "Mine 3 Cobblestone", 1,
            List.of("Mine 3 Cobblestone"), List.of("cobblestone:8"), List.of(6)));
        
        QUESTS.put(8, new Quest(8, "Upgrade", "Craft a Stone Pickaxe", 1,
            List.of("Craft 1 Stone Pickaxe"), List.of("torch:16", "coal:2"), List.of(7)));
        
        QUESTS.put(9, new Quest(9, "Lumberjack I", "Collect 32 Logs", 1,
            List.of("Collect 32 Logs (any wood)"), List.of("coal:8", "torch:8"), List.of(2)));
        
        QUESTS.put(10, new Quest(10, "Charcoal Grill", "Smelt 8 Logs into Charcoal", 1,
            List.of("Smelt 8 Charcoal"), List.of("coal:8", "bread:4"), List.of(9)));

        QUESTS.put(11, new Quest(11, "Light Show", "Craft 16 Torches", 1,
            List.of("Craft 16 Torches"), List.of("bread:5", "coal:4"), List.of(10)));
        
        QUESTS.put(12, new Quest(12, "Hoarder", "Craft 2 Chests", 1,
            List.of("Craft 2 Chests"), List.of("oak_log:8", "iron_ingot:1"), List.of(3)));
        
        QUESTS.put(13, new Quest(13, "Home Sweet Home", "Build a shelter with walls and a roof", 1,
            List.of("Build a simple shelter"), List.of("wool:4", "bread:3"), List.of(12)));
        
        QUESTS.put(14, new Quest(14, "Sleeping Tight", "Craft a Bed", 1,
            List.of("Craft 1 Bed"), List.of("wool:8", "bread:4"), List.of(13)));
        
        QUESTS.put(15, new Quest(15, "Stone Warrior", "Craft a full set of Stone Tools", 1,
            List.of("Craft Stone Sword, Pickaxe, Axe, Shovel"), List.of("iron_ingot:3", "coal:8"), List.of(8)));
        
        QUESTS.put(16, new Quest(16, "Heated Debate", "Craft a Furnace", 1,
            List.of("Craft 1 Furnace"), List.of("cobblestone:16", "coal:4"), List.of(7)));
        
        QUESTS.put(17, new Quest(17, "Baker's Dozen", "Craft 13 Bread", 1,
            List.of("Craft 13 Bread"), List.of("golden_apple:1"), List.of(3)));
        
        QUESTS.put(18, new Quest(18, "Deep Mining", "Mine down to Y-level 16 or below", 1,
            List.of("Reach Y-16 or lower"), List.of("torch:32", "bread:8"), List.of(8)));
        
        QUESTS.put(19, new Quest(19, "The Floor is Lava", "Find Lava", 1,
            List.of("Locate a lava source"), List.of("water_bucket:1", "cobblestone:16"), List.of(18)));
        
        QUESTS.put(20, new Quest(20, "Miner's Delight", "Mine 64 Cobblestone", 1,
            List.of("Mine 64 Cobblestone"), List.of("iron_ingot:2", "coal:16"), List.of(7)));

        // Chapter 2: Iron Era (Quests 21-40) - Mid Early Game
        QUESTS.put(21, new Quest(21, "The Iron Age", "Mine 3 Iron Ore", 2,
            List.of("Mine 3 Iron Ore"), List.of("coal:16"), List.of(18)));
        
        QUESTS.put(22, new Quest(22, "Smelter", "Smelt 3 Iron Ingots", 2,
            List.of("Smelt 3 Iron Ingots"), List.of("iron_ingot:2"), List.of(21)));
        
        QUESTS.put(23, new Quest(23, "Iron Will", "Craft an Iron Pickaxe", 2,
            List.of("Craft 1 Iron Pickaxe"), List.of("iron_ingot:3", "coal:8"), List.of(22)));
        
        QUESTS.put(24, new Quest(24, "Iron Man", "Craft a full set of Iron Tools", 2,
            List.of("Craft Iron Sword, Pickaxe, Axe, Shovel"), List.of("diamond:1", "iron_ingot:8"), List.of(23)));
        
        QUESTS.put(25, new Quest(25, "Armor Up I", "Craft an Iron Helmet", 2,
            List.of("Craft 1 Iron Helmet"), List.of("iron_ingot:3"), List.of(22)));
        
        QUESTS.put(26, new Quest(26, "Armor Up II", "Craft an Iron Chestplate", 2,
            List.of("Craft 1 Iron Chestplate"), List.of("iron_ingot:5"), List.of(25)));
        
        QUESTS.put(27, new Quest(27, "Armor Up III", "Craft Iron Leggings", 2,
            List.of("Craft 1 Iron Leggings"), List.of("iron_ingot:4"), List.of(26)));
        
        QUESTS.put(28, new Quest(28, "Armor Up IV", "Craft Iron Boots", 2,
            List.of("Craft 1 Iron Boots"), List.of("iron_ingot:3"), List.of(27)));
        
        QUESTS.put(29, new Quest(29, "Full Iron Armor", "Wear a complete set of Iron Armor", 2,
            List.of("Equip full Iron Armor"), List.of("diamond:2", "iron_ingot:16"), List.of(28)));
        
        QUESTS.put(30, new Quest(30, "Shield Bearer", "Craft a Shield", 2,
            List.of("Craft 1 Shield"), List.of("iron_ingot:4"), List.of(22)));
        
        QUESTS.put(31, new Quest(31, "Bucket List", "Craft a Bucket", 2,
            List.of("Craft 1 Bucket"), List.of("water_bucket:1"), List.of(22)));
        
        QUESTS.put(32, new Quest(32, "Waterworks", "Collect Water in a Bucket", 2,
            List.of("Fill 1 Water Bucket"), List.of("bucket:2"), List.of(31)));
        
        QUESTS.put(33, new Quest(33, "Obsidian Discovery", "Create Obsidian", 2,
            List.of("Create 1 Obsidian block"), List.of("diamond:1", "obsidian:4"), List.of(32, 19)));
        
        QUESTS.put(34, new Quest(34, "Lumberjack II", "Collect 128 Logs", 2,
            List.of("Collect 128 Logs (any wood)"), List.of("iron_ingot:8", "coal:16"), List.of(9)));
        
        QUESTS.put(35, new Quest(35, "Stone Mason", "Mine 256 Stone", 2,
            List.of("Mine 256 Stone/Cobblestone"), List.of("iron_ingot:12"), List.of(20)));
        
        QUESTS.put(36, new Quest(36, "Coal Baron", "Mine 64 Coal Ore", 2,
            List.of("Mine 64 Coal Ore"), List.of("torch:64", "coal:32"), List.of(18)));
        
        QUESTS.put(37, new Quest(37, "Iron Reserves", "Mine 32 Iron Ore", 2,
            List.of("Mine 32 Iron Ore"), List.of("diamond:2", "iron_ingot:16"), List.of(23)));
        
        QUESTS.put(38, new Quest(38, "Gold Rush", "Mine 16 Gold Ore", 2,
            List.of("Mine 16 Gold Ore"), List.of("gold_ingot:8", "iron_ingot:4"), List.of(23)));
        
        QUESTS.put(39, new Quest(39, "Redstone Engineer", "Mine 32 Redstone", 2,
            List.of("Mine 32 Redstone Dust"), List.of("redstone:64", "iron_ingot:8"), List.of(23)));
        
        QUESTS.put(40, new Quest(40, "Lapis Collector", "Mine 16 Lapis Lazuli", 2,
            List.of("Mine 16 Lapis Lazuli"), List.of("lapis_lazuli:32", "iron_ingot:4"), List.of(23)));

        // Chapter 3: Diamond Quest (Quests 41-60) - Late Early Game
        QUESTS.put(41, new Quest(41, "Diamond Expedition", "Find Diamonds at Y-16 or below", 3,
            List.of("Reach diamond mining level"), List.of("torch:64", "bread:16"), List.of(23)));
        
        QUESTS.put(42, new Quest(42, "Diamonds!", "Mine 1 Diamond Ore", 3,
            List.of("Mine 1 Diamond Ore"), List.of("diamond:3", "iron_ingot:16"), List.of(41)));
        
        QUESTS.put(43, new Quest(43, "Diamond Pickaxe", "Craft a Diamond Pickaxe", 3,
            List.of("Craft 1 Diamond Pickaxe"), List.of("diamond:2", "iron_ingot:8"), List.of(42)));
        
        QUESTS.put(44, new Quest(44, "Diamond Tools", "Craft a full set of Diamond Tools", 3,
            List.of("Craft Diamond Sword, Pickaxe, Axe, Shovel"), List.of("diamond:8", "emerald:4"), List.of(43)));
        
        QUESTS.put(45, new Quest(45, "Diamond Armor I", "Craft a Diamond Helmet", 3,
            List.of("Craft 1 Diamond Helmet"), List.of("diamond:3"), List.of(42)));
        
        QUESTS.put(46, new Quest(46, "Diamond Armor II", "Craft a Diamond Chestplate", 3,
            List.of("Craft 1 Diamond Chestplate"), List.of("diamond:5"), List.of(45)));
        
        QUESTS.put(47, new Quest(47, "Diamond Armor III", "Craft Diamond Leggings", 3,
            List.of("Craft 1 Diamond Leggings"), List.of("diamond:4"), List.of(46)));
        
        QUESTS.put(48, new Quest(48, "Diamond Armor IV", "Craft Diamond Boots", 3,
            List.of("Craft 1 Diamond Boots"), List.of("diamond:3"), List.of(47)));
        
        QUESTS.put(49, new Quest(49, "Full Diamond Armor", "Wear a complete set of Diamond Armor", 3,
            List.of("Equip full Diamond Armor"), List.of("emerald:16", "diamond:8"), List.of(48)));
        
        QUESTS.put(50, new Quest(50, "Emerald Hunter", "Find an Emerald Ore", 3,
            List.of("Mine 1 Emerald Ore"), List.of("emerald:4", "diamond:2"), List.of(41)));
        
        QUESTS.put(51, new Quest(51, "Book Worm", "Craft 15 Bookshelves", 3,
            List.of("Craft 15 Bookshelves"), List.of("book:8", "iron_ingot:16"), List.of(34)));
        
        QUESTS.put(52, new Quest(52, "Enchanter", "Craft an Enchanting Table", 3,
            List.of("Craft 1 Enchanting Table"), List.of("lapis_lazuli:64", "diamond:4"), List.of(42, 51)));
        
        QUESTS.put(53, new Quest(53, "First Enchantment", "Enchant any item", 3,
            List.of("Enchant 1 item"), List.of("lapis_lazuli:32", "diamond:2"), List.of(52)));
        
        QUESTS.put(54, new Quest(54, "Anvil Crafting", "Craft an Anvil", 3,
            List.of("Craft 1 Anvil"), List.of("iron_ingot:16"), List.of(37)));
        
        QUESTS.put(55, new Quest(55, "Portal Builder", "Build a Nether Portal", 3,
            List.of("Build a Nether Portal frame"), List.of("obsidian:14", "flint_and_steel:1"), List.of(33)));
        
        QUESTS.put(56, new Quest(56, "The Nether", "Enter the Nether dimension", 3,
            List.of("Enter the Nether"), List.of("diamond:4", "golden_apple:2"), List.of(55)));
        
        QUESTS.put(57, new Quest(57, "Zombie Slayer", "Defeat 10 Zombies", 3,
            List.of("Kill 10 Zombies"), List.of("iron_ingot:8", "bread:8"), List.of(5)));
        
        QUESTS.put(58, new Quest(58, "Skeleton Archer", "Defeat 10 Skeletons", 3,
            List.of("Kill 10 Skeletons"), List.of("arrow:64", "bow:1"), List.of(5)));
        
        QUESTS.put(59, new Quest(59, "Creeper Killer", "Defeat 5 Creepers", 3,
            List.of("Kill 5 Creepers safely"), List.of("gunpowder:16", "diamond:2"), List.of(5)));
        
        QUESTS.put(60, new Quest(60, "Monster Hunter", "Defeat at least 50 hostile mobs", 3,
            List.of("Kill 50 hostile mobs"), List.of("diamond:8", "emerald:8", "golden_apple:1"), List.of(57, 58, 59)));

        // Chapter 4: Nether Conquest (Quests 61-90) - Nether exploration
        QUESTS.put(61, new Quest(61, "Nether Explorer", "Travel 100 blocks in the Nether", 4,
            List.of("Explore the Nether dimension"), List.of("netherrack:64", "fire_charge:8"), List.of(56)));
        
        QUESTS.put(62, new Quest(62, "Hot Stuff", "Collect 32 Netherrack", 4,
            List.of("Mine 32 Netherrack"), List.of("magma_cream:4", "gold_ingot:8"), List.of(61)));
        
        QUESTS.put(63, new Quest(63, "Soul Seeker", "Find Soul Sand", 4,
            List.of("Collect Soul Sand"), List.of("soul_sand:16", "nether_wart:8"), List.of(61)));
        
        QUESTS.put(64, new Quest(64, "Wart Farmer", "Collect Nether Wart", 4,
            List.of("Collect 16 Nether Wart"), List.of("nether_wart:32", "diamond:2"), List.of(63)));
        
        QUESTS.put(65, new Quest(65, "Bright Lights", "Collect Glowstone", 4,
            List.of("Mine 16 Glowstone"), List.of("glowstone:32", "diamond:3"), List.of(61)));
        
        QUESTS.put(66, new Quest(66, "Fortress Hunter", "Find a Nether Fortress", 4,
            List.of("Locate a Nether Fortress"), List.of("diamond:4", "golden_apple:2"), List.of(61)));
        
        QUESTS.put(67, new Quest(67, "Blaze Rod Collector", "Defeat a Blaze and collect its rod", 4,
            List.of("Kill a Blaze"), List.of("blaze_rod:4", "diamond:3"), List.of(66)));
        
        QUESTS.put(68, new Quest(68, "Blaze Slayer", "Defeat 10 Blazes", 4,
            List.of("Kill 10 Blazes"), List.of("blaze_rod:8", "diamond:5", "fire_resistance_potion:2"), List.of(67)));
        
        QUESTS.put(69, new Quest(69, "Wither Skeleton Hunter", "Defeat a Wither Skeleton", 4,
            List.of("Kill a Wither Skeleton"), List.of("coal:32", "diamond:4"), List.of(66)));
        
        QUESTS.put(70, new Quest(70, "Skull Collector", "Obtain a Wither Skeleton Skull", 4,
            List.of("Get 1 Wither Skeleton Skull"), List.of("diamond:8", "emerald:4"), List.of(69)));
        
        QUESTS.put(71, new Quest(71, "Magma Explorer", "Find a Magma Cube", 4,
            List.of("Encounter a Magma Cube"), List.of("magma_cream:8", "gold_ingot:16"), List.of(61)));
        
        QUESTS.put(72, new Quest(72, "Ghast Hunter", "Defeat a Ghast", 4,
            List.of("Kill a Ghast"), List.of("ghast_tear:2", "diamond:4"), List.of(61)));
        
        QUESTS.put(73, new Quest(73, "Tear Collector", "Collect 5 Ghast Tears", 4,
            List.of("Collect 5 Ghast Tears"), List.of("regeneration_potion:3", "diamond:6"), List.of(72)));
        
        QUESTS.put(74, new Quest(74, "Piglin Encounter", "Meet a Piglin", 4,
            List.of("Find a Piglin"), List.of("gold_ingot:32"), List.of(61)));
        
        QUESTS.put(75, new Quest(75, "Gold for Piglins", "Trade with Piglins", 4,
            List.of("Barter with Piglins 10 times"), List.of("ender_pearl:8", "diamond:5"), List.of(74)));
        
        QUESTS.put(76, new Quest(76, "Bastion Discovery", "Find a Bastion Remnant", 4,
            List.of("Locate a Bastion Remnant"), List.of("diamond:6", "ancient_debris:1"), List.of(61)));
        
        QUESTS.put(77, new Quest(77, "Ancient Debris", "Find Ancient Debris", 4,
            List.of("Mine 1 Ancient Debris"), List.of("ancient_debris:2", "diamond:8"), List.of(43)));
        
        QUESTS.put(78, new Quest(78, "Netherite Scrap", "Smelt Ancient Debris", 4,
            List.of("Smelt 4 Netherite Scrap"), List.of("netherite_scrap:2", "diamond:10"), List.of(77)));
        
        QUESTS.put(79, new Quest(79, "Netherite Ingot", "Craft a Netherite Ingot", 4,
            List.of("Craft 1 Netherite Ingot"), List.of("diamond:16", "emerald:8"), List.of(78)));
        
        QUESTS.put(80, new Quest(80, "Netherite Tool", "Upgrade a Diamond tool to Netherite", 4,
            List.of("Upgrade any Diamond tool"), List.of("netherite_ingot:1", "diamond:16"), List.of(79)));
        
        QUESTS.put(81, new Quest(81, "Netherite Armor", "Upgrade a Diamond armor piece", 4,
            List.of("Upgrade any Diamond armor"), List.of("netherite_ingot:1", "diamond:16"), List.of(79)));
        
        QUESTS.put(82, new Quest(82, "Full Netherite", "Full set of Netherite armor", 4,
            List.of("Equip full Netherite armor"), List.of("netherite_ingot:4", "diamond:32", "emerald:16"), List.of(81)));
        
        QUESTS.put(83, new Quest(83, "Strider Rider", "Ride a Strider", 4,
            List.of("Mount a Strider"), List.of("warped_fungus:16", "diamond:4"), List.of(61)));
        
        QUESTS.put(84, new Quest(84, "Lava Walker", "Ride a Strider across lava", 4,
            List.of("Travel on a Strider"), List.of("saddle:1", "diamond:6"), List.of(83)));
        
        QUESTS.put(85, new Quest(85, "Beacon Base", "Collect 164 Iron/Gold/Diamond/Emerald blocks", 4,
            List.of("Gather beacon pyramid materials"), List.of("diamond:16", "emerald:8"), List.of(49)));
        
        QUESTS.put(86, new Quest(86, "Beacon Craft", "Craft a Beacon", 4,
            List.of("Craft 1 Beacon"), List.of("diamond:32", "emerald:16", "nether_star:1"), List.of(85, 72)));
        
        QUESTS.put(87, new Quest(87, "Brewing Stand", "Craft a Brewing Stand", 4,
            List.of("Craft 1 Brewing Stand"), List.of("nether_wart:32", "blaze_powder:16"), List.of(67)));
        
        QUESTS.put(88, new Quest(88, "First Potion", "Brew your first potion", 4,
            List.of("Brew 1 potion"), List.of("glass_bottle:16", "diamond:4"), List.of(87)));
        
        QUESTS.put(89, new Quest(89, "Potion Master", "Brew 10 different potions", 4,
            List.of("Brew 10 unique potions"), List.of("diamond:16", "emerald:8", "dragon_breath:4"), List.of(88)));
        
        QUESTS.put(90, new Quest(90, "Nether Master", "Complete all Nether exploration", 4,
            List.of("Complete Chapter 4"), List.of("netherite_ingot:8", "diamond:64", "emerald:32"), List.of(82, 89, 84)));

        // Chapter 5: The End (Quests 91-120) - End dimension
        QUESTS.put(91, new Quest(91, "Eye of Ender", "Craft an Eye of Ender", 5,
            List.of("Craft 1 Eye of Ender"), List.of("ender_pearl:16", "diamond:8"), List.of(67, 75)));
        
        QUESTS.put(92, new Quest(92, "Stronghold Seeker", "Find a Stronghold", 5,
            List.of("Locate a Stronghold"), List.of("diamond:16", "emerald:8"), List.of(91)));
        
        QUESTS.put(93, new Quest(93, "End Portal", "Activate the End Portal", 5,
            List.of("Place 12 Eyes of Ender"), List.of("diamond:32", "ender_pearl:32"), List.of(92)));
        
        QUESTS.put(94, new Quest(94, "Enter The End", "Step into The End", 5,
            List.of("Enter The End dimension"), List.of("diamond:32", "golden_apple:4"), List.of(93)));
        
        QUESTS.put(95, new Quest(95, "Enderman Slayer", "Defeat 10 Endermen", 5,
            List.of("Kill 10 Endermen"), List.of("ender_pearl:32", "diamond:16"), List.of(94)));
        
        QUESTS.put(96, new Quest(96, "Dragon's Lair", "Reach the main End island", 5,
            List.of("Explore the End"), List.of("diamond:32", "emerald:16"), List.of(94)));
        
        QUESTS.put(97, new Quest(97, "End Crystal Destroyer", "Destroy an End Crystal", 5,
            List.of("Destroy 1 End Crystal"), List.of("diamond:16", "arrow:128"), List.of(96)));
        
        QUESTS.put(98, new Quest(98, "Dragon Fight Prep", "Destroy all End Crystals", 5,
            List.of("Destroy all 10 End Crystals"), List.of("diamond:32", "golden_apple:8"), List.of(97)));
        
        QUESTS.put(99, new Quest(99, "Dragon Slayer", "Defeat the Ender Dragon", 5,
            List.of("Kill the Ender Dragon"), List.of("diamond:64", "emerald:32", "dragon_egg:1"), List.of(98)));
        
        QUESTS.put(100, new Quest(100, "Gateway Finder", "Find an End Gateway", 5,
            List.of("Locate an End Gateway"), List.of("ender_pearl:64", "diamond:32"), List.of(99)));
        
        QUESTS.put(101, new Quest(101, "Outer End", "Reach the outer End islands", 5,
            List.of("Travel through gateway"), List.of("chorus_fruit:32", "diamond:32"), List.of(100)));
        
        QUESTS.put(102, new Quest(102, "End City Discovery", "Find an End City", 5,
            List.of("Locate an End City"), List.of("diamond:32", "emerald:16"), List.of(101)));
        
        QUESTS.put(103, new Quest(103, "Shulker Hunter", "Defeat a Shulker", 5,
            List.of("Kill a Shulker"), List.of("shulker_shell:2", "diamond:16"), List.of(102)));
        
        QUESTS.put(104, new Quest(104, "Shulker Box", "Craft a Shulker Box", 5,
            List.of("Craft 1 Shulker Box"), List.of("diamond:16", "emerald:8"), List.of(103)));
        
        QUESTS.put(105, new Quest(105, "End Ship", "Find an End Ship", 5,
            List.of("Locate an End Ship"), List.of("diamond:32", "emerald:16"), List.of(102)));
        
        QUESTS.put(106, new Quest(106, "Elytra", "Obtain Elytra wings", 5,
            List.of("Get Elytra from End Ship"), List.of("diamond:64", "emerald:32", "phantom_membrane:8"), List.of(105)));
        
        QUESTS.put(107, new Quest(107, "First Flight", "Fly with Elytra", 5,
            List.of("Use Elytra to glide"), List.of("firework_rocket:64", "diamond:32"), List.of(106)));
        
        QUESTS.put(108, new Quest(108, "Dragon's Breath", "Collect Dragon's Breath", 5,
            List.of("Collect Dragon's Breath"), List.of("dragon_breath:8", "diamond:32"), List.of(99)));
        
        QUESTS.put(109, new Quest(109, "Chorus Harvest", "Harvest 64 Chorus Fruit", 5,
            List.of("Collect 64 Chorus Fruit"), List.of("chorus_fruit:32", "diamond:16"), List.of(101)));
        
        QUESTS.put(110, new Quest(110, "Purpur Builder", "Craft 64 Purpur Blocks", 5,
            List.of("Craft 64 Purpur Blocks"), List.of("diamond:16", "emerald:8"), List.of(109)));
        
        QUESTS.put(111, new Quest(111, "Ender Chest", "Craft an Ender Chest", 5,
            List.of("Craft 1 Ender Chest"), List.of("ender_pearl:16", "diamond:8"), List.of(95)));
        
        QUESTS.put(112, new Quest(112, "Respawn Dragon", "Respawn the Ender Dragon", 5,
            List.of("Use 4 End Crystals"), List.of("diamond:32", "end_crystal:4"), List.of(99)));
        
        QUESTS.put(113, new Quest(113, "Dragon Slayer II", "Defeat Dragon again", 5,
            List.of("Kill respawned Dragon"), List.of("diamond:64", "emerald:32", "dragon_head:1"), List.of(112)));
        
        QUESTS.put(114, new Quest(114, "Enderman Farm", "Build an Enderman farm", 5,
            List.of("Create Enderman XP farm"), List.of("diamond:32", "emerald:16"), List.of(95)));
        
        QUESTS.put(115, new Quest(115, "Pearl Collector", "Collect 128 Ender Pearls", 5,
            List.of("Get 128 Ender Pearls"), List.of("diamond:32", "emerald:16"), List.of(114)));
        
        QUESTS.put(116, new Quest(116, "Levitation", "Get hit by Shulker projectile", 5,
            List.of("Experience Levitation effect"), List.of("slow_falling_potion:4", "diamond:16"), List.of(103)));
        
        QUESTS.put(117, new Quest(117, "Sky Limit", "Reach Y=256 with Elytra", 5,
            List.of("Fly to build limit"), List.of("firework_rocket:64", "diamond:32"), List.of(107)));
        
        QUESTS.put(118, new Quest(118, "Long Distance", "Fly 1000 blocks with Elytra", 5,
            List.of("Travel 1000 blocks flying"), List.of("diamond:64", "emerald:32"), List.of(107)));
        
        QUESTS.put(119, new Quest(119, "Shulker Collection", "Craft 10 Shulker Boxes", 5,
            List.of("Craft 10 Shulker Boxes"), List.of("diamond:32", "emerald:16"), List.of(104)));
        
        QUESTS.put(120, new Quest(120, "End Master", "Complete all End challenges", 5,
            List.of("Complete Chapter 5"), List.of("elytra:1", "diamond:128", "emerald:64", "nether_star:2"), List.of(113, 118, 119)));

        // Chapter 6: Ultimate Challenges (Quests 121-150) - Advanced gameplay
        QUESTS.put(121, new Quest(121, "Wither Summoner", "Summon the Wither", 6,
            List.of("Place Wither Skeleton Skulls"), List.of("diamond:64", "emerald:32"), List.of(70)));
        
        QUESTS.put(122, new Quest(122, "Wither Slayer", "Defeat the Wither", 6,
            List.of("Kill the Wither"), List.of("nether_star:2", "diamond:128", "emerald:64"), List.of(121)));
        
        QUESTS.put(123, new Quest(123, "Beacon Activation", "Activate a Beacon", 6,
            List.of("Power up a Beacon"), List.of("diamond:64", "emerald:32"), List.of(86, 122)));
        
        QUESTS.put(124, new Quest(124, "Max Beacon", "Create max level Beacon", 6,
            List.of("Build level 4 Beacon pyramid"), List.of("diamond:128", "emerald:64", "nether_star:4"), List.of(123)));
        
        QUESTS.put(125, new Quest(125, "Trident Discovery", "Obtain a Trident", 6,
            List.of("Get Trident from Drowned"), List.of("diamond:32", "emerald:16"), List.of(49)));
        
        QUESTS.put(126, new Quest(126, "Conduit Power", "Craft a Conduit", 6,
            List.of("Craft 1 Conduit"), List.of("heart_of_the_sea:1", "diamond:32"), List.of(125)));
        
        QUESTS.put(127, new Quest(127, "Ocean Monument", "Find an Ocean Monument", 6,
            List.of("Locate Ocean Monument"), List.of("diamond:32", "sponge:8"), List.of(49)));
        
        QUESTS.put(128, new Quest(128, "Elder Guardian", "Defeat an Elder Guardian", 6,
            List.of("Kill Elder Guardian"), List.of("diamond:64", "emerald:32", "sponge:16"), List.of(127)));
        
        QUESTS.put(129, new Quest(129, "Raid Trigger", "Trigger a Raid", 6,
            List.of("Get Bad Omen effect"), List.of("diamond:32", "emerald:16"), List.of(60)));
        
        QUESTS.put(130, new Quest(130, "Raid Victor", "Win a Raid", 6,
            List.of("Defeat a Raid"), List.of("diamond:64", "emerald:32", "totem_of_undying:1"), List.of(129)));
        
        QUESTS.put(131, new Quest(131, "Totem of Undying", "Use a Totem of Undying", 6,
            List.of("Activate Totem"), List.of("totem_of_undying:2", "diamond:64"), List.of(130)));
        
        QUESTS.put(132, new Quest(132, "Hero of Village", "Get Hero of Village", 6,
            List.of("Win Raid near village"), List.of("diamond:64", "emerald:64"), List.of(130)));
        
        QUESTS.put(133, new Quest(133, "Trade Master", "Trade with all villager types", 6,
            List.of("Trade with 13 professions"), List.of("diamond:64", "emerald:128"), List.of(132)));
        
        QUESTS.put(134, new Quest(134, "Mending Book", "Obtain Mending enchantment", 6,
            List.of("Get Mending book"), List.of("diamond:64", "emerald:64"), List.of(133)));
        
        QUESTS.put(135, new Quest(135, "Full Enchant", "Fully enchant armor set", 6,
            List.of("Max enchant all armor"), List.of("diamond:128", "emerald:64"), List.of(134, 82)));
        
        QUESTS.put(136, new Quest(136, "Treasure Hunter", "Find buried treasure", 6,
            List.of("Use treasure map"), List.of("heart_of_the_sea:1", "diamond:32"), List.of(49)));
        
        QUESTS.put(137, new Quest(137, "Cartographer", "Fill a map completely", 6,
            List.of("Create filled map"), List.of("map:8", "diamond:16"), List.of(49)));
        
        QUESTS.put(138, new Quest(138, "Name Tag", "Use a Name Tag on a mob", 6,
            List.of("Name a mob"), List.of("name_tag:4", "diamond:32"), List.of(49)));
        
        QUESTS.put(139, new Quest(139, "Mob Head Collection", "Collect 5 different mob heads", 6,
            List.of("Get 5 unique mob heads"), List.of("diamond:64", "emerald:32"), List.of(70, 113)));
        
        QUESTS.put(140, new Quest(140, "Music Disc", "Collect a Music Disc", 6,
            List.of("Get any music disc"), List.of("music_disc_cat:1", "diamond:32"), List.of(59)));
        
        QUESTS.put(141, new Quest(141, "Disc Collector", "Collect 5 Music Discs", 6,
            List.of("Get 5 unique discs"), List.of("diamond:64", "emerald:32"), List.of(140)));
        
        QUESTS.put(142, new Quest(142, "Jukebox", "Craft and use a Jukebox", 6,
            List.of("Play music"), List.of("music_disc_13:1", "diamond:32"), List.of(141)));
        
        QUESTS.put(143, new Quest(143, "Iron Golem", "Create an Iron Golem", 6,
            List.of("Build Iron Golem"), List.of("iron_ingot:64", "diamond:32"), List.of(37)));
        
        QUESTS.put(144, new Quest(144, "Snow Golem", "Create a Snow Golem", 6,
            List.of("Build Snow Golem"), List.of("snowball:64", "diamond:16"), List.of(49)));
        
        QUESTS.put(145, new Quest(145, "Flower Power", "Collect all flower types", 6,
            List.of("Get 15 unique flowers"), List.of("diamond:32", "emerald:16"), List.of(49)));
        
        QUESTS.put(146, new Quest(146, "Dye Master", "Collect all 16 dye colors", 6,
            List.of("Get all dye types"), List.of("diamond:32", "emerald:16"), List.of(145)));
        
        QUESTS.put(147, new Quest(147, "Wood Collector", "Collect all wood types", 6,
            List.of("Get all 9 wood types"), List.of("diamond:32", "emerald:16"), List.of(34)));
        
        QUESTS.put(148, new Quest(148, "Stone Variety", "Collect all stone types", 6,
            List.of("Get 10 stone variants"), List.of("diamond:32", "emerald:16"), List.of(35)));
        
        QUESTS.put(149, new Quest(149, "Ore Collection", "Collect all ore types", 6,
            List.of("Get 10 unique ores"), List.of("diamond:64", "emerald:32"), List.of(77, 42, 50)));
        
        QUESTS.put(150, new Quest(150, "Ultimate Champion", "Complete all ultimate challenges", 6,
            List.of("Complete Chapter 6"), List.of("diamond_block:64", "emerald_block:32", "nether_star:8"), List.of(135, 141, 149)));

        VoxelScape.LOGGER.info("Created {} default quests", QUESTS.size());
    }

    private static void saveQuests(Path questFile) {
        try {
            Files.createDirectories(questFile.getParent());
            try (Writer writer = Files.newBufferedWriter(questFile)) {
                List<Quest> questList = new ArrayList<>(QUESTS.values());
                questList.sort(Comparator.comparingInt(Quest::getId));
                GSON.toJson(questList, writer);
            }
            VoxelScape.LOGGER.info("Saved quests to {}", questFile);
        } catch (IOException e) {
            VoxelScape.LOGGER.error("Failed to save quests", e);
        }
    }

    public static Quest getQuest(int id) {
        return QUESTS.get(id);
    }

    public static Collection<Quest> getAllQuests() {
        return QUESTS.values();
    }

    public static int getQuestCount() {
        return QUESTS.size();
    }

    public static List<Quest> getQuestsByChapter(int chapter) {
        return QUESTS.values().stream()
            .filter(q -> q.getChapter() == chapter)
            .sorted(Comparator.comparingInt(Quest::getId))
            .toList();
    }
    
    public static List<Integer> getAllQuestIds() {
        return new ArrayList<>(QUESTS.keySet());
    }
}
