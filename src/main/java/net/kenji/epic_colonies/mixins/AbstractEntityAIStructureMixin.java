package net.kenji.epic_colonies.mixins;

import com.ldtteam.structurize.placement.AbstractBlueprintIterator;
import com.ldtteam.structurize.placement.BlockPlacementResult;
import com.ldtteam.structurize.placement.StructurePhasePlacementResult;
import com.ldtteam.structurize.placement.StructurePlacer;
import com.ldtteam.structurize.util.ChangeStorage;
import com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.core.colony.buildings.AbstractBuilding;
import com.minecolonies.core.colony.jobs.AbstractJob;
import com.minecolonies.core.entity.ai.workers.AbstractEntityAIStructure;
import net.kenji.epic_colonies.EpicColoniesConfigCommon;
import net.kenji.epic_colonies.gameasset.EpicColoniesAnimations;
import net.kenji.epic_colonies.gameasset.patch.CitizenEntityPatch;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jline.utils.Log;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;

import java.util.function.Supplier;

@Mixin(value = AbstractEntityAIStructure.class, remap = false)
public abstract class AbstractEntityAIStructureMixin<J extends AbstractJob<?, J>, B extends AbstractBuilding> {

    @Shadow
    protected BlockPos blockToMine;

    @Shadow
    public abstract AbstractEntityCitizen getWorker();

    @Unique
    private int epicColonies$stuckMiningTicks = 0;
    @Unique private static final int epicColonies$MAX_STUCK_MINING_TICKS = 2; // tune to taste



    @Redirect(
            method = "structureStep",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/ldtteam/structurize/placement/StructurePlacer;executeStructureStep(Lnet/minecraft/world/level/Level;Lcom/ldtteam/structurize/util/ChangeStorage;Lnet/minecraft/core/BlockPos;Lcom/ldtteam/structurize/placement/StructurePlacer$Operation;Ljava/util/function/Supplier;Z)Lcom/ldtteam/structurize/placement/StructurePhasePlacementResult;"
            )
    )
    private StructurePhasePlacementResult epicColonies$buildSolid(
            StructurePlacer instance, Level world, ChangeStorage storage, BlockPos pos,
            StructurePlacer.Operation operation, Supplier<AbstractBlueprintIterator.Result> iterate,
            boolean includeEntities) {

        // run the original call
        StructurePhasePlacementResult result = instance.executeStructureStep(world, storage, pos, operation, iterate, includeEntities);

        if (result != null && result.getBlockResult() != null) {
            BlockPlacementResult.Result r = result.getBlockResult().getResult();
            if (r == BlockPlacementResult.Result.SUCCESS || r == BlockPlacementResult.Result.FINISHED) {
                CitizenEntityPatch<?> patch = EpicFightCapabilities.getEntityPatch(this.getWorker(), CitizenEntityPatch.class);
                if (patch != null) {
                    Log.info("Logginge FINAL PLACE");
                    patch.playAnimationSynchronized(EpicColoniesAnimations.CITIZEN_USE, 0.1F);
                }
            }
        }
        return result; // must return it so the original code keeps working
    }

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