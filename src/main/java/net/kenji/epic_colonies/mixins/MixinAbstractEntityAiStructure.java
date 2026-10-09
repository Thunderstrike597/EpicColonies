package net.kenji.epic_colonies.mixins;

import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.entity.ai.ITickingStateAI;
import com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.ai.statemachine.states.IState;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.core.colony.buildings.AbstractBuilding;
import com.minecolonies.core.colony.buildings.AbstractBuildingStructureBuilder;
import com.minecolonies.core.colony.jobs.AbstractJob;
import com.minecolonies.core.colony.jobs.JobBuilder;
import com.minecolonies.core.entity.ai.workers.*;
import net.kenji.epic_colonies.EpicColoniesConfigCommon;
import net.kenji.epic_colonies.api.IDoMiningHeartbeat;
import net.kenji.epic_colonies.gameasset.EpicColoniesLivingMotions;
import net.kenji.epic_colonies.gameasset.patch.CitizenEntityPatch;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jline.utils.Log;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;

@Mixin(value = AbstractEntityAIStructure.class, remap = false)
public abstract class MixinAbstractEntityAiStructure implements IDoMiningHeartbeat {

    @Shadow public abstract AbstractEntityCitizen getWorker();

    @Shadow
    protected BlockPos blockToMine;
    @Unique
    private long epicColonies$lastDoMiningTick = Long.MIN_VALUE;

    @Inject(method = "doMining", at = @At("HEAD"))
    private void epicColonies$markDoMiningHeartbeat(CallbackInfoReturnable<IAIState> cir) {
        epicColonies$lastDoMiningTick = getWorker().level().getGameTime();
    }

    @Override
    public long epicColonies$getLastDoMiningTick() {
        return epicColonies$lastDoMiningTick;
    }

    @Override
    public BlockPos epicColonies_Versions$getMiningBlock() {
        return this.blockToMine;
    }
}
