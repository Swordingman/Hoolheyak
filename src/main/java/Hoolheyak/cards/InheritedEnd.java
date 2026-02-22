package Hoolheyak.cards;

import Hoolheyak.character.Hoolheyak;
import Hoolheyak.powers.InheritedEndPower;
import Hoolheyak.util.CardStats;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class InheritedEnd extends BaseCard {
    public static final String ID = makeID("InheritedEnd");

    private static final int COST = 1;
    private static final int UPGRADED_COST = 0;

    public InheritedEnd() {
        super(ID, new CardStats(
                Hoolheyak.Meta.CARD_COLOR,
                CardType.SKILL,
                CardRarity.RARE,
                CardTarget.SELF,
                COST
        ));
        setCostUpgrade(UPGRADED_COST);
        setExhaust(true); // 消耗
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        // 获得“传承终焉”状态，持续本回合
        addToBot(new ApplyPowerAction(p, p, new InheritedEndPower(p, 1), 1));
    }
}