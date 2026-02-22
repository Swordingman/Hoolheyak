package Hoolheyak.powers;

import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.RemoveSpecificPowerAction;
import com.megacrit.cardcrawl.core.AbstractCreature;

public class LiftPower extends BasePower {
    public static final String POWER_ID = Hoolheyak.HoolheyakMod.makeID("Lift");

    public LiftPower(AbstractCreature owner, AbstractCreature source, int amount) {
        super(POWER_ID, PowerType.DEBUFF, false, owner, source, amount);
    }

    @Override
    public void onInitialApplication() {
        // 【联动：失重】获得的升力增加100%
        if (this.owner.hasPower(WeightlessPower.POWER_ID)) {
            this.amount *= 2;
        }
        checkLevitate();
    }

    @Override
    public void stackPower(int stackAmount) {
        // 【联动：失重】
        if (this.owner.hasPower(WeightlessPower.POWER_ID)) {
            stackAmount *= 2;
        }
        super.stackPower(stackAmount);
        checkLevitate();
    }

    private boolean triggered = false;
    // 检查是否触发【浮空】
    private void checkLevitate() {
        if (triggered) return;

        if (this.owner.hasPower(GravityPower.POWER_ID)) {
            int gravity = this.owner.getPower(GravityPower.POWER_ID).amount;
            if (this.amount >= gravity) {

                triggered = true;

                addToBot(new ApplyPowerAction(
                        this.owner, this.source,
                        new LevitatePower(this.owner, this.source)
                ));

                addToBot(new RemoveSpecificPowerAction(
                        this.owner, this.source, this
                ));
            }
        }
    }

    @Override
    public void updateDescription() {
        this.description = DESCRIPTIONS[0];
    }
}