package Hoolheyak.cards;

import Hoolheyak.actions.VariableAction;
import Hoolheyak.character.Hoolheyak;
import Hoolheyak.powers.AnalysisPower;
import Hoolheyak.powers.EruditionPower;
import Hoolheyak.powers.MeanderPower;
import Hoolheyak.util.CardStats;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

import java.util.ArrayList;

public class FlyUp extends BaseCard {
    public static final String ID = makeID("FlyUp");

    private static final int COST = 2;

    public FlyUp() {
        super(ID, new CardStats(
                Hoolheyak.Meta.CARD_COLOR,
                CardType.SKILL,
                CardRarity.RARE,
                CardTarget.SELF,
                COST
        ));
    }

    // 实时更新卡面上的 X 数值
    @Override
    public void applyPowers() {
        super.applyPowers();
        int x = 0;
        if (AbstractDungeon.player != null && AbstractDungeon.player.hasPower(AnalysisPower.POWER_ID)) {
            x = AbstractDungeon.player.getPower(AnalysisPower.POWER_ID).amount;
        }
        // 动态拼接文本，EXTENDED_DESCRIPTION 里存放了提示语句
        this.rawDescription = cardStrings.DESCRIPTION + cardStrings.EXTENDED_DESCRIPTION[2] + x + cardStrings.EXTENDED_DESCRIPTION[3];
        this.initializeDescription();
    }

    @Override
    public void onMoveToDiscard() {
        this.rawDescription = cardStrings.DESCRIPTION;
        this.initializeDescription();
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        int x = p.hasPower(AnalysisPower.POWER_ID) ? p.getPower(AnalysisPower.POWER_ID).amount : 0;

        ArrayList<VariableAction.VariableChoice> choices = new ArrayList<>();

        // 选项 α：触发 X 次博览
        choices.add(new VariableAction.VariableChoice(cardStrings.EXTENDED_DESCRIPTION[0], () -> {
            if (x > 0) {
                addToBot(new ApplyPowerAction(p, p, new EruditionPower(p, x), x));
            }
        }));

        // 选项 β：触发 X 次逶迤
        choices.add(new VariableAction.VariableChoice(cardStrings.EXTENDED_DESCRIPTION[1], () -> {
            if (x > 0) {
                addToBot(new ApplyPowerAction(p, p, new MeanderPower(p, x), x));
            }
        }));

        addToBot(new VariableAction(this, choices));
    }
}