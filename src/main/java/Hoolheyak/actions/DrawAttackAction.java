package Hoolheyak.actions;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.EmptyDeckShuffleAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

public class DrawAttackAction extends AbstractGameAction {
    public DrawAttackAction(int amount) {
        this.amount = amount;
        this.actionType = ActionType.DRAW;
        this.duration = Settings.ACTION_DUR_FAST;
    }

    @Override
    public void update() {
        if (this.duration == Settings.ACTION_DUR_FAST) {
            AbstractPlayer p = AbstractDungeon.player;

            if (p.hand.size() >= 10) {
                p.createHandIsFullDialog();
                this.isDone = true;
                return;
            }

            int drawn = 0;
            // 从牌堆顶往下找攻击牌
            for (int i = p.drawPile.size() - 1; i >= 0 && drawn < this.amount; i--) {
                AbstractCard c = p.drawPile.group.get(i);
                if (c.type == AbstractCard.CardType.ATTACK) {
                    p.drawPile.moveToHand(c, p.drawPile);
                    drawn++;
                    if (p.hand.size() >= 10) {
                        p.createHandIsFullDialog();
                        break;
                    }
                }
            }

            // 如果没抽够，且弃牌堆里还有攻击牌，洗牌后继续抽剩下的
            if (drawn < this.amount && p.hand.size() < 10) {
                boolean hasAttackInDiscard = false;
                for (AbstractCard c : p.discardPile.group) {
                    if (c.type == AbstractCard.CardType.ATTACK) {
                        hasAttackInDiscard = true;
                        break;
                    }
                }
                if (hasAttackInDiscard) {
                    addToTop(new DrawAttackAction(this.amount - drawn));
                    addToTop(new EmptyDeckShuffleAction());
                }
            }

            this.isDone = true;
        }
    }
}