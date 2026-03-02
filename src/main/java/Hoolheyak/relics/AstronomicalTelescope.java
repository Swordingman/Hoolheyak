package Hoolheyak.relics;

import Hoolheyak.HoolheyakMod;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndObtainEffect;
import java.util.ArrayList;

public class AstronomicalTelescope extends BaseRelic {
    public static final String ID = HoolheyakMod.makeID("AstronomicalTelescope");

    public AstronomicalTelescope() {
        super(ID, "AstronomicalTelescope", RelicTier.UNCOMMON, LandingSound.MAGICAL);
    }

    @Override
    public void onEquip() {
        ArrayList<AbstractCard> transformableCards = new ArrayList<>();

        for (AbstractCard c : AbstractDungeon.player.masterDeck.group) {
            if (c.hasTag(AbstractCard.CardTags.STARTER_STRIKE) || c.hasTag(AbstractCard.CardTags.STARTER_DEFEND)) {
                transformableCards.add(c);
            }
        }

        if (!transformableCards.isEmpty()) {
            // 随机挑选一张基础牌移除
            AbstractCard target = transformableCards.get(AbstractDungeon.cardRandomRng.random(transformableCards.size() - 1));
            AbstractDungeon.player.masterDeck.removeCard(target);

            // 获得随机无色牌
            AbstractCard newCard = AbstractDungeon.returnColorlessCard(AbstractCard.CardRarity.UNCOMMON).makeCopy();
            AbstractDungeon.effectsQueue.add(new ShowCardAndObtainEffect(newCard, Settings.WIDTH / 2.0F, Settings.HEIGHT / 2.0F));
        } else {
            // 没有基础牌，获得稀有无色牌
            AbstractCard newCard = AbstractDungeon.returnColorlessCard(AbstractCard.CardRarity.RARE).makeCopy();
            AbstractDungeon.effectsQueue.add(new ShowCardAndObtainEffect(newCard, Settings.WIDTH / 2.0F, Settings.HEIGHT / 2.0F));
        }
    }
}