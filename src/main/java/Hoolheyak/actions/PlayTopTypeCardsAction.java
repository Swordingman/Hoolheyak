package Hoolheyak.actions;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.utility.NewQueueCardAction;
import com.megacrit.cardcrawl.actions.utility.UnlimboAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

public class PlayTopTypeCardsAction extends AbstractGameAction {
    private int amountToPlay;
    private AbstractCard.CardType targetType;

    public PlayTopTypeCardsAction(int amountToPlay, AbstractCard.CardType type) {
        this.amountToPlay = amountToPlay;
        this.targetType = type;
        this.duration = Settings.ACTION_DUR_FAST;
    }

    @Override
    public void update() {
        if (this.duration == Settings.ACTION_DUR_FAST) {
            int played = 0;
            // 从抽牌堆顶部往下找
            for (int i = AbstractDungeon.player.drawPile.size() - 1; i >= 0 && played < this.amountToPlay; i--) {
                AbstractCard c = AbstractDungeon.player.drawPile.group.get(i);
                if (c.type == this.targetType) {

                    // 将其移出抽牌堆，放入 Limbo (悬空区) 准备打出
                    AbstractDungeon.player.drawPile.group.remove(i);
                    AbstractDungeon.getCurrRoom().souls.remove(c);
                    c.exhaustOnUseOnce = false;
                    AbstractDungeon.player.limbo.group.add(c);

                    // 设置动画位置
                    c.current_y = -200.0F * Settings.scale;
                    c.target_x = Settings.WIDTH / 2.0F + 200.0F * Settings.xScale;
                    c.target_y = Settings.HEIGHT / 2.0F;
                    c.targetAngle = 0.0F;
                    c.lighten(false);
                    c.drawScale = 0.12F;
                    c.targetDrawScale = 0.75F;

                    c.applyPowers();
                    // 加入打出队列 (参数: 卡牌, 随机目标, 不耗费能量, 排队打出)
                    addToTop(new NewQueueCardAction(c, true, false, true));
                    addToTop(new UnlimboAction(c));

                    played++;
                }
            }
            this.isDone = true;
        }
    }
}