package Hoolheyak.actions;

import Hoolheyak.powers.EruditionPower;
import Hoolheyak.powers.KukulkanLegacyPower;
import Hoolheyak.powers.MeanderPower;
import Hoolheyak.powers.phases.SextilePower;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

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
        // 基础阈值：博览(ERUDITION)为 4，其他(如 MEANDER)为 5
        int baseThreshold = (type == KeywordType.ERUDITION) ? 4 : 5;

        // 如果有 SextilePower，则博览变为 2 (4-2)，逶迤变为 3 (5-2) -> 这里假设减少量是固定的
        // 或者你可以直接指定具体数值：
        int threshold = target.hasPower(SextilePower.POWER_ID) ? (baseThreshold - 2) : baseThreshold;

        if (target.hasPower(KukulkanLegacyPower.POWER_ID)) {
            threshold += 3 * target.getPower(KukulkanLegacyPower.POWER_ID).amount;
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
        this.isDone = true; // Action 执行完毕必须标记为 true
    }
}