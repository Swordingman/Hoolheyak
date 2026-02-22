package Hoolheyak.cards;

import Hoolheyak.character.Hoolheyak;
import Hoolheyak.powers.BygoneWingsPower;
import Hoolheyak.util.CardStats;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class BygoneWings extends BaseCard {
    public static final String ID = makeID("BygoneWings");

    private static final int COST = 2;
    private static final int UPGRADED_COST = 1;

    public BygoneWings() {
        super(ID, new CardStats(
                Hoolheyak.Meta.CARD_COLOR,
                CardType.POWER,
                CardRarity.UNCOMMON,
                CardTarget.SELF,
                COST
        ));
        setCostUpgrade(UPGRADED_COST);
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new ApplyPowerAction(p, p, new BygoneWingsPower(p, 1), 1));
    }
}