package Hoolheyak.powers;

import Hoolheyak.HoolheyakMod;
import Hoolheyak.character.Hoolheyak;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class BygoneWingsPower extends BasePower {
    public static final String POWER_ID = HoolheyakMod.makeID("BygoneWingsPower");

    public BygoneWingsPower(AbstractCreature owner, int amount) {
        super(POWER_ID, PowerType.BUFF, false, owner, amount);
    }

    @Override
    public void atEndOfTurn(boolean isPlayer) {
        if (isPlayer) {
            boolean flashed = false;
            for (AbstractMonster mo : AbstractDungeon.getCurrRoom().monsters.monsters) {
                // 判断敌人存活且血量不高于一半
                if (!mo.isDeadOrEscaped() && mo.currentHealth <= mo.maxHealth / 2) {
                    if (!flashed) {
                        this.flash();
                        flashed = true;
                    }
                    // 每回合补充失重层数，抵消自然衰减，实现“永久”
                    addToBot(new ApplyPowerAction(mo, this.owner, new WeightlessPower(mo, this.owner, this.amount), this.amount));
                }
            }
        }
    }

    @Override
    public void updateDescription() {
        this.description = DESCRIPTIONS[0] + this.amount + DESCRIPTIONS[1];
    }
}