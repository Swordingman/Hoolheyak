package Hoolheyak.cards;

import Hoolheyak.actions.VariableAction;
import Hoolheyak.actions.VariableChoiceCard;
import Hoolheyak.character.Hoolheyak; // 请根据你的实际包名调整
import Hoolheyak.util.IVariableCard;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInHandAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardBrieflyEffect;

import java.util.ArrayList;

public class FactorialExperiment extends BaseCard implements IVariableCard {
    public static final String ID = "Hoolheyak:FactorialExperiment";

    private static final int COST = 2;
    private static final int DAMAGE = 12;
    private static final int UPGRADE_PLUS_DMG = 6;

    public FactorialExperiment() {
        super(
                ID,
                COST,
                CardType.ATTACK,
                CardTarget.ENEMY,
                CardRarity.RARE,
                Hoolheyak.Meta.CARD_COLOR
        );

        setDamage(DAMAGE, UPGRADE_PLUS_DMG);
    }

    @Override
    public ArrayList<VariableAction.VariableChoice> getVariableChoices(AbstractPlayer p, AbstractMonster m, boolean isAutoTriggered) {
        ArrayList<VariableAction.VariableChoice> choices = new ArrayList<>();

        // 选项 α：触发手牌中所有变量卡的随机变量
        choices.add(new VariableAction.VariableChoice(cardStrings.EXTENDED_DESCRIPTION[0], () -> {
            for (AbstractCard c : p.hand.group) {
                // 核心判定：是变量卡，且【允许被自动触发】
                if (c instanceof IVariableCard && ((IVariableCard) c).canBeAutoTriggered()) {

                    addToBot(new AbstractGameAction() {
                        @Override
                        public void update() {
                            ArrayList<VariableAction.VariableChoice> handChoices =
                                    ((IVariableCard) c).getVariableChoices(p, m, true);

                            if (handChoices != null && !handChoices.isEmpty()) {
                                int roll = AbstractDungeon.cardRandomRng.random(handChoices.size() - 1);
                                VariableAction.VariableChoice chosen = handChoices.get(roll);

                                String optionName = VariableAction.TEXT[0] + getGreekSuffix(roll);

                                // 实例化提示卡并展示
                                VariableChoiceCard choiceCard = new VariableChoiceCard(c, optionName, roll, chosen.description, chosen.effect);
                                AbstractDungeon.effectList.add(new ShowCardBrieflyEffect(choiceCard.makeStatEquivalentCopy()));
                                c.superFlash();

                                // 执行选项效果
                                choiceCard.onChoseThisOption();
                            }
                            this.isDone = true;
                        }
                    });

                    addToBot(new com.megacrit.cardcrawl.actions.utility.WaitAction(0.2f));
                }
            }
        }));

        // 选项 β：将随机变量卡填满你的手牌，他们在本回合中耗能为 0
        choices.add(new VariableAction.VariableChoice(cardStrings.EXTENDED_DESCRIPTION[1], () -> {
            for (int i = 0; i < 3; i++) {
                AbstractCard randomVarCard = getRandomVariableCard().makeCopy();
                randomVarCard.setCostForTurn(0);
                addToBot(new MakeTempCardInHandAction(randomVarCard, 1));
            }
        }));

        return choices;
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        // 1. 先打出伤害
        addToBot(new DamageAction(m, new DamageInfo(p, damage, damageTypeForTurn), AbstractGameAction.AttackEffect.BLUNT_HEAVY));

        // 2. 触发变量选择
        addToBot(new VariableAction(this, getVariableChoices(p, m), true));
    }

    // 辅助方法：希腊字母后缀
    private String getGreekSuffix(int index) {
        switch (index) {
            case 0: return "α";
            case 1: return "β";
            case 2: return "ω";
            default: return String.valueOf(index + 1);
        }
    }

    private AbstractCard getRandomVariableCard() {
        ArrayList<AbstractCard> variableCardsPool = new ArrayList<>();

        // 1. 遍历当前角色的三个标准战斗卡池，寻找实现了 IVariableCard 接口的牌
        for (AbstractCard c : AbstractDungeon.srcCommonCardPool.group) {
            if (c instanceof IVariableCard) {
                variableCardsPool.add(c);
            }
        }
        for (AbstractCard c : AbstractDungeon.srcUncommonCardPool.group) {
            if (c instanceof IVariableCard) {
                variableCardsPool.add(c);
            }
        }
        for (AbstractCard c : AbstractDungeon.srcRareCardPool.group) {
            // 可选：如果你不希望大招捞出另一张大招导致无限套娃，可以在这里加个排除判定
            if (c instanceof IVariableCard) {
                variableCardsPool.add(c);
            }
        }

        // 2. 防呆设计：如果池子意外为空（比如卡池被某些奇怪的遗物清空了），给一张保底卡
        if (variableCardsPool.isEmpty()) {
            return new CrossExperiment();
        }

        // 3. 使用战斗中的随机数生成器抽取一张
        int roll = AbstractDungeon.cardRandomRng.random(variableCardsPool.size() - 1);
        AbstractCard chosenCard = variableCardsPool.get(roll);

        // 4. 🚨 极其关键的一步：必须返回 makeCopy()！
        // 绝对不能直接返回选中的牌，否则会弄乱底层卡池的内存引用。
        return chosenCard.makeCopy();
    }
}