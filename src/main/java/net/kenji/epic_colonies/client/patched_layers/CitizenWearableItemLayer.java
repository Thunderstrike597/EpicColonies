package net.kenji.epic_colonies.client.patched_layers;

import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.jobs.IJobView;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.inventory.InventoryCitizen;
import com.mojang.blaze3d.vertex.PoseStack;

import java.util.*;

import com.mojang.datafixers.util.Pair;
import net.kenji.epic_colonies.EpicColoniesConfigClient;
import net.kenji.epic_colonies.api.helpers.NoopVertexConsumer;
import net.kenji.epic_colonies.cosmetic.ICosmeticArmorHolder;
import net.kenji.epic_colonies.gameasset.patch.CitizenEntityPatch;
import net.kenji.epic_colonies.mixins.AccessorHumanoidArmorLayer;
import net.kenji.epic_colonies.mixins.AccessorWearableItemLayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlot.Type;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.registries.ForgeRegistries;
import net.veroxuniverse.samurai_dynasty.item.armor.EtyriteSamuraiArmorItem;
import net.veroxuniverse.samurai_dynasty.registry.ItemsRegistry;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.asset.JsonAssetLoader;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.client.model.transformer.HumanoidModelBaker;
import yesman.epicfight.api.exception.AssetLoadingException;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.mesh.HumanoidMesh;
import yesman.epicfight.client.renderer.patched.layer.WearableItemLayer;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class CitizenWearableItemLayer<E extends AbstractEntityCitizen, T extends LivingEntityPatch<E>,
        M extends HumanoidModel<E>, AM extends HumanoidMesh>
        extends WearableItemLayer<E, T, M, AM> {
    private static final Map<ResourceLocation, SkinnedMesh> ARMOR_MODELS = new HashMap();
    private static final Map<String, ResourceLocation> EPICFIGHT_OVERRIDING_TEXTURES = new HashMap();
    private final boolean firstPersonModel;
    private final TextureAtlas armorTrimAtlas;

    private static final List<JobEntry> validRenderEntries = new ArrayList<>();

    private static final Map<UUID, Map<EquipmentSlot, Boolean>> shouldRenderArmorMap = new HashMap<>();
    public static void clearModels() {
        ARMOR_MODELS.values().forEach(SkinnedMesh::destroy);
        ARMOR_MODELS.clear();
        EPICFIGHT_OVERRIDING_TEXTURES.clear();
    }

    public static void putModel(ResourceLocation rl, SkinnedMesh skinnedMesh) {
        ARMOR_MODELS.computeIfPresent(rl, (key, mesh) -> {
            if (mesh != skinnedMesh) {
                mesh.destroy();
            }

            return mesh;
        });
        ARMOR_MODELS.put(rl, skinnedMesh);
    }

    public static boolean shouldHidePart(AbstractEntityCitizen citizen, EquipmentSlot slot){
        ICitizenDataView view = citizen.getCitizenDataView();
        if(view != null) {
            if (view.getInventory() instanceof ICosmeticArmorHolder iHolder) {
                return (!citizen.getItemBySlot(slot).isEmpty()
                        || !iHolder.ec$getCosmeticArmorItemFromSlot(slot).isEmpty())
                        && shouldRenderArmorMap
                        .getOrDefault(citizen.getUUID(), Collections.emptyMap())
                        .getOrDefault(slot, true);
            }
        }
        return !citizen.getItemBySlot(slot).isEmpty()
                && shouldRenderArmorMap
                .getOrDefault(citizen.getUUID(), Collections.emptyMap())
                .getOrDefault(slot, true);
    }

    public static SkinnedMesh getCachedModel(Item item) {
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(item);
        return (SkinnedMesh)ARMOR_MODELS.get(key);
    }

    public CitizenWearableItemLayer(AssetAccessor<AM> meshProvider, boolean firstPersonModel, ModelManager modelManager) {
        super(meshProvider, false, modelManager); // always false — we handle full body
        this.firstPersonModel = firstPersonModel;
        this.armorTrimAtlas = modelManager.getAtlas(Sheets.ARMOR_TRIMS_SHEET);
        validRenderEntries.add(ModJobs.knight.get());
        validRenderEntries.add(ModJobs.archer.get());
    }

    private boolean hidePartHelmet(EquipmentSlot slot){
        return slot == EquipmentSlot.HEAD && EpicColoniesConfigClient.HIDE_CITIZEN_HELMET.get();
    }

    public void renderLayer(T entitypatch, E entityliving, HumanoidArmorLayer<E, M, M> vanillaLayer, PoseStack poseStack, MultiBufferSource buf, int packedLight, OpenMatrix4f[] poses, float bob, float yRot, float xRot, float partialTicks) {
        ICitizenDataView view = entityliving.getCitizenDataView();
        JobEntry jobEntry = null;
        if (view != null) {
            IJobView jobView = view.getJobView();
            if (jobView == null)
                return;
            jobEntry = jobView.getEntry();
        }else return;

        InventoryCitizen inventoryCitizen = view.getInventory();

        if(inventoryCitizen instanceof ICosmeticArmorHolder iHolder) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                ItemStack cosmeticStack = iHolder.ec$getCosmeticArmorItemFromSlot(slot);

                if (slot.getType() == Type.ARMOR) {
                    if (EpicColoniesConfigClient.JOB_ONLY_ARMOR.get()) {
                        if (jobEntry == null)
                            return;
                        String jobName = jobEntry.getKey().toString();
                        if (!EpicColoniesConfigClient.VISIBLE_ARMOR_JOBS.get().contains(jobName)) {
                            Map<EquipmentSlot, Boolean> allHidden = new EnumMap<>(EquipmentSlot.class);
                            for (EquipmentSlot slot2 : EquipmentSlot.values()) {
                                allHidden.put(slot2, false);
                            }
                            shouldRenderArmorMap.put(entityliving.getUUID(), allHidden);
                            return;
                        }
                    }
                    if(slot == EquipmentSlot.HEAD) {
                        if (hidePartHelmet(slot) && cosmeticStack.isEmpty()) {
                            shouldRenderArmorMap
                                    .computeIfAbsent(entityliving.getUUID(), id -> new EnumMap<>(EquipmentSlot.class))
                                    .put(slot, false);
                            continue;
                        }
                    }

                    shouldRenderArmorMap
                            .computeIfAbsent(entityliving.getUUID(), id -> new EnumMap<>(EquipmentSlot.class))
                            .put(slot, true);


                    ItemStack itemstack = cosmeticStack.isEmpty() ? entityliving.getItemBySlot(slot) : cosmeticStack;
                    Item item = itemstack.getItem();

                    if (item instanceof ArmorItem) {

                        ArmorItem armorItem = (ArmorItem) item;
                        if (slot != armorItem.getEquipmentSlot()) {
                            return;
                        }

                        poseStack.pushPose();
                        float head = 0.0F;
                        if (slot == EquipmentSlot.HEAD) {
                            poseStack.translate((double) 0.0F, (double) head * 0.055, (double) 0.0F);
                        }

                        HumanoidModel<?> defaultModel = ((AccessorHumanoidArmorLayer) vanillaLayer).invokeGetArmorModel(slot);
                        Model armorModel = ForgeHooksClient.getArmorModel(entityliving, itemstack, slot, defaultModel);
                        SkinnedMesh armorMesh = this.getArmorModel(vanillaLayer, defaultModel, armorModel, entityliving, armorItem, itemstack, slot);
                        if (armorMesh == null) {
                            poseStack.popPose();
                            return;
                        }


                        if (armorModel instanceof HumanoidModel humanoidModel) {
                            boolean shouldSit = entityliving.isPassenger() && entityliving.getVehicle() != null && entityliving.getVehicle().shouldRiderSit();
                            float f8 = 0.0F;
                            float f5 = 0.0F;
                            if (!shouldSit && entityliving.isAlive()) {
                                f8 = entityliving.walkAnimation.speed(partialTicks);
                                f5 = entityliving.walkAnimation.position(partialTicks);
                                if (entityliving.isBaby()) {
                                    f5 *= 3.0F;
                                }

                                if (f8 > 1.0F) {
                                    f8 = 1.0F;
                                }
                            }

                            try {
                                humanoidModel.setupAnim(entityliving, f8, f5, bob, yRot, xRot);
                            } catch (ClassCastException var29) {
                            }

                            humanoidModel.head.loadPose(humanoidModel.head.getInitialPose());
                            humanoidModel.hat.loadPose(humanoidModel.hat.getInitialPose());
                            humanoidModel.body.loadPose(humanoidModel.body.getInitialPose());
                            humanoidModel.leftArm.loadPose(humanoidModel.leftArm.getInitialPose());
                            humanoidModel.rightArm.loadPose(humanoidModel.rightArm.getInitialPose());
                            humanoidModel.leftLeg.loadPose(humanoidModel.leftLeg.getInitialPose());
                            humanoidModel.rightLeg.loadPose(humanoidModel.rightLeg.getInitialPose());
                        }

                        armorMesh.initialize();

                        if (armorItem instanceof DyeableLeatherItem dyeable && dyeable.hasCustomColor(itemstack)) {
                            DyeableLeatherItem dyeableItem = (DyeableLeatherItem) armorItem;
                            int i = dyeableItem.getColor(itemstack);
                            float r = (float) (i >> 16 & 255) / 255.0F;
                            float g = (float) (i >> 8 & 255) / 255.0F;
                            float b = (float) (i & 255) / 255.0F;
                            ((AccessorWearableItemLayer) this).invokeRenderArmor(poseStack, buf, packedLight, armorMesh, entitypatch.getArmature(), r, g, b, ((AccessorWearableItemLayer) this).invokeGetArmorTexture(itemstack, entityliving, armorMesh, slot, (String) null, defaultModel), poses);
                            ((AccessorWearableItemLayer) this).invokeRenderArmor(poseStack, buf, packedLight, armorMesh, entitypatch.getArmature(), 1.0F, 1.0F, 1.0F, ((AccessorWearableItemLayer) this).invokeGetArmorTexture(itemstack, entityliving, armorMesh, slot, "overlay", defaultModel), poses);
                        } else {
                            ((AccessorWearableItemLayer) this).invokeRenderArmor(poseStack, buf, packedLight, armorMesh, entitypatch.getArmature(), 1.0F, 1.0F, 1.0F, ((AccessorWearableItemLayer) this).invokeGetArmorTexture(itemstack, entityliving, armorMesh, slot, (String) null, defaultModel), poses);
                        }

                        ArmorTrim.getTrim(entityliving.level().registryAccess(), itemstack).ifPresent((armorTrim) -> ((AccessorWearableItemLayer) this).invokeRenderTrim(poseStack, buf, packedLight, armorMesh, entitypatch.getArmature(), armorItem.getMaterial(), armorTrim, slot, poses));
                        if (itemstack.hasFoil()) {
                            ((AccessorWearableItemLayer) this).invokeRenderGlint(poseStack, buf, packedLight, armorMesh, entitypatch.getArmature(), poses);
                        }

                        poseStack.popPose();
                    }
                }
            }
        }
    }

    private SkinnedMesh getArmorModel(HumanoidArmorLayer<E, M, M> originalRenderer, HumanoidModel originalModel, Model forgeHooksArmorModel, E entityliving, ArmorItem armorItem, ItemStack itemstack, EquipmentSlot slot) {
        ResourceLocation registryName = ForgeRegistries.ITEMS.getKey(armorItem);
        if (ARMOR_MODELS.containsKey(registryName) && !ClientEngine.getInstance().renderEngine.shouldRenderVanillaModel()) {
            return (SkinnedMesh)ARMOR_MODELS.get(registryName);
        } else {
            ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
            ResourceLocation rl = ResourceLocation.fromNamespaceAndPath(ForgeRegistries.ITEMS.getKey(armorItem).getNamespace(), "animmodels/armor/" + ForgeRegistries.ITEMS.getKey(armorItem).getPath() + ".json");
            SkinnedMesh skinnedMesh = null;
            if (resourceManager.getResource(rl).isPresent()) {
                try {
                    JsonAssetLoader modelLoader = new JsonAssetLoader(resourceManager, rl);
                    skinnedMesh = modelLoader.loadSkinnedMesh(SkinnedMesh::new);
                } catch (AssetLoadingException e) {
                    e.printStackTrace();
                    skinnedMesh = null;
                }
            } else {
                prepareForBake(originalModel, slot);
                if (forgeHooksArmorModel instanceof HumanoidModel<?> armorHumanoid && armorHumanoid != originalModel) {
                    prepareForBake(armorHumanoid, slot);
                }

                skinnedMesh = HumanoidModelBaker.bakeArmor(entityliving, itemstack, armorItem, slot, originalModel,
                        forgeHooksArmorModel, (HumanoidModel) originalRenderer.getParentModel(), (HumanoidMesh) this.mesh.get());
            }

            putModel(registryName, skinnedMesh);
            return skinnedMesh;
        }
    }
    private static void prepareForBake(HumanoidModel<?> model, EquipmentSlot slot) {
        model.young = false;
        model.crouching = false;
        model.riding = false;
        model.leftArmPose = HumanoidModel.ArmPose.EMPTY;
        model.rightArmPose = HumanoidModel.ArmPose.EMPTY;

        for (ModelPart part : new ModelPart[] { model.head, model.hat, model.body,
                model.rightArm, model.leftArm, model.rightLeg, model.leftLeg }) {
            part.loadPose(part.getInitialPose());
        }

        model.setAllVisible(false);
        switch (slot) {
            case HEAD -> { model.head.visible = true; model.hat.visible = true; }
            case CHEST -> { model.body.visible = true; model.rightArm.visible = true; model.leftArm.visible = true; }
            case LEGS -> { model.body.visible = true; model.rightLeg.visible = true; model.leftLeg.visible = true; }
            case FEET -> { model.rightLeg.visible = true; model.leftLeg.visible = true; }
            default -> {}
        }
    }

}
