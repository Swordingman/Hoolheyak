package Hoolheyak.cards;

import Hoolheyak.actions.VariableAction;
import Hoolheyak.character.Hoolheyak;
import Hoolheyak.util.CardStats;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.utility.NewQueueCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.rewards.RewardItem;

import java.util.ArrayList;

public class RecursiveExperiment extends BaseCard {
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

    // 处理循环递归的核心 Action
    public static class RecursiveExperimentAction extends AbstractGameAction {
        private final AbstractCard sourceCard;

        public RecursiveExperimentAction(AbstractCard sourceCard) {
            this.sourceCard = sourceCard;
            this.actionType = ActionType.SPECIAL;
        }

        @Override
        public void update() {
            // 如果玩家当前没有能量，直接终止实验，防止卡死
            if (AbstractDungeon.player.energy.energy < 1) {
                this.isDone = true;
                return;
            }

            String[] extDesc = CardCrawlGame.languagePack.getCardStrings(ID).EXTENDED_DESCRIPTION;
            ArrayList<VariableAction.VariableChoice> choices = new ArrayList<>();

            // 选项 α：终止本次实验
            choices.add(new VariableAction.VariableChoice(extDesc[0], () -> {
                // 不做任何事，实验链条断裂
            }));

            // 选项 β：花费1点能量继续
            choices.add(new VariableAction.VariableChoice(extDesc[1], () -> {
                AbstractPlayer p = AbstractDungeon.player;

                // 扣除 1 点能量
                p.energy.use(1);

                CardGroup masterDeck = p.masterDeck;
                if (!masterDeck.isEmpty()) {
                    // 随机抽取一张牌
                    AbstractCard removedCard = masterDeck.getRandomCard(AbstractDungeon.cardRandomRng);

                    // 永久移除
                    masterDeck.removeCard(removedCard);

                    // 同时从战斗中的卡组清理干净
                    p.drawPile.removeCard(removedCard);
                    p.discardPile.removeCard(removedCard);
                    p.hand.removeCard(removedCard);

                    // 打出该牌的一份复制体（打完即焚）
                    AbstractCard copy = removedCard.makeSameInstanceOf();
                    copy.purgeOnUse = true;
                    AbstractMonster target = AbstractDungeon.getMonsters().getRandomMonster(null, true, AbstractDungeon.cardRandomRng);
                    addToBot(new NewQueueCardAction(copy, target, false, true));

                    // 结算战斗后额外获得该类型的奖励
                    RewardItem reward = new RewardItem();
                    reward.cards.clear();
                    while (reward.cards.size() < 3) {
                        AbstractCard c = AbstractDungeon.returnTrulyRandomCardInCombat(removedCard.type).makeCopy();
                        boolean isDuplicate = false;
                        for (AbstractCard rc : reward.cards) {
                            if (rc.cardID.equals(c.cardID)) { isDuplicate = true; break; }
                        }
                        if (!isDuplicate) reward.cards.add(c);
                    }
                    AbstractDungeon.getCurrRoom().rewards.add(reward);

                    // 【递归核心】：重新将自己排入队列末尾
                    addToBot(new RecursiveExperimentAction(sourceCard));
                }
            }));

            // 在队列顶部呼出选择界面
            addToTop(new VariableAction(sourceCard, choices));
            this.isDone = true;
        }
    }
}