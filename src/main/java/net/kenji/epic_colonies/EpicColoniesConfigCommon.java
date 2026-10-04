package net.kenji.epic_colonies;


import net.neoforged.neoforge.common.ModConfigSpec;

public class EpicColoniesConfigCommon {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static ModConfigSpec.ConfigValue<Integer> MINE_COUNTER;


    static {
        BUILDER.push("Cosmetic");

        MINE_COUNTER = BUILDER
                .comment("The counter which dictates how long after a citizen stops breaking a block, for the mining animation to stop (This only applies as a fallback if the animation state happens to get stuck)")
                .define("Builder Mining Counter", 1);


        SPEC = BUILDER.build();
    }
}
