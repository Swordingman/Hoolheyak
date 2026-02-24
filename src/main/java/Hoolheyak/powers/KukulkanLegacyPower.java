package Hoolheyak.powers;

import Hoolheyak.HoolheyakMod;
import Hoolheyak.character.Hoolheyak;
import com.megacrit.cardcrawl.core.AbstractCreature;

public class KukulkanLegacyPower extends BasePower {
    public static final String POWER_ID = HoolheyakMod.makeID("KukulkanLegacy");

    public KukulkanLegacyPower(AbstractCreature owner, int amount) {
        super(POWER_ID, PowerType.BUFF, false, owner, amount);
    }

    @Override
    public void stackPower(int stackAmount) {
        this.fontScale = 8.0f;
    }

    @Override
    public void updateDescription() {
        this.description = DESCRIPTIONS[0];
    }
}