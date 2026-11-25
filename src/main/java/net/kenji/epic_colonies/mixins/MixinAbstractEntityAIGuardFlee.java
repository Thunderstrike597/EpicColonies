package net.kenji.epic_colonies.mixins;

import com.minecolonies.api.entity.ai.combat.CombatAIStates;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.core.entity.ai.workers.AbstractAISkeleton;
import com.minecolonies.core.entity.ai.workers.guard.AbstractEntityAIGuard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * MineColonies only runs AbstractEntityAIGuard#shouldFlee() every 40 ticks (GUARD_REGEN_INTERVAL), and
 * EntityCitizen#handleDamagePerformed caps each hit at 20% of max health. So a guard sits below the 20% flee
 * threshold for roughly one hit, and with Epic Fight's faster attack combos that hit usually lands before
 * the next 40-tick check. They die without ever fleeing.
 *
 * This adds the same check (MineColonies' own shouldFlee: setting, health, distance, research) every tick
 * while ATTACKING or NO_TARGET. Nothing else changes: flee/regen still behave like vanilla MineColonies.
 */
@Mixin(value = AbstractEntityAIGuard.class, remap = false)
public abstract class MixinAbstractEntityAIGuardFlee {

    @Shadow
    private boolean shouldFlee() {
        throw new AssertionError();
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void epicColonies$addFastFleeCheck(CallbackInfo ci) {
        var stateAI = ((AbstractAISkeleton<?>) (Object) this).getStateAI();
        stateAI.addTransition(new AITarget<IAIState>(CombatAIStates.ATTACKING, () -> this.epicColonies$checkFlee(), () -> AIWorkerState.GUARD_FLEE, 1));
        stateAI.addTransition(new AITarget<IAIState>(CombatAIStates.NO_TARGET, () -> this.epicColonies$checkFlee(), () -> AIWorkerState.GUARD_FLEE, 1));

    }

    @Unique
    private boolean epicColonies$checkFlee() {
        boolean result = this.shouldFlee();
        return result;
    }
}
