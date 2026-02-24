package Hoolheyak.powers;

import Hoolheyak.HoolheyakMod;
import Hoolheyak.character.Hoolheyak;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInHandAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.CardLibrary;

import java.util.ArrayList;

public class EntropyIncreasePower extends BasePower {
    public static final String POWER_ID = HoolheyakMod.makeID("EntropyIncrease");

    public EntropyIncreasePower(AbstractCreature owner, int amount) {
        super(POWER_ID, PowerType.BUFF, false, owner, amount);
    }

    @Override
    public void atStartOfTurnPostDraw() {
        this.flash();
        for (int i = 0; i < this.amount; i++) {
            ArrayList<AbstractCard> powerCards = new ArrayList<>();
            for (AbstractCard c : CardLibrary.getAllCards()) {
                if (c.type == AbstractCard.CardType.POWER) {
                    powerCards.add(c);
                }
            }

            if (!powerCards.isEmpty()) {
                AbstractCard randomPower = powerCards.get(AbstractDungeon.cardRandomRng.random(powerCards.size() - 1)).makeCopy();
                // 也可以根据需要让它本回合变0费： randomPower.setCostForTurn(0);
                addToBot(new MakeTempCardInHandAction(randomPower, 1));
            }
        }
    }

    @Override
    public void updateDescription() {
        this.description = DESCRIPTIONS[0] + this.amount + DESCRIPTIONS[1];
    }
}