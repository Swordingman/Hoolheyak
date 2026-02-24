package Hoolheyak.cards;

import Hoolheyak.actions.VariableAction;
import Hoolheyak.character.Hoolheyak;
import Hoolheyak.util.CardStats;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.EmptyDeckShuffleAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.CardStrings;

import java.util.ArrayList;
import java.util.function.Consumer;

public class Stargazing extends BaseCard {
    public static final String ID = makeID("Stargazing");

    private static final CardStrings cardStrings = CardCrawlGame.languagePack.getCardStrings(ID);

    private static final int COST = 0;
    private static final int MAGIC = 3; // 看3张
    private static final int UPGRADE_PLUS_MAGIC = 2; // 升级看5张

    public Stargazing() {
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
    public void use(AbstractPlayer p, AbstractMonster m) {
        // 防呆设计：如果抽牌堆空了但弃牌堆有牌，先触发洗牌动作
        if (p.drawPile.isEmpty() && !p.discardPile.isEmpty()) {
            addToBot(new EmptyDeckShuffleAction());
        }

        // 压入我们自定义的“先选牌，后变量”的 Action
        addToBot(new StargazingAction(this, this.magicNumber));
    }

    // 核心 Action
    private static class StargazingAction extends AbstractGameAction {
        private final AbstractCard sourceCard;
        private final int lookAmount;
        private final CardGroup tempGroup;

        public StargazingAction(AbstractCard sourceCard, int lookAmount) {
            this.sourceCard = sourceCard;
            this.lookAmount = lookAmount;
            this.actionType = ActionType.CARD_MANIPULATION;
            this.duration = Settings.ACTION_DUR_FAST;
            this.tempGroup = new CardGroup(CardGroup.CardGroupType.UNSPECIFIED);
        }

        @Override
        public void update() {
            // 第一阶段：从牌堆顶取出牌，并打开网格选择界面
            if (this.duration == Settings.ACTION_DUR_FAST) {
                AbstractPlayer p = AbstractDungeon.player;

                int count = Math.min(this.lookAmount, p.drawPile.size());
                if (count == 0) {
                    this.isDone = true;
                    return;
                }

                // 取出顶部的牌
                for (int i = 0; i < count; i++) {
                    tempGroup.addToBottom(p.drawPile.getNCardFromTop(i));
                }
                for (AbstractCard c : tempGroup.group) {
                    p.drawPile.removeCard(c);
                }

                // 打开选择界面
                // 假设 EXTENDED_DESCRIPTION[2] 是 "选择至多 3 张牌。"
                String msg = Stargazing.cardStrings.EXTENDED_DESCRIPTION.length > 2 ?
                        Stargazing.cardStrings.EXTENDED_DESCRIPTION[2] : "选择至多 3 张牌。";

                AbstractDungeon.gridSelectScreen.open(tempGroup, 3, true, msg);

                this.tickDuration();
                return;
            }

            // 第二阶段：玩家选完牌，界面关闭后
            if (!AbstractDungeon.isScreenUp) {
                AbstractPlayer p = AbstractDungeon.player;
                // 获取玩家选中的牌
                ArrayList<AbstractCard> selected = new ArrayList<>(AbstractDungeon.gridSelectScreen.selectedCards);

                // 1. 将没有被选中的牌放回抽牌堆顶部（倒序放回以保持原序）
                for (int i = tempGroup.size() - 1; i >= 0; i--) {
                    AbstractCard c = tempGroup.group.get(i);
                    if (!selected.contains(c)) {
                        p.drawPile.addToTop(c);
                    }
                }

                // 清理选牌网格的缓存
                AbstractDungeon.gridSelectScreen.selectedCards.clear();

                // 2. 如果玩家确实选中了牌，才呼出变量选项
                if (!selected.isEmpty()) {
                    ArrayList<VariableAction.VariableChoice> choices = new ArrayList<>();

                    // 选项 α：弃掉它们
                    choices.add(new VariableAction.VariableChoice(Stargazing.cardStrings.EXTENDED_DESCRIPTION[0], () -> {
                        for (AbstractCard c : selected) {
                            p.discardPile.addToTop(c);
                            c.triggerOnManualDiscard();
                        }
                    }));

                    // 选项 β：置于抽牌堆底
                    choices.add(new VariableAction.VariableChoice(Stargazing.cardStrings.EXTENDED_DESCRIPTION[1], () -> {
                        for (AbstractCard c : selected) {
                            p.drawPile.addToBottom(c);
                        }
                    }));

                    // 把变量选择界面压入队列
                    addToBot(new VariableAction(sourceCard, choices));
                }

                this.isDone = true;
            }
        }
    }
}