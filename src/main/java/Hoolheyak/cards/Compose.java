package Hoolheyak.cards;

import Hoolheyak.util.CardStats;
import Hoolheyak.character.Hoolheyak;
import Hoolheyak.actions.VariableAction;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

import java.util.ArrayList;

public class Compose extends BaseCard {
    public static final String ID = makeID("Compose");

    private static final int COST = 1;
    private static final int UPGRADED_COST = 0;

    public Compose() {
        super(ID, new CardStats(
                Hoolheyak.Meta.CARD_COLOR,
                CardType.SKILL,
                CardRarity.RARE,
                CardTarget.NONE,
                COST
        ));
        setCostUpgrade(UPGRADED_COST);
        setExhaust(true);
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        ArrayList<VariableAction.VariableChoice> choices = new ArrayList<>();

        // 选项 α：将一张手牌的费用减 3
        choices.add(new VariableAction.VariableChoice(cardStrings.EXTENDED_DESCRIPTION[0], () -> {
            addToBot(new AbstractGameAction() {
                private boolean openedScreen = false;

                // 初始化 Action
                {
                    this.actionType = ActionType.CARD_MANIPULATION;
                    this.duration = Settings.ACTION_DUR_FAST;
                }

                @Override
                public void update() {
                    // 第一阶段：打开选牌界面
                    if (!this.openedScreen) {
                        if (p.hand.isEmpty()) {
                            this.isDone = true;
                            return;
                        }
                        // 使用 EXTENDED_DESCRIPTION[2] 作为打开选牌界面的上方提示语
                        AbstractDungeon.handCardSelectScreen.open(cardStrings.EXTENDED_DESCRIPTION[2], 1, false, false, false, false);
                        this.openedScreen = true;
                        return;
                    }

                    // 第二阶段：处理玩家选好的牌
                    if (!AbstractDungeon.handCardSelectScreen.wereCardsRetrieved) {
                        for (AbstractCard c : AbstractDungeon.handCardSelectScreen.selectedCards.group) {
                            int newCost = c.costForTurn - 3;
                            if (newCost < 0) {
                                newCost = 0;
                            }
                            c.setCostForTurn(newCost);
                            c.isCostModifiedForTurn = true;
                            c.superFlash(); // 闪烁一下提示玩家降费成功
                            p.hand.addToTop(c); // 将牌放回手牌
                        }
                        AbstractDungeon.handCardSelectScreen.wereCardsRetrieved = true;
                        AbstractDungeon.handCardSelectScreen.selectedCards.group.clear();
                        p.hand.refreshHandLayout();
                        this.isDone = true;
                    }
                }
            });
        }));

        // 选项 β：将所有手牌的费用减 1
        choices.add(new VariableAction.VariableChoice(cardStrings.EXTENDED_DESCRIPTION[1], () -> {
            addToBot(new AbstractGameAction() {
                @Override
                public void update() {
                    for (AbstractCard c : p.hand.group) {
                        int newCost = c.costForTurn - 1;
                        if (newCost < 0) {
                            newCost = 0;
                        }
                        c.setCostForTurn(newCost);
                        c.isCostModifiedForTurn = true;
                        c.superFlash();
                    }
                    this.isDone = true;
                }
            });
        }));

        // 将组装好的选项传入你写好的 VariableAction 中
        addToBot(new VariableAction(this, choices));
    }
}