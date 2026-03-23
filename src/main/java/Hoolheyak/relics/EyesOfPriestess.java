package Hoolheyak.relics;

import Hoolheyak.HoolheyakMod;
import Hoolheyak.actions.StargazingAction;
import Hoolheyak.cards.Stargazing;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.EmptyDeckShuffleAction;
import com.megacrit.cardcrawl.actions.common.RelicAboveCreatureAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

public class EyesOfPriestess extends BaseRelic {
    public static final String ID = HoolheyakMod.makeID("EyesOfPriestess");

    private boolean isPrimed = false;

    public EyesOfPriestess() {
        super(
                ID,
                "EyesOfPriestess", // 确保路径 HoolheyakResources/images/relics/EyesOfPriestess.png 存在
                AbstractCard.CardColor.COLORLESS,
                RelicTier.RARE, // 稀有遗物
                LandingSound.MAGICAL
        );
    }

    // 1. 洗牌时触发：激活标记
    @Override
    public void onShuffle() {
        if (!this.isPrimed) {
            this.isPrimed = true;
            // 开启持续脉冲闪烁，视觉上提示玩家遗物已“充能”准备就绪
            this.beginLongPulse();
        }
    }

    // 2. 回合开始时触发：检查标记并执行
    @Override
    public void atTurnStart() {
        // 预判：如果牌库空了，且弃牌堆有牌，说明接下来的系统抽牌一定会触发洗牌
        boolean willShuffle = AbstractDungeon.player.drawPile.isEmpty() && !AbstractDungeon.player.discardPile.isEmpty();

        // 如果遗物已经充能，或者我们预判到马上就要洗牌
        if (this.isPrimed || willShuffle) {
            this.flash(); // 触发时的强光闪烁
            this.stopPulse();

            // 如果是因为预判到要洗牌才触发的，必须在这里【提前强行洗牌】
            // 否则接下来的观星 Action 面对的是空牌库，什么也选不到
            if (willShuffle) {
                addToBot(new EmptyDeckShuffleAction());
            }

            // 播放头顶的遗物触发动画
            addToBot(new RelicAboveCreatureAction(AbstractDungeon.player, this));

            // 呼叫观星：看5张，最多选5张
            addToBot(new StargazingAction(new Stargazing(), 5, 5));

            // 【核心细节：将状态重置放进动作队列的尾部】
            // 因为上面的 EmptyDeckShuffleAction 会再次调用你的 onShuffle()
            // 如果你直接写 this.isPrimed = false; 它马上又会被洗牌动作变回 true
            addToBot(new AbstractGameAction() {
                @Override
                public void update() {
                    EyesOfPriestess.this.isPrimed = false;
                    EyesOfPriestess.this.stopPulse();
                    this.isDone = true;
                }
            });
        }
    }

    // 3. 战斗结束时：清理状态（防止状态残留到下一场战斗）
    @Override
    public void onVictory() {
        this.isPrimed = false;
        this.stopPulse();
    }
}