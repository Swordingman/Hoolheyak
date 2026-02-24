package Hoolheyak.powers;

import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.ReducePowerAction;
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

    // 检查是否触发【浮空】
    private void checkLevitate() {
        // 【关键逻辑】：如果目标已经处于浮空状态，就不再重复触发。
        // 多出来的升力会安全地保留在怪物身上，等到回合结束时配合重力造成大量真实伤害！
        if (this.owner.hasPower(LevitatePower.POWER_ID)) {
            return;
        }

        if (this.owner.hasPower(GravityPower.POWER_ID)) {
            int gravity = this.owner.getPower(GravityPower.POWER_ID).amount;
            if (this.amount >= gravity) {

                // 【关键修复】：使用 addToTop，让浮空的结算强制“插队”到当前攻击循环中间！
                // 注意：addToTop 是“后进先出”（栈）的结构，所以最先执行的代码要写在最后面。

                // 2. 然后执行：赋予浮空状态（此时怪物的格挡会被清空）
                addToTop(new ApplyPowerAction(this.owner, this.source, new LevitatePower(this.owner, this.source)));

                // 1. 最先执行：扣除用于触发浮空的重力等量层数。
                // 这样写能完美保留溢出的升力。如果扣减后层数归 0，ReducePowerAction 底层会自动移除 LiftPower，无需手写 Remove。
                addToTop(new ReducePowerAction(this.owner, this.source, this, gravity));
            }
        }
    }

    @Override
    public void updateDescription() {
        this.description = DESCRIPTIONS[0];
    }
}