package Hoolheyak.actions;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.vfx.combat.FlashAtkImgEffect;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

public class SeparationAction extends AbstractGameAction {
    private DamageInfo info;

    public SeparationAction(AbstractMonster target, DamageInfo info) {
        this.info = info;
        this.target = target;
        this.actionType = ActionType.DAMAGE;
        this.duration = Settings.ACTION_DUR_FAST;
    }

    @Override
    public void update() {
        if (this.duration == Settings.ACTION_DUR_FAST && this.target != null) {
            AbstractDungeon.effectList.add(new FlashAtkImgEffect(this.target.hb.cX, this.target.hb.cY, AttackEffect.SLASH_HEAVY));

            // 对目标造成伤害
            this.target.damage(this.info);

            // 获取这次攻击实际造成的未格挡伤害
            int actualDamageDealt = this.target.lastDamageTaken;

            // 如果打出了真实伤害，扣除等量的最大生命值
            if (actualDamageDealt > 0 && !this.target.isDeadOrEscaped()) {
                ((AbstractMonster) this.target).decreaseMaxHealth(actualDamageDealt);
            }

            if (AbstractDungeon.getCurrRoom().monsters.areMonstersBasicallyDead()) {
                AbstractDungeon.actionManager.clearPostCombatActions();
            }
        }
        tickDuration();
    }
}