package Hoolheyak.powers;

import Hoolheyak.HoolheyakMod;
import Hoolheyak.character.Hoolheyak;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInHandAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.UIStrings;

public class WardrobePower extends BasePower {
    public static final String POWER_ID = HoolheyakMod.makeID("Wardrobe");

    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(HoolheyakMod.makeID("CardModifiers"));

    public static final String EXHAUST_APPEND = uiStrings.TEXT[0];

    // 用于记录是否需要把生成的牌变成 0 费
    public boolean makeZeroCost;

    public WardrobePower(AbstractCreature owner, int amount, boolean makeZeroCost) {
        super(POWER_ID, PowerType.BUFF, false, owner, amount);
        this.makeZeroCost = makeZeroCost;
        updateDescription();
    }

    // 当有一张牌被打出时触发
    @Override
    public void onUseCard(AbstractCard card, UseCardAction action) {
        // 【关键修复】：不仅要颜色不同，还必须明确排除状态牌和诅咒牌！
        if (card.color != Hoolheyak.Meta.CARD_COLOR
                && card.type != AbstractCard.CardType.STATUS
                && card.type != AbstractCard.CardType.CURSE) {

            this.flash(); // 闪烁一下能力图标

            for (int i = 0; i < this.amount; i++) {
                // 从当前角色的卡池里，随机抓取一张牌并复制
                AbstractCard randomCard = AbstractDungeon.returnTrulyRandomCardInCombat().makeCopy();

                // 赋予它消耗属性
                randomCard.exhaust = true;
                randomCard.rawDescription = randomCard.rawDescription + EXHAUST_APPEND;
                randomCard.initializeDescription();

                // 如果能力包含升级效果，强行把它改成 0 费
                if (this.makeZeroCost) {
                    // 对于 X 费牌，原版游戏通常不需要改 cost，但为了保险起见一起写上
                    randomCard.cost = 0;
                    randomCard.costForTurn = 0;
                    randomCard.isCostModified = true;
                }

                // 将这张牌加入手牌队列
                addToBot(new com.megacrit.cardcrawl.actions.common.MakeTempCardInHandAction(randomCard, 1));
            }
        }
    }

    @Override
    public void updateDescription() {
        if (this.makeZeroCost) {
            // "每当你打出一张不属于你职业的牌时，随机将 #b%d 张本职业的牌加入手牌，使其带有 消耗 ，并且在本场战斗中耗能变为 #b0 。"
            this.description = DESCRIPTIONS[0] + this.amount + DESCRIPTIONS[1] + DESCRIPTIONS[2];
        } else {
            // "每当你打出一张不属于你职业的牌时，随机将 #b%d 张本职业的牌加入手牌，使其带有 消耗 。"
            this.description = DESCRIPTIONS[0] + this.amount + DESCRIPTIONS[1];
        }
    }
}