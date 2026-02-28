package Hoolheyak.cards;

import Hoolheyak.character.Hoolheyak;
import Hoolheyak.util.CardStats;
import com.megacrit.cardcrawl.actions.common.GainEnergyAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

import java.util.ArrayList;

public class ContingencyPlan extends BaseCard {
    public static final String ID = makeID("ContingencyPlan");

    private static final int COST = 0;

    public ContingencyPlan() {
        super(ID, new CardStats(
                Hoolheyak.Meta.CARD_COLOR,
                CardType.SKILL,
                CardRarity.UNCOMMON,
                CardTarget.NONE,
                COST
        ));
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new GainEnergyAction(1));
    }

    // 提供给 逶迤/博览 呼叫的静态监听方法
    public static void returnFromDiscard(boolean isEruditionTrigger) {
        AbstractPlayer p = AbstractDungeon.player;
        if (p == null || p.discardPile == null) return;

        ArrayList<AbstractCard> cardsToMove = new ArrayList<>();

        for (AbstractCard c : p.discardPile.group) {
            if (c.cardID.equals(ID)) {
                // 如果是 逶迤 触发，无条件返回。
                // 如果是 博览 触发，要求这张牌必须是升级版 (upgraded) 才返回。
                if (!isEruditionTrigger || c.upgraded) {
                    cardsToMove.add(c);
                }
            }
        }

        // 把符合条件的牌拿回手牌
        for (AbstractCard c : cardsToMove) {
            c.lighten(true);
            c.unhover();
            p.discardPile.moveToHand(c, p.discardPile);
        }
    }
}