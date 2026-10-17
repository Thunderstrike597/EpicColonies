package net.kenji.epic_colonies.mixins;

import com.asanginxst.epicfightx.gameassets.EpicFightSkillsX;
import com.example.minecoloniesmages.combat.MageCastingController;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.yukami.efiscompat.animation.Animation;
import net.kenji.epic_colonies.EpicColonies;
import net.kenji.epic_colonies.gameasset.patch.CitizenEntityPatch;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.jline.utils.Log;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;

@Mixin(value = MageCastingController.class, remap = false)
public class MixinMageCastingController {

    @Inject(method = "tryCastAtTarget", at = @At("RETURN"))
    private static void tryPlayCastingAnimation(Mob caster, LivingEntity target, CallbackInfoReturnable<Boolean> cir){
        if(cir.getReturnValue() == null) return;
        if(EpicColonies.isLoaded("efiscompat")) {

            boolean isSuccessful = cir.getReturnValue();

            if (caster instanceof AbstractEntityCitizen citizen) {
                CitizenEntityPatch<?> patch = EpicFightCapabilities.getEntityPatch(citizen, CitizenEntityPatch.class);
                if (patch == null)
                    return;

                if (isSuccessful) {
                    patch.playAnimationSynchronized(Animation.CASTING_ONE_HAND_TOP, 0F);
                }
            }
        }
    }
}
