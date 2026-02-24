package Hoolheyak.powers;

import Hoolheyak.HoolheyakMod;
import Hoolheyak.character.Hoolheyak;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.AbstractCreature;

public class CorrectedVariablePower extends BasePower {
    public static final String POWER_ID = HoolheyakMod.makeID("CorrectedVariable");

    public CorrectedVariablePower(AbstractCreature owner, int amount) {
        super(POWER_ID, PowerType.BUFF, false, owner, amount);
    }

    @Override
    public void onCardDraw(AbstractCard card) {
        // 抽到状态牌
        if (card.type == AbstractCard.CardType.STATUS) {
            this.flash();
            addToBot(new ApplyPowerAction(this.owner, this.owner, new EruditionPower(this.owner, this.amount), this.amount));
        }
        // 抽到诅咒牌
        else if (card.type == AbstractCard.CardType.CURSE) {
            this.flash();
            addToBot(new ApplyPowerAction(this.owner, this.owner, new MeanderPower(this.owner, this.amount), this.amount));
        }
    }

    @Override
    public void updateDescription() {
        this.description = DESCRIPTIONS[0];
    }
}