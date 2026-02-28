package Hoolheyak.cards;

import Hoolheyak.character.Hoolheyak;
import Hoolheyak.powers.EruditionPower;
import Hoolheyak.powers.MeanderPower;
import Hoolheyak.util.CardStats;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class Foreshadowing extends BaseCard {
    public static final String ID = makeID("Foreshadowing");

    private static final int COST = 2;
    private static final int BLOCK = 10;
    private static final int BLOCK_UPG = 2;
    private static final int ERUDITION = 2;
    private static final int ERUDITION_UPG = 1;
    private static final int MEANDER = 1;
    private static final int MEANDER_UPG = 1;

    public Foreshadowing() {
        super(ID, new CardStats(
                Hoolheyak.Meta.CARD_COLOR,
                CardType.SKILL,
                CardRarity.COMMON,
                CardTarget.SELF,
                COST
        ));
        setBlock(BLOCK, BLOCK_UPG);
        setMagic(ERUDITION, ERUDITION_UPG);
        setCustomVar("MEANDER", MEANDER, MEANDER_UPG);
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new GainBlockAction(p, p, block));
        addToBot(new ApplyPowerAction(p, p, new EruditionPower(p, magicNumber), magicNumber));
        addToBot(new ApplyPowerAction(p, p, new MeanderPower(p, customVar("MEANDER")), customVar("MEANDER")));
    }
}