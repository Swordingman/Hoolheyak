package Hoolheyak.powers.phases;

import Hoolheyak.powers.BasePower;
import Hoolheyak.HoolheyakMod;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;

public class ConjunctionPower extends BasePower {
    public static final String POWER_ID = HoolheyakMod.makeID("Conjunction");

    public ConjunctionPower(AbstractCreature owner) {
        // -1 代表不显示层数（下同）
        super(POWER_ID, PowerType.BUFF, false, owner, -1);
    }

    // 增加你造成的伤害
    @Override
    public float atDamageGive(float damage, DamageInfo.DamageType type) {
        if (type == DamageInfo.DamageType.NORMAL) {
            return damage * 1.5f;
        }
        return damage;
    }

    // 增加你受到的伤害（反向等同于增加所有敌人的伤害）
    @Override
    public float atDamageReceive(float damage, DamageInfo.DamageType type) {
        if (type == DamageInfo.DamageType.NORMAL) {
            return damage * 1.5f;
        }
        return damage;
    }

    @Override
    public void updateDescription() {
        this.description = DESCRIPTIONS[0];
    }
}