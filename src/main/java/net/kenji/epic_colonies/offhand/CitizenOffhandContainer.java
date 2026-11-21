package net.kenji.epic_colonies.offhand;

import com.minecolonies.api.colony.ICitizen;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.inventory.InventoryCitizen;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Write-through single-slot Container over the offhand list stored on InventoryCitizen
 * (same idea as CosmeticArmorContainer - edits the real list, nothing to copy back).
 */
public class CitizenOffhandContainer implements Container {

    private final ICitizen citizen;
    private final InventoryCitizen inventory;
    private final NonNullList<ItemStack> items;

    public CitizenOffhandContainer(ICitizen citizen, InventoryCitizen inventory) {
        this.citizen = citizen;
        this.inventory = inventory;
        this.items = ((ICitizenOffhandHolder) inventory).ec$getOffhand();
    }

    @Override public int getContainerSize() { return items.size(); }

    @Override public boolean isEmpty() { return items.get(0).isEmpty(); }

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
        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        setChanged();
    }

    /**
     * Marks the citizen dirty (save + view sync).
     * When the slot is emptied we also clear the entity's real offhand, so whatever MineColonies last parked there
     * (while our override was hiding it) doesn't suddenly reappear. MineColonies re-equips what it needs on its own.
     * While the slot is filled nothing else is needed: the entity mixins make getItemBySlot(OFFHAND) return it, and
     * vanilla's equipment-change detection broadcasts it to clients next tick.
     */
    @Override
    public void setChanged() {
        inventory.markDirty();
        if (items.get(0).isEmpty() && citizen instanceof ICitizenData data) {
            data.getEntity().ifPresent(entity -> {
                if (!entity.level().isClientSide) {
                    entity.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
                }
            });
        }
    }

    @Override public boolean stillValid(Player player) { return true; } // menu's own stillValid() handles distance/permissions

    @Override
    public void clearContent() {
        items.set(0, ItemStack.EMPTY);
        setChanged();
    }
}
