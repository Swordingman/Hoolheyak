package Hoolheyak.util;

import Hoolheyak.cards.phases.*;
import Hoolheyak.powers.phases.*;
import Hoolheyak.vfx.PhaseAuroraEffect;
import com.badlogic.gdx.graphics.Color;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.RemoveSpecificPowerAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.vfx.AbstractGameEffect;

public class PhaseManager {
    public static final String[] ALL_PHASE_IDS = {
            "Hoolheyak:Conjunction", "Hoolheyak:Quincunx", "Hoolheyak:Sextile",
            "Hoolheyak:Trine", "Hoolheyak:Square", "Hoolheyak:Opposition"
    };

    // 1. 纯粹的施加相位逻辑（供遗物和技能卡调用）
    public static void applyPhase(AbstractPower newPhasePower) {
        AbstractPlayer p = AbstractDungeon.player;

        // 1. 清除旧相位的动作
        AbstractDungeon.actionManager.addToBottom(new AbstractGameAction() {
            @Override
            public void update() {
                for (String phaseId : ALL_PHASE_IDS) {
                    if (p.hasPower(phaseId)) {
                        // 使用 addToTop 确保在此动作期间立刻移除
                        addToTop(new RemoveSpecificPowerAction(p, p, phaseId));
                    }
                }
                this.isDone = true;
            }
        });

        // 2. 挂上新相位的动作
        AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(p, p, newPhasePower));

        // 3. 生成极光特效的动作 (确保在 ApplyPower 之后才执行)
        AbstractDungeon.actionManager.addToBottom(new AbstractGameAction() {
            @Override
            public void update() {
                for (AbstractGameEffect effect : AbstractDungeon.effectsQueue) {
                    if (effect instanceof PhaseAuroraEffect) {
                        ((PhaseAuroraEffect) effect).forceFadeOut();
                    }
                }
                // 保险起见，顶层特效队列也遍历一遍
                for (AbstractGameEffect effect : AbstractDungeon.topLevelEffects) {
                    if (effect instanceof PhaseAuroraEffect) {
                        ((PhaseAuroraEffect) effect).forceFadeOut();
                    }
                }

                CardCrawlGame.sound.play("HEART_BEAT", 0.05F);
                Color phaseColor = Color.WHITE.cpy();
                float maxAlpha = 0.5f;

                switch (newPhasePower.ID) {
                    case "Hoolheyak:Conjunction":
                        // 合相：耀眼的金黄色，能量融合
                        phaseColor = new Color(1.0f, 0.85f, 0.2f, 1f);
                        maxAlpha = 0.65f;
                        break;
                    case "Hoolheyak:Sextile":
                        // 六分相：清新的青蓝色，轻快顺畅
                        phaseColor = new Color(0.2f, 0.8f, 0.8f, 1f);
                        maxAlpha = 0.45f;
                        break;
                    case "Hoolheyak:Square":
                        // 四分相：充满张力的橙色，摩擦与挑战
                        phaseColor = new Color(1.0f, 0.5f, 0.0f, 1f);
                        maxAlpha = 0.6f;
                        break;
                    case "Hoolheyak:Trine":
                        // 三分相：生机盎然的翠绿色，极致和谐
                        phaseColor = new Color(0.2f, 0.9f, 0.3f, 1f);
                        maxAlpha = 0.5f;
                        break;
                    case "Hoolheyak:Quincunx":
                        // 梅花相：诡异神秘的深紫色，盲点与调整
                        phaseColor = new Color(0.6f, 0.1f, 0.8f, 1f);
                        maxAlpha = 0.5f;
                        break;
                    case "Hoolheyak:Opposition":
                        // 对分相：压迫感十足的猩红色，极端对立
                        phaseColor = new Color(0.9f, 0.05f, 0.05f, 1f);
                        maxAlpha = 0.7f;
                        break;
                    default:
                        // 兜底保护
                        phaseColor = Color.WHITE.cpy();
                        maxAlpha = 0.5f;
                        break;
                }

                // 此时玩家身上必然已经有这个 Power 了，特效不会再自杀
                AbstractDungeon.effectsQueue.add(new PhaseAuroraEffect(phaseColor, newPhasePower.ID, maxAlpha));
                this.isDone = true;
            }
        });
    }

    // 2. 状态牌抽到时的逻辑（消耗卡牌 + 施加相位）
    public static void triggerPhaseCardDrawn(AbstractCard phaseCard, AbstractPower newPhasePower) {
        AbstractPlayer p = AbstractDungeon.player;
        AbstractDungeon.actionManager.addToBottom(new AbstractGameAction() {
            @Override
            public void update() {
                if (p.hand.contains(phaseCard)) {
                    p.hand.moveToExhaustPile(phaseCard);
                }
                this.isDone = true;
            }
        });
        applyPhase(newPhasePower); // 复用施加逻辑
    }

    // 3. 随机获取一个相位（供遗物使用）
    public static AbstractPower getRandomPhase(AbstractCreature owner) {
        int roll = AbstractDungeon.cardRandomRng.random(5);
        switch (roll) {
            case 0: return new ConjunctionPower(owner);
            case 1: return new QuincunxPower(owner);
            case 2: return new SextilePower(owner);
            case 3: return new TrinePower(owner);
            case 4: return new SquarePower(owner);
            default: return new OppositionPower(owner);
        }
    }

    // 4. 随机获取一个吉相
    public static AbstractPower getRandomGoodPhase(AbstractCreature owner) {
        int roll = AbstractDungeon.cardRandomRng.random(1);
        if (roll == 0) {
            return new SextilePower(owner);
        } else {
            return new TrinePower(owner);
        }
    }

    // 5. 随机获取一张相位卡牌
    public static AbstractCard getRandomPhaseCard() {
        int roll = AbstractDungeon.cardRandomRng.random(5);
        switch (roll) {
            case 0: return new ConjunctionCard();
            case 1: return new QuincunxCard();
            case 2: return new SextileCard();
            case 3: return new TrineCard();
            case 4: return new SquareCard();
            default: return new OppositionCard();
        }
    }
}