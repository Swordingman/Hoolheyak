package Hoolheyak.actions;

import Hoolheyak.character.HoolheyakDifficultyHelper;
import Hoolheyak.powers.EruditionPower;
import Hoolheyak.powers.KukulkanLegacyPower;
import Hoolheyak.powers.MeanderPower;
import Hoolheyak.powers.phases.SextilePower;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.core.AbstractCreature;

public class TriggerKeywordAction extends AbstractGameAction {

    // 用枚举来区分你要触发哪个关键词
    public enum KeywordType {
        ERUDITION, // 博览
        MEANDER    // 逶迤
    }

    private final KeywordType type;

    public TriggerKeywordAction(AbstractCreature target, KeywordType type, int times) {
        this.target = target;
        this.amount = times; // 这里的 amount 代表你想触发的【次数】
        this.type = type;
        this.actionType = ActionType.SPECIAL;
    }

    // ★ 核心计算公式：统一写在这里，方便未来维护 ★
    public static int getThreshold(AbstractCreature target, KeywordType type) {
        // 1. 获取默认基础阈值：博览(ERUDITION)为 4，逶迤(MEANDER)为 5
        int baseThreshold = (type == KeywordType.ERUDITION) ? 4 : 5;

        // 2. 根据难度选项调整基础阈值 (引入你的 DifficultyHelper)
        HoolheyakDifficultyHelper.DifficultyLevel diff = HoolheyakDifficultyHelper.currentDifficulty;

        if (diff == HoolheyakDifficultyHelper.DifficultyLevel.EASY && type == KeywordType.MEANDER) {
            // 简单难度 (初步调查)：逶迤的层数上限减少 1 (5 -> 4)
            baseThreshold -= 1;
        } else if (diff == HoolheyakDifficultyHelper.DifficultyLevel.HARD && type == KeywordType.ERUDITION) {
            // 困难难度 (深入分析)：博览的层数上限增加 1 (4 -> 5)
            baseThreshold += 1;
        }

        // 如果 target 为空（理论上不应该，但加个保险防报错），直接返回当前算出的基础阈值
        if (target == null) {
            return baseThreshold;
        }

        // 3. 计算遗物 / 能力 (Power) 带来的修饰
        int threshold = baseThreshold;

        if (target.hasPower(SextilePower.POWER_ID)) {
            threshold -= 2; // 减少2层需求
        }

        if (target.hasPower(KukulkanLegacyPower.POWER_ID)){
            int legacyAmount = target.getPower(KukulkanLegacyPower.POWER_ID).amount;

            if (diff == HoolheyakDifficultyHelper.DifficultyLevel.EASY) {
                threshold += 2 * legacyAmount;
            } else {
                threshold += 3 * legacyAmount;
            }
        }

        if (threshold < 1) {
            threshold = 1;
        }

        return threshold;
    }

    @Override
    public void update() {
        if (this.amount > 0 && this.target != null) {
            // 计算需要给予的总层数 = 单次阈值 × 触发次数
            int totalStacks = getThreshold(this.target, this.type) * this.amount;

            if (this.type == KeywordType.ERUDITION) {
                addToTop(new ApplyPowerAction(this.target, this.target, new EruditionPower(this.target, totalStacks), totalStacks));
            } else if (this.type == KeywordType.MEANDER) {
                addToTop(new ApplyPowerAction(this.target, this.target, new MeanderPower(this.target, totalStacks), totalStacks));
            }
        }
        this.isDone = true;
    }
}