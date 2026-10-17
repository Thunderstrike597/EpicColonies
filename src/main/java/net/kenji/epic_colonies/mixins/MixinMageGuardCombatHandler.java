package net.kenji.epic_colonies.mixins;

import com.example.minecoloniesmages.event.MageGuardCombatHandler;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.yukami.efiscompat.animation.Animation;
import net.kenji.epic_colonies.EpicColonies;
import net.kenji.epic_colonies.gameasset.patch.CitizenEntityPatch;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import org.jline.utils.Log;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;

@Mixin(value = MageGuardCombatHandler.class, remap = false)
public class MixinMageGuardCombatHandler {

    @Inject(method = "tryCastAtTarget", at = @At("RETURN"))
    private static void tryPlayCastingAnimation(LivingEntity caster, LivingEntity target, CallbackInfoReturnable<Boolean> cir){
        if(cir.getReturnValue() != null && !cir.getReturnValue()) return;

        if(EpicColonies.isLoaded("efiscompat")) {

            if (caster instanceof AbstractEntityCitizen citizen) {
                CitizenEntityPatch<?> patch = EpicFightCapabilities.getEntityPatch(citizen, CitizenEntityPatch.class);
                if (patch == null)
                    return;

                patch.playAnimationSynchronized(Animation.CASTING_ONE_HAND_TOP, 0.05F);
                patch.playAnimation(Animation.CASTING_ONE_HAND_TOP, 0.05F);

                Log.info("Logging Cast!");
            }
        }
    }
}
