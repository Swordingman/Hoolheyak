package Hoolheyak.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.screens.SingleCardViewPopup;
import basemod.ReflectionHacks;
import Hoolheyak.cards.BaseCard;

@SpirePatch(
        clz = SingleCardViewPopup.class,
        method = "update"
)
public class SingleCardViewCarouselPatch {

    @SpirePostfixPatch
    public static void fixCarousel(SingleCardViewPopup __instance) {
        AbstractCard card = ReflectionHacks.getPrivate(__instance, SingleCardViewPopup.class, "card");

        if (card instanceof BaseCard) {
            ((BaseCard) card).updateCarouselLogic();
        }
    }
}