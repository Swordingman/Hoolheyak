package Hoolheyak.powers.phases;

import Hoolheyak.powers.BasePower;
import Hoolheyak.HoolheyakMod;
import com.megacrit.cardcrawl.core.AbstractCreature;

public class SextilePower extends BasePower {
    public static final String POWER_ID = HoolheyakMod.makeID("Sextile");

    public SextilePower(AbstractCreature owner) {
        super(POWER_ID, PowerType.BUFF, false, owner, -1);
    }

    @Override
    public void updateDescription() {
        this.description = DESCRIPTIONS[0];
    }
}