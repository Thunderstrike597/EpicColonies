package net.kenji.epic_colonies.gameasset.behaviours.compat.efn;


import com.hm.efn.gameasset.EFNWeaponCategories;
import com.hm.efn.gameasset.animations.EFNDodgeAnimations;
import com.hm.efn.gameasset.animations.EFNMurasamaAnimations;
import com.hm.efn.gameasset.animations.EFNYamatoAnimations;
import net.kenji.epic_colonies.gameasset.behaviours.CombatBehaviourBase;
import net.kenji.epic_colonies.gameasset.behaviours.CompatMobCombatBehaviours;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.world.capabilities.entitypatch.HumanoidMobPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors;

public class EFNCombatBehaviours extends CombatBehaviourBase {

    static CombatBehaviors.Builder<HumanoidMobPatch<?>> yamatoBehaviour;
    static CombatBehaviors.Builder<HumanoidMobPatch<?>> hfMurasamaBehaviour;


    @SuppressWarnings("unchecked")
    public static void init(){
        buildMotions();
        CompatMobCombatBehaviours.HUMANOID_YAMATO = register(
                EFNWeaponCategories.EFN_YAMATO,
                CompatMobCombatBehaviours.motion(CapabilityItem.Styles.TWO_HAND, yamatoBehaviour, EFNYamatoAnimations.YAMATO_IDLE, EFNYamatoAnimations.YAMATO_WALK, EFNYamatoAnimations.YAMATO_RUN, EFNYamatoAnimations.YAMATO_RUN)
        );
        CompatMobCombatBehaviours.HUMANOID_HF_MURASAMA = register(
                EFNWeaponCategories.EFN_HF_MURASAMA,
                CompatMobCombatBehaviours.motion(CapabilityItem.Styles.TWO_HAND, hfMurasamaBehaviour, EFNMurasamaAnimations.HF_MURASAMA_IDLE_COMBAT, EFNMurasamaAnimations.HF_MURASAMA_WALK_COMBAT, EFNMurasamaAnimations.HF_MURASAMA_RUN_COMBAT_2, EFNMurasamaAnimations.HF_MURASAMA_RUN_COMBAT_2)
        );
    }
    @SuppressWarnings("unchecked")
    private static void buildMotions(){
        yamatoBehaviour = CombatBehaviors.builder().newBehaviorSeries(
                createBehaviourSeries(getAttackWeight(AttackWeightType.SINGLE_X3), DynamicBehaviour.of(EFNYamatoAnimations.YAMATO_NORMAL_AUTO1).distanceMinMax(getMinAttackDist(), getMaxAttackDist()).build())
        ).newBehaviorSeries(
                createBehaviourSeries(getAttackWeight(AttackWeightType.SINGLE_X3), DynamicBehaviour.of(EFNYamatoAnimations.YAMATO_NORMAL_AUTO2).distanceMinMax(getMinAttackDist(), getMaxAttackDist()).build())
        ).newBehaviorSeries(
                        createBehaviourSeries(getAttackWeight(AttackWeightType.SINGLE_X3), DynamicBehaviour.of(EFNYamatoAnimations.YAMATO_NORMAL_AUTO3).distanceMinMax(getMinAttackDist(), getMaxAttackDist()).build())
        ).newBehaviorSeries(
                createBehaviourSeries(getAttackWeight(AttackWeightType.DOUBLE), DynamicBehaviour.of(EFNYamatoAnimations.YAMATO_NORMAL_AUTO1).distanceMinMax(getMinAttackDist(), getMaxAttackDist()).build(), DynamicBehaviour.of(EFNYamatoAnimations.YAMATO_NORMAL_AUTO2).distanceMinMax(getMinAttackDist(), getMaxAttackDist()).build())
        ).newBehaviorSeries(
                createBehaviourSeries(getAttackWeight(AttackWeightType.TRIPLE), DynamicBehaviour.of(EFNYamatoAnimations.YAMATO_NORMAL_AUTO1).distanceMinMax(getMinAttackDist(), getMaxAttackDist()).build(), DynamicBehaviour.of(EFNYamatoAnimations.YAMATO_NORMAL_AUTO2).distanceMinMax(getMinAttackDist(), getMaxAttackDist()).build(), DynamicBehaviour.of(EFNYamatoAnimations.YAMATO_EXTEND_AUTO3).distanceMinMax(getMinAttackDist(), getMaxAttackDist()).build())
        ).newBehaviorSeries(
                createBehaviourSeries(getAttackWeight(AttackWeightType.QUAD), DynamicBehaviour.of(EFNYamatoAnimations.YAMATO_NORMAL_AUTO1).distanceMinMax(getMinAttackDist(), getMaxAttackDist()).build(), DynamicBehaviour.of(EFNYamatoAnimations.YAMATO_NORMAL_AUTO2).distanceMinMax(getMinAttackDist(), getMaxAttackDist()).build(), DynamicBehaviour.of(EFNYamatoAnimations.YAMATO_EXTEND_AUTO3).distanceMinMax(getMinAttackDist(), getMaxAttackDist()).build(), DynamicBehaviour.of(EFNYamatoAnimations.YAMATO_EXTEND_AUTO4).distanceMinMax(getMinAttackDist(), getMaxAttackDist()).build())
        ).newBehaviorSeries(
                        createBehaviourSeries(getAttackWeight(AttackWeightType.X5), DynamicBehaviour.of(EFNYamatoAnimations.YAMATO_NORMAL_AUTO1).distanceMinMax(getMinAttackDist(), getMaxAttackDist()).build(), DynamicBehaviour.of(EFNYamatoAnimations.YAMATO_NORMAL_AUTO2).distanceMinMax(getMinAttackDist(), getMaxAttackDist()).build(), DynamicBehaviour.of(EFNYamatoAnimations.YAMATO_EXTEND_AUTO3).distanceMinMax(getMinAttackDist(), getMaxAttackDist()).build(), DynamicBehaviour.of(EFNYamatoAnimations.YAMATO_EXTEND_AUTO4).distanceMinMax(getMinAttackDist(), getMaxAttackDist()).build(), DynamicBehaviour.of(EFNYamatoAnimations.YAMATO_EXTEND_AUTO5).distanceMinMax(getMinAttackDist(), getMaxAttackDist()).build())
        ).newBehaviorSeries(
                createBehaviourSeries(getDodgeWeight(DodgeWeightType.BACK), DynamicBehaviour.of(EFNDodgeAnimations.YAMATO_STEP_B).distanceMinMax(0, 1.5F).build())
        ).newBehaviorSeries(
                createBehaviourSeries(getDodgeWeight(DodgeWeightType.LEFT), DynamicBehaviour.of(EFNDodgeAnimations.YAMATO_STEP_L).distanceMinMax(0, 1.8F).build())
        ).newBehaviorSeries(
                createBehaviourSeries(getDodgeWeight(DodgeWeightType.RIGHT), DynamicBehaviour.of(EFNDodgeAnimations.YAMATO_STEP_R).distanceMinMax(0, 1.8F).build())
        );
        hfMurasamaBehaviour = CombatBehaviors.builder().newBehaviorSeries(
                createBehaviourSeries(getAttackWeight(AttackWeightType.SINGLE_X3), DynamicBehaviour.of(EFNMurasamaAnimations.HF_MURASAMA_X).distanceMinMax(getMinAttackDist(), getMaxAttackDist()).build())
        ).newBehaviorSeries(
                createBehaviourSeries(getAttackWeight(AttackWeightType.SINGLE_X3), DynamicBehaviour.of(EFNMurasamaAnimations.HF_MURASAMA_XX).distanceMinMax(getMinAttackDist(), getMaxAttackDist()).build())
        ).newBehaviorSeries(
                createBehaviourSeries(getAttackWeight(AttackWeightType.SINGLE_X3), DynamicBehaviour.of(EFNMurasamaAnimations.HF_MURASAMA_XXX).distanceMinMax(getMinAttackDist(), getMaxAttackDist()).build())
        ).newBehaviorSeries(
                createBehaviourSeries(getAttackWeight(AttackWeightType.DOUBLE), DynamicBehaviour.of(EFNMurasamaAnimations.HF_MURASAMA_X).distanceMinMax(getMinAttackDist(), getMaxAttackDist()).build(), DynamicBehaviour.of(EFNMurasamaAnimations.HF_MURASAMA_XX).distanceMinMax(getMinAttackDist(), getMaxAttackDist()).build())
        ).newBehaviorSeries(
                createBehaviourSeries(getAttackWeight(AttackWeightType.TRIPLE), DynamicBehaviour.of(EFNMurasamaAnimations.HF_MURASAMA_X).distanceMinMax(getMinAttackDist(), getMaxAttackDist()).build(), DynamicBehaviour.of(EFNMurasamaAnimations.HF_MURASAMA_XX).distanceMinMax(getMinAttackDist(), getMaxAttackDist()).build(), DynamicBehaviour.of(EFNMurasamaAnimations.HF_MURASAMA_XXX).distanceMinMax(getMinAttackDist(), getMaxAttackDist()).build())
        ).newBehaviorSeries(
                createBehaviourSeries(getAttackWeight(AttackWeightType.QUAD), DynamicBehaviour.of(EFNMurasamaAnimations.HF_MURASAMA_X).distanceMinMax(getMinAttackDist(), getMaxAttackDist()).build(), DynamicBehaviour.of(EFNMurasamaAnimations.HF_MURASAMA_XX).distanceMinMax(getMinAttackDist(), getMaxAttackDist()).build(), DynamicBehaviour.of(EFNMurasamaAnimations.HF_MURASAMA_XXX).distanceMinMax(getMinAttackDist(), getMaxAttackDist()).build(), DynamicBehaviour.of(EFNMurasamaAnimations.HF_MURASAMA_XXXX).distanceMinMax(getMinAttackDist(), getMaxAttackDist()).build())
        ).newBehaviorSeries(
                createBehaviourSeries(getAttackWeight(AttackWeightType.X5), DynamicBehaviour.of(EFNMurasamaAnimations.HF_MURASAMA_X).distanceMinMax(getMinAttackDist(), getMaxAttackDist()).build(), DynamicBehaviour.of(EFNMurasamaAnimations.HF_MURASAMA_XX).distanceMinMax(getMinAttackDist(), getMaxAttackDist()).build(), DynamicBehaviour.of(EFNMurasamaAnimations.HF_MURASAMA_XXX).distanceMinMax(getMinAttackDist(), getMaxAttackDist()).build(), DynamicBehaviour.of(EFNMurasamaAnimations.HF_MURASAMA_XXXX).distanceMinMax(getMinAttackDist(), getMaxAttackDist()).build(), DynamicBehaviour.of(EFNMurasamaAnimations.HF_MURASAMA_XXXY).distanceMinMax(getMinAttackDist(), getMaxAttackDist()).build())
        ).newBehaviorSeries(
                createBehaviourSeries(getDodgeWeight(DodgeWeightType.BACK), DynamicBehaviour.of(EFNDodgeAnimations.YAMATO_STEP_B).distanceMinMax(0, 1.5F).build())
        ).newBehaviorSeries(
                createBehaviourSeries(getDodgeWeight(DodgeWeightType.LEFT), DynamicBehaviour.of(Animations.BIPED_STEP_LEFT).distanceMinMax(0, 1.8F).build())
        ).newBehaviorSeries(
                createBehaviourSeries(getDodgeWeight(DodgeWeightType.RIGHT), DynamicBehaviour.of(Animations.BIPED_STEP_RIGHT).distanceMinMax(0, 1.8F).build())
        ).newBehaviorSeries(createGuardBehaviourSeries(getGuardWeight(), getMinGuardTicks(), getMaxGuardTicks()));



    }
}
