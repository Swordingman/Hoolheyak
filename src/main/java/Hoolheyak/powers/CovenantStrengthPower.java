package Hoolheyak.powers;

import Hoolheyak.HoolheyakMod;
import Hoolheyak.character.Hoolheyak;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.StrengthPower;

public class CovenantStrengthPower extends BasePower {
    public static final String POWER_ID = HoolheyakMod.makeID("CovenantStrength");

    public CovenantStrengthPower(AbstractCreature owner, int amount) {
        super(POWER_ID, PowerType.BUFF, false, owner, amount);
    }

    // 提供给 EruditionPower 调用的触发器
    public static void trigger(AbstractCreature owner) {
        if (owner.hasPower(POWER_ID)) {
            int amt = owner.getPower(POWER_ID).amount;
            owner.getPower(POWER_ID).flash();
            com.megacrit.cardcrawl.dungeons.AbstractDungeon.actionManager.addToBottom(
                    new ApplyPowerAction(owner, owner, new StrengthPower(owner, amt), amt)
            );
        }
    }

    @Override
    public void updateDescription() {
        this.description = DESCRIPTIONS[0] + this.amount + DESCRIPTIONS[1];
    }
}