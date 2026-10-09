package net.kenji.epic_colonies.api;

import net.minecraft.core.BlockPos;

public interface IDoMiningHeartbeat {
    long epicColonies$getLastDoMiningTick();
    BlockPos epicColonies_Versions$getMiningBlock();
}