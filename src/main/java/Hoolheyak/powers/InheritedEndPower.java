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
        // 我们只关心攻击牌和技能牌的交替
        if (card.type == AbstractCard.CardType.ATTACK || card.type == AbstractCard.CardType.SKILL) {

            // 如果之前已经记录了第一张牌，且当前打出的牌与它类型不同，则视为【交替成功】
            if (this.firstCardType != null && this.firstCardType != card.type) {
                this.flash();
                addToBot(new GainEnergyAction(1));
                addToBot(new DrawCardAction(this.owner, 1));

                // 【核心修改】：触发奖励后，立刻重置计数。
                // 这样下一张打出的牌会被视为全新一对的起点。
                this.firstCardType = null;
            } else {
                // 如果当前没有记录，或者玩家连续打出了相同的牌（比如 攻击->攻击），
                // 则直接把最新打出的这张牌记为配对的起点。
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
        this.description = DESCRIPTIONS[0];
    }
}