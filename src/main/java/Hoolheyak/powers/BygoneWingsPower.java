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
        // 判定条件：卡牌颜色不等于当前角色的颜色，且不是无色、诅咒或状态卡
        if (card.color != AbstractDungeon.player.getCardColor() &&
                card.color != AbstractCard.CardColor.COLORLESS &&
                card.color != AbstractCard.CardColor.CURSE &&
                card.type != AbstractCard.CardType.CURSE &&
                card.type != AbstractCard.CardType.STATUS) {

            this.flash(); // 触发时闪烁能力图标

            addToBot(new AbstractGameAction() {
                @Override
                public void update() {
                    // 根据能力的层数决定减费的次数（比如2层就随机减2次）
                    for (int i = 0; i < amount; i++) {
                        ArrayList<AbstractCard> validCards = new ArrayList<>();

                        // 筛选出手牌中当前费用 > 0 的卡牌
                        for (AbstractCard c : AbstractDungeon.player.hand.group) {
                            if (c.costForTurn > 0) {
                                validCards.add(c);
                            }
                        }

                        if (!validCards.isEmpty()) {
                            // 随机挑选一张有效卡牌
                            AbstractCard target = validCards.get(AbstractDungeon.cardRandomRng.random(validCards.size() - 1));

                            // 战斗内永久减费 (如果只需要本回合减费，请使用 target.setCostForTurn(target.costForTurn - 1); )
                            target.modifyCostForCombat(-1);

                            // 卡牌闪烁金光提示玩家
                            target.superFlash(Color.GOLD.cpy());
                        }
                    }
                    this.isDone = true;
                }
            });
        }
    }
}