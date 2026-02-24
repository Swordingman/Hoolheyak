package Hoolheyak.powers;

import Hoolheyak.HoolheyakMod;
import Hoolheyak.character.Hoolheyak;
import com.megacrit.cardcrawl.core.AbstractCreature;

public class OmniscientHorizonPower extends BasePower {
    public static final String POWER_ID = HoolheyakMod.makeID("OmniscientHorizon");

    public OmniscientHorizonPower(AbstractCreature owner, int amount) {
        super(POWER_ID, PowerType.BUFF, false, owner, amount);
    }

    @Override
    public void updateDescription() {
        int currentReward = this.amount * 2;
        this.description = DESCRIPTIONS[0] + currentReward + DESCRIPTIONS[1] + currentReward + DESCRIPTIONS[2];
    }
}