package Hoolheyak.powers;

import com.megacrit.cardcrawl.core.AbstractCreature;

public class AnalysisPower extends BasePower {
    public static final String POWER_ID = Hoolheyak.HoolheyakMod.makeID("Analysis");

    public AnalysisPower(AbstractCreature owner, int amount) {
        super(
                POWER_ID,
                PowerType.BUFF,
                false,
                owner,
                amount
        );
    }

    @Override
    public void updateDescription() {
        this.description = DESCRIPTIONS[0];
    }
}