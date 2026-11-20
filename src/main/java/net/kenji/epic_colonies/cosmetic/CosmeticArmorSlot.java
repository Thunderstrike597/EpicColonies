package net.kenji.epic_colonies.cosmetic;

import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class CosmeticArmorSlot extends Slot {

    /** Top-left of the first (head) slot's item area, relative to the screen's leftPos/topPos. Shared with the screen mixin. */
    public static final int START_X = 170;
    public static final int START_Y = 98;
    public static final int SPACING = 18;

    /** Left to right on screen: head, chest, legs, feet. */
    public static final EquipmentSlot[] ORDER = { EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET };

    private final EquipmentSlot equipmentSlot;

    public CosmeticArmorSlot(CosmeticArmorContainer container, EquipmentSlot equipmentSlot, int x, int y) {
        // container index == EquipmentSlot.getIndex(), matching MineColonies' armorInventory convention
        super(container, equipmentSlot.getIndex(), x, y);
        this.equipmentSlot = equipmentSlot;
    }
    public static EquipmentSlot getEquipmentSlotForItem(ItemStack stack) {
        final EquipmentSlot slot = stack.getEquipmentSlot();
        if (slot != null) return slot; // FORGE: Allow modders to set a non-default equipment slot for a stack; e.g. a non-armor chestplate-slot item
        Equipable equipable = Equipable.get(stack);
        return equipable != null ? equipable.getEquipmentSlot() : EquipmentSlot.MAINHAND;
    }
    public EquipmentSlot getEquipmentSlot() { return equipmentSlot; }

    /**
     * Anything that equips into this slot: armor, plus Equipable items like carved pumpkins / skulls on the head,
     * and modded armor via Forge's IForgeItem#getEquipmentSlot.
     * Deliberately NOT applying MineColonies' guard-gear building-level restriction - showing gear above the
     * building level is the point of cosmetics.
     */
    @Override
    public boolean mayPlace(ItemStack stack) {
        return !stack.isEmpty() && getEquipmentSlotForItem(stack) == equipmentSlot;
    }

    @Override public int getMaxStackSize() { return 1; }

    @Override public int getMaxStackSize(ItemStack stack) { return 1; }

    /** Vanilla's faded armor outlines when the slot is empty. */
    @Nullable
    @Override
    public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
        ResourceLocation icon = switch (equipmentSlot) {
            case HEAD -> InventoryMenu.EMPTY_ARMOR_SLOT_HELMET;
            case CHEST -> InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE;
            case LEGS -> InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS;
            default -> InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS;
        };
        return Pair.of(InventoryMenu.BLOCK_ATLAS, icon);
    }
}