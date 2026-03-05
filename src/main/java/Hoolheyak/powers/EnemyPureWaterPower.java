package Hoolheyak.powers;

import Hoolheyak.HoolheyakMod;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.PowerStrings;

public class EnemyPureWaterPower extends BasePower {
    public static final String POWER_ID = HoolheyakMod.makeID("EnemyPureWater");
    private static final PowerStrings powerStrings = CardCrawlGame.languagePack.getPowerStrings(POWER_ID);

    public EnemyPureWaterPower(AbstractCreature owner) {
        super(
                POWER_ID,
                PowerType.BUFF,
                false, // 不按回合衰减
                owner,
                -1     // 隐藏层数
        );
    }

    @Override
    public void updateDescription() {
        this.description = powerStrings.DESCRIPTIONS[0];
    }
}