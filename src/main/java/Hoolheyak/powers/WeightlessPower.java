package Hoolheyak.powers;

import com.megacrit.cardcrawl.actions.common.ReducePowerAction;
import com.megacrit.cardcrawl.core.AbstractCreature;

public class WeightlessPower extends BasePower {
    public static final String POWER_ID = Hoolheyak.HoolheyakMod.makeID("Weightless");

    public WeightlessPower(AbstractCreature owner, AbstractCreature source, int amount) {
        super(POWER_ID, PowerType.DEBUFF, true, owner, source, amount);
    }

    @Override
    public void atEndOfRound() {
        // 回合结束时层数 -1
        addToBot(new ReducePowerAction(this.owner, this.owner, this, 1));
    }

    @Override
    public void updateDescription() {
        this.description = DESCRIPTIONS[0];
    }
}