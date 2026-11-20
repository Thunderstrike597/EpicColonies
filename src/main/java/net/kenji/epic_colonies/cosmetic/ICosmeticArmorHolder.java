package net.kenji.epic_colonies.cosmetic;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public interface ICosmeticArmorHolder {

    public NonNullList<ItemStack> ec$getCosmeticArmor();
    public ItemStack ec$getCosmeticArmorItemFromSlot(EquipmentSlot slot);
}
