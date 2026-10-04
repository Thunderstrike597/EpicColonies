package net.kenji.epic_colonies.gameasset;

import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.WeaponCategory;

public enum EpicColoniesWeaponCategory implements WeaponCategory {
    DUAL_SWORDS(null),
    DUAL_DAGGER(null),
    RANGED_DAGGER(CapabilityItem.WeaponCategories.DAGGER);

    int id = 0;
    final WeaponCategory parentCategory ;
    EpicColoniesWeaponCategory(WeaponCategory parent){
        id = ENUM_MANAGER.assign(this);
        parentCategory = parent;
    };
    @Override
    public int universalOrdinal() {
        return id;
    }
}
