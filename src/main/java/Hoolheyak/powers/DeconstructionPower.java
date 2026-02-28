package Hoolheyak.powers;

import Hoolheyak.HoolheyakMod;
import com.megacrit.cardcrawl.core.AbstractCreature;

public class DeconstructionPower extends BasePower {
    public static final String POWER_ID = HoolheyakMod.makeID("Deconstruction");

    public DeconstructionPower(AbstractCreature owner, int amount) {
        super(POWER_ID, PowerType.DEBUFF, false, owner, amount);
    }

    @Override
    public void updateDescription() {
        this.description = DESCRIPTIONS[0] + this.amount + DESCRIPTIONS[1];
    }
}