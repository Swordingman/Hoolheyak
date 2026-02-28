package Hoolheyak.powers;

import Hoolheyak.HoolheyakMod;
import Hoolheyak.powers.phases.OppositionPower;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

public class GravityPower extends BasePower {
    public static final String POWER_ID = HoolheyakMod.makeID("Gravity");

    private int lastMaxHealth;

    public GravityPower(AbstractCreature owner) {
        super(POWER_ID, PowerType.BUFF, false, owner, Math.max(1, owner.maxHealth / 10));
    }

    // 【新增核心方法】：动态计算真实重力
    public void recalculateGravity() {
        // 获取最原始的基础重力
        int baseGravity = Math.max(1, this.owner.maxHealth / 10);

        if (AbstractDungeon.player.hasPower(OppositionPower.POWER_ID)) {
            this.amount = baseGravity / 2;
        } else {
            this.amount = baseGravity;
        }

        // 刷新UI数字
        updateDescription();

        if (this.owner.hasPower(LiftPower.POWER_ID)) {
            ((LiftPower) this.owner.getPower(LiftPower.POWER_ID)).checkLevitate();
        }
    }

    @Override
    public void onInitialApplication() {
        // 第一次挂上能力时，记录当前的最大生命值
        this.lastMaxHealth = this.owner.maxHealth;
        recalculateGravity();
    }

    // 【新增核心方法】：利用底层刷新钩子，实时监控生命上限
    @Override
    public void update(int slot) {
        super.update(slot); // 必须调用 super

        // 如果拥有者存在，并且现在的最大生命值 和 我们记录的不一样了
        if (this.owner != null && this.owner.maxHealth != this.lastMaxHealth) {
            this.lastMaxHealth = this.owner.maxHealth; // 更新记录
            recalculateGravity(); // 重新计算并刷新UI！
        }
    }

    @Override
    public void updateDescription() {
        this.description = DESCRIPTIONS[0] + this.amount + DESCRIPTIONS[1];
    }
}