package Hoolheyak.cards;

import Hoolheyak.actions.StargazingAction;
import Hoolheyak.actions.VariableAction;
import Hoolheyak.character.Hoolheyak;
import Hoolheyak.util.CardStats;
import Hoolheyak.util.IVariableCard;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

import java.util.ArrayList;

public class Linger extends BaseCard implements IVariableCard {
    public static final String ID = makeID("Linger");

    private static final int COST = 1;
    private static final int BLOCK = 6;
    private static final int LOOK_AMOUNT = 1;
    private static final int UPG_LOOK_AMOUNT = 1; // 升级额外加1
    private static final int DRAW = 1;
    private static final int UPG_DRAW = 1;

    public Linger() {
        super(ID, new CardStats(
                Hoolheyak.Meta.CARD_COLOR,
                CardType.SKILL,
                CardRarity.BASIC,
                CardTarget.SELF,
                COST
        ));

        setBlock(BLOCK);
        setMagic(LOOK_AMOUNT, UPG_LOOK_AMOUNT);
        setCustomVar("DRAW", DRAW, UPG_DRAW);
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        // 1. 叠甲
        addToBot(new GainBlockAction(p, p, block));

        // 2. 呼叫观星 Action (看 magicNumber 张，最多也选 magicNumber 张)
        addToBot(new StargazingAction(this, this.magicNumber, this.magicNumber));

        // 3. 抽牌（固定抽 1 张）
        addToBot(new DrawCardAction(p, customVar("DRAW")));
    }

    @Override
    public ArrayList<VariableAction.VariableChoice> getVariableChoices(AbstractPlayer p, AbstractMonster m, boolean isAutoTriggered) {
        return new ArrayList<>();
    }

    @Override
    public boolean canBeAutoTriggered() {
        return false; // 严禁被自动触发
    }
}
