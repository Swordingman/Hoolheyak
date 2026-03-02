package Hoolheyak.relics;

import Hoolheyak.HoolheyakMod;
import Hoolheyak.powers.DeconstructionPower;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class TailCareKit extends BaseRelic {
    public static final String ID = HoolheyakMod.makeID("TailCareKit");

    public TailCareKit() {
        super(ID, "TailCareKit", RelicTier.COMMON, LandingSound.FLAT);
    }

    @Override
    public void atBattleStart() {
        this.flash();
        for (AbstractMonster m : AbstractDungeon.getMonsters().monsters) {
            if (!m.isDeadOrEscaped()) {
                addToBot(new ApplyPowerAction(m, AbstractDungeon.player, new DeconstructionPower(m, 7), 7));
            }
        }
    }
}