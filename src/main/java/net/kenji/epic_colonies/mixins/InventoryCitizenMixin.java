package net.kenji.epic_colonies.mixins;

import com.minecolonies.api.inventory.InventoryCitizen;
import net.kenji.epic_colonies.cosmetic.ICosmeticArmorHolder;
import net.kenji.epic_colonies.offhand.ICitizenOffhandHolder;
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
public abstract class InventoryCitizenMixin implements ICosmeticArmorHolder, ICitizenOffhandHolder {
    @Unique
    private static final String EC_KEY = "epic_colonies:cosmetic_armor";
    @Unique private final NonNullList<ItemStack> ec$cosmetic = NonNullList.withSize(4, ItemStack.EMPTY); // index = EquipmentSlot.getIndex()

    @Unique
    private static final String EC_OFFHAND_KEY = "epic_colonies:offhand";
    @Unique private final NonNullList<ItemStack> ec$offhand = NonNullList.withSize(1, ItemStack.EMPTY);


    @Override
    public NonNullList<ItemStack> ec$getCosmeticArmor() { return ec$cosmetic; }
    @Override
    public ItemStack ec$getCosmeticArmorItemFromSlot(EquipmentSlot slot) { return ec$cosmetic.get(slot.getIndex()); }
    @Override
    public NonNullList<ItemStack> ec$getOffhand() { return ec$offhand; }

    @Inject(method = "write", at = @At("TAIL"))
    private void ec$write(CompoundTag tag, CallbackInfo ci) {
        ListTag list = new ListTag();
        for (int i = 0; i < 4; i++) if (!ec$cosmetic.get(i).isEmpty()) {
            CompoundTag t = new CompoundTag(); t.putByte("Slot", (byte) i);
            list.add(ec$cosmetic.get(i).save(t));
        }
        tag.put(EC_KEY, list);

        if (!ec$offhand.get(0).isEmpty()) {
            tag.put(EC_OFFHAND_KEY, ec$offhand.get(0).save(new CompoundTag()));
        }
    }

    @Inject(method = "read", at = @At("TAIL"))
    private void ec$read(CompoundTag tag, CallbackInfo ci) {
        for (int i = 0; i < 4; i++) ec$cosmetic.set(i, ItemStack.EMPTY);
        for (Tag t : tag.getList(EC_KEY, Tag.TAG_COMPOUND)) {
            CompoundTag c = (CompoundTag) t; int s = c.getByte("Slot");
            if (s >= 0 && s < 4) ec$cosmetic.set(s, ItemStack.of(c));
        }

        // Always reset first: the client-side view re-reads this tag on every sync, so a removed offhand must clear.
        ec$offhand.set(0, tag.contains(EC_OFFHAND_KEY, Tag.TAG_COMPOUND) ? ItemStack.of(tag.getCompound(EC_OFFHAND_KEY)) : ItemStack.EMPTY);
    }
}