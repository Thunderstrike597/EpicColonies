package net.kenji.epic_colonies.mixins;

import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.core.entity.ai.workers.AbstractEntityAIStructure;
import net.kenji.epic_colonies.api.IDoMiningHeartbeat;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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
