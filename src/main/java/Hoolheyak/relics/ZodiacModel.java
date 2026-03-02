package Hoolheyak.relics;

import Hoolheyak.HoolheyakMod;
import Hoolheyak.character.Hoolheyak;
import Hoolheyak.powers.LiftPower;
import Hoolheyak.patches.MonsterIntentPatch; // 引入上面的补丁
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class ZodiacModel extends BaseRelic {
    public static final String ID = HoolheyakMod.makeID("ZodiacModel");

    public ZodiacModel() {
        super(ID, "ZodiacModel", Hoolheyak.Meta.CARD_COLOR, RelicTier.COMMON, LandingSound.CLINK);
    }

    @Override
    public void atTurnStart() {
        boolean triggered = false;

        for (AbstractMonster m : AbstractDungeon.getMonsters().monsters) {
            if (!m.isDeadOrEscaped()) {
                // 读取我们在 Patch 中缓存的上回合意图
                AbstractMonster.Intent lastIntent = MonsterIntentPatch.LastIntentField.lastIntent.get(m);

                // 上回合意图不为空，且与当前意图类型不同
                if (lastIntent != AbstractMonster.Intent.NONE && lastIntent != m.intent) {
                    triggered = true;
                    // 给予升力
                    addToBot(new ApplyPowerAction(m, AbstractDungeon.player, new LiftPower(m, AbstractDungeon.player, 2), 2));
                }
            }
        }

        if (triggered) {
            this.flash();
        }
    }
}