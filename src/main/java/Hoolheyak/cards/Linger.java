package Hoolheyak.cards;

import Hoolheyak.character.Hoolheyak;
import Hoolheyak.util.CardStats;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class Linger extends BaseCard{
    public static final String ID = makeID("Linger");

    private static final int COST = 1;
    private static final int BLOCK = 7;
    private static final int DRAW = 1;
    private static final int DRAW_UPG = 1;

    public Linger() {
        super(ID, new CardStats(
                Hoolheyak.Meta.CARD_COLOR,
                CardType.SKILL,
                CardRarity.BASIC,
                CardTarget.SELF,
                COST
        ));

        setBlock(BLOCK);
        setMagic(DRAW, DRAW_UPG);
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new GainBlockAction(p, p, block));
        addToBot(new DrawCardAction(p, this.magicNumber));
    }
}
