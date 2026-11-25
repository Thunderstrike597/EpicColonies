package net.kenji.epic_colonies.api.helpers;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.research.util.ResearchConstants;
import com.minecolonies.core.colony.buildings.AbstractBuildingGuards;
import net.kenji.epic_colonies.EpicColonies;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * DEV ONLY - logs each condition of MineColonies' AbstractEntityAIGuard#shouldFlee() so you can see which one
 * is stopping the retreat.
 *
 * On with EITHER JVM argument:  -Depic_colonies.debugRetreat=true  or  -Depic_colonies.debugDamage=true
 *
 * Logs:
 *   [retreat] ...        every time a guard takes damage
 *   [retreat-check] ...  from MixinAbstractEntityAIGuardFlee, every 10 ticks while a guard is under 25% health
 *                        (if these never appear, the mixin isn't applied)
 *
 * shouldFlee() (MineColonies 1.1.1160) needs ALL of:
 *   - hut setting "retreat" on (default on)
 *   - health < (int) maxHealth * 0.2
 *   - distanceSqr to hut centre > 20  (~4.5 blocks)
 *   - research effect minecolonies:effects/retreatunlock > 0  (Combat research "Regeneration")
 */
@Mod.EventBusSubscriber(modid = EpicColonies.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class GuardRetreatDebug {

    public static final boolean ENABLED =
            Boolean.getBoolean("epic_colonies.debugRetreat") || Boolean.getBoolean("epic_colonies.debugDamage");

    private GuardRetreatDebug() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onGuardDamaged(LivingDamageEvent event) {
        if (!ENABLED || event.isCanceled()) return;
        if (!(event.getEntity() instanceof AbstractEntityCitizen citizen) || citizen.level().isClientSide()) return;
        if (citizen.getCitizenJobHandler() == null || citizen.getCitizenJobHandler().getColonyJob() == null
                || !citizen.getCitizenJobHandler().getColonyJob().isGuard()) return;

        EpicColonies.LOGGER.info("[retreat] {} took {} | {}",
                citizen.getName().getString(),
                String.format("%.2f", event.getAmount()),
                describe(citizen, citizen.getHealth() - event.getAmount()));
    }

    /** Called from MixinAbstractEntityAIGuardFlee with the result of MineColonies' own shouldFlee(). */
    public static void logCheck(AbstractEntityCitizen citizen, boolean shouldFlee) {
        if (!ENABLED || citizen.getHealth() >= citizen.getMaxHealth() * 0.25F) return;
        if (citizen.level().getGameTime() % 10 != 0) return;

        EpicColonies.LOGGER.info("[retreat-check] {} shouldFlee={} | {}",
                citizen.getName().getString(), shouldFlee, describe(citizen, citizen.getHealth()));
    }

    private static String describe(AbstractEntityCitizen citizen, float health) {
        double threshold = ((int) citizen.getMaxHealth()) * 0.2D;

        IJob<?> job = citizen.getCitizenJobHandler() == null ? null : citizen.getCitizenJobHandler().getColonyJob();
        Object state = job == null || job.getWorkerAI() == null ? "no worker AI" : job.getWorkerAI().getState();

        ICitizenData data = citizen.getCitizenData();
        IBuilding building = data == null ? null : data.getWorkBuilding();
        String buildingInfo;
        if (building instanceof AbstractBuildingGuards guards) {
            double distSqr = citizen.distanceToSqr(building.getID().getCenter());
            buildingInfo = String.format("setting=%s | far=%s (distSqr %.1f)",
                    guards.shallRetrieveOnLowHealth(), distSqr > 20, distSqr);
        } else {
            buildingInfo = "building=" + (building == null ? "null" : building.getClass().getName()) + " (NOT AbstractBuildingGuards)";
        }

        IColony colony = citizen.getCitizenColonyHandler().getColonyOrRegister();
        double research = colony == null ? -1
                : colony.getResearchManager().getResearchEffects().getEffectStrength(ResearchConstants.RETREAT);

        return String.format("hp %.2f/%.1f | lowHealth=%s (< %.1f) | %s | research=%s (%.1f) | state=%s",
                health, citizen.getMaxHealth(), health < threshold, threshold, buildingInfo, research > 0, research, state);
    }
}
