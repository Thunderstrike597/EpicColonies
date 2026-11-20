package net.kenji.epic_colonies.cosmetic;

import com.minecolonies.api.inventory.InventoryCitizen;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Write-through Container over the cosmetic list stored on InventoryCitizen.
 * Unlike MineColonies' armor slots (which wrap a SimpleContainer copy), this edits the real list directly,
 * so there's nothing to copy back and no way for the two to drift apart.
 */
public class CosmeticArmorContainer implements Container {
    private final InventoryCitizen inventory;
    private final NonNullList<ItemStack> items;

    public CosmeticArmorContainer(InventoryCitizen inventory) {
        this.inventory = inventory;
        this.items = ((ICosmeticArmorHolder) inventory).ec$getCosmeticArmor();
    }

    @Override public int getContainerSize() { return items.size(); }

    @Override
    public boolean isEmpty() {
        for (ItemStack s : items) if (!s.isEmpty()) return false;
        return true;
    }

    @Override public ItemStack getItem(int slot) { return items.get(slot); }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack out = ContainerHelper.removeItem(items, slot, amount);
        if (!out.isEmpty()) setChanged();
        return out;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        setChanged();
    }

    @Override public int getMaxStackSize() { return 1; }

    /** Marks the citizen dirty server-side so it gets saved and the ICitizenDataView is re-synced (that's what your render layer reads). */
    @Override
    public void setChanged() {
        inventory.markDirty();
    }

    @Override public boolean stillValid(Player player) { return true; } // the menu's own stillValid() handles distance/permissions

    @Override
    public void clearContent() {
        for (int i = 0; i < items.size(); i++) items.set(i, ItemStack.EMPTY);
        setChanged();
    }
}