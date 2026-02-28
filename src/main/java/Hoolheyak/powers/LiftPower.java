package Hoolheyak.powers;

import Hoolheyak.HoolheyakMod;
import Hoolheyak.powers.phases.SextilePower;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.ReducePowerAction;
import com.megacrit.cardcrawl.actions.common.RemoveSpecificPowerAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

public class LiftPower extends BasePower {
    public static final String POWER_ID = HoolheyakMod.makeID("Lift");

    public LiftPower(AbstractCreature owner, AbstractCreature source, int amount) {
        super(POWER_ID, PowerType.DEBUFF, false, owner, source, amount);
    }

    @Override
    public void onInitialApplication() {
        // 【联动：六合】禁止获得升力
        if (AbstractDungeon.player.hasPower(SextilePower.POWER_ID)) {
            this.amount = 0; // 层数归零
            // 立即移除自身，不产生任何后续影响
            addToTop(new RemoveSpecificPowerAction(this.owner, this.owner, this));
            return;
        }

        // 【联动：失重】获得的升力增加100%
        if (this.owner.hasPower(WeightlessPower.POWER_ID)) {
            this.amount *= 2;
        }
        checkLevitate();
    }

    @Override
    public void stackPower(int stackAmount) {
        // 【联动：六合】禁止叠加升力
        if (AbstractDungeon.player.hasPower(SextilePower.POWER_ID)) {
            return;
        }

        if (this.owner.hasPower(WeightlessPower.POWER_ID)) {
            stackAmount *= 2;
        }
        super.stackPower(stackAmount);
        checkLevitate();
    }

    // 将此方法改为 public，允许重力改变时强制触发检查！
    public void checkLevitate() {
        if (this.owner.hasPower(LevitatePower.POWER_ID)) return;

        if (this.owner.hasPower(GravityPower.POWER_ID)) {
            int gravity = this.owner.getPower(GravityPower.POWER_ID).amount;
            if (this.amount >= gravity && gravity > 0) { // 加一个 gravity > 0 兜底，防止重力为 0 时报错
                addToTop(new ApplyPowerAction(this.owner, this.source, new LevitatePower(this.owner, this.source)));
                addToTop(new ReducePowerAction(this.owner, this.source, this, gravity));
            }
        }
    }

    @Override
    public void updateDescription() {
        this.description = DESCRIPTIONS[0];
    }
}