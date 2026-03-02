package Hoolheyak.relics;

import Hoolheyak.HoolheyakMod;
import Hoolheyak.character.Hoolheyak;
import Hoolheyak.powers.GravityPower; // 请确认你的重力Power类名是否一致
import Hoolheyak.powers.LiftPower;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.rooms.MonsterRoomBoss;
import com.megacrit.cardcrawl.rooms.MonsterRoomElite;

public class StarryRevelation extends BaseRelic {
    public static final String ID = HoolheyakMod.makeID("StarryRevelation");

    public StarryRevelation() {
        // SPECIAL 代表特殊/事件遗物，不会在普通箱子中掉落
        super(ID, "StarryRevelation", Hoolheyak.Meta.CARD_COLOR, RelicTier.SPECIAL, LandingSound.MAGICAL);
    }

    @Override
    public void atBattleStart() {
        // 判定当前房间是否为 精英房间 或 Boss房间
        boolean isEliteOrBoss = AbstractDungeon.getCurrRoom() instanceof MonsterRoomElite ||
                AbstractDungeon.getCurrRoom() instanceof MonsterRoomBoss;

        if (isEliteOrBoss) {
            this.flash();
            for (AbstractMonster m : AbstractDungeon.getMonsters().monsters) {
                if (!m.isDeadOrEscaped()) {
                    // 【注意】这里我假设“重力”是施加在怪物身上的状态。
                    // 如果你的机制中“重力”是施加在玩家身上的，请把 m.hasPower 换成 AbstractDungeon.player.hasPower
                    int gravityAmt = 0;
                    if (m.hasPower(GravityPower.POWER_ID)) {
                        gravityAmt = m.getPower(GravityPower.POWER_ID).amount;
                    }

                    int liftAmt = gravityAmt / 2;

                    if (liftAmt > 0) {
                        addToBot(new ApplyPowerAction(m, AbstractDungeon.player, new LiftPower(m, AbstractDungeon.player, liftAmt), liftAmt));
                    }
                }
            }
        }
    }
}