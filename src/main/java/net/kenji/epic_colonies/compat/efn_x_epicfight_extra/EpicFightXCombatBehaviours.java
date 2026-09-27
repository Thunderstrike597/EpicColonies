package net.kenji.epic_colonies.compat.efn_x_epicfight_extra;



import com.asanginxst.epicfightx.gameassets.animations.AnimationsX;
import com.hm.efn.gameasset.animations.*;
import net.kenji.epic_colonies.compat.CombatBehaviourBase;
import net.kenji.epic_colonies.compat.CompatMobCombatBehaviours;
import net.kenji.epic_colonies.gameasset.EpicColoniesWeaponCategory;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.world.capabilities.entitypatch.HumanoidMobPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors;

public class EpicFightXCombatBehaviours extends CombatBehaviourBase {

    static CombatBehaviors.Builder<HumanoidMobPatch<?>> swordBehaviour;
    static CombatBehaviors.Builder<HumanoidMobPatch<?>> swordDualBehaviour;

    static CombatBehaviors.Builder<HumanoidMobPatch<?>> daggerBehaviour;
    static CombatBehaviors.Builder<HumanoidMobPatch<?>> daggerDualBehaviour;

    static CombatBehaviors.Builder<HumanoidMobPatch<?>> tachiBehaviour;

    static CombatBehaviors.Builder<HumanoidMobPatch<?>> longswordSBehaviour;
    static CombatBehaviors.Builder<HumanoidMobPatch<?>> greatswordSBehaviour;

    static CombatBehaviors.Builder<HumanoidMobPatch<?>> spearOneHandSBehaviour;
    static CombatBehaviors.Builder<HumanoidMobPatch<?>> spearTwoHandSBehaviour;

    static CombatBehaviors.Builder<HumanoidMobPatch<?>> katanaSBehaviour;

    @SuppressWarnings("unchecked")
    public static void init(){
     buildMotions();

        CompatMobCombatBehaviours.HUMANOID_S_SWORD = register(
                CapabilityItem.WeaponCategories.SWORD,
                CompatMobCombatBehaviours.motion(CapabilityItem.Styles.ONE_HAND, swordBehaviour, Animations.BIPED_HOLD_LONGSWORD, Animations.BIPED_WALK, Animations.BIPED_RUN)
        );
        CompatMobCombatBehaviours.HUMANOID_S_SWORD_DUAL = register(
                EpicColoniesWeaponCategory.DUAL_SWORDS,
                CompatMobCombatBehaviours.motion(CapabilityItem.Styles.TWO_HAND, swordDualBehaviour, Animations.BIPED_HOLD_DUAL_WEAPON, Animations.BIPED_HOLD_DUAL_WEAPON, Animations.BIPED_RUN_DUAL)
        );
        CompatMobCombatBehaviours.HUMANOID_S_DAGGER = register(
                CapabilityItem.WeaponCategories.DAGGER,
                CompatMobCombatBehaviours.motion(CapabilityItem.Styles.ONE_HAND, daggerBehaviour, Animations.BIPED_IDLE, Animations.BIPED_WALK, Animations.BIPED_HOLD_SPEAR, Animations.BIPED_RUN)
                );
        CompatMobCombatBehaviours.HUMANOID_S_DAGGER_DUAL = register(
                EpicColoniesWeaponCategory.DUAL_DAGGER,
                CompatMobCombatBehaviours.motion(CapabilityItem.Styles.TWO_HAND, daggerDualBehaviour, Animations.BIPED_HOLD_DUAL_WEAPON, Animations.BIPED_HOLD_DUAL_WEAPON, Animations.BIPED_RUN_DUAL)
        );
        CompatMobCombatBehaviours.HUMANOID_S_TACHI = register(
                CapabilityItem.WeaponCategories.TACHI,
                CompatMobCombatBehaviours.motion(CapabilityItem.Styles.TWO_HAND, tachiBehaviour, Animations.BIPED_HOLD_TACHI, Animations.BIPED_HOLD_TACHI, Animations.BIPED_RUN_SPEAR)
        );
        CompatMobCombatBehaviours.HUMANOID_S_LONGSWORD = register(
                CapabilityItem.WeaponCategories.LONGSWORD,
                CompatMobCombatBehaviours.motion(CapabilityItem.Styles.TWO_HAND, longswordSBehaviour, Animations.BIPED_HOLD_LONGSWORD, Animations.BIPED_WALK_LONGSWORD, Animations.BIPED_HOLD_SPEAR, Animations.BIPED_RUN_LONGSWORD)
        );
        CompatMobCombatBehaviours.HUMANOID_S_SPEAR = register(
                CapabilityItem.WeaponCategories.SPEAR,
                CompatMobCombatBehaviours.motion(CapabilityItem.Styles.ONE_HAND, spearOneHandSBehaviour, Animations.BIPED_HOLD_SPEAR, Animations.BIPED_HOLD_SPEAR, Animations.BIPED_RUN_SPEAR),
                CompatMobCombatBehaviours.motion(CapabilityItem.Styles.TWO_HAND, spearTwoHandSBehaviour, Animations.BIPED_HOLD_SPEAR, Animations.BIPED_HOLD_SPEAR, Animations.BIPED_RUN_SPEAR)

        );
        CompatMobCombatBehaviours.HUMANOID_S_GREATSWORD = register(
                CapabilityItem.WeaponCategories.GREATSWORD,
                CompatMobCombatBehaviours.motion(CapabilityItem.Styles.TWO_HAND, greatswordSBehaviour, Animations.BIPED_HOLD_GREATSWORD, Animations.BIPED_WALK_GREATSWORD, Animations.BIPED_RUN_GREATSWORD)
        );
        CompatMobCombatBehaviours.HUMANOID_UCHIGATANA = register(
                CapabilityItem.WeaponCategories.UCHIGATANA,
                motion(CapabilityItem.Styles.TWO_HAND, katanaSBehaviour, AnimationsX.BIPED_HOLD_UCHIGATANA, AnimationsX.BIPED_HOLD_UCHIGATANA, AnimationsX.BIPED_RUN_UCHIGATANA)
        );
    }
    @SuppressWarnings("unchecked")
    private static void buildMotions(){
        swordBehaviour = CombatBehaviors.builder().newBehaviorSeries(
                createBehaviourSeries(15, DynamicBehaviour.of(EFNSwordAnimations.NF_SWORD_AUTO1).distanceMinMax(1, 3).build())
        ).newBehaviorSeries(
                createBehaviourSeries(15, DynamicBehaviour.of(EFNSwordAnimations.NF_SWORD_AUTO2).distanceMinMax(1, 3).build())
        ).newBehaviorSeries(
                createBehaviourSeries(15, DynamicBehaviour.of(EFNSwordAnimations.NF_SWORD_AUTO3).distanceMinMax(1, 3).build())
        ).newBehaviorSeries(
                createBehaviourSeries(15, DynamicBehaviour.of(EFNSwordAnimations.NF_SWORD_AUTO4).distanceMinMax(1, 3).build())
        ).newBehaviorSeries(
                createBehaviourSeries(25, DynamicBehaviour.of(EFNSwordAnimations.NF_SWORD_AUTO1).distanceMinMax(1, 3).build(), DynamicBehaviour.of(EFNSwordAnimations.NF_SWORD_AUTO2).distanceMinMax(1, 3).build())
        ).newBehaviorSeries(
                createBehaviourSeries(21, DynamicBehaviour.of(EFNSwordAnimations.NF_SWORD_AUTO1).distanceMinMax(1, 3).build(), DynamicBehaviour.of(EFNSwordAnimations.NF_SWORD_AUTO2).distanceMinMax(1, 3).build(), DynamicBehaviour.of(EFNSwordAnimations.NF_SWORD_AUTO3).distanceMinMax(1, 3).build())
        ).newBehaviorSeries(
                createBehaviourSeries(20, DynamicBehaviour.of(EFNSwordAnimations.NF_SWORD_AUTO1).distanceMinMax(1, 3).build(), DynamicBehaviour.of(EFNSwordAnimations.NF_SWORD_AUTO2).distanceMinMax(1, 3).build(), DynamicBehaviour.of(EFNSwordAnimations.NF_SWORD_AUTO3).distanceMinMax(1, 3).build(), DynamicBehaviour.of(EFNSwordAnimations.NF_SWORD_AUTO4).distanceMinMax(1, 3).build())
        ).newBehaviorSeries(
                createBehaviourSeries(45, DynamicBehaviour.of(Animations.BIPED_ROLL_BACKWARD).distanceMinMax(0, 1.5F).build())
        ).newBehaviorSeries(
                createBehaviourSeries(45, DynamicBehaviour.of(Animations.BIPED_STEP_LEFT).distanceMinMax(0, 1.20F).build())
        );
        swordDualBehaviour = CombatBehaviors.builder().newBehaviorSeries(
                createBehaviourSeries(15, DynamicBehaviour.of(EFNDualSwordAnimations.NF_DUAL_AUTO1).distanceMinMax(1, 3).build())
        ).newBehaviorSeries(
                createBehaviourSeries(15, DynamicBehaviour.of(EFNDualSwordAnimations.NF_DUAL_AUTO2).distanceMinMax(1, 3).build())
        ).newBehaviorSeries(
                createBehaviourSeries(15, DynamicBehaviour.of(EFNDualSwordAnimations.NF_DUAL_AUTO3).distanceMinMax(1, 3).build())
        ).newBehaviorSeries(
                createBehaviourSeries(15, DynamicBehaviour.of(EFNDualSwordAnimations.NF_DUAL_AUTO4).distanceMinMax(1, 3).build())
        ).newBehaviorSeries(
                createBehaviourSeries(25, DynamicBehaviour.of(EFNDualSwordAnimations.NF_DUAL_AUTO1).distanceMinMax(1, 3).build(), DynamicBehaviour.of(EFNDualSwordAnimations.NF_DUAL_AUTO2).distanceMinMax(1, 3).build())
        ).newBehaviorSeries(
                createBehaviourSeries(21, DynamicBehaviour.of(EFNDualSwordAnimations.NF_DUAL_AUTO1).distanceMinMax(1, 3).build(), DynamicBehaviour.of(EFNDualSwordAnimations.NF_DUAL_AUTO2).distanceMinMax(1, 3).build(), DynamicBehaviour.of(EFNDualSwordAnimations.NF_DUAL_AUTO3).distanceMinMax(1, 3).build())
        ).newBehaviorSeries(
                createBehaviourSeries(20, DynamicBehaviour.of(EFNDualSwordAnimations.NF_DUAL_AUTO1).distanceMinMax(1, 3).build(), DynamicBehaviour.of(EFNDualSwordAnimations.NF_DUAL_AUTO2).distanceMinMax(1, 3).build(), DynamicBehaviour.of(EFNDualSwordAnimations.NF_DUAL_AUTO3).distanceMinMax(1, 3).build(), DynamicBehaviour.of(EFNDualSwordAnimations.NF_DUAL_AUTO4).distanceMinMax(1, 3).build())
        ).newBehaviorSeries(
                createBehaviourSeries(45, DynamicBehaviour.of(Animations.BIPED_ROLL_BACKWARD).distanceMinMax(0, 1.5F).build())
        ).newBehaviorSeries(
                createBehaviourSeries(45, DynamicBehaviour.of(Animations.BIPED_STEP_LEFT).distanceMinMax(0, 1.20F).build())
        ).newBehaviorSeries(
                createBehaviourSeries(45, DynamicBehaviour.of(Animations.BIPED_STEP_RIGHT).distanceMinMax(0, 1.20F).build())
        );
        daggerBehaviour = CombatBehaviors.builder().newBehaviorSeries(
                createBehaviourSeries(15, DynamicBehaviour.of(EFNShortSwordAnimations.NF_SHORTSWORD_AUTO1).distanceMinMax(1, 2).build())
        ).newBehaviorSeries(
                createBehaviourSeries(15, DynamicBehaviour.of(EFNShortSwordAnimations.NF_SHORTSWORD_AUTO2).distanceMinMax(1, 2).build())
        ).newBehaviorSeries(
                createBehaviourSeries(15, DynamicBehaviour.of(EFNShortSwordAnimations.NF_SHORTSWORD_AUTO3).distanceMinMax(1, 2).build())
        ).newBehaviorSeries(
                createBehaviourSeries(15, DynamicBehaviour.of(EFNShortSwordAnimations.NF_SHORTSWORD_AUTO4).distanceMinMax(1, 2).build())
        ).newBehaviorSeries(
                createBehaviourSeries(25, DynamicBehaviour.of(EFNShortSwordAnimations.NF_SHORTSWORD_AUTO1).distanceMinMax(1, 2).build(), DynamicBehaviour.of(EFNShortSwordAnimations.NF_SHORTSWORD_AUTO2).distanceMinMax(1, 3).build())
        ).newBehaviorSeries(
                createBehaviourSeries(21, DynamicBehaviour.of(EFNShortSwordAnimations.NF_SHORTSWORD_AUTO1).distanceMinMax(1, 2).build(), DynamicBehaviour.of(EFNShortSwordAnimations.NF_SHORTSWORD_AUTO2).distanceMinMax(1, 3).build(), DynamicBehaviour.of(EFNShortSwordAnimations.NF_SHORTSWORD_AUTO3).distanceMinMax(1, 3).build())
        ).newBehaviorSeries(
                createBehaviourSeries(20, DynamicBehaviour.of(EFNShortSwordAnimations.NF_SHORTSWORD_AUTO1).distanceMinMax(1, 2).build(), DynamicBehaviour.of(EFNShortSwordAnimations.NF_SHORTSWORD_AUTO2).distanceMinMax(1, 3).build(), DynamicBehaviour.of(EFNShortSwordAnimations.NF_SHORTSWORD_AUTO3).distanceMinMax(1, 3).build(), DynamicBehaviour.of(EFNShortSwordAnimations.NF_SHORTSWORD_AUTO4).distanceMinMax(1, 3).build())
        ).newBehaviorSeries(
                createBehaviourSeries(20, DynamicBehaviour.of(EFNShortSwordAnimations.NF_SHORTSWORD_AUTO1).distanceMinMax(1, 2).build(), DynamicBehaviour.of(EFNShortSwordAnimations.NF_SHORTSWORD_AUTO2).distanceMinMax(1, 3).build(), DynamicBehaviour.of(EFNShortSwordAnimations.NF_SHORTSWORD_AUTO3).distanceMinMax(1, 3).build(), DynamicBehaviour.of(EFNShortSwordAnimations.NF_SHORTSWORD_AUTO4).distanceMinMax(1, 3).build(), DynamicBehaviour.of(EFNShortSwordAnimations.NF_SHORTSWORD_AUTO5).distanceMinMax(1, 3).build())
        ).newBehaviorSeries(
                createBehaviourSeries(45, DynamicBehaviour.of(Animations.BIPED_ROLL_BACKWARD).distanceMinMax(0, 1.5F).build())
        ).newBehaviorSeries(
                createBehaviourSeries(45, DynamicBehaviour.of(Animations.BIPED_STEP_LEFT).distanceMinMax(0, 1.20F).build())
        ).newBehaviorSeries(
                createBehaviourSeries(45, DynamicBehaviour.of(Animations.BIPED_STEP_RIGHT).distanceMinMax(0, 1.20F).build())
        );
        daggerDualBehaviour = CombatBehaviors.builder().newBehaviorSeries(
                createBehaviourSeries(15, DynamicBehaviour.of((AnimationManager.AnimationAccessor<? extends StaticAnimation>) AnimationsX.DAGGER_DUAL_AUTO1).distanceMinMax(1, 2).build())
        ).newBehaviorSeries(
                createBehaviourSeries(15, DynamicBehaviour.of((AnimationManager.AnimationAccessor<? extends StaticAnimation>) AnimationsX.DAGGER_DUAL_AUTO2).distanceMinMax(1, 2).build())
        ).newBehaviorSeries(
                createBehaviourSeries(15, DynamicBehaviour.of((AnimationManager.AnimationAccessor<? extends StaticAnimation>) AnimationsX.DAGGER_DUAL_AUTO3).distanceMinMax(1, 2).build())
        ).newBehaviorSeries(
                createBehaviourSeries(25, DynamicBehaviour.of((AnimationManager.AnimationAccessor<? extends StaticAnimation>) AnimationsX.DAGGER_DUAL_AUTO1).distanceMinMax(1, 2).build(), DynamicBehaviour.of((AnimationManager.AnimationAccessor<? extends StaticAnimation>) AnimationsX.DAGGER_DUAL_AUTO2).distanceMinMax(1, 3).build())
        ).newBehaviorSeries(
                createBehaviourSeries(21, DynamicBehaviour.of((AnimationManager.AnimationAccessor<? extends StaticAnimation>) AnimationsX.DAGGER_DUAL_AUTO1).distanceMinMax(1, 2).build(), DynamicBehaviour.of((AnimationManager.AnimationAccessor<? extends StaticAnimation>) AnimationsX.DAGGER_DUAL_AUTO2).distanceMinMax(1, 3).build(), DynamicBehaviour.of((AnimationManager.AnimationAccessor<? extends StaticAnimation>) AnimationsX.DAGGER_DUAL_AUTO3).distanceMinMax(1, 3).build())
        ).newBehaviorSeries(
                createBehaviourSeries(21, DynamicBehaviour.of((AnimationManager.AnimationAccessor<? extends StaticAnimation>) AnimationsX.DAGGER_DUAL_AUTO1).distanceMinMax(1, 2).build(), DynamicBehaviour.of((AnimationManager.AnimationAccessor<? extends StaticAnimation>) AnimationsX.DAGGER_DUAL_AUTO2).distanceMinMax(1, 3).build(), DynamicBehaviour.of((AnimationManager.AnimationAccessor<? extends StaticAnimation>) AnimationsX.DAGGER_DUAL_AUTO3).distanceMinMax(1, 3).build(), DynamicBehaviour.of((AnimationManager.AnimationAccessor<? extends StaticAnimation>) AnimationsX.DAGGER_DUAL_AUTO4).distanceMinMax(1, 3).build())
        ).newBehaviorSeries(
                createBehaviourSeries(45, DynamicBehaviour.of(Animations.BIPED_ROLL_BACKWARD).distanceMinMax(0, 1.5F).build())
        ).newBehaviorSeries(
                createBehaviourSeries(45, DynamicBehaviour.of(Animations.BIPED_STEP_LEFT).distanceMinMax(0, 1.20F).build())
        ).newBehaviorSeries(
                createBehaviourSeries(45, DynamicBehaviour.of(Animations.BIPED_STEP_RIGHT).distanceMinMax(0, 1.20F).build())
        );
        tachiBehaviour = CombatBehaviors.builder().newBehaviorSeries(
                createBehaviourSeries(15, DynamicBehaviour.of(EFNTachiAnimations.NF_TACHI_AUTO1).distanceMinMax(1, 3).build())
        ).newBehaviorSeries(
                createBehaviourSeries(15, DynamicBehaviour.of(EFNTachiAnimations.NF_TACHI_AUTO2).distanceMinMax(1, 3).build())
        ).newBehaviorSeries(
                createBehaviourSeries(15, DynamicBehaviour.of(EFNTachiAnimations.NF_TACHI_AUTO3).distanceMinMax(1, 3).build())
        ).newBehaviorSeries(
                createBehaviourSeries(25, DynamicBehaviour.of(EFNTachiAnimations.NF_TACHI_AUTO1).distanceMinMax(1, 3).build(), DynamicBehaviour.of(EFNTachiAnimations.NF_TACHI_AUTO2).distanceMinMax(1, 3).build())
        ).newBehaviorSeries(
                createBehaviourSeries(21, DynamicBehaviour.of(EFNTachiAnimations.NF_TACHI_AUTO1).distanceMinMax(1, 3).build(), DynamicBehaviour.of(EFNTachiAnimations.NF_TACHI_AUTO2).distanceMinMax(1, 3).build(), DynamicBehaviour.of(EFNTachiAnimations.NF_TACHI_AUTO3).distanceMinMax(1, 3).build())
        ).newBehaviorSeries(
                createBehaviourSeries(20, DynamicBehaviour.of(EFNTachiAnimations.NF_TACHI_AUTO1).distanceMinMax(1, 3).build(), DynamicBehaviour.of(EFNTachiAnimations.NF_TACHI_AUTO2).distanceMinMax(1, 3).build(), DynamicBehaviour.of(EFNTachiAnimations.NF_TACHI_AUTO3).distanceMinMax(1, 3).build(), DynamicBehaviour.of(EFNTachiAnimations.NF_TACHI_AUTO4).distanceMinMax(1, 3).build())
        ).newBehaviorSeries(
                createBehaviourSeries(20, DynamicBehaviour.of(EFNTachiAnimations.NF_TACHI_AUTO1).distanceMinMax(1, 3).build(), DynamicBehaviour.of(EFNTachiAnimations.NF_TACHI_AUTO2).distanceMinMax(1, 3).build(), DynamicBehaviour.of(EFNTachiAnimations.NF_TACHI_AUTO3).distanceMinMax(1, 3).build(), DynamicBehaviour.of(EFNTachiAnimations.NF_TACHI_AUTO4).distanceMinMax(1, 3).build(), DynamicBehaviour.of(EFNTachiAnimations.NF_TACHI_AUTO5).distanceMinMax(1, 3).build())
        ).newBehaviorSeries(
                createBehaviourSeries(45, DynamicBehaviour.of(Animations.BIPED_ROLL_BACKWARD).distanceMinMax(0, 1.5F).build())
        ).newBehaviorSeries(
                createBehaviourSeries(45, DynamicBehaviour.of(Animations.BIPED_STEP_LEFT).distanceMinMax(0, 1.20F).build())
        ).newBehaviorSeries(
                createBehaviourSeries(45, DynamicBehaviour.of(Animations.BIPED_STEP_RIGHT).distanceMinMax(0, 1.20F).build())
        );

        longswordSBehaviour = CombatBehaviors.builder().newBehaviorSeries(
                createBehaviourSeries(15, DynamicBehaviour.of(AnimationsX.LONGSWORD_AUTO1).distanceMinMax(1, 3).build())
        ).newBehaviorSeries(
                createBehaviourSeries(15, DynamicBehaviour.of(AnimationsX.LONGSWORD_AUTO2).distanceMinMax(1, 3).build())
        ).newBehaviorSeries(
                createBehaviourSeries(15, DynamicBehaviour.of(AnimationsX.LONGSWORD_AUTO3).distanceMinMax(1, 3).build())
        ).newBehaviorSeries(
                createBehaviourSeries(25, DynamicBehaviour.of(AnimationsX.LONGSWORD_AUTO1).distanceMinMax(1, 3).build(), DynamicBehaviour.of(AnimationsX.LONGSWORD_AUTO2).distanceMinMax(1, 3).build())
        ).newBehaviorSeries(
                createBehaviourSeries(21, DynamicBehaviour.of(AnimationsX.LONGSWORD_AUTO1).distanceMinMax(1, 3).build(), DynamicBehaviour.of(AnimationsX.LONGSWORD_AUTO2).distanceMinMax(1, 3).build(), DynamicBehaviour.of(AnimationsX.LONGSWORD_AUTO3).distanceMinMax(1, 3).build())
        ).newBehaviorSeries(
                createBehaviourSeries(45, DynamicBehaviour.of(Animations.BIPED_ROLL_BACKWARD).distanceMinMax(0, 1.5F).build())
        ).newBehaviorSeries(
                createBehaviourSeries(45, DynamicBehaviour.of(Animations.BIPED_STEP_LEFT).distanceMinMax(0, 1.20F).build())
        ).newBehaviorSeries(
                createBehaviourSeries(45, DynamicBehaviour.of(Animations.BIPED_STEP_RIGHT).distanceMinMax(0, 1.20F).build())
        );

        spearOneHandSBehaviour = CombatBehaviors.builder().newBehaviorSeries(
                createBehaviourSeries(15, DynamicBehaviour.of(AnimationsX.SPEAR_ONEHAND_AUTO).distanceMinMax(1, 3).build())
        ).newBehaviorSeries(
                createBehaviourSeries(45, DynamicBehaviour.of(Animations.BIPED_ROLL_BACKWARD).distanceMinMax(0, 1.5F).build())
        ).newBehaviorSeries(
                createBehaviourSeries(45, DynamicBehaviour.of(Animations.BIPED_STEP_LEFT).distanceMinMax(0, 1.20F).build())
        ).newBehaviorSeries(
                createBehaviourSeries(45, DynamicBehaviour.of(Animations.BIPED_STEP_RIGHT).distanceMinMax(0, 1.20F).build())
        );
        spearTwoHandSBehaviour = CombatBehaviors.builder().newBehaviorSeries(
                createBehaviourSeries(15, DynamicBehaviour.of(AnimationsX.SPEAR_TWOHAND_AUTO1).distanceMinMax(1, 3).build())
        ).newBehaviorSeries(
                createBehaviourSeries(15, DynamicBehaviour.of(AnimationsX.SPEAR_TWOHAND_AUTO2).distanceMinMax(1, 3).build())
        ).newBehaviorSeries(
                createBehaviourSeries(25, DynamicBehaviour.of(AnimationsX.SPEAR_TWOHAND_AUTO1).distanceMinMax(1, 3).build(), DynamicBehaviour.of(AnimationsX.SPEAR_TWOHAND_AUTO2).distanceMinMax(1, 3).build())
        ).newBehaviorSeries(
                createBehaviourSeries(45, DynamicBehaviour.of(Animations.BIPED_ROLL_BACKWARD).distanceMinMax(0, 1.5F).build())
        ).newBehaviorSeries(
                createBehaviourSeries(45, DynamicBehaviour.of(Animations.BIPED_STEP_LEFT).distanceMinMax(0, 1.20F).build())
        ).newBehaviorSeries(
                createBehaviourSeries(45, DynamicBehaviour.of(Animations.BIPED_STEP_RIGHT).distanceMinMax(0, 1.20F).build())
        );
        greatswordSBehaviour = CombatBehaviors.builder().newBehaviorSeries(
                createBehaviourSeries(15, DynamicBehaviour.of(EFNGreatSwordAnimations.NG_GREATSWORD_AUTO1).distanceMinMax(1, 3).build())
        ).newBehaviorSeries(
                createBehaviourSeries(15, DynamicBehaviour.of(EFNGreatSwordAnimations.NG_GREATSWORD_AUTO2).distanceMinMax(1, 3).build())
        ).newBehaviorSeries(
                createBehaviourSeries(15, DynamicBehaviour.of(EFNGreatSwordAnimations.NG_GREATSWORD_AUTO3).distanceMinMax(1, 3).build())
        ).newBehaviorSeries(
                createBehaviourSeries(15, DynamicBehaviour.of(EFNGreatSwordAnimations.NG_GREATSWORD_AUTO1).distanceMinMax(1, 3).build())
        ).newBehaviorSeries(
                createBehaviourSeries(25, DynamicBehaviour.of(EFNGreatSwordAnimations.NG_GREATSWORD_AUTO1).distanceMinMax(1, 3).build(), DynamicBehaviour.of(EFNGreatSwordAnimations.NG_GREATSWORD_AUTO2).distanceMinMax(1, 3).build())
        ).newBehaviorSeries(
                createBehaviourSeries(25, DynamicBehaviour.of(EFNGreatSwordAnimations.NG_GREATSWORD_AUTO1).distanceMinMax(1, 3).build(), DynamicBehaviour.of(EFNGreatSwordAnimations.NG_GREATSWORD_AUTO2).distanceMinMax(1, 3).build(), DynamicBehaviour.of(EFNGreatSwordAnimations.NG_GREATSWORD_AUTO3).distanceMinMax(1, 3).build())
        ).newBehaviorSeries(
                createBehaviourSeries(45, DynamicBehaviour.of(Animations.BIPED_ROLL_BACKWARD).distanceMinMax(0, 1.5F).build())
        ).newBehaviorSeries(
                createBehaviourSeries(45, DynamicBehaviour.of(Animations.BIPED_STEP_LEFT).distanceMinMax(0, 1.20F).build())
        ).newBehaviorSeries(
                createBehaviourSeries(45, DynamicBehaviour.of(Animations.BIPED_STEP_RIGHT).distanceMinMax(0, 1.20F).build())
        );
        katanaSBehaviour = CombatBehaviors.builder().newBehaviorSeries(
                createBehaviourSeries(15, DynamicBehaviour.of(AnimationsX.UCHIGATANA_AUTO1).distanceMinMax(1, 3).build())
        ).newBehaviorSeries(
                createBehaviourSeries(15, DynamicBehaviour.of(AnimationsX.UCHIGATANA_AUTO2).distanceMinMax(1, 3).build())
        ).newBehaviorSeries(
                createBehaviourSeries(15, DynamicBehaviour.of(AnimationsX.UCHIGATANA_AUTO3).distanceMinMax(1, 3).build())
        ).newBehaviorSeries(
                createBehaviourSeries(25, DynamicBehaviour.of(AnimationsX.UCHIGATANA_AUTO1).distanceMinMax(1, 3).build(), DynamicBehaviour.of(AnimationsX.UCHIGATANA_AUTO2).distanceMinMax(1, 3).build())
        ).newBehaviorSeries(
                createBehaviourSeries(21, DynamicBehaviour.of(AnimationsX.UCHIGATANA_AUTO1).distanceMinMax(1, 3).build(), DynamicBehaviour.of(AnimationsX.UCHIGATANA_AUTO2).distanceMinMax(1, 3).build(), DynamicBehaviour.of(AnimationsX.UCHIGATANA_AUTO3).distanceMinMax(1, 3).build())
        ).newBehaviorSeries(
                createBehaviourSeries(45, DynamicBehaviour.of(Animations.BIPED_ROLL_BACKWARD).distanceMinMax(0, 1.5F).build())
        ).newBehaviorSeries(
                createBehaviourSeries(45, DynamicBehaviour.of(Animations.BIPED_STEP_LEFT).distanceMinMax(0, 1.20F).build())
        ).newBehaviorSeries(
                createBehaviourSeries(45, DynamicBehaviour.of(Animations.BIPED_STEP_RIGHT).distanceMinMax(0, 1.20F).build())
        );
    }
   
}
