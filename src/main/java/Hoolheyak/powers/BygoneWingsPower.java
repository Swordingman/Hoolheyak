package Hoolheyak.powers;

import Hoolheyak.HoolheyakMod;
import Hoolheyak.character.Hoolheyak;
import com.badlogic.gdx.graphics.Color;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

import java.util.ArrayList;

public class BygoneWingsPower extends BasePower {
    public static final String POWER_ID = HoolheyakMod.makeID("BygoneWingsPower");

    public BygoneWingsPower(AbstractCreature owner, int amount) {
        super(POWER_ID, PowerType.BUFF, false, owner, amount);
    }

    @Override
    public void updateDescription() {
        this.description = DESCRIPTIONS[0] + this.amount + DESCRIPTIONS[1];
    }

    @Override
    public void onUseCard(AbstractCard card, UseCardAction action) {
        // 判定：只要不是本职业卡，且不是诅咒或状态牌
        if (card.color != AbstractDungeon.player.getCardColor() &&
                card.type != AbstractCard.CardType.CURSE &&
                card.type != AbstractCard.CardType.STATUS) {

            this.flash(); // 触发时闪烁图标

            addToBot(new AbstractGameAction() {
                @Override
                public void update() {
                    // 【核心修复】必须写 BygoneWingsPower.this.amount，防止读取到 Action 自己的 0！
                    for (int i = 0; i < BygoneWingsPower.this.amount; i++) {
                        ArrayList<AbstractCard> validCards = new ArrayList<>();

                        for (AbstractCard c : AbstractDungeon.player.hand.group) {
                            // 筛选：只找当前费用大于 0 的牌
                            if (c.costForTurn > 0) {
                                validCards.add(c);
                            }
                        }

                        if (!validCards.isEmpty()) {
                            AbstractCard target = validCards.get(AbstractDungeon.cardRandomRng.random(validCards.size() - 1));

                            // 减费逻辑
                            target.setCostForTurn(target.costForTurn - 1);
                            target.cost = target.costForTurn;
                            target.isCostModified = true;

                            target.superFlash(Color.GOLD.cpy());
                        }
                    }

                    // 强制刷新手牌光效和数值状态
                    AbstractDungeon.player.hand.glowCheck();

                    this.isDone = true;
                }
            });
        }
    }
}