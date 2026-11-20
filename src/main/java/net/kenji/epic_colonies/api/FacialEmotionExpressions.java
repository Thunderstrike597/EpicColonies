package net.kenji.epic_colonies.api;

import net.kenji.epic_colonies.EpicColoniesConfigCommon;
import net.kenji.epic_colonies.gameasset.EpicColoniesAnimations;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.types.StaticAnimation;

public enum FacialEmotionExpressions {
    HAPPY(7.25F, EpicColoniesAnimations.CITIZEN_HAPPY, true),
    NEUTRAL(4.0, EpicColoniesAnimations.CITIZEN_FACIAL_DEFAULT, true),
    SAD(2.25F, EpicColoniesAnimations.CITIZEN_SAD, true),
    UPSET(0.0F, EpicColoniesAnimations.CITIZEN_ANGRY, true);
    final double happiness;
    final boolean isBlink;
    final AnimationManager.AnimationAccessor<? extends StaticAnimation> expressionAnimation;

    FacialEmotionExpressions(double happiness, AnimationManager.AnimationAccessor<? extends StaticAnimation> animation, boolean isBlink){
        this.happiness = happiness;
        this.isBlink = isBlink;
        expressionAnimation = animation;
    }

    public static AnimationManager.AnimationAccessor<? extends StaticAnimation> getAnimationFromHappiness(double happiness){
        for (int i = 0; i < FacialEmotionExpressions.values().length; i++) {

            if(happiness >= FacialEmotionExpressions.values()[i].happiness){
                return FacialEmotionExpressions.values()[i].expressionAnimation;
            }
        }

        return NEUTRAL.expressionAnimation;
    }
}
