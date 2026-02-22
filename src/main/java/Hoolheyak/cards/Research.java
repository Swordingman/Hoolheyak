package Hoolheyak.cards;

import Hoolheyak.character.Hoolheyak;
import Hoolheyak.powers.MeanderPower;
import Hoolheyak.util.CardStats;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class Research extends BaseCard {
    public static final String ID = makeID("Research");
    private static final int COST = 1;
    private static final int DRAW = 2;
    private static final int DRAW_UPG = 1;

    public Research() {
        super(ID, new CardStats(
                Hoolheyak.Meta.CARD_COLOR,
                CardType.SKILL,
                CardRarity.COMMON,
                CardTarget.SELF,
                COST
        ));

        setMagic(DRAW, DRAW_UPG);
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new DrawCardAction(p, magicNumber));
        addToBot(new ApplyPowerAction(p, p, new MeanderPower(p, 1), 1));
    }
}