package net.kenji.epic_colonies.client.render.patched_renderer;

import net.kenji.epic_colonies.client.meshes.EpicColoniesMeshes;
import net.kenji.epic_colonies.client.render.patched_renderer.regular.p_renderer.PAmazonChiefRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.EntityType;

public class AmazonChiefPatchRenderer extends PAmazonChiefRenderer {
    public AmazonChiefPatchRenderer(EntityRendererProvider.Context context, EntityType<?> entityType) {
        super(EpicColoniesMeshes.AMAZON_CHIEF, context, entityType);
    }
}
