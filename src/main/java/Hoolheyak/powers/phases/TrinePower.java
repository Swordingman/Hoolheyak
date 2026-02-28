package Hoolheyak.powers.phases;

import Hoolheyak.powers.BasePower;
import Hoolheyak.HoolheyakMod;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.actions.common.GainEnergyAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.AbstractCreature;

public class TrinePower extends BasePower {
    public static final String POWER_ID = HoolheyakMod.makeID("Trine");

    public TrinePower(AbstractCreature owner) {
        super(POWER_ID, PowerType.BUFF, false, owner, -1);
    }

    @Override
    public void atStartOfTurnPostDraw() {
        this.flash();
        addToBot(new GainEnergyAction(1));
        addToBot(new DrawCardAction(this.owner, 2));
    }

    @Override
    public void onUseCard(AbstractCard card, UseCardAction action) {
        // 如果打出的是攻击牌，强行让该 Action 的消耗标签变为 true
        if (card.type == AbstractCard.CardType.ATTACK) {
            this.flash();
            action.exhaustCard = true;
        }
    }

    @Override
    public void updateDescription() {
        this.description = DESCRIPTIONS[0];
    }
}