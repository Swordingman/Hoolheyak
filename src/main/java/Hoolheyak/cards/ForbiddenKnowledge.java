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
    public void use(AbstractPlayer p, AbstractMonster m) {
        ArrayList<VariableAction.VariableChoice> choices = new ArrayList<>();

        // 选项 α：当你触发博览时，额外抽 1 张牌
        choices.add(new VariableAction.VariableChoice(cardStrings.EXTENDED_DESCRIPTION[0], () -> {
            addToBot(new ApplyPowerAction(p, p, new ForbiddenKnowledgeEruPower(p, DRAW_AMOUNT), DRAW_AMOUNT));
        }));

        // 选项 β：当你触发逶迤时，对所有敌人造成 5 点伤害
        choices.add(new VariableAction.VariableChoice(cardStrings.EXTENDED_DESCRIPTION[1], () -> {
            addToBot(new ApplyPowerAction(p, p, new ForbiddenKnowledgeMeaPower(p, AOE_DAMAGE), AOE_DAMAGE));
        }));

        addToBot(new VariableAction(this, choices, true));
    }
}