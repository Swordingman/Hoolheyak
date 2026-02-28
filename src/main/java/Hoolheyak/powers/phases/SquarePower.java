package Hoolheyak.powers.phases;

import Hoolheyak.powers.BasePower;
import Hoolheyak.powers.LiftPower;
import Hoolheyak.HoolheyakMod;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class SquarePower extends BasePower {
    public static final String POWER_ID = HoolheyakMod.makeID("Square");

    public SquarePower(AbstractCreature owner) {
        super(POWER_ID, PowerType.BUFF, false, owner, -1);
    }

    @Override
    public void onUseCard(AbstractCard card, UseCardAction action) {
        this.flash();

        // 受到 1 点 HP_LOSS（无视格挡）的伤害
        addToBot(new DamageAction(this.owner, new DamageInfo(this.owner, 1, DamageInfo.DamageType.HP_LOSS)));

        // 遍历存活的敌人，给每个人拍 1 层升力
        for (AbstractMonster m : AbstractDungeon.getMonsters().monsters) {
            if (!m.isDeadOrEscaped()) {
                addToBot(new ApplyPowerAction(m, this.owner, new LiftPower(m, this.owner, 1), 1));
            }
        }
    }

    @Override
    public void updateDescription() {
        this.description = DESCRIPTIONS[0];
    }
}