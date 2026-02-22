package Hoolheyak.powers;

import Hoolheyak.cards.InheritedMemories;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import Hoolheyak.util.CustomTags;

public class MeanderPower extends BasePower {
    public static final String POWER_ID = Hoolheyak.HoolheyakMod.makeID("Meander");

    public MeanderPower(AbstractCreature owner, int amount) {
        // 使用 amount 作为计数器，最高为4
        super(POWER_ID, PowerType.BUFF, false, owner, amount);
    }

    @Override
    public void onUseCard(AbstractCard card, UseCardAction action) {
        if (card.hasTag(CustomTags.HOOLHEYAK_VARIABLE)) {
            return;
        }

        if (card.type == AbstractCard.CardType.SKILL) {
            this.amount++;
            if (this.amount >= 4) {
                InheritedMemoriesPower.onTriggerKeyword(this.owner);
                this.amount = 0; // 重置计数
                flash();

                // 获取解析层数 X
                int x = 0;
                if (this.owner.hasPower(AnalysisPower.POWER_ID)) {
                    x = this.owner.getPower(AnalysisPower.POWER_ID).amount;
                }

                // 触发 X 次效果
                for (int i = 0; i < x; i++) {
                    addToBot(new AbstractGameAction() {
                        @Override
                        public void update() {
                            AbstractMonster target = AbstractDungeon.getMonsters().getRandomMonster(null, true, AbstractDungeon.cardRandomRng);
                            if (target != null && !target.isDeadOrEscaped()) {
                                // 造成4点伤害
                                addToTop(new ApplyPowerAction(target, owner, new LiftPower(target, owner, 2), 2));
                                addToTop(new DamageAction(target, new DamageInfo(owner, 4, DamageInfo.DamageType.THORNS), AttackEffect.SLASH_DIAGONAL));
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
        this.description = DESCRIPTIONS[0] + this.amount + DESCRIPTIONS[1]; // 自行匹配你的JSON文本
    }
}