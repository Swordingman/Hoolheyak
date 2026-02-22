package Hoolheyak.powers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.RemoveAllBlockAction;
import com.megacrit.cardcrawl.actions.common.RemoveSpecificPowerAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.Settings;

public class LevitatePower extends BasePower {
    public static final String POWER_ID = Hoolheyak.HoolheyakMod.makeID("Levitate");

    public LevitatePower(AbstractCreature owner, AbstractCreature source) {
        super(POWER_ID, PowerType.DEBUFF, false, owner, source, 1);
    }

    private float floatTimer = 0f;
    private float baseY;
    @Override
    public void onInitialApplication() {
        // 进入此状态时，移除所有格挡
        addToBot(new RemoveAllBlockAction(this.owner, this.source));

        baseY = owner.drawY;
    }

    @Override
    public float atDamageReceive(float damage, DamageInfo.DamageType type) {
        // 受到伤害增加50%
        if (type == DamageInfo.DamageType.NORMAL) {
            return damage * 1.5f;
        }
        return damage;
    }

    @Override
    public float atDamageGive(float damage, DamageInfo.DamageType type) {
        // 造成的伤害减少70%
        if (type == DamageInfo.DamageType.NORMAL) {
            return damage * 0.3f;
        }
        return damage;
    }

    @Override
    public float modifyBlock(float blockAmount) {
        // 获得的格挡减少70%
        return blockAmount * 0.3f;
    }

    @Override
    public void atEndOfRound() {
        // 目标的回合结束时，解除此状态并受到伤害
        int gravityAmt = this.owner.hasPower(GravityPower.POWER_ID) ? this.owner.getPower(GravityPower.POWER_ID).amount : 0;
        int remainingLift = this.owner.hasPower(LiftPower.POWER_ID) ? this.owner.getPower(LiftPower.POWER_ID).amount : 0;

        int damageAmt = (gravityAmt + remainingLift) * 2;

        flash();

        // 随后移除所有升力和浮空状态
        addToBot(new RemoveSpecificPowerAction(this.owner, this.source, this));
        if (this.owner.hasPower(LiftPower.POWER_ID)) {
            addToBot(new RemoveSpecificPowerAction(this.owner, this.source, LiftPower.POWER_ID));
        }

        // 受到真实伤害 (或荆棘伤害类型，避免被力量或易伤再次修饰)
        addToBot(new DamageAction(this.owner, new DamageInfo(this.source, damageAmt, DamageInfo.DamageType.HP_LOSS)));
    }

    @Override
    public void update(int slot) {
        super.update(slot);

        floatTimer += Gdx.graphics.getDeltaTime();

        float offset = MathUtils.sin(floatTimer * 2f) * 10f * Settings.scale;

        owner.drawY = baseY + 120f * Settings.scale + offset;
    }

    @Override
    public void onRemove() {
        owner.drawY = baseY;
    }

    @Override
    public void updateDescription() {
        this.description = DESCRIPTIONS[0];
    }
}