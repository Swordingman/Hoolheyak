package Hoolheyak.actions;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.utility.NewQueueCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

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

            if (!this.card.purgeOnUse && this.repeatTimes > 0) {
                for (int i = 0; i < this.repeatTimes; i++) {
                    AbstractCard tmp = this.card.makeSameInstanceOf();
                    tmp.current_x = tmp.target_x = this.card.current_x;
                    tmp.current_y = tmp.target_y = this.card.current_y;
                    tmp.purgeOnUse = true;

                    AbstractDungeon.actionManager.cardsPlayedThisTurn.add(tmp);

                    addToTop(new NewQueueCardAction(tmp, this.target, false, true));
                }
            }
        }
        this.isDone = true;
    }
}