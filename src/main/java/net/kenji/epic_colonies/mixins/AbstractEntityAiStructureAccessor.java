package net.kenji.epic_colonies.mixins;

import com.ldtteam.structurize.placement.StructurePlacer;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.core.entity.ai.workers.AbstractEntityAIStructure;
import com.minecolonies.core.entity.ai.workers.util.BuildingStructureHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = AbstractEntityAIStructure.class)
public interface AbstractEntityAiStructureAccessor {
    @Accessor("structurePlacer")
    Tuple<StructurePlacer, BuildingStructureHandler<?, ?>> getStructurePlacer();

}
