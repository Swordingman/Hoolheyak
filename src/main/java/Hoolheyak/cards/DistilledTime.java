package Hoolheyak.cards;

import Hoolheyak.character.Hoolheyak;
import Hoolheyak.powers.DistilledTimePower;
import Hoolheyak.util.CardStats;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class DistilledTime extends BaseCard {
    public static final String ID = makeID("DistilledTime");

    private static final int COST = 3;
    private static final int UPGRADED_COST = 2;
    private static final int MAGIC = 1;

    public DistilledTime() {
        super(ID, new CardStats(
                Hoolheyak.Meta.CARD_COLOR,
                CardType.POWER,
                CardRarity.RARE,
                CardTarget.SELF,
                COST
        ));
        setCostUpgrade(UPGRADED_COST);
        setMagic(MAGIC);
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new ApplyPowerAction(p, p, new DistilledTimePower(p, this.magicNumber), this.magicNumber));
    }
}