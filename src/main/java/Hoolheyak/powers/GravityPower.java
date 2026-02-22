package Hoolheyak.powers;

import com.megacrit.cardcrawl.core.AbstractCreature;

public class GravityPower extends BasePower {
    public static final String POWER_ID = Hoolheyak.HoolheyakMod.makeID("Gravity");

    public GravityPower(AbstractCreature owner) {
        super(
                POWER_ID,
                PowerType.BUFF,
                false,
                owner,
                0
        );
    }

    @Override
    public void onInitialApplication() {
        this.amount = this.owner.maxHealth / 10;
        updateDescription();
    }

    @Override
    public void updateDescription() {
        this.description = DESCRIPTIONS[0] + this.amount + DESCRIPTIONS[1];
    }
}