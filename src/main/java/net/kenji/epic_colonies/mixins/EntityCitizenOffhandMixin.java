package net.kenji.epic_colonies.mixins;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.core.entity.citizen.EntityCitizen;
import net.kenji.epic_colonies.offhand.CitizenOffhand;
import net.kenji.epic_colonies.offhand.ICitizenOffhandHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Read-side lock (server only): while an offhand item is assigned, getItemBySlot(OFFHAND) returns it.
 *
 * That single method is what everything reads - getOffhandItem(), getItemInHand(OFF_HAND), vanilla's
 * equipment-change detection (which applies attribute modifiers and sends ClientboundSetEquipmentPacket),
 * MineColonies' own detectEquipmentUpdates(), and Epic Fight's patches. So clients get the item through
 * normal vanilla sync; no custom packet needed.
 *
 * The returned stack is the real stored stack (not a copy) so durability loss from blocking / Epic Fight
 * offhand attacks lands on it, and if it breaks it becomes empty and the lock releases itself.
 */
@Mixin(value = EntityCitizen.class, remap = false)
public abstract class EntityCitizenOffhandMixin {

    @Inject(method = "getItemBySlot", at = @At("HEAD"), cancellable = true, remap = true)
    private void ec$returnAssignedOffhand(EquipmentSlot slot, CallbackInfoReturnable<ItemStack> cir) {
        if (slot != EquipmentSlot.OFFHAND) {
            return;
        }
        ItemStack override = CitizenOffhand.getOverride((AbstractEntityCitizen) (Object) this);
        if (!override.isEmpty()) {
            cir.setReturnValue(override);
        }
    }

    /**
     * MineColonies drops the whole citizen inventory on death (EntityCitizen#dropEquipment);
     * do the same for the assigned offhand so it isn't silently lost.
     */
    @Inject(method = "dropEquipment", at = @At("TAIL"), remap = true)
    private void ec$dropAssignedOffhand(CallbackInfo ci) {
        AbstractEntityCitizen self = (AbstractEntityCitizen) (Object) this;
        if (self.level().isClientSide) {
            return;
        }
        ICitizenData data = self.getCitizenData();
        if (data == null || !(data.getInventory() instanceof ICitizenOffhandHolder holder)) {
            return;
        }
        ItemStack stack = holder.ec$getOffhandItem();
        if (!stack.isEmpty()) {
            holder.ec$getOffhand().set(0, ItemStack.EMPTY);
            self.spawnAtLocation(stack);
            data.getInventory().markDirty();
        }
    }
}
