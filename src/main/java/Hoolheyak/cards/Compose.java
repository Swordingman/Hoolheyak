package Hoolheyak.cards;

import Hoolheyak.character.Hoolheyak;
import Hoolheyak.actions.VariableAction;
import Hoolheyak.util.CardStats;
import Hoolheyak.util.IVariableCard;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

import java.util.ArrayList;

public class Compose extends BaseCard implements IVariableCard {
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

    // 实现带有 isAutoTriggered 的新接口方法
    @Override
    public ArrayList<VariableAction.VariableChoice> getVariableChoices(AbstractPlayer p, AbstractMonster m, boolean isAutoTriggered) {
        ArrayList<VariableAction.VariableChoice> choices = new ArrayList<>();

        // 选项 α：将一张手牌的费用减 3 (动态分歧)
        choices.add(new VariableAction.VariableChoice(cardStrings.EXTENDED_DESCRIPTION[0], () -> {

            // 🌟 分歧点 1：如果是被大招自动触发的，走随机逻辑（无 UI）
            if (isAutoTriggered) {
                addToBot(new AbstractGameAction() {
                    @Override
                    public void update() {
                        if (!p.hand.isEmpty()) {
                            // 随机抽取一张手牌
                            AbstractCard randomCard = p.hand.getRandomCard(AbstractDungeon.cardRandomRng);
                            int newCost = Math.max(0, randomCard.costForTurn - 3);
                            randomCard.setCostForTurn(newCost);
                            randomCard.isCostModifiedForTurn = true;
                            randomCard.superFlash();
                        }
                        this.isDone = true;
                    }
                });
            }
            // 🌟 分歧点 2：如果是玩家手动打出的，走选牌逻辑（有 UI）
            else {
                addToBot(new AbstractGameAction() {
                    private boolean openedScreen = false;

                    {
                        this.actionType = ActionType.CARD_MANIPULATION;
                        this.duration = Settings.ACTION_DUR_FAST;
                    }

                    @Override
                    public void update() {
                        if (!this.openedScreen) {
                            if (p.hand.isEmpty()) {
                                this.isDone = true;
                                return;
                            }
                            AbstractDungeon.handCardSelectScreen.open(cardStrings.EXTENDED_DESCRIPTION[2], 1, false, false, false, false);
                            this.openedScreen = true;
                            return;
                        }

                        if (!AbstractDungeon.handCardSelectScreen.wereCardsRetrieved) {
                            for (AbstractCard c : AbstractDungeon.handCardSelectScreen.selectedCards.group) {
                                int newCost = Math.max(0, c.costForTurn - 3);
                                c.setCostForTurn(newCost);
                                c.isCostModifiedForTurn = true;
                                c.superFlash();
                                p.hand.addToTop(c);
                            }
                            AbstractDungeon.handCardSelectScreen.wereCardsRetrieved = true;
                            AbstractDungeon.handCardSelectScreen.selectedCards.group.clear();
                            p.hand.refreshHandLayout();
                            this.isDone = true;
                        }
                    }
                });
            }
        }));

        // 选项 β：将所有手牌的费用减 1 (纯后台计算，无需分歧)
        choices.add(new VariableAction.VariableChoice(cardStrings.EXTENDED_DESCRIPTION[1], () -> {
            addToBot(new AbstractGameAction() {
                @Override
                public void update() {
                    for (AbstractCard c : p.hand.group) {
                        int newCost = Math.max(0, c.costForTurn - 1);
                        c.setCostForTurn(newCost);
                        c.isCostModifiedForTurn = true;
                        c.superFlash();
                    }
                    this.isDone = true;
                }
            });
        }));

        return choices;
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        // 正常打出时，调用默认接口方法（isAutoTriggered 会默认为 false）
        addToBot(new VariableAction(this, getVariableChoices(p, m), true));
    }
}