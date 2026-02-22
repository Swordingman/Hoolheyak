package Hoolheyak.cards;

import Hoolheyak.actions.VariableAction;
import Hoolheyak.character.Hoolheyak;
import Hoolheyak.powers.ForbiddenKnowledgeEruPower;
import Hoolheyak.powers.ForbiddenKnowledgeMeaPower;
import Hoolheyak.util.CardStats;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

import java.util.ArrayList;

public class ForbiddenKnowledge extends BaseCard {
    public static final String ID = makeID("ForbiddenKnowledge");

    private static final int COST = 1;

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
    public void use(AbstractPlayer p, AbstractMonster m) {
        ArrayList<VariableAction.VariableChoice> choices = new ArrayList<>();

        // 选项 α：打出能力牌加博览
        choices.add(new VariableAction.VariableChoice(cardStrings.EXTENDED_DESCRIPTION[0], () -> {
            addToBot(new ApplyPowerAction(p, p, new ForbiddenKnowledgeEruPower(p, 2), 2));
        }));

        // 选项 β：打出能力牌加逶迤
        choices.add(new VariableAction.VariableChoice(cardStrings.EXTENDED_DESCRIPTION[1], () -> {
            addToBot(new ApplyPowerAction(p, p, new ForbiddenKnowledgeMeaPower(p, 2), 2));
        }));

        addToBot(new VariableAction(this, choices));
    }
}