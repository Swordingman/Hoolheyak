package Hoolheyak.cards;

import Hoolheyak.character.Hoolheyak;
import Hoolheyak.powers.WeightlessPower;
import Hoolheyak.util.CardStats;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class Rapids extends BaseCard {
    public static final String ID = makeID("Rapids");

    private static final int COST = 1;
    private static final int UPGRADED_COST = 0; // 升级变 0 费

    public Rapids() {
        super(ID, new CardStats(
                Hoolheyak.Meta.CARD_COLOR,
                CardType.SKILL,
                CardRarity.UNCOMMON,
                CardTarget.ENEMY,
                COST
        ));
        setCostUpgrade(UPGRADED_COST);
        this.exhaust = true; // 消耗
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new ApplyPowerAction(m, p, new WeightlessPower(m, p, 99), 99));
    }
}