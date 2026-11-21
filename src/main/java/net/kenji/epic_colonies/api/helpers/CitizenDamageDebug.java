package net.kenji.epic_colonies.api.helpers;

import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.kenji.epic_colonies.EpicColonies;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;

import java.util.stream.Collectors;

/**
 * DEV ONLY - logs every hit a MineColonies citizen deals, so you can check the weapon's damage is actually applied.
 *
 * Off by default. Turn it on with the JVM argument:  -Depic_colonies.debugDamage=true
 * (in build.gradle: runs { client { jvmArgs '-Depic_colonies.debugDamage=true' } }, or in your launcher's JVM args)
 *
 * Each hit prints three stages:
 *   [1 raw]   amount straight from Mob#doHurtTarget = ATTACK_DAMAGE attribute (+ enchantment bonus).
 *             This is the number the weapon fix affects.
 *   [2 hurt]  amount after Epic Fight's hurtEvent and other mods' LivingHurtEvent listeners (still before armor).
 *   [3 final] amount actually removed from the target's health, after armor / resistance / absorption.
 *
 * What to look for: with no fix, [1 raw] is ~2.0 (Epic Fight's default base) whatever the citizen holds.
 * With the fix, it should be roughly 2.0 + the weapon's attack damage (iron sword ~8, netherite sword ~10).
 *
 * Off-hand strikes: Epic Fight swaps the two hand items for the duration of an off-hand hit, so on those lines
 * "main=" shows the OFF-HAND weapon. Test dual wielding with two different weapons to tell the strikes apart.
 * [2 hurt] / [1 raw] is the attack animation's own damage multiplier.
 *
 * Add -Depic_colonies.debugDamagePlayers=true to also log players, to compare a player using the same weapons
 * and animation against a citizen. (Player [1 raw] includes the player's base damage of 1 instead of 2.)
 */
@Mod.EventBusSubscriber(modid = EpicColonies.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CitizenDamageDebug {

    private static final boolean ENABLED = Boolean.getBoolean("epic_colonies.debugDamage");
    private static final boolean LOG_PLAYERS = Boolean.getBoolean("epic_colonies.debugDamagePlayers");

    private CitizenDamageDebug() {}

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onHurtRaw(LivingHurtEvent event) {
        LivingEntity citizen = loggedAttacker(event.getSource());
        if (citizen == null) return;

        AttributeInstance attackDamage = citizen.getAttribute(Attributes.ATTACK_DAMAGE);
        String attributeInfo = attackDamage == null
                ? "ATTACK_DAMAGE attribute MISSING"
                : String.format("ATTACK_DAMAGE base=%.2f total=%.2f modifiers=[%s]",
                attackDamage.getBaseValue(), attackDamage.getValue(),
                attackDamage.getModifiers().stream()
                        .map(CitizenDamageDebug::describe)
                        .collect(Collectors.joining(", ")));

        EpicColonies.LOGGER.info("[CitizenDamage] {} -> {} | [1 raw] {} | main={} off={} | {} | {}",
                citizen.getName().getString(),
                event.getEntity().getName().getString(),
                format(event.getAmount()),
                citizen.getMainHandItem(),
                citizen.getOffhandItem(),
                describeSource(event.getSource()),
                attributeInfo);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onHurtAfterModifiers(LivingHurtEvent event) {
        if (loggedAttacker(event.getSource()) == null) return;
        EpicColonies.LOGGER.info("[CitizenDamage]     [2 hurt]  {} (cancelled={})",
                format(event.getAmount()), event.isCanceled());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDamageFinal(LivingDamageEvent event) {
        if (loggedAttacker(event.getSource()) == null) return;
        EpicColonies.LOGGER.info("[CitizenDamage]     [3 final] {} (target health {} -> ~{})",
                format(event.getAmount()),
                format(event.getEntity().getHealth()),
                format(Math.max(0, event.getEntity().getHealth() - event.getAmount())));
    }

    private static LivingEntity loggedAttacker(DamageSource source) {
        if (!ENABLED) return null;
        if (source.getEntity() instanceof LivingEntity attacker && !attacker.level().isClientSide()) {
            if (attacker instanceof AbstractEntityCitizen) return attacker;
            if (LOG_PLAYERS && attacker instanceof Player) return attacker;
        }
        return null;
    }

    private static String describeSource(DamageSource source) {
        if (source instanceof EpicFightDamageSource efm) {
            String anim = efm.getAnimation() != null ? efm.getAnimation().registryName().toString() : "none";
            return "source=EpicFight anim=" + anim;
        }
        // Anything else means the hit came from somewhere other than Epic Fight's attack animation
        // (e.g. MineColonies' own guard AI, projectiles, thorns).
        return "source=" + source.getMsgId() + " (" + source.getClass().getSimpleName() + ")";
    }

    private static String describe(AttributeModifier modifier) {
        return String.format("%s %+.2f %s", modifier.getName(), modifier.getAmount(), modifier.getOperation());
    }

    private static String format(float value) {
        return String.format("%.2f", value);
    }
}