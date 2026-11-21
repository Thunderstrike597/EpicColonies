package net.kenji.epic_colonies.offhand;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.inventory.InventoryCitizen;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Small helper shared by the entity mixins.
 *
 * "Override" = the player has put something in the Epic Colonies offhand slot. While an override exists, the citizen's
 * OFFHAND equipment slot is locked to it: reads return it and MineColonies' writes to that slot are ignored.
 * Server-side only - the client just receives the result through vanilla's equipment sync.
 */
public final class CitizenOffhand {

    private CitizenOffhand() {}

    /** The player-assigned offhand stack, or EMPTY if there is none (or we're on the client / data isn't ready yet). */
    public static ItemStack getOverride(@Nullable AbstractEntityCitizen citizen) {
        if (citizen == null || citizen.level().isClientSide) {
            return ItemStack.EMPTY;
        }
        ICitizenData data = citizen.getCitizenData();
        if (data == null) {
            return ItemStack.EMPTY; // happens during entity construction / before the citizen is bound to its data
        }
        return getOverride(data.getInventory());
    }

    public static ItemStack getOverride(@Nullable InventoryCitizen inventory) {
        if (inventory instanceof ICitizenOffhandHolder holder) {
            return holder.ec$getOffhandItem();
        }
        return ItemStack.EMPTY;
    }

    public static boolean hasOverride(@Nullable AbstractEntityCitizen citizen) {
        return !getOverride(citizen).isEmpty();
    }
}
