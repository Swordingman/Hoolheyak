package Hoolheyak.powers;

import Hoolheyak.HoolheyakMod;
import com.megacrit.cardcrawl.actions.common.RemoveSpecificPowerAction;
import com.megacrit.cardcrawl.actions.utility.NewQueueCardAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
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

    // 🌟 核心修复：监听卡牌被打出的事件
    @Override
    public void onUseCard(AbstractCard card, UseCardAction action) {
        // 如果玩家主动打出了被追踪的牌，说明它不是被“丢弃”的，取消对它的自动打出标记
        if (this.trackedCards.contains(card)) {
            this.trackedCards.remove(card);

            // 可选：同步更新 Power 的层数显示
            this.amount = this.trackedCards.size();
            this.updateDescription();

            // 如果所有追踪的牌都被打出了，直接移除这个 Power
            if (this.trackedCards.isEmpty()) {
                addToBot(new RemoveSpecificPowerAction(this.owner, this.owner, this.ID));
            }
        }
    }

    // 🌟 核心修复 2：实时监控弃牌堆，实现“丢弃时立刻打出”
    @Override
    public void update(int slot) {
        super.update(slot);

        // 防空指针检查
        if (AbstractDungeon.player == null || this.trackedCards == null) return;

        // 使用迭代器安全地遍历和移除卡牌
        java.util.Iterator<AbstractCard> iterator = this.trackedCards.iterator();
        while (iterator.hasNext()) {
            AbstractCard c = iterator.next();

            // 如果这张卡出现在了弃牌堆，说明它刚刚被“弃掉”了！
            if (AbstractDungeon.player.discardPile.contains(c)) {

                // 1. 把它从弃牌堆里拽出来
                AbstractDungeon.player.discardPile.removeCard(c);

                // 2. 塞进暂存区（Limbo）让它在屏幕中央悬停
                AbstractDungeon.player.limbo.addToBottom(c);
                c.current_x = c.target_x = Settings.WIDTH / 2.0F - 300.0F * Settings.scale;
                c.current_y = c.target_y = Settings.HEIGHT / 2.0F;

                // 3. 设置白嫖打出
                c.freeToPlayOnce = true;

                // 4. 寻找随机目标并加入打出队列
                AbstractMonster target = AbstractDungeon.getMonsters().getRandomMonster(null, true, AbstractDungeon.cardRandomRng);
                addToBot(new NewQueueCardAction(c, target, false, true));

                // 5. 触发完毕，把它从追踪名单里划掉
                iterator.remove();
            }
        }

        // 动态更新 Power 的层数显示
        if (this.amount != this.trackedCards.size()) {
            this.amount = this.trackedCards.size();
            this.updateDescription();
        }

        // 如果手里的牌都被弃光并触发了，功成身退，移除自己
        if (this.trackedCards.isEmpty()) {
            addToBot(new RemoveSpecificPowerAction(this.owner, this.owner, this.ID));
        }
    }

    @Override
    public void updateDescription() {
        this.description = DESCRIPTIONS[0] + this.amount + DESCRIPTIONS[1];
    }
}