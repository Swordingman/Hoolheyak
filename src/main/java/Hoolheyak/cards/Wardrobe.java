package Hoolheyak.cards;

import Hoolheyak.character.Hoolheyak;
import Hoolheyak.powers.AnalysisPower;
import Hoolheyak.powers.WardrobePower;
import Hoolheyak.util.CardStats;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class Wardrobe extends BaseCard {
    public static final String ID = makeID("Wardrobe");

    private static final int COST = 2;
    private static final int UPGRADED_COST = 1;

    public Wardrobe() {
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
        // 瞬间结算当前的解析层数
        int amt = p.hasPower(AnalysisPower.POWER_ID) ? p.getPower(AnalysisPower.POWER_ID).amount : 0;

        if (amt > 0) {
            addToBot(new ApplyPowerAction(p, p, new WardrobePower(p, amt), amt));
        }
    }
}