package Hoolheyak.powers;

import Hoolheyak.HoolheyakMod;
import Hoolheyak.actions.TriggerKeywordAction;
import Hoolheyak.cards.ContingencyPlan;
import Hoolheyak.powers.phases.QuincunxPower;
import Hoolheyak.powers.phases.SextilePower;
import Hoolheyak.util.CustomTags;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class EruditionPower extends BasePower {
    public static final String POWER_ID = HoolheyakMod.makeID("Erudition");

    public EruditionPower(AbstractCreature owner, int amount) {
        super(POWER_ID, PowerType.BUFF, false, owner, amount);
    }

    @Override
    public void onUseCard(AbstractCard card, UseCardAction action) {

        if (card.hasTag(CustomTags.HOOLHEYAK_VARIABLE)) {
            return;
        }

        boolean isTriggerType = (card.type == AbstractCard.CardType.ATTACK);
        if (this.owner.hasPower(QuincunxPower.POWER_ID)) {
            isTriggerType = (card.type == AbstractCard.CardType.SKILL);
        }

        if (isTriggerType) {
            this.amount++;
            checkAndTrigger();
        }
    }

    // 允许被“光行遥遥”等外部效果直接塞层数引爆
    @Override
    public void stackPower(int stackAmount) {
        super.stackPower(stackAmount);
        checkAndTrigger();
    }

    // 将结算逻辑封装，方便调用
    private void checkAndTrigger() {
        int threshold = TriggerKeywordAction.getThreshold(this.owner, TriggerKeywordAction.KeywordType.ERUDITION);
        int multiplier = 1;

        if (this.owner.hasPower(KukulkanLegacyPower.POWER_ID)) {
            int legacyStacks = this.owner.getPower(KukulkanLegacyPower.POWER_ID).amount;
            multiplier = (int) Math.pow(2, legacyStacks);
        }

        // 使用循环确保能多次触发
        while (this.amount >= threshold) {
            this.amount -= threshold; // 扣除达标的层数

            this.flash();

            int x = this.owner.hasPower(AnalysisPower.POWER_ID) ? this.owner.getPower(AnalysisPower.POWER_ID).amount : 0;

            // 应用翻倍效果
            int finalDamage = x * multiplier;
            int finalLift = 1 * multiplier;

            for (int i = 0; i < 4; i++) {
                addToBot(new AbstractGameAction() {
                    @Override
                    public void update() {
                        AbstractMonster target = AbstractDungeon.getMonsters().getRandomMonster(null, true, AbstractDungeon.cardRandomRng);
                        if (target != null && !target.isDeadOrEscaped()) {
                            addToTop(new ApplyPowerAction(target, owner, new LiftPower(target, owner, finalLift), finalLift));
                            addToTop(new DamageAction(target, new DamageInfo(owner, finalDamage, DamageInfo.DamageType.THORNS), AbstractGameAction.AttackEffect.SLASH_DIAGONAL));
                        }
                        this.isDone = true;
                    }
                });
            }

            if (AbstractDungeon.player.hasPower(ForbiddenKnowledgeEruPower.POWER_ID)) {
                ForbiddenKnowledgeEruPower power = (ForbiddenKnowledgeEruPower) AbstractDungeon.player.getPower(ForbiddenKnowledgeEruPower.POWER_ID);
                power.onTriggerErudition();
            }

            // 触发额外的能力
            ContingencyPlan.returnFromDiscard(true);
            InheritedMemoriesPower.onTriggerKeyword(this.owner);
            CovenantStrengthPower.trigger(this.owner);
        }

        // 更新描述
        updateDescription();
    }

    @Override
    public void updateDescription() {
        // 动态获取当前的博览阈值（这样即使以后再改数字，描述也会自动同步）
        int threshold = TriggerKeywordAction.getThreshold(this.owner, TriggerKeywordAction.KeywordType.ERUDITION);
        this.description = DESCRIPTIONS[0] + this.amount + "/" + threshold + DESCRIPTIONS[1];
    }
}