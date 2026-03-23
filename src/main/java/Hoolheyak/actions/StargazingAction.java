package Hoolheyak.actions;

import Hoolheyak.character.Hoolheyak;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.UIStrings;
import java.util.ArrayList;
import Hoolheyak.HoolheyakMod;


public class StargazingAction extends AbstractGameAction {
    // 统一读取 UI 文本
    // ⚠️ 确保你的 UIStrings.json 中有 "YourModID:StargazingActionUI" 这个词条
    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(HoolheyakMod.makeID("StargazingActionUI"));
    public static final String[] TEXT = uiStrings.TEXT;

    private final AbstractCard sourceCard;
    private final int lookAmount;   // 翻开几张牌
    private final int selectAmount; // 最多选几张牌
    private final CardGroup tempGroup;

    public StargazingAction(AbstractCard sourceCard, int lookAmount, int selectAmount) {
        this.sourceCard = sourceCard;
        this.lookAmount = lookAmount;
        this.selectAmount = selectAmount;

        this.actionType = ActionType.CARD_MANIPULATION;
        this.duration = Settings.ACTION_DUR_FAST;
        this.tempGroup = new CardGroup(CardGroup.CardGroupType.UNSPECIFIED);
    }

    @Override
    public void update() {
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

            // 打开选择界面，TEXT[0] 对应 "选择你要处理的牌。"
            AbstractDungeon.gridSelectScreen.open(tempGroup, this.selectAmount, true, TEXT[0]);
            this.tickDuration();
            return;
        }

        if (!AbstractDungeon.isScreenUp) {
            AbstractPlayer p = AbstractDungeon.player;
            ArrayList<AbstractCard> selected = new ArrayList<>(AbstractDungeon.gridSelectScreen.selectedCards);

            for (AbstractCard c : tempGroup.group) {
                c.stopGlowing();   // 停止发光
                c.unhover();       // 取消悬停状态
                c.unfadeOut();     // 取消褪色效果
                c.isSelected = false;
            }

            // 1. 将没有被选中的牌放回抽牌堆顶部（倒序放回以保持原序）
            for (int i = tempGroup.size() - 1; i >= 0; i--) {
                AbstractCard c = tempGroup.group.get(i);
                if (!selected.contains(c)) {
                    p.drawPile.addToTop(c);
                }
            }

            AbstractDungeon.gridSelectScreen.selectedCards.clear();

            // 2. 如果玩家选中了牌，生成变量选项
            if (!selected.isEmpty()) {
                ArrayList<VariableAction.VariableChoice> choices = new ArrayList<>();

                // 选项 α：弃掉它们，TEXT[1] 对应 "弃掉它们。"
                choices.add(new VariableAction.VariableChoice(TEXT[1], () -> {
                    for (AbstractCard c : selected) {
                        c.current_x = Settings.WIDTH / 2.0F;
                        c.current_y = Settings.HEIGHT / 2.0F;
                        p.discardPile.addToTop(c);
                        c.triggerOnManualDiscard();
                        c.shrink();
                        c.darken(false);
                        AbstractDungeon.getCurrRoom().souls.discard(c, true);
                    }
                }));

                // 选项 β：置于抽牌堆底，TEXT[2] 对应 "放到抽牌堆底。"
                choices.add(new VariableAction.VariableChoice(TEXT[2], () -> {
                    for (AbstractCard c : selected) {
                        c.current_x = Settings.WIDTH / 2.0F;
                        c.current_y = Settings.HEIGHT / 2.0F;
                        p.drawPile.addToBottom(c);
                        c.shrink();
                        c.darken(false);
                        AbstractDungeon.getCurrRoom().souls.onToDeck(c, false, true);
                    }
                }));

                // 🚨 关键修复：使用 addToTop，确保选项结算优先于队列中后续的 Action（如抽牌）
                AbstractDungeon.actionManager.addToTop(new VariableAction(this.sourceCard, choices));
            }

            this.isDone = true;
        }
    }
}