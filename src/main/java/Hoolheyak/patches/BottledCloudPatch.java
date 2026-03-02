package Hoolheyak.patches;

import Hoolheyak.relics.BottledCloud;
import com.evacipated.cardcrawl.modthespire.lib.ByRef;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.DexterityPower;

@SpirePatch(
        clz = ApplyPowerAction.class,
        method = SpirePatch.CONSTRUCTOR,
        paramtypez = {
                AbstractCreature.class,
                AbstractCreature.class,
                AbstractPower.class,
                int.class,
                boolean.class,
                AbstractGameAction.AttackEffect.class
        }
)
public class BottledCloudPatch {
    @SpirePrefixPatch
    public static void Prefix(ApplyPowerAction __instance, AbstractCreature target, AbstractCreature source, AbstractPower powerToApply, @ByRef int[] stackAmount, boolean isFast, AbstractGameAction.AttackEffect effect) {
        // 检查是否是给予玩家敏捷，且层数大于 0
        if (target != null && target.isPlayer && powerToApply.ID.equals(DexterityPower.POWER_ID) && stackAmount[0] > 0) {
            // 检查玩家是否有瓶装云朵遗物
            if (AbstractDungeon.player != null && AbstractDungeon.player.hasRelic(BottledCloud.ID)) {
                // 遗物闪烁提示
                AbstractDungeon.player.getRelic(BottledCloud.ID).flash();

                // 将准备施加的层数 + 1
                stackAmount[0] += 1;
                powerToApply.amount += 1;
            }
        }
    }
}