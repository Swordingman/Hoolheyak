package Hoolheyak.powers;

import Hoolheyak.HoolheyakMod;
import com.megacrit.cardcrawl.actions.common.RemoveSpecificPowerAction;
import com.megacrit.cardcrawl.actions.utility.NewQueueCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

import java.util.ArrayList;
import java.util.Iterator;

public class RedundantExperimentPower extends BasePower {
    public static final String POWER_ID = HoolheyakMod.makeID("RedundantExperimentPower");
    private final ArrayList<AbstractCard> trackedCards;

    public RedundantExperimentPower(AbstractCreature owner, ArrayList<AbstractCard> cards) {
        super(POWER_ID, PowerType.BUFF, true, owner, cards.size());
        this.trackedCards = new ArrayList<>(cards);
        this.updateDescription();
    }

    // 【核心修复】：彻底删除 update() 方法！不要在 update 里处理游戏结算逻辑！

    // 使用这个时机：在点击结束回合后，但在系统弃掉手牌之前
    @Override
    public void atEndOfTurnPreEndTurnCards(boolean isPlayer) {
        if (isPlayer && !this.trackedCards.isEmpty()) {
            this.flash();

            for (AbstractCard c : this.trackedCards) {
                // 1. 安全检查并从当前位置移除卡牌
                if (AbstractDungeon.player.hand.contains(c)) {
                    AbstractDungeon.player.hand.removeCard(c);
                } else if (AbstractDungeon.player.discardPile.contains(c)) {
                    // 以防玩家在回合内手动把它弃掉了
                    AbstractDungeon.player.discardPile.removeCard(c);
                } else if (AbstractDungeon.player.drawPile.contains(c)) {
                    AbstractDungeon.player.drawPile.removeCard(c);
                }

                // 2. 将卡牌放入 limbo（暂存区），这是让卡牌能在空中悬停打出的标准做法
                AbstractDungeon.player.limbo.addToBottom(c);
                c.current_x = c.target_x = Settings.WIDTH / 2.0F - 300.0F * Settings.scale;
                c.current_y = c.target_y = Settings.HEIGHT / 2.0F;

                // 3. 设置打出属性
                c.freeToPlayOnce = true;
                // 如果你希望这张牌打出后直接消耗掉，取消下面这行的注释
                // c.exhaustOnUseOnce = true;

                // 4. 寻找随机目标并加入打出队列
                AbstractMonster target = AbstractDungeon.getMonsters().getRandomMonster(null, true, AbstractDungeon.cardRandomRng);
                addToBot(new NewQueueCardAction(c, target, false, true));
            }

            // 5. 结算完毕，清空追踪列表并移除此能力
            this.trackedCards.clear();
            addToBot(new RemoveSpecificPowerAction(this.owner, this.owner, this.ID));
        }
    }

    @Override
    public void updateDescription() {
        this.description = DESCRIPTIONS[0] + this.amount + DESCRIPTIONS[1];
    }
}