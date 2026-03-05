package Hoolheyak.powers;

import Hoolheyak.HoolheyakMod;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.PowerStrings;

public class PureWaterPower extends BasePower {
    public static final String POWER_ID = HoolheyakMod.makeID("PureWater");
    private static final PowerStrings powerStrings = CardCrawlGame.languagePack.getPowerStrings(POWER_ID);

    public PureWaterPower(AbstractCreature owner) {
        super(
                POWER_ID,
                PowerType.BUFF,
                false, // 是否是按回合衰减的类型
                owner,
                -1     // amount 设为 -1 就会隐藏层数数字
        );
    }

    @Override
    public void updateDescription() {
        this.description = powerStrings.DESCRIPTIONS[0];
    }
}