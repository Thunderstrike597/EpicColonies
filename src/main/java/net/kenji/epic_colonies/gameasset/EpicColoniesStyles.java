package net.kenji.epic_colonies.gameasset;

import yesman.epicfight.world.capabilities.item.Style;

public enum EpicColoniesStyles implements Style {
        ONE_HAND_CLOSE_RANGE(false);


        final boolean canUseOffhand;
        final int id;

        private EpicColoniesStyles(boolean canUseOffhand) {
            this.id = Style.ENUM_MANAGER.assign(this);
            this.canUseOffhand = canUseOffhand;
        }

        public int universalOrdinal() {
            return this.id;
        }

        public boolean canUseOffhand() {
            return this.canUseOffhand;
        }
    }