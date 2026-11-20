package net.kenji.epic_colonies.api.animation_types;

import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.model.Armature;

public class BlinkAnimation extends StaticAnimation {

    /**
     * How often the mob blinks after playing this blink.
     *
     * @param minIntervalTicks  shortest normal gap between blinks (20 ticks = 1 second)
     * @param maxIntervalTicks  longest gap (a long stare). Gaps lean toward the short end,
     *                          averaging about 30% of the way from min to max.
     * @param doubleBlinkChance 0..1 chance the next blink comes almost right away
     */
    public record Timing(int minIntervalTicks, int maxIntervalTicks, float doubleBlinkChance) {
        /** ~2s to 12s between blinks, averaging ~5s, 15% double blinks. */
        public static final Timing DEFAULT = new Timing(40, 240, 0.15F);

        public Timing {
            minIntervalTicks = Math.max(0, minIntervalTicks);
            maxIntervalTicks = Math.max(minIntervalTicks, maxIntervalTicks);
            doubleBlinkChance = Math.max(0.0F, Math.min(1.0F, doubleBlinkChance));
        }
    }

    private final float weight;   // how likely this blink is picked vs the others in the list
    private final Timing timing;

    public BlinkAnimation(float weight, Timing timing, float transitionTime, boolean isRepeat,
                          AnimationManager.AnimationAccessor<? extends StaticAnimation> accessor,
                          AssetAccessor<? extends Armature> armature) {
        super(transitionTime, isRepeat, accessor, armature);
        this.weight = weight;
        this.timing = timing;
    }

    public BlinkAnimation(float weight, float transitionTime, boolean isRepeat,
                          AnimationManager.AnimationAccessor<? extends StaticAnimation> accessor,
                          AssetAccessor<? extends Armature> armature) {
        this(weight, Timing.DEFAULT, transitionTime, isRepeat, accessor, armature);
    }

    /** Old signature, kept so existing registrations still compile. cooldownTime and motion are unused. */
    @Deprecated
    public BlinkAnimation(int weight, double cooldownTime, LivingMotion motion, float transitionTime, boolean isRepeat,
                          AnimationManager.AnimationAccessor<? extends StaticAnimation> accessor,
                          AssetAccessor<? extends Armature> armature) {
        this((float) weight, Timing.DEFAULT, transitionTime, isRepeat, accessor, armature);
    }

    public float getWeight() {
        return weight;
    }

    public Timing getTiming() {
        return timing;
    }
}