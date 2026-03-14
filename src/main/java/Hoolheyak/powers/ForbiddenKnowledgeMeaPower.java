package Hoolheyak.powers;

import Hoolheyak.HoolheyakMod;
import com.megacrit.cardcrawl.actions.common.DamageAllEnemiesAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;

public class ForbiddenKnowledgeMeaPower extends BasePower {
    public static final String POWER_ID = "HoolheyakMod:ForbiddenKnowledgeMea";

    public ForbiddenKnowledgeMeaPower(AbstractCreature owner, int amount) {
        super(POWER_ID, PowerType.BUFF, false, owner, amount);
    }

    // 自定义方法：当逶迤被触发时调用
    public void onTriggerMeander() {
        this.flash();
        // 造成无视护甲的群体伤害（这里用了荆棘伤害类型避免受力量加成，如果你希望受力量加成，可以改成 NORMAL）
        addToBot(new DamageAllEnemiesAction(null, DamageInfo.createDamageMatrix(this.amount, true),
                DamageInfo.DamageType.THORNS, com.megacrit.cardcrawl.actions.AbstractGameAction.AttackEffect.FIRE));
    }

    @Override
    public void updateDescription() {
        this.description = DESCRIPTIONS[0] + this.amount + DESCRIPTIONS[1]; // 记得在本地化文件里改成：当你触发逶迤时，对所有敌人造成 #b 点伤害。
    }
}