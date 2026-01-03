package com.voxelscape.quest;

import java.util.ArrayList;
import java.util.List;

public class Quest {
    private final int id;
    private final String name;
    private final String description;
    private final int chapter;
    private final List<String> requirements;
    private final List<String> rewards;
    private final List<Integer> dependencies;

    public Quest(int id, String name, String description, int chapter, 
                 List<String> requirements, List<String> rewards, List<Integer> dependencies) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.chapter = chapter;
        this.requirements = requirements != null ? requirements : new ArrayList<>();
        this.rewards = rewards != null ? rewards : new ArrayList<>();
        this.dependencies = dependencies != null ? dependencies : new ArrayList<>();
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getChapter() { return chapter; }
    public List<String> getRequirements() { return requirements; }
    public List<String> getRewards() { return rewards; }
    public List<Integer> getDependencies() { return dependencies; }
}
