package Hoolheyak.cards;

import Hoolheyak.character.Hoolheyak;
import Hoolheyak.powers.LevitatePower;
import Hoolheyak.powers.LiftPower;
import Hoolheyak.util.CardStats;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class AirflowDisturbance extends BaseCard {
    public static final String ID = makeID("AirflowDisturbance");

    private static final int COST = 1;
    private static final int MAGIC = 2; // 升力
    private static final int UPGRADE_PLUS_MAGIC = 1;

    public AirflowDisturbance() {
        super(ID, new CardStats(
                Hoolheyak.Meta.CARD_COLOR,
                CardType.SKILL,
                CardRarity.COMMON,
                CardTarget.ENEMY,
                COST
        ));
        setMagic(MAGIC, UPGRADE_PLUS_MAGIC);
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        // 提前记录打出此卡瞬间，敌人是否已经有浮空状态
        boolean alreadyLevitating = (m != null && m.hasPower(LevitatePower.POWER_ID));

        // 给予升力
        addToBot(new ApplyPowerAction(m, p, new LiftPower(m, p, magicNumber), magicNumber));

        // 如果在打出前就已经浮空，则抽2张牌
        if (alreadyLevitating) {
            addToBot(new DrawCardAction(p, 2));
        }
    }
}