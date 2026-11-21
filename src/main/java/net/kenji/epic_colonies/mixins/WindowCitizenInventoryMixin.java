package net.kenji.epic_colonies.mixins;

import com.minecolonies.api.inventory.container.ContainerCitizenInventory;
import com.minecolonies.core.client.gui.containers.WindowCitizenInventory;
import net.kenji.epic_colonies.cosmetic.CosmeticArmorSlot;
import net.kenji.epic_colonies.offhand.CitizenOffhandSlot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * CLIENT-only mixin: draws slot backgrounds for the cosmetic row and the offhand slot (the menu mixin adds the actual slots;
 * the texture has no frames there, so without this the items would float on the grey panel).
 */
@Mixin(value = WindowCitizenInventory.class, remap = false)
public abstract class WindowCitizenInventoryMixin extends AbstractContainerScreen<ContainerCitizenInventory> {

    /** Same texture MineColonies blits its armor slot frames from: u=0, v=300, 18x18 on a 350x350 sheet. */
    @Unique
    private static final ResourceLocation EC$CITIZEN_TEXTURE = ResourceLocation.fromNamespaceAndPath("minecolonies", "textures/gui/citizen_container.png");

    protected WindowCitizenInventoryMixin(ContainerCitizenInventory menu, Inventory inv, Component title) {
        super(menu, inv, title);
    }

    /**
     * Row-count fix.
     *
     * MineColonies' constructor derives the citizen inventory height from the TOTAL slot count of the menu:
     *     inventoryRows = (menu.getItems().size() - 36) / 9;
     * That count includes MineColonies' 4 armor slots and every slot we append. 4 armor + 4 cosmetic = 8 still
     * rounded down to the right answer; the offhand slot made it 9 and the screen drew a phantom extra row,
     * pushing the player-inventory background/label out of line with the real (hard-coded) slot positions.
     *
     * We hide our own slots from that one size() call (the only NonNullList.size() in the constructor),
     * so it stays correct however many slots we add in future.
     */
    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/NonNullList;size()I"), remap = false)
    private int ec$excludeOurSlotsFromRowCount(NonNullList<?> items, ContainerCitizenInventory menu, Inventory inv, Component title) {
        int ours = 0;
        for (Slot slot : menu.slots) {
            if (slot instanceof CosmeticArmorSlot || slot instanceof CitizenOffhandSlot) {
                ours++;
            }
        }
        return items.size() - ours;
    }

    // renderBg overrides a vanilla method, so its runtime name is SRG -> this injector must be remapped (default remap = true).
    @Inject(method = "renderBg", at = @At("TAIL"), remap = true)
    private void ec$drawCosmeticSlotFrames(GuiGraphics graphics, float partialTick, int mouseX, int mouseY, CallbackInfo ci) {
        for (int i = 0; i < CosmeticArmorSlot.ORDER.length; i++) {
            int x = this.leftPos + CosmeticArmorSlot.START_X + i * CosmeticArmorSlot.SPACING - 1; // frame is 1px up/left of the item area
            int y = this.topPos + CosmeticArmorSlot.START_Y - 1;
            graphics.blit(EC$CITIZEN_TEXTURE, x, y, 0.0F, 300.0F, 18, 18, 350, 350);
        }

        // Offhand slot frame, below the cosmetic feet slot.
        graphics.blit(EC$CITIZEN_TEXTURE,
                this.leftPos + CitizenOffhandSlot.X - 1,
                this.topPos + CitizenOffhandSlot.Y - 1,
                0.0F, 300.0F, 18, 18, 350, 350);
    }
}