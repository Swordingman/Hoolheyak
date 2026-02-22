package Hoolheyak.cards;

import Hoolheyak.character.Hoolheyak;
import Hoolheyak.util.CardStats;
import com.megacrit.cardcrawl.actions.unique.RetainCardsAction;
import com.megacrit.cardcrawl.actions.utility.NewQueueCardAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class ParameterCorrection extends BaseCard {
    public static final String ID = makeID("ParameterCorrection");

    private static final int COST = -2; // 不能被打出
    private static final int MAGIC = 1; // 保留 1 张
    private static final int UPGRADE_PLUS_MAGIC = 1;

    public ParameterCorrection() {
        super(ID, new CardStats(
                Hoolheyak.Meta.CARD_COLOR,
                CardType.SKILL,
                CardRarity.UNCOMMON,
                CardTarget.NONE,
                COST
        ));
        setMagic(MAGIC, UPGRADE_PLUS_MAGIC);
        setExhaust(true); // 被打出时会消耗
    }

    // 阻止玩家手动打出
    @Override
    public boolean canUse(AbstractPlayer p, AbstractMonster m) {
        return false;
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        // 卡牌自带 Exhaust，被打出时什么都不用做，直接消耗掉即可。
        // 如果它被“视为打出”，它依然可以触发“打出技能牌”的遗物和能力（如逶迤）。
    }

    // 在手牌中结束回合时，触发保留效果
    @Override
    public void triggerOnEndOfTurnForPlayingCard() {
        addToBot(new RetainCardsAction(AbstractDungeon.player, this.magicNumber));
    }

    // 当被手动丢弃时，视为被打出
    @Override
    public void triggerOnManualDiscard() {
        // 从弃牌堆中将它拉出来，排入行动队列打出
        if (AbstractDungeon.player.discardPile.contains(this)) {
            AbstractDungeon.player.discardPile.removeCard(this);
        }
        addToBot(new NewQueueCardAction(this, true, true, true));
    }
}