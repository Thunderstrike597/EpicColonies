package net.kenji.epic_colonies.api.handlers;

import net.kenji.epic_colonies.gameasset.EpicColoniesWeaponCategory;
import net.kenji.epic_colonies.gameasset.patch.base.AbstractExpressiveHumanoidPatch;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.gameasset.Animations;

import yesman.epicfight.particle.HitParticleType;
import yesman.epicfight.registry.entries.EpicFightParticles;
import yesman.epicfight.registry.entries.EpicFightSounds;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.CapabilityItem.Styles;
import yesman.epicfight.world.capabilities.item.CapabilityItem.WeaponCategories;
import yesman.epicfight.world.capabilities.item.WeaponCategory;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.damagesource.EpicFightDamageTypeTags;

/**
 * Server-side guard state for one patch. Mirrors Epic Fight's player GuardSkill, but driven by AI instead of a key:
 * <ul>
 *   <li>{@link #tryBlock} is called from the patch's {@code tryHurt} override. Returning BLOCKED makes Epic Fight's
 *       LivingAttackEvent handler cancel the hit – exactly how the player guard skill works.</li>
 *   <li>The hold pose comes for free: weapon capabilities already map {@code LivingMotions.BLOCK} to their guard pose and
 *       shields map {@code BLOCK_SHIELD} to {@code BIPED_BLOCK}. We only have to set the composite motion
 *       ({@link #getCompositeMotion()}).</li>
 *   <li>Guard points stand in for the player's stamina: each block costs points (scaled by the hit's impact),
 *       running out = guard break.</li>
 * </ul>
 */
public class CitizenGuardHandler {

    public enum GuardType { NONE, WEAPON, SHIELD }

    // ---- Tuning (move to EpicColoniesConfigCommon once it feels right) -------------------------------------
    public static float MAX_GUARD_POINTS        = 10.0F;
    public static float WEAPON_COST_MULTIPLIER  = 1.0F;   // cost = (BASE + impact) * multiplier
    public static float SHIELD_COST_MULTIPLIER  = 0.6F;
    public static float BASE_BLOCK_COST         = 1.0F;
    public static int   REGEN_DELAY_TICKS       = 40;     // ticks after the last block before points regen
    public static float REGEN_PER_TICK          = 0.1F;
    public static int   GUARD_BREAK_COOLDOWN    = 80;     // ticks before the citizen may guard again after a break
    public static int   SHIELD_DISABLE_COOLDOWN = 100;    // vanilla axe-vs-shield disable time
    public static float REACTIVE_GUARD_CHANCE   = 0.35F;  // chance to raise guard after taking an unguarded hit

    private final AbstractExpressiveHumanoidPatch<?> patch;

    private GuardType activeType = GuardType.NONE;
    private int guardTicks;
    private int queuedGuardTicks;
    private int cooldown;
    private float guardPoints = MAX_GUARD_POINTS;
    private int lastBlockTick = -1000;

    public CitizenGuardHandler(AbstractExpressiveHumanoidPatch<?> patch) {
        this.patch = patch;
    }

    // =========================================================================================================
    //  State
    // =========================================================================================================

    public boolean isGuarding() {
        return this.activeType != GuardType.NONE;
    }

    public GuardType getActiveType() {
        return this.activeType;
    }

    public float getGuardPoints() {
        return this.guardPoints;
    }

    /** Composite motion to show while guarding, or null when not guarding. */
    public LivingMotion getCompositeMotion() {
        return switch (this.activeType) {
            case SHIELD -> LivingMotions.BLOCK_SHIELD;
            case WEAPON -> LivingMotions.BLOCK;
            case NONE -> null;
        };
    }

    /** What the citizen could guard with right now (shield in offhand wins over a guard-capable weapon). */
    public GuardType getAvailableType() {
        PathfinderMob mob = this.patch.getOriginal();
        ItemStack offhand = mob.getOffhandItem();
        CapabilityItem offhandCap = this.patch.getHoldingItemCapability(InteractionHand.OFF_HAND);

        if (offhand.getItem() instanceof ShieldItem || offhandCap.getWeaponCategory() == WeaponCategories.SHIELD) {
            return GuardType.SHIELD;
        }

        // Generic: a weapon whose current style has a BLOCK living motion reports UseAnim.BLOCK.
        // Covers Epic Fight presets, datapack weapons and modded weapon capabilities alike.
        CapabilityItem mainCap = this.patch.getHoldingItemCapability(InteractionHand.MAIN_HAND);
        if (mainCap.getUseAnimation(this.patch) == UseAnim.BLOCK) {
            return GuardType.WEAPON;
        }

        return GuardType.NONE;
    }

    public boolean canStartGuard() {
        return this.cooldown <= 0
                && this.getAvailableType() != GuardType.NONE
                && !this.patch.getEntityState().attacking()
                && !this.patch.getEntityState().hurt();
    }

    public boolean startGuard(int ticks) {
        if (!this.canStartGuard()) return false;
        this.activeType = this.getAvailableType();
        this.guardTicks = Math.max(this.guardTicks, ticks);
        return true;
    }

    /** Start guarding as soon as the citizen is able to (e.g. once a hit-stun ends). */
    public void queueGuard(int ticks) {
        this.queuedGuardTicks = Math.max(this.queuedGuardTicks, ticks);
    }

    public void stopGuard() {
        this.activeType = GuardType.NONE;
        this.guardTicks = 0;
    }

    // =========================================================================================================
    //  Tick (server)
    // =========================================================================================================

    public void serverTick() {
        PathfinderMob mob = this.patch.getOriginal();

        if (this.cooldown > 0) this.cooldown--;

        if (mob.tickCount - this.lastBlockTick > REGEN_DELAY_TICKS && this.guardPoints < MAX_GUARD_POINTS) {
            this.guardPoints = Math.min(MAX_GUARD_POINTS, this.guardPoints + REGEN_PER_TICK);
        }

        if (this.queuedGuardTicks > 0 && !this.isGuarding() && this.canStartGuard()) {
            this.startGuard(this.queuedGuardTicks);
            this.queuedGuardTicks = 0;
        }

        if (!this.isGuarding()) return;

        boolean lostWeapon = this.getAvailableType() != this.activeType;
        boolean busy = this.patch.getEntityState().attacking() || this.patch.getEntityState().hurt() || this.patch.getEntityState().knockDown();

        if (--this.guardTicks <= 0 || this.patch.getTarget() == null || lostWeapon || busy) {
            this.stopGuard();
        }
    }

    // =========================================================================================================
    //  Blocking (server) – call from the patch's tryHurt
    // =========================================================================================================



    /**
     * @return BLOCKED result if the hit was guarded, or null to let normal damage handling continue.
     */
    public AttackResult tryBlock(DamageSource source, float amount) {
        if (!this.isGuarding()) {
            this.maybeReactiveGuard(source);
            return null;
        }

        Entity offender = getOffender(source);
        if (offender == null || !this.isBlockableSource(source) || !this.isInFront(source)) {
            return null;
        }

        PathfinderMob self = this.patch.getOriginal();
        boolean shield = this.activeType == GuardType.SHIELD;

        float impact = 0.5F;
        float knockback = 0.25F;

        if (source instanceof EpicFightDamageSource efSource) {
            if (efSource.is(EpicFightDamageTypeTags.GUARD_PUNCTURE)) {
                return null;
            }
            impact = efSource.calculateImpact();
            knockback += Math.min(impact * 0.1F, 1.0F);
        }

        if (offender instanceof LivingEntity livingOffender) {
            knockback += EnchantmentHelper.modifyKnockback((ServerLevel)livingOffender.level(), livingOffender.getItemInHand(livingOffender.getUsedItemHand()), livingOffender, source, knockback);
        }

        float cost = (BASE_BLOCK_COST + impact) * (shield ? SHIELD_COST_MULTIPLIER : WEAPON_COST_MULTIPLIER);
        boolean axeDisablesShield = shield && offender instanceof LivingEntity attacker
                && attacker.getMainHandItem().canDisableShield(self.getOffhandItem(), self, attacker);

        this.guardPoints -= cost;
        this.lastBlockTick = self.tickCount;
        this.patch.knockBackEntity(offender.position(), knockback);

        boolean guardBreak = this.guardPoints <= 0.0F || axeDisablesShield;

        if (guardBreak) {
            this.onGuardBreak(axeDisablesShield);
        } else {
            this.playBlockFeedback(offender, shield);
        }

        this.notifyBlocked(source, amount);
        return AttackResult.blocked(amount);
    }

    private void playBlockFeedback(Entity offender, boolean shield) {
        if (shield) {
            this.patch.playSound(SoundEvents.SHIELD_BLOCK, -0.05F, 0.1F);
        } else {
            this.patch.playSound(EpicFightSounds.CLASH.get(), -0.05F, 0.1F);
            if (this.patch.getOriginal().level() instanceof ServerLevel serverLevel) {
                EpicFightParticles.HIT_BLUNT.get().spawnParticleWithArgument(serverLevel, HitParticleType.FRONT_OF_EYES, HitParticleType.ZERO, this.patch.getOriginal(), offender);
            }
        }

        AnimationManager.AnimationAccessor<? extends StaticAnimation> hit = this.getGuardHitMotion();
        if (hit != null) {
            this.patch.playAnimationSynchronized(hit, 0.0F);
        }
    }

    private void onGuardBreak(boolean byAxe) {
        this.stopGuard();
        this.guardPoints = 0.0F;
        this.cooldown = byAxe ? SHIELD_DISABLE_COOLDOWN : GUARD_BREAK_COOLDOWN;

        if (byAxe) {
            this.patch.playSound(SoundEvents.SHIELD_BREAK, -0.05F, 0.1F);
        }
        this.patch.playSound(EpicFightSounds.NEUTRALIZE_MOBS.get(), 3.0F, 0.0F, 0.1F);
        this.patch.playAnimationSynchronized(this.getGuardBreakMotion(), 0.0F);
    }

    /** Same bookkeeping as GuardSkill#dealEvent. */
    private void notifyBlocked(DamageSource source, float amount) {
        this.patch.countHurtTime(amount);

        EpicFightCapabilities.getUnparameterizedEntityPatch(source.getEntity(), LivingEntityPatch.class)
                .ifPresent(attacker -> attacker.setLastAttackEntity(this.patch.getOriginal()));

        LivingEntityPatch<?> directPatch = EpicFightCapabilities.getEntityPatch(source.getDirectEntity(), LivingEntityPatch.class);
        if (directPatch != null) {
            directPatch.onAttackBlocked(source, this.patch);
        }
    }

    private void maybeReactiveGuard(DamageSource source) {
        if (this.getAvailableType() == GuardType.NONE || this.cooldown > 0) return;
        if (getOffender(source) == null || !this.isBlockableSource(source)) return;

        if (this.patch.getOriginal().getRandom().nextFloat() < REACTIVE_GUARD_CHANCE) {
            this.queueGuard(30 + this.patch.getOriginal().getRandom().nextInt(30));
        }
    }

    // =========================================================================================================
    //  Rules
    // =========================================================================================================

    protected boolean isBlockableSource(DamageSource source) {
        boolean common = !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)
                && !source.is(EpicFightDamageTypeTags.UNBLOCKALBE)
                && !source.is(DamageTypeTags.BYPASSES_ARMOR)
                && !source.is(DamageTypeTags.IS_EXPLOSION)
                && !source.is(DamageTypes.MAGIC)
                && !source.is(DamageTypeTags.IS_FIRE);

        // Shields stop arrows like vanilla; weapon guards don't (same as the player GuardSkill)
        boolean projectileOk = this.activeType == GuardType.SHIELD || !source.is(DamageTypeTags.IS_PROJECTILE);
        return common && projectileOk;
    }

    protected boolean isInFront(DamageSource source) {
        Vec3 sourcePos = source.getSourcePosition();
        if (sourcePos == null) return false;

        PathfinderMob self = this.patch.getOriginal();
        Vec3 view = self.getViewVector(1.0F);
        view = view.subtract(0, view.y, 0).normalize();
        Vec3 toSource = sourcePos.subtract(self.position()).normalize();

        return toSource.dot(view) > 0.0D;
    }

    public static Entity getOffender(DamageSource source) {
        return source.getDirectEntity() == null ? source.getEntity() : source.getDirectEntity();
    }

    // =========================================================================================================
    //  Animations – same table as GuardSkill.createGuardBuilder(), plus shields and Epic Colonies categories
    // =========================================================================================================

    protected AnimationManager.AnimationAccessor<? extends StaticAnimation> getGuardHitMotion() {
        if (this.activeType == GuardType.SHIELD) {
            return Animations.BIPED_HIT_SHIELD;
        }

        CapabilityItem cap = this.patch.getHoldingItemCapability(InteractionHand.MAIN_HAND);
        WeaponCategory category = cap.getWeaponCategory();

        if (category == WeaponCategories.GREATSWORD) return Animations.GREATSWORD_GUARD_HIT;
        if (category == WeaponCategories.UCHIGATANA) return Animations.UCHIGATANA_GUARD_HIT;
        if (category == WeaponCategories.LONGSWORD || category == WeaponCategories.TACHI) return Animations.LONGSWORD_GUARD_HIT;
        if (category == WeaponCategories.SPEAR) return Animations.SPEAR_GUARD_HIT;
        if (category == WeaponCategories.AXE) return Animations.SWORD_GUARD_HIT;
        if (category == EpicColoniesWeaponCategory.DUAL_SWORDS) return Animations.SWORD_DUAL_GUARD_HIT;
        if (category == WeaponCategories.SWORD) {
            return cap.getStyle(this.patch) == Styles.TWO_HAND ? Animations.SWORD_DUAL_GUARD_HIT : Animations.SWORD_GUARD_HIT;
        }

        // Modded/datapack weapon with a BLOCK pose but no known hit reaction
        return Animations.LONGSWORD_GUARD_HIT;
    }

    protected AnimationManager.AnimationAccessor<? extends StaticAnimation> getGuardBreakMotion() {
        CapabilityItem cap = this.patch.getHoldingItemCapability(InteractionHand.MAIN_HAND);
        if (this.activeType == GuardType.WEAPON && cap.getWeaponCategory() == WeaponCategories.GREATSWORD) {
            return Animations.GREATSWORD_GUARD_BREAK;
        }
        return Animations.BIPED_COMMON_NEUTRALIZED;
    }
}