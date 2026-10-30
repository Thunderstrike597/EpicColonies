package net.kenji.epic_colonies;

import net.minecraftforge.common.ForgeConfigSpec;

public class EpicColoniesConfigCommon {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static ForgeConfigSpec.ConfigValue<Integer> MINE_COUNTER;
    public static ForgeConfigSpec.ConfigValue<Boolean> UNBREAKABLE_TOOLS_FIX;
    public static ForgeConfigSpec.ConfigValue<Boolean> ARCHER_DAGGER_MELEE;
    public static ForgeConfigSpec.ConfigValue<Boolean> DISABLE_3D_FACES;
    public static ForgeConfigSpec.ConfigValue<Boolean> DISABLE_BLINKING;
    public static ForgeConfigSpec.ConfigValue<Boolean> DISABLE_HAPPINESS_FACIAL_FEATURES;
    public static ForgeConfigSpec.ConfigValue<Boolean> DISABLE_EYES_FOLLOW;


    public static ForgeConfigSpec.ConfigValue<Integer> SINGLE_ATTACK_WEIGHT_X4;
    public static ForgeConfigSpec.ConfigValue<Integer> SINGLE_ATTACK_WEIGHT_X3;
    public static ForgeConfigSpec.ConfigValue<Integer> SINGLE_ATTACK_WEIGHT_X2;
    public static ForgeConfigSpec.ConfigValue<Integer> SINGLE_ATTACK_WEIGHT_X1;
    public static ForgeConfigSpec.ConfigValue<Integer> DOUBLE_ATTACK_WEIGHT;
    public static ForgeConfigSpec.ConfigValue<Integer> TRIPLE_ATTACK_WEIGHT;
    public static ForgeConfigSpec.ConfigValue<Integer> QUAD_ATTACK_WEIGHT;
    public static ForgeConfigSpec.ConfigValue<Integer> X5_ATTACK_WEIGHT;

    public static ForgeConfigSpec.ConfigValue<Integer> LEFT_DODGE_WEIGHT;
    public static ForgeConfigSpec.ConfigValue<Integer> RIGHT_DODGE_WEIGHT;
    public static ForgeConfigSpec.ConfigValue<Integer> BACK_DODGE_WEIGHT;

    static {
        BUILDER.push("Values");

        MINE_COUNTER = BUILDER
                .comment("The counter which dictates how long after a citizen stops breaking a block, for the mining animation to stop (This only applies as a fallback if the animation state happens to get stuck)")
                .define("Builder Mining Counter", 24);

        BUILDER.pop();

        BUILDER.push("Fixes");
        UNBREAKABLE_TOOLS_FIX = BUILDER
                .comment("A fix for a bug related to citizens of a job under level 5 not accepting tools which are unbreakable (This is usually caused by mods which make all tools unbreakable, leading to the player unable to fulfill 'In Need <X> Item' requests for items which have no tier (Eg. Bow, shears, fishing rod, shield and flint & steel..))")
                .define("Unbreakable Tools Fix", false);

        BUILDER.pop();
        BUILDER.push("Features");
        BUILDER.push("Experimental Features");
        ARCHER_DAGGER_MELEE = BUILDER
                .comment("A feature which allows archers to user daggers in close range to aggressors")
                .define("Archer Dagger Melee", false);

        BUILDER.pop();
        BUILDER.push("Facial Features");
        DISABLE_3D_FACES = BUILDER
                .comment("If true, citizens will no longer have 3D faces (This will help with custom texture packs where the faces are misaligned on the citizen 3D Faces)")
                .define("Disable Citizen 3D Faces", false);

        DISABLE_BLINKING = BUILDER
                .comment("If true, citizens will no longer blink")
                .define("Disable Citizen Blinking", false);
        DISABLE_HAPPINESS_FACIAL_FEATURES = BUILDER
                .comment("If true, citizens will no longer smile/frown/look-angry when happiness is above/below a certain point (The will remain neutral in expression)")
                .define("Disable Citizen Facial Expressions", false);
        DISABLE_EYES_FOLLOW = BUILDER
                .comment("If true, citizens eyes will no longer follow you when in looking range/angle")
                .define("Disable Citizen Eyes Follow", false);


        BUILDER.pop();
        BUILDER.pop();
        BUILDER.comment("Attack weights decide how often a mob picks each attack. They are relative, not percentages: " +
                        "chance = weight / total weight of all moves available at that moment (dodges included). " +
                        "A weight of 0 means never use it.")
                .push("Combat Attack Weights");

        SINGLE_ATTACK_WEIGHT_X4 = BUILDER
                .comment("(NEEDS RESTART TO TAKE EFFECT) Weight of EACH single-hit attack, for weapons with 4 single-hit attacks. " +
                        "All single hits together = this value x 4. Lower it so mobs with many single hits don't favor them over combos.")
                .define("Combat Attack Weight Single X4", 4);
        SINGLE_ATTACK_WEIGHT_X3 = BUILDER
                .comment("(NEEDS RESTART TO TAKE EFFECT) Weight of EACH single-hit attack, for weapons with 3 single-hit attacks. " +
                        "All single hits together = this value x 3.")
                .define("Combat Attack Weight Single X3", 6);
        SINGLE_ATTACK_WEIGHT_X2 = BUILDER
                .comment("(NEEDS RESTART TO TAKE EFFECT) Weight of EACH single-hit attack, for weapons with 2 single-hit attacks. " +
                        "All single hits together = this value x 2.")
                .define("Combat Attack Weight Single X2", 10);
        SINGLE_ATTACK_WEIGHT_X1 = BUILDER
                .comment("(NEEDS RESTART TO TAKE EFFECT) Weight of the single-hit attack, for weapons with only 1 single-hit attack.")
                .define("Combat Attack Weight Single X1", 12);
        DOUBLE_ATTACK_WEIGHT = BUILDER
                .comment("(NEEDS RESTART TO TAKE EFFECT) Weight of the 2-hit combo (two attacks in a row).")
                .define("Combat Attack Weight Double", 22);
        TRIPLE_ATTACK_WEIGHT = BUILDER
                .comment("(NEEDS RESTART TO TAKE EFFECT) Weight of the 3-hit combo (three attacks in a row).")
                .define("Combat Attack Weight Triple", 20);
        QUAD_ATTACK_WEIGHT = BUILDER
                .comment("(NEEDS RESTART TO TAKE EFFECT) Weight of the 4-hit combo (four attacks in a row).")
                .define("Combat Attack Weight Quad", 16);
        X5_ATTACK_WEIGHT = BUILDER
                .comment("(NEEDS RESTART TO TAKE EFFECT) Weight of the 5-hit combo (five attacks in a row).")
                .define("Combat Attack Weight X5", 14);
        LEFT_DODGE_WEIGHT = BUILDER
                .comment("(NEEDS RESTART TO TAKE EFFECT) Weight of the left dodge")
                .define("Dodge Weight Left", 40);
        RIGHT_DODGE_WEIGHT = BUILDER
                .comment("(NEEDS RESTART TO TAKE EFFECT) Weight of the right dodge")
                .define("Dodge Weight Right", 40);
        BACK_DODGE_WEIGHT = BUILDER
                .comment("(NEEDS RESTART TO TAKE EFFECT) Weight of the back dodge")
                .define("Dodge Weight Back", 40);

        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}
