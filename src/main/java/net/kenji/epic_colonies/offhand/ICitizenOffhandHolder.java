package net.kenji.epic_colonies.offhand;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

/**
 * Implemented on InventoryCitizen by InventoryCitizenMixin.
 * Holds the player-assigned offhand item (a single-element list so the container can write straight through to it).
 */
public interface ICitizenOffhandHolder {

    NonNullList<ItemStack> ec$getOffhand();

    default ItemStack ec$getOffhandItem() {
        return ec$getOffhand().get(0);
    }
}
