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
        triggerImmediateRitual();
        // 彻底删掉这里关于 energyMaster 和 loseEnergy 的代码！
    }

    @Override
    public void stackPower(int stackAmount) {
        super.stackPower(stackAmount);
        triggerImmediateRitual();
        // 彻底删掉这里关于 energyMaster 和 loseEnergy 的代码！
    }

    // 【新增这个钩子】：这是杀戮尖塔专用的能量重置钩子
    // 每次回合开始，游戏给你回满能量后，立刻触发这里
    @Override
    public void onEnergyRecharge() {
        this.flash(); // 让能力的图标闪烁一下，提醒玩家“我扣你费了”
        AbstractDungeon.player.loseEnergy(this.amount); // 直接从当前能量池扣除层数对应的能量
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
        this.description = DESCRIPTIONS[0] + this.amount + DESCRIPTIONS[1] + this.amount + DESCRIPTIONS[2];
    }
}