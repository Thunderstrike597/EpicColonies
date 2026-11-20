package net.kenji.epic_colonies.mixins;

import com.minecolonies.api.inventory.InventoryCitizen;
import net.kenji.epic_colonies.cosmetic.ICosmeticArmorHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = InventoryCitizen.class, remap = false)
public abstract class InventoryCitizenMixin implements ICosmeticArmorHolder {
    @Unique
    private static final String EC_KEY = "epiccolonies:cosmetic_armor";
    @Unique private final NonNullList<ItemStack> ec$cosmetic = NonNullList.withSize(4, ItemStack.EMPTY); // index = EquipmentSlot.getIndex()


    @Override
    public NonNullList<ItemStack> ec$getCosmeticArmor() { return ec$cosmetic; }
    @Override
    public ItemStack ec$getCosmeticArmorItemFromSlot(EquipmentSlot slot) { return ec$cosmetic.get(slot.getIndex()); }

    @Inject(method = "write", at = @At("TAIL"))
    private void ec$write(HolderLookup.Provider provider, CompoundTag tag, CallbackInfo ci) {
        ListTag list = new ListTag();
        for (int i = 0; i < 4; i++) if (!ec$cosmetic.get(i).isEmpty()) {
            CompoundTag t = new CompoundTag(); t.putByte("Slot", (byte) i);
            list.add(ec$cosmetic.get(i).save(provider, t));
        }
        tag.put(EC_KEY, list);
    }

    @Inject(method = "read", at = @At("TAIL"))
    private void ec$read(HolderLookup.Provider provider, CompoundTag tag, CallbackInfo ci) {
        for (int i = 0; i < 4; i++) ec$cosmetic.set(i, ItemStack.EMPTY);
        for (Tag t : tag.getList(EC_KEY, Tag.TAG_COMPOUND)) {
            CompoundTag c = (CompoundTag) t; int s = c.getByte("Slot");
            if (s >= 0 && s < 4) ec$cosmetic.set(s, ItemStack.parseOptional(provider, c));
        }
    }
}