package net.kenji.epic_colonies.gameasset.ai.goal;

import yesman.epicfight.world.capabilities.entitypatch.MobPatch;
import yesman.epicfight.world.entity.ai.goal.AnimatedAttackGoal;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors;

import java.util.function.BooleanSupplier;

/**
 * Epic Fight's AnimatedAttackGoal only looks at Mob#getTarget(), so it keeps swinging even while
 * MineColonies' worker AI wants the guard to retreat (GUARD_FLEE / GUARD_REGEN keep the target set).
 * This version stands down while {@code suppressed} is true.
 *
 * Still an AnimatedAttackGoal, so MobPatch#selectGoalToRemove keeps removing it on initAI().
 */
public class GatedAnimatedAttackGoal<T extends MobPatch<?>> extends AnimatedAttackGoal<T> {
    private final BooleanSupplier suppressed;

    public GatedAnimatedAttackGoal(T mobpatch, CombatBehaviors<T> combatBehaviors, BooleanSupplier suppressed) {
        super(mobpatch, combatBehaviors);
        this.suppressed = suppressed;
    }

    // AnimatedAttackGoal doesn't override canContinueToUse(), so Goal's default (-> canUse()) covers both.
    @Override
    public boolean canUse() {
        return !this.suppressed.getAsBoolean() && super.canUse();
    }
}
