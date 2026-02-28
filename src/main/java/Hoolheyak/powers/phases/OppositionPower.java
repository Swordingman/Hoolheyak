package Hoolheyak.powers.phases;

import Hoolheyak.powers.BasePower;
import Hoolheyak.powers.GravityPower;
import Hoolheyak.HoolheyakMod;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class OppositionPower extends BasePower {
    public static final String POWER_ID = HoolheyakMod.makeID("Opposition");

    public OppositionPower(AbstractCreature owner) {
        super(POWER_ID, PowerType.DEBUFF, false, owner, -1);
    }

    @Override
    public void onInitialApplication() {
        // 对冲降临时，让全场怪物重新计算重力
        for (AbstractMonster m : AbstractDungeon.getMonsters().monsters) {
            if (!m.isDeadOrEscaped() && m.hasPower(GravityPower.POWER_ID)) {
                ((GravityPower) m.getPower(GravityPower.POWER_ID)).recalculateGravity();
            }
        }
    }

    @Override
    public void onRemove() {
        // 对冲被顶掉或战斗结束时，让全场怪物恢复重力
        for (AbstractMonster m : AbstractDungeon.getMonsters().monsters) {
            if (!m.isDeadOrEscaped() && m.hasPower(GravityPower.POWER_ID)) {
                ((GravityPower) m.getPower(GravityPower.POWER_ID)).recalculateGravity();
            }
        }
    }

    @Override
    public float modifyBlock(float blockAmount) {
        if (blockAmount > 0) {
            this.flash();
        }
        return 0f; // 你无法获得格挡
    }

    @Override
    public void updateDescription() {
        this.description = DESCRIPTIONS[0];
    }
}