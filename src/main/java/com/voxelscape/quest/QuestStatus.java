package com.voxelscape.quest;

public enum QuestStatus {
    LOCKED,      // Red - Dependencies not met
    AVAILABLE,   // Red - Can start but not started
    IN_PROGRESS, // Yellow - Started but not complete
    COMPLETED    // Green - Done
}
