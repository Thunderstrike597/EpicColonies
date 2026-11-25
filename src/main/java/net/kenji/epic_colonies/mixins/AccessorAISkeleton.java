package net.kenji.epic_colonies.mixins;

import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.core.entity.ai.workers.AbstractAISkeleton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = AbstractAISkeleton.class, remap = false)
public interface AccessorAISkeleton {

    @Accessor("worker")
    AbstractEntityCitizen epicColonies$getWorker();
}
