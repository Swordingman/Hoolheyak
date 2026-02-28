package Hoolheyak.cards;

import Hoolheyak.actions.RepeatAction;
import Hoolheyak.character.Hoolheyak;
import Hoolheyak.util.CardStats;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class TransitionalFlow extends BaseCard{
    public static final String ID = makeID("TransitionalFlow");

    private static final int COST = 1;
    private static final int BLOCK = 4;
    private static final int BLOCK_UPG = 2;

    public TransitionalFlow() {
        super(ID, new CardStats(
                Hoolheyak.Meta.CARD_COLOR,
                CardType.SKILL,
                CardRarity.COMMON,
                CardTarget.SELF,
                COST
        ));

        setBlock(BLOCK, BLOCK_UPG);
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new GainBlockAction(p, p, block));
        addToBot(new RepeatAction(this, m, 1));
    }
}
