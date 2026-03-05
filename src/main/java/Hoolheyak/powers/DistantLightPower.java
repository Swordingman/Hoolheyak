package Hoolheyak.powers;

import Hoolheyak.HoolheyakMod;
import Hoolheyak.actions.TriggerKeywordAction;
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

                addToBot(new TriggerKeywordAction(this.owner, TriggerKeywordAction.KeywordType.ERUDITION, 1));
                addToBot(new TriggerKeywordAction(this.owner, TriggerKeywordAction.KeywordType.MEANDER, 1));
            }
            this.damageDealtThisTurn = 0;
        }
    }

    @Override
    public void updateDescription() {
        this.description = DESCRIPTIONS[0];
    }
}