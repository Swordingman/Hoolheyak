package Hoolheyak.relics;

import Hoolheyak.HoolheyakMod;
import Hoolheyak.character.Hoolheyak;
import Hoolheyak.powers.LiftPower;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class WeatherBalloon extends BaseRelic {
    public static final String ID = HoolheyakMod.makeID("WeatherBalloon");

    public WeatherBalloon() {
        super(ID, "WeatherBalloon", Hoolheyak.Meta.CARD_COLOR, RelicTier.COMMON, LandingSound.MAGICAL);
    }

    public void onEnemyLevitate(AbstractMonster target) {
        this.flash();
        addToBot(new ApplyPowerAction(target, AbstractDungeon.player, new LiftPower(target, AbstractDungeon.player, 5), 5));
    }
}