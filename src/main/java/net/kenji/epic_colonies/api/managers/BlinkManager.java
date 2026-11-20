package net.kenji.epic_colonies.api.managers;

import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.kenji.epic_colonies.EpicColoniesConfigClient;
import net.kenji.epic_colonies.api.animation_types.BlinkAnimation;
import net.kenji.epic_colonies.gameasset.patch.base.AbstractExpressiveHumanoidPatch;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Client-side blinking.
 *
 * Blinks are purely cosmetic, so they never touch the server: every client rolls its own
 * random timings and plays the blink locally. No packets, no sync, and it doesn't matter
 * that two players see a wolf blink at slightly different moments.
 *
 * Timing model (the "natural" part):
 *  - Each blink is followed by a random gap between minInterval and maxInterval.
 *  - The gap is NOT evenly random. It's skewed so most gaps are on the short side and the
 *    occasional one is long, which is how real blinking looks (lots of normal blinks,
 *    sometimes a long stare).
 *  - Sometimes a blink is followed almost immediately by a second one (a double blink).
 *  - The very first wait is fully random, so a pack of mobs that loads in together
 *    doesn't blink in sync.
 */
public class BlinkManager {
    private static final float TRANSITION_TIME = 0.05F;

    // Client-side only. Weak keys: when the client drops the entity, its state goes with it,
    // so there's nothing to clean up on death/unload.
    private static final Map<LivingEntity, BlinkState> STATES = new WeakHashMap<>();

    private static class BlinkState {
        int ticksUntilNextBlink = -1; // -1 = needs its first wait rolled
    }

    public static void manageBlinking(LivingEntity entityLiving) {
        if(EpicColoniesConfigClient.DISABLE_BLINKING.get())return;

        if (!entityLiving.level().isClientSide()) return;
        if(!(entityLiving instanceof AbstractEntityCitizen citizen))return;

        AbstractExpressiveHumanoidPatch<?> patch = EpicFightCapabilities.getEntityPatch(citizen, AbstractExpressiveHumanoidPatch.class);
        if (patch == null) return;

        List<AnimationManager.AnimationAccessor<? extends StaticAnimation>> blinks = patch.getBlinkAnimations();
        if (blinks == null || blinks.isEmpty()) return;

        BlinkState state = STATES.computeIfAbsent(entityLiving, e -> new BlinkState());
        RandomSource random = entityLiving.getRandom();

        // Dead or sleeping mobs don't blink. Reset so they get a fresh random start later.
        if (!entityLiving.isAlive() || entityLiving.isSleeping()) {
            state.ticksUntilNextBlink = -1;
            return;
        }

        // First time we see this entity: random start anywhere in the normal range
        if (state.ticksUntilNextBlink < 0) {
            BlinkAnimation.Timing timing = timingOf(blinks.get(0));
            state.ticksUntilNextBlink = Mth.nextInt(random, 0, timing.maxIntervalTicks());
            return;
        }

        if (state.ticksUntilNextBlink > 0) {
            state.ticksUntilNextBlink--;
            return;
        }

        // Time to blink
        AnimationManager.AnimationAccessor<? extends StaticAnimation> chosen = weightedRandom(blinks, random);
        patch.playAnimationInClientSide(chosen, TRANSITION_TIME);

        // Schedule the next one: let this blink finish first, then wait the gap
        BlinkAnimation.Timing timing = timingOf(chosen);
        int blinkLength = Math.max(1, Mth.ceil(chosen.get().getTotalTime() * 20.0F));

        int gap;
        if (random.nextFloat() < timing.doubleBlinkChance()) {
            gap = Mth.nextInt(random, 2, 5);            // quick second blink
        } else {
            gap = rollNaturalGap(random, timing.minIntervalTicks(), timing.maxIntervalTicks());
        }

        state.ticksUntilNextBlink = blinkLength + gap;
    }

    // ------------------------------------------------------------------------------------

    /**
     * Picks a wait between min and max that leans toward the short end.
     *
     * Step 1: average two random numbers (0..1). That gives a value that's usually near
     *         the middle and rarely near 0 or 1 (a "bell" shape instead of flat).
     * Step 2: square it. Squaring a number between 0 and 1 makes it smaller, and pulls
     *         small numbers down more than big ones, so the bell slides toward 0.
     *
     * Result: about 4 out of 5 gaps land in the shorter half of the range and
     * the rest are the occasional long stare. The average lands around 30% of the way
     * from min to max (e.g. min 40, max 240 -> average ~5 seconds).
     */
    private static int rollNaturalGap(RandomSource random, int min, int max) {
        if (max <= min) return min;
        float r = (random.nextFloat() + random.nextFloat()) * 0.5F;
        r = r * r;
        return min + Math.round(r * (max - min));
    }

    /**
     * Weighted random: a blink with weight 3 is picked 3x as often as one with weight 1.
     *
     * Picture all the weights laid end to end on a line. Throw a dart at a random spot on
     * that line; whichever animation's section it lands in wins. Bigger weight = bigger
     * section = more likely to be hit.
     *
     * Non-BlinkAnimation entries count as weight 1.
     */
    private static AnimationManager.AnimationAccessor<? extends StaticAnimation> weightedRandom(
            List<AnimationManager.AnimationAccessor<? extends StaticAnimation>> blinks, RandomSource random) {
        if (blinks.size() == 1) return blinks.get(0);

        float total = 0.0F;
        for (AnimationManager.AnimationAccessor<? extends StaticAnimation> blink : blinks) {
            total += weightOf(blink);
        }

        if (total <= 0.0F) {
            return blinks.get(random.nextInt(blinks.size()));
        }

        float dart = random.nextFloat() * total;
        for (AnimationManager.AnimationAccessor<? extends StaticAnimation> blink : blinks) {
            dart -= weightOf(blink);
            if (dart <= 0.0F) return blink;
        }
        return blinks.get(blinks.size() - 1); // float rounding safety
    }

    private static float weightOf(AnimationManager.AnimationAccessor<? extends StaticAnimation> accessor) {
        return accessor.get() instanceof BlinkAnimation blink ? Math.max(0.0F, blink.getWeight()) : 1.0F;
    }

    /** Timing settings for an entry; plain StaticAnimations fall back to the defaults. */
    private static BlinkAnimation.Timing timingOf(AnimationManager.AnimationAccessor<? extends StaticAnimation> accessor) {
        return accessor.get() instanceof BlinkAnimation blink ? blink.getTiming() : BlinkAnimation.Timing.DEFAULT;
    }
}