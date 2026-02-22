package Hoolheyak.powers;

import Hoolheyak.HoolheyakMod;
import com.megacrit.cardcrawl.actions.common.ObtainPotionAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.potions.AttackPotion;
import com.megacrit.cardcrawl.potions.PowerPotion;
import com.megacrit.cardcrawl.potions.SkillPotion;

public class DistilledTimePower extends BasePower {
    public static final String POWER_ID = HoolheyakMod.makeID("DistilledTimePower");

    public DistilledTimePower(AbstractCreature owner, int amount) {
        super(POWER_ID, PowerType.BUFF, false, owner, amount);
    }

    @Override
    public void updateDescription() {
        this.description = DESCRIPTIONS[0] + this.amount + DESCRIPTIONS[1];
    }

    @Override
    public void atStartOfTurnPostDraw() {
        if (!AbstractDungeon.getMonsters().areMonstersBasicallyDead()) {
            this.flash();

            for (int i = 0; i < this.amount; i++) {
                int roll = AbstractDungeon.cardRandomRng.random(2);
                AbstractPotion p = null;
                switch (roll) {
                    case 0:
                        p = new AttackPotion();
                        break;
                    case 1:
                        p = new SkillPotion();
                        break;
                    case 2:
                        p = new PowerPotion();
                        break;
                }

                if (p != null) {
                    addToBot(new ObtainPotionAction(p));
                }
            }
        }
    }
}