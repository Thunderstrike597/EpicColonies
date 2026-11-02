package net.kenji.epic_colonies.item.custom;

import net.minecraft.world.item.Tier;
import yesman.epicfight.world.item.TieredWeaponItem;

public class DualWeaponItem extends TieredWeaponItem {
    public DualWeaponItem(Tier tier, int damageIn, float speedIn, Properties builder) {
        super(tier, builder.attributes(createAttributes(tier, damageIn, speedIn, 0.0F)));
    }
}
