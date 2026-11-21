package net.kenji.epic_colonies.offhand;

import com.mojang.datafixers.util.Pair;
import net.kenji.epic_colonies.cosmetic.CosmeticArmorSlot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class CitizenOffhandSlot extends Slot {

    /** Directly below the cosmetic FEET slot (4th in the cosmetic row). Item-area position relative to leftPos/topPos. */
    public static final int X = CosmeticArmorSlot.START_X + 3 * CosmeticArmorSlot.SPACING;
    public static final int Y = CosmeticArmorSlot.START_Y + CosmeticArmorSlot.SPACING;

    public CitizenOffhandSlot(CitizenOffhandContainer container) {
        super(container, 0, X, Y);
    }

    /** Anything goes in the offhand, same as a player's. */
    @Override
    public boolean mayPlace(ItemStack stack) {
        return !stack.isEmpty();
    }

    /** Vanilla's faded shield outline when empty. */
    @Nullable
    @Override
    public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
        return Pair.of(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD);
    }
}
