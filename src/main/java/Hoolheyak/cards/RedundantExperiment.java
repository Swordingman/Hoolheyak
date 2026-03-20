package Hoolheyak.cards;

import Hoolheyak.actions.VariableAction;
import Hoolheyak.character.Hoolheyak;
import Hoolheyak.powers.RedundantExperimentPower;
import Hoolheyak.util.CardStats;
import Hoolheyak.util.IVariableCard;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.actions.common.RemoveSpecificPowerAction;
import com.megacrit.cardcrawl.actions.utility.NewQueueCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

import java.util.ArrayList;

public class RedundantExperiment extends BaseCard implements IVariableCard {
    public static final String ID = makeID("RedundantExperiment");

    private static final int COST = 2;
    private static final int MAGIC = 1; // 选 1 张
    private static final int UPGRADE_PLUS_MAGIC = 1; // 升级选 2 张

    public RedundantExperiment() {
        super(ID, new CardStats(
                Hoolheyak.Meta.CARD_COLOR,
                CardType.SKILL,
                CardRarity.UNCOMMON,
                CardTarget.NONE,
                COST
        ));
        setMagic(MAGIC, UPGRADE_PLUS_MAGIC);
    }

    @Override
    public ArrayList<VariableAction.VariableChoice> getVariableChoices(AbstractPlayer p, AbstractMonster m, boolean isAutoTriggered) {
        ArrayList<VariableAction.VariableChoice> choices = new ArrayList<>();

        // 🌟 分歧：如果被大招自动触发
        if (isAutoTriggered) {
            // 大招触发时，我们直接把 α 和 β 的效果揉在一起做成纯后台 Action
            choices.add(new VariableAction.VariableChoice(cardStrings.EXTENDED_DESCRIPTION[3], () -> {
                addToBot(new AbstractGameAction() {
                    @Override
                    public void update() {
                        if (!p.hand.isEmpty()) {
                            // 1. 随机挑选 hand 里的牌 (数量为 magicNumber)
                            ArrayList<AbstractCard> randomTargets = new ArrayList<>();
                            ArrayList<AbstractCard> handCopy = new ArrayList<>(p.hand.group);
                            handCopy.remove(RedundantExperiment.this); // 排除自己

                            int count = Math.min(magicNumber, handCopy.size());
                            while (randomTargets.size() < count) {
                                AbstractCard randomCard = handCopy.get(AbstractDungeon.cardRandomRng.random(handCopy.size() - 1));
                                randomTargets.add(randomCard);
                                handCopy.remove(randomCard);
                            }

                            // 2. 随机决定是给它们上 Buff(α)，还是弃掉抽牌(β)
                            boolean rollAlpha = AbstractDungeon.cardRandomRng.randomBoolean();

                            if (rollAlpha) {
                                addToTop(new ApplyPowerAction(p, p, new RedundantExperimentPower(p, randomTargets)));
                            } else {
                                int discardCount = 0;
                                for (AbstractCard c : randomTargets) {
                                    if (p.hand.contains(c)) {
                                        p.hand.moveToDiscardPile(c);
                                        c.triggerOnManualDiscard();
                                        GameActionManager.incrementDiscard(false);
                                        discardCount++;
                                    }
                                }
                                if (discardCount > 0) {
                                    addToTop(new DrawCardAction(p, discardCount));
                                }
                            }
                        }
                        this.isDone = true;
                    }
                });
            }));
            return choices;
        }

        // 如果不是自动触发（也就是玩家手动打出），返回空列表，让 use() 方法里的 UI 逻辑去接管
        return choices;
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new AbstractGameAction() {
            private boolean opened = false;

            {
                this.actionType = ActionType.CARD_MANIPULATION;
                this.duration = Settings.ACTION_DUR_FAST;
            }

            @Override
            public void update() {
                if (!this.opened) {
                    if (p.hand.isEmpty()) {
                        this.isDone = true;
                        return;
                    }
                    // 打开选牌界面，使用 EXTENDED_DESCRIPTION[0]: "选择牌作为实验目标"
                    AbstractDungeon.handCardSelectScreen.open(cardStrings.EXTENDED_DESCRIPTION[0], magicNumber, true, true);
                    this.opened = true;
                    return;
                }

                if (!AbstractDungeon.handCardSelectScreen.wereCardsRetrieved) {
                    ArrayList<AbstractCard> selectedCards = new ArrayList<>();
                    for (AbstractCard c : AbstractDungeon.handCardSelectScreen.selectedCards.group) {
                        selectedCards.add(c);
                        p.hand.addToTop(c); // 将选中的牌先放回手牌
                        c.superFlash();     // 闪光提示
                    }

                    if (!selectedCards.isEmpty()) {
                        // 准备变量选择列表
                        ArrayList<VariableAction.VariableChoice> choices = new ArrayList<>();

                        // 选项 α: 使它们在丢弃时视为被打出
                        choices.add(new VariableAction.VariableChoice(cardStrings.EXTENDED_DESCRIPTION[1], () -> {
                            addToBot(new ApplyPowerAction(p, p, new RedundantExperimentPower(p, selectedCards)));
                        }));

                        // 选项 β: 弃掉它们，并抽等量的牌。
                        choices.add(new VariableAction.VariableChoice(cardStrings.EXTENDED_DESCRIPTION[2], () -> {
                            // 将弃牌与抽牌操作打包进 Action，确保在 All-In 时严格按照先后顺序执行
                            addToBot(new AbstractGameAction() {
                                @Override
                                public void update() {
                                    int discardCount = 0;
                                    for (AbstractCard c : selectedCards) {
                                        // 二次确认卡牌仍在手牌中（防止其他效果干扰）
                                        if (p.hand.contains(c)) {
                                            p.hand.moveToDiscardPile(c);
                                            c.triggerOnManualDiscard();
                                            GameActionManager.incrementDiscard(false);
                                            discardCount++;
                                        }
                                    }
                                    // 结算完弃牌后，抽等量的牌
                                    if (discardCount > 0) {
                                        addToTop(new DrawCardAction(p, discardCount));
                                    }
                                    this.isDone = true;
                                }
                            });
                        }));

                        // 使用 VariableAction 呼出选项，并允许遗物“星图投影”全选 (canAllIn = true)
                        addToTop(new VariableAction(RedundantExperiment.this, choices, true));
                    }

                    // 清理选牌界面状态
                    AbstractDungeon.handCardSelectScreen.wereCardsRetrieved = true;
                    AbstractDungeon.handCardSelectScreen.selectedCards.group.clear();
                    this.isDone = true;
                }
            }
        });
    }
}