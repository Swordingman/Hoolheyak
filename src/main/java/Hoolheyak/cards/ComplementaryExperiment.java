package Hoolheyak.cards;

import Hoolheyak.character.Hoolheyak;
import Hoolheyak.powers.ComplementaryExperimentPower;
import Hoolheyak.util.CardStats;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class ComplementaryExperiment extends BaseCard {
    public static final String ID = makeID("ComplementaryExperiment");

    private static final int COST = 1;
    private static final int MAGIC = 2; // 给2格挡
    private static final int UPGRADE_PLUS_MAGIC = 1; // 升级给3格挡

    public ComplementaryExperiment() {
        super(ID, new CardStats(
                Hoolheyak.Meta.CARD_COLOR,
                CardType.POWER,
                CardRarity.UNCOMMON,
                CardTarget.SELF,
                COST
        ));
        setMagic(MAGIC, UPGRADE_PLUS_MAGIC);
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new ApplyPowerAction(p, p, new ComplementaryExperimentPower(p, magicNumber), magicNumber));
    }
}