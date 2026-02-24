package Hoolheyak.powers;

import Hoolheyak.HoolheyakMod;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;

public class DistantLightPower extends BasePower {
    public static final String POWER_ID = HoolheyakMod.makeID("DistantLight");
    private int damageDealtThisTurn = 0;

    public DistantLightPower(AbstractCreature owner, int amount) {
        super(POWER_ID, PowerType.BUFF, false, owner, amount);
    }

    @Override
    public void stackPower(int stackAmount) {
        this.fontScale = 8.0f;
    }

    @Override
    public void onAttack(DamageInfo info, int damageAmount, AbstractCreature target) {
        if (info.type != DamageInfo.DamageType.THORNS && info.type != DamageInfo.DamageType.HP_LOSS) {
            this.damageDealtThisTurn += damageAmount;
        }
    }

    @Override
    public void atEndOfTurn(boolean isPlayer) {
        if (isPlayer) {
            if (this.damageDealtThisTurn < 6) {
                this.flash();

                // 动态获取当前触发所需的阈值层数
                int requiredStacks = 5;
                if (this.owner.hasPower(KukulkanLegacyPower.POWER_ID)) {
                    requiredStacks += 3 * this.owner.getPower(KukulkanLegacyPower.POWER_ID).amount;
                }

                // 赋予等同于阈值的层数，触发它们自带的 checkAndTrigger() 逻辑
                addToBot(new ApplyPowerAction(this.owner, this.owner, new EruditionPower(this.owner, requiredStacks), requiredStacks));
                addToBot(new ApplyPowerAction(this.owner, this.owner, new MeanderPower(this.owner, requiredStacks), requiredStacks));
            }
            this.damageDealtThisTurn = 0;
        }
    }

    @Override
    public void updateDescription() {
        this.description = DESCRIPTIONS[0];
    }
}