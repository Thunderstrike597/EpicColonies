package net.kenji.epic_colonies;

import com.minecolonies.api.colony.jobs.ModJobs;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.List;

public class EpicColoniesConfigCommon {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static ForgeConfigSpec.ConfigValue<Integer> MINE_COUNTER;


    static {
        BUILDER.push("Cosmetic");

        MINE_COUNTER = BUILDER
                .comment("The counter which dictates how long after a citizen stops breaking a block, for the mining animation to stop (This only applies as a fallback if the animation state happens to get stuck)")
                .define("Builder Mining Counter", 1);


        SPEC = BUILDER.build();
    }
}
