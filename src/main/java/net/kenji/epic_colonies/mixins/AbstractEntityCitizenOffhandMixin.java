package net.kenji.epic_colonies.mixins;

import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.kenji.epic_colonies.offhand.CitizenOffhand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Write-side lock: while the player has assigned an offhand item, MineColonies may not replace it.
 *
 * Every MineColonies write to the offhand funnels through AbstractEntityCitizen#setItemSlot
 * (setItemInHand -> setItemSlot, and CitizenItemUtils / the AIs call setItemSlot directly). The ones that were
 * clearing your command-given offhand:
 *   - AbstractEntityAIFight#cleanVisibleSlots   - clears OFFHAND if that item isn't also in the citizen's inventory
 *   - AbstractEntityAIBasic#dumpOneMoreSlot     - clears OFFHAND when it dumps the slot the offhand pointed at
 *   - AbstractAISkeleton#onRemoval / AbstractJob#onRemoval - clear all equipment on job change
 *   - KnightCombatAI / EntityAICombatTraining / JobKnight - swap the shield in and out
 *   - AbstractEntityAICrafting, EntityAIWorkCrusher, EntityAIWorkSifter - put crafting inputs in the offhand
 *
 * Cancelling here (rather than letting the write through and only overriding the read) also stops MineColonies
 * from calling markEquipmentDirty() every time cleanVisibleSlots fires, so no extra equipment packets.
 */
@Mixin(value = AbstractEntityCitizen.class, remap = false)
public abstract class AbstractEntityCitizenOffhandMixin {

    // setItemSlot overrides a vanilla method -> SRG name at runtime -> this injector must be remapped.
    @Inject(method = "setItemSlot", at = @At("HEAD"), cancellable = true, remap = true)
    private void ec$lockOffhand(EquipmentSlot slot, ItemStack stack, CallbackInfo ci) {
        if (slot != EquipmentSlot.OFFHAND) {
            return;
        }
        AbstractEntityCitizen self = (AbstractEntityCitizen) (Object) this;
        ItemStack override = CitizenOffhand.getOverride(self);
        // Let it through if it's already our stack (harmless) - only block MineColonies swapping in something else.
        if (!override.isEmpty() && stack != override) {
            ci.cancel();
        }
    }
}
