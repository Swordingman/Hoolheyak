package Hoolheyak.powers;

import Hoolheyak.HoolheyakMod;
import Hoolheyak.character.Hoolheyak;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInHandAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

public class ByproductPower extends BasePower {
    public static final String POWER_ID = HoolheyakMod.makeID("Byproduct");
    private boolean isUpgraded; // 记录是否给予升级牌

    public ByproductPower(AbstractCreature owner, int amount, boolean isUpgraded) {
        super(POWER_ID, PowerType.BUFF, false, owner, amount);
        this.isUpgraded = isUpgraded;
        updateDescription();
    }

    // 由 VariableChoiceCard 调用的触发器
    public void onVariableTriggered() {
        this.flash();
        for (int i = 0; i < this.amount; i++) {
            AbstractCard randomCard = AbstractDungeon.returnTrulyRandomCardInCombat().makeCopy();
            if (this.isUpgraded) {
                randomCard.upgrade();
            }
            addToBot(new MakeTempCardInHandAction(randomCard, 1));
        }
    }

    @Override
    public void stackPower(int stackAmount) {
        super.stackPower(stackAmount);
        // 如果再次打出的是升级版，覆盖为升级版状态
        // 如果想把逻辑写得更复杂（比如一层普通一层升级），可以使用多个布尔值，这里采用覆盖制
    }

    @Override
    public void updateDescription() {
        if (this.isUpgraded) {
            this.description = DESCRIPTIONS[0] + this.amount + DESCRIPTIONS[2];
        } else {
            this.description = DESCRIPTIONS[0] + this.amount + DESCRIPTIONS[1];
        }
    }
}