package Hoolheyak.relics;

import Hoolheyak.HoolheyakMod;
import com.megacrit.cardcrawl.actions.common.HealAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

public class CocktailShaker extends BaseRelic {
    public static final String ID = HoolheyakMod.makeID("CocktailShaker");

    public CocktailShaker() {
        super(ID, "CocktailShaker", RelicTier.UNCOMMON, LandingSound.CLINK);
    }

    @Override
    public void onEquip() {
        AbstractDungeon.player.increaseMaxHp(3, true);
    }

    @Override
    public void onUseCard(AbstractCard card, UseCardAction action) {
        // 判断卡牌颜色是否与当前角色的卡牌颜色不同（无色牌也会触发）
        if (card.color != AbstractDungeon.player.getCardColor()) {
            this.flash();
            addToBot(new HealAction(AbstractDungeon.player, AbstractDungeon.player, 1));
        }
    }
}