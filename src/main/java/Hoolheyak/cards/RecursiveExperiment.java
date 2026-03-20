package Hoolheyak.cards;

import Hoolheyak.actions.VariableAction;
import Hoolheyak.character.Hoolheyak;
import Hoolheyak.relics.FailedExperimentProduct;
import Hoolheyak.util.CardStats;
import Hoolheyak.util.IVariableCard;
import Hoolheyak.util.RecursiveExperimentReward;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.actions.utility.WaitAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.rewards.RewardItem;
import com.megacrit.cardcrawl.ui.panels.EnergyPanel;

import java.util.ArrayList;

public class RecursiveExperiment extends BaseCard implements IVariableCard {
    public static final String ID = makeID("RecursiveExperiment");

    private static final int COST = 1;
    private static final int UPGRADED_COST = 0;

    public RecursiveExperiment() {
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
        addToBot(new RecursiveExperimentAction(this));
    }

    @Override
    public ArrayList<VariableAction.VariableChoice> getVariableChoices(AbstractPlayer p, AbstractMonster m, boolean isAutoTriggered) {
        return new ArrayList<>();
    }

    @Override
    public boolean canBeAutoTriggered() {
        return false;
    }

    public static class RecursiveExperimentAction extends AbstractGameAction {
        private final AbstractCard sourceCard;

        public RecursiveExperimentAction(AbstractCard sourceCard) {
            this.sourceCard = sourceCard;
            this.actionType = ActionType.SPECIAL;
        }

        @Override
        public void update() {
            if (EnergyPanel.totalCount < 1) {
                this.isDone = true;
                return;
            }

            String[] extDesc = CardCrawlGame.languagePack.getCardStrings(ID).EXTENDED_DESCRIPTION;
            ArrayList<VariableAction.VariableChoice> choices = new ArrayList<>();

            // 选项 α：终止本次实验
            choices.add(new VariableAction.VariableChoice(extDesc[0], () -> {
            }));

            // 选项 β：花费1点能量继续
            choices.add(new VariableAction.VariableChoice(extDesc[1], () -> {
                AbstractPlayer p = AbstractDungeon.player;
                p.energy.use(1);

                CardGroup masterDeck = p.masterDeck;
                if (!masterDeck.isEmpty()) {
                    AbstractCard removedCard = masterDeck.getRandomCard(AbstractDungeon.cardRandomRng);
                    masterDeck.removeCard(removedCard);
                    p.drawPile.removeCard(removedCard);
                    p.discardPile.removeCard(removedCard);
                    p.hand.removeCard(removedCard);

                    AbstractCard copy = removedCard.makeSameInstanceOf();
                    // 1. 将原来起名为 target 的变量，改名为 randomMonster
                    AbstractMonster randomMonster = AbstractDungeon.getMonsters().getRandomMonster(null, true, AbstractDungeon.cardRandomRng);

                    // --- 视觉初始化部分保持不变 ---
                    p.limbo.addToBottom(copy);
                    copy.current_x = Settings.WIDTH / 2.0F;
                    copy.current_y = Settings.HEIGHT / 2.0F;
                    copy.target_x = Settings.WIDTH / 2.0F - 300.0F * Settings.scale;
                    copy.target_y = Settings.HEIGHT / 2.0F;
                    copy.drawScale = 0.12F;
                    copy.targetDrawScale = 0.75F;
                    copy.lighten(false);
                    copy.targetAngle = 0.0f;

                    // 2. 这里也要改成 randomMonster
                    if (randomMonster != null) { copy.calculateCardDamage(randomMonster); }
                    copy.applyPowers();
                    copy.freeToPlayOnce = true;
                    copy.purgeOnUse = true;
                    copy.energyOnUse = EnergyPanel.totalCount;

                    if (!Settings.FAST_MODE) {
                        addToBot(new WaitAction(Settings.ACTION_DUR_MED));
                    } else {
                        addToBot(new WaitAction(Settings.ACTION_DUR_FASTER));
                    }

                    // 1. 提前判断这是否是一次“失败”的实验
                    boolean isFailure = (removedCard.type == AbstractCard.CardType.CURSE || removedCard.type == AbstractCard.CardType.STATUS);

                    addToBot(new AbstractGameAction() {
                        @Override
                        public void update() {
                            // 2. 原本的 use 逻辑
                            copy.use(p, randomMonster);
                            addToBot(new UseCardAction(copy, randomMonster));

                            addToBot(new AbstractGameAction() {
                                @Override
                                public void update() {

                                    // 3. 核心分歧点：根据是否失败决定奖励
                                    if (isFailure) {
                                        // 如果玩家已经有了这个遗物
                                        if (AbstractDungeon.player.hasRelic(FailedExperimentProduct.ID)) {
                                            FailedExperimentProduct relic = (FailedExperimentProduct) AbstractDungeon.player.getRelic(FailedExperimentProduct.ID);
                                            relic.incrementCounter(); // 次数 +1
                                            relic.flash();            // 闪烁一下提示玩家
                                        } else {
                                            // 首次获得遗物，直接在屏幕中央生成并获取
                                            AbstractDungeon.getCurrRoom().spawnRelicAndObtain(
                                                    Settings.WIDTH / 2.0F, Settings.HEIGHT / 2.0F,
                                                    new FailedExperimentProduct()
                                            );
                                        }
                                    } else {
                                        // 如果是常规卡牌，按原计划给卡牌奖励
                                        RecursiveExperimentReward reward = new RecursiveExperimentReward(removedCard.type);
                                        AbstractDungeon.getCurrRoom().rewards.add(reward);
                                    }

                                    // 继续递归
                                    addToBot(new RecursiveExperimentAction(sourceCard));
                                    this.isDone = true;
                                }
                            });
                            this.isDone = true;
                        }
                    });
                }
            }));

            addToTop(new VariableAction(sourceCard, choices));
            this.isDone = true;
        }
    }
}