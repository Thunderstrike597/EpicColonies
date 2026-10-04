package net.kenji.epic_colonies.mixins;

import com.minecolonies.api.equipment.ModEquipmentTypes;
import net.kenji.epic_colonies.EpicColoniesConfigCommon;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ModEquipmentTypes.class, remap = false)
public abstract class ModEquipmentTypesMixin {

    @Inject(method = "durabilityBasedLevel", at = @At("HEAD"), cancellable = true)
    private static void unbreakableAwareLevel(ItemStack stack, int vanillaDurability,
                                              CallbackInfoReturnable<Integer> cir) {
        if(!EpicColoniesConfigCommon.UNBREAKABLE_TOOLS_FIX.get())return;

        if (!stack.isDamageableItem()) {
            int max = stack.getMaxDamage();
            if (max > 0) {
                // Unbreakable via NBT: durability is still real, so rate it normally
                cir.setReturnValue(Math.min(max / vanillaDurability, 5));
            } else {
                // Max damage is 0: durability is unknowable, so pick a fallback tier (1 = vanilla-equivalent)
                cir.setReturnValue(1);
            }
        }
    }
}