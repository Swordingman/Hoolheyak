package Hoolheyak.cards;

import Hoolheyak.actions.VariableAction;
import Hoolheyak.character.Hoolheyak;
import Hoolheyak.powers.ForbiddenKnowledgeEruPower;
import Hoolheyak.powers.ForbiddenKnowledgeMeaPower;
import Hoolheyak.util.CardStats;
import Hoolheyak.util.IVariableCard;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

import java.util.ArrayList;

public class ForbiddenKnowledge extends BaseCard implements IVariableCard {
    public static final String ID = makeID("ForbiddenKnowledge");
    private static final int COST = 1;

    // 设定新机制的数值
    private static final int DRAW_AMOUNT = 1;
    private static final int AOE_DAMAGE = 5;

    public ForbiddenKnowledge() {
        super(ID, new CardStats(
                Hoolheyak.Meta.CARD_COLOR,
                CardType.POWER,
                CardRarity.UNCOMMON,
                CardTarget.SELF,
                COST
        ));
        setInnate(false, true); // 升级后固有
    }

    @Override
    public ArrayList<VariableAction.VariableChoice> getVariableChoices(AbstractPlayer p, AbstractMonster m, boolean isAutoTriggered) {
        ArrayList<VariableAction.VariableChoice> choices = new ArrayList<>();
        choices.add(new VariableAction.VariableChoice(cardStrings.EXTENDED_DESCRIPTION[0], () -> {
            addToBot(new ApplyPowerAction(p, p, new ForbiddenKnowledgeEruPower(p, DRAW_AMOUNT), DRAW_AMOUNT));
        }));

        choices.add(new VariableAction.VariableChoice(cardStrings.EXTENDED_DESCRIPTION[1], () -> {
            addToBot(new ApplyPowerAction(p, p, new ForbiddenKnowledgeMeaPower(p, AOE_DAMAGE), AOE_DAMAGE));
        }));

        return choices;
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new VariableAction(this, getVariableChoices(p, m), true));
    }
}