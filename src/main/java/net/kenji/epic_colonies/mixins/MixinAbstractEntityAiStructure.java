package net.kenji.epic_colonies.mixins;

import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.core.colony.buildings.AbstractBuilding;
import com.minecolonies.core.colony.jobs.AbstractJob;
import com.minecolonies.core.entity.ai.workers.AbstractEntityAIInteract;
import com.minecolonies.core.entity.ai.workers.AbstractEntityAISkill;
import net.kenji.epic_colonies.gameasset.patch.CitizenEntityPatch;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;

@Mixin(value = AbstractEntityAIInteract.class, remap = false)
public class MixinAbstractEntityAiStructure<J extends AbstractJob<?, J>, B extends AbstractBuilding> extends AbstractEntityAISkill<J, B> {
    @Unique
    private IJob epicColonies_Versions$accesssedJob;

    protected MixinAbstractEntityAiStructure(@NotNull J job) {
        super(job);
        this.epicColonies_Versions$accesssedJob = job;
    }

    @Inject(method = "checkMiningLocation", at = @At("RETURN"))
    public void onCheckMiningLocation(BlockPos blockToMine, BlockPos safeStand, CallbackInfoReturnable<Boolean> cir){

        if(epicColonies_Versions$accesssedJob != null && epicColonies_Versions$accesssedJob.getCitizen() != null) {
            if (epicColonies_Versions$accesssedJob.getCitizen().getEntity().isPresent()) {
                CitizenEntityPatch<?> patch = EpicFightCapabilities.getEntityPatch(epicColonies_Versions$accesssedJob.getCitizen().getEntity().get(), CitizenEntityPatch.class);
                if(patch == null)return;
                if (cir.getReturnValue() == null) return;

                boolean original = cir.getReturnValue();

                if (!original) {
                    patch.resetMotion = LivingMotions.DIGGING;
                }

            }
        }
    }

    @Override
    public Class<B> getExpectedBuildingClass() {
        return null;
    }
}
