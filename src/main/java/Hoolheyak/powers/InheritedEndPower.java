package Hoolheyak.powers;

import Hoolheyak.HoolheyakMod;
import Hoolheyak.character.Hoolheyak;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.actions.common.GainEnergyAction;
import com.megacrit.cardcrawl.actions.common.RemoveSpecificPowerAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.AbstractCreature;

public class InheritedEndPower extends BasePower {
    public static final String POWER_ID = HoolheyakMod.makeID("InheritedEndPower");

    // 用于记录当前正在等待配对的第一张牌的类型
    private AbstractCard.CardType firstCardType = null;

    public InheritedEndPower(AbstractCreature owner, int amount) {
        super(POWER_ID, PowerType.BUFF, true, owner, amount);
    }

    @Override
    public void onUseCard(AbstractCard card, UseCardAction action) {
        if (card.type == AbstractCard.CardType.ATTACK || card.type == AbstractCard.CardType.SKILL) {

            if (this.firstCardType != null && this.firstCardType != card.type) {
                this.flash();

                // 【核心修复】：将硬编码的 1 替换为 this.amount
                addToBot(new GainEnergyAction(this.amount));
                addToBot(new DrawCardAction(this.owner, this.amount));

                this.firstCardType = null;
            } else {
                this.firstCardType = card.type;
            }
        }
    }

    @Override
    public void atEndOfTurn(boolean isPlayer) {
        // 回合结束时移除此能力
        if (isPlayer) {
            addToBot(new RemoveSpecificPowerAction(this.owner, this.owner, this));
        }
    }

    @Override
    public void updateDescription() {
        // 建议在 JSON 本地化文本里这样写：
        // DESCRIPTIONS[0] -> "每当你交替打出攻击牌与技能牌时，获得 "
        // DESCRIPTIONS[1] -> " 点能量并抽 "
        // DESCRIPTIONS[2] -> " 张牌。"
        this.description = DESCRIPTIONS[0] + this.amount + DESCRIPTIONS[1] + this.amount + DESCRIPTIONS[2];
    }
}