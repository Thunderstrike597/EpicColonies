package net.kenji.epic_colonies.mixins;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.inventory.InventoryCitizen;
import com.minecolonies.core.entity.citizen.EntityCitizen;
import net.kenji.epic_colonies.cosmetic.ICosmeticArmorHolder;
import net.kenji.epic_colonies.offhand.ICitizenOffhandHolder;
import net.minecraft.core.NonNullList;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EntityCitizen.class, remap = false)
public abstract class EntityCitizenDeathMixin {

    // die() overrides LivingEntity.die -> SRG name m_6667_ outside dev
    @Inject(method = "die", at = @At("HEAD"))
    private void ec$moveCosmeticsIntoInventory(DamageSource source, CallbackInfo ci) {
        EntityCitizen self = (EntityCitizen) (Object) this;
        if (self.level().isClientSide) return;

        ICitizenData data = self.getCitizenData();
        if (data == null) return;

        InventoryCitizen inv = data.getInventory();
        if (inv instanceof ICosmeticArmorHolder holder) {
            NonNullList<ItemStack> cosmetics = holder.ec$getCosmeticArmor();
            boolean changed = false;
            for (int i = 0; i < cosmetics.size(); i++) {
                ItemStack stack = cosmetics.get(i);
                if (stack.isEmpty()) continue;

                cosmetics.set(i, ItemStack.EMPTY); // clear first, so the grave's citizen snapshot never contains it
                ItemStack leftover = ItemHandlerHelper.insertItemStacked(inv, stack, false);
                if (!leftover.isEmpty()) {
                    self.spawnAtLocation(leftover); // inventory full -> drop at the body, same as MineColonies' own overflow
                }
                changed = true;
            }
            if (changed) inv.markDirty();
        }
        if(inv instanceof ICitizenOffhandHolder holder){
            ItemStack offHandStack = holder.ec$getOffhandItem();
            boolean changed = false;
            if (offHandStack.isEmpty()) return;

            ItemStack leftover = ItemHandlerHelper.insertItemStacked(inv, offHandStack, false);
            if (!leftover.isEmpty()) {
                self.spawnAtLocation(leftover); // inventory full -> drop at the body, same as MineColonies' own overflow
            }
            changed = true;
            if (changed) inv.markDirty();
        }

    }
}