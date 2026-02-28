package Hoolheyak.patches;

import Hoolheyak.powers.DeconstructionPower;
import com.evacipated.cardcrawl.modthespire.lib.ByRef;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.actions.common.RemoveSpecificPowerAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

@SpirePatch(clz = AbstractCreature.class, method = "addBlock")
public class DeconstructionPatch {

    @SpirePrefixPatch
    public static void Prefix(AbstractCreature __instance, @ByRef int[] blockAmount) {
        // 如果将要获得格挡的单位身上有“解构”
        if (__instance.hasPower(DeconstructionPower.POWER_ID)) {
            DeconstructionPower power = (DeconstructionPower) __instance.getPower(DeconstructionPower.POWER_ID);

            // 如果确实是在加格挡，且解构层数 > 0
            if (blockAmount[0] > 0 && power.amount > 0) {
                power.flash();

                // 计算要抵消多少
                int blockToReduce = Math.min(blockAmount[0], power.amount);

                // 扣除格挡值
                blockAmount[0] -= blockToReduce;

                // 扣除层数
                power.amount -= blockToReduce;

                if (power.amount <= 0) {
                    AbstractDungeon.actionManager.addToTop(new RemoveSpecificPowerAction(__instance, __instance, power));
                } else {
                    power.updateDescription();
                }
            }
        }
    }
}