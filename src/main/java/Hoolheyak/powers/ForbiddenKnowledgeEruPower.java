package Hoolheyak.powers;

import Hoolheyak.HoolheyakMod;
import Hoolheyak.character.Hoolheyak;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.AbstractCreature;

public class ForbiddenKnowledgeEruPower extends BasePower {
    public static final String POWER_ID = HoolheyakMod.makeID("ForbiddenKnowledgeEru");

    public ForbiddenKnowledgeEruPower(AbstractCreature owner, int amount) {
        super(POWER_ID, PowerType.BUFF, false, owner, amount);
    }

    @Override
    public void onUseCard(AbstractCard card, UseCardAction action) {
        if (card.type == AbstractCard.CardType.POWER) {
            this.flash();
            addToBot(new ApplyPowerAction(this.owner, this.owner, new EruditionPower(this.owner, this.amount), this.amount));
        }
    }

    @Override
    public void updateDescription() {
        this.description = DESCRIPTIONS[0] + this.amount + DESCRIPTIONS[1];
    }
}