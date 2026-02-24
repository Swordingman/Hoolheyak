package Hoolheyak.powers;

import Hoolheyak.HoolheyakMod;
import Hoolheyak.character.Hoolheyak;
import basemod.BaseMod;
import com.megacrit.cardcrawl.core.AbstractCreature;

public class WardrobePower extends BasePower {
    public static final String POWER_ID = HoolheyakMod.makeID("Wardrobe");

    public WardrobePower(AbstractCreature owner, int amount) {
        super(POWER_ID, PowerType.BUFF, false, owner, amount);
    }

    @Override
    public void onInitialApplication() {
        // 直接修改 BaseMod 提供的上限
        BaseMod.MAX_HAND_SIZE += this.amount;
    }

    @Override
    public void stackPower(int stackAmount) {
        super.stackPower(stackAmount);
        BaseMod.MAX_HAND_SIZE += stackAmount;
    }

    @Override
    public void onRemove() {
        // 移除能力时还原上限
        BaseMod.MAX_HAND_SIZE -= this.amount;
    }

    @Override
    public void onVictory() {
        // 战斗胜利时还原上限，防止全局污染带到下一场战斗
        BaseMod.MAX_HAND_SIZE -= this.amount;
    }

    @Override
    public void updateDescription() {
        this.description = DESCRIPTIONS[0] + this.amount + DESCRIPTIONS[1];
    }
}