package Hoolheyak.powers;

import Hoolheyak.HoolheyakMod;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.RitualPower;

public class SerpentBloodlinePower extends BasePower {
    public static final String POWER_ID = HoolheyakMod.makeID("SerpentBloodline");

    public SerpentBloodlinePower(AbstractCreature owner, int amount) {
        super(POWER_ID, PowerType.BUFF, false, owner, amount);
    }

    // 将打出时立刻结算仪式的逻辑封装，方便复用
    private void triggerImmediateRitual() {
        if (this.owner.hasPower(AnalysisPower.POWER_ID)) {
            int analysisAmt = this.owner.getPower(AnalysisPower.POWER_ID).amount;
            if (analysisAmt > 0) {
                this.flash();
                addToBot(new ApplyPowerAction(this.owner, this.owner, new RitualPower(this.owner, analysisAmt, true), analysisAmt));
            }
        }
    }

    @Override
    public void onInitialApplication() {
        // 第一次打出时，结算当前解析，并扣除1点能量上限
        triggerImmediateRitual();
        AbstractDungeon.player.energy.energyMaster -= this.amount;
    }

    @Override
    public void stackPower(int stackAmount) {
        super.stackPower(stackAmount);

        // 【关键修复1】：如果玩家通过某种方式（比如双发）打出了第二张，
        // 必须立刻再给他算一次当前解析对应的仪式，不能让他白白掉能量！
        triggerImmediateRitual();

        // 继续扣除对应的能量上限
        AbstractDungeon.player.energy.energyMaster -= stackAmount;
    }

    @Override
    public void onRemove() {
        // 战斗结束或被清除时，归还所有扣除的能量上限
        AbstractDungeon.player.energy.energyMaster += this.amount;
    }

    @Override
    public void onApplyPower(AbstractPower power, AbstractCreature target, AbstractCreature source) {
        if (power.ID.equals(AnalysisPower.POWER_ID) && target == this.owner) {
            this.flash();

            // 【关键修复2】：获得的仪式数量，必须乘以这个血脉能力的层数！
            // 比如你有2层羽蛇血脉，获得了1层解析，这里就会直接给你 1 * 2 = 2层仪式！
            int ritualToGain = power.amount * this.amount;

            addToBot(new ApplyPowerAction(this.owner, this.owner, new RitualPower(this.owner, ritualToGain, true), ritualToGain));
        }
    }

    @Override
    public void updateDescription() {
        // 文本可以写成：
        // DESCRIPTIONS[0] -> "获得等同于 解析 层数的 仪式 。每次获得 解析 时，获得 "
        // DESCRIPTIONS[1] -> " 倍层数的 仪式 。 NL 每回合开始时，失去 #b"
        // DESCRIPTIONS[2] -> " 点能量。"
        this.description = DESCRIPTIONS[0] + this.amount + DESCRIPTIONS[1] + this.amount + DESCRIPTIONS[2];
    }
}