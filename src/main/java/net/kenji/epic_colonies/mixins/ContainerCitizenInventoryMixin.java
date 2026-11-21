package net.kenji.epic_colonies.mixins;

import com.minecolonies.api.colony.ICitizen;
import com.minecolonies.api.inventory.InventoryCitizen;
import com.minecolonies.api.inventory.container.ContainerCitizenInventory;
import net.kenji.epic_colonies.cosmetic.CosmeticArmorContainer;
import net.kenji.epic_colonies.cosmetic.CosmeticArmorSlot;
import net.kenji.epic_colonies.offhand.CitizenOffhandContainer;
import net.kenji.epic_colonies.offhand.CitizenOffhandSlot;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Adds 4 cosmetic armor slots to the citizen inventory menu.
 * Runs on BOTH sides (common mixin): the slot list must be identical on client and server or slot sync breaks.
 *
 * Slot order after this mixin:
 *   [0, inventorySize)                      citizen inventory (grows with university research - doesn't matter to us)
 *   [inventorySize, inventorySize + 4)      MineColonies' real armor slots
 *   next 27                                 player main inventory
 *   next 9                                  player hotbar
 *   [ec$cosmeticStart, ec$cosmeticStart+4)  our cosmetic slots (always appended LAST so no existing index moves)
 *   ec$cosmeticStart + 4                    our offhand slot (also excluded from shift-click destinations, see below)
 */
@Mixin(value = ContainerCitizenInventory.class, remap = false)
public abstract class ContainerCitizenInventoryMixin extends AbstractContainerMenu {

    @Unique
    private int ec$cosmeticStart = -1;

    protected ContainerCitizenInventoryMixin(MenuType<?> type, int id) {
        super(type, id);
    }

    @Shadow
    public abstract ICitizen getCitizenData();

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;II)V", at = @At("TAIL"))
    private void ec$addCosmeticSlots(int windowId, Inventory playerInventory, int colonyId, int citizenId, CallbackInfo ci) {
        ICitizen citizen = getCitizenData();
        if (citizen == null) {
            return; // colony/citizen not found - MineColonies' own slots will be broken anyway, don't add ours
        }
        InventoryCitizen inventory = citizen.getInventory();
        if (inventory == null) {
            return;
        }

        CosmeticArmorContainer container = new CosmeticArmorContainer(inventory);
        ec$cosmeticStart = this.slots.size();

        for (int i = 0; i < CosmeticArmorSlot.ORDER.length; i++) {
            EquipmentSlot slot = CosmeticArmorSlot.ORDER[i];
            this.addSlot(new CosmeticArmorSlot(
                    container,
                    slot,
                    CosmeticArmorSlot.START_X + i * CosmeticArmorSlot.SPACING,
                    CosmeticArmorSlot.START_Y));
        }

        // Offhand slot sits after the cosmetic slots, so the shift-click cap below excludes it too.
        this.addSlot(new CitizenOffhandSlot(new CitizenOffhandContainer(citizen, inventory)));
    }

    /**
     * Shift-click fix.
     *
     * MineColonies' quickMoveStack does:
     *   if (index < inventorySize) moveItemStackTo(stack, inventorySize, this.slots.size(), true);   // citizen -> armor/player
     *   else                       moveItemStackTo(stack, 0, inventorySize, false);                  // armor/player -> citizen
     *
     * Because our slots are now at the end of the list and the first branch fills in REVERSE, shift-clicking a helmet
     * out of the citizen inventory would land in the cosmetic head slot before anything else.
     * We cap that upper bound at ec$cosmeticStart so the cosmetic slots are never a shift-click destination.
     * (Shift-clicking OUT of a cosmetic slot hits the else-branch and goes into the citizen inventory, which is fine.)
     *
     * this.slots.size() is the only NonNullList.size() call in quickMoveStack in 1.20.1 (slots.get(index) is a different method).
     */
    @Redirect(method = "quickMoveStack", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/NonNullList;size()I"))
    private int ec$excludeCosmeticFromShiftClick(NonNullList<?> list) {
        return ec$cosmeticStart >= 0 ? ec$cosmeticStart : list.size();
    }
}
