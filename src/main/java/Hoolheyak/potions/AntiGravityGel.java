package Hoolheyak.potions;

import Hoolheyak.HoolheyakMod;
import Hoolheyak.powers.LiftPower;
import Hoolheyak.powers.WeightlessPower;
import com.badlogic.gdx.graphics.Color;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

public class AntiGravityGel extends BasePotion {
    public static final String POTION_ID = HoolheyakMod.makeID("AntiGravityGel");

    public AntiGravityGel() {
        super(
                POTION_ID,
                1, // 基础效力
                PotionRarity.COMMON,
                PotionSize.JAR,
                Color.SKY.cpy(),
                Color.LIME,
                null
        );
        this.isThrown = true;       // 需要丢向敌人
        this.targetRequired = true; // 需要目标
    }

    @Override
    public String getDescription() {
        return DESCRIPTIONS[0] + (this.potency * 15) + DESCRIPTIONS[1] + this.potency + DESCRIPTIONS[2];
    }

    @Override
    public void use(AbstractCreature target) {
        AbstractCreature p = AbstractDungeon.player;

        if (target != null && !target.isDeadOrEscaped()) {
            // 先给失重，再给升力，这样如果失重有联动效果可以立即生效
            addToBot(new ApplyPowerAction(target, p, new WeightlessPower(target, p, this.potency), this.potency));
            addToBot(new ApplyPowerAction(target, p, new LiftPower(target, p, this.potency * 15), this.potency * 15));
        }
    }
}