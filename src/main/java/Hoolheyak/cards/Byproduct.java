package Hoolheyak.cards;

import Hoolheyak.character.Hoolheyak;
import Hoolheyak.powers.ByproductPower;
import Hoolheyak.util.CardStats;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class Byproduct extends BaseCard {
    public static final String ID = makeID("Byproduct");

    private static final int COST = 1;

    public Byproduct() {
        super(ID, new CardStats(
                Hoolheyak.Meta.CARD_COLOR,
                CardType.POWER,
                CardRarity.RARE,
                CardTarget.SELF,
                COST
        ));
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        // 将是否升级过的状态传递给能力，决定生成的牌是否升级
        addToBot(new ApplyPowerAction(p, p, new ByproductPower(p, 1, this.upgraded), 1));
    }
}