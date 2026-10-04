package net.kenji.epic_colonies;


import net.neoforged.neoforge.common.ModConfigSpec;

public class EpicColoniesConfigCommon {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static ModConfigSpec.ConfigValue<Integer> MINE_COUNTER;
    public static ModConfigSpec.ConfigValue<Boolean> UNBREAKABLE_TOOLS_FIX;
    public static ModConfigSpec.ConfigValue<Boolean> ARCHER_DAGGER_MELEE;


    static {
        BUILDER.push("Values");

        MINE_COUNTER = BUILDER
                .comment("The counter which dictates how long after a citizen stops breaking a block, for the mining animation to stop (This only applies as a fallback if the animation state happens to get stuck)")
                .define("Builder Mining Counter", 1);

        BUILDER.pop();

        BUILDER.push("Fixes");
        UNBREAKABLE_TOOLS_FIX = BUILDER
                .comment("A fix for a bug related to citizens of a job under level 5 not accepting tools which are unbreakable (This is usually caused by mods which make all tools unbreakable, leading to the player unable to fulfill 'In Need <X> Item' requests for items which have no tier (Eg. Bow, shears, fishing rod, shield and flint & steel..))")
                .define("Unbreakable Tools Fix", false);

        BUILDER.pop();

        BUILDER.push("Features");
        ARCHER_DAGGER_MELEE = BUILDER
                .comment("A feature which allows archers to user daggers in close range to aggressors")
                .define("Archer Dagger Melee", false);
        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
