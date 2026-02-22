package Hoolheyak.actions;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.utility.NewQueueCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class RepeatAction extends AbstractGameAction {
    private final AbstractCard card;
    private final AbstractMonster target;
    private final int repeatTimes;

    public RepeatAction(AbstractCard card, AbstractMonster target, int repeatTimes) {
        this.card = card;
        this.target = target;
        this.repeatTimes = repeatTimes;
        this.actionType = ActionType.SPECIAL;
        this.duration = Settings.ACTION_DUR_FAST;
    }

    @Override
    public void update() {
        if (this.duration == Settings.ACTION_DUR_FAST) {
            // 确保本体不是被复制出来的衍生牌，防止无限指数级循环
            if (!this.card.dontTriggerOnUseCard && this.repeatTimes > 0) {
                for (int i = 0; i < this.repeatTimes; i++) {
                    AbstractCard tmp = this.card.makeSameInstanceOf();
                    tmp.current_x = tmp.target_x = this.card.current_x;
                    tmp.current_y = tmp.target_y = this.card.current_y;

                    // 核心：让复制出来的牌打完即焚，且不再触发自身机制
                    tmp.dontTriggerOnUseCard = true;
                    tmp.purgeOnUse = true;

                    // 将复制卡排入行动队列自动打出（不消耗费用）
                    addToTop(new NewQueueCardAction(tmp, this.target, false, true));
                }
            }
        }
        this.isDone = true;
    }
}