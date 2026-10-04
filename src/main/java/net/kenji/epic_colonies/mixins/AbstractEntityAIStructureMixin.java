package net.kenji.epic_colonies.mixins;

import com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.core.colony.buildings.AbstractBuilding;
import com.minecolonies.core.colony.jobs.AbstractJob;
import com.minecolonies.core.entity.ai.workers.AbstractEntityAIStructure;
import net.kenji.epic_colonies.EpicColoniesConfigCommon;
import net.minecraft.core.BlockPos;
import org.jline.utils.Log;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = AbstractEntityAIStructure.class, remap = false)
public abstract class AbstractEntityAIStructureMixin<J extends AbstractJob<?, J>, B extends AbstractBuilding> {

    @Shadow
    protected BlockPos blockToMine;

    @Unique
    private int epicColonies$stuckMiningTicks = 0;

    @Inject(method = "doMining", at = @At("HEAD"), cancellable = true)
    private void epicColonies$detectStuckMining(CallbackInfoReturnable<IAIState> cir) {
        if (this.blockToMine == null) {
            epicColonies$stuckMiningTicks = 0;
            return;
        }

        epicColonies$stuckMiningTicks++;
       // Log.info("[epicColonies] doMining stuck-check, tick=" + epicColonies$stuckMiningTicks + " blockToMine=" + this.blockToMine);

        if (epicColonies$stuckMiningTicks > EpicColoniesConfigCommon.MINE_COUNTER.get()) { // lowered for testing
            epicColonies$stuckMiningTicks = 0;
            this.blockToMine = null;
            cir.setReturnValue(AIWorkerState.BUILDING_STEP);
        }
    }
}