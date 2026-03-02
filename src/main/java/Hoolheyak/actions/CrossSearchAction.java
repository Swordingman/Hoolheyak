package Hoolheyak.actions;

import Hoolheyak.powers.EruditionPower;
import Hoolheyak.powers.MeanderPower;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

public class CrossSearchAction extends AbstractGameAction {
    private AbstractPlayer p;
    // 调用原版弃牌界面文字
    private static final String[] TEXT = CardCrawlGame.languagePack.getUIString("DiscardAction").TEXT;

    public CrossSearchAction() {
        this.p = AbstractDungeon.player;
        this.actionType = ActionType.DISCARD;
        this.duration = Settings.ACTION_DUR_FAST;
    }

    @Override
    public void update() {
        if (this.duration == Settings.ACTION_DUR_FAST) {
            if (this.p.hand.size() == 0) {
                this.isDone = true;
                return;
            }
            // 如果手牌只有 1 张，自动丢弃它，不弹窗
            if (this.p.hand.size() == 1) {
                AbstractCard c = this.p.hand.getTopCard();
                this.p.hand.moveToDiscardPile(c);
                c.triggerOnManualDiscard();
                GameActionManager.incrementDiscard(false);
                applyEffectBasedOnType(c);
                this.isDone = true;
                return;
            }
            // 否则打开手牌选择界面
            AbstractDungeon.handCardSelectScreen.open(TEXT[0], 1, false, false);
            tickDuration();
            return;
        }

        if (!AbstractDungeon.handCardSelectScreen.wereCardsRetrieved) {
            for (AbstractCard c : AbstractDungeon.handCardSelectScreen.selectedCards.group) {
                this.p.hand.moveToDiscardPile(c);
                c.triggerOnManualDiscard();
                GameActionManager.incrementDiscard(false);
                applyEffectBasedOnType(c);
            }
            AbstractDungeon.handCardSelectScreen.wereCardsRetrieved = true;
        }
        tickDuration();
    }

    private void applyEffectBasedOnType(AbstractCard c) {
        if (c.type == AbstractCard.CardType.ATTACK) {
            addToTop(new ApplyPowerAction(p, p, new EruditionPower(p, 1), 1));
        } else if (c.type == AbstractCard.CardType.SKILL) {
            addToTop(new ApplyPowerAction(p, p, new MeanderPower(p, 1), 1));
        }
    }
}