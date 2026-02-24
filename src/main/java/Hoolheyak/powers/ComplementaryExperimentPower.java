package Hoolheyak.powers;

import Hoolheyak.HoolheyakMod;
import Hoolheyak.character.Hoolheyak;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.core.AbstractCreature;

public class ComplementaryExperimentPower extends BasePower {
    public static final String POWER_ID = HoolheyakMod.makeID("ComplementaryExperiment");

    public ComplementaryExperimentPower(AbstractCreature owner, int amount) {
        super(POWER_ID, PowerType.BUFF, false, owner, amount);
    }

    // 由 VariableChoiceCard 调用的触发器
    public void onVariableTriggered() {
        this.flash();
        addToBot(new GainBlockAction(this.owner, this.owner, this.amount));
    }

    @Override
    public void updateDescription() {
        this.description = DESCRIPTIONS[0] + this.amount + DESCRIPTIONS[1];
    }
}