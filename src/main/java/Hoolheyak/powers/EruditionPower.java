package Hoolheyak.powers;

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
    public static final String POWER_ID = Hoolheyak.HoolheyakMod.makeID("Erudition");

    public EruditionPower(AbstractCreature owner, int amount) {
        // 使用 amount 作为计数器，最高为4
        super(POWER_ID, PowerType.BUFF, false, owner, amount);
    }

    @Override
    public void onUseCard(AbstractCard card, UseCardAction action) {
        if (card.hasTag(CustomTags.HOOLHEYAK_VARIABLE)) {
            return;
        }

        if (card.type == AbstractCard.CardType.ATTACK) {
            this.amount++;
            if (this.amount >= 4) {
                InheritedMemoriesPower.onTriggerKeyword(this.owner);
                this.amount = 0; // 重置计数
                flash();

                // 获取解析层数 X
                int x;
                if (this.owner.hasPower(AnalysisPower.POWER_ID)) {
                    x = this.owner.getPower(AnalysisPower.POWER_ID).amount;
                } else {
                    x = 0;
                }

                // 固定重复触发 4 次
                for (int i = 0; i < 4; i++) {
                    addToBot(new AbstractGameAction() {
                        @Override
                        public void update() {
                            // 每次触发都重新索敌，符合“对随机敌人”的尖塔常规多段伤害逻辑
                            AbstractMonster target = AbstractDungeon.getMonsters().getRandomMonster(null, true, AbstractDungeon.cardRandomRng);
                            if (target != null && !target.isDeadOrEscaped()) {
                                // 每次给予 1 层升力
                                addToTop(new ApplyPowerAction(target, owner, new LiftPower(target, owner, 1), 1));
                                // 每次造成 X 点伤害
                                addToTop(new DamageAction(target, new DamageInfo(owner, x, DamageInfo.DamageType.THORNS), AbstractGameAction.AttackEffect.SLASH_DIAGONAL));
                            }
                            this.isDone = true;
                        }
                    });
                }
            }
            updateDescription();
        }
    }

    @Override
    public void updateDescription() {
        this.description = DESCRIPTIONS[0] + this.amount + DESCRIPTIONS[1]; // 根据你的JSON动态拼接
    }
}