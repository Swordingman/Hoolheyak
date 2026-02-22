package Hoolheyak.powers;

import Hoolheyak.HoolheyakMod;
import com.megacrit.cardcrawl.actions.common.RemoveSpecificPowerAction;
import com.megacrit.cardcrawl.actions.utility.NewQueueCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

import java.util.ArrayList;
import java.util.Iterator;

public class RedundantExperimentPower extends BasePower {
    public static final String POWER_ID = HoolheyakMod.makeID("RedundantExperimentPower");
    private final ArrayList<AbstractCard> trackedCards;

    public RedundantExperimentPower(AbstractCreature owner, ArrayList<AbstractCard> cards) {
        // 利用 cards 的数量作为能力层数，直观提示玩家还有几张牌生效
        super(POWER_ID, PowerType.BUFF, true, owner, cards.size());
        this.trackedCards = new ArrayList<>(cards);
        this.updateDescription();
    }

    // 黑科技：update(int slot) 在每帧都会调用，我们可以进行实时的弃牌堆监控
    @Override
    public void update(int slot) {
        super.update(slot);
        if (this.trackedCards != null && !this.trackedCards.isEmpty()) {
            Iterator<AbstractCard> it = this.trackedCards.iterator();
            while (it.hasNext()) {
                AbstractCard c = it.next();

                // 一旦检测到目标卡进入了弃牌堆
                if (AbstractDungeon.player.discardPile.contains(c)) {
                    // 从弃牌堆捞出
                    AbstractDungeon.player.discardPile.removeCard(c);
                    c.freeToPlayOnce = true; // 视为打出，免耗费

                    AbstractMonster target = AbstractDungeon.getMonsters().getRandomMonster(null, true, AbstractDungeon.cardRandomRng);
                    AbstractDungeon.actionManager.addToBottom(new NewQueueCardAction(c, target, false, true));

                    it.remove(); // 移除监控名单，避免重复打出

                    this.amount = this.trackedCards.size();
                    if (this.amount <= 0) {
                        AbstractDungeon.actionManager.addToBottom(new RemoveSpecificPowerAction(this.owner, this.owner, this));
                    }
                }
            }
        }
    }

    @Override
    public void atEndOfTurn(boolean isPlayer) {
        if (isPlayer) {
            addToBot(new RemoveSpecificPowerAction(this.owner, this.owner, this));
        }
    }

    @Override
    public void updateDescription() {
        this.description = DESCRIPTIONS[0] + this.amount + DESCRIPTIONS[1];
    }
}