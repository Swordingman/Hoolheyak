package Hoolheyak.powers.phases;

import Hoolheyak.powers.BasePower;
import Hoolheyak.powers.GravityPower;
import Hoolheyak.HoolheyakMod;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class OppositionPower extends BasePower {
    public static final String POWER_ID = HoolheyakMod.makeID("Opposition");

    public OppositionPower(AbstractCreature owner) {
        super(POWER_ID, PowerType.BUFF, false, owner, -1);
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
        // 【修复点】：使用 addToTop 将重新计算逻辑包装成 Action
        // 确保它在当前这个 RemovePowerAction 彻底结束（对冲被完全移除出列表）后才执行
        AbstractDungeon.actionManager.addToTop(new AbstractGameAction() {
            @Override
            public void update() {
                for (AbstractMonster m : AbstractDungeon.getMonsters().monsters) {
                    if (m != null && !m.isDeadOrEscaped() && m.hasPower(GravityPower.POWER_ID)) {
                        ((GravityPower) m.getPower(GravityPower.POWER_ID)).recalculateGravity();
                    }
                }
                this.isDone = true;
            }
        });
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