package com.voxelscape;

import com.voxelscape.commands.CollectionCommand;
import com.voxelscape.commands.PerksCommand;
import com.voxelscape.commands.QuestCommand;
import com.voxelscape.commands.QuestsCommand;
import com.voxelscape.data.QuestDataManager;
import com.voxelscape.events.CollectionTracker;
import com.voxelscape.events.PerkEffectHandler;
import com.voxelscape.events.PlayerJoinHandler;
import com.voxelscape.events.QuestTracker;
import com.voxelscape.items.HeroJournal;
import com.voxelscape.quest.QuestManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VoxelScape implements ModInitializer {
    public static final String MOD_ID = "voxelscape";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    
    // Register Hero's Journal item - using direct registry
    public static final Item HERO_JOURNAL = new HeroJournal(new Item.Properties().stacksTo(1));

    @Override
    public void onInitialize() {
        LOGGER.info("Voxel Scape initializing...");
        
        // Register the Hero's Journal item
        Registry.register(BuiltInRegistries.ITEM, MOD_ID + ":hero_journal", HERO_JOURNAL);

        // Initialize data manager for persistent storage
        QuestDataManager.initialize();

        // Load quests from JSON
        QuestManager.loadQuests();

        // Register event handlers
        PlayerJoinHandler.register();
        QuestTracker.register();
        PerkEffectHandler.register();
        CollectionTracker.register();

        // Register commands
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            QuestsCommand.register(dispatcher);
            QuestCommand.register(dispatcher);
            PerksCommand.register(dispatcher);
            CollectionCommand.register(dispatcher);
        });

        LOGGER.info("Voxel Scape loaded with {} quests!", QuestManager.getQuestCount());
    }
}
