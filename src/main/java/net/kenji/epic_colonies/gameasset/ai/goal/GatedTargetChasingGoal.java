package net.kenji.epic_colonies.gameasset.ai.goal;

import net.minecraft.world.entity.PathfinderMob;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;
import yesman.epicfight.world.entity.ai.goal.TargetChasingGoal;

import java.util.function.BooleanSupplier;

/**
 * Epic Fight's TargetChasingGoal (a MeleeAttackGoal) re-paths to the target every few ticks, which
 * overwrites the path MineColonies sets when a guard retreats to its building. This version stops
 * chasing while {@code suppressed} is true, leaving the navigator to MineColonies.
 *
 * Still a TargetChasingGoal, so MobPatch#selectGoalToRemove keeps removing it on initAI().
 */
public class GatedTargetChasingGoal extends TargetChasingGoal {
    private final BooleanSupplier suppressed;

    public GatedTargetChasingGoal(MobPatch<? extends PathfinderMob> mobpatch, PathfinderMob mob, double speedModifier,
                                  boolean longMemory, BooleanSupplier suppressed) {
        super(mobpatch, mob, speedModifier, longMemory);
        this.suppressed = suppressed;
    }


    @Override
    public boolean canUse() {
        return !this.suppressed.getAsBoolean() && super.canUse();
    }

    // MeleeAttackGoal overrides canContinueToUse() separately, so gate it too.
    @Override
    public boolean canContinueToUse() {
        return !this.suppressed.getAsBoolean() && super.canContinueToUse();
    }
}
