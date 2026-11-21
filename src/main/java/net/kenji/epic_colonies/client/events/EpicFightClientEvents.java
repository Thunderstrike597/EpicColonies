package net.kenji.epic_colonies.client.events;

import net.kenji.epic_colonies.EpicColonies;
import net.kenji.epic_colonies.api.MobPatchFactory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import yesman.epicfight.api.client.event.types.registry.RegisterPatchedRenderersEvent;


public class EpicFightClientEvents {

    public static void registerPatchedEntityRenderers(RegisterPatchedRenderersEvent.AddEntity event) {
        for (MobPatchFactory.MobPatchDefinitions def : MobPatchFactory.mobPatches) {
            event.addPatchedEntityRenderer(
                    def.entityType,
                    entityType -> def.rendererFactory.create(event.getContext(), entityType)
            );
        }
    }
}
