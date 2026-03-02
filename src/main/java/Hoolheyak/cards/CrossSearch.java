package Hoolheyak.cards;

import Hoolheyak.character.Hoolheyak;
import Hoolheyak.actions.CrossSearchAction;
import Hoolheyak.util.CardStats;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class CrossSearch extends BaseCard {
    public static final String ID = makeID("CrossSearch");

    private static final int COST = 0;
    private static final int MAGIC = 2; // 抽 2
    private static final int UPGRADE_PLUS_MAGIC = 1; // 升级抽 3

    public CrossSearch() {
        super(ID, new CardStats(
                Hoolheyak.Meta.CARD_COLOR,
                CardType.SKILL,
                CardRarity.COMMON,
                CardTarget.NONE,
                COST
        ));
        setMagic(MAGIC, UPGRADE_PLUS_MAGIC);
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        // 先抽牌
        addToBot(new DrawCardAction(p, this.magicNumber));
        // 再呼叫专属的丢牌判定动作
        addToBot(new CrossSearchAction());
    }
}